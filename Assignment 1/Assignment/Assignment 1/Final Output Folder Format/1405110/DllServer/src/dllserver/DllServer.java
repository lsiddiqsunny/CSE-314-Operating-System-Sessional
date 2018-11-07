package dllserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Soumita
 */
public class DllServer {
    
    private static ServerSocket serverSocket;
    private static Socket clientSocket;

   
    
    
    public static void main(String[] args) throws IOException {

        try {
            serverSocket = new ServerSocket(4997);
            System.out.println("Server started.");
        } catch (Exception e) {
            System.err.println("Port already in use.");
            System.exit(1);
        }
        
         HashMap<String,Socket> clients=new HashMap<String,Socket>();
         HashMap<Integer,Socket> cli = new HashMap<Integer,Socket>();
         HashMap<Integer,Integer> clientid=new HashMap<Integer,Integer>();

        int userid;
        int c = 0;
        while (true) {
            try {
                clientSocket = serverSocket.accept();
                System.out.println("Accepted connection : " + clientSocket);
                
                c++;
                
                cli.put(c,clientSocket);
                clients.put(clientSocket.getInetAddress().getHostName(), clientSocket);
                Thread t = new Thread(new DllConnection(clientSocket,c,cli,clientid));

                t.start();
                System.out.println("client "+c +" has joined");
                

            } catch (Exception e) {
                System.err.println("Error in connection attempt.");
            }
           /* finally
            {
                if(serverSocket != null)
                    serverSocket.close();
                
            }*/
        }
    }
}