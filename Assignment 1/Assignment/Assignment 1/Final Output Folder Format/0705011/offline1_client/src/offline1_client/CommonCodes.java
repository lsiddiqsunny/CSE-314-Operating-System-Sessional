package offline1_client;

import java.io.*; 
import java.lang.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static offline1_client.Offline1_client.FILE_TO_SEND;

class CommonCodes {
   
    public static byte[] bytes  ;
//    public static void main(String[] args){
//     
//        String finals = new String();
//        String filename = "test.txt";
//        File file = new File("C:\\Users\\rothy\\Desktop\\DLL\\" + filename);
//        
//       
//        try{
//            FileInputStream fis = new FileInputStream(file);
//            BufferedInputStream bis = new BufferedInputStream(fis);
//            
//             
//            byte[] contents;
//            
//            long fileLength = file.length();
//           // System.out.println(fileLength);
//            long current = 0;
//            while(current!=fileLength){ 
//                    int size = 400;
//                    if(fileLength - current >= size)
//                            current += size;    
//                    else{ 
//                            size = (int)(fileLength - current); 
//                            current = fileLength;
//                    } 
//                    contents = new byte[size]; 
//                    bis.read(contents, 0, size); 
////                    for(int i=0;i<contents.length;i++){
////                        System.out.println(contents[i]);
////                    }
//                     
//                    //System.out.println(contents);
//                    
//                    //String stuffed = CommonCodes.bitStuff(contents);
//                    
//                     //for(int i=0;i<contents.length;i++){
//                       // System.out.println(stuffed);
//                    //}
//                  ///  String dstuffed =   CommonCodes.bitDeStuff(stuffed);
//                   // for(int i=0;i<dstuffed.length;i++){
//                        //System.out.println(dstuffed[i]);
//                    //}
//                     //System.out.println(dstuffed);
//                     finals += "";
//            }   
//
//
//        }
//        catch(Exception e){
//             System.out.println(e);
//       }
//     
//                    
//                    try {
//                         byte [] newstuff=new byte[finals.length()/8];
//                         for(int i=0;i<finals.length()/8;i++){
//                            int first=i*8;
//                            int last=(i+1)*8;
//                            String substr=finals.substring(first, last);
//                            newstuff[i]= Byte.parseByte(substr,2);
//                         }
//                         
//                        FileOutputStream stream = new FileOutputStream("C:\\Users\\rothy\\Desktop\\DLL\\test2.txt");
//                        stream.write(newstuff);
//                    } 
//                    catch(Exception e){
//                        
//                    }
//                    finally {
//                        //stream.close();
//                    }
//    }
  
   
    public static String bitDeStuff(String s) {
        

        String destuffing_string=s;
        String out=new String();
         
        int counter=0;
        int count;
        for(int i=0;i<destuffing_string.length();i++)
                {
                   
                    if(destuffing_string.charAt(i) == '1')
                        {
                            
                            counter++;
                            out = out + destuffing_string.charAt(i);
                           
                        }
                    else
                        {
                             out = out + destuffing_string.charAt(i);
                             counter = 0;
                        }
                   if(counter == 5)
                        {
                              out=out; 
                             i++;
                              counter = 0;
                        }
               }
          
          //  System.out.println("Message Recevied...Successfully!!!");
           System.out.println("The Destuffed Message is: "+out+"len"+out.length());
           String destuffing=new String();
           count=0;
           for(int i=0;i<out.length();i++){
               
               char m=out.charAt(i);
               if (m=='1'){break;}
               count++;
           }
           //System.out.println(count);
           destuffing=out.substring(count);
           //System.out.println(count+"  "+destuffing+" "+destuffing.length());
            
            int lentu1=destuffing.length();
             
             int vagses1=lentu1-(lentu1/8)*8;
             int len1;
             if(vagses1>0){len1=(lentu1/8)+1;}
             else {len1=lentu1/8;}
             //System.out.println(lentu1+"  vagses?   "+vagses1+"  "+len1);
             byte [] newstuff1=new byte[len1];
            if(vagses1 != 0){
                for(int i=1;i<=8-vagses1;i++){
                 
                destuffing="0"+destuffing; 
             }
            }
       
        return destuffing;
    }
    //-----------------------------------------------------------------	

        public static String bitStuff(byte[] p){
	
          String destuff=new String(p);
          
          char a;
          int n;
          String strstuffing=new String();
         
          for(int y=0;y<destuff.length();y++)
        {
              a=destuff.charAt(y);
              //System.out.println(a);
              n= (int)a;
              String ni=Integer.toBinaryString(n);
              ni=String.format("%8s",ni).replace(" ","0");
              strstuffing=strstuffing+ni;
             
        }
              System.out.println("printing stuffing  "+strstuffing);
            
             String res=new String();
             
             int count=0;
            for(int i=0;i<strstuffing.length();i++){
                
                if(strstuffing.charAt(i) == '1')
                        {
                            count++;
                            res = res + strstuffing.charAt(i);
                        }
                 else
                        {
                            res = res + strstuffing.charAt(i);
                             count = 0;
                        }
                   if(count == 5)
                        {
                            res = res + '0';
                            count = 0;
                        }
            }
             strstuffing=res;
             System.out.println("without o pading "+strstuffing);
             int lentu=strstuffing.length();
             
             int vagses=lentu-(lentu/8)*8;
             int len;
             if(vagses>0){len=(lentu/8)+1;}
             else {len=lentu/8;}
             
             byte [] newstuff=new byte[len+2];
             if(vagses != 0){
                 for(int i=1;i<=8-vagses;i++){
                 
                    strstuffing="0"+strstuffing; 
                }
           
             }
             
             System.out.println("PRINTING stuffed code with 0 padding "+strstuffing+" ");
//             for(int i=0;i<len;i++){
//                 int first=i*8;
//                 int last=(i+1)*8;
//                String substr=strstuffing.substring(first, last);
//               // newstuff[i]= (byte)Integer.parseInt(substr,2) ;
//               // newstuff[i] = byte.(substr,2);
//              
//             }
//           for(int i=0;i<len;i++){
//              //System.out.println(newstuff[i]);
//           }
                
	   return strstuffing;
      
	}
}

