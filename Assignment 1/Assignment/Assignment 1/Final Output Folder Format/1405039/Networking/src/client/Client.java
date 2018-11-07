package client;

import java.util.*;
import java.net.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import utility.ConnectionSetup;


public class Client {
    
    public static void main(String[] args) throws SocketException {
        System.out.println("Enter your username : ");
        
        Scanner in = new Scanner(System.in);
        String username=in.nextLine();
        ConnectionSetup connection=new ConnectionSetup("127.0.0.1",22222, username);
        connection.sc.setSoTimeout(30000);
                               
        connection.write((Object)username);
        try {
            String response = (String)connection.read();
            if(response.equalsIgnoreCase("true"))
            {
                System.out.println("Connected.");
                Thread t = new Thread(new ClientReaderWriter(connection));
                t.start();
            }
            else{
                System.out.println("The same student is already logged in.");
            }
        } catch (Exception ex) {
            System.out.println("Error in client response receiving.");
        }
   
        while(true);
    }
}
