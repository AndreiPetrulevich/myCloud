package geekbrains.myCloud;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static geekbrains.myCloud.Sender.getFile;
import static geekbrains.myCloud.Sender.sendFile;

public class Handler implements Runnable, Closeable {
    private final Socket socket;
    private static final int SIZE = 2048;

    private Path serverDir;
    private DataInputStream is;
    private DataOutputStream os;
    private final byte[] buffer;

    public Handler(Socket socket) throws IOException {
        this.socket = socket;
        is = new DataInputStream(socket.getInputStream());
        os = new DataOutputStream(socket.getOutputStream());
        serverDir = Paths.get("data");
        buffer = new byte[SIZE];
        sendFileToServer();
    }

    public void sendFileToServer() throws IOException {
        List<String> files = Files.list(serverDir)
                .map(p -> p.getFileName().toString())
                .collect(Collectors.toList());
        os.writeUTF("#list#");
        os.writeInt(files.size());
        for (String file : files) {
            os.writeUTF(file);
        }
        os.flush();
    }


    @Override
    public void close() throws IOException {
        socket.close();
    }

    @Override
    public void run() {
        try {
            String command = is.readUTF();
            System.out.println("Received command " + command);
            if (command.equals("#file#")) {
                getFile(is, serverDir, SIZE, buffer);
                sendFileToServer();
            }else if (command.equals("#get_file#")) {
                String fileName = is.readUTF();
                sendFile(fileName, os, serverDir);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
