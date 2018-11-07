/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filetransfer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ClientThread implements Runnable{
    String userID;
    NetworkUtil client;
    HashMap<String,NetworkUtil> clientList2;
    HashMap<String,NetworkUtil> clientList;
    ClientThread(NetworkUtil client,HashMap<String,NetworkUtil> clientList,HashMap<String,NetworkUtil> clientList2){
           this.clientList = clientList;
           this.client = client;
           this.clientList2 = clientList2;
    }

    

    @Override
    public void run() {
        boolean reciever = false;
        while(true){
            String iniMsg = null;
            try {
                iniMsg = client.read().toString();
            } catch (SocketTimeoutException ex) {
                Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
            }
            if(iniMsg.contains("RecieverSocket")){
                reciever = true;
                break;
            }
            
                
            String msg = "Enter your userID:";
            client.write(msg);
            try {
                userID = client.read().toString();
            } catch (SocketTimeoutException ex) {
                Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            if(clientList.get(userID) != null){
                System.out.println("Duplicate ID is blocked successfully");
                msg = "You are already logged in another ID\nThis connection is closing down...";
                client.write(msg);
                client.closeConnection();
            }
            else{
                clientList.put(userID,client );
                System.out.println(userID + " successfully connected!");
                msg = "ok, You are connected";
                client.write(msg);
                while(true){
                    //msg = "Send filename and size:";
                    //client.write(msg);
                    String send = null;
                    try {
                        send = client.read().toString();
                    } catch (SocketTimeoutException ex) {
                        Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    if(send.equals("send")){
                        msg = "Send Reciever ID:";
                        client.write(msg);
                        String recieverID = null;
                        try {
                            recieverID = client.read().toString();
                        } catch (SocketTimeoutException ex) {
                            Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        if(clientList.get(recieverID) == null){
                            msg = "Reciever is not logged in!"; 
                            client.write(msg);
                            continue;
                        }
                        else client.write("halar vai");


                        msg = "Send filename and size:";
                        client.write(msg);
                        String fileName = null;
                        try {
                            fileName = client.read().toString();
                        } catch (SocketTimeoutException ex) {
                            Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        int fileSize = 0;
                        try {
                            fileSize = (int)(long)client.read();
                        } catch (SocketTimeoutException ex) {
                            Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        if((Server.currentSize+fileSize)>Server.maxSize){
                            msg="break";
                        }
                        else msg="ok";
                        client.write(msg);
                        if(msg.equals("break")) continue;

                        Server.currentSize+=fileSize;

                        int randomNum = (fileSize/2 - fileSize/5) + (int)(Math.random() % (fileSize/5)) ; 
                        if(randomNum==0) randomNum++;

                        client.write(11);

                        client.write("Send");
                        byte[] file = new byte[fileSize];
                        randomNum=11;
                        int count=0;
                        if((int)fileSize%randomNum==0) count=(int)fileSize/randomNum;
                        if((int)fileSize%randomNum!=0) count=(int)fileSize/randomNum +1;
                        int curr=0 , flag=0;
                        
                        System.out.println("Transaction User "+userID+" to "+recieverID+", FileName "+fileName);
                        
                        for(int i =0;i<count;i++){
                            byte[] file2 ;

                            int framesize = 0;
                            try {
                                framesize = (int)client.read();
                            } catch (SocketTimeoutException ex) {
                                Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            byte[] file1 = new byte[framesize];
                            client.readByte(file1,0,framesize);
                            //fis.read(data, 0, (int) randomNum);
                            //server1.write(data);
                            ArrayList tmpData = new ArrayList(randomNum+5);
                            int count1 = 0, count51 = 0;
                            int bitcount=0, done=0;
                            byte yoo=0;
                            
                            System.out.println("Frame "+(i+1)+" before bit-Destuffing :");
                            for(int x=0;x<file1.length;x++){
                                for(int y=7;y>=0;y--){
                                    byte z=(byte)((file1[x]>>y)& 1);
                                    System.out.print(z);
                                    if(z==0 && count51==5) {count51=0; continue;}
                                    else if(z==1 && x>0) { count51++; }
                                    else if(z==0) count51=0;
                                    yoo=(byte) ((byte) (z<<(7-bitcount)) | (byte) yoo);
                                    bitcount++;                                                
                                    if(bitcount==8){
                                        tmpData.add(yoo);
                                        yoo=0;
                                        bitcount=0;
                                        //System.out.print(" ");
                                    } 
                                    //if(done==1) break;
                                    //if(count51==6){done=1;}
                                }       
                            }   
                            System.out.println();
                                    
                            System.out.println("Frame "+(i+1)+" after bit-Destuffing :");
                            file2 = new byte[tmpData.size()-5];
                            for(int j=0;j<tmpData.size()-5;j++){
                                file2[j] = (byte)tmpData.get(j+3);
                                for(int y=7;y>=0;y--){
                                    byte z=(byte)((file2[j]>>y)& 1);
                                    System.out.print(z);
                                    if(z==1) count1++;
                                }
                            }
                            System.out.println();

                            byte checksum;
                            count1=count1%128;
                            
                            if(count1%2==0) checksum = (byte) ((byte) (0<<7) | (byte) count1);
                            else checksum = (byte) ((byte) (1<<7) | (byte) count1);
                            
                            if(flag==(i+1)){
                                if(count1%2==0) checksum = (byte) ((byte) (1<<7) | (byte) count1);
                                else checksum = (byte) ((byte) (0<<7) | (byte) count1);
                                flag=0;
                            }
                            
                            if((byte)checksum==(byte)tmpData.get(tmpData.size()-2)){
                                //System.out.println("ok");
                                byte[] ack = new byte[(int)4];
                                ack[0]=(byte)126;
                                ack[1]=(byte)1;
                                ack[2]=(byte)(i+1);
                                ack[3]=(byte)126;
                                client.write(ack);
                            }
                            else {
                                //System.out.println("not ok");
                                System.out.println("Something wrong. send again.");
                                i--;
                                continue;
                            }                                       
                            if(i<count-1){
                                for(int j=0;j<randomNum;j++){
                                    file[j + curr] = file2[j];
                                }
                            }
                            else{
                                for(int j=0;j<fileSize-randomNum*(count-1);j++){
                                    file[j + curr] = file2[j];
                                }
                            }
                            curr+=randomNum;
                        }

                        msg = "Full file recieved by server";
                        client.write(msg);
                        new Thread(new ServerSenderThread(recieverID, userID, clientList2,fileName,fileSize,file)).start();
                    }
                }
            }
        }
               
        if(reciever == true){
            String msg = "Return userID";
            client.write(msg);
            String userID = null;
            try {
                userID = client.read().toString();
            } catch (SocketTimeoutException ex) {
                Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
            }
            clientList2.put(userID, client);
        }
    }
}