class Frame {

    byte kind;
    byte seqNo;
    byte ackNo;
    byte payload[];
    byte checksum;
    
    Frame(byte[] arr) {

        kind = arr[0];
        seqNo = arr[1];
        ackNo = arr[2];
        payload = new byte[arr.length - 4];
        System.arraycopy(arr, 3, payload, 0, payload.length);
        checksum = arr[arr.length - 1];
        
    }
    Frame(int sNo, int aNo, byte[] a, int kind) {//ack soho frame pathasche
        this.kind = (byte)kind;
        seqNo = (byte) sNo;
        ackNo = (byte) aNo;

        payload = new byte[a.length];

        try {
            System.arraycopy(a, 0, payload, 0, a.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        checksum = calcChecksum();
    }
   
    int getSeqNo() {
        return seqNo;
    }

    int getAckNo() {
        return ackNo;
    }

    byte[] getPayload() {
        return payload;
    }

    int getChecksum() {
        return checksum;
    }
    byte[] getBytes() {
        byte[] frame = new byte[payload.length + 4];
        frame[0] = kind;
        frame[1] = seqNo;
        frame[2] = ackNo;
        try {
            System.arraycopy(payload, 0, frame, 3, payload.length);
            frame[payload.length + 3] = checksum;
            return frame;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    //=======================================================

    String getString() {
        return new String("Kind " + kind + " Seq No=" + seqNo + " | Ack No=" + ackNo + " | Payload=" + new String(payload) + " | Checksum=" + checksum);
    }
    byte calcChecksum() {
        byte cSum = 0;
        cSum = (byte) (kind^seqNo ^ ackNo);
        for (int i = 0; i < payload.length; i++) {
            cSum = (byte) (cSum ^ payload[i]);
        }     
        return cSum;
    }
 
    boolean hasCheckSumError() {
        if ((checksum ^ calcChecksum()) !=0) {
            return true;
        } else {
            return false;
        }
    }
}

class Packet {

    byte data[];
    byte[] header;
    int fid;
    String fileName;
    int reciverid;
    int senderid;
    int chunkSize;
    byte payload[];

    Packet() {
    }

    Packet(byte[] payloaddata, File file, int fileid, int chunksize,int recv, int sender ) {
       
        
            fileName=file.getName();
            fid=fileid;
            reciverid = recv;
            senderid = sender;
            chunkSize=chunksize;  
            payload = payloaddata;
            header = new byte[4];
            header[0] = (byte) fileid;
            header[1] = (byte) reciverid;
            header[2] = (byte) senderid;
            header[3] = (byte) chunksize;
            data = new byte[chunksize + 4];
            System.arraycopy(payload, 0, data, 4, chunksize);
            System.arraycopy(header, 0, data, 0, header.length);
      
     
    }
    byte[] getPayload(byte[] packetdata){
       
        data = new byte[packetdata.length - 4];
        System.arraycopy(packetdata, 4, data, 0, data.length);
        return data;
        
    }
    
    byte[] getPayload() {
        return payload;
    }
    byte[] getBytes() {
        return data;
    }
}

class MyTimer extends Thread {

    int frameId;
    boolean running;
    int duration; 

    public MyTimer(int id) {
        frameId = id;
        
        running = false;
    }

    public MyTimer() {
        
        running = false;
    }

    public void startTimer(int timeout_duration) {
        running = true;
      
        duration = timeout_duration * 1000;
        start();
       			
    }

    synchronized public void stopTimer() {
        running = false;
        this.interrupt();
       
    }

    public int getFrameId() {
        return frameId;
    }

    synchronized public boolean isRunning() {
        return running;
    }

    public void run() {
        //--------Using delay------------------
        try {
            Thread.sleep(duration);
          
            System.out.println("Time's up for frame seq no: " + frameId + "\n");
            //------------------------------------------
           
        } catch (InterruptedException e) {
            System.out.println("Timer for frame seq no: " + frameId + " stopped\n");
            
        }
    }
}
class ByteArray {

    byte[] bArray;
    //-----------------------------------------------------------------	

    ByteArray(int size) {
        bArray = new byte[size];
    }
    
    void setAt(int index, byte[] b) {
        System.arraycopy(b, 0, bArray, index, b.length);
    }
    //-----------------------------------------------------------------	

    byte getByteVal(int index) {
        return bArray[index];
    }

    void setByteVal(int index, byte b) {
        bArray[index] = b;
    }
    //-----------------------------------------------------------------	

    byte[] getAt(int index, int length) {
        byte[] temp = new byte[length];
        System.arraycopy(bArray, index, temp, 0, length);
        return temp;
    }
    //-----------------------------------------------------------------	

    byte[] getBytes() {
        return bArray;
    }

    int getSize() {
        return bArray.length;
    }
}