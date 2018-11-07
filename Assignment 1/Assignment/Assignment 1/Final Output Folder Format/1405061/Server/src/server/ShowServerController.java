/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;

/**
 * FXML Controller class
 *
 * @author ASUS
 */
public class ShowServerController implements Initializable,Runnable {

    @FXML
    private TextArea sta;

    /**
     * Initializes the controller class.
     */
    serverThread st;
    Thread t;
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            // TODO
            st=new serverThread(6791);
            t=new Thread(this);
            t.start();
        } catch (IOException ex) {
            Logger.getLogger(ShowServerController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }    

    @Override
    public void run() {
         //2016/11/16 12:08:43
         //To change body of generated methods, choose Tools | Templates.
         while(true)
         {
             try {
                 Thread.sleep(1000);
             } catch (InterruptedException ex) {
                 Logger.getLogger(ShowServerController.class.getName()).log(Level.SEVERE, null, ex);
             }
             if(serverThread.log_id.equals("-1"))
             {
              //do nothing   
             }
             else
             {
                 DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        System.out.println(dateFormat.format(date));
                 sta.appendText("ID "+serverThread.log_id+" logged in at "+dateFormat.format(date)+'\n');
                 serverThread.log_id="-1";
             }
             if(serverThread.log_out_id.equals("-1"))
             {
                 
             }
             else 
             {
                 DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        System.out.println(dateFormat.format(date));
                 sta.appendText("ID "+serverThread.log_out_id+" logged out in at "+dateFormat.format(date)+'\n');
                 serverThread.log_out_id="-1";
             }
             
             
         }
    }
    
}
