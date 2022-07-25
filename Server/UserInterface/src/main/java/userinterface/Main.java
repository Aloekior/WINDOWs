package userinterface;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;

import static database.DatabaseConnection.*;
import static userinterface.commandCases.*;

public class Main {
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("Please enter command:");
        String command = scanner.nextLine();
        
        switch (command) {
            case "addClient" -> caseAddClient();
            case "removeClient" -> caseRemoveClient();
            case "printStates" -> casePrintStates();
            case "printHistory" -> casePrintHistory();
            default -> caseHelp();
        }
    }
    
    public static void addClient() throws IOException {
        int serverPort = 57332;
        String token;
        
        ServerSocket socket = new ServerSocket(serverPort);
        System.out.println("Waiting for new client to connect..");
        Socket client = socket.accept();

        BufferedReader clientInput = new BufferedReader(new InputStreamReader(client.getInputStream()));
        
        String macAddress = clientInput.readLine();
        
        System.out.println("Connected to: " + macAddress);
        
        token = getToken(macAddress);
        System.out.println("Token sent to client: '" + token + "'");

        PrintWriter printWriter = new PrintWriter(client.getOutputStream());
        printWriter.println(token);
        printWriter.flush();
        
        client.close();
        socket.close();
    }
    
    public static String getToken(String macAddress) {
        int tokenLength = 8;
        String token = databaseCheckMac(macAddress);
        
        if (token.length() != tokenLength) {
            do {
                token = generateToken(tokenLength);
            } while (databaseCheckToken(token));
        }
        return token;
    }
    
    public static String generateToken(int tokenLength) {
        Random rand = new Random();
        String token = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnopqrstuvxyz";

        StringBuilder tokenBuilder = new StringBuilder(tokenLength);

        for (int i = 0; i < tokenLength; i++) {
            int index = token.length() * rand.nextInt();
            tokenBuilder.append(token.charAt(index));
        }

        return tokenBuilder.toString();
    }
}
