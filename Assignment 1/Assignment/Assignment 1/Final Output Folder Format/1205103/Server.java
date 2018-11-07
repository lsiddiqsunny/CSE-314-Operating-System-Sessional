/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package datalinklayer;
import java.util.*;
import java.io.*; 
import java.net.*;
//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.*;
/**
 *
 * @author rafs
 */
public class Server {

    HashMap<String,Boolean> loggedIn=new HashMap<String,Boolean>();
    HashMap<String,PrintWriter> logs=new HashMap<String,PrintWriter>() ;
    final int bufferSize=1000;
    
    
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        new Server().go();
        
    }    
    
    public void go(){
        
        try{
            ServerSocket server=new ServerSocket(12345);
            while(true){
                Socket connection=server.accept();
                InputStreamReader streamReader=new InputStreamReader(connection.getInputStream());
                BufferedReader reader=new BufferedReader(streamReader);
                PrintWriter writer=new PrintWriter(connection.getOutputStream());
                //System.out.println("New Connectionb Established from server");
                
                //if(reader.readLine()!=null) System.out.println("jgvv");
                String studentID=reader.readLine();
                //System.out.println("ffjcfjf");
                if(loggedIn.containsKey(studentID) && loggedIn.get(studentID)){//invalid login
                    writer.println("NO");
                    writer.flush();
                    connection.close();
                }
                else{//accept login
                    System.out.println(studentID+" has just logged in");
                    
                    loggedIn.put(studentID,true);
                    logs.put(studentID, writer);
                    writer.println("YES");
                    writer.flush();
                    
                    Thread clientHandler=new Thread(new HandleClient(connection,studentID));
                    clientHandler.start();
                }
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        
    }
    
    
    public class HandleClient implements Runnable{
        BufferedReader reader;
        PrintWriter writer;
        Socket connection;
        String studentID;
        private HandleClient(Socket sock,String id){
            try{
                studentID=id;
                connection=sock;
                InputStreamReader streamReader=new InputStreamReader(connection.getInputStream());
                reader=new BufferedReader(streamReader);
                writer=new PrintWriter(connection.getOutputStream());
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }
        public void run() {
            String message;
            try{
                while((message=reader.readLine())!=null){
                    System.out.println(studentID +":"+message);
                    if(message.equals("log out")){
                        loggedIn.put(studentID,false);
                        System.out.println(studentID +" has just logged out");
                        connection.close();
                        break;
                    }
                    else{
                        String[] words=message.split(" ");
                        if(words[0].equals("send")){
                            
                            
                            int chunkSize=(int)Math.ceil(Math.random()*10) ;
                            writer.println("send "+words[2]+" "+chunkSize);
                            writer.flush();
                            
                            
                            int length=Integer.parseInt(reader.readLine());
                            Thread receiver=new Thread(new ReceiveFile(words[2],chunkSize,connection,length));
                            receiver.start();
                            
                        }
                        
                        else{
                            System.out.println("other");
                        }
                    }
                    

                }
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }
        
        
    }
 
    
    private class ReceiveFile implements Runnable{

        String fileName;
        int chunkSize;
        Socket connection;
        int fileLength;

        private ReceiveFile(String name, int size,Socket sock,int length) {
            String[] words=name.split("/");
            fileName=words[words.length-1]; 
            System.out.println("file name: "+fileName);
            chunkSize=size;
            fileLength=length;
            connection=sock;
        }
    
        public void run() {
            try{
                
                byte[] bytes=new byte[65536];
                File file=new File(fileName);
                FileOutputStream out=new FileOutputStream(file);
                InputStream in=connection.getInputStream();
                DataInputStream din=new DataInputStream(in);
                //System.out.println("UTMFn");
                InputStreamReader streamReader=new InputStreamReader(connection.getInputStream());
                BufferedReader reader=new BufferedReader(streamReader);
                //int read=din.read(bytes);
                System.out.println("file size is :"+fileLength);
                int received=0;
                while(received<fileLength){
                    
                    //int remaining=chunkSize;
                    //if(remaining>bytes.length) remaining=bytes.length;
                    int read=din.read(bytes);
                    //int read=din.read(bytes, 0,remaining);
                    System.out.println("vfvbfdv");
                    if(read==-1){
                        System.out.println("file read error in server");
                        break;
                    }
                    out.write(bytes,0,read);
                    received+=read;
                    System.out.println("file read in server");
                    
                }
                if(received>=fileLength){
                    System.out.println("successful transfer");
                }
                
            }catch(Exception ex){
                ex.printStackTrace();
            }   
            
        }
        
        
    }
}
