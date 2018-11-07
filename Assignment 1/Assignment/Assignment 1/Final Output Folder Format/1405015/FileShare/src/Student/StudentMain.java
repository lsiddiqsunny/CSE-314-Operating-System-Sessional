package Student;

import Tools.NetworkUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Created by Toufik on 9/22/2017.
 */
public class StudentMain extends Application{
    static Stage window;
    static Parent root;
    @Override
    public void start(Stage primaryStage) throws Exception{
        root = FXMLLoader.load(getClass().getResource("StudentLogIn.fxml"));
        window = primaryStage;
        window.setTitle("Student");
        window.setScene(new Scene(root));
        window.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
