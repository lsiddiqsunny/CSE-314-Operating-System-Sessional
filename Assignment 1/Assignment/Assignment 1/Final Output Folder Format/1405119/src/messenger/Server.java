package messenger;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import util.ConnectionUtillities;


public class Server {
    
    public ServerSocket servSocket;
    HashMap<String,Information> clientList;
    static HashMap<String,FileInfo> fileList;
    static long chunksize;
    static int chunkno;
    static long rest;
    static String q,d;
    static int i;
    public Server(int port){
        
        chunksize=1000;
        chunkno=100;
        rest=chunkno*chunksize;
        q="id";
        i=0;
        clientList=new HashMap<String, Information>();
        fileList=new HashMap<String,FileInfo>();
        
        try {
            servSocket=new ServerSocket(port);
            System.out.println("Server is created");
            
            while(true){
                Socket clientSocket=servSocket.accept();
                System.out.println(clientSocket);
                ConnectionUtillities connection=new ConnectionUtillities(clientSocket);
                new Thread(new CreateClientConnection(clientList,connection)).start();                
            }
            
             
        } catch (Exception ex) {
            //System.out.println("Excecption at server dada");
        }
        
        
    }
    
    static String fileID()
    {
        i++;
        d = q + Integer.toString(i);
        return d;
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
        Server server = new Server(5678);
    }
}
