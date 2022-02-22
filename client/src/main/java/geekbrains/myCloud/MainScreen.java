package geekbrains.myCloud;

import geekbrains.myCloud.core.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class MainScreen implements ClientObserver {
    public BorderPane rootPane;
    public ListView<String> serverFilesList;
    public ListView<String> clientFilesList;
    public TextField clientFilePath;
    public TextField serverFilePath;
    public Button serverPathUpButton;
    public Runnable goToLoginForm;

    private Optional<Path> clientDir = null;
    private String serverDir = SERVER_ROOT_PATH;
    private AlertErrorHandler alertErrorHandler = new AlertErrorHandler();

    private static final String SERVER_ROOT_PATH = "";

    public void initialize() {
        initMouseListeners();

        Client.shared.addObserver(this);
        Client.shared.sendGoTo(SERVER_ROOT_PATH);
    }

    public void deinitialize() {
        Client.shared.removeObserver(this);
    }

    private void initMouseListeners() {
        clientFilesList.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                clientDir
                        .map(p -> p.resolve(getItem(clientFilesList)))
                        .filter(Files::isDirectory)
                        .ifPresent(p -> {
                            clientDir = Optional.of(p);
                            Platform.runLater(this::updateClientUI);
                        });
            }
        });

        serverFilesList.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Path newPath = Path.of(serverDir).resolve(getItem(serverFilesList));
                Client.shared.sendGoTo(newPath.toString());
            }
        });

        serverFilesList.setCellFactory(lv -> {
            FileCell cell = new FileCell(pair -> {
                Path oldPath = Path.of(serverDir).resolve(pair.oldName);
                Path newPath = Path.of(serverDir).resolve(pair.newName);
                Client.shared.sendFileRename(oldPath.toString(), newPath.toString());
            }, f -> {
                Path pathToDeleteFile = Path.of(serverDir).resolve(f);
                Client.shared.sendFileDelete(pathToDeleteFile.toString());
            });
            return cell;
        });
    }

    private String getItem(ListView<String> view) {
        return view.getSelectionModel().getSelectedItem();
    }

    public void uploadFile(ActionEvent actionEvent) throws IOException {
        String fileName = clientFilesList.getSelectionModel().getSelectedItem();
        Client.shared.sendUpload(clientDir.get().resolve(fileName), serverDir);
    }

    public void downloadFile(ActionEvent actionEvent) throws IOException {
        String fileName = serverFilesList.getSelectionModel().getSelectedItem();
        Client.shared.sendDownload(fileName);
    }

    public void clientPathLevelUp(ActionEvent actionEvent) {
        clientDir = getParentPath(clientFilePath);
        updateClientUI();
    }

    public void serverPathLevelUp(ActionEvent actionEvent) {
        Optional<Path> pathToSend = getParentPath(serverFilePath);
        Client.shared.sendGoTo(pathToSend.map(Path::toString).orElse(SERVER_ROOT_PATH));
    }

    private Optional<Path> getParentPath(TextField field) {
        if (field != null) {
            try {
                return Optional.ofNullable(Paths.get(field.getText()).getParent());
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
        Optional<File> file = Optional.ofNullable(chooseDir.showDialog(rootPane.getScene().getWindow()));
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

    public void handleListMessage(ListMessage message){
        serverDir = message.getPath();
            Platform.runLater(() -> {
            serverFilePath.setText(message.getPath());
            serverPathUpButton.setDisable(serverDir.equals(SERVER_ROOT_PATH));
            serverFilesList.getItems().clear();
            serverFilesList.getItems().addAll(message.getFiles());
        });
    }

    public void handleFileMessage(FileUploadMessage message) {
        try {
            Optional<Path> fullPath = clientDir.map(p -> p.resolve(message.getFileName()));
            Rethrow.of(fullPath).ifPresent(path -> {
                Files.write(path, message.getBytes());
            });
            Platform.runLater(this::updateClientUI);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleSuccessMessage(AuthenticationSuccess message) {

    }

    @Override
    public void handleErrorMessage(ErrorMessage message) {
        alertErrorHandler.handle(message.getError());
        if (message.getError() == ErrorType.AUTHORIZATION_FAILED && goToLoginForm != null) {
            Platform.runLater(goToLoginForm);
        }
    }
}
