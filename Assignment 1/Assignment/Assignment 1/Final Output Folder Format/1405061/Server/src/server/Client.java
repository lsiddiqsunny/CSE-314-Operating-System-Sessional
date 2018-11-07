/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author ASUS
 */



public class Client extends Application{
    public static void main(String args[]) throws Exception {
        
       // new clientThread(6791);
       launch(args);
 }

    @Override
    public void start(Stage primaryStage) throws Exception {
         //To change body of generated methods, choose Tools | Templates.
         Parent root = FXMLLoader.load(getClass().getResource("Login.fxml"));
         Scene scene = new Scene(root);
         Stage stage = new Stage();
         stage.setScene(scene);
         stage.show();
         
    }
    
}
