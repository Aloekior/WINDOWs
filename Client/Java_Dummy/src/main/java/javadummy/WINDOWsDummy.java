package javadummy;

import javadummy.objects.DummyData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class WINDOWsDummy {
    public static void main(String[] args) throws InterruptedException, IOException {
        int times = 100;
        int run = 0;
        List<DummyData> dummies = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            dummies.add(dummySetup());
        }

        while (run++ < times) {
            for (int i = 0; i < 5; i++) {
                Thread.sleep(4000);
                dummyRunner(dummies.get(i), run);
            }
        }
    }

    private static DummyData dummySetup() throws InterruptedException {
        DummyData dummy = new DummyData();
        String token;

        while (dummy.getToken().length() != 36) {
            try (Socket socket = new Socket("localhost", 57336)) {
                String serverMessage = dummy.getMacAddress();

                PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
                printWriter.println(serverMessage);
                printWriter.flush();

                BufferedReader serverResponse = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                token = serverResponse.readLine().toLowerCase();
                System.out.println("token: " + token);
                dummy.setToken(token);
            } catch (IOException e) {
                Thread.sleep(1000);
            }
        }
        return dummy;
    }

    private static void dummyRunner(DummyData dummy, int round) throws IOException {
        try (Socket socket = new Socket("localhost", 57335)) {
            PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
            printWriter.println(dummy.getMacAddress());
            printWriter.println(dummy.getToken());
            printWriter.println(round % 2);
            printWriter.flush();

        } catch (ConnectException f) {
            System.out.println("Connection problem");
        }
    }
}
