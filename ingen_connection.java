import com.fazecast.jSerialComm.SerialPort;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;

public class Main {

    // Database credentials
    private static final String DB_URL = "jdbc:mysql://localhost:3306/sensor_data";
    private static final String USER = "root"; // replace with your MySQL username
    private static final String PASS = "Joeman339617!?"; // replace with your MySQL password

    // Serial port settings
    private static final String PORT_NAME = "COM3"; // replace with your Arduino's COM port
    private static final int BAUD_RATE = 9600;

    public static void main(String[] args) {
        // Initialize serial port
        SerialPort serialPort = SerialPort.getCommPort(PORT_NAME);
        serialPort.setBaudRate(BAUD_RATE);
        serialPort.openPort();

        // Connect to database
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            System.out.println("Connected to database");

            // Create a prepared statement for inserting data
            String sql = "INSERT INTO sensor_readings (temperature, humidity) VALUES (?, ?)";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);

            // Read data from Arduino
            try (InputStream inputStream = serialPort.getInputStream();
                 Scanner scanner = new Scanner(inputStream)) {

                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    String[] data = line.split(",");

                    // Check if the data has the expected format
                    if (data.length == 2) {
                        try {
                            float temperature = Float.parseFloat(data[0]);
                            float humidity = Float.parseFloat(data[1]);

                            // Insert data into database
                            preparedStatement.setFloat(1, temperature);
                            preparedStatement.setFloat(2, humidity);
                            preparedStatement.executeUpdate();

                            System.out.println("Data inserted: Temp = " + temperature + "°C, Humidity = " + humidity + "%");
                        } catch (NumberFormatException e) {
                            System.err.println("Error parsing data: " + line);
                        }
                    } else {
                        System.err.println("Unexpected data format: " + line);
                    }
                }
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace(); // Ideally use proper logging here
        } finally {
            serialPort.closePort();
        }
    }
}
