/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package finalserver;

import static finalserver.Finalserver.clientt;
import static finalserver.Finalserver.receivefile;

import static finalserver.Finalserver.rolls;

import static finalserver.Finalserver.sz;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Ayesha
 */
public class Finalserver {

    public static String rolls[] = new String[1001];
    public static Socket clientt[] = new Socket[151];
    public static int sz = 0;

    public static File receivefile(Socket s, String f) throws FileNotFoundException, IOException {
        File file = new File(f);

        FileOutputStream fos = new FileOutputStream(file);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        InputStream is = s.getInputStream();

        byte[] contents = new byte[10000];
        int bytesRead = 0;

        while ((bytesRead = is.read(contents)) != -1) {
            bos.write(contents, 0, bytesRead);
        }

        bos.flush();
        s.close();
        return file;
    }

    public static void main(String argv[]) throws Exception {
        int workerThreadCount = 0;
        int id = 1;
        
        
//        byte b = -128;
//        String bitt = Integer.toBinaryString(b & 0xFF);
//        System.out.println(bitt);
        
        
        
        
        
        
        ServerSocket welcomeSocket = new ServerSocket(6789);
        while (true) {
            Socket connectionSocket = welcomeSocket.accept();
            WorkerThread wt = new WorkerThread(connectionSocket, id);
            Thread t = new Thread(wt);
            t.start();
            workerThreadCount++;
            System.out.println("Client [" + id + "] is now connected. No. of worker threads = " + workerThreadCount);
            id++;
        }

    }
}

class WorkerThread implements Runnable {

    private Socket connectionSocket;
    private int id;

    public WorkerThread(Socket ConnectionSocket, int id) {
        this.connectionSocket = ConnectionSocket;
        this.id = id;
    }

    @Override
    public void run() {
        String client1, send, client2;
        while (true) {
            try {
                DataOutputStream outToServer = new DataOutputStream(connectionSocket.getOutputStream());
                BufferedReader inFromServer = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                client1 = inFromServer.readLine();
                //send = inFromServer.readLine();
                //System.out.println(send);
                int flag = 0;
                for (int i = 0; i < sz; i++) {
                    //System.out.println(rolls[i]);
                    if (rolls[i].equalsIgnoreCase(client1)) {
                        flag = 1;
                        break;
                    }
                }
                if (flag == 0) {

                    int xx = Integer.valueOf(client1);
                    xx %= 1405000;
                    clientt[xx] = connectionSocket;
                    rolls[sz++] = client1;
                    send = inFromServer.readLine();
                    System.out.println(send);
                    
                    String destr = new String();
                    int cnt=0;
                    
                    for(int i=8;i<send.length()-8;i++)
                    {
                        
                        if(cnt==5)
                        {
                            cnt=0;
                        }
                        else destr+=send.charAt(i);
                        if(send.charAt(i)=='1')
                        {
                            cnt++;
                        }
                        else cnt=0;
                        
                    }
                    System.out.println(destr);
                    
                    
                    //File file = receivefile(clientt[xx], client1);

                } else {

                    System.out.println("client already connected");
                    connectionSocket.close();

                }
            } catch (Exception e) {

            }
        }
    }
}
