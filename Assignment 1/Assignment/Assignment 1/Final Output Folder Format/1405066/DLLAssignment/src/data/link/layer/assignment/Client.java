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
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

/**
 *
 * @author User
 */
public class Client implements Runnable{
    
 
    private Socket socket;
    private DataInputStream din;
    private DataOutputStream dout;
    private Thread thread;
    private String username;
    private String password;

    public Thread getThread() {
        return thread;
    }
    
    
    
    public Client(String username, String password, Socket socket) throws IOException{
        ClientInfo clientInfo = ClientInfo.getInstance();
        this.username = username;
        this.password = password;
        this.socket = socket;
        din = new DataInputStream(socket.getInputStream());
        dout = new DataOutputStream(socket.getOutputStream());
        thread = new Thread(this);
        thread.start();
    }
    
    
    
    @Override
    public void run() {
            
    }
        
    
    
    
    
    
}





class ClientInfo{
    private static final ClientInfo clientInfo = new ClientInfo();
    private String receiverid;
    private String fileLocation;
    
    private ClientInfo(){}

    public String getReceiverid() {
        return receiverid;
    }

    public void setReceiverid(String receiverid) {
        this.receiverid = receiverid;
    }

    public String getFileLocation() {
        return fileLocation;
    }

    public void setFileLocation(String fileLocation) {
        this.fileLocation = fileLocation;
    }
    
    
    
    public static ClientInfo getInstance(){
        return clientInfo;
    }
}
