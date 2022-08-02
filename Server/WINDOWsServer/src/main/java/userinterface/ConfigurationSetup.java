package userinterface;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class ConfigurationSetup {
    public static final Scanner input = new Scanner(System.in);

    private ConfigurationSetup() {
    }

    public static void runSetup() throws IOException {
        String fileName = "localConfig";
        File config = new File(fileName);
        Path filePath = config.toPath();

        if (checkForFile(config)) {
            System.out.println("File already exists. Overwrite? ('yes' to confirm)");
            if (input.nextLine().equalsIgnoreCase("yes")) {
                Files.delete(filePath);
                config = new File(fileName);
            }
        }
        try {
            if (config.createNewFile()) {
                System.out.println("File created");
            }
            writeToFile(fileName);
        } catch (IOException e) {
            System.out.println("Error creating file");
        }
    }

    private static boolean checkForFile(File fileName) {
        try (Scanner fileInput = new Scanner(fileName)) {
            return fileInput.hasNextLine();
        } catch (FileNotFoundException e) {
            return false;
        }
    }

    private static void writeToFile(String filename) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Please enter database url: ");
        String url = scanner.nextLine();
        System.out.print("Please enter username for background runner: ");
        String username = scanner.nextLine();
        System.out.print("Please enter password for user '" + username + "': ");
        String password = scanner.nextLine();

        try (FileWriter configWrite = new FileWriter(filename)) {
            configWrite.write(url + "\n");
            configWrite.write(username + "\n");
            configWrite.write(password);
            System.out.println("Successfully written to file\nSetup finished.");
        } catch (IOException e) {
            System.out.println("Error writing to file\nSetup failed..\nPlease try again!");
        }
    }
}
