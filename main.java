import com.fazecast.jSerialComm.SerialPort;
import java.sql.*;

public class Main {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/sensor_data?serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "K2TYRWgvK7VYGi&";
    private static Float temperature, humidity;

    public static void main(String[] args) {
        SerialPort comPort = SerialPort.getCommPort("COM5");
        if (comPort == null || !comPort.openPort()) {
            System.err.println("Error: Could not open COM5.");
            return;
        }
        comPort.setBaudRate(9600);
        System.out.println("Successfully opened COM5.");

        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASS)) {
            StringBuilder dataBuffer = new StringBuilder();
            while (true) {
                if (comPort.bytesAvailable() > 0) {
                    byte[] readBuffer = new byte[comPort.bytesAvailable()];
                    int numBytesRead = comPort.readBytes(readBuffer, readBuffer.length);
                    dataBuffer.append(new String(readBuffer, 0, numBytesRead));

                    while (dataBuffer.indexOf("\n") != -1) {
                        processMessage(dataBuffer.substring(0, dataBuffer.indexOf("\n")).trim());
                        dataBuffer.delete(0, dataBuffer.indexOf("\n") + 1);

                        if (temperature != null && humidity != null) {
                            insertSensorData(connection, temperature, humidity);
                            temperature = humidity = null;
                        }
                    }
                }
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (comPort.isOpen()) comPort.closePort();
            System.out.println("COM5 port closed.");
        }
    }

    // Method to process each message and set temperature or humidity
    private static void processMessage(String message) {
        try {
            // Trim and remove extra spaces before processing
            message = message.replaceAll("\\s+", "").toLowerCase();

            if (message.startsWith("temperatur=")) {
                temperature = Float.parseFloat(message.replace("temperatur=", "").replace("c", ""));
                System.out.println("Temperature set to: " + temperature);
            } else if (message.startsWith("luftfuktighet=")) {
                humidity = Float.parseFloat(message.replace("luftfuktighet=", "").replace("%", ""));
                System.out.println("Humidity set to: " + humidity);
            } else {
                System.err.println("Unknown message format: " + message);
            }
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
        }
    }

    private static void insertSensorData(Connection connection, float temperature, float humidity) {
        try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO sensor_readings (temperature, humidity) VALUES (?, ?)")) {
            stmt.setFloat(1, temperature);
            stmt.setFloat(2, humidity);
            System.out.println("Inserting data: Temperature: " + temperature + " °C, Humidity: " + humidity + " %.");
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error inserting data: " + e.getMessage());
        }
    }
}
