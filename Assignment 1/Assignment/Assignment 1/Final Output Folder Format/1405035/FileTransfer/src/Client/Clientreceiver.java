/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

import filetransfer.NetworkUtil;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Clientreceiver implements Runnable {
    NetworkUtil server;
    String userID;
    public Clientreceiver(NetworkUtil server,String userID) {
        this.userID = userID;        
        this.server = server;
    }

    @Override
    public void run() {
        Scanner input = new Scanner(System.in);
        String iniMsg = "RecieverSocket";
        server.write(iniMsg);
        String msg = null;
        try {
            msg = server.read().toString();
        } catch (SocketTimeoutException ex) {
            Logger.getLogger(Clientreceiver.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(msg.contains("userID")){
            server.write(userID);
        }
        while(true){
            try {
                //System.out.println(msg);
                msg = server.read().toString();
            } catch (SocketTimeoutException ex) {
                Logger.getLogger(Clientreceiver.class.getName()).log(Level.SEVERE, null, ex);
            }
            if(msg.contains("Do you want to accept?")){
                System.out.println(msg);
                String tmp = input.nextLine();
                //System.out.println(tmp);
                if(tmp.contains("y")){
                    server.write(tmp);
                    
                    try {
                        byte []file = (byte[])server.read();
                        msg="Enter File path: ";
                        System.out.print(msg);
                        String path=input.nextLine();
                        FileOutputStream fos = new FileOutputStream(path);
                        fos.write(file);
                        fos.close();
                        System.out.println("File Stored");
                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(Clientreceiver.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(Clientreceiver.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                }
                else{
                    server.write(tmp);
                }
            }
            
        }
    }
    
}
