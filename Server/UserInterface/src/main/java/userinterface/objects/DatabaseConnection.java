package userinterface.objects;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.Scanner;
import java.util.UUID;

public class DatabaseConnection {
    private String url;
    private Connection connection;
    private String filename = "localConfig";
    private User user;
    
    public DatabaseConnection() {
        File config = new File(filename);
        
        try (Scanner fileInput = new Scanner(config)){
            while (fileInput.hasNextLine()) {
                this.url = fileInput.nextLine();
            }
        } catch (FileNotFoundException e) {
            System.out.println("Configuration file not found!");
        }
    }
    
    public boolean databaseUserValid() {
        return this.user.isValid();
    }
    
    public void connect(User user) {
        this.user = user;
        if (connect()) {
            user.isValid(true);
        }
        disconnect();
        System.out.printf("Successfully connected to database using '%s'%n", user.getUsername());
    }
    
    private boolean connect() {
        boolean noError = true;
        try {
            this.connection = DriverManager.getConnection(url, user.getUsername(), user.getPassword());
        } catch (SQLException e) {
            noError = false;
            System.out.println("Connection to database failed.");
        }
        return noError;
    }
    
    private boolean userIsAdminCheck() {
        // TODO: implement userIsAdminCheck()
        
        String query = "SHOW GRANTS FOR '" + user.getUsername() + "'";
        try (Statement grant = connection.createStatement()) {
            ResultSet grants = grant.executeQuery(query);
            
        } catch (SQLException e) {
            
        }
        return false;
    }
    
    public void addSensor() {
        int serverPort = 57336;
        String token;

        try (ServerSocket socket = new ServerSocket(serverPort)){
            System.out.println("Waiting for new sensor to connect..");
            Socket sensor = socket.accept();

            BufferedReader sensorInput = new BufferedReader(new InputStreamReader(sensor.getInputStream()));

            String macAddress = sensorInput.readLine();

            System.out.println("Connected to: " + macAddress);

            token = getToken(macAddress);
            sendStringToSensor(token, sensor);

            System.out.printf("Token sent to sensor: '%s'%n", token);

            sensor.close();
        } catch (IOException e){
            System.out.println("Problem creating the server socket");
        }
    }

    private String getToken(String macAddress) {
        String token = checkMac(macAddress);

        if (token.equals("")) {
            do {
                token = UUID.randomUUID().toString();
            } while (checkToken(token));
        }
        return token;
    }
    
    private boolean checkToken(String token) {
        String procedure = "checkTokenExists";
        return Boolean.parseBoolean(multiMethodCall(procedure, token));
    }
    
    private void sendStringToSensor(String token, Socket sensor) throws IOException {
        PrintWriter printWriter = new PrintWriter(sensor.getOutputStream());
        printWriter.println(token);
        printWriter.flush();
    }
    
    public void removeSensor() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("""
                        Options to deactivate sensor by:
                        - macAddress
                        - roomName
                        """);
        String sensorToRemove = scanner.nextLine();

        if (isMacAddress(sensorToRemove)) {
            if (checkMac(sensorToRemove).equals("")) {
                System.out.println("MAC Address does not exist in database!");
            } else {
                deactivateSensorByMac(sensorToRemove);
            }
        } else {
            if (!checkRoomExists(sensorToRemove)) {
                System.out.println("Room name does not exist in database!");
            } else {
                deactivateSensorsByRoom(sensorToRemove);
            }
        }
    }

    private boolean isMacAddress(String input) {
        return !Character.isLetter(input.charAt(2));
    }
    
    private String checkMac(String macAddress) {
        String procedure = "checkSensorExists";
        return multiMethodCall(procedure, macAddress);
    }
    
    private void deactivateSensorByMac(String macAddress) {
        String procedure = "deactivateSensorByMac";
        multiMethodCall(procedure, macAddress);
    }
    
    public boolean checkRoomExists(String room) {
        // TODO: implement method databaseCheckRoom()
        return false;
    }
    
    private void deactivateSensorsByRoom(String room) {
        String procedure = "deactivateSensorsByRoom";
        multiMethodCall(procedure, room);
        System.out.printf("WARNING: ALL SENSORS IN ROOM '%s' DEACTIVATED!", room);
    }
    
    public void printCurrentStates() {
        // TODO: implement method databasePrintCurrentStates()
    }
    
    public void printHistory() {
        // TODO: implement method databasePrintHistory()
    }
    
    public void createUser() {
        // TODO: implement method createUser() for user.isAdmin
    }
    
    private String multiMethodCall(String procedure, String value) {
        connect();
        try (Statement call = connection.createStatement()) {
            String query = "CALL windows."+ procedure + "('" + value + "')";
            ResultSet token = call.executeQuery(query);

            return token.getString(1);
        } catch (SQLException e) {
            System.out.println("Could not validate Mac Address");
        } finally {
            disconnect();
        }
        return "";
    }
    
    private void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            System.out.println("Could not disconnect from database");
        }
    }
}
