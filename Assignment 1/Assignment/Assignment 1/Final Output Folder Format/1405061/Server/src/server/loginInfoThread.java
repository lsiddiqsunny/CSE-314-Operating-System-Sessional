/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ASUS
 */
public class loginInfoThread implements Runnable{

     ArrayList<String>onlineId = new ArrayList<String>(); // those who are online now
    CopyOnWriteArrayList<String>arrivedFileId=new CopyOnWriteArrayList<String>();
    Hashtable<String,loginThread> onlineThread = new Hashtable<String,loginThread>();
    
    BufferedReader inFromClient;
    DataOutputStream outToClient;
    DataInputStream byteFromClient;
    Thread t;
    serverProcessByte spb= new serverProcessByte();
    loginThread logInClient;
    loginInfoThread()
    {
        t=new Thread(this);
        
        t.start();
    }
    
    @Override
    public void run() {
        while(true)
        {
            try {
                Thread.sleep(1000);
                checkLogout();
                checkHaveFile();
                manageFile();
            } catch (InterruptedException | IOException ex) {
                Logger.getLogger(loginInfoThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void checkHaveFile() throws IOException
    {
       // System.out.println("check have file");
        String temp="";
        Enumeration<String> enu=onlineThread.keys();
      //   System.out.println("OK");
         loginThread temp4;
         while(enu.hasMoreElements())
           {
                temp=enu.nextElement();
                temp4=onlineThread.get(temp);    
                if(temp4.haveFile && temp4.enList == false)
                {
                   arrivedFileId.add(temp4.fileId);
                   temp4.enList=true;
                }
           }
    }
    
    synchronized public void manageFile() throws IOException
    {
        String fileId="";
        String send="";
        String rec="";
        StringTokenizer st;
        loginThread sender;
        loginThread receiver;
        Iterator<String>fid=arrivedFileId.iterator();
        while(fid.hasNext())
        {
            
            fileId=fid.next();
            System.out.println(fileId+" has arrived ");
            st=new StringTokenizer(fileId);
            st.nextToken("_");
            send=st.nextToken("_");//sender
            rec=st.nextToken("_");//receiver
            if(onlineThread.containsKey(rec)){
            System.out.println("receiver : "+rec+" is online now");
            receiver=onlineThread.get(rec);
            sendFile(receiver,fileId);
            }
        }
    }
    
    synchronized public void sendFile(loginThread receiver,String fileId) throws IOException
    {
        if(receiver.logInFlag)//receiver is online
        {
            String s="";
            byte []b=new byte[10];
            
            byte bt;
            char c;
            System.out.println("receiver is online");
            String ns="New file arrived!!"+'\n';
            byte []bs=ns.getBytes();
            byte []fbs=spb.addFlag(bs);
            receiver.outToClient.write(fbs);
            System.out.println("waiting for receiver response");
            receiver.recFileId=fileId;
            receiver.byteFromClient.read(b);
            byte []x=spb.extractFlag(b);
          // s=receiver.byteFromClient.toString();
          // bt= receiver.byteFromClient.readByte();
            s=new String(x).trim();
            System.out.println("receiver response"+s+" and b = "+x.length+s.equals("ES"));
            if(s.equals("ES") || s.equals(("Y"))){
            System.out.println("receiver wants to receive the file");
            byte []p=("receive_"+fileId+'\n').getBytes();
            byte []q=spb.addFlag(p);
            receiver.outToClient.write(q);
            receiver.sendFile(fileId);}
            else
            {
                System.out.println("File is not delivered!!");
            }
           
            completeSending(fileId); //deletes the file from server
            
        }
        else 
        {
            System.out.println("receiver in not online now!!");
            return;
        }
    }
    
    public void completeSending(String fileId) throws IOException
    {
       File file=new File("server"+"\\"+fileId);
       if(file.exists()){
       file.delete();}
       System.out.println("File is deleted from server");
       arrivedFileId.remove(fileId);
    }
    
    public void checkLogout()
    {
         String temp="";
       
         Enumeration<String> enu=onlineThread.keys();
      //   System.out.println("OK");
         loginThread temp4;
         while(enu.hasMoreElements())
           {
                temp=enu.nextElement();
                temp4=onlineThread.get(temp);
                if(!temp4.logInFlag)
                {
                    loginThread lt;
                    lt=onlineThread.remove(temp);
                    serverThread.log_out_id=temp;
                    System.out.println("log out id is "+serverThread.log_out_id);
                    if(lt != null)
                    {
                        System.out.println("Successfully logged out");
                    }
                }
                else {
                    System.out.println("Online :"+temp4.stId);
                }    
           }
    }
    
    public void insertID(String stId,String pass,DataOutputStream dos,BufferedReader br,DataInputStream dis) throws IOException
    {
        outToClient=dos;
        inFromClient=br;
        byteFromClient=dis;
        checkIdPass(stId,pass);
    }
    
    public boolean checkIdPass(String Id,String pass) throws FileNotFoundException, IOException
    {
        if(onlineThread.containsKey(Id))
        {
            already_logged_in();
            return false;
        }
        System.out.println("online thread contains "+onlineThread.contains(Id));
        BufferedReader br=null;
        br = new BufferedReader(new FileReader("namePass.txt"));
        String fileInfo="";
        while( (fileInfo = br.readLine()) != null)
        {
        if(Id.equals(fileInfo) && pass.equals(br.readLine()))
        {
           onlineId.add(Id);
           successfully_logged_in();  //successfully logged in
           System.out.println("Successfully logged in"); 
           br.close();
           logInClient=new loginThread(Id,outToClient,byteFromClient,inFromClient);
           serverThread.log_id = Id;
           System.out.println("login client created");
           onlineThread.put(Id, logInClient);
           System.out.println("login client created "+onlineThread.containsKey(Id));
           
           return true;
        }
        br.readLine();
        }
        wrong_pass_name();
        br.close();
        return false;
    }
    
    
    public void already_logged_in() throws IOException
    {
        System.out.println("Already logged in");
        byte []x=new byte[1];
        x="3".getBytes();
        byte[]y=spb.addFlag(x);
        outToClient.write(y);
    }
    
    
    public void successfully_logged_in() throws IOException
    {
        byte []x=new byte[1];
        x="1".getBytes();
        byte[]y=spb.addFlag(x);
        outToClient.write(y);
    }
    
    
    public void wrong_pass_name() throws IOException
    {
        byte []x=new byte[1];
        x = "2".getBytes();
        spb.printByteArray(x);
        byte[]y=spb.addFlag(x);
       // System.out.println("wrong length "+y.length);
        outToClient.write(y);
    }

    
    
}
