/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server2;

import java.net.*;
import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
//import public static map;

public class Server2 implements Runnable
{  private Connectclient users[] = new Connectclient[500];
   private ServerSocket server = null;
   private Thread thread = null;
   private int clientsize = 0;
   public static Map map = new HashMap();
    //public static String sender;
   // public static String reciever;
   
    public static long buffersize=500000000;
    public static long buffer;
    public static int FileID=0;
    public static Map filemap=new HashMap();

   public Server2(int port)
   {  try
      {  
         server = new ServerSocket(port);  
         System.out.println("Server started: " + server);
         begin();
      }
      catch(IOException ioe)
      {  
      }
   }
   public void run()
   {  while (thread != null)
      {  try
         {  
           // addThread(server.accept());
             Socket socket= server.accept();
             if (clientsize < users.length)
      {  
          System.out.println("Client accepted: " +socket);
           users[clientsize] = new Connectclient(this, socket);
         try
         {  users[clientsize].open(); 
            users[clientsize].start();  
            clientsize++; }
         catch(IOException ioe)
         {  System.out.println("Error opening thread: " + ioe); } }
           else
         System.out.println("Client refused: maximum " + users.length + " reached.");
            
            
         }
         catch(IOException ioe)
         {  System.out.println("Server accept error: " + ioe);
          end(); }
      }
   }
   public void begin()  {
       
        if (thread == null)
      {  thread = new Thread(this); 
         thread.start();
      }
   }
   public void end()   { 
       if (thread != null)
      {  thread.stop(); 
         thread = null;
      }
   }
   
  
    public synchronized void send(String filename,String sender,String reciever) throws FileNotFoundException, IOException 
   { 
       
     int tmp=0;
       String value = (String) map.get(reciever);
       if(value!=null){
         int ID1=Integer.parseInt(value);
         for (int i = 0; i < clientsize; i++){
         if (users[i].ID == ID1){
           tmp=i;
             
         
            
         }
       }
         users[tmp].communicate(filename,sender);
       }
       else{
           System.out.println("Cant Send To Reciever");
          
           File f=new File(filename);
           Server2.buffer=Server2.buffer-filename.length();
           
           f.delete();
       }
     
                                        
        
       
   }
     public synchronized void cut(int ID)
   {  int tmp = -1;
         for (int i = 0; i < clientsize; i++){
           
       
         if (users[i].ID == ID){
            tmp= i;
         }
       }
      if (tmp >= 0)
      {    Connectclient remove= users[tmp];
         System.out.println("Removing client thread " + ID + " at " + tmp);
         if (tmp< clientsize-1)
            for (int i = tmp+1; i < clientsize; i++)
               users[i-1] = users[i];
         clientsize--;
         try
         {  remove.close(); }
         catch(IOException ioe)
         {  System.out.println("Error closing thread: " + ioe); }
         remove.stop(); }
   }
   public synchronized void remove(int ID)
   {      int tmp =-1 ;
       Iterator iterator = map.keySet().iterator();
       while(iterator.hasNext()){
         String key   = (String) iterator.next();
         // System.out.println(key);
         String value = (String) map.get(key);
        // System.out.println(value);
         if(value.equals(String.valueOf(ID))){
              
             map.remove(key);
             break;
             
         }
       }
       for (int i = 0; i < clientsize; i++){
           
       
         if (users[i].ID == ID){
             
            tmp= i;
         }
       }
      
   
      if (tmp >= 0)
      {   Connectclient remove = users[tmp];
      
         System.out.println("Removing client thread " + ID + " at " + tmp);
         
         if (tmp < clientsize-1){
            for (int i = tmp+1; i < clientsize; i++){
               users[i-1] = users[i];
            }
         }
         clientsize--;
         try
         {  remove.close(); }
         catch(IOException ioe)
         {  
         }
         remove.stop(); }
   }
 
 
   public static void main(String args[]) { 
       
        Server2 server = null;
      
        server = new Server2(5555);
   }
   
}


 class Connectclient extends Thread
{  private  Server2  server    = null;
   private Socket socket    = null;
   public int ID        = -1;
   private DataInputStream  input  =  null;
   private DataOutputStream output = null;
   private String  m;
   private String sender;
   private String reciever;
   private long filesize;
   private String filename;
   private int size;
   private int bit;
   FileOutputStream f1;
   BufferedOutputStream b1;
   
   public Connectclient(Server2 s, Socket so)
   {  
       super();
   
      server = s;
      
      socket = so;
      ID = socket.getPort();
   }
  
   
   
