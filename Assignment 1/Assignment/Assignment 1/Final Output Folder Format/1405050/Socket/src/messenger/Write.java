/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package messenger;

import java.io.BufferedInputStream;
import java.io.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import static messenger.ServerIO.decodeBinary;

/**
 *
 * @author uesr
 */
public class Write implements Runnable{
    public ConnectionUtillities connection;
    public String clientId; 
    public FileOption fileOption ;
    public ArrayList < Chunk > ChunkList;
    public Frame timeout  ;
    String fileId;
    public Write(ConnectionUtillities connection,String id){
        this.connection=connection;
        this.clientId = id ;
    }

    @Override
    public void run() {
        
           
        
        while(true){
            
             //   connection.sc.setSoTimeout(0);


            Object obj= null;
            try {
                obj = connection.read();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            System.out.println("Server: <"+obj.toString()+" >");
            
            Frame tt = new Frame();

            tt.deFraming(obj.toString());
            System.out.println("CHECK");
            String s ;
            
            s = tt.binStringToString(tt.bitDestufffing(tt.getPayLoad()));
           
            System.out.println(s);
           
            if(s.contains("RECEIVE ITEM")){
                
                String split[]=s.split("#");
                String fileName = split[1];
                
                String name = fileName.substring(fileName.lastIndexOf("\\"));


                byte[] file = new byte[0];
                try {
                    file = (byte[])connection.read();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }


                //make folder
                File folder = new File(System.getProperty("user.home")+"\\Documents\\ReceivingItem");
                folder.mkdir();
                //

                String fileDir = System.getProperty("user.home")+"\\Documents\\ReceivingItem\\"+name;
                
                File recFile = new File (fileDir);
                try {
                    FileOutputStream fileOutStream = new FileOutputStream (recFile);
                    fileOutStream.write(file);
                    fileOutStream.close();
                    
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(Write.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(Write.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                
            }
            
            else if(s.contains("The client can start sending the file now")){
                
                String str[]=s.split("#",2);// split to obtain fileID from server
                fileId=str[1]; //sent from server side
                
                String fileInfo[] = fileId.split("#",3) ;
                
                try{
                    
                    
                    System.out.println("Sending... ");
                    
                    File file = new File(fileInfo[1]); // contains file Name
                    
                    FileInputStream fis = new FileInputStream(file);
                    BufferedInputStream bis = new BufferedInputStream(fis);
                    
                    
                    byte[] contents;
                    
                    long fileLength=file.length();
                    long curr = 0;
                    System.out.println(fileInfo[2].length());
                    int size = Integer.parseInt(fileInfo[2]); // type cast chunksize
                    // ChunkList = new ArrayList <>() ; // initialize
                    
                    int count=(int) Math.ceil(fileLength/(float)size); //chunk no
                    System.out.println("Number of chunks: "+count);
                    //
                    Frame f = new Frame() ;
                    int seqNo = 0 ;
                    //
                    
                    
                    while(curr!=fileLength){
                        
                        Frame filemsg = new Frame ();
                        sendFrame(filemsg,0,0,0,"FILE SENDING#"+fileId);
                        
                        connection.write(filemsg.getFrame());
                        
                        if(fileLength - curr >= size)
                            curr += size;
                        else{
                            size = (int)(fileLength - curr);
                            curr = fileLength;
                        }
                        
                        contents = new byte[size];
                        bis.read(contents, 0, size);
                        
                        // assignment 2
                        seqNo++;
                        f.setFrameKind(1); // 1 for data
                        f.setSeqNo(seqNo);   //chunkNum
                        f.setAckNo(0);
                        f.setPayLoad(contents) ;
                        f.setCheckSum();
                        
                        String frame= f.getFrame();
                        System.out.println("length : " +frame.length());
                        

                        if(seqNo==5){
                            connection.write( errorFrame(frame));
                        }else connection.write(frame);

                        
                        
                        count--; //remaining chunk
                        try {
                            connection.sc.setSoTimeout(3000);
                            String response = (String) connection.read();
                            connection.sc.setSoTimeout(0);
                            System.out.println("Response : " + response);
                        }catch (Exception e){
                            e.printStackTrace();
                            System.out.println("EXCEPTION in client sending to server");
                            try {

                                String response = (String) connection.read();
                                System.out.println("Response in catch : " + response);

                                f.setAckNo(1);
                                connection.write(f.getFrame());

                            } catch (ClassNotFoundException e1) {
                                e1.printStackTrace();
                            }
                        }





                        if(count==0){
                            Frame lastCHUNK = new Frame();
                            System.out.println("Last chunk");
                            sendFrame(lastCHUNK,0,0,0,"LAST CHUNK#"+fileId);
                            connection.write(lastCHUNK.getFrame());
                        }

                        System.out.println("Sending file ... "+(curr*100)/fileLength+"%");

                    }
                    
                    
                    System.out.println("File sent successfully!");
                }
                catch (IOException ex) {                          
                    System.out.println("could not send file ");
                }   
            }
        
        
            
        } 
    }
    
    public void sendFrame(Frame f,int kind,int seq,int ack,String msg){
     
            f.setFrameKind(kind);
            f.setSeqNo(seq);
            f.setAckNo(ack);
            f.setPayLoad(msg.getBytes()) ; 
            f.setCheckSum();
     
     
    }

    public int random(int min,int max){
        int randomNum = ThreadLocalRandom.current().nextInt(min, max + 1);

        return randomNum;
    }

    public void print (String s){
        int i , len ;
        len = s.length();
        for( i = 0 ; i<len ; i++){
            if(i%8==0){ System.out.print(" ");System.out.print(s.charAt(i));}
            else System.out.print(s.charAt(i));
        }
        System.out.println();
    }

    public String errorFrame(String s){

        int ran= random(33,s.length()-17);
        System.out.print("GIVEN FRAME: ");print(s);

        char[] error = s.toCharArray();
        if(error[ran]=='0')error[ran]='1';
        else error[ran]='0';
        s = String.valueOf(error);
        System.out.print("ERROR FRAME: ");print(s);
        return  s ;
    }


}
