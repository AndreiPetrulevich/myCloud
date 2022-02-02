package geekbrains.myCloud;

import geekbrains.myCloud.core.Rethrow;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class Client implements Initializable {
    public ListView<String> serverView;
    public ListView<String> clientView;
    public TextField clientFilePath;
    public TextField serverFilePath;

    private static final int SIZE = 2048;
    private DataInputStream is;
    private DataOutputStream os;
    private Optional<Path> clientDir = null;
    private Optional<Path> serverDir = null;
    private byte[] buf;
    private Stage primaryStage;

    @Override
    public void initialize() {
        try {
            buf = new byte[SIZE];
            primaryStage = new Stage();
            serverDir = Optional.of(Paths.get("data"));
            Socket socket = new Socket("localhost", 8190);
            is = new DataInputStream(socket.getInputStream());
            os = new DataOutputStream(socket.getOutputStream());
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
                String command = is.readUTF();
                System.out.println("received command: " + command);
                if(command.equals("#file#")) {
                    Rethrow.of(clientDir).ifPresent(path -> Sender.getFile(is, path, SIZE, buf));
                    Platform.runLater(this::updateClientUI);
                } else if (command.equals("#list#")) {
                    Platform.runLater(() -> serverView.getItems().clear());
                    int filesCount = is.readInt();
                    for (int i = 0; i < filesCount; i++) {
                        String fileName = is.readUTF();
                        Platform.runLater(() -> serverView.getItems().add(fileName));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void uploadFile(ActionEvent actionEvent) throws IOException {
        String fileName = clientView.getSelectionModel().getSelectedItem();
        Rethrow.of(clientDir).ifPresent(path -> Sender.sendFile(fileName, os, path));
    }

    public void downloadFile(ActionEvent actionEvent) throws IOException {
        String fileName = serverView.getSelectionModel().getSelectedItem();
        os.writeUTF("#get_file#");
        os.writeUTF(fileName);
        os.flush();
    }

    public void clientPathLevelUp(ActionEvent actionEvent) {
        clientDir = getParentPath(clientFilePath);
        updateClientUI();
    }

    public void serverPathLevelUp(ActionEvent actionEvent) {
        serverDir = getParentPath(serverFilePath);
        updateServerUI();
    }

    private void updateUIForPath(TextField field, Optional<Path> path, ListView<String> view) {
        path.ifPresent(p -> {
            field.setText(p.toString());
            updateView(view, path);
        });
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

    public void selectServerDir() {
        serverDir = selectDir();
        updateServerUI();
    }

    private Optional<Path> selectDir() {
        DirectoryChooser chooseDir = new DirectoryChooser();
        chooseDir.setTitle("Upload file path");
        Optional<File> file = Optional.ofNullable(chooseDir.showDialog(primaryStage));
        return file.map(f -> Paths.get(f.getAbsolutePath()));
    }

    private void updateServerUI() {
        updateUIForPath(serverFilePath, serverDir, serverView);
    }

    private void updateClientUI() {
        updateUIForPath(clientFilePath, clientDir, clientView);
    }

    private void updateView(ListView<String> view, Optional<Path> path) {
        try {
            view.getItems().clear();
            Rethrow.of(path).ifPresent(p -> {
                Files.list(p)
                        .map(a -> a.getFileName().toString())
                        .forEach(f -> view.getItems().add(f));
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
