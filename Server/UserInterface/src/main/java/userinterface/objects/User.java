package userinterface.objects;

import java.util.Scanner;

public class User {
    private final String username;
    private final String password;
    private boolean isValid = false;
    
    public User() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please enter username");
        this.username = scanner.nextLine().toLowerCase();
        System.out.println("Please enter password for user '" + username + "'");
        this.password = scanner.nextLine();
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
    
    public boolean isValid() {
        return this.isValid;
    }
    
    public void isValid(boolean isValid) {
        this.isValid = isValid;
    }
}
