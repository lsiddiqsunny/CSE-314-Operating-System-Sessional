package dllserver;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Soumita
 */
public class DllConnection implements Runnable {

    private Socket cSocket;
    private BufferedReader in = null;
    private int cid;
    private PrintStream ps;
    
    HashMap<String,Socket> clients=new HashMap<String,Socket>();
    HashMap<Integer,Socket> cli = new HashMap<Integer,Socket>();
    HashMap<Integer,Integer> clientid=new HashMap<Integer,Integer>();
    

    public DllConnection(Socket client,int c,HashMap<Integer,Socket> cli,HashMap<Integer,Integer>clientid)  {
        this.cSocket = client;
        this.cid = c;
        this.cli = cli;
        this.clientid=clientid;
    }

    @Override
    public void run() {
       
        try {
            in = new BufferedReader(new InputStreamReader(
                    cSocket.getInputStream()));
            String clientSelection;
            String rid;
            String id1 = null;
            String fname;
            Long fs;
            int id2 = 0,id3;
            int memsize = 800000;
            int current = 0;
            
            
          
            System.out.println("Client : "+cid);
            
       
        
        int id=Integer.parseInt(in.readLine());
        
        clientid.put(id,cid);
        
        System.out.println("id "+id);
        String dec=in.readLine();
        System.out.println("operation "+dec);
        
       
            if(dec.equals("send"))
            {
                fname=in.readLine();
                System.out.println("fname "+fname);
                
                /*fs=Long.parseLong(in.readLine());
                System.out.println("fsize "+fs);*/
                
               
                
                rid=in.readLine();
                id1=rid;
                id2 = Integer.parseInt(id1);
                
                id3 = id2;
                System.out.println("rid "+rid);
                
                writeFile_try_2();
                
              
             //   receive1File();
                sendtoClient2(fname,id2);
                
            }
            else
            {
                
                
                //receiveFile();
                
                fname = in.readLine();
                System.out.println(fname);
               // sendFile(fname);
               //sendtoClient2(fname,id2);
            }
           
      //  }
      //  }
      // in.close();
       
       // clientSocket.close();
        
        

        } catch (IOException ex) {
            System.out.println("error");
           
            
        }
     
        
        
    }

   
    

   
    
    
    
    
     public void sendtoClient2(String fileName,int rid) {
           int rval = 0;
           for (Map.Entry<Integer,Integer> entry: clientid.entrySet()) {
               int tmp=entry.getKey();
               System.out.println(tmp);
               if(tmp == rid)
               {
                   rval = entry.getValue();
                   System.out.println(rval);
               }
               
           }
           System.out.println("got rval"+rval);
           for (Map.Entry<Integer,Socket> entry: cli.entrySet()) {
                
            int cid = entry.getKey();
            
           // if(cid == rid )
            
            if (cid == rval)
            {
               
                cSocket = entry.getValue();
     
     
     try{
                
            ps = new PrintStream(cSocket.getOutputStream());
            File myFile = new File("new"+fileName);
            long fsize = myFile.length();
            System.out.println(myFile.length());
            
            

            FileInputStream fis = new FileInputStream(myFile);
            BufferedInputStream bis = new BufferedInputStream(fis);
          
            

            
            DataInputStream dis = new DataInputStream(bis);
           
            OutputStream os = cSocket.getOutputStream();
            DataOutputStream dos = new DataOutputStream(os);
            
            
            Random rn = new Random();
            int chunk = rn.nextInt(10000)+1000;
            System.out.println(chunk);
           
           
           dos.writeInt(chunk);
           dos.flush();
            
            
            
            byte store[] = new byte[chunk];
            int rd;
            int total =0;
            
            
           
           
           dos.writeUTF("Receive File");
           
           
           
           dos.writeLong(fsize);
            System.out.println(myFile.length());
            
            while(fsize > 0 && (rd=dis.read(store,0,(int)Math.min(store.length,fsize))) != -1 )
            {
               
                total = total + rd;
                System.out.println("total "+total);
                ps.write(store,0,rd);
                System.out.println("ps written");
            
                dos.write(store,0,rd);
                
               
                
                System.out.println("dos written");
              
                
                
                
                fsize = fsize - rd;
                System.out.println("bytes sent "+rd +" "+total+" "+fsize);
              
                
            }
            System.out.println("file sent to client successfully");
            
           
            System.out.println("File "+fileName+" sent to client.");
            
           
            
        } catch (Exception e) {
            System.err.println("File does not exist!");
        }
           }
            }
           
     }
     
    
     
     
     
    
     
     
     
     
     
     
     public static String getChksum(String s)
     {
         int sum = 0;
         int got=0;
         for(int i=0;i<s.length();i++)
         {
             if(s.charAt(i) == '1')
             {
                 got = Character.getNumericValue(s.charAt(i));
                // System.out.println(got);
                 sum = sum + got;
                // System.out.println(sum);
                 
             }
         }
         
         sum = sum % 256;
         //return got;
         String sgot = Integer.toBinaryString(sum);
         
         int lg = sgot.length();
         for(int lp=0;lp<(8-lg);lp++)
         {
             sgot="0"+sgot;
         }
        // System.out.println(sgot);
         return sgot;
         
     }
     
     
     public static String BinaryToString(String binaryCode)
 {
     String[] code = binaryCode.split(" ");
     String word="";
     for(int i=0;i<code.length;i++)
     {
         word+= (char)Integer.parseInt(code[i],2);
     }
     System.out.println(word);
     return word;
 }
     
     
     
     
     
