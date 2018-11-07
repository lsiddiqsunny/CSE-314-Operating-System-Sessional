/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Offline2V4;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ASUS
 */
public class SendfromServer extends Thread {

    Socket socket;
    ServerFrame2 sf;
    SendtoServer sendtoserver;
    String Reciever, Sender, filename;

    SendfromServer(Socket socket, String filename, String Sender, String Reciever, ServerFrame2 sf) {
        this.socket = socket;
        this.filename = filename;
        this.Sender = Sender;
        this.Reciever = Reciever;
        this.sf = sf;
       // System.out.println(socket + " " + filename + " " + Sender + " " + Reciever);
    }

    @Override
    public void run() {

        if (socket != null) {
            //System.out.println(socket);
        }
        Socket cSock = sf.getClientFileSharingSocket(Reciever);

        //System.out.println(cSock);
        FileInputStream fis = null;
        if (cSock != null) {
            System.out.println("Trying sending file ...");
            try {
                File file = new File(filename);
                fis = new FileInputStream(file);
                BufferedInputStream bis = new BufferedInputStream(fis);

                OutputStream os = cSock.getOutputStream();

                byte[] contents;
                long fileLength = file.length();
                long current = 0;
                long start = System.nanoTime();
                while (current != fileLength) {
                    int size = 10000;
                    if (fileLength - current >= size) {
                        current += size;
                    } else {
                        size = (int) (fileLength - current);
                        current = fileLength;
                    }
                    contents = new byte[size];
                    bis.read(contents, 0, size);
                    //System.out.println(new String(contents));
                    os.write(contents);
                    System.out.print("Sending file ... " + (current * 100) / fileLength + "% complete!");
                }
                os.flush();

                // socket.close();
                System.out.println("File sent succesfully!");
            } catch (FileNotFoundException ex) {
                System.out.println("no one found to send file");
            } catch (IOException ex) {
                System.out.println("no one found to send file2");
            } finally {
                try {
                    fis.close();
                } catch (IOException ex) {
                    System.out.println("no one found to send file3");
                }
            }
        } else {
            System.out.println("ei nam er kono socket nai");
        }
    }
}
