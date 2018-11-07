
package client2;


import com.sun.org.apache.xalan.internal.xsltc.dom.BitArray;
import java.net.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import static sun.security.krb5.Confounder.bytes;

public class Client2 implements Runnable
{  
   private Socket socket = null;
   private Thread thread = null;
   private DataInputStream  instream = null;
   private DataOutputStream outstream = null;
   private Filetrans user = null;
   public static int start=0;
   public File file;
   public  long fileLength;

   public Client2(String s, int p)
   { 
      try
      {  socket = new Socket(s, p);
         System.out.println("Connected: " + socket);
         
         System.out.println("Enter Your ID: ");
         start++;
         begin();
        
      }
      catch(UnknownHostException uhe)
      {  
      }
      catch(IOException ioe)
      {  
      }
   }
   public void run()
   {  while (thread != null)
      {  try
         { 
            // String s=console.readLine();
            if(start==1){
                
            start=0;
            outstream.writeUTF("sending");
            outstream.flush();  
            String s=instream.readLine();
            outstream.writeUTF(s);
            outstream.flush();
             }
             else if(start==2){
                
                 
            start=0;
            System.out.println("Enter Reciever ID : ");
            outstream.writeUTF("sending r");
            outstream.flush();  
            String s=instream.readLine();
            outstream.writeUTF(s);
           outstream.flush();
                 
                 
             }
             else if(start==3){
                
            start=0;
            outstream.writeUTF("sending file");
            outstream.flush();  
            System.out.println("Enter File Name : ");
            
            
           
            String filename=instream.readLine();
             file=new File(filename);
         
            outstream.writeUTF(filename);
            outstream.flush();
	     fileLength = file.length();
            outstream.writeUTF(String.valueOf(fileLength));
            outstream.flush();

           
                 
             }
             else if(start==4){
                 start=0;
              
               outstream.writeUTF("send chunk");
               outstream.flush();
                 
                
                 
                 
                 
             }
              else if(start==5){
                 
                  start=0;
                  outstream.writeUTF("s");
                  outstream.flush();
                 
             }
              
          
           
          
         }
         catch(IOException ioe)
         { // System.out.println("Sending error: " + ioe.getMessage());
            end();
         }
      }
   }

   
   
  public void begin() throws IOException
   {  
     
      outstream = new DataOutputStream(socket.getOutputStream());
      
       instream  = new DataInputStream(System.in);
       
      if (thread == null)
          
      {  user = new Filetrans(this, socket);
      
         thread = new Thread(this); 
         
         thread.start();
      }
       
   }
 
   
   
   public void end()
   {  if (thread != null)
       
      {  thread.stop();  
         thread = null;
         
      }
      try
      {  if (instream   != null) {
          instream.close();
      }
         if (outstream != null) {
             outstream.close();
         }
         if (socket    != null)  {
             socket.close();
         }
      }
      catch(IOException ioe)
      {  System.out.println("Error closing ..."); }
      user.close();  
      user.stop();
   }
   public static void main(String args[])
   {    Client2 user = null;
      
         user = new Client2("localhost",5555);
   }
}


 class Filetrans extends Thread
 
{  private Socket socket   = null;

   private Client2  user   = null;
   
   private DataInputStream  input = null;
   
   private DataOutputStream  output = null;
   
   private   int chunksize;
   
   private int  fileId;