     public static String bitStuffing1 (String str)
     {
         int i,j,k;
      
         String fixed = "";
         int l = str.length();
     
      int cnt = 0;
         for(i=0;i<l;i++)
         {
             if(str.charAt(i)=='1')
             {
                 cnt++;
                 fixed=fixed+str.charAt(i);
                 System.out.println(fixed);
             }
             else
             {
                 fixed=fixed+str.charAt(i);
                 System.out.println(fixed);
                 cnt=0;
                 
                 
             }
             if(cnt == 5)
             {
                 fixed=fixed+"0";
                 System.out.println(fixed);
                 cnt=0;
                 
             }
         }
         
         
       
         return fixed;
     
     
         
     }
     
     public static String bit_deStuffing1 (String str)
     {
          int i,j,k;
       
         String fixed = "";
         int l = str.length();
     
      int cnt = 0;
         for(i=0;i<l;i++)
         {
             if(str.charAt(i)=='1')
             {
                 cnt++;
                 fixed=fixed+str.charAt(i);
                // System.out.println(fixed);
             }
             else
             {
                 fixed=fixed+str.charAt(i);
                // System.out.println(fixed);
                 cnt=0;
                 
                 
             }
             if(cnt == 5)
             {
                 if((i+2)!=l)
                 {
                     fixed=fixed+str.charAt(i+2);
                     
                 }
                 else
                 {
                     fixed=fixed+'1';
                     
                 }
                 i=i+2;
                 cnt=1;
                 
             }
         }
         
         
         
        
         return fixed;
     
     
         
         
     }
     
     public static String fileBits(byte b)
     {
         String tmp="";
        
         int val = b;
         for(int i=0;i<8;i++)
         {
            
             if((val & 128) == 0) 
             {
                 tmp = tmp+"0";
             }
             else
             {
                 tmp = tmp+"1";
             }
             val <<= 1;
         }
         return tmp;
         
     }
     
     
     
      
      
      
      
     
      
      public static String errIntroduce(String str)
      {
          
          StringBuilder sb = new StringBuilder(str);
          
          if(str.charAt(0) == '0')
          {
          
          sb.setCharAt(0, '1');
          }
          else
              sb.setCharAt(0, '0');
          
          return sb.toString();
          
          
      }
      
      
      
