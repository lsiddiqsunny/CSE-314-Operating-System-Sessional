/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ClientPack;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Scanner;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 *
 * @author USER
 */
public class Client extends Application {
    public static Scene scene;
    public static Stage window;
      
    @Override
    public void start(Stage primaryStage) throws IOException {
        
        window=primaryStage;
        Parent root1 = FXMLLoader.load(getClass().getResource("LogInFXML.fxml"));
        
        primaryStage.setTitle("File Transmission");
        scene=new Scene(root1);
        primaryStage.setScene(scene);
        primaryStage.show();
        //setUpNetworking();
    }
    
    public static void setScenario(FXMLLoader fxmlLoader) throws IOException
    {
        Parent root2=fxmlLoader.load();
        scene=new Scene(root2);
        window.setScene(scene);
        window.show();
        
        
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
