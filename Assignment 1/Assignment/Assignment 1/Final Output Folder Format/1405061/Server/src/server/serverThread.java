/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

/**
 *
 * @author ASUS
 */

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class serverThread implements Runnable {
    

    int inPort;
    Thread t;
    String id;
    static String log_id="-1";
    static String log_out_id="-1";
    String pass;
    String lp;
    ServerSocket welcomeSocket;
    Socket connectionSocket;
    BufferedReader inFromClient;
    DataOutputStream outToClient;
    DataInputStream byteFromClient;
    String comStr="";
    StringTokenizer st;
    loginInfoThread infoThread;
    serverProcessByte spb;
    byte []recByte=new byte[23];
   
        
    public serverThread(int port) throws IOException
    {
        inPort=port;
        spb=new serverProcessByte();
        welcomeSocket = new ServerSocket(inPort);
        infoThread=new loginInfoThread();
        t=new Thread(this);
        t.start();
    }

    @Override
    public void run() {
        
        
         while(true)
         {
             try {
                 Thread.sleep(1000);
                 connectionSocket = welcomeSocket.accept();
                 System.out.println(connectionSocket.getLocalPort());
                 inFromClient =
                 new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                 InputStream isr=connectionSocket.getInputStream();
                 byteFromClient=new DataInputStream(isr);
                 outToClient = new DataOutputStream(connectionSocket.getOutputStream());
               //  checkIdPass();
             //  fromClient=inFromClient
             
             
                byteFromClient.read(recByte);
                System.out.println("receive from client");
                spb.printByteArray(recByte);
               comStr = receiveProcessByte(recByte);
                
                 //comStr=inFromClient.readLine();
                 st=new StringTokenizer(comStr); //slice the input from client as command basically
                String temp="";
                 
                 while(st.hasMoreElements())
                 {
                     temp=st.nextToken();
                     // for login
                     
                     if(temp.equals("login"))
                     {
                         String temp1="";
                         String temp2="";
                         String temp3="";
                         temp1=st.nextToken();//id
                         temp2=st.nextToken();//pass
                         temp3=st.nextToken();//port
                         System.out.println(temp1+" "+temp2+" "+temp3);
                         //for checking name id 
                         infoThread.insertID(temp1,temp2,outToClient,inFromClient,byteFromClient);
                         break;
                     }
                 }
             }catch (InterruptedException | IOException ex) {
              //   Logger.getLogger(serverThread.class.getName()).log(Level.SEVERE, null, ex);
              System.out.println("hello");
             }
             
         }
    }
    
    
    
    
    public String receiveProcessByte(byte[]nb) throws IOException
    {
        //spb.printByteArray(nb);
        byte [] a=spb.extractFlag(nb);
        System.out.println("destuffed array");
        a=spb.destuffing(a);
       // spb.printByteArray(a);
       // spb.printByteArray(a);
        int checkSum=spb.getCheckSum(a);
        System.out.println("checkSum "+checkSum);
        a=spb.extractCheck(a);
        System.out.println("length "+a.length);
        int presentSum = spb.countOne(a);
        System.out.println("Extracted array");
        //spb.printByteArray(a);
     
        if( presentSum != checkSum)
        {
            System.out.println("data changed!!!"+presentSum+" "+checkSum);
            spb.printByteArray(a);
            sendErrorAcknowledgement();
            return null;   
        }
        else
        {
            sendSuccessAcknowledgement();
            return new String(a);
        }
    }
    
    
    public void sendErrorAcknowledgement() throws IOException
    {
        byte[]x=new byte[1];
        x="0".getBytes();
        byte []y=spb.addFlag(x);
        System.out.println("error");
        spb.printByteArray(y);
        outToClient.write(y);
    }
    
    public void sendSuccessAcknowledgement() throws IOException
    {
        byte[]x=new byte[1];
        x="1".getBytes();
        byte []y=spb.addFlag(x);
        System.out.println("success");
        spb.printByteArray(y);
        outToClient.write(y);
    }
   
    private void printHashtable(Hashtable<String, DataOutputStream> ht) {
        Enumeration<String>en=ht.keys();
        while(en.hasMoreElements())
        {
            System.out.println(en.toString());
        } //To change body of generated methods, choose Tools | Templates.
    }
}
