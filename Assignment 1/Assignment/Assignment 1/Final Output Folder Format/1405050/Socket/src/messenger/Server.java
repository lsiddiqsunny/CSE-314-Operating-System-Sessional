/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package messenger;

import java.io.File;
import java.io.FileOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.print.attribute.HashAttributeSet;

/**
 *
 * @author TIS
 */
public class Server {
    
    public ServerSocket serverSocket;
    HashMap<String,Information> clientList;
    
    
    
    public Server(int port){
        
        clientList=new HashMap<>();
        
        
        try {
            serverSocket=new ServerSocket(port);
            System.out.println("Server started successfully! Server is Running...");
            while(true){
                
                Socket clientSocket=serverSocket.accept();
                ConnectionUtillities connection=new ConnectionUtillities(clientSocket);
                CreateClientConnection connect = new CreateClientConnection(clientList,connection); 
                new Thread(connect).start();    
              
            }
            
             
        } catch (Exception ex) {
            System.out.println("Server starting failed");
        }
        
        
    }
    
    
    public static void main(String[] args) {
        new Server(22000);
    }
    
    
    
}
