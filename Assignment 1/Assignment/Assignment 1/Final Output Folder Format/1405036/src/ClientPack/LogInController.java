/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ClientPack;

/*import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import util.NetworkUtil;*/
import DataPack.ConnectionUtilities;
import DataPack.DataClass;
import DataPack.FrameCreator;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
//import util.Reader;
//import util.Writer;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.TextArea;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 * FXML Controller class
 *
 * @author
 */
public class LogInController implements Initializable{
    
    Socket client;
    //BufferedReader reader;
    //PrintWriter writer;
    Scanner sc;
    
    @FXML
    TextArea textLogin;
    @FXML
    Button buttonLogin;
   
    static String username;
    ConnectionUtilities connectionSend;
    ConnectionUtilities connectionReceive;

    public void go() 
    {
        connectionSend=new ConnectionUtilities("127.0.0.1",5000);
        
        System.out.println("Network Writing Established");
        
        connectionReceive = new ConnectionUtilities("127.0.0.1", 5000);

        System.out.println("Network Reading Established");
        

        //Thread t = new Thread(new IncomingReader(connectionReceive));
       // t.start();
        //while (true);

    }

    
     @FXML
    private void logIn(ActionEvent event) throws IOException
    {
          username=textLogin.getText();
            /*connectionSend.roll = username;
            connectionReceive.roll=username;
            DataClass dc = new DataClass();
            dc.setData(username);
            connectionReceive.write(dc);

            dc = new DataClass();
            dc = (DataClass) connectionReceive.read();
            if (dc.isRead)
            {
                System.out.println("Logged in Successfully");*/
                
        
                
                FXMLLoader fXMLLoader=new FXMLLoader(getClass().getResource("ClientFXML.fxml"));
                //ClientController cc=new ClientController(username);
                Parent root=fXMLLoader.load();
                //fXMLLoader.setController(cc);
                Scene scene=new Scene(root);
                Client.window.setScene(scene);
                Client.window.show();
           /* } 
            else 
            {
                System.out.println("Multiple Log in found!\n Connection is closed");
                try
                {
                    connectionReceive.getSocket().close();
                    connectionSend.getSocket().close();
                    System.exit(0);
                } 
                catch (IOException ex) 
                {
                    Logger.getLogger(LogInController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            textLogin.setText("");*/
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) 
    {
        //go();
           
    }
    
    
}

    

    
     
    
