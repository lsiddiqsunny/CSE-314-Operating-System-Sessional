/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

import filetransfer.NetworkUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.SocketTimeoutException;

public class Clientsender implements Runnable {
    NetworkUtil server1 ;
    //NetworkUtil server2;
    String userID;
    public Clientsender(NetworkUtil server1) {
        this.server1 = server1;
        //this.server2 = server2;
    }

    @Override
    public void run() {
        Scanner in = new Scanner(System.in);
        String fileName;
        long fileSize;
        
        while(true){
            
            String msg;
            while(true){
                msg = in.nextLine();

                if(msg.equals("send")){
                    server1.write(msg);
                    try {
                        msg = server1.read().toString();
                    } catch (SocketTimeoutException ex) {
                        Logger.getLogger(Clientsender.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    System.out.println(msg);

                    String recieverID = in.nextLine();
                    server1.write(recieverID);
                    String shohan = null;
                    try {
                        shohan = server1.read().toString();
                    } catch (SocketTimeoutException ex) {
                        Logger.getLogger(Clientsender.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    if(shohan.equals("Reciever is not logged in!")){
                        System.out.println(shohan);
                        continue;
                    }
                    try {
                        //System.out.println(msg);
                        msg = server1.read().toString();
                    } catch (SocketTimeoutException ex) {
                        Logger.getLogger(Clientsender.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    if(msg.contains("Send filename and size")){
                        System.out.println("Enter file path:");
                        String path = in.nextLine();
                        File file = new File(path);
                        //String retMsg = file.getName() + ":" + file.length();
                        fileName = file.getName();
                        fileSize = file.length();
                        server1.write(fileName);
                        server1.write(fileSize);

                        try {
                            msg=server1.read().toString();
                        } catch (SocketTimeoutException ex) {
                            Logger.getLogger(Clientsender.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        if(msg.equals("break")){
                            System.out.println("Server Doesn't Have enough Space, transaction failed.");
                            continue;
                        }

                        int chunkSize = 0;
                        try {
                            chunkSize = (int)server1.read();
                        } catch (SocketTimeoutException ex) {
                            Logger.getLogger(Clientsender.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        try {
                            msg = server1.read().toString();
                        } catch (SocketTimeoutException ex) {
                            Logger.getLogger(Clientsender.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        if(msg.contains("Send")){
                            try {
                                FileInputStream fin = new FileInputStream(file);
                                byte []data = new byte[(int)chunkSize];

                                int count=0;
                                if((int)fileSize%chunkSize==0) count=(int)fileSize/chunkSize;
                                if((int)fileSize%chunkSize!=0) count=(int)fileSize/chunkSize +1;

                                for (int i = 0; i < count; i++) {
                                    fin.read(data);

                                    ArrayList tmpData = new ArrayList(chunkSize + 6);
                                    int count1, count51 = 0;
                                    int bitcount = 0;
                                    byte yoo = 0;

                                    byte a = (byte) 126;                       // 01111110 in starting
                                    byte b = (byte) 0;                         // 00000000 for data frame
                                    byte c = (byte) (i + 1);                   // sequence number
                                    tmpData.add(a);
                                    tmpData.add(b);
                                    for (int y = 7; y >= 0; y--) {
                                        byte z = (byte) ((c >> y) & 1);
                                        if (z == 1) {
                                            count51++;
                                        } else {
                                            count51 = 0;
                                        }
                                        yoo = (byte) ((byte) (z << (7 - bitcount)) | (byte) yoo);
                                        bitcount++;
                                        if (bitcount == 8) {
                                            tmpData.add(yoo);
                                            yoo = 0;
                                            bitcount = 0;
                                        }
                                        if (count51 == 5) {
                                            yoo = (byte) ((byte) (0 << (7 - bitcount)) | (byte) yoo);
                                            bitcount++;
                                            count51 = 0;
                                            if (bitcount == 8) {
                                                tmpData.add(yoo);
                                                yoo = 0;
                                                bitcount = 0;
                                            }
                                        }
                                    }
                                    count1 = 0;
                                    System.out.println("Frame "+(i+1)+" before bit-Stuffing :");
                                    for (int x = 0; x < data.length; x++) {
                                        for (int y = 7; y >= 0; y--) {
                                            byte z = (byte) ((data[x] >> y) & 1);
                                            System.out.print(z);
                                            if (z == 1) {
                                                count1++;
                                                count51++;
                                            } else {
                                                count51 = 0;
                                            }
                                            //System.out.print(z); 

                                            yoo = (byte) ((byte) (z << (7 - bitcount)) | (byte) yoo);
                                            bitcount++;

                                            if (bitcount == 8) {
                                                tmpData.add(yoo);
                                                yoo = 0;
                                                bitcount = 0;
                                            }

                                            if (count51 == 5) {
                                                yoo = (byte) ((byte) (0 << (7 - bitcount)) | (byte) yoo);
                                                bitcount++;
                                                count51 = 0;
                                                if (bitcount == 8) {
                                                    tmpData.add(yoo);
                                                    yoo = 0;
                                                    bitcount = 0;
                                                }
                                            }
                                        }
                                        //System.out.print(" ");
                                    }
                                    System.out.println();
                                    
                                    byte checksum;
                                    count1 = count1 % 128;
                                    if (count1 % 2 == 0) {
                                        checksum = (byte) ((byte) (0 << 7) | (byte) count1);
                                    } else {
                                        checksum = (byte) ((byte) (1 << 7) | (byte) count1);
                                    }

                                    for (int y = 7; y >= 0; y--) {
                                        byte z = (byte) ((checksum >> y) & 1);
                                        if (z == 1) {
                                            count51++;
                                        } else {
                                            count51 = 0;
                                        }

                                        yoo = (byte) ((byte) (z << (7 - bitcount)) | (byte) yoo);
                                        bitcount++;

                                        if (bitcount == 8) {
                                            tmpData.add(yoo);
                                            yoo = 0;
                                            bitcount = 0;
                                        }

                                        if (count51 == 5) {
                                            yoo = (byte) ((byte) (0 << (7 - bitcount)) | (byte) yoo);
                                            bitcount++;
                                            count51 = 0;
                                            if (bitcount == 8) {
                                                tmpData.add(yoo);
                                                yoo = 0;
                                                bitcount = 0;
                                            }
                                        }
                                    }

                                    for (int y = 7; y >= 0; y--) {
                                        if (y == 0 | y == 7) {
                                            yoo = (byte) ((byte) (0 << (7 - bitcount)) | (byte) yoo);
                                            bitcount++;
                                        } else {
                                            yoo = (byte) ((byte) (1 << (7 - bitcount)) | (byte) yoo);
                                            bitcount++;
                                        }
                                        if (bitcount == 8) {
                                            tmpData.add(yoo);
                                            yoo = 0;
                                            bitcount = 0;
                                        }
                                    }
                                    if (bitcount != 0) {
                                        tmpData.add(yoo);
                                    }
                                    //tmpData.add(a);                         // 01111110 in finishing
                                    System.out.println("Frame "+(i+1)+" after bit-Stuffing :");
                                    byte[] data1 = new byte[tmpData.size()];
                                    for (int j = 0; j < tmpData.size(); j++) {
                                        data1[j] = (byte) tmpData.get(j);
                                        for(int y=7;y>=0;y--){
                                            System.out.print((byte)((data1[j]>>y)& 1));
                                        }
                                    }
                                    byte mehrab = data1[3];
                                    //if((i+1)==2) data1[3]= 50;
                                    
                                    System.out.println(); 

                                    int framesize = tmpData.size();
                                    while (true) {
                                        server1.write(framesize);
                                        server1.writeByte(data1);
                                        data1[3] = mehrab;
                                        server1.setSoTimeout(10000);
                                        byte[] ack;
                                        try {
                                            ack = (byte[]) server1.read();
                                            System.out.println("Frame " + (i + 1) + " successfully recieved by server");
                                            break;

                                        } catch (SocketTimeoutException s) {
                                            System.out.println("Frame " + (i + 1) + " is timed out. send again.");
                                            continue;
                                        }
                                    }
                                }

                            } catch (FileNotFoundException ex) {
                                Logger.getLogger(Clientsender.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (IOException ex) {
                                Logger.getLogger(Clientsender.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            try {
                                msg = server1.read().toString();
                            } catch (SocketTimeoutException ex) {
                                Logger.getLogger(Clientsender.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            if(msg.contains("File Recieved")){
                                System.out.println(msg);
                                break;
                            }
                            else{
                                System.out.println(msg);

                            }


                        }
                    }
                }
            }
            
        
        }
    }
    
}
