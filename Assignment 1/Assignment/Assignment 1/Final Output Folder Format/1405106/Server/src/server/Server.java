/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

/**
 *
 * @author Arafat
 */
import java.nio.file.*;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;




 
public class Server extends Thread {
    

    public static final int PORT1 = 3332;
    public static final int PORT2 = 2000;
    public static final int BUFFER_SIZE = 8192;
    public static String s=null;
    FileOutputStream fos = null;
    String fileName=null;
    String line;
    String part1,part2,part3;
    DataInputStream is;
    PrintStream os;
    boolean bool=false;
    ArrayList clientOutputStreams;
    ArrayList<String> users;
    ArrayList <Pair <String,Socket> > l =new ArrayList <Pair <String,Socket> > ();
    @Override
    public void run() {
        try {
            
            ServerSocket serverSocket = new ServerSocket(PORT1);
            ServerSocket serverSock = new ServerSocket(PORT2);
            //int i=1;
            while (true) { 
                Socket s = serverSock.accept();
                try {
           
                    is = new DataInputStream(s.getInputStream());
                    os = new PrintStream(s.getOutputStream());
 

 
                    while (true) {
                      line = is.readUTF();
                      os.println(line); 
                      System.out.println(line);
                      String[] parts = line.split(":");
                      part1=parts[0];
                      part2=parts[1];
                      part3=parts[2];
                      System.out.println(part1);
                      System.out.println(part2);
                      System.out.println(part3);
                      //serverSock.close();
                      is.close();
                      os.close();
                      //s.close();
                    }
                }   
                catch (IOException e) {
                           System.out.println("hui"+e);
                        }
                //System.out.println("Waiting for the receiver");
                
                
                if(part1.equals("receive"))
                {
                    bool=true;
                    Socket ss = serverSocket.accept();
                    l.add(new Pair <String,Socket> (part2, ss));
                    saveFile(ss);
                    
                }
                if(bool==true)
                {
                    Socket ss = serverSocket.accept();
                    l.add(new Pair <String,Socket> (part2, ss));
                    sendFile(ss);
                }
               
                
                
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public class Pair<String,Socket> {
        private String l;
        private Socket r;
        public Pair(String l, Socket r){
            this.l = l;
            this.r = r;
        }
        public String getL(){ return l; }
        public Socket getR(){ return r; }
        public void setL(String l){ this.l = l; }
        public void setR(Socket r){ this.r = r; }
    }
    
    
    public static String intToBinary(int s){
        //System.out.println("In function1: "+s);
      
        String sCheck=Integer.toBinaryString(s);
        //System.out.println("In function2: "+sCheck);
        return sCheck;
    }

    
    private String deleteHeadTail(String s){
        String s1=s.substring(8);
        String s2=s1.substring(0,s1.length()-8);
        
        return s2;
    }
    
    
    public static String getBits(byte b)
    {
        String binary = "";
        for(int i = 7; i >=0; i--){
            binary+=((1<<i) & b)==0?"0":"1";
        }
        return binary;
    }
    
    
//    public static String intToBinary(int s){
//        System.out.println("In function1: "+s);
//      
//        String sCheck=Integer.toBinaryString(s);
//        System.out.println("In function2: "+sCheck);
//        return sCheck;
//    }
//    
    
    public static String bitDstaffing(String res)
    {
        int counter=0;
        int co=0;
        String out = new String();
        for(int i=0;i<res.length();i++)
        {

            if(res.charAt(i) == '1')
            {

                counter++;
                out = out + res.charAt(i);

            }
            else
            {
                 out = out + res.charAt(i);
                 counter = 0;
            }
           if(counter == 5)
            {
                  co++;

                i++;
                counter=0;
            }
       }
       //System.out.println(co);
       return out; 
        
    }
    
    
    public static String getCheckSum(String s){
        String str=s.substring(s.length()-8);
        System.out.println(str);
        return str;
    }
    
    private void saveFile(Socket socket) throws Exception {
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
        int i=0;
        int seqNo=0;
        //byte [] buffer = new byte[BUFFER_SIZE]; 
 
        
        Object o = ois.readObject();
        fileName=o.toString();
        File file1 = new File(fileName);
        System.out.println("fileName : "+fileName);
        if (o instanceof String) {
            
            fos = new FileOutputStream(file1);
        } else {
            throwException("Something is wrong");
        }
 
        
        Integer bytesRead = 0;
        int x=0;
        do {
            try{
                o = ois.readObject();
            }catch(Exception e){
                Path currentRelativePath = Paths.get("");
                String textPath = currentRelativePath.toAbsolutePath().toString();
                
                textPath=textPath+"\\"+fileName;
                Path path = Paths.get(textPath);
                //fos.close();
                //Files.delete(path);
                System.out.println("Here it ends");
            }
            try{
                if (!(o instanceof Integer)) {
                    throwException("Something is wrong");
                }
            }catch(Exception e){
                System.out.println("Something went wrong");
                break;
            }
 
            bytesRead = (Integer)o;
            System.out.println(bytesRead);
            o = ois.readObject();
 
            if (!(o instanceof byte[])) {
                throwException("Something is wrong");
            }
            i++;
            System.out.println(i);
            
            byte [] buffer = new byte[bytesRead];
            //System.out.println("array length : "+buffer.length);
            buffer = (byte[])o;
            String ss="";
            String bPattern="";
            for(int j=0;j<buffer.length;j++)
            {
                //System.out.print(buffer[j]);
                if(buffer[j]==1){
                    bPattern=bPattern+"1";
                }
                else{
                    bPattern=bPattern+"0";
                }
            }
            seqNo++;
            //System.out.println();
            //System.out.println("bit len: "+bPattern.length());
            String rh=deleteHeadTail(bPattern);
            String bdStaff=bitDstaffing(rh);
            //System.out.println("Pattern : "+rh);
            //System.out.println("new len : "+bdStaff);
            String sNo=intToBinary(seqNo);
            int len=sNo.length();
            String removeSeqNo=bdStaff.substring(len);
            //get Checksum String
            String gCheckSum=getCheckSum(removeSeqNo);
            //System.out.println("String check sum: "+gCheckSum);
            //*********get Checksum byte complement
            byte[] checkSum1=new byte[gCheckSum.length()];
            for (int cs=0; cs<gCheckSum.length(); cs++) {
                checkSum1[cs]= gCheckSum.charAt(cs)=='1' ? (byte)0 : (byte)1;
            }
            
            //*********remove checkSum
            String removeChecksum=removeSeqNo.substring(0, removeSeqNo.length()-8);
            //System.out.println("work pls : "+removeChecksum);
            
            
            
            System.out.println();
            
            
            
            //*********byte value of removeChecksum
            byte[] bval = new BigInteger(removeChecksum, 2).toByteArray();
            
            
            
            
            //*********new Checksum
            byte checkSum2=bval[0];
            for(int cs=1;cs<bval.length;cs++){
                checkSum2=(byte)(checkSum2 ^ bval[cs]);
                //System.out.print(checkSum2);
            }
            System.out.println();
            
            String csum=getBits(checkSum2);
            //System.out.println("Checksum : "+csum);
            
            byte[] checkSum3=new byte[csum.length()];
            for (int cs=0; cs<csum.length(); cs++) {
                checkSum3[cs]= csum.charAt(cs)=='1' ? (byte)1 : (byte)0;
            }
            //byte[] checkSum4=new byte[csum.length()];
            int temp;
            String compare="";
            for(int c=0;c<csum.length();c++)
            {
                temp=(int)checkSum3[c]+(int)checkSum1[c];
                System.out.println("check : "+temp);
                compare=compare+Integer.toString(temp); 
            }
            
            //check checkSum
            if(removeChecksum.length()==8192)
            {
                if(compare.equals("11111111")){ 
                    System.out.println("correct chunk");
                    removeChecksum = removeChecksum.replaceAll("(\\d{8})", "$1;");
                    String[] byteTokens = removeChecksum.split(";");
                    for (String sos : byteTokens) {
                        fos.write(Integer.valueOf(sos, 2));
                    }
                    oos.writeObject(1);
                }
                else{
                    oos.writeObject(0); 
                }
            }else{
                removeChecksum = removeChecksum.replaceAll("(\\d{8})", "$1;");
                String[] byteTokens = removeChecksum.split(";");
                for (String sos : byteTokens) {
                    fos.write(Integer.valueOf(sos, 2));
                }
                oos.writeObject(1);
            
            }
            
            
            
            //System.out.println("compare"+compare);
            System.out.println("final len: "+removeChecksum.length());
            
            
//            byte array[]=new BigInteger(removeChecksum,2).toByteArray();
//            
//            fos.write(array, 0, array.length);
            //x=array.length;
           
        } while (bytesRead>-1);
         
        System.out.println("File transfer success");
         
        fos.close();
 
        ois.close();
        oos.close();
        //socket.close();
        
    }
 
    public static void throwException(String message) throws Exception {
        throw new Exception(message);
    }
    
    
    private void sendFile(Socket s) throws Exception {

        FileInputStream fis = null;
        BufferedInputStream bis = null;
        OutputStream os = null;

        try {
           Path currentRelativePath = Paths.get("");
           String str = currentRelativePath.toAbsolutePath().toString();
           System.out.println(str);
           str=str+"\\"+fileName;
          //String path=System.getProperty("user.dir")
          //File myFile = new File ("E:\\BUET\\L3-T2\\Computer Networks\\Sessional\\Offline1\\Server\\"+fileName);
          File myFile = new File (str);

          byte [] mybytearray  = new byte [(int)myFile.length()];
          fis = new FileInputStream(myFile);
          bis = new BufferedInputStream(fis);
          bis.read(mybytearray,0,mybytearray.length);
          os = s.getOutputStream();
          //System.out.println("Sending " + "E:\\BUET\\L3-T2\\Computer Networks\\Sessional\\Offline1\\Server\\"+fileName + "(" + mybytearray.length + " bytes)");
          os.write(mybytearray,0,mybytearray.length);
          os.flush();
          System.out.println("Done.");
        }
        finally {
          if (bis != null) bis.close();
          if (os != null) os.close();
          //if (s!=null) s.close();
        }
        
        
        
        
        
    }
    
        
   
 
    public static void main(String[] args) {
        new Server().start();
    }
}  
