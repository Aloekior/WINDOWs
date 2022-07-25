package userinterface;

import java.io.IOException;
import java.util.Scanner;

import static userinterface.Main.addClient;

public class commandCases {
    static Scanner scanner = new Scanner(System.in);
    
    public static void caseAddClient() throws IOException {
        try {
            addClient();
        } catch (IOException e) {
            throw new IOException();
        }
    }
    
    public static void caseRemoveClient() {
        System.out.println();
        String clientToRemove = scanner.nextLine();
    }
    
    public static void casePrintStates() {
        
    }
    
    public static void casePrintHistory() {
        
    }
    
    public static void caseHelp() {
        System.out.println("Available Commands:");
        System.out.println("""
                        help            displays this manual
                        addClient       initiates procedure to add a new sensor to the system
                        removeClient    remove sensor from the system (history data will not be deleted)
                        printStates      prints all last reported sensor states
                        printHistory    prints 50 most recent history entries
                        """);
    }
}
