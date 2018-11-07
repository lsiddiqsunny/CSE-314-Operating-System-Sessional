/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

import filetransfer.NetworkUtil;
import java.net.SocketTimeoutException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client {
    String serverAddr;
    int serverPort;
    
    public Client(String ip,int port){
        serverAddr = ip;
        serverPort = port;
        NetworkUtil server1 = new NetworkUtil(ip, port);
        NetworkUtil server2;
        String iniMsg = "SenderSocket";
        server1.write(iniMsg);
        String msg = null;
        try {
            msg = server1.read().toString();
        } catch (SocketTimeoutException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println(msg);
        String userID;
        Scanner in = new Scanner(System.in);
        if(msg.contains("Enter your userID:")){
                userID = in.nextLine();
                server1.write(userID);
            try {
                msg = server1.read().toString();
            } catch (SocketTimeoutException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
                System.out.println(msg);
                if(msg.contains("ok")){
                    server2 = new NetworkUtil(ip, port);
                    new Thread(new Clientsender(server1)).start();
                    new Thread(new Clientreceiver(server2,userID)).start();
                }
                
                
        }
    }
    public static void main(String[] args) {
        //Scanner in = new Scanner(System.in);
        //System.out.println("Input Ip Address:");
        String ip = "localhost";
        //System.out.println("Input Port:");
        int port = 5050;
        new Client(ip,port);
        
    }
}
