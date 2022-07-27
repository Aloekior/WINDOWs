package backgroundlistener.objects;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Configuration {
    private String url;
    private String username;
    private String password;
    
    public Configuration() {
        File localConfig = new File ("localConfig");
        try (Scanner read = new Scanner(localConfig)){
            this.url = read.nextLine();
            this.username = read.nextLine();
            this.password = read.nextLine();
        } catch (FileNotFoundException e) {
            System.getLogger("Configuration file not found!");
        }
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
