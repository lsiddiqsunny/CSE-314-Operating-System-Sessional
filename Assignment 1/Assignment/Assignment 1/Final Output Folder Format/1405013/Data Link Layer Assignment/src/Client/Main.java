package Client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Created by rafid on 23/9/2017.
 */
public class Main extends Application{
    private static Stage window;
    private static Client client;
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("ClientStartUp.fxml"));
        window = primaryStage;
        client = new Client();
        primaryStage.setTitle("File Transfer");
        primaryStage.setScene(new Scene(root, 302, 174));
        primaryStage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }

    static Stage getWindow() {
        return window;
    }

    public static Client getClient() {
        return client;
    }
}
