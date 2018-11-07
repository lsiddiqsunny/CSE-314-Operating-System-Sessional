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
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author User
 */
public class LoginController implements Initializable {
    @FXML
    TextField usernameText;
    @FXML
    PasswordField passwordText;
    @FXML
    Button loginButton;
    @FXML
    Label wrongLabel;
    
    private String username ;
    
    
    
    
    @FXML
    private void handleButtonAction(ActionEvent event) throws IOException{
       
        Stage stage = (Stage)loginButton.getScene().getWindow();
        if(event.getSource() == loginButton){
                Socket sender = new Socket("localhost", 1234);
                Socket reciver = new Socket("localhost", 2345);
                DataInputStream din = new DataInputStream(sender.getInputStream());
                DataOutputStream dout = new DataOutputStream(sender.getOutputStream());
                DataInputStream rin = new DataInputStream(reciver.getInputStream());
                DataOutputStream rout = new DataOutputStream(reciver.getOutputStream());

                username = usernameText.getText();
                String password = passwordText.getText();

                dout.writeUTF("login"); // 1
                dout.writeUTF(username); // 2
                dout.writeUTF(password); // 3

                String confirmation = din.readUTF(); // 4
                if(confirmation.equals("yes")){
                    MessagingTools.socket = sender;         // STORING THE CONNECTION SOCKET
                    MessagingTools.din = din;               // STORING THE DATAINPUTSTREAM
                    MessagingTools.dout = dout;             // STORING THE DATAOUTPUTSTREAM
                    MessagingTools.rSocket = reciver;
                    MessagingTools.rin = rin;
                    MessagingTools.rout= rout;
                    MessagingTools.username = username;
                    Parent root = FXMLLoader.load(getClass().getResource("profile.fxml"));
                    
                    stage.setScene(new Scene(root));
                    stage.show();
                }
                else{
                    wrongLabel.setText("wrong username or password");
                    din.close();
                    dout.close();
                    sender.close();
                }
        }
        
            
        
        
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }  
    
    
    
}



class MessagingTools{
    static Socket socket, rSocket;
    static DataInputStream din, rin;
    static DataOutputStream dout, rout;
    static String username;
    static String fileid;
}