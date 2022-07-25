package background.runner;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class RunnerSocket {
    public static void main(String[] args) throws IOException {
        int receiveConfirmation = 34; // ASCII !
        int serverPort = 57331;
        int macAddressBegin = 4;
        int clientStatePosition = 21;
        
        ServerSocket socket = new ServerSocket(serverPort);
        System.out.println("Server is waiting for Client connection");
        Socket client = socket.accept();
        
        InputStream clientInput = client.getInputStream();
        
        byte[] clientBytes = clientInput.readAllBytes();
        
        OutputStream clientOutput = client.getOutputStream();
        clientOutput.write(receiveConfirmation);
        clientOutput.flush();
        
        client.close();
        socket.close();
        // spawn new socket thread here ?
        
        StringBuilder tokenBuilder = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            tokenBuilder.append(Character.toString(clientBytes[i]));
        }
        String token = tokenBuilder.toString();
        
        StringBuilder macAddressBuilder = new StringBuilder();
        for (int i = macAddressBegin; i < clientStatePosition; i++) {
            macAddressBuilder.append(Character.toString(clientBytes[i]));
        }
        String macAddress = macAddressBuilder.toString();
        
        byte clientState = clientBytes[clientStatePosition];

        System.out.println(token);
        System.out.println(macAddress);
        System.out.println(clientState);
        
    }
}
