/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filetransfer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    
    public ServerSocket servSock;
    HashMap<String,NetworkUtil> clientList;
    HashMap<String, NetworkUtil>  clientList2;
    
    public static int maxSize = 104857600;
    public static int currentSize = 0;
    
    Server(){
        clientList = new HashMap<>();
        clientList2 = new HashMap<>();
        //HashMap<String,>
        try {
            servSock = new ServerSocket(5050);
            System.out.println(InetAddress.getLocalHost());
            while(true){
                Socket client = servSock.accept();
                NetworkUtil nu = new NetworkUtil(client);
                new Thread(new ClientThread(nu,clientList,clientList2)).start();
            }
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
