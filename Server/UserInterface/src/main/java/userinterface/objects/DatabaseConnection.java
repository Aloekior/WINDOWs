package userinterface.objects;

import java.io.*;
import java.net.InetAddress;
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
            InetAddress localhost = InetAddress.getLocalHost();
            System.out.println("Waiting for new sensor to connect..");
            System.out.println("Local IP Address : " + localhost.getHostAddress().trim());
            Socket sensor = socket.accept();

            BufferedReader sensorInput = new BufferedReader(new InputStreamReader(sensor.getInputStream()));

            String macAddress = sensorInput.readLine();

            System.out.println("Connected to: " + macAddress);

            token = getToken(macAddress);
            sendStringToSensor(token, sensor);
            addSensorToDataBase(macAddress, token);
            

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
        String error = "checkToken FAILED";
        return Boolean.parseBoolean(multiMethodCall(procedure, token, error));
    }
    
    private void sendStringToSensor(String token, Socket sensor) throws IOException {
        PrintWriter printWriter = new PrintWriter(sensor.getOutputStream());
        printWriter.println(token);
        printWriter.flush();
    }
    
    private void addSensorToDataBase(String macAddress, String token) {
        String procedure = "addSensor";
        String databaseString = macAddress + "', '" + token;
        String error = "addSensor FAILED";
        multiMethodCall(procedure, databaseString, error);
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
        String error = "checkMac FAILED";
        return multiMethodCall(procedure, macAddress, error);
    }
    
    private void deactivateSensorByMac(String macAddress) {
        String procedure = "deactivateSensorByMac";
        String error = "deactivateSensorByMac FAILED";
        multiMethodCall(procedure, macAddress, error);
    }
    
    public boolean checkRoomExists(String room) {
        // TODO: implement method databaseCheckRoom()
        return false;
    }
    
    private void deactivateSensorsByRoom(String room) {
        String procedure = "deactivateSensorsByRoom";
        String error = "deactivateSensorsByRoom FAILED";
        multiMethodCall(procedure, room, error);
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
    
    private String multiMethodCall(String procedure, String value, String error) {
        connect();
        try (Statement call = connection.createStatement()) {
            String query = "CALL windows."+ procedure + "('" + value + "')";
            ResultSet databaseAnswer = call.executeQuery(query);

//            System.out.println(databaseAnswer.getString(1));
//  TODO: fix retrieval of data from database
//            return token.getString(1);
            return "";
        } catch (SQLException e) {
            System.out.println(error);
            e.printStackTrace();
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
