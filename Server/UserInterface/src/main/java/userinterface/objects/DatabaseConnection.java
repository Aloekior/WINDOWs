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
            addSensorToDataBase(macAddress, token);
        }
        return token;
    }
    
    private String checkMac(String macAddress) {
        String procedure = "checkSensorExists";
        String error = "checkMac FAILED";
        return multiMethodCall(procedure, macAddress, error);
    }
    
    private boolean checkToken(String token) {
        String procedure = "checkTokenExists";
        String error = "checkToken FAILED";
        return Boolean.parseBoolean(multiMethodCall(procedure, token, error));
    }
    
    private void addSensorToDataBase(String macAddress, String token) {
        String procedure = "addSensor";
        String databaseString = macAddress + "', '" + token;
        String error = "addSensor FAILED";
        multiMethodCall(procedure, databaseString, error);
    }
    
    private void sendStringToSensor(String token, Socket sensor) throws IOException {
        PrintWriter printWriter = new PrintWriter(sensor.getOutputStream());
        printWriter.println(token);
        printWriter.flush();
    }
    
    public void deactivateSensor() {
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
    
    private void deactivateSensorByMac(String macAddress) {
        String procedure = "deactivateSensorByMac";
        String error = "deactivateSensorByMac FAILED";
        multiMethodCall(procedure, macAddress, error);
    }
    
    private boolean checkRoomExists(String room) {
        String procedure = "checkSensorRoom";
        String error = procedure + " failed";
        return Boolean.parseBoolean(multiMethodCall(procedure, room, error));
    }
    
    private void deactivateSensorsByRoom(String room) {
        String procedure = "deactivateSensorsByRoom";
        String error = "deactivateSensorsByRoom FAILED";
        multiMethodCall(procedure, room, error);
        System.out.printf("WARNING: ALL SENSORS IN ROOM '%s' DEACTIVATED!", room);
    }
    
    public void changeSensorLocation(boolean window) {
        String macAddress = getStringFromInput("Please enter Sensor MAC address:");
        String procedure;
        String location;
        if (window) {
            procedure = "changeSensorWindow";
            location = getStringFromInput("Please enter window name:");
        } else {
            procedure = "changeSensorRoom";
            location = getStringFromInput("Please enter room name:");
        }
        String value = macAddress + "', '" + location;
        String error = procedure + " failed";
        
        multiMethodCall(procedure,value,error);
    }
    
    private String getStringFromInput(String comment) {
        Scanner input = new Scanner(System.in);
        System.out.println(comment);
        return input.nextLine();
    }
    
    public void printCurrentStatesPrepareQuery(boolean checkRoom) {
        String query;
        
        if (!checkRoom) {
            query = "CALL getSensorStates('')";
            printStates(query, checkRoom);
        } else {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Please enter room name:");
            String room = scanner.nextLine();
            query = "CALL getSensorStates('" + room + "')";
            printStates(query, checkRoom);
        }
        
        
    }
    
    private void printStates(String query, boolean checkRoom) {
        int columnCount;
        
        connect();
        if (checkRoom) {
            columnCount = 2;
        } else {
            columnCount = 3;
        }
        
        try (Statement call = connection.createStatement()) {
            ResultSet databaseAnswer = call.executeQuery(query);

            StringBuilder output = printDatabaseTable(databaseAnswer, columnCount);
            System.out.println(output);
        } catch (SQLException e) {
            sqlError("Could not print states", e);
        }
        
        disconnect();
    }
    
    public void printHistory() {
        connect();
        String query = "CALL getSensorHistory()";
        
        try (Statement call = connection.createStatement()) {
            ResultSet databaseAnswer = call.executeQuery(query);
            
            int columnCount = databaseAnswer.getMetaData().getColumnCount();

            StringBuilder output = printDatabaseTable(databaseAnswer, columnCount);
            System.out.println(output);
        } catch (SQLException e) {
            sqlError("Could not print states", e);
        }

        disconnect();
    }

    private StringBuilder printDatabaseTable(ResultSet databaseAnswer, int columnCount) throws SQLException {
        StringBuilder output = new StringBuilder();
        while (databaseAnswer.next()) {
            for (int i = 1; i <= columnCount; i++) {
                output.append(databaseAnswer.getString(i));
                if (i != columnCount) {
                    output.append("\t\t");
                } else {
                    output.append("\n");
                }
            }
        }
        return output;
    }

    public void userOption(boolean create) {
        Scanner scanner = new Scanner(System.in);
        String procedure = "addReadOnlyUser";
        String error = "Failed to perform action!\nAre you permitted to modify users?";
        String password = "";
        
        System.out.println("Please enter username:");
        String username = scanner.nextLine();
        if (create) {
            System.out.println("Please enter password for new user:");
            password = scanner.nextLine();
        }
        
        String value = username + "', '" + password;
        multiMethodCall(procedure, value, error);
    }
    
    private String multiMethodCall(String procedure, String value, String error) {
        connect();
        try (Statement call = connection.createStatement()) {
            String query = "CALL windows."+ procedure + "('" + value + "')";
            ResultSet databaseAnswer = call.executeQuery(query);
            databaseAnswer.next();

            System.out.println(procedure + "successful");
            return databaseAnswer.getString(1);
        } catch (SQLException e) {
            sqlError(error, e);
        }
        disconnect();
        return "";
    }

    private void sqlError(String error, SQLException e) {
        System.out.println(error);
        e.printStackTrace();
    }

    private void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            System.out.println("Could not disconnect from database");
        }
    }
}
