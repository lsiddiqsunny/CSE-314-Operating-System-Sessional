package clientSide;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;

public class Client extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("login.fxml"));
        File file = new File("D:\\Google Drive\\Programming\\Java\\File Transfer Protocol\\src\\clientSide\\login.fxml");
        if(file.exists()){
//            System.out.println("File is File");
        }
        else{
//            System.out.println("ERROR");
        }
        primaryStage.setTitle("Login");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
