package userinterface;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.UUID;

import static database.DatabaseConnection.*;
import static userinterface.userCommands.*;

public class Main {
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        boolean quitApplication = false;
        
        while (!quitApplication) {
            System.out.print("\nPlease enter a command: ");
            String command = scanner.nextLine();
            switch (command) {
                case "addSensor" -> caseAddSensor();
                case "removeSensor" -> caseRemoveSensor();
                case "printStates" -> casePrintStates();
                case "printHistory" -> casePrintHistory();
                case "exit" -> quitApplication = true;
                default -> {
                    System.out.println("Unknown command '" + command + "'\n");
                    caseHelp();
                }
            }
        }
    }
    
    public static void addSensor() throws IOException {
        int serverPort = 57336;
        String token;
        
        ServerSocket socket = new ServerSocket(serverPort);
        System.out.println("Waiting for new sensor to connect..");
        Socket sensor = socket.accept();
        
        BufferedReader sensorInput = new BufferedReader(new InputStreamReader(sensor.getInputStream()));
        
        String macAddress = sensorInput.readLine();
        
        System.out.println("Connected to: " + macAddress);
        
        token = getToken(macAddress);
        sendStringToSensor(token, sensor);
        
        System.out.println("Token sent to sensor: '" + token + "'");
        
        sensor.close();
        socket.close();
    }
    
    public static String getToken(String macAddress) {
        String token = databaseCheckMac(macAddress);
        
        if (token.equals("")) {
            do {
                token = UUID.randomUUID().toString();
            } while (databaseCheckToken(token));
        }
        return token;
    }
    
    private static void sendStringToSensor(String token, Socket sensor) throws IOException {
        PrintWriter printWriter = new PrintWriter(sensor.getOutputStream());
        printWriter.println(token);
        printWriter.flush();
    }
}
