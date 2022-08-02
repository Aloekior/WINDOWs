package backgroundrunner;

import backgroundrunner.objects.RunnerConfiguration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class BackgroundRunner {
    private static final RunnerConfiguration settings = new RunnerConfiguration();
    
    private BackgroundRunner() {}
    
    public static void serverListener() throws IOException {
        boolean run = true;
        int serverPort = 57335;
        
        while (run) {
            try (ServerSocket socket = new ServerSocket(serverPort)) {
                Socket sensor = socket.accept();

                BufferedReader sensorInput = new BufferedReader(new InputStreamReader(sensor.getInputStream()));

                String sensorMacAddress = sensorInput.readLine();
                String sensorToken = sensorInput.readLine();
                int sensorState = Integer.parseInt(sensorInput.readLine());

                sendOKToSensor(sensor);

                sensorInput.close();
                run = checkExit(sensorMacAddress);
                if (run) {
                    updateSensorState(sensorMacAddress, sensorToken, sensorState);
                }
            }
        }
    }

    private static boolean checkExit(String sensorMacAddress) {
        return !sensorMacAddress.equals("exitListener");
    }

    private static void sendOKToSensor(Socket sensor) throws IOException {
        PrintWriter printWriter = new PrintWriter(sensor.getOutputStream());
        printWriter.println("OK");
        printWriter.flush();
    }

    private static void updateSensorState(String macAddress, String token, int state) {
        try (Connection databaseConnection = DriverManager.getConnection(settings.getUrl(), settings.getUsername(), settings.getPassword())) {
            String query = "CALL windows.updateSensorState('" + macAddress + "', '" + token + "', " + state + ")";
            try (Statement call = databaseConnection.createStatement()) {
                call.executeQuery(query);
            }
        } catch (SQLException e) {
            System.out.println("Database Update failed for '" + macAddress +"'!" +
                    "\nSQL Error: " + e.getErrorCode() +" " + e.getSQLState());
            e.printStackTrace();
        }
    }
}
