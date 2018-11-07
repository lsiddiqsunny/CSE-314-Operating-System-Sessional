/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spserver;


import NetUtil.ConnectionUtillities;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;



/**
 *
 * @author user
 */
public class Initialize implements Runnable {
    public ServerSocket sSocket;
    public static HashMap<String,String> IPMap=new HashMap<>();
    public static HashMap<String,Boolean> receiverBusy=new HashMap<>();
    public static HashMap<String,ArrayList<String> >receiverFile=new HashMap<>();
    Thread thread;
    
    
    public Initialize(int port) throws IOException
    {
        sSocket=new ServerSocket(port);
        thread = new Thread(this);
        thread.start();
    }
    
    String socketAddressToIP(SocketAddress sa)
    {
        String tempIP=sa.toString();
        int it;
        for(it=0;it<tempIP.length();it++)    {
            if(tempIP.charAt(it)==':')    break;
        }
        String clientIP=tempIP.substring(0,it);
        return clientIP;
    }
    
    
    @Override
    public void run() 
    {
        try{
           while(true)    {
               Socket clientSocket=sSocket.accept();
               SocketAddress socketIP=clientSocket.getRemoteSocketAddress();
               String clientIP=socketAddressToIP(socketIP);
               ConnectionUtillities connection=new ConnectionUtillities(clientSocket);
               String clientID=connection.read().toString();
               String type=connection.read().toString();
               
               
               
               if(clientID.equals("$$$terminating$$$"))    {
                   break;
               }
               
               System.out.println(clientID+" is trying to start connection as "+type+" from IP-"+clientIP);
               
               
               if(IPMap.containsKey(clientID) && !IPMap.get(clientID).equals(clientIP))    {
                    System.out.println(clientID+"is already connected from another IP!");
                    connection.write("loginnotok");
                    connection.Close();
               }
               
               else    {
                   System.out.println(clientID+" has connected as "+type);
                   if(type.equals("sender"))    {
                       connection.write("loginok");
                       IPMap.put(clientID,clientIP);
                       Sender sender=new Sender(connection,clientID);
                       
                       
                   }
                   
                   else    {
                       receiverBusy.put(clientID,Boolean.FALSE);
                       Receiver receiver=new Receiver(connection,clientID);
                   }
                   
               }
               
           }
        }
        catch(IOException ex)    {
            //ex.printStackTrace();
        }
    }
}
