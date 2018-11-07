/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client_fxml;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
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
 * FXML Controller class
 *
 * @author user
 */
public class HomePageController implements Initializable {

    /**
     * Initializes the controller class.
     */
    @FXML
    private Label welcomeLabel;

    @FXML
    void sendButtonPressed(ActionEvent e) {
        //server k ready krlam
        try {    
            Client_FXML.dOut.writeShort(Client_FXML.outgoingReq);
        } catch (IOException ex) {
            Logger.getLogger(HomePageController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //send file gui choley asbe
        try {
            Parent homePage = FXMLLoader.load(getClass().getResource("SendFile.fxml"));
            Scene homePageScene = new Scene(homePage);
            Stage homePageStage = (Stage) ((Node) e.getSource()).getScene().getWindow();

            homePageStage.setScene(homePageScene);
            homePageStage.setTitle("SendFile-GUI");
            homePageStage.show();

        } catch (IOException ex) {
            System.out.println("SendFile.fxml load hoi nai");
            Logger.getLogger(LogInPageController.class.getName()).log(Level.SEVERE, null, ex);
            
        }
    }

    @FXML
    void logOutButtonPressed(ActionEvent e) {
        try {
            Client_FXML.socket.close();
        } catch (IOException ex) {
            Logger.getLogger(HomePageController.class.getName()).log(Level.SEVERE, null, ex);
        }
        ShowAlert("Log-Out Window", "You have been successfully logged-out", Alert.AlertType.CONFIRMATION);
        System.exit(0);
    }

    void ShowAlert(String title, String ctext, Alert.AlertType type) {
        Alert failed = new Alert(type);
        failed.setTitle(title);
        failed.setContentText(ctext);
        failed.setHeaderText(null);
        failed.showAndWait();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        welcomeLabel.setText("Welcome " + Client_FXML.clientID);
    }

}
