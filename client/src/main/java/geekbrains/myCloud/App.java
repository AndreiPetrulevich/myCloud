package geekbrains.myCloud;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {
    private Stage stage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        stage.setTitle("Cloud storage");

        showLoginForm();

        stage.show();
    }

    @Override
    public void stop() {
        try {
            Client.shared.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showLoginForm() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("logInLayout.fxml"));
        Parent logInLayout = loader.load();
        LoginForm form = loader.getController();
        form.goToMainScreen = () -> {
            form.deinitialize();
            try {
                this.showMainScreen();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        stage.setScene(new Scene(logInLayout));
    }

    public void showMainScreen() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("myCloudLayout.fxml"));
        Parent myCloudLayout = loader.load();
        MainScreen screen = loader.getController();
        screen.goToLoginForm = () -> {
            screen.deinitialize();
            try {
                this.showLoginForm();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        stage.setScene(new Scene(myCloudLayout));
    }
}
