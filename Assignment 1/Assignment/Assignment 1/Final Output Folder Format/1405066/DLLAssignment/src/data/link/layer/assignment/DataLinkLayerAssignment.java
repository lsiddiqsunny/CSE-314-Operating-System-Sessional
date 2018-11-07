/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.link.layer.assignment;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author User
 */
public class DataLinkLayerAssignment extends Application {
    
    
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("login.fxml"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setOnHidden(e -> {
            try {
                MessagingTools.dout.writeUTF("logout");
                MessagingTools.rout.writeUTF("logout");
                MessagingTools.din.close();
                MessagingTools.dout.close();
                MessagingTools.socket.close();
                MessagingTools.rout.close();
                MessagingTools.rin.close();
                MessagingTools.rSocket.close();
            } catch (Exception ex) {
               
            }
        });
        stage.show();
    }
 
    
    
    
    public static void main(String[] args) throws IOException {
        
        launch(args);
    }
    
    
    
}
