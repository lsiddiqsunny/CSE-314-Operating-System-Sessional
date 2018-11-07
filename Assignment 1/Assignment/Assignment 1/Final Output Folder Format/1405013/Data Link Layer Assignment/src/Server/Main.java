package Server;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Created by rafid on 23/9/2017.
 */



public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    private static Stage window;

    static Stage getWindow() {
        return window;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("ServerStartUp.fxml"));
        window = primaryStage;
        primaryStage.setTitle("Server");
        primaryStage.setScene(new Scene(root, 302, 174));
        primaryStage.show();
    }
}
