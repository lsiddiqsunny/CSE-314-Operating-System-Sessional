/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ASUS
 */
public class loginThread implements Runnable {

    String stId="";
    String receiver="";
    String sender="";
    String fileId="";
    String recFile="";
    String sendFile="";
    String fileSize="";
    String recFileId="-1";
    boolean haveFile=false;
    boolean enList=false;
     int size;
    DataOutputStream outToClient;
    DataInputStream byteFromClient;
    FileOutputStream fos;
    FileInputStream fis;
    BufferedReader inFromClient;
    Thread t;
    String clInput="";
    StringTokenizer str;
    boolean logInFlag=false;
    File serverFolder=new File("server");
    String serFilePath="C:\\Users\\ASUS\\Desktop\\Study\\Networking_Sessional\\Assignment1\\Server\\server";
    long highestChunk=10000000; // limit 
    long curChunk=0;
    long minChunk=100;
    boolean complete=false;
    byte []recByte=new byte[21];
    serverProcessByte spb=new serverProcessByte();
    
    
    
    loginThread(String id,DataOutputStream dos,DataInputStream dis,BufferedReader ifc)
    {
        stId=id;
        outToClient=dos;
        inFromClient=ifc;
        byteFromClient=dis;
        logInFlag=true;
        System.out.println("loginThread created");
        t=new Thread(this);
        t.start();
    }
    @Override
    public void run() {
        String temp="";
        while(true)
        {
            try {
                Thread.sleep(1000);
                System.out.println("waiting for client input");
                byteFromClient.read(recByte);
                clInput=processInfoByte(recByte); //extracting flag and gain info in string
                System.out.println("login thread "+clInput);
                str=new StringTokenizer(clInput);
                while(str.hasMoreTokens())
                {
                    
                    temp=str.nextToken();
                    if(temp.equals("logout"))
                    {
                        logout();
                        break;
                    }
                    ////for sending file
                     else if(temp.equals("send"))
                     {
                         System.out.println("Want to send a file "+temp);
                         stId=str.nextToken();
                         sender=stId;
                         recFile=str.nextToken();
                         fileSize=str.nextToken();
                         receiver=str.nextToken();
                         int size=Integer.parseInt(fileSize.trim());
                         System.out.println("sending info "+stId+" "+receiver+" "+recFile+" "+size);
                         checkChunk(stId,receiver,size);
                     }
                    
                }
                
            } catch (InterruptedException | IOException ex) {
              //  Logger.getLogger(loginThread.class.getName()).log(Level.SEVERE, null, ex);
              System.out.println("from login thread exception");
                try {
                        fos.close();
                        complete=true;
                    deleteFile();
                    logInFlag=false;
                    break;
                } catch (IOException ex1) {
                    Logger.getLogger(loginThread.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
            if(!logInFlag)
            {
                break;
            }
        }
        
         //To change body of generated methods, choose Tools | Templates.
    }
    
    public String processInfoByte(byte[] nb)
    {
      
        byte []x=spb.extractFlag(nb);
        String s=new String(x);
      //  System.out.println("processing in server "+s);
        return s;
    }
    
    
    synchronized public void logout()
    {
        
        if(t.isAlive())
        {
            t=null;
        }
        System.out.println("logout called");
        logInFlag=false;
    }
    synchronized public void checkChunk(String sid,String rec,int size) throws IOException
    {
        File[] files=serverFolder.listFiles();
        System.out.println("File count in server : "+serverFolder.exists()+" "+files.length);
        int count=files.length;
        int clientChunk=0;
        
        for(int i=0;i<count;i++)
        {
            if(files[i].isFile())
            {
                curChunk+=files[i].length();
            }
        }
        System.out.println("Total data in server "+curChunk);
        
        // check limit value after adding curChunk with file size
        
        if(curChunk+size > highestChunk)
        {
            
            outToClient.writeBytes("Sorry!! overflow occured!!"+'\n'); 
        }
        
        else
        {
            minChunk = size / 10;
            if (minChunk == 0)
            {
                minChunk=size;
                clientChunk=(int) minChunk;
            }
            else 
            {
            clientChunk= (int) minChunk;
            }
            //fileID = sid + receiverId +size +clientChunk + fName;
            
            fileId="send_"+sid+"_"+rec+"_"+size+"_"+clientChunk+"_"+recFile; //coming file from client to server
            
            byte []x=(fileId+" "+'\n').getBytes();
            x=spb.addFlag(x);
            System.out.println("length of sending info from server "+x.length);
            outToClient.write(x);
            System.out.println(fileId);
            
            receiveFile(fileId,sid,rec,clientChunk,size);
        }
      
    }

    
    
    public void processWhileSending()
    {
        
    }
    
    
    public void receiveFile(String fileId,String sid,String rec, int clientChunk, int size) throws FileNotFoundException, IOException
    {
         //To change body of generated methods, choose Tools | Templates.
        byte []x=new byte[50];
        int total_seq = 0;
        if(size%clientChunk == 0) total_seq=(int) (size/clientChunk);
        else total_seq = (int) (size/clientChunk)+1;
         String temp="";
         int recByte=0;
       //System.out.println(serFilePath+"\\"+fName);
         String fileName=serFilePath+"\\"+fileId;
         File recFile=new File(fileName);
       //  System.out.println(recFile.exists());
         
         if(!recFile.exists())
         {
             recFile.createNewFile();
        
         }
         fos=new FileOutputStream(recFile);
         byte[] fb;
         byte[]efb;
         byte[]store_efb;
         byte[]estb;
         byte[]esb;
         byte[]ecb;
         byte []rb=new byte[clientChunk];
         String checkEnd="";
         int seq=0;
         int prevSeq=0;
         int checkSum;
         int count1;
         int cc;
         byte []a;
         byte []b;
         int seqErr=0;
         boolean checkError=false;
         int receive_chunk =0; 
        // System.out.println("File is going to be copied");
        System.out.println("Total sequence to be received "+total_seq);
         while(true)
         {
             System.out.println();
             receive_chunk=clientChunk+10+((clientChunk/5)/8)+1;
             fb=new byte[receive_chunk];
             efb=new byte[receive_chunk-2];
             estb=new byte[receive_chunk-2-(((clientChunk/5)/8)+1)];
             esb = new byte[receive_chunk-2-(((clientChunk/5)/8)+1)-4];
             ecb = new byte[receive_chunk-2-(((clientChunk/5)/8)+1)-4-4];
             byteFromClient.read(fb);// destuffing
             
             //protocol
             
             efb=spb.extractFlag(fb);
             store_efb=efb;
             estb=spb.destuffing(efb);
             seq=spb.getSeq(estb);
             System.out.println("sequence received "+seq);
             esb=spb.extractSeq(estb);
             checkSum=spb.getCheckSum(esb);
             ecb=spb.extractCheck(esb);
             
             checkEnd=new String(ecb);
             
             checkEnd=checkEnd.trim();
             System.out.println("from client "+checkEnd);
             

             System.out.println("CheckSum and CountOne "+checkSum+" "+spb.countOne(ecb));
             //System.out.println("recByte and size and rb size "+recByte+" "+size+" "+rb.length);
            //protocol
            //acknowledgement
             System.out.println("CheckSum and countOne "+checkSum +" "+spb.countOne(ecb));
                if( hasCheckSumError(checkSum,spb.countOne(ecb)))
                {
                    if( seq == (prevSeq+1) && seq <=total_seq)
                    {
                    System.out.println("1.Received Frame");
                    spb.printByteArray(fb);
                    System.out.println("2.Extracting flag");
                    spb.printByteArray(store_efb);
                    System.out.println("3.Destuffed Frame");
                    spb.printByteArray(estb);
                    System.out.println("4.Extracting sequence");
                    spb.printByteArray(esb);
                    System.out.println("5.Extracting checkSum and main data");
                    spb.printByteArray(ecb);
                    checkError=false;
                    prevSeq++;
                    fos.write(ecb);
                    recByte=recByte+rb.length;
                    byte[]p=new byte[4];
                    ByteBuffer bf=ByteBuffer.allocate(4);//bits of checksum == 4*8 = 32
                    bf.putInt(prevSeq);
                    p = bf.array();
                    b =new byte[6];
                    b=spb.addFlag(p);
                   // System.out.println("chunk received problem "+new String(b));
                    System.out.println("Sequence printed "+prevSeq);
                    outToClient.write(b);
                    if(seq == (total_seq-1))
                    {
                        clientChunk=size-(seq*clientChunk);
                    }
                    if( seq == total_seq)
                    {
                        fos.close();
                        System.out.println("Successfully received in server");
                        System.out.println();
                        break;
                    }
                    }//
                    else if(seq == (prevSeq+2) && !checkError)
                    {
                        System.out.println("Frame Lost "+(seq-1));
                    
                    }
                    else
                    {
                        System.out.println("Discarded sequence is "+seq);
                   
                    }
                }
                else
                {
                    checkError=true;
                    System.out.println("CheckSum error detected at sequence "+seq);
                    System.out.println("Previous sequence "+prevSeq);
             
                }
                
         }
        
         recFile(rec,fileId);
    }

    
    public boolean hasCheckSumError(int x,int y)
    {
        return x==y;
    }
    synchronized public void sendFile(String recFileId) throws FileNotFoundException, IOException
    {
      int sendChunk=10; //// from server to client 
      byte [] nb;
      String checkByte="";
      int i=1;
      int sendByte=0;
      StringTokenizer st=new StringTokenizer(recFileId);
      st.nextToken("_");
      sender=st.nextToken("_");//sender
      receiver= st.nextToken("_");//receiver
      size=Integer.parseInt(st.nextToken("_").trim());
      sendChunk=Integer.parseInt(st.nextToken("_").trim());
      sendFile=st.nextToken("_").trim();//fileName
      //serFilePath=serFilePath+"\\"+sendFile;
      File file=new File(serFilePath+"\\"+recFileId);// find file
     recFileId=recFileId.trim();
      System.out.println("received fileId "+recFileId+" "+file.exists());
      if(file.exists())
      {
          System.out.println("file exists");
      }
     fis=new FileInputStream(serFilePath+"\\"+recFileId);
     
        do{
            System.out.println("File is sending to receiver");
            nb=new byte[sendChunk];
            i=fis.read(nb);
            if(i != -1){
            if(i == 0)
            {
                outToClient.writeBytes("EndFile");
                System.out.println("End file from server");
                fis.close();
                break;
            }
            outToClient.write(nb);
            sendByte+=nb.length;
            if(size-sendByte < sendChunk)
            {
                sendChunk=(int) (size-sendByte);
            }
            }
          }while( i != -1);
       System.out.println("File Successfully send from "+sender+" to "+receiver);
    }
    public void recFile(String rec,String fileId) {
        //To change body of generated methods, choose Tools | Templates.
        receiver=rec;
        haveFile=true;
        complete=true;
    }
    
    public void deleteFile() throws IOException
    {
      
        File file=new File("server"+"\\"+fileId);
        haveFile=false;
        if(file.exists()){
            System.out.println("File deleted");
        file.delete();} 
    } 
}
