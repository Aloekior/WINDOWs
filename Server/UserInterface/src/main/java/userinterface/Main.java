package userinterface;

import userinterface.objects.DatabaseConnection;
import userinterface.objects.User;

import java.util.Scanner;

public class Main {
    static Scanner scanner = new Scanner(System.in);
    
    public static void main(String[] args) {
        boolean quitApplication = false;
        
        DatabaseConnection database = userLogin();
        
        if (database.databaseUserValid()) {
            while (!quitApplication) {
                System.out.print("\nPlease enter a command: ");
                String command = scanner.nextLine();
                switch (command) {
                    case "addSensor" -> database.addSensor();
                    case "deactivateSensor" -> database.removeSensor();
                    case "printStates" -> database.printCurrentStates();
                    case "printHistory" -> database.printHistory();
                    case "createUser" -> database.createUser();
                    case "exit" -> quitApplication = true;
                    default -> {
                        System.out.println("Unknown command '" + command + "'\n");
                        displayHelp();
                    }
                }
            }
        }
    }
    
    public static DatabaseConnection userLogin() {
        User user;
        DatabaseConnection database = new DatabaseConnection();
        int loginAttempts = 0;

        do {
            user = new User();
            database.connect(user);
        } while (!user.isValid() && loginAttempts++ < 3);
        
        return database;
    }

    public static void displayHelp() {
        System.out.println("""
                        Available Commands:
                        addSensor           initiates procedure to add a new sensor to the system
                        deactivateSensor    remove sensor from the system (sensor will be set inactive)
                        printStates         prints all last reported sensor states
                        printHistory        prints 50 most recent history entries
                        """);
    }
}
