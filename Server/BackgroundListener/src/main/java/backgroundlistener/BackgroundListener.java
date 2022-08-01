package backgroundlistener;

import backgroundlistener.objects.Configuration;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class BackgroundListener {
    private static final Configuration configuration = new Configuration();
    
    public static void main(String[] args) throws IOException {
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
        try (Connection databaseConnection = DriverManager.getConnection(configuration.getUrl(), configuration.getUsername(), configuration.getPassword())) {
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
