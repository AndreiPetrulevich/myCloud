package geekbrains.myCloud;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    public static void main(String[] args) {

        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Cloud storage");

        Parent logInLayout = FXMLLoader.load(getClass().getResource("logInLayout.fxml"));
        //Parent myCloudLayout = FXMLLoader.load(getClass().getResource("myCloudLayout.fxml"));
        stage.setScene(new Scene(logInLayout));

        stage.show();
    }
}