   public Filetrans(Client2 c, Socket s)
   {  user   = c;
      socket   = s;
      open();  
      start();
   }
   public void open()
   {  try
      {  input = new DataInputStream((socket.getInputStream()));
         output = new DataOutputStream(socket.getOutputStream());
      }
      catch(IOException ioe)
      {  
         user.end();
      }
   }
   public void close()
   {  try
      {  if (input != null)
          input.close();
      }
      catch(IOException ioe)
      {  System.out.println("Error closing input stream: " + ioe);
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
   public StringBuilder randombit(String s){
        int error;
        Random r= new Random();
        StringBuilder sb= new StringBuilder();
        sb.append(s);        
        error=r.nextInt(4);
        if(error==0){
            
            int changebit=r.nextInt(s.length()-1);
            if(s.charAt(changebit)=='1'){
            
                sb.setCharAt(changebit, '0');
                
            }
            else{
                 sb.setCharAt(changebit, '1');
            
            }
            System.out.println("Error generated at bit : "+changebit);
            System.out.println("Error generated payload : "+sb);
            return sb;
                 
            
            
        }
        else{
           
            return sb;
        }
        
       
   }
     public StringBuffer checksome (String s){
      
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
        return strbuf.reverse();

       
       
   }
  
   public void run()
   {    while (true)
        { 
            try {
                String response=input.readUTF();
                if(response.equals("You Are Logged In")){
                    System.out.println("Server Says : "+response);
                      Client2.start=2;




               }
                if(response.equals("You Are Logged Out")){
                    System.out.println("Server Says : "+response);
                 //  client.handle(response);
                    user.end();
               } 
                if(response.equals("Online")){
                   System.out.println("Server Says : Reciever is "+response);
                   Client2.start=3;



               } 
                if(response.equals("Offline")){
                   System.out.println("Server Says : Reciever is "+response);
                    Client2.start=2;

               } 
                 if(response.equals("Invalid")){
                   System.out.println("Server Says : Reciever is "+response);
                    Client2.start=2;


               } 
                 if(response.equals("send")){
                       System.out.println("Server Says : Your File is ready to send "); 

                       Client2.start=4;

                 }
                 if(response.equals("not send")){

                      System.out.println("Server Says : Your File is Big ! Try another ");
                      Client2.start=3;



                 }
                 if(response.equals("chunksize")){

                    chunksize=Integer.parseInt(input.readUTF());

                    System.out.println("chunksize : "+chunksize);
                    System.out.println();
                    fileId=Integer.parseInt(input.readUTF());
                    Client2.start=5;



                 }
                 if(response.equals("s")){
                    int tmp=0;
                    FileInputStream f1 = new FileInputStream(user.file);
                    BufferedInputStream b1 = new BufferedInputStream(f1);
                    OutputStream o1 = socket.getOutputStream();

                    byte[] data;

                    long Length = user.fileLength;





                    long current = 0;


                    int i=1;
                    int size=chunksize;
                    long total=0;

                    while(current!=Length){




                        input.readUTF();
                        if(Length - current <= size){
                           size = (int)(Length - current); 
                        }

                        current += size;


                        data = new byte[size];

                        total+=size;
                        b1.read(data, 0, size); 




                        StringBuilder sb = new StringBuilder(data.length * Byte.SIZE);
                        for( int k = 0; k< Byte.SIZE * data.length; k++ ){
                            sb.append((data[k / Byte.SIZE] << k% Byte.SIZE & 0x80) == 0 ? '0' : '1');
                        }

                        System.out.println("Payload :"+sb);



                        int seq=0;
                        while(true){
                             long tstart = System.nanoTime();
                            // System.out.println(start);

                            StringBuilder payload = new StringBuilder(data.length * Byte.SIZE);
                            payload=randombit(sb.toString());

                            StringBuilder strb = new StringBuilder();

                            StringBuffer strbuf =new StringBuffer();
                            int s1=seq;
                            for(int n=0;n<8;n++) 
                            {
                                strbuf.append( s1 % 2 ) ;
                                s1 = s1 /2;
                            }










                            strb.append("00000001"+strbuf.reverse()+"00000000").append(payload.substring(0, payload.length())).append(checksome(sb.toString()));
                            System.out.println("Sequence no : "+strbuf);
                            System.out.println("Checksum: "+checksome(sb.toString()));

                            System.out.println("Frame before bit stuff : "+strb);



                            StringBuilder s= new StringBuilder();
                            s.append("01111110"+ bitstuff(strb.toString())+"01111110");
                            System.out.println("Frame after stuffing : "+s); 



                            int sLen = s.length();
                            byte[] Return = new byte[(sLen + Byte.SIZE - 1) / Byte.SIZE];
                            char c;
                            for( int k = 0; k< sLen; k++ ){
                                if( (c = s.charAt(k)) == '1' ){
                                    Return[k / Byte.SIZE] = (byte) (Return[k / Byte.SIZE] | (0x80 >>> (k % Byte.SIZE)));
                                }
                                else if ( c != '0' ){


                                    throw new IllegalArgumentException();
                                }
                            }







                          //  System.out.println(toReturn);
                            output.writeInt(Return.length);
                            output.flush();
                            input.readUTF();
                            o1.write(Return);



                            System.out.println("Sending file : frame  "+i+" Of FileID "+fileId);
                            long tend = System.nanoTime();



                            String a=input.readUTF();
                            long d=(tend-tstart)/1000;
                            StringBuilder ack=new StringBuilder();
                            ack.append(destuff(a));
                            // System.out.println(ack);

                            a=ack.substring(16,24);

                            if(a.equals("11111111")){

                                System.out.println("Total Time taken :"+d+" microsec");
                                if(d>5000){

                                    System.out.println("Server Timeout");
                                    output.writeUTF("timeout");
                                    output.flush();
                                }
                                else{
                                    System.out.println("Server Says :Positive acknowledgement,send next frame");
                                    output.writeUTF("timenotout");
                                    output.flush();
                                    System.out.println();
                                    System.out.println();
                                    System.out.println();
                                    break;
                                }





                            }
                            else{
                                System.out.println("Server Says :Negative acknowledgement,send again");

                            }



                            seq=seq+1; 


                        }

                        i++;



                    }


                    output.writeUTF("last");
                    output.flush();


                    o1.flush(); 
                    b1.close();
                    f1.close();

                    String s=input.readUTF();


                    System.out.println("Serrver Says: "+s);

                    if(s.equals("File is Sent Successfully")){
                        output.writeUTF("send to another client");
                        output.flush();
                        Client2.start=2;
                    }
                     else{
                        Client2.start=2;
                    }


                }
                if(response.equals("ok")){

                    String fname=input.readUTF();
                    Long fsize=input.readLong();
                    String fromid=input.readUTF();
                    System.out.println("You Will Recieve a File Named "+fname+" Of Size "+fsize+" Bytes From "+fromid);


                    File file = new File("D:"+fname);

                    FileOutputStream f1 = new FileOutputStream(file);
                    BufferedOutputStream b1 = new BufferedOutputStream(f1);
                    InputStream i1 = socket.getInputStream();
                    long read = 0;
                    long total=0;






                    byte[] data = new byte[123];




                    while(total!=fsize){


                        read=i1.read(data);

                        total=total+read;

                        b1.write(data, 0, (int)read); 
                    }
                    b1.flush();

                    System.out.println("File Recieved Successfully");
                    b1.close();
                    f1.close();






                }



             } 
            catch (IOException ex) {
                Logger.getLogger(Filetrans.class.getName()).log(Level.SEVERE, null, ex);
                user.end();
            }
      
        } 
    }
}


