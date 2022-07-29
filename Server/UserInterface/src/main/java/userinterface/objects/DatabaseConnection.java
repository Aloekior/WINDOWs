package userinterface.objects;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.Scanner;
import java.util.UUID;

import static userinterface.UiSetup.runUiSetup;

public class DatabaseConnection {
    private String url;
    private Connection connection;
    private User user;


    public DatabaseConnection() {
        String filename = "localConfig";
        File config = new File(filename);

        try (Scanner fileInput = new Scanner(config)) {
            readUrl(fileInput);
        } catch (FileNotFoundException e) {
            runUiSetup();
            try (Scanner fileInput = new Scanner(config)) {
                readUrl(fileInput);
            } catch (FileNotFoundException f) {
                System.out.println("Problem with file 'localConfig'");
            }
        }
    }

    private void readUrl(Scanner fileInput) {
        while (fileInput.hasNextLine()) {
            this.url = fileInput.nextLine();
        }
    }

    public boolean databaseUserValid() {
        return this.user.isValid();
    }

    public boolean databaseUserAdmin() {
        return this.user.isAdmin();
    }

    public void connect(User user) {
        this.user = user;
        if (connect()) {
            user.isValid(true);

            String userRole = getUserRole();
            System.out.println(userRole);
            if (userRole.equals("windowsadmin")) {
                user.setAdmin();
            }

            disconnect();
            System.out.printf("Successfully connected to database using '%s'%n", user.getUsername());
        }
    }
    
    private String getUserRole() {
        connect();
        try (Statement call = connection.createStatement()) {
            String query = "SELECT CURRENT_ROLE()";
            ResultSet databaseAnswer = call.executeQuery(query);
            databaseAnswer.next();

            return databaseAnswer.getString(1);
        } catch (SQLException e) {
            sqlError("Failed to retrieve user role", e);
        }
        disconnect();
        return "";
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

        try (ServerSocket socket = new ServerSocket(serverPort)) {
            InetAddress localhost = InetAddress.getLocalHost();
            System.out.println("Waiting for new sensor to connect..");
            System.out.println("Local IP Address : " + localhost.getHostAddress().trim());
            Socket sensor = socket.accept();

            BufferedReader sensorInput = new BufferedReader(new InputStreamReader(sensor.getInputStream()));

            String macAddress = sensorInput.readLine().toLowerCase();

            System.out.println("Connected to: " + macAddress);

            token = getToken(macAddress);
            sendStringToSensor(token, sensor);

            System.out.printf("Token sent to sensor: '%s'%n", token);

            sensor.close();
        } catch (IOException e) {
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
        String comment = """
                Options to deactivate sensor by:
                - MAC Address (aa:bb:cc:dd:ee:ff)
                - Room name (alphabetical)
                                        
                Please enter MAC or room:""";
        String sensorToRemove = getStringFromInput(comment);

        if (isMacAddress(sensorToRemove)) {
            if (checkMac(sensorToRemove).equals("")) {
                System.out.println("MAC Address does not exist in database!");
            } else {
                deactivateSensorByMac(sensorToRemove);
            }
        } else {
            deactivateSensorsByRoom(sensorToRemove);
        }
    }

    private String getStringFromInput(String comment) {
        Scanner input = new Scanner(System.in);
        System.out.print(comment);
        return input.nextLine().toLowerCase();
    }

    private boolean isMacAddress(String input) {
        return !Character.isLetter(input.charAt(2));
    }

    private void deactivateSensorByMac(String macAddress) {
        String procedure = "deactivateSensorByMac";
        String error = "deactivateSensorByMac FAILED";
        multiMethodCall(procedure, macAddress, error);
    }

    private void deactivateSensorsByRoom(String room) {
        String procedure = "deactivateSensorsByRoom";
        String error = "deactivateSensorsByRoom FAILED";
        multiMethodCall(procedure, room, error);
        System.out.printf("WARNING: ALL SENSORS IN ROOM '%s' DEACTIVATED!", room);
    }

    public void changeSensorLocation(boolean window) {
        String macAddress = getStringFromInput("Please enter Sensor MAC address: ");
        String procedure;
        String location;
        if (window) {
            procedure = "changeSensorWindow";
            location = getStringFromInput("Please enter window name: ");
        } else {
            procedure = "changeSensorRoom";
            location = getStringFromInput("Please enter room name: ");
        }
        String value = macAddress + "', '" + location;
        String error = procedure + " failed";

        multiMethodCall(procedure, value, error);
    }

    public void printCurrentStatesPrepareQuery(boolean checkRoom) {
        String query;
        int columnCount;

        if (!checkRoom) {
            columnCount = 3;
            query = "CALL windows.getSensorStates('')";
            printStates(query, columnCount);
        } else {
            columnCount = 2;
            String room = getStringFromInput("Please enter room name: ");
            query = "CALL windows.getSensorStates('" + room + "')";
            printStates(query, columnCount);
        }
    }

    private void printStates(String query, int columnCount) {
        connect();
        try (Statement call = connection.createStatement()) {
            ResultSet databaseAnswer = call.executeQuery(query);

            StringBuilder output = printDatabaseTable(databaseAnswer, columnCount);
            System.out.println(output);
        } catch (SQLException e) {
            String error = "Could not print states";
            sqlError(error, e);
        }
        disconnect();
    }

    public void printHistory() {
        connect();
        String query = "CALL windows.getSensorHistory()";

        try (Statement call = connection.createStatement()) {
            ResultSet databaseAnswer = call.executeQuery(query);

            int columnCount = databaseAnswer.getMetaData().getColumnCount();

            StringBuilder output = printDatabaseTable(databaseAnswer, columnCount);
            System.out.println(output);
        } catch (SQLException e) {
            String error = "Could not print states";
            sqlError(error, e);
        }

        disconnect();
    }

    private StringBuilder printDatabaseTable(ResultSet databaseAnswer, int columnCount) throws SQLException {
        StringBuilder output = new StringBuilder();
        while (databaseAnswer.next()) {
            for (int i = 1; i <= columnCount; i++) {
                output.append(databaseAnswer.getString(i));
                if (i < columnCount - 1) {
                    output.append(", ");
                } else if (i == columnCount - 1) {
                    output.append(":");
                } else { // i == columnCount
                    output.append("\n");
                }
            }
        }
        return output;
    }

    public void userOption(boolean create) {
        String procedure = "createRemoveUser";
        String error = "Failed to perform action!\nAre you permitted to modify users?";
        String password = "";
        int isAdmin = 0;

        String username = getStringFromInput("Please enter username: ");
        if (create) {
            Scanner scanner = new Scanner(System.in); // password must not be case-insensitive -> don't use 'getStringFromInput()'!
            System.out.print("Please enter password for new user: ");
            password = scanner.nextLine();
            String isAdminTemp = getStringFromInput("Grant admin rights to new user '" + username + "'? (enter 'yes' or any other): ");
            if (isAdminTemp.equals("yes")) {
                isAdmin = 1;
            }
        }

        String value = username + "', '" + password + "', '" + isAdmin;
        multiMethodCall(procedure, value, error);
    }

    private String multiMethodCall(String procedure, String value, String error) {
        connect();
        try (Statement call = connection.createStatement()) {
            String query = "CALL windows." + procedure + "('" + value + "')";
            ResultSet databaseAnswer = call.executeQuery(query);
            databaseAnswer.next();

            System.out.println(procedure + " SUCCESSFUL");
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
            sqlError("Could not disconnect from database", e);
        }
    }
}
