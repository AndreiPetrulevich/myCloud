package geekbrains.myCloud;

import geekbrains.myCloud.core.*;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Client {
    private ObjectDecoderInputStream is;
    private ObjectEncoderOutputStream os;
    private List<ClientObserver> observers;

    public static Client shared = new Client();

    private Client() {
        try {
            observers = Collections.synchronizedList(new ArrayList<>());
            Socket socket = new Socket("localhost", 8190);
            os = new ObjectEncoderOutputStream(socket.getOutputStream());
            is = new ObjectDecoderInputStream(socket.getInputStream());
            var readThread = new Thread(this::readLoop);
            readThread.setDaemon(true);
            readThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addObserver(ClientObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(ClientObserver observer) {
        observers.remove(observer);
    }

    private void readLoop() {
        try {
            while(true) {
                CloudMessage message = (CloudMessage) is.readObject();

                switch (message.getType()) {
                    case FILE -> observers.forEach(o -> o.handleFileMessage((FileMessage) message));
                    case LIST -> observers.forEach(o -> o.handleListMessage((ListMessage) message));
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void sendRegister(String login, String password) {
        sendCommand(new SignUp(login, password));
    }

    public void sendGoTo(String dir) {
        sendCommand(new GoToDir(dir));
    }

    public void sendFileRename(String oldPath, String newPath){
        sendCommand(new FileRename(oldPath, newPath));
    }

    public void sendFileDelete(String path){
        sendCommand(new FileDelete(path));
    }

    public void sendUpload(Path path) throws IOException {
        sendCommand(new FileMessage(path));
    }

    public void sendDownload(String path){
        sendCommand(new FileRequest(path));
    }

    private void sendCommand(CloudMessage message) {
        try {
            os.writeObject(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
