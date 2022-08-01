package backgroundlistener;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class ListenerSetup {
    public static void main(String[] args) {
        String filename = "localListenerConfig";
        
        try {
            File config = new File(filename);
            if (config.createNewFile()) {
                System.out.println("File created");
            } else {
                System.out.println("File already exists");
            }
            writeToFile(filename);
        } catch (IOException e) {
            System.out.println("Error creating file");
        }
    }

    private static void writeToFile(String filename) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Please enter database url: ");
        String url = scanner.nextLine();
        System.out.print("Please enter username for background process: ");
        String username = scanner.nextLine();
        System.out.print("Please enter password for user '" + username + "': ");
        String password = scanner.nextLine();
        
        try (FileWriter configWrite = new FileWriter(filename)){
            configWrite.write(url + "\n");
            configWrite.write(username + "\n");
            configWrite.write(password);
            System.out.println("Successfully written to file");
        } catch (IOException e) {
            System.out.println("Error writing to file");
        }
    }
}
