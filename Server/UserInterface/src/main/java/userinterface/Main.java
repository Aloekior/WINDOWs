package userinterface;

import userinterface.objects.DatabaseConnection;
import userinterface.objects.User;

import java.util.Scanner;

public class Main {
    static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        DatabaseConnection database = userLogin();

        if (database.databaseUserValid()) {
            selection(database, database.databaseUserAdmin());
        }
    }

    private static DatabaseConnection userLogin() {
        User user;
        DatabaseConnection database = new DatabaseConnection();
        int loginAttempts = 0;

        do {
            user = new User();
            database.connect(user);
        } while (!user.isValid() && loginAttempts++ < 3);

        return database;
    }

    private static void selection(DatabaseConnection database, boolean isAdmin) {
        while (true) {
            System.out.print("\nPlease enter a command: ");
            String command = scanner.nextLine().toLowerCase();
            if (isAdmin) {
                if (adminSelection(database, command)) return;
            } else {
                if (userSelection(database, command)) return;
            }
        }
    }

    private static boolean adminSelection(DatabaseConnection database, String command) {
        switch (command) {
            case "sensors" -> database.printSensors();
            case "add" -> database.addSensor();
            case "deactivate" -> database.deactivateSensor();
            case "change room" -> database.changeSensorLocation(false);
            case "change window" -> database.changeSensorLocation(true);
            case "states" -> database.printCurrentStatesPrepareQuery(false);
            case "room" -> database.printCurrentStatesPrepareQuery(true);
            case "history" -> database.printHistory();
            case "create user" -> database.userOption(true);
            case "delete user" -> database.userOption(false);
            case "exit" -> {
                return true;
            }
            default -> {
                if (!command.equals("help")) {
                    System.out.printf("Unknown command '%s'%n", command);
                }
                printAdminHelp();
            }
        }
        return false;
    }

    private static boolean userSelection(DatabaseConnection database, String command) {
        switch (command) {
            case "states" -> database.printCurrentStatesPrepareQuery(false);
            case "room" -> database.printCurrentStatesPrepareQuery(true);
            case "history" -> database.printHistory();
            case "exit" -> {
                return true;
            }
            default -> {
                if (!command.equals("help")) {
                    System.out.printf("Unknown command '%s'%n", command);
                }
                printUserHelp();
            }
        }
        return false;
    }


    private static void printAdminHelp() {
        System.out.println("""
                Available Commands:
                states              prints all last reported sensor states
                room                prints last reported sensor states assigned to the entered room only
                history             prints 50 most recent history entries
                sensors             list all existing sensors
                add                 initiates procedure to add a new sensor to the system
                deactivate          remove sensor from the system (will be set inactive)
                change room         allows to change a sensors assigned room name
                change window       like 'change room', just for window within a room
                create user         Create new read-sensors-only database user
                delete user         Delete read-sensors-only database user
                exit                quit application
                """);
    }

    private static void printUserHelp() {
        System.out.println("""
                Available Commands:
                states              prints all last reported sensor states
                room                prints last reported sensor states assigned to the entered room only
                history             prints 50 most recent history entries
                exit                quit application
                """);
    }
}
