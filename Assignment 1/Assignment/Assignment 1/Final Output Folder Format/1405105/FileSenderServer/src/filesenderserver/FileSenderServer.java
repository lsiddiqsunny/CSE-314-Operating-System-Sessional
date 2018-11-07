/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filesenderserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


/**
 *
 * @author hp
 */
public class FileSenderServer {
     int i=0;
    // counter for clients
 
    public static void main(String[] args) throws IOException
    {
        // server is listening on port 1234
        ServerSocket ss = new ServerSocket(1234);
        
        
        Socket s;
        while(true) {
        try {
//         running infinite loop for getting
        // client request2221
            // Accept the incoming request
            s = ss.accept();
            DataInputStream di = new DataInputStream(s.getInputStream());
            DataOutputStream ds = new DataOutputStream(s.getOutputStream());
            System.out.println("New client request received: " + s.getInetAddress() + " with port number " +s.getPort());
             
            // obtain input and output streams
          
            // Create a new handler object for handling this request.
            WorkThread mtch = new WorkThread(s,di,ds);
 
            // Create a new Thread with this object.
            Thread thread = new Thread(mtch);

        //   System.out.println("Adding this client to active client list");
 
           // add this client to active clients list
//            ar.add(mtch);
            
            // start the thread.
            thread.start();
         //   t1.start();
           // di.readUTF();
          //  di.close();
          //  ds.close();
        }
        catch (Exception e) {
        //    s.close();
           // e.printStackTrace();
            }
        }
            // increment i for new client.
            // i is used for naming only, and can be replaced
            // by any naming scheme
            //i++;
     }
 }