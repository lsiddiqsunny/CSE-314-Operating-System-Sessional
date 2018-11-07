/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import static server.clientOnlineThread.ct;

/**
 * FXML Controller class
 *
 * @author ASUS
 */
public class FileSendController implements Initializable, Runnable {

    

    /**
     * Initializes the controller class.
     */
    
    
    
    
    @FXML
    private TextField filePath;
    @FXML
    private TextField recId;
    
    
    static clientOnlineThread col;
    static clientThread ct;
    
    static String studentId="";
    @FXML
    private Button selectBut;
    @FXML
    private Button sendBut;
    @FXML
    private TextField notifTxt;
    @FXML
    private static Button yesBut;
    @FXML
    private static Button noBut;
    @FXML
    private Label stdId;
    Thread t;
    public static String flag="-1";
    @FXML
    private Button logout;
    @FXML
    private Button error;
    @FXML
    private Button lostFrame;
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        stdId.setText("Student Id : "+studentId);
        t = new Thread(this);
        t.start();
        
    }    

    @FXML
    private void chooseFile(ActionEvent event) {
        FileChooser fc = new FileChooser();
        File selectedFile = fc.showOpenDialog(null);
        if (selectedFile != null)
        {
            filePath.setText(selectedFile.getAbsolutePath());
            col.sendFilePath=filePath.getText();
        }
        
    }

    @FXML
    private void sendFile(ActionEvent event) throws IOException, FileNotFoundException, InterruptedException {
         ct.checkFile(filePath.getText(), recId.getText());
    }

    @FXML
    private void receiveFile(ActionEvent event) throws IOException, FileNotFoundException, InterruptedException {
        notifTxt.clear();
        col.startReceiveFile();
    }

    @FXML
    private void rejectFile(ActionEvent event) throws IOException {
        notifTxt.clear();
        notifTxt.setText("File Rejected!!!");
        col.startRejectFile();
    }
    

    @Override
    public void run() {
         while(true)
         {
             try {
                 Thread.sleep(1000);
             } catch (InterruptedException ex) {
                 Logger.getLogger(FileSendController.class.getName()).log(Level.SEVERE, null, ex);
             }
             if(flag != "-1")
             {
                 notifTxt.clear();
                 notifTxt.setText(flag);
                 flag = "-1";
             }
         }
         
    }

    @FXML
    private void logoutNow(ActionEvent event) throws IOException {
        ct.col.t=null;
        ct.logout();
        
         Stage stage1=(Stage)((Node)event.getSource()).getScene().getWindow();
         stage1.close();
    }

    @FXML
    private void introCheckSumError(ActionEvent event) {
        ct.col.checkError=true;
    }

    @FXML
    private void introRandomLostFrame(ActionEvent event) {
        ct.col.randLostFrame=true;
         Random rand = new Random();
         ct.col.lostFrame=rand.nextInt((int) 9) + 1;
    }
    
    
   
    
}
