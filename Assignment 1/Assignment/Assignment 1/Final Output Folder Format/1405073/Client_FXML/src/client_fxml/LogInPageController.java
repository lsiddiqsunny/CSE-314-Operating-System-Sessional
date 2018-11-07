/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client_fxml;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 *
 * @author user
 */
public class LogInPageController implements Initializable{

    boolean logInSuccess;
    boolean validID;
    boolean isConnected;

    @FXML
    private TextField tfield;

    @FXML
    private void logInButtonPressed(ActionEvent e) {

        //client er id nilam
        try {
            Client_FXML.clientID = Integer.parseInt(tfield.getText());
            validID = true;
        } catch (NumberFormatException numberFormatException) {
            tfield.clear();
            validID = false;
            ShowAlert("ID-Error","Enter Numbers Only",Alert.AlertType.ERROR);
        }

        //id valid hoile Proceed, server e connect kra lagbey
        if(validID)
        {
            try {
                Client_FXML.socket=new Socket("localhost",6789);
            } catch (IOException ex) {
                Logger.getLogger(LogInPageController.class.getName()).log(Level.SEVERE, null, ex);
                ShowAlert("Server Error","Failed to connect to ",Alert.AlertType.ERROR);
                System.exit(0);      
            } 
            isConnected=validID;     
        }
        
        if (isConnected)
        {
            //I/O Stream Initialize krlam
            try {
                Client_FXML.dIn = new DataInputStream(Client_FXML.socket.getInputStream());
                Client_FXML.dOut = new DataOutputStream(Client_FXML.socket.getOutputStream());
            } catch (IOException ex) {
                Logger.getLogger(LogInPageController.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            //Server e ID pathailam
            try {
                Client_FXML.dOut.writeInt(Client_FXML.clientID);
            } catch (IOException ex) {
                Logger.getLogger(LogInPageController.class.getName()).log(Level.SEVERE, null, ex);
            }
            //Server thekey feedback nilam
            try {
                logInSuccess=Client_FXML.dIn.readBoolean();
            } catch (IOException ex) {
                Logger.getLogger(LogInPageController.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            if (logInSuccess)
            {
                ShowAlert("Log-In Status","You have been successfully logged in.",Alert.AlertType.INFORMATION);
                try {
                    //kaaj kormo shuru krra lagbey
                    Parent homePage = FXMLLoader.load(getClass().getResource("HomePage.fxml"));
                    Scene homePageScene = new Scene(homePage);
                    Stage homePageStage =(Stage) ((Node)e.getSource()).getScene().getWindow();
                    
                    homePageStage.setScene(homePageScene);
                    homePageStage.setTitle("Client-GUI");
                    homePageStage.show();
                    
                } catch (IOException ex) {
                    Logger.getLogger(LogInPageController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            else
            {
                ShowAlert("Log-In Status","Already Logged-In, Connection is closed.",Alert.AlertType.ERROR);
                System.exit(0);
            }
            
        }
        
        
       
    }

    
    
    
    
    
    
    
    
    void ShowAlert(String title, String ctext, Alert.AlertType type) {
        Alert failed = new Alert(type);
        failed.setTitle(title);
        failed.setContentText(ctext);
        failed.setHeaderText(null);
        failed.showAndWait();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        validID=false;
        logInSuccess=false;
        isConnected=false;
    }
}
