/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package messenger;

import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import javax.print.attribute.HashAttributeSet;
import util.ConnectionUtillities;
import util.FileInfo;

/**
 *
 * @author uesr
 */
public class Server {
    
    public ServerSocket servSocket;
    public static HashMap<String,Information> clientList;
    public static ConcurrentHashMap<Integer,FileInfo> fileMap;
    //public static HashMap<String,Integer> receiverAndFileID;
    public static ArrayList<String> receivers;

    public static int serverStorage;
    public static int remainingStorage;
    public static int fileID;

    
    public Server(int port)
    {
        clientList = new HashMap<String, Information>();
        fileMap = new ConcurrentHashMap<Integer, FileInfo>();
        //receiverAndFileID = new HashMap<String, Integer>();
        receivers = new ArrayList<String>();

        serverStorage = 1000 * 1000 * 1000; // bytes 1000kb
        remainingStorage = serverStorage;
        fileID = 0;
        
        try {
            servSocket=new ServerSocket(port);
            
            while(true){
                //printHashMap();
                Socket clientSocket=servSocket.accept();
                ConnectionUtillities connection=new ConnectionUtillities(clientSocket);
                new Thread(new CreateClientConnection(connection)).start();
            }
            
             
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        
    }
    
    public void printHashMap(){
        
        Set set = clientList.entrySet();
      
        Iterator i = set.iterator();      
        System.out.println("Current User--");
        while(i.hasNext()) {
           Map.Entry me = (Map.Entry)i.next();
           System.out.println(me.getKey() + " : ");                   
        }
    
        
    }
    
    
    public static void main(String[] args) {
        new Server(22228);
    }
}
