package geekbrains.myCloud;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) throws IOException {
        var serverSocket = new ServerSocket(8190);

        System.out.println("Server started.");

        try {
            while(true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");
                new Thread(new Handler(socket)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
