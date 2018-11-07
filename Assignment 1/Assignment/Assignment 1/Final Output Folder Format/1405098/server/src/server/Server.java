/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.net.*;
import java.io.*;

public class Server {

    public static int ClientCount = 0;
    public static String OnlineClient[];
    public static int ID;
    
  

    public static void main(String[] args) {

        InputStreamReader IS;
        PrintWriter pr;
        BufferedReader br;
        String line;
        OnlineClient = new String[120];
        for(int i=0;i<120;i++)
        {
            OnlineClient[i]="";
        }
        String str="";
        try {
            ServerSocket SSocket = new ServerSocket(1558);
            System.out.println("Server Started Successfully");

            while (true) {

                Socket ConnectionSocket = SSocket.accept();
                IS = new InputStreamReader(ConnectionSocket.getInputStream());
                pr = new PrintWriter(ConnectionSocket.getOutputStream());
                br = new BufferedReader(IS);
                str=str+ConnectionSocket.getRemoteSocketAddress();
                try {
                    line = br.readLine();
                    ID = Integer.parseInt(line);

                } catch (Exception ex) {
                    System.out.println("Error reading from client");
                }
                int key = ID % 120;
                if (OnlineClient[key]=="") {
                    ClientConnected c = new ClientConnected(ConnectionSocket, ID);
                    Thread t = new Thread(c);
                    t.start();
                    ClientCount++;
                    System.out.println("Clinet Connected :" + ID + "\n" + "Number of connected Client: " + ClientCount);
                    OnlineClient[key] = str;
                    pr.println("yes");
                    pr.flush();
                } else if(OnlineClient[key]!="") {
                    pr.println(str);
                    pr.flush();
                 //   System.out.println("You are already logged in with IP address: "+str);
                    ConnectionSocket.close();
                   
                    
                }
                

            }

        } catch (Exception e) {
            System.err.println("ServerSocket Could not be started" + "\n" + "Exiting......");
        }

    }

}
