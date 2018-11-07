/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Random;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author User
 */
public class Server {
public static int max_buffer_size=400000000;
    public static int current_buffer_size=0;
    public static HashMap<Integer,File>FHM=null;//for getting file
    public static HashMap<String,networkutil>HM=null;//for getting whose online
    public static HashMap<String,receivers_file_info>RHM=null;
    public static int file_id=1;
public static void main(String[] args) throws IOException {
    Thread t=new ServerThread();
    t.start();
}
}
class ServerThread extends Thread{
     ServerSocket serverSocket = null;
      Socket clientsocket = null;
     
       networkutil nc=null;
       public ServerThread(){
           Server.FHM=new HashMap<Integer,File>();
           Server.HM=new HashMap<String,networkutil>(120);
           Server.RHM=new HashMap<String,receivers_file_info>();
           try {
            serverSocket = new ServerSocket(44444);
           }catch (IOException ex) {
            System.out.println("Can't setup server on this port number. ");
           }
       }
    public void run(){
        while(true){
        try {
            clientsocket = serverSocket.accept();
            nc=new networkutil(clientsocket);
             Thread t=new working_thread_of_a_client(nc);
                t.start();
                System.out.println("a client is connected");
        }catch (IOException ex) {
            System.out.println("Can't accept client connection. ");
        } 
        }
    }
}
class working_thread_of_a_client extends Thread{
    Thread t; String acknowledgement=null;
    int count;String z;String kon_fram="";
    boolean should_go_to_initial_loop=false;
    int number_of_chunk=0;
int this_file_id=0;int co1=0;
    networkutil nnc=null;
    String receiver_roll=null;
    String filename=null;
    int bytes_read=0;
    int chunksize=0;
    String s=null;
    Random rand=new Random();  int masks=0x80;
    String client_roll=null;
    int index=0;
    int filesize=0;
    networkutil nc=null;
   OutputStream out = null;
   BufferedInputStream in=null;
 File f=null;
 boolean is_late=false;
   String koy_number_frame_e_timeout_error,s_n;
   int koy_number_frame_e_timeout_errors;
   byte[]bytes=null;
   int[]parity=null;
   int parity_numbers=0;
    int ager_koynumber=0;String stuff,no_stuff;
    boolean repeat_write=false;
    public working_thread_of_a_client(networkutil nc){
      
        this.nc=nc;
        parity=new int[8];
    }
    public void run(){
       
        try {
            while(true){
                try{
            s=nc.ois.readObject().toString();//taking client roll
                }catch(SocketTimeoutException e){
                    continue;
                    
                }
                break;
            }
            System.out.println("clientroll"+s+"is trying to connect");
             if(Server.HM.containsKey(s)){
                       nc.closeConnection();
                       return;
             }else{
                      Server.HM.put(s, nc);
                      client_roll=s;
                      s="server has identified your roll as"+client_roll;
                      nc.write(s);
                  }
        
        }
        catch(Exception e){
            nc.closeConnection();
            
            System.out.println(e);
            return;
        }
      while(true){
      receivers_file_info useful=null;
      while(true){
      try{
        receiver_roll=nc.ois.readObject().toString();
      }catch(SocketTimeoutException e){
          continue;
      }catch(Exception ex){
         Server.HM.remove(client_roll);
          nc.closeConnection();
          return;
      }
      break;
      }
       if(receiver_roll.contains("no")){
               
                if(!Server.RHM.containsKey(client_roll)){
                    nc.write("none");
                    continue;
                }else{
                    if(Server.RHM.get(client_roll).queue.isEmpty()==true){
                        nc.write("none");
                        continue;
                    }
                }
                useful=Server.RHM.get(client_roll);
               
                if(useful.is_empty()==false){
                    singleinfo single=useful.queue.remove();
                f=Server.FHM.get(new Integer(single.file_id));
           System.out.println("file name is"+f.getAbsolutePath());
                nc.write("roll number"+single.ke_pathaise+"wants to send you file number "+single.file_id+"at chunk size 1024 press yes to receive and no to ignore file size is"+f.length());
    
                bytes=new byte[1024];
                   while(true){
                       try{
                 s=nc.ois.readObject().toString();
                       }catch(SocketTimeoutException e){
                           continue;
                       }catch(Exception e){
                           
                       }
                       break;
                   }
                 if(s.contains("no")){
                     Server.current_buffer_size-=(int)f.length();
             
                if(f.exists()){f.delete();System.out.println("hoise");}
              
                       
                 Server.FHM.remove(single.file_id);
                 single=null;
                 continue;
                 }
                 nc.write(f.getName());
                 nc.write((int)f.length());
                 nc.write(single.file_id);
                 try {
                 in = new BufferedInputStream(new FileInputStream(f));
                 } catch (FileNotFoundException ex) {
                 System.out.println("file not found"+ex);
                 
                 Logger.getLogger(working_thread_of_a_client.class.getName()).log(Level.SEVERE, null, ex);
                 continue;
                 }
    
                int p=1;
                int size2=0;
                 try {
                    while ((count = in.read(bytes)) > 0) {
            
                     try {
                        nc.bos.write(bytes, 0, count);
                        nc.bos.flush();
                        size2+=count;
                
                
                System.out.println("chunk size"+count+"chunk number"+p); p++;
            } catch (IOException ex) {
               
               in.close();
               Server.RHM.get(client_roll).queue.add(single);
                 Server.HM.remove(client_roll);
                Logger.getLogger(working_thread_of_a_client.class.getName()).log(Level.SEVERE, null, ex);
                nc.closeConnection();
                return;
            }
           
           
 
        }
      
           
    } catch (IOException ex) {
                        try {
                          
                            in.close();       } catch (IOException ex1) {
                            Logger.getLogger(working_thread_of_a_client.class.getName()).log(Level.SEVERE, null, ex1);
                        }
         Server.HM.remove(client_roll);
         Server.RHM.get(client_roll).queue.add(single);
         nc.closeConnection();
        Logger.getLogger(working_thread_of_a_client.class.getName()).log(Level.SEVERE, null, ex);
        return;
    }
           File f=Server.FHM.get(single.file_id);
            Server.current_buffer_size-=(int)f.length();
                    try {
                        in.close();
                    } catch (IOException ex) {
                        Logger.getLogger(working_thread_of_a_client.class.getName()).log(Level.SEVERE, null, ex);
                    }
                if(f.exists()){System.out.println("hoise1"+f.getAbsolutePath());f.delete();}
             Server.FHM.remove(single.file_id);
           
             System.out.println("done");
           
       }
                continue;
       }
      
        if(Server.HM.containsKey(receiver_roll)==false){
            nc.write("your receiver is offline");
            continue;
             
            
        }else{
            nc.write("ok your receiver is online");
        }
        while(true){
            try{
        filesize=(int)nc.ois.readObject();
            }catch(SocketTimeoutException e){
                continue;
            }catch(Exception e){
                break;
            }
            break;
        }
        System.out.println("filesize is"+filesize);
        filename=nc.read().toString();
        System.out.println("file name is"+filename);
         if(filesize>(Server.max_buffer_size-Server.current_buffer_size)){
         
                nc.write("exceed");
                continue;
             
            
            }else{
                 nc.write("your file_id is"+Server.file_id);
                this_file_id=Server.file_id;
                Server.file_id++;
         }
         chunksize=rand.nextInt(filesize)%8192;
         nc.write(chunksize);
         while(true){
            try {
                koy_number_frame_e_timeout_error=nc.ois.readObject().toString();
            }
            catch(SocketTimeoutException e){
                continue;
            }catch (IOException ex) {
                Logger.getLogger(working_thread_of_a_client.class.getName()).log(Level.SEVERE, null, ex);continue;
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(working_thread_of_a_client.class.getName()).log(Level.SEVERE, null, ex);continue;
            }
            break;
         }
         if(koy_number_frame_e_timeout_error.contains("no")){koy_number_frame_e_timeout_errors=-1;}
         else{koy_number_frame_e_timeout_errors=Integer.parseInt(koy_number_frame_e_timeout_error);}
         System.out.println("sender said timeout should be  in "+koy_number_frame_e_timeout_error);
         number_of_chunk=filesize/chunksize;
         if(filesize%chunksize!=0){number_of_chunk+=1;}
       
         bytes = new byte[chunksize];
      
    try {
        
        int index=filename.lastIndexOf('.');
         String first=filename.substring(0,index);
     
       String last=filename.substring(index+1,filename.length());
       filename="D:/serverreceived/"+first+this_file_id+"."+last;
       f=new File(filename);
        out = new FileOutputStream(f);
        
    } catch (FileNotFoundException ex) {
        Logger.getLogger(working_thread_of_a_client.class.getName()).log(Level.SEVERE, null, ex);
        continue;
    }
        int count;
        int j=1;
        int size=0;
        String receivedchunk=null;
        byte[]finals=new byte[chunksize+100];
    
       byte[]initial=new byte[chunksize+100];
       int dataframe_naki_acno;
       String acno="00000001";
        //   int ager_frame=0;
           int counts;int expected_seq_no=0; int sender_er_pathano_seq_number;
    try {
        while(true) {
          stuff="";no_stuff="";
           try{
               initial=(byte[])nc.ois.readObject();
               receivedchunk=new String(initial);
               stuff=receivedchunk;
              
                receivedchunk=receivedchunk.substring(8);
               receivedchunk=receivedchunk.substring(0,receivedchunk.length()-8);
                dataframe_naki_acno=Integer.parseInt(receivedchunk.substring(0,8),2);
                if(dataframe_naki_acno!=0){
                    System.out.println("this is not a dataframe\n");System.out.flush();continue;
                }
               co1=0;
               for(int i=0;i<receivedchunk.length();i++){
                   if(receivedchunk.charAt(i)=='1'){
                       co1++;
                       no_stuff+='1';
                       if(co1==5){i++;co1=0;}
                   }else{
                       co1=0;
                       no_stuff+='0';
                   }
               }
                s_n=no_stuff.substring(8,8+8);
                sender_er_pathano_seq_number = Integer.parseInt(s_n, 2);
                
        
           System.out.println("\nframe no"+j+"original frame with stuffing"+stuff);System.out.flush();
              System.out.println("\nframe no"+j+"original frame without stuffing 01111110"+no_stuff+"01111110");System.out.flush();
          
            
           }catch(SocketTimeoutException e){
               continue;
           }
           
          
             
          String second="";
        
          
         
          
           co1=0;
           
           int koynumber=0; parity_numbers=0;
           for(int k=7;k>=0;k--){parity[k]=0;}
           for(int i=16+8;i<no_stuff.length();i++){
               if(no_stuff.charAt(i)=='1'){
                   second+='1';
                     parity[parity_numbers]=1-parity[parity_numbers];
                  
                
                 
                   }
               else{
                    
                   second+='0';
               }
               if(second.length()==8){
                    int val = Integer.parseInt(second, 2);
finals[koynumber] = (byte) val;
koynumber++;

second="";
 
               }
               
                  parity_numbers++;
               if(parity_numbers==8){parity_numbers=0;}
           }
        
           
           System.out.print("\nframe no"+j+"detected parity pattern by server");System.out.flush();
            
       for(int i=0;i<8;i++){
               System.out.print(parity[i]);
           }
           
           
                 System.out.println("\nframe no"+j+"received parity pattern by sender "+no_stuff.substring(8+8,8+8+8)+"\n");System.out.flush();
                   parity_numbers=0;boolean parity_vul_paise=false;
           for(int i=8+8;i<16+8;i++){
               if(!((no_stuff.charAt(i)=='0'&&parity[parity_numbers]==0)||(no_stuff.charAt(i)=='1'&&parity[parity_numbers]==1))){
                   System.out.println("frame no"+j+"parity error at parity bit"+parity_numbers+" senders parity bit here "+no_stuff.charAt(i)+"but server getting parity bit here  "+parity[parity_numbers]);
                   parity_vul_paise=true;
                   break;
               }
               parity_numbers++;
           }
            if(parity_vul_paise==true){
           
              System.out.println("\nok parity error in frame "+j+"sender will send again\n");
            
               
               continue;
               
           }
           
              if  (sender_er_pathano_seq_number!=expected_seq_no){ 

 //((FileOutputStream)out).getChannel().truncate(f.length()-koynumber);
   // size-=koynumber;
     //        Server.current_buffer_size-=koynumber;
     System.out.println("\nframe no"+j+"server abar paise sender theke\n");System.out.flush();
     

          }else{
                  
                  expected_seq_no=1-expected_seq_no;
                   size+=koynumber;
             Server.current_buffer_size+=koynumber;
              out.write(finals,0,koynumber);
              out.flush();
              }
           
               
     
         
          
     if(koy_number_frame_e_timeout_errors==j){
            koy_number_frame_e_timeout_errors=-1;
              System.out.println("\nok timeout error in frame "+j+"sender will send again\n");
             
              continue;
          }
        
      
          s="";
        
       
           acknowledgement="01111110"+acno+s_n+"01111110";
            
               nc.oos.writeObject(acknowledgement.getBytes());    
            if(j==number_of_chunk){
                s=nc.read().toString();
                System.out.println("client said"+s);
                 if(size==filesize){
                   nc.write("successfullyreceived");
                    // nc.write("total size"+size+" successfully received");
                     break;
                 }else{
                     
                     nc.write("total size"+size+"received but initial file was "+filesize);
                     should_go_to_initial_loop=true;
                     f=null;
                    break;
                 }
            }
           
           j++;
           
           
        }
       // ((FileOutputStream)out).getChannel().close();
        out.close();
        
        if(should_go_to_initial_loop==true){
            should_go_to_initial_loop=false;
            Server.current_buffer_size-=size;
            continue;
        }
        f=new File(filename);
        Server.FHM.put(new Integer(this_file_id),f);
        System.out.println(" file size "+f.length());
        if(Server.RHM.containsKey(receiver_roll)){
           // System.out.println("add hoise");
            Server.RHM.get(receiver_roll).add_file(client_roll,this_file_id);
        }else{
            receivers_file_info ft=new receivers_file_info();
           // System.out.println("client roll is"+client_roll+"id "+this_file_id);
            ft.add_file(client_roll,new Integer(this_file_id));
        Server.RHM.put(receiver_roll,ft);
        }
        System.out.println("done");
    } catch (Exception ex) {
        System.out.println("exception is client is offline"+ex);
          try {
              out.close();
          } catch (IOException ex1) {
              Logger.getLogger(working_thread_of_a_client.class.getName()).log(Level.SEVERE, null, ex1);
          }
f.delete();
        Server.current_buffer_size-=size;
        if(Server.FHM.containsKey(this_file_id)){
       Server.FHM.remove(this_file_id);
        }
        Logger.getLogger(working_thread_of_a_client.class.getName()).log(Level.SEVERE, null, ex);
         continue;
    }
    
      }
    }
}

              
             
  
   
   
    
   
   