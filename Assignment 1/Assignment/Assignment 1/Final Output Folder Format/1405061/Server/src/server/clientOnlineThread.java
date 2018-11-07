/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import static server.FileSendController.ct;

/**
 *
 * @author ASUS
 */
public class clientOnlineThread implements Runnable{

    
    DataOutputStream outToServer;
    DataInputStream byteFromServer;
    BufferedReader inFromServer;
    BufferedReader inFromUser;
    Thread t;
    byte [] bt;
    String command="";
    StringTokenizer st;
    StringTokenizer sn;
    String sender="";
    String receiver="";
    String fileName="";
    String fileSize="";
    String chunk="";
    int size;
    int receiveChunk=0;
    int sendChunk=0;
    static clientThread ct;
    clientProcessByte cpb=new clientProcessByte();
    
    String recFilePath="receiver";
    static String sendFilePath="";
    String temp="";
    String s="";//opinion
    
    boolean receiveFile=false;
    String recFlag="";
    String newFlag="";
    byte[] recByte=new byte[40];
    boolean checkError=false;
    boolean randLostFrame=false;
    int lostFrame=0;
    boolean logout=false;
    
    
    clientOnlineThread(DataOutputStream dos,DataInputStream dis,BufferedReader bf)
    {
        this.outToServer=dos;
        byteFromServer=dis;
        this.inFromServer=bf;
        inFromUser=new BufferedReader(new InputStreamReader(System.in));
        t=new Thread(this);
        t.start();
    }
    @Override
    public void run() {
       //To change body of generated methods, choose Tools | Templates.
       while(true)
       {
           
           try {
               Thread.sleep(500);
               System.out.println("waiting for server input");
               byteFromServer.read(recByte);
               System.out.println("got server input");
               command=processInfoByte(recByte);
               System.out.println("server says "+command);
               st = new StringTokenizer(command);
               sn = new StringTokenizer(command);
               recFlag = st.nextToken("_");
               newFlag=sn.nextToken(" ");
               if(newFlag.equals("New"))
               {
                   FileSendController.flag="New file arrived. Do you want to receive?";
                    System.out.println("ready to receive a file");
               }
               else if(recFlag.equals("receive"))
               {
                   StringTokenizer fname;
                   st.nextToken("_");
                   sender=st.nextToken("_");
                    receiver=st.nextToken("_");     
                    fileSize=st.nextToken("_");
                    chunk=st.nextToken("_");
                    fileName=st.nextToken("_");
                    fname= new StringTokenizer(fileName);
                    fileName=fname.nextToken("\n");
                    receiveChunk=Integer.parseInt(chunk);
                    size=Integer.parseInt(fileSize);
                    System.out.println(fileName+" is coming");
                    receiveFile(sender,receiver, fileName,receiveChunk,size);
               }
               else 
               {
                   if(command.equals("Sorry!! overflow occured!!"))
                    {
                        FileSendController.flag="Sorry!! overflow occured!!";
                    }
                   if(recFlag.equals("send")){
                   sender=st.nextToken("_");
                   receiver=st.nextToken("_");
                   fileSize=st.nextToken("_");
                   chunk=st.nextToken("_");
                   fileName=st.nextToken("_").trim();
                 //  System.out.println("chunk is "+chunk);
                   sendChunk=Integer.parseInt(chunk.trim());
                 //  
                   size=Integer.parseInt(fileSize.trim());
                  System.out.println("chunk size is "+sendChunk);
                   sendFile(sendFilePath,sendChunk,size);
               }}
           } catch (InterruptedException | IOException ex) {
              // Logger.getLogger(clientOnlineThread.class.getName()).log(Level.SEVERE, null, ex);
              System.out.println("logout from clientOnlineThread");
              return;
              
           
           }
       }
       
    }
    
    public String processInfoByte(byte[] a)
    {
        cpb.printByteArray(a);
        byte []x=cpb.extractFlag(a);
        System.out.println("processed "+new String(x));
        return new String(x);
    }
    
    
    
    
    
