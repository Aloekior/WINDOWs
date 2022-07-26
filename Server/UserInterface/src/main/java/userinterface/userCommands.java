package userinterface;

import java.io.IOException;
import java.util.Scanner;

import static userinterface.Main.addSensor;

public class userCommands {
    static Scanner scanner = new Scanner(System.in);
    
    public static void caseAddSensor() throws IOException {
        try {
            addSensor();
        } catch (IOException e) {
            throw new IOException();
        }
    }
    
    public static void caseRemoveSensor() {
        System.out.println("""
                        Options to remove sensor by:
                        - name
                        - macAddress
                        - roomName
                        """);
        String sensorToRemove = scanner.nextLine();
    }
    
    public static void casePrintStates() {
        
    }
    
    public static void casePrintHistory() {
        
    }
    
    public static void caseHelp() {
        System.out.println("""
                        Available Commands:
                        addSensor       initiates procedure to add a new sensor to the system
                        removeSensor    remove sensor from the system (sensor will be set inactive)
                        printStates     prints all last reported sensor states
                        printHistory    prints 50 most recent history entries
                        """);
    }
}
