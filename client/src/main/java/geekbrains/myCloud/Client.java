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
import java.util.Optional;
import java.util.function.Function;

public class Client {
    private Socket socket;
    private ObjectDecoderInputStream is;
    private ObjectEncoderOutputStream os;
    private List<ClientObserver> observers;
    private Optional<String> token = Optional.empty();

    public static Client shared = new Client();

    private Client() {
        try {
            observers = Collections.synchronizedList(new ArrayList<>());
            socket = new Socket("localhost", 8190);
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

    public void disconnect() throws IOException {
        observers.forEach(o -> removeObserver(o));
        socket.close();
        os.close();
        is.close();
    }

    private void readLoop() {
        try {
            while(true) {
                CloudMessage message = (CloudMessage) is.readObject();

                switch (message.getType()) {
                    case FILE_DOWNLOAD -> observers.forEach(o -> o.handleFileMessage((FileUploadMessage) message));
                    case LIST -> observers.forEach(o -> o.handleListMessage((ListMessage) message));
                    case AUTH_SUCCESS -> {
                        AuthenticationSuccess success = (AuthenticationSuccess) message;
                        token = Optional.of(success.getToken());

                        observers.forEach(o -> o.handleSuccessMessage(success));
                    }
                    case ERROR -> observers.forEach(o -> o.handleErrorMessage((ErrorMessage) message));
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void sendRegister(String login, String password) {
        sendCommand(new SignUp(login, password));
    }

    public void sendLogin(String login, String password) {
        sendCommand(new Login(login, password));
    }

    public void sendGoTo(String dir) {
        sendAuthorizedCommand(t -> new GoToDir(t, dir));
    }

    public void sendFileRename(String oldPath, String newPath){
        sendAuthorizedCommand(t ->new FileRename(t, oldPath, newPath));
    }

    public void sendFileDelete(String path){
        sendAuthorizedCommand(t -> new FileDelete(t, path));
    }

    public void sendUpload(Path path, String serverDir) {
        sendOptionalAuthorizedCommand(t -> {
            try {
                return Optional.of(new FileUploadMessage(t, path, serverDir));
            } catch (IOException e) {
                e.printStackTrace();
                return Optional.empty();
            }
        });
    }

    public void sendDownload(String path){
        sendAuthorizedCommand(t -> new FileRequest(t, path));
    }

    private void sendAuthorizedCommand(Function<String, AuthorizedCloudMessage> commandBuilder) {
        token.map(commandBuilder).ifPresent(msg -> {
            sendCommand(msg);
        });
    }

    private void sendOptionalAuthorizedCommand(Function<String, Optional<AuthorizedCloudMessage>> commandBuilder) {
        token.flatMap(commandBuilder).ifPresent(msg -> {
            sendCommand(msg);
        });
    }

    private void sendCommand(CloudMessage message) {
        try {
            os.writeObject(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
