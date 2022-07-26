package backgroundListener;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class BackgroundListener {
    public static void main(String[] args) throws IOException {
        while (true) {
            int serverPort = 57335;
            
            ServerSocket socket = new ServerSocket(serverPort);
            Socket sensor = socket.accept();
    
            BufferedReader sensorInput = new BufferedReader(new InputStreamReader(sensor.getInputStream()));
    
            String sensorMacAddress = sensorInput.readLine();
            String sensorToken = sensorInput.readLine();
            int sensorState = Integer.parseInt(sensorInput.readLine());
            
            sendOKToSensor(sensor);
            
            sensorInput.close();
            socket.close();
            // spawn new socket thread here ?
            
            updateSensorState(sensorMacAddress, sensorToken, sensorState);
            if (sensorMacAddress.equals("quitThisRuntime")) {
                break;
            }
        }
    }
    
    private static void sendOKToSensor(Socket sensor) throws IOException {
        PrintWriter printWriter = new PrintWriter(sensor.getOutputStream());
        printWriter.println("OK");
        printWriter.flush();
    }

    private static void updateSensorState(String macAddress, String token, int state) {
        String url = "jdbc:mysql://10.37.129.4:3306";
        try (Connection databaseConnection = DriverManager.getConnection(url, "serverListener", "a8aKJFAL8%lo113ZZ&Bvm12g_$1!")) {
            String query = "CALL windows.updateSensorState('" + macAddress + "', '" + token + "', " + state + ")";
            Statement call = databaseConnection.createStatement();

            call.executeQuery(query);
            call.close();

        } catch (SQLException e) {
            System.getLogger("Database Update failed for '" + macAddress +"'!" +
                    "\nSQL Error: " + e.getErrorCode() +" " + e.getSQLState());
        }
    }
}
