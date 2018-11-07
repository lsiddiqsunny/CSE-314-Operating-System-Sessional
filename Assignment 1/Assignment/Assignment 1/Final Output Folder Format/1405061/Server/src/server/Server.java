/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

/**
 *
 * @author ASUS
 */

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;








public class Server extends Application {

    /**
     * @param args the command line arguments
     */
    
    public static void main(String[] args) throws Exception {
        
      // File file=new File("server");
      // file.mkdir();
        //new serverThread(6791);
         launch(args);
        
        //updated file id = stid+recId+fileSize+chunkSize+fileName
        
            
    }
    @Override
    public void start(Stage primaryStage) throws Exception {
         //To change body of generated methods, choose Tools | Templates.
         Parent root = FXMLLoader.load(getClass().getResource("showServer.fxml"));
         Scene scene = new Scene(root);
         Stage stage = new Stage();
         stage.setScene(scene);
         stage.show();
         
    }
    
    
}
