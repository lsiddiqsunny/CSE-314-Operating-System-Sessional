/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package messenger;


import java.io.*;

import java.io.OutputStream;
import static java.lang.System.exit;
import static java.lang.System.setOut;
import static java.lang.Thread.sleep;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ServerIO implements Runnable{

    public HashMap<String,Information> clientList;
    public ConnectionUtillities connection;
    public String user;
    public int f_flag=0; //flag for file send confirmation
    public int j=0, _count = 0;
    public int  fileSize=0;
    public int  totalChunkSize=0;
    byte[] tank; //initialize 10KB tank
    public int  chunkSize;
    public String receiver;
    public String fileId;
    public String fileName=null;
    public boolean active = true ;
    public int lostFrame=0 ;
    public int checklostframe ;
    
    public long maxCapacity = 200000000;  
    public long currentCapacity = 0;
    public HashMap<String, ArrayList<Chunk>> chunkStorage;
    
    public ServerIO(String user,ConnectionUtillities connection, HashMap<String,Information> clientList){
        this.user=user;
        this.clientList=clientList;
        this.connection=connection;
        chunkStorage = new HashMap <String, ArrayList<Chunk>>();
    }
    
    @Override
    public void run(){
        while(active){

            Object obj= null;
            try {
                obj = connection.read();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            // System.out.println(obj.toString());
            //
            Frame ff = new Frame();
            ff.deFraming(obj.toString());
            String s ;
            
            s = ff.binStringToString(ff.bitDestufffing(ff.getPayLoad() ) );
           
            System.out.println("After DeStuffing :"+s);
            //
            
        if(s.equals("logout")){
             clientList.remove(user);
             System.out.println(user+" is offline");
             
             active = false ;
        }
          
        else if(f_flag==0 && "SENDING INPUT".contains(s)){

            Object o = null;




            try {
                o = connection.read();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }


            String data=o.toString();
            String msg[]=data.split("-",3);
            
          
           receiver=msg[0];
           fileName=msg[1]; 
           

            int size=Integer.parseInt(msg[2]);

            
            fileSize=size; //store file size
              chunkSize=random((int)(size/210),(int)(size/128)); //randomly generates chunk size

            //  chunkSize = 15 ;
              lostFrame = random(1,(int)(size/chunkSize));
            fileId= receiver +"#"+ fileName +"#"+ chunkSize ;
            
            
            System.out.println("chunkSIZE= "+chunkSize );

            
            tank=new byte[size] ; 
            System.out.println("file size= "+tank.length);
            if(clientList.containsKey(receiver)){//check receiver name
                
                if(size > 200000000){ System.out.println("OverFlows the maximum chunk size"); }
                else{
                    Information info=clientList.get(user);
                    
                    //#########
                    Frame f = new Frame(); 
                    sendFrame(f,"The client can start sending the file now#"+fileId);

                    try {
                        info.connection.write(f.getFrame());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    f_flag=1;
                    
                }
            }
        
            else{
                try {
                    connection.write(receiver+" is not logged into Server ");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
        else if(f_flag==1 && s.contains("FILE SENDING")) {

            //
            Information info=clientList.get(user);
          /* try {

                _count++;

                if(_count == lostFrame){
                    connection.read() ;
                    Thread.sleep(4000);
                    info.connection.write("Send again frame no "+lostFrame);
                    System.out.println("\n\n receiving error frame no : "+lostFrame+"\n\n");
                }
                else info.connection.write("data recieved by the Server");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }catch (ClassNotFoundException e){
                e.printStackTrace();
            }
*/
            //
            System.out.println("received");
            //get file id 
            String[] split= s.split("#",2);
            String id = split[1] ;
            //
            Object recO = null;
            try {
                recO = connection.read();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            Frame rec = new Frame();
            System.out.println("Received frame length : "+recO.toString().length());
            String destuff = rec.bitDestufffing(recO.toString()) ;
            System.out.println("read : "+destuff);
            System.out.println("length after destuff: "+destuff.length());

            rec.deFraming(destuff);

            String sF ;
            sF = rec.getFrameKind()+rec.getSeqNo()+rec.getAckNo()+rec.getPayLoad()+rec.getCheckSum();


            try {

              //  _count++;

             //   if(_count == lostFrame){
                if(hasCheckSumError(rec.getPayLoad(),rec.getCheckSum())==1){
                    System.out.println("ERRROR ASE");

                    Thread.sleep(4000);
                    System.out.println("hi");
                    info.connection.write("Send again");
                    try{
                        recO=connection.read();
                        System.out.println("Received new frame length : "+recO.toString().length());
                        destuff = rec.bitDestufffing(recO.toString()) ;
                        System.out.println("read new : "+destuff);
                        System.out.println("length after new destuff: "+destuff.length());

                        rec.deFraming(destuff);

                        sF = rec.getFrameKind()+rec.getSeqNo()+rec.getAckNo()+rec.getPayLoad()+rec.getCheckSum();
                    }catch(Exception e){
                        e.printStackTrace();
                    }

                }
                else info.connection.write("data recieved by the Server");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("Destuffing:");
            rec.print(sF);
            byte[] data=decodeBinary(rec.getPayLoad());
           // System.out.println("Data size: "+data.length);

           Chunk chunk = new Chunk(chunkSize,data,id);
            
            if(chunkStorage.get(chunk.getFileId())==null){
               // System.out.println("nulllllll");
                chunkStorage.put(chunk.getFileId(), new ArrayList<>());
            }
             chunkStorage.get(chunk.getFileId()).add(chunk);
             int indx=chunkStorage.get(chunk.getFileId()).size();
           //  System.out.println(chunkStorage.get(chunk.getFileId()).get(indx-1).getSize()) ;
             
             totalChunkSize+=chunkStorage.get(chunk.getFileId()).get(indx-1).getSize();
             System.out.println("Total Chunk: "+totalChunkSize);





        }
        
        if(s.contains("LAST CHUNK")){
           
            
            
          //  if(totalChunkSize==fileSize){
                if(true){
            
       /*     getClientInfo(receiver).connection.write("Do you want to receive?\n"
                    + "fileName: "+fileName+"\nFileSize: "+fileSize+"\nFrom: "+user); */
            
            Frame success = new Frame ();
            sendFrame(success,"Done!! All Chunks received by the server");
                    try {
                        getClientInfo(user).connection.write(success.getFrame() ) ;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    // getClientInfo(user).connection.write("Done!! All Chunks received by the server");
           
            System.out.println("Received successfully by the server");
            
            String name = fileName.substring(fileName.lastIndexOf("\\"));
            // make folder
            File folder = new File(System.getProperty("user.home")+"\\Documents\\Server");
            folder.mkdir();
            //
            
            String Desktop = System.getProperty("user.home")+"\\Documents\\Server"+name;
           
            
            String str[] = s.split("#",2); //  get file id
           
            mergeChunks(Desktop,chunkStorage.get(str[1]));
            // reset values 
            f_flag=0; 
            
            
            sendToreceiver(Desktop,fileId);
            
         
        } else{
                
            System.out.println("Chunks size and file size doesn't match.\nDeleting all Chunks...");

        } 
            
            
        } 
        
        
        
        
        }
    }
public int random(int min,int max){    
    int randomNum = ThreadLocalRandom.current().nextInt(min, max + 1);

    return randomNum;
}
public Information getClientInfo(String name){
            
            Information inf=clientList.get(name);
            return inf;
            
}

public File mergeChunks (String fileName, ArrayList<Chunk> chunkArray){
        try {
            File mergedFile = new File (fileName);
            try (FileOutputStream fileOutStream = new FileOutputStream (mergedFile)) {
                for (int i = 0; i < chunkArray.size(); i++) {
                    fileOutStream.write(chunkArray.get(i).getFileBytes());
                }
                
                System.out.println("File merged successfully. Location : " + mergedFile.getAbsolutePath());
            }
            return mergedFile;
        } catch (IOException ex) {
            System.out.println("Error in merging chunks.");
            
            return null;
        }
    }


 public void sendToreceiver(String fileDir,String fileId){
     
                // send file to receiver 
                Path path = Paths.get(fileDir);
                try { 
                    byte[] data = Files.readAllBytes(path);
                    
                    Frame sent =new Frame() ; 
                    sendFrame(sent,"RECEIVE ITEM"+fileId);
                    getClientInfo(receiver).connection.write(sent.getFrame());
                  
                    getClientInfo(receiver).connection.write(data);
                    
                } catch (IOException ex) {
                    Logger.getLogger(ServerIO.class.getName()).log(Level.SEVERE, null, ex);
                }
                
 }   

 public byte[] binStringToByte(String f){
            int i = 0 ;
            byte [] byt = new byte[f.length()];
            for(String str : f.split("(?<=\\G.{8})")){
                byt[i]=(byte)Integer.parseInt(str,2) ;
                i++;
            }
            return byt;
    }
 public void sendFrame(Frame f,String msg){
     
            
            
            f.setFrameKind(0);
            f.setSeqNo(1);
            f.setAckNo(0);
            f.setPayLoad(msg.getBytes()) ; 
            f.setCheckSum();
     
     
 }
 
 static byte[] decodeBinary(String s) {
    if (s.length() % 8 != 0) throw new IllegalArgumentException(
        "Binary data length must be multiple of 8");
    byte[] data = new byte[s.length() / 8];
    for (int i = 0; i < s.length(); i++) {
        char c = s.charAt(i);
        if (c == '1') {
            data[i >> 3] |= 0x80 >> (i & 0x7);
        } else if (c != '0') {
            throw new IllegalArgumentException("Invalid char in binary string");
        }
    }
    return data;
}

 public  int hasCheckSumError(String s,String prev){

     int i,len,parity=0;
     len = s.length() ;

     for(i = 0 ;i<len ; i++){

         parity += s.charAt(i)-'0' ;


     }
     parity %=2 ;


     if(parity==1 && prev.equals("00000001")){
         return 0;
     }else if(parity==0 && prev.equals("00000000")){
         return 0;
     }
     return 1 ;

 }

}
