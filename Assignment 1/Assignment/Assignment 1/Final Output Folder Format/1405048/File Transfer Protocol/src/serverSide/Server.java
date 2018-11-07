/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package serverSide;


import util.ConnectionUtilities;
import util.Information;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

/**
 *
 * @author Antu
 */
public class Server{
    
    private ServerSocket serverSenderSocket,serverReceiverSocket;
    public HashMap<String, Information> storage;



    public Server(int port){
        
        storage  = new HashMap<>();

        try {
            serverSenderSocket=new ServerSocket(port);
            serverReceiverSocket = new ServerSocket(33333);

            while(true){
                Socket senderSocket=serverSenderSocket.accept();
                ConnectionUtilities senderConnection=new ConnectionUtilities(senderSocket);

                Socket receiverSocket = serverReceiverSocket.accept();
                ConnectionUtilities receiverConnection = new ConnectionUtilities(receiverSocket);
                new Thread(new CreateClientConnection(storage,senderConnection,receiverConnection)).start();
            }
            
             
        } catch (Exception ex) {
            System.out.println("Server Terminated");
//            ex.printStackTrace();
            System.exit(0);
        }
        
        
    }

    public static void main(String[] args) { new Server(22222); }
}
