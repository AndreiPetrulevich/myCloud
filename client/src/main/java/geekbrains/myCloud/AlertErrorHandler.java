package geekbrains.myCloud;

import geekbrains.myCloud.core.ErrorType;
import javafx.application.Platform;
import javafx.scene.control.Alert;

public class AlertErrorHandler {
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.show();
    }

    public void handle(ErrorType type) {
        String msg;
        switch (type) {
            case LOGIN_EXISTS -> msg = "Login is already taken";
            case WRONG_CREDENTIALS -> msg = "Wrong credentials. Check CapsLock and keyboard layout and try again.";
            case AUTHORIZATION_FAILED -> msg = "Authorization failed. Login again.";
            default -> msg = "Unknown error";
        }
        Platform.runLater(() -> {
            showAlert(Alert.AlertType.ERROR, "Server error", msg);
        });
    }
}