       public void writeFile_try_2()
    {
        try {
            
            
            
            int MAX = 500000;
            int curr = 0;
            
            
           
           
           DataInputStream clientData = new DataInputStream(cSocket.getInputStream());
            
            
            BufferedReader bf = new BufferedReader(new InputStreamReader(cSocket.getInputStream()));
            
            
            
            
            ps = new PrintStream(cSocket.getOutputStream());
            
            
            
          
          
          
            String fname ="";
          
            fname=bf.readLine();
           
            System.out.println("fname"+fname+" got");
          
          
           
            
            System.out.println("hi");

           
            OutputStream output = new FileOutputStream(("new"+fname));
           
           
            System.out.println("here");
            long size;// = 0;
            size = Long.parseLong(bf.readLine().trim());
                    
           
            
            System.out.println("here");
            
            long filesize = size;
            System.out.println("size "+size);
            
            size = filesize+((2+2+1+1)*(filesize/2));
            
            
            
           int chunk = 2;
           byte buf1[] = new byte[chunk+2+2+1+1];
            
            
            long bts=0;
            
            size = filesize;
            
            
          
            System.out.println(buf1.length);
            
            String snd="";
            int c = 1;
            //byte b;
            int bytesRead=0;
            int index = 1;
            
            String pl = "";
            
           
            
            
            long total=0;
            
            
           byte []buff = null;
           
           
           
            String flag = "01111110";
            String k = "";
            String seqNo ="";
            String ackNo="";
           
           
           // String ans = "";
           
            while(total < size)
            {
                int btsize=0;
                String ans = "";
                int cnt = 0;
               while(ans.equals("") || (ans.charAt(31) != '1'))
               {
                
                System.out.println("Here comes");
                 
            
          
           ps.println("Write the integer");
         //   {
             btsize = clientData.readInt();//Integer.parseInt(bytesize);
             
             
            
        
           
           
            System.out.println(btsize);
          //  }
            
               buff = new byte[btsize];
               
              
               
               
              
            
             
               
                bytesRead= clientData.read(buff,0,btsize);//,0,btsize);
               
               //clientData.read(buff);
                
                System.out.println(bytesRead);
               
                System.out.println("loop starts");
                
                
              //  System.out.println(bf.readLine());
               
                
                
                
                String test="";
                
                
                
                 for(int i=0;i<bytesRead;i++)
                {
                    System.out.println("string loop");
                    
                  
                   
                    String try1 = fileBits(buff[i]);
                    
                  
                   
                    test = test + try1;
                    
                    System.out.println(test);
                    
                   // payload=payload+fileBits(bt[i]);
                   // cont = cont + fileBits(bt[i]);
                }
              
              
              
            String st="";
            
           int z = clientData.readInt();
           // st = clientData.readUTF();//bf.readLine();
          
           // System.out.println("hello"+st);
            
            System.out.println(test.length());
            
            String second = "";
            
            if((z == 0) || (z == 8))
            {
            
            second = test.substring(8, test.length()-8-(z%8));
            
            }
            else
            {
                second = test.substring(8, test.length()-8-(8-z%8));
            }
            
            System.out.println(second.length());
            
            String test1 = bit_deStuffing1(second);
            
            System.out.println(test1.length());
            
            
            
            
            
            
            System.out.println(test1);
            
          //  pl = test1.substring(32, 48);
            
          //  System.out.println(pl);
          
         
           
           pl = test1.substring(24,test1.length()-8);  //actual payload
           
           String errpl = errIntroduce(pl);
           
           
           
           System.out.println("Error payload "+errpl);
           
           
           System.out.println("payload "+pl);
           
          
          
          String sum = getChksum(pl);//.substring(0,test1.length()-8));
           
           System.out.println(sum);
           
           
           
          
           String gotsum = test1.substring(24+pl.length(),test1.length());
           
           System.out.println(gotsum);
           
          // String ans = "";
          // String flag= "01111110";
           
           if(sum.equals(gotsum))
           {
              // cnt++;
               System.out.println("Checksum matched");
               
               String retack = test1.substring(0,test1.length()-8-pl.length());
               
               StringBuilder sb = new StringBuilder(retack);
               
              // retack.charAt(8-1) = '0';
                       
               
               
               
               System.out.println(retack);
               
               
               
               k = Integer.toBinaryString(0);//(k1);//Integer.toBinaryString(1);
                
                int lk=k.length();
                for(int s=0;s<(8-lk);s++)
                {
                  k = "0"+k;  
                //  System.out.println(k);
                }
               
                
                
                
                seqNo=Integer.toBinaryString(cnt);
                int ls=seqNo.length();
                for(int s=0;s<(8-ls);s++)
                {
                  seqNo= "0"+seqNo;  
                 // System.out.println(seqNo);
                }
               
                ackNo = Integer.toBinaryString(1);
                int la=ackNo.length();
                for(int s=0;s<(8-la);s++)
                {
                  ackNo = "0"+ackNo;  
                 // System.out.println(ackNo);
                  
                 
                }
                
                ans = flag+k+seqNo+ackNo+flag;
                System.out.println(ans);
                
                
                
                
                
               
               
               
               
               
               
           }
           
           else
           {
               //cnt++;
               
               
               k = Integer.toBinaryString(0);//(k1);//Integer.toBinaryString(1);
                
                int lk=k.length();
                for(int s=0;s<(8-lk);s++)
                {
                  k = "0"+k;  
                //  System.out.println(k);
                }
               
                
                
                
                seqNo=Integer.toBinaryString(cnt);
                int ls=seqNo.length();
                for(int s=0;s<(8-ls);s++)
                {
                  seqNo= "0"+seqNo;  
                 // System.out.println(seqNo);
                }
               
                ackNo = Integer.toBinaryString(0);
                int la=ackNo.length();
                for(int s=0;s<(8-la);s++)
                {
                  ackNo = "0"+ackNo;  
                 // System.out.println(ackNo);
                  
                 
                }
                
                ans = flag+k+seqNo+ackNo+flag;
                System.out.println(ans);
                
                cnt++;
               
           }
           
           ps.println(ans);
           
               }
           
           
           
           
          
          
            
            
            byte []array = new byte[(pl.length()+8-1)/8];
                
                int lpl = pl.length();
                
                for(int no=0;no<lpl;no++)
                {
                    
                    if(pl.charAt(no) == '1')
                    {
                        
                        array[no/8] = (byte) ( array[no/8] | (0x80 >>> (no % 8)));
                        
                    }
                    
                    
                }
                
           
            
           
           
           
           output.write(array);
           output.flush();
           
           int plsize = array.length;
           
           
           total = total + plsize;
            
            
            
          //  output.flush();
            
         
                
              
                System.out.println("input read");
               // total = total + bytesRead;
               
                System.out.println("total"+total+"size"+size+"read"+bytesRead);
                
                
                
               // output.write(buff, 0, bytesRead);
               
              //  output.flush();
                System.out.println("output written");
                
              
           
                System.out.println(btsize+" "+size);
                
                System.out.println(c+" chunks received successfully" );
                
              //  ps.println(c+" chunks received successfully");
              //  ps.flush();
              
               index++;
              
                c++;
               
           
           // }
            }
             
             
           
        
            System.out.println("chunks received");
            System.out.println("hi");

           // output.close();
          //  clientData.close();
           // bf.close();

            System.out.println("File  "+fname+"received from client");//...receiver is "+receiver);
          
        } catch (IOException ex) {
          
           ex.printStackTrace();
        }
        
    }
       
       
      
       
      
     
     
      
     
     
     
            
}