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
                    case "deactivateSensor" -> database.deactivateSensor();
                    case "changeSensorRoom" -> database.changeSensorLocation(false);
                    case "changeSensorWindow" -> database.changeSensorLocation(true);
                    case "printStates" -> database.printCurrentStatesPrepareQuery(false);
                    case "printRoom" -> database.printCurrentStatesPrepareQuery(true);
                    case "printHistory" -> database.printHistory();
                    case "createUser" -> database.userOption(true);
                    case "deleteUser" -> database.userOption(false);
                    case "exit" -> quitApplication = true;
                    default -> {
                        System.out.println("Unknown command '" + command + "'\n");
                        printHelp();
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

    public static void printHelp() {
        System.out.println("""
                        Available Commands:
                        addSensor           initiates procedure to add a new sensor to the system
                        deactivateSensor    remove sensor from the system (sensor will be set inactive)
                        changeSensorRoom    allows to change a sensors assigned room name
                        changeSensorWindow  like changeRoom, just for window within a room
                        printStates         prints all last reported sensor states
                        printRoom           prints last reported sensor states assigned to the entered room only
                        printHistory        prints 50 most recent history entries
                        createUser          (ADMIN ONLY) Create new read-sensors-only database user
                        deleteUser          (ADMIN ONLY) Delete read-sensors-only database user
                        exit                quit application
                        """);
    }
}
