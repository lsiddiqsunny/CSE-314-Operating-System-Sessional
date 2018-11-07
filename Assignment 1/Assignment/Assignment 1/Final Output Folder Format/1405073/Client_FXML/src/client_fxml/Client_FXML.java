/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client_fxml;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author user
 */
public class Client_FXML extends Application {
    
    static int clientID;
    static int receiverID;
    
    static byte [] clientBuffer;
    
    static short incomingReq=20,outgoingReq=43;
    //43 maney 21 (10101) sathey ekta parity bit-1
    //20 maney 10 (01010) sathey ekta parity bit-0
    
    static File chosenFile;
    static long fileSize;
    static int chunkSize;
    
    static Socket socket;
    static DataInputStream dIn;
    static DataOutputStream dOut;
    

    
    @Override
    public void start(Stage stage) throws Exception {
        
        
        Parent root = FXMLLoader.load(getClass().getResource("LogInPage.fxml"));
        
        Scene scene = new Scene(root);
        
        stage.setScene(scene);
        stage.setTitle("Client-GUI");
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
