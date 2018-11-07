/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server_new;

import java.io.*;
import java.net.*;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author user
 */
public class ServerThread extends Thread {

    int clientID;
    int receiverID;

    short what;

    long fileSize;
    int chunkSize;
    String fileName;

    Socket socket;
    DataInputStream dIn;
    DataOutputStream dOut;

    ServerThread(Socket socket) throws IOException {
        this.socket = socket;
        dIn = new DataInputStream(socket.getInputStream());
        dOut = new DataOutputStream(socket.getOutputStream());
    }

    void Sending() {
        System.out.println("Really");
    }

    @Override
    public void run() {
        try {
            //id nilam
            Thread.sleep(5);
            clientID = dIn.readInt();

        } catch (IOException ex) {
            System.out.println("Failed to get ClientID");
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
            try {
                dOut.writeBoolean(false);
            } catch (IOException ex1) {
                Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex1);
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            //id already asey kina check krlam
            if (Server_New.FindInOnline(clientID)) {
                dOut.writeBoolean(false);
                socket.close();
            } else {
                dOut.writeBoolean(true);
                Server_New.InsertIntoOnline(clientID);
                System.out.println(clientID + " is connected");
            }
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            //Server ki recieve krbe na send krbe?
            what = dIn.readShort();
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (what == 43) {//sender side er kaaj

            while (true) { //thik receiver na paua porjonto receiver id nitei thakbey

                //reciever ID nilam
                try {
                    Thread.sleep(10);
                    receiverID = dIn.readInt();
                } catch (IOException ex) {
                    Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
                }

                //reciever online asey kina check kra lagbe
                if (Server_New.FindInOnline(receiverID) && receiverID != clientID) {
                    try {
                        dOut.writeBoolean(true);
                        break; /*online e thakle break*/

                    } catch (IOException ex) {
                        Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    try {
                        dOut.writeBoolean(false);
                    } catch (IOException ex) {
                        Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

            //file size nite hobe
            try {
                fileSize = dIn.readLong();
            } catch (IOException ex) {
                Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
            }

            //chunkSize pathailam
            if (Server_New.buffStart + fileSize < 1024 * 1024 * 5) {

                chunkSize = Math.abs(new Random().nextInt() % 500) + 1;
                if (chunkSize * 255 < fileSize) {
                    chunkSize += (fileSize / 255);
                }

                try {
                    dOut.writeBoolean(true);
                    dOut.writeInt(chunkSize);
                    Thread.sleep(500);
                } catch (IOException | InterruptedException ex) {
                    Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                //file er naam nitey hobey
                try {
                    fileName=dIn.readUTF();
                    System.out.println("User "+clientID+" will send "+fileName+" of size "+fileSize+" to user "+receiverID+" with chunk size "+chunkSize+".");
                } catch (IOException ex) {
                    Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
                }
                //ei khanei chunk neua, de-stuffing... shob kra lagbey
                

                
                
                
                
                
                    //ei khanei chunk neua, de-stuffing... shob kra lagbey
                
            } else { //buffer e jaiga nai
                try {
                    dOut.writeBoolean(false);
                } catch (IOException ex) {
                    Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            
        } else if (what == 20) {
            //reciever er side er kaaj

        } else {
            System.out.println("No Action to Do.\nServer is closing.");
            System.exit(0);
        }

    }

}
