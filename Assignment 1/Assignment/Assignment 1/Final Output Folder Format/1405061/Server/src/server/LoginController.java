/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.IOException;
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
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author ASUS
 */
public class LoginController implements Initializable {

    @FXML
    private TextField id;
    @FXML
    private PasswordField pass;

    /**
     * Initializes the controller class.
     */
    clientThread ct;
    @FXML
    private Label status;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    

    @FXML
    private void sign_in(ActionEvent event) throws IOException {
        
        ct = new clientThread(6791);
        String stId = id.getText();
        String stPass = pass.getText();
        System.out.println(stId+stPass);
        ct.start(id.getText(), pass.getText());
        
        Parent root = FXMLLoader.load(getClass().getResource("fileSend.fxml"));
        Scene scene = new Scene(root);
        Stage stage1=(Stage)((Node)event.getSource()).getScene().getWindow();
        
        
        ///should started from here
        if(ct.log_var)
        {
            System.out.println("Succesfully logged in");
            stage1.hide();
            stage1.setScene(scene);
            stage1.show();
        }
        else if(ct.x == 2)
        {
            status.setText("Wrong name or password");
            id.clear();
            pass.clear();
        }
        else if(ct.x ==3)
        {
            status.setText("Already logged in");
            id.clear();
            pass.clear();
        }
        else
        {
            System.out.println("not logged in");
            stage1.close();
        }
    }

    @FXML
    private void exit(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("Login.fxml"));
        Scene scene = new Scene(root);
        Stage stage1=(Stage)((Node)event.getSource()).getScene().getWindow();
        stage1.close();
    }
    
}
