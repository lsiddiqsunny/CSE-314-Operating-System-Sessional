/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package FileTransmitter;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 *
 * @author uesrs
 */
public class Server {
    
    public ServerSocket servSocket;
    public static int maxSize ;
    public static int curSize ;
    public static int curFileID ;
    HashMap<String,ClientInfo> clientList;
    public static Vector chunks = new Vector() ; 
    
    public static synchronized void removeChunks( int fileID)
    {
        System.err.println("chunks with fileID "+fileID + " gonna removed....");
        System.out.println("size of chunks is "+ chunks.size() + " before removal");
        for(int i = 0 ; i < chunks.size() ; i++)
        {
            Chunk chunk = (Chunk) chunks.get(i);
            if(chunk.fileID == fileID)
            {
                curSize -= chunk.bytesArray.length ;
                chunks.remove(i);
                i--;
            }
        }
        System.out.println("size of chunks is "+ chunks.size() + " after removal");
    }
    public Server(int port){
        
        clientList=new HashMap<String, ClientInfo>();
        
        try {
            servSocket=new ServerSocket(port);
           
            while(true){
                //printHashMap();
                //System.out.println("sigh");
                Socket clientSocket=servSocket.accept();
                ConnectionUtillities connection=new ConnectionUtillities(clientSocket);
                Object o=connection.read();
                String username=o.toString();     
                if(clientList.containsKey(username))
                {
                    System.out.println("Student "+username+"'s login is denied by server");
                    connection.write("login Failed");
                }
                else 
                {
                    clientList.put(username, new ClientInfo(connection, username) );
                    connection.write("login done!");
                    System.out.println("student "+username+ " is online now!!");
                    new Thread(new ServerReaderWriter(username,connection, clientList)).start(); 
                }


                            
            }
            
             
        } catch (Exception ex) {
            System.out.println("jhamela jhamela");
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
        System.out.println("Server starts");
        //System.out.println("please write the maximum size of all chunks in Server");
        //Scanner in=new Scanner(System.in);
        //maxSize = in.nextInt() ;
        maxSize = 1000000000; 
        curSize = 0 ;
        curFileID = 0 ;
        new Server(22222);
    }
}
