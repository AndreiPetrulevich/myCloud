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
        Parent parent = FXMLLoader.load(getClass().getResource("myCloudLayout.fxml"));
        stage.setMinHeight(416);
        stage.setMinWidth(616);
        stage.setScene(new Scene(parent));
        stage.show();

    }
}