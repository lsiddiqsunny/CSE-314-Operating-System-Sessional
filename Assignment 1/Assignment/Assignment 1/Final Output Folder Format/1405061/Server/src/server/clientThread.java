/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.*;
import java.net.*;
import java.nio.file.Path;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.stage.Stage;

/**
 *
 * @author ASUS
 */
public class clientThread  {

    String sentence;
    String stId;
    String stPass;
    String modifiedSentence;
    Thread t;
    int inPort=0;
    BufferedReader inFromUser;
    Socket clientSocket ;
    DataOutputStream outToServer;
    DataInputStream byteFromServer;
    BufferedReader inFromServer;
    String userIn="";
    StringTokenizer st;
    String temp="";
    String filePath="";
    clientOnlineThread col;
    ActionEvent event;
    boolean send_info;
    boolean log_var;
    byte[]flag=new byte[3];// number of bytes that contain acknowledgement
    clientProcessByte cpb;
    int x=-1;
    
    clientThread(int port) throws IOException
    {
        this.inPort=port;
        inFromUser = new BufferedReader(new InputStreamReader(System.in));
        clientSocket = new Socket("localhost", inPort);
        byteFromServer=new DataInputStream(clientSocket.getInputStream());
        System.out.println(clientSocket.getLocalPort());
        System.out.println(clientSocket.getPort());
        InetAddress ipAddr = InetAddress.getLocalHost();
        System.out.println(ipAddr.getHostAddress());
        outToServer = new DataOutputStream(clientSocket.getOutputStream());
        inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        System.out.println("Enter Student ID and Password");
        cpb=new clientProcessByte();
    }
    
    public void start(String id, String pass) throws IOException
    {
        stId = id;
        stPass = pass;
        login();
        while(send_info != true)
        {
            login();
        }
    }
    synchronized public boolean login() throws IOException
    {
        String log_st="";
        try {
            String lp=Integer.toString(clientSocket.getLocalPort());
            String st="login"+" "+stId+" "+stPass+" "+lp+'\n';
            byte[]x=st.getBytes();
            System.out.println("length of array"+x.length);
            x=sendProcessByte(x);
            outToServer.write(x);
           // outToServer.writeBytes("login"+" "+stId+" "+stPass+" "+lp+'\n');
            System.out.println("Successfull from client");
            byteFromServer.read(flag);
            
            //problem
            
            log_st=receiveProcessByte(flag);
            System.out.println(log_st);
            
            System.out.println("FROM SERVER: " + log_st);
        } catch (IOException ex) {
            Logger.getLogger(clientThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(log_st.equals("1"))
        {
            String s="";
            send_info=true;
            byteFromServer.read(flag);
            s=receiveProcessByte(flag);
            if(s.equals("1"))
            {
                log_var=true;
           System.out.println("login completed");
           col= new clientOnlineThread(outToServer,byteFromServer,inFromServer);
           FileSendController.col=col;
           FileSendController.ct = this;
           FileSendController.studentId= stId;
           x=1;
           return true;
            }
            else if(s.equals("2"))
            {
                System.out.println("Wrong name or password");
                x=2;
                return false;
            }
            else if(s.equals("3"))
            {
                System.out.println("Already logged in");
                x=3;
                return false;
            }
            else return false;
        }
        else 
        {
            System.out.println("halted here "+log_st+"111");
            return false;
        }
    }
    
    public byte[] sendProcessByte(byte[] a)
    {
        int c=cpb.countOne(a);
        System.out.println("number of 1 --> "+c);
        byte[] b=cpb.addCheckSum(c,a);
        b=cpb.stuffing(b);
        b=cpb.addFlag(b);
        cpb.printByteArray(b);
        return b;
    }
    
    
    public String receiveProcessByte(byte[] a)
    {
        //specially for acknowledgement
        System.out.println("Acknowledgement here");
        cpb.printByteArray(a);
        System.out.println();
        byte []x=cpb.extractFlag(a);
        cpb.printByteArray(x);
        String s=new String(x);
        return s;
    }
    
    
    
    
    
    
    public void logout() throws IOException
    {
         byte []x=("logout"+" "+stId+'\n').getBytes();
         byte []y=cpb.addFlag(x);
         col.logout=true;
         outToServer.write(y);
         send_info=false;
         log_var=false;
         clientSocket.close();
         return;
    }
    public void checkFile(String fName,String rec) throws IOException, FileNotFoundException, InterruptedException {
     
       String fileName="";
       String receiver=rec;
       
       filePath=fName;
       
       System.out.println(filePath);
       File file=new File(filePath);
       fileName = file.getName();
       long bytes=file.length();
       double db=file.length();
       if(!file.exists()){
           System.out.println("not exist!!! TRY AGAIN");
           file.createNewFile();}
       System.out.println("file length "+file.length());
       
       //sending file info
       
      String s="send"+" "+stId+" "+fileName+" "+bytes+" "+receiver+'\n';
      byte [] x=s.getBytes();
      x=cpb.addFlag(x);
      
     // System.out.println("size of sending info "+x.length);
       outToServer.write(x);
   //    System.out.println("send"+" "+stId+" "+fileName+" "+bytes+" "+receiver+'\n');
       System.out.println("before coming confirmation message");
    }
}
