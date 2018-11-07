/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filetransfer;

import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerSenderThread implements Runnable{
    HashMap<String, NetworkUtil> clientList;
    String recieverID;
    String senderID;
    NetworkUtil reciever;
    String fileName;
    int fileSize;
    byte file[];
    public ServerSenderThread(String recieverID, String senderID, HashMap<String,NetworkUtil> cl,String fileName,int fileSize,byte []file) {
        clientList = cl;
        this.recieverID = recieverID;
        this.senderID = senderID;
        this.fileName=fileName;
        this.fileSize=fileSize;
        this.file = file;
    }

    @Override
    public void run() {
        //System.out.println("In run");
        if(clientList.get(recieverID) == null){
            System.out.println("Sender is not online!\n");
            return;
        }
        else{
            reciever = clientList.get(recieverID);
            
                //System.out.println("In while");
                String msg = senderID + " have sent you a file named " +fileName +  " .Do you want to accept?(y/n):";
                reciever.write(msg);
                String retMsg = null;
            try {
                retMsg = reciever.read().toString();
            } catch (SocketTimeoutException ex) {
                Logger.getLogger(ServerSenderThread.class.getName()).log(Level.SEVERE, null, ex);
            }
                //System.out.println(retMsg);
                if(retMsg.equals("y")){
                    reciever.write(file);
                    System.out.println("Transaction Completed");
                }
                else{
                    System.out.println("File Deleted! & Transaction deleted!");
                }
                
                Server.currentSize-=fileSize;
                
            
        }
    }
        
}
