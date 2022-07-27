package javadummy;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class WINDOWsDummy {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost",57336);
        String serverMessage = "12:34:56:78:ab:cd";
        
        PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
        printWriter.println(serverMessage);
        printWriter.flush();

        socket.close();
    }
}
