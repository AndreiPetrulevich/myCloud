package geekbrains.myCloud;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Client implements Initializable {
    public ListView<String> serverView;
    public ListView<String> clientView;
    public TextField clientFilePath;
    public TextField serverFilePath;

    private static final int SIZE = 2048;
    private DataInputStream is;
    private DataOutputStream os;
    private Path clientDir;
    private byte[] buf;

    @Override
    public void initialize() {
        try {
            buf = new byte[SIZE];
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
                    Sender.getFile(is, clientDir, SIZE, buf);
                    Platform.runLater(this::updateClientView);
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
        Sender.sendFile(fileName, os, clientDir);
    }

    public void downloadFile(ActionEvent actionEvent) throws IOException {
        String fileName = serverView.getSelectionModel().getSelectedItem();
        os.writeUTF("#get_file#");
        os.writeUTF(fileName);
        os.flush();
    }

    public void clientPathLevelUp(ActionEvent actionEvent) {
    }

    public void serverPathLevelUp(ActionEvent actionEvent) {
    }

    public Path asDirectoryPath() {
        FileChooser choose = new FileChooser();
        choose.getExtensionFilters().add(new FileChooser.ExtensionFilter("All files", "."));
        String filePath = choose.showOpenDialog(null).getAbsolutePath();
        Path parentPath = Paths.get(filePath).getParent();
        clientDir = parentPath;
        Platform.runLater(() -> {
            clientFilePath.appendText(parentPath.toString());
        });
        return parentPath;
    }

    public void updateClientView() {
        try {
            clientView.getItems().clear();
            Files.list(clientDir)
                    .map(p -> p.getFileName().toString())
                    .forEach(f -> clientView.getItems().add(f));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
