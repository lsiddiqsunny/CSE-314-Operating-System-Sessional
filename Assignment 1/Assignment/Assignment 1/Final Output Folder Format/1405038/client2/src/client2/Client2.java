/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client2;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author User
 */
public class Client2 {

  
   public static void main(String[] args) throws Exception {
     Thread t=new ClientThread();
     t.start();
 }
}
class ClientThread extends Thread{
     int consecutive1=0;char ack;
     int mask,number_of_chunk;
    boolean is_late=false;
    boolean h=true;
    int kon_bit_e_parity_error;
     int kon_frame_e_parity_error;
    boolean parity_error_ase=false;
    Scanner scanner=null;
     //BufferedInputStream in=null;
    String filename=null;
    int file_size=0;
    int file_id=0;
    ObjectOutputStream oos=null;
    ObjectInputStream ois=null;
    String s=null;
    byte[]acknowledgement=new byte[3];
     BufferedInputStream in=null;
    networkutil nc=null;
    File file=null;
    byte[]bytes=null;
    int index=0;
     boolean is_receiving_state=false;
      int count;
      int parity_numbers=0;
      String d=null;int masks=0x80;
      char sequence_number;String kon_fram_2="";
      String s_n="";
     public ClientThread(){
     nc=new networkutil("localhost",44444);
      
}
     public void run(){
       
         String receiver_roll=null;
       int chunksize=0;
       boolean is_connected=true;
       
        try { 
            System.out.println("please enter your roll");
               scanner=new Scanner(System.in);
            s=scanner.nextLine();
            
        } catch (Exception ex) {
            Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
        } 
        try {
            nc.write(s);//client roll
        } catch (Exception ex) {
            
            is_connected=false;
            Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
           
        }
        try{
       s=nc.read().toString();
       System.out.println(s);
        }catch(Exception e){
       System.out.println("sorry someelse is online with your account");
      is_connected=false;
        }
        while(is_connected){
        System.out.println("enter your receivers roll to send file or press no to receive any file");
        s=scanner.nextLine();
          nc.write(s);
        if(s.contains("no")==false){
            //receiver roll
          
            s=nc.read().toString();
            System.out.println(s);
            if(s.contains("offline")){
                System.out.println(s);
                continue;
               
            }
        
       
       
        System.out.println("enter file name or directory to send");
        s=scanner.nextLine();
        File file = new File(s);
        System.out.println("file name is"+file.getName());
         nc.write((int)file.length());
         nc.write(file.getName());
         while(true){
             try{
         
         s=nc.ois.readObject().toString();
             }catch(SocketTimeoutException e){
                 continue;
             }catch(Exception e){
                 
             }
             break;
         }
         if(s.contains("exceed")){
             System.out.println("sorry server is full try again later");
             continue;
         }else{
             System.out.println(s);
         }
         chunksize=(int)nc.read();
        number_of_chunk=(int)file.length()/chunksize;
         if((int)file.length()%chunksize!=0){number_of_chunk+=1;}
        System.out.println("Server has fixed your chunksize as"+chunksize+"\n if you want to introduce timeout error in any frame put that frame number from 1 to  "+number_of_chunk);
        s=scanner.nextLine();
            try {
                nc.oos.writeObject(s);
                 nc.oos.flush();
            } catch (IOException ex) {
                Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
            }
       System.out.println("\n if you want to introduce parity error in any frame put that frame number from 1 to  "+number_of_chunk+"or press no not to introduce parity error");
         s=scanner.nextLine();
         if(s.contains("no")){
             parity_error_ase=false;
         }else{
             parity_error_ase=true;
             kon_frame_e_parity_error=Integer.parseInt(s);
             System.out.println("enter kon bit e parity error chao");
             s=scanner.nextLine();
             kon_bit_e_parity_error=Integer.parseInt(s);
         }
       
        bytes =new byte[chunksize];
             try {
                 in = new BufferedInputStream(new FileInputStream(file));
             } catch (FileNotFoundException ex) {
                 Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
             }
          try {
                
            boolean abar=false;   
             int[] parity=new int[8];
             
             String parity_pattern="";
              int j=1;//expected first seq no 0,next 1
              int frame_number=1;
              String dataframe="00000000";int acno_by_server;
   while (true) {
       if(abar==false){
       count = in.read(bytes);
       if(count<0){break;}
       j=1-j;
    
       }
       else{abar=false;
       System.out.println("\nok i am sending again frame number"+frame_number);}
       s_n="";
           for(int i=0;i<8;i++){ 
                boolean fo=(j & (masks>>>i)) != 0;
                 if(fo){
                 s_n+='1';
                 
                 }else{
                     s_n+='0';
                    
                 }
                 
            
     }
       for(int i=0;i<=7;i++){
               parity[i]=0;
           }
        parity_pattern="";s="";
       
                     consecutive1=0;
                     d="";
    for(int i=0;i<count;i++){
           
                mask=0x80;int p=0;
        for(int k=7;k>=0;k--,p++){
                 boolean fo=(bytes[i] & (mask>>>p)) != 0;
                 if(fo){
             //    s+='1';
                  parity[p]=1-parity[p];
                 d+='1';
              
                 }else{
              
                      d+='0';
             
                   
                 }
             }
    
              
         }
   
                    parity_pattern="";
                     for(int k=0;k<=7;k++){
             
             if(parity[k]==0){parity_pattern+='0';}
             else{parity_pattern+='1';}
                     }
                  
                  if(frame_number==kon_frame_e_parity_error&&parity_error_ase==true){
                      parity_error_ase=false;
                       //System.out.println("kuguyfvyu");
                     d=replace(d,kon_bit_e_parity_error,(d.charAt(kon_bit_e_parity_error)=='0'?'1':'0'));
                  }
                     String f=new String(dataframe+s_n+parity_pattern+d);
                     System.out.println("frame no"+frame_number+"full frame without bitstuff 01111110"+f+"01111110");System.out.flush();
                     consecutive1=0;s="";
                     for(int i=0;i<f.length();i++){
                         if(f.charAt(i)=='1'){
                             s+='1';
                             consecutive1++;
                             if(consecutive1==5){consecutive1=0;s+='0';}
                             
                         }else{
                             consecutive1=0;
                             s+='0';
                         }
                     }
                     System.out.println("frame no"+frame_number+"full frame with bitstuff 01111110"+s+"01111110");System.out.flush();
                 
                  nc.oos.writeObject(("01111110"+s+"01111110").getBytes());
                  nc.oos.flush();
                  
                      parity_pattern="";                        
                      try{
                        acknowledgement=(byte[])nc.ois.readObject();
                        s=new String(acknowledgement);
                        acno_by_server= Integer.parseInt(s.substring(8,8+8), 2);
                        if(acno_by_server!=1){
                            System.out.println("this is not an acknowledgement frame");System.out.flush();continue;
                        }
                        System.out.println("\nframe no"+frame_number+"acknowledge frame sent by server"+s+"\n\n\n");System.out.flush();
                     
                        
                        }catch( SocketTimeoutException e){abar=true;System.out.println("\nserver acknowledge pathay nai frame"+frame_number+" er jonno tai abar pathano lagbe\n\n\n");System.out.flush();
                            continue;} 
                        catch (ClassNotFoundException ex) {
               Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
           }
                      
                         
                  
                     frame_number++;
                 } 
                  
                 nc.write("completed sending full file");
                
                 s=nc.read().toString();
                 System.out.println(s);
             } catch (IOException ex) {
                 Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
             }System.out.println("done");
           
        }else{
            while(true){
            try{
            s=nc.ois.readObject().toString();
            }catch(SocketTimeoutException e){
               continue;
            } catch(Exception e){
                
            }
            break;
            }
            System.out.println(s);
            if(s.contains("none")){System.out.println("try later no file to receive now");continue;}
            s=scanner.nextLine();
            byte[]b=s.getBytes();
           
            nc.write(s);
            if(s.contains("no")){continue;}
            
           
            filename=nc.read().toString();
             file_size=(int)nc.read();
            file_id=(int)nc.read();
            System.out.println("file name is "+filename+"size is"+file_size);
           File ff=new File("D:/received/"+filename);
            bytes=new byte[1024];
            int count;
            int j=1;
            int size=0;
             OutputStream out=null;
             try {
                 out = new FileOutputStream(ff);
             } catch (FileNotFoundException ex) {
                 Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
             }
             try {
                 while ((count = nc.bis.read(bytes))>0) {
                     System.out.println("chunk number"+j+"size"+count);j++;
                     size+=count;
                      out.write(bytes, 0, count);
                      if(size==file_size){System.out.println("total size"+size+" successfully received"); out.close();break;}
                    
                     
                 }    } catch (IOException ex) {
               
                     
                 Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
             }
            
            
        }
     }
     }
        
        static  String replace(String str, int index, char replace){     
    if(str==null){
        return str;
    }else if(index<0 || index>=str.length()){
        return str;
    }
    char[] chars = str.toCharArray();
    chars[index] = replace;
    return String.valueOf(chars);       
}
           
}

