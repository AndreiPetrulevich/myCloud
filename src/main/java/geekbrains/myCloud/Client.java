package geekbrains.myCloud;

import geekbrains.myCloud.core.*;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class Client implements Initializable {
    public ListView<String> serverFilesList;
    public ListView<String> clientFilesList;
    public TextField clientFilePath;
    public TextField serverFilePath;
    public Button serverPathUpButton;

    private ObjectDecoderInputStream is;
    private ObjectEncoderOutputStream os;
    private Optional<Path> clientDir = null;
    private Optional<Path> serverDir = null;
    private Stage primaryStage;

    @Override
    public void initialize() {
        try {
            primaryStage = new Stage();
            initMouseListeners();
            Socket socket = new Socket("localhost", 8190);
            os = new ObjectEncoderOutputStream(socket.getOutputStream());
            is = new ObjectDecoderInputStream(socket.getInputStream());
            var readThread = new Thread(this::readLoop);
            readThread.setDaemon(true);
            readThread.start();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readLoop() {
        try {
            while(true) {
                CloudMessage message = (CloudMessage) is.readObject();

                switch (message.getType()) {
                    case FILE -> processFileMessage((FileMessage) message);
                    case LIST -> processListMessage((ListMessage) message);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    private void processListMessage(ListMessage message) {
        serverDir = Optional.of(message.getPath());
        Platform.runLater(() -> {
            Path rootPath = Path.of("/");
            serverFilePath.setText(message.getPath().toString());
            serverPathUpButton.setDisable(serverDir.orElse(rootPath).equals(rootPath));
            serverFilesList.getItems().clear();
            serverFilesList.getItems().addAll(message.getFiles());
        });
    }

    private void processFileMessage(FileMessage message) throws IOException {
        Optional<Path> fullPath = clientDir.map(p -> p.resolve(message.getFileName()));
        Rethrow.of(fullPath).ifPresent(path -> {
            Files.write(path, message.getBytes());
        });
        Platform.runLater(this::updateClientUI);
    }


    public void uploadFile(ActionEvent actionEvent) throws IOException {
        String fileName = clientFilesList.getSelectionModel().getSelectedItem();
        os.writeObject(new FileMessage(clientDir.get().resolve(fileName)));
    }

    public void downloadFile(ActionEvent actionEvent) throws IOException {
        String fileName = serverFilesList.getSelectionModel().getSelectedItem();
        os.writeObject(new FileRequest(fileName));
    }

    public void clientPathLevelUp(ActionEvent actionEvent) {
        clientDir = getParentPath(clientFilePath);
        updateClientUI();
    }

    public void serverPathLevelUp(ActionEvent actionEvent) {
    }

    private Optional<Path> getParentPath(TextField field) {
        if (field != null) {
            try {
                Path parentPath = Paths.get(field.getText()).getParent();
                return Optional.of(parentPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return Optional.empty();
    }

    public void selectClientDir() {
        clientDir = selectDir();
        updateClientUI();
    }

    private Optional<Path> selectDir() {
        DirectoryChooser chooseDir = new DirectoryChooser();
        chooseDir.setTitle("Upload file path");
        Optional<File> file = Optional.ofNullable(chooseDir.showDialog(primaryStage));
        return file.map(f -> Paths.get(f.getAbsolutePath()));
    }

    private void updateClientUI() {
        clientDir.ifPresent(p -> {
            clientFilePath.setText(p.toString());

            try {
                clientFilesList.getItems().clear();
                Files.list(p)
                        .map(a -> a.getFileName().toString())
                        .forEach(f -> clientFilesList.getItems().add(f));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void initMouseListeners() {
        clientFilesList.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                clientDir
                        .map(p -> p.resolve(getItem()))
                        .filter(Files::isDirectory)
                        .ifPresent(p -> {
                            clientDir = Optional.of(p);
                            Platform.runLater(this::updateClientUI);
                        });
            }
        });

        serverFilesList.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {

            }
        });
    }

    private String getItem() {
        return clientFilesList.getSelectionModel().getSelectedItem();
    }

    public void reconnect(ActionEvent actionEvent) {
    }
}
