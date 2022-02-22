package geekbrains.myCloud;

import geekbrains.myCloud.core.ErrorMessage;
import geekbrains.myCloud.core.FileUploadMessage;
import geekbrains.myCloud.core.ListMessage;
import geekbrains.myCloud.core.AuthenticationSuccess;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.util.Pair;

import java.util.function.Consumer;

public class LoginForm implements ClientObserver {
    public TextField login;
    public PasswordField password;
    public AnchorPane rootPane;
    private AlertErrorHandler alertErrorHandler = new AlertErrorHandler();
    public Runnable goToMainScreen;

    public void initialize() {
        Client.shared.addObserver(this);
    }

    public void deinitialize() {
        Client.shared.removeObserver(this);
    }

    public void logIn(ActionEvent actionEvent) {
        processFields(pair -> Client.shared.sendLogin(pair.getKey(), pair.getValue()));
    }

    public void register(ActionEvent actionEvent) {
        processFields(pair -> Client.shared.sendRegister(pair.getKey(), pair.getValue()));
    }

    private void processFields(Consumer<Pair<String, String>> lambda) {
        if (login.getText().trim().isEmpty() || password.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Form Error", "Fill in all fields!");
        } else {
            String loginString = login.getText().trim();
            String passwordString = password.getText().trim();
            login.clear();
            password.clear();
            lambda.accept(new Pair<>(loginString, passwordString));
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.show();
    }


    @Override
    public void handleSuccessMessage(AuthenticationSuccess message) {
        if (goToMainScreen != null) {
            Platform.runLater(goToMainScreen);
        }
    }

    @Override
    public void handleErrorMessage(ErrorMessage message) {
        alertErrorHandler.handle(message.getError());
    }

    @Override
    public void handleListMessage(ListMessage message) {

    }

    @Override
    public void handleFileMessage(FileUploadMessage message) {

    }
}