    public void communicate(String msg,String id)
   {   try
       { 
           int size1=12;
          output.writeUTF("ok");
          output.flush();
         
         File file =new File(msg);
          
         FileInputStream f2 = new FileInputStream(msg);
         BufferedInputStream b2 = new BufferedInputStream(f2);
         OutputStream o2 = socket.getOutputStream();
         byte[] data;
	 long fileLength =file.length();
                
          output.writeUTF(msg);
          output.flush();
          output.writeLong(file.length());
          output.flush();
          output.writeUTF(id);
          output.flush();
     
                 long current = 0;
                                                  
                   while(current!=fileLength){
                     
                     
                         
                         //System.out.println(tstart);

                        if( fileLength- current <= 12){
                             size1 = (int)(fileLength - current); 
                        }
                        
                        current += size1;
                               
                       
                        data = new byte[size1];
                        
                        
                        b2.read(data, 0, size1); 
                        o2.write(data);
                                                           
                                                   								//to send the file size in bytes.
							
                   }

                
                
                
                        
                        o2.flush();
                        b2.close();
                        f2.close();
                        
                        
                        
                    Server2.buffer=Server2.buffer-fileLength;
                   
                   
                    file.delete();
                  
                   

                }   
          
          
          
          
          
          
          
          
          
          
          
          
       
       catch(IOException ioe)
       { // System.out.println(ID + " ERROR sending: " + ioe.getMessage());
          server.remove(ID);
          stop();
       }
   
   }
     public StringBuilder bitstuff(String s){
        int fidx=0,count=0;
        
        StringBuilder sb= new StringBuilder();
        for(int i=0;i<s.length();i++)
        {

            sb.append(s.charAt(i));

            if(s.charAt(i)=='1')
                count++;
            else
                count=0;
            if(count==5)
            {
              
                sb.append('0');
                count=0;
            }
        }
        return sb;
  
   }
    public StringBuilder destuff(String s){
        int fidx=0,count=0,sum=0;
        
        StringBuilder sb= new StringBuilder();
        for(int i=8;i<s.length();i++){
           
           

            if(s.charAt(i)=='1'){
                sb.append(s.charAt(i));  
                sum++;
                count++;
                if(count==5){
                    if(s.charAt(i+1)=='0'){
                        i++;
                        count=0;
                    }
                    else{
                        int in=sum-6;
                        int out=sum;
                        sb.delete(in,out);
                        break;
                      
                    }
                  
                }
         
            }
            else {
             
                sb.append(s.charAt(i));
                sum++;
                count=0;
                
             
            
            }
       
        }
        return sb;
  
   }
    public boolean error_check(String s,String c){
         
        int count=0;
        for (int i=0;i<s.length();i++){
            if(s.charAt(i)=='1'){
               count++;
               
               
           }
       }
        StringBuffer strbuf =new StringBuffer();
        for(int n=0;n<8;n++) 
        {
            strbuf.append(count % 2 ) ;
            count = count /2;
        }
       // System.out.println(c);
        String f=strbuf.reverse().toString();
        if(c.equals(f)){
          // System.out.println("true");
            return true;
        }
        else{
            return false;
        }

        
    }
        
    
   
   
    public void run()
   {    
        System.out.println("Server Thread " + ID + " running.");
        while (true)
        {      
            try {
                String recieve=input.readUTF();
                if(recieve.equals("sending")){
                    sender=input.readUTF();
                    String check= (String)Server2.map.get(sender);

                    if(check==null){
                        output.writeUTF("You Are Logged In");
                        output.flush();

                        Server2.map.put(sender,String.valueOf(ID));

                      }
                    else{
                       output.writeUTF("You Are Logged Out");
                       output.flush();

                       server.cut(ID); 
                    }
                 // server.handle(ID,sender);

               }
                if(recieve.equals("sending r")){

                    reciever=input.readUTF();
                  //server.handle1(ID,reciever,sender);
                    if(sender.equals(reciever)){
                        output.writeUTF("Invalid");
                        output.flush();
                    }



                 else {
                    String check= (String) Server2.map.get(reciever);
                    if(check==null){
                        output.writeUTF("Offline");
                        output.flush();

                    }
                     else{
                            output.writeUTF("Online");
                            output.flush();

                        }
                    }

               }
                 if(recieve.equals("sending file")){
                   //  System.out.println("u");
                    filename=input.readUTF();
                    filesize=Long.parseLong(input.readUTF());
                    if(Server2.buffer+filesize> Server2.buffersize){

                       output.writeUTF("not send");
                       output.flush();



               }
                  else{
                       output.writeUTF("send");
                       output.flush();
                  }
                 }
                 if(recieve.equals("send chunk")){
                        output.writeUTF("chunksize");
                        output.flush();
                        Random r= new Random();
                      //
                       //size=r.nextInt(10);
                        size=2;
                        byte[] contents = new byte[size];
                                    //int i=(int)filesize;

                        output.writeUTF(String.valueOf(size));
                        output.flush();
                        Server2.FileID++;
                        output.writeUTF(String.valueOf(Server2.FileID));
                        output.flush();

                 }
                if(recieve.equals("s")){
                     bit=0;
                     int tmp=0;
                     File file = new File(filename);
                     f1 = new FileOutputStream(filename);
                     b1 = new BufferedOutputStream(f1);
                     InputStream i1 = socket.getInputStream();
                     long read = 0; 
                     long full=0;
                     long t=0;





                   //  byte[] contents = new byte[10];



                     Server2.buffer+=filesize;
                    // System.out.println(filename);
                     bit=1;
                     output.writeUTF("s");
                     output.flush();

                    //  System.out.println(filesize);



                    while(full!=filesize)    
                    {
                        long tstart = System.nanoTime();
                        output.writeUTF("send size");
                        output.flush();

                        while(true){
                            int size1=input.readInt();

                            output.writeUTF("got size");
                            output.flush();
                            byte[] contents = new byte[size1];
                            read=i1.read(contents);


                            StringBuilder sb = new StringBuilder(contents.length * Byte.SIZE);
                            for( int k = 0; k< Byte.SIZE * contents.length; k++ ){
                                sb.append((contents[k / Byte.SIZE] << k% Byte.SIZE & 0x80) == 0? '0' : '1');
                            }
                            //System.out.println(sb);

                            StringBuilder s= new StringBuilder();
                            s.append(destuff(sb.toString()));
                            //System.out.println("destuff "+s);
                            StringBuilder seq=new StringBuilder();
                            seq.append(s.substring(8,16));
                            StringBuilder data= new StringBuilder();
                            data.append(s.substring(24,s.length()-8));
                            StringBuilder check= new StringBuilder();
                            check.append(s.substring(s.length()-8,s.length()));

                            //System.out.println("payload "+data);
                            int payloadbyte=data.length()/Byte.SIZE;
                            //System.out.println(payloadbyte);

                           // System.out.println("checksome "+check);
                            boolean right=error_check(data.toString(),check.toString());

                            int sLen = data.length();
                            byte[] toReturn = new byte[(sLen + Byte.SIZE - 1) / Byte.SIZE];
                            char c;
                            for( int k = 0; k< sLen; k++ ){
                                if( (c = data.charAt(k)) == '1' ){
                                    toReturn[k / Byte.SIZE] = (byte) (toReturn[k / Byte.SIZE] | (0x80 >>> (k % Byte.SIZE)));
                                }
                                else if ( c != '0' ){
                                    throw new IllegalArgumentException();
                                }
                            }



                            if(right==false){
                                String frame="01111110"+bitstuff("00000000"+seq.toString()+"00000000").toString()+"01111110";

                            //System.out.println("frame: "+frame);
                            //System.out.println("seq: "+seq);

                                output.writeUTF(frame);
                                output.flush();


                            }

                            else{
                                String frame="01111110"+bitstuff("00000000"+seq.toString()+"11111111").toString()+"01111110";
                            //System.out.println("frame: "+frame);
                            // System.out.println("seq: "+seq);

                                output.writeUTF(frame);
                                output.flush();
                                if(input.readUTF().equals("timenotout")){
                                    b1.write(toReturn, 0, toReturn.length); 
                                    full=full+payloadbyte;

                                    break; 
                                }

                            }


                        } 



                    }




                    b1.flush();
                    b1.close();
                    f1.close();
                    input.readUTF();

                    if(full==filesize && tmp==0) {
                        bit=0;
                        output.writeUTF("File is Sent Successfully");
                        output.flush();     
                    }
                    else{

                        bit=0;
                        output.writeUTF("File Sent unSuccessfully");
                        output.flush(); 
                        Server2.buffer=Server2.buffer-filesize;


                        file.delete();
                    }

                }
                if(recieve.equals("send to another client")){

                    server.send(filename,sender,reciever);

                }












            } 
            catch (IOException ex) {
                if(bit==1){

                    try {
                        b1.close();
                        f1.close();
                        File f= new File(filename);

                        f.delete();
                        Server2.buffer=Server2.buffer-filesize;
                      // System.out.println( Server2.buffer);
                    } 
                    catch (IOException ex1) {
                       Logger.getLogger(Connectclient.class.getName()).log(Level.SEVERE, null, ex1);
                    }







               }
               //System.out.println(filename+" "+filesize);
               server.remove(ID);
               Logger.getLogger(Connectclient.class.getName()).log(Level.SEVERE, null, ex);
           }


        }
    }
    public void open() throws IOException
   {  
        input = new DataInputStream(new  BufferedInputStream(socket.getInputStream()));
        output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
   }
    public void close() throws IOException
    {  
        if (socket != null) { 
            socket.close();
        }
        if (input!= null) {
            input.close();
         }
        if (output != null){
          
      
            output.close();
        }
    }
}
