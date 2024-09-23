import com.fazecast.jSerialComm.SerialPort;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Main {

    // MySQL connection details
    private static final String DB_URL = "jdbc:mysql://localhost:3306/sensor_data";
    private static final String USER = "root";
    private static final String PASS = "K2TYRWgvK7VYGi&";

    public static void main(String[] args) {
        // List all available COM ports to check visibility
        SerialPort[] ports = SerialPort.getCommPorts();
        System.out.println("Available ports:");
        for (SerialPort port : ports) {
            System.out.println(" - " + port.getSystemPortName());
        }

        // Find the specific COM5 port
        SerialPort comPort = SerialPort.getCommPort("COM5");

        // Check if the port is available and open it
        if (comPort == null) {
            System.err.println("COM5 not found or is currently unavailable.");
            return;
        }

        // Attempt to open the port and check if successful
        if (!comPort.openPort()) {
            System.err.println("Failed to open COM5. Check if it's already in use.");
            return;
        } else {
            System.out.println("COM5 opened successfully.");
        }

        // Set baud rate for communication
        comPort.setBaudRate(9600);

        // Connect to MySQL database
        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASS)) {
            System.out.println("Connected to MySQL database");

            // Continuously read from Arduino and log to MySQL
            while (true) {
                if (comPort.bytesAvailable() > 0) {
                    // Read data from Arduino
                    byte[] readBuffer = new byte[comPort.bytesAvailable()];
                    comPort.readBytes(readBuffer, readBuffer.length);
                    String data = new String(readBuffer).trim();

                    // Print received data
                    System.out.println("Received from Arduino: " + data);

                    // Parse the received data
                    if (data.startsWith("Temperature")) {
                        String[] parts = data.split(",");
                        String tempPart = parts[0].split(":")[1].trim().replace(" °C", "");
                        String humidityPart = parts[1].split(":")[1].trim().replace(" %", "");

                        // Debug output for parsed values
                        System.out.println("Parsed Temperature: " + tempPart);
                        System.out.println("Parsed Humidity: " + humidityPart);

                        // Convert to float
                        try {
                            float temperature = Float.parseFloat(tempPart);
                            float humidity = Float.parseFloat(humidityPart);

                            // Print the values before inserting into the database
                            System.out.println("Temperature value to insert: " + temperature);
                            System.out.println("Humidity value to insert: " + humidity);

                            // Insert the data into MySQL
                            insertSensorData(connection, "temperature", temperature);
                            insertSensorData(connection, "humidity", humidity);
                        } catch (NumberFormatException e) {
                            System.err.println("Error parsing number: " + e.getMessage());
                        }
                    }
                }
                Thread.sleep(2000);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Thread interrupted: " + e.getMessage());
        } finally {
            comPort.closePort();
            System.out.println("COM5 closed.");
        }
    }

    // Method to insert data into MySQL
    private static void insertSensorData(Connection connection, String sensor, float value) {
        String query = "INSERT INTO sensor_data (sensor, value) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, sensor);
            stmt.setFloat(2, value);
            stmt.executeUpdate();
            System.out.println(sensor + " data inserted into MySQL: " + value);
        } catch (SQLException e) {
            System.err.println("Error inserting data: " + e.getMessage());
        }
    }
}
