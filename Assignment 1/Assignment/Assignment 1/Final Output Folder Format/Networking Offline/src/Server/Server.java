package Server;

import Utility.NetworkUtil;

import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Server {
    private int i = 1;
    public Hashtable<String, NetworkUtil> table=new Hashtable<>();
    public Hashtable<String, Queue< ArrayList<byte[]> >> FileStorage=new Hashtable<>();
    public Hashtable<String, Queue< String >> Senders=new Hashtable<>();

    public Long BufferSize= ThreadLocalRandom.current().nextLong(1000000L,1500000L);
    public Long UsedBuffer=0L;
    public Long FileCount=0L;


    public Server(){
        System.out.println("Server Started");
        table = new Hashtable<>();
        try{
            ServerSocket ServSock = new ServerSocket(33333);
            while(true){
                Socket clientSock = ServSock.accept();
                NetworkUtil nc=new NetworkUtil(clientSock);

                String UserName=(String)nc.read();
                System.out.println("UserName : "+UserName);

                if(table.containsKey(UserName)) nc.write("Error : LogIn from Multiple IP denied");
                else{
                    table.put(UserName,nc);
                    if(!FileStorage.containsKey(UserName)) FileStorage.put(UserName,new LinkedList<>());
                    nc.write("OK : LogIn Successful");
                    new ServerThread(nc,this,UserName);
                }
            }
        }
        catch(Exception e) {
            System.out.println("Server starts:"+e);
        }

    }

    public static void main(String[] args) {
        Server t=new Server();
    }
}


