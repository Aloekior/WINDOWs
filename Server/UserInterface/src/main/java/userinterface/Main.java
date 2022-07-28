package userinterface;

import userinterface.objects.DatabaseConnection;
import userinterface.objects.User;

import java.util.Locale;
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
                switch (command.toLowerCase()) {
                    case "add sensor" -> database.addSensor();
                    case "deactivate sensor" -> database.deactivateSensor();
                    case "change sensor room" -> database.changeSensorLocation(false);
                    case "change sensor window" -> database.changeSensorLocation(true);
                    case "states" -> database.printCurrentStatesPrepareQuery(false);
                    case "room" -> database.printCurrentStatesPrepareQuery(true);
                    case "history" -> database.printHistory();
                    case "create user" -> database.userOption(true);
                    case "delete user" -> database.userOption(false);
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
                        add                 (sensor) initiates procedure to add a new sensor to the system
                        deactivate          (sensor) remove sensor from the system (will be set inactive)
                        change room         allows to change a sensors assigned room name
                        change window       like 'change room', just for window within a room
                        states              prints all last reported sensor states
                        room                prints last reported sensor states assigned to the entered room only
                        history             prints 50 most recent history entries
                        create user         (ADMIN ONLY) Create new read-sensors-only database user
                        delete user         (ADMIN ONLY) Delete read-sensors-only database user
                        exit                quit application
                        """);
    }
}
