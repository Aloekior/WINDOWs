package userinterface;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class UiSetup {
    public static void main(String[] args) {
        String filename = "localConfig";
        
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

        System.out.println("Please enter database url");
        String url = scanner.nextLine();
        
        try (FileWriter configWrite = new FileWriter(filename)){
            configWrite.write(url);
            System.out.println("Data successfully written to file.");
        } catch (IOException e) {
            System.out.println("Error writing to file");
        }
    }
}