    synchronized public void sendFile(String filePath, int chunk,long size) throws FileNotFoundException, IOException, InterruptedException 
    {
        byte [] nb;
        byte[]sb;
       
       
        String checkByte="";
        int i=5;
        int sendByte=0;
        FileInputStream fis=new FileInputStream(filePath);
       
        timeThread checkTime=new timeThread();
        getSequenceFromServer gsfs=new getSequenceFromServer(byteFromServer);
        int cc=0;
        int cs=0;
        int seq=0; //sequence
        int gseq=0;
        boolean endFile= false;
        boolean start=false;
        int count =0;
        int f=0;
     
        int totalSeq;
        if(size%chunk == 0) totalSeq=(int) (size/chunk);
        else totalSeq = (int) (size/chunk)+1;
        int flag_chunk=chunk;
        int oldSeq=0;
        do{
         //   System.out.println("##checkFlag "+checkTime.flag+" "+start+" "+endFile);
            System.out.println();
            System.out.println();
            System.out.println("get sequence from acknowledgement");
            seq=gsfs.getSeq;
            if(seq == totalSeq)
            {
                System.out.println("End file from client");
                endFile=true;
                fis.close();
                break;
            }
            System.out.println("Sequence is "+seq);
            fis.getChannel().position(seq*chunk);
            sendByte=seq*chunk;
            chunk=flag_chunk;
            checkTime.startTime();
            //System.out.println("read file from "+seq*chunk+" position");
            while(checkTime.flag )
            {
            System.out.println();
            System.out.println();
            cc=chunk+((chunk/5)/8)+10+1;
            
            System.out.println("chunk "+chunk);
            nb=new byte[chunk];
            sb=new byte[cc];
           
            i=fis.read(nb);
            
            seq++;
            
            checkByte=new String(nb);
           // System.out.println("collected txt "+checkByte+" size of nb "+nb.length+" value of i "+i);
           // System.out.println("i is :"+i);
           
            if(i != -1){
               
              // System.out.println("Sequence and received seq "+totalSeq+" "+gsfs.getSeq);
               
               if(gsfs.getSeq == totalSeq)
                {
                System.out.println("End file from client");
                endFile=true;
                fis.close();
                break;
                }
               else if( i== 0) //again go and back 
                {
                 System.out.println("Go and back to the last acknowledgement");
                 seq=gsfs.getSeq;
                 fis.getChannel().position(seq*flag_chunk);
                 if((totalSeq-1) == seq)
                 {
                 chunk=(int) (size-(seq*flag_chunk));
                 sendByte=seq*flag_chunk;
                 }
                 else
                 {
                     chunk = flag_chunk;
                     sendByte=seq*flag_chunk;
                 }
                 
                 continue;
                }
               
            System.out.println("1. Payload to be sent");
            cpb.printByteArray(nb);
            cs=cpb.countOne(nb);
         //   System.out.println("checkSum and countOne "+cs+" "+cpb.countOne(nb));
            System.out.println("CheckSum "+cs);
            
            System.out.println("Sequence "+seq);
            if(checkError && seq==(totalSeq/2))
            {
                checkError=false;
                System.out.println("Error occured at sequence "+seq);
                nb=cpb.changeBit(nb);
                sb=cpb.addCheckSum(cs,nb);
                System.out.println("correct checkSum and Error checkSum "+cs+" "+cpb.countOne(nb));
            }
            else
            {
                sb=cpb.addCheckSum(cs,nb);
            }
            System.out.println("2. CheckSum added to payload");
            cpb.printByteArray(sb);
            sb=cpb.addSeq(seq, sb);
            System.out.println("3. Sequence added to payload");
            cpb.printByteArray(sb);
            sb=cpb.stuffing(sb);
            System.out.println("4. After stuffing");
            cpb.printByteArray(sb);
            sb=cpb.addFlag(sb);
            System.out.println("5. After adding flag and final frame");
            cpb.printByteArray(sb);
            //stuffing
            
            
         
            
            if(checkTime.flag )
            {
            if(seq != lostFrame)
            {
            outToServer.write(sb);
            sendByte+=nb.length;
            }
            if(randLostFrame && seq == lostFrame)
            {
                sendByte+=nb.length;
                System.out.println("Random lost frame occured");
                System.out.println("Lost frame "+seq);
                randLostFrame=false;
                lostFrame=-1;
            }
            if(size-sendByte < chunk)
            {
                chunk=(int) (size-sendByte);
                System.out.println("chunk is truncated here "+chunk);
            }
           // System.out.println("sendByte"+sendByte);
      
           // System.out.println("sequence get_sequence "+seq+" "+gsfs.getSeq);
            
            
            }
            }
            }//while loop bracket
            if(endFile)
            {
                fis.close();
                checkTime.completeSending();
                checkTime.t=null;
                System.out.println("Break from do while loop");
                
                break;//break from main do-while loop
            }
          //  System.out.println("halted here");
          }while( !endFile);
    }
    
    public void receiveFile(String sid,String rec, String fName, int clientChunk, int size) throws FileNotFoundException, IOException, InterruptedException
    {
        
         String temp="";
         int recByte=0;
         System.out.println(recFilePath+"\\"+fName);
         String fileName=recFilePath+"\\"+fName;
         File recFile=new File(fileName);
         System.out.println(recFile.exists());
         recFile.createNewFile();
         FileOutputStream fos=new FileOutputStream(recFile);
         byte[] fb=new byte[clientChunk];
         String checkEnd="";
         while(true)
         {
             fb=new byte[clientChunk];
             byteFromServer.read(fb);
             System.out.println("receive byte :"+recByte+" file size: "+size);
             if(recByte == size)
             {
                 if(temp.equals("EndFile"))
                 {
                     System.out.println("file end!!! recByte :"+recByte);
                     break;
                 }
                 else
                 {
                     System.out.println("recByte is greater than size. Received: "+checkEnd);
                     fb=new byte[10];
                     byteFromServer.read(fb);
                     checkEnd=new String(fb);
                     System.out.println(checkEnd);
                     temp=temp+checkEnd;
                     temp=temp.trim();
                     System.out.println(temp+"  "+checkEnd);
                    // break;
                     if(temp.equals("EndFile"))
                     {
                         System.out.println("Endfile from client"+"file size "+size+recFile.length());
                         fos.close();
                         break;
                     }
                 }
             }
             else {
             fos.write(fb);
             fos.flush();
             recByte=recByte+fb.length;
             if(size-recByte <clientChunk)
             {
                 clientChunk=size-recByte;
             }
             System.out.println("chunk received");
             }
         }
         recFile();
        
    }

    
    
    
    //protocol stuffing ***
   
    
    public void recFile() {
        //To change body of generated methods, choose Tools | Templates.
        System.out.println("Successfully received a file !!");
        FileSendController.flag="Successfully received a file !!";
    }

    public void startReceiveFile() throws IOException, FileNotFoundException, InterruptedException {
        System.out.println("File Received");
        byte []x=("YES"+"\n").getBytes();
        outToServer.write(x);
    }

    public void startRejectFile() throws IOException {
        System.out.println("File Rejected");
        byte []x=("NO"+"\n").getBytes();
        outToServer.write(x);
    }
    
    
    
}
