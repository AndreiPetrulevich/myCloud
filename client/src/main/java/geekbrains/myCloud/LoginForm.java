package geekbrains.myCloud;

import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.util.Pair;

import java.util.function.Consumer;

public class LoginForm {

    public TextField login;
    public TextField password;

    public void logIn(ActionEvent actionEvent) {
        // processFields(pair -> Client.shared.sendLogin(pair.getKey(), pair.getValue()));
    }

    public void register(ActionEvent actionEvent) {
        processFields(pair -> Client.shared.sendRegister(pair.getKey(), pair.getValue()));
    }

    private void processFields(Consumer<Pair<String, String>> lambda) {
        if(login.getText().trim().isEmpty() || password.getText().trim().isEmpty()){
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
}
