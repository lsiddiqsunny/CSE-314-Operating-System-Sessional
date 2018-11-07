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
public class Client {
    Socket connection;
    BufferedReader reader;
    PrintWriter writer;
    Scanner scanner;
    static int i=0;
    String fileName="C://abc.txt";
    
    
    public static void main(String[] args) {
        // TODO code application logic here
        Client user=new Client();
        user.go();
    }        
    
    public void go(){
        i++;
        
        if(setUpNetworking()){
            
            
            Thread inputHandler=new Thread(new InputHandle());
            Thread incomingDataHandler=new Thread(new IncomingDataHandle());
            inputHandler.start();
            incomingDataHandler.start();
            while(true){
                
            }
        }
        
        
        
        
    }
    
    
    
    public boolean setUpNetworking(){
        try{
            connection=new Socket("127.0.0.1",12345);
            InputStreamReader streamReader=new InputStreamReader(connection.getInputStream());
            reader=new BufferedReader(streamReader);
            writer=new PrintWriter(connection.getOutputStream());
            //System.out.println("New Connection Established from client");
            
            System.out.println("Enter Student ID:");
            scanner=new Scanner(System.in);
            String studentID=scanner.nextLine();
            
            writer.println(studentID);
            writer.flush();
            //System.out.println(studentID);
            String response=reader.readLine();
            
            if(response.equals("YES")){
                
                System.out.println("successful login");
                return true;
            }
            else{
                System.out.println(response);
                System.out.println("login failed");
                connection.close();
                return false;
            }
            
            
        }catch(Exception ex){
            ex.printStackTrace();
            return false;
        } 
        
        
    }
    
    private class InputHandle implements Runnable{
        
        public void run(){
            try{
                while(true){
                    System.out.print(">");
                    String response=scanner.nextLine();
                  //  System.out.println(response); 
                    if(response.equals("log out")){
                        System.out.println("logging out!"); 
                        writer.println(response);
                        writer.flush();
                        //connection.close();
                        System.out.print("logged out!"); 
                        //connection.close();
                        break;

                    }
                    else{
                        String[] words=response.split(" ");
                        if(words[0].equals("send")){
                            String receiver=words[1];
                            //String fileName=words[2];
                            writer.println(response+" "+fileName);
                            writer.flush();
                            //String message=reader.readLine()
                        }
                    }

                }
            }catch(Exception ex){
                ex.printStackTrace();
            }
            
        }
        
        
    }
    private class IncomingDataHandle implements Runnable{
        
        public void run() {
            String message;
            try{
                while((message=reader.readLine())!=null){
                    System.out.println("message from server:" +message);
                    String[] words=message.split(" ");
                    
                    if(words[0].equals("send")){
                        String fileName=words[1];
                        int chunkSize=Integer.parseInt(words[2]);
                        Thread fileSender=new Thread(new SendFile(fileName,chunkSize));
                        fileSender.start();
                    }
                
                }
            }catch(Exception ex){
                ex.printStackTrace();
            }
            
            
        }
        
        
        
    }
    private class SendFile implements Runnable{

        String fileName;
        int chunkSize;

        private SendFile(String name, int size) {
            fileName=name;
            chunkSize=size;
        }
    
        public void run() {
            try{
                byte[] bytes=new byte[65536];
                File file=new File(fileName);
                FileInputStream in=new FileInputStream(file);
                OutputStream out=connection.getOutputStream();
                DataOutputStream dout=new DataOutputStream(out);
                PrintWriter writer=new PrintWriter(connection.getOutputStream());
                int fileLength=(int)file.length();
                System.out.println("file size:"+fileLength);
                writer.println(fileLength);
                writer.flush();
                int sent=0;
                int i=0;
                while(sent<fileLength){
                    int remaining=chunkSize;
                    if(remaining>bytes.length) remaining=bytes.length;
                    int read=in.read(bytes, 0,remaining);
                    if(read==-1){
                        System.out.println(i+" file read error");
                        break;
                    }
                    dout.write(bytes,0,read);
                    sent+=read;
                    i++;
                }
                if(sent>=fileLength) System.out.println("successfully sent"); 
                
            }catch(Exception ex){
                ex.printStackTrace();
            }   
            
        }
        
        
    
    }
}
