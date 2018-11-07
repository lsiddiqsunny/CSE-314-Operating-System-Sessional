//package dllclient;


import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.*;
import java.io.PrintStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.imageio.ImageIO;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Soumita
 */
public class DllClient {

    private static Socket sck;
    private static String fileName;
    private static BufferedReader stdin;
    private static BufferedWriter bw;
    private static PrintWriter pw;
    private static PrintStream os;
   
    private static int cid;
    private static Scanner scanner;
    private static OutputStream ot;
   // private static DataInputStream clientData;

    public static void main(String[] args) throws IOException {
        try {
            sck = new Socket("localhost",4997);
            stdin = new BufferedReader(new InputStreamReader(System.in));
            scanner = new Scanner(System.in);
            
           /* System.out.println("Enter your id : ");
        String sid=scanner.nextLine();
        os.println(sid);*/
            
        } catch (Exception e) {
            System.err.println("Cannot connect to the server, try again later.");
            System.exit(1);
        }
  
        ot = sck.getOutputStream();
        bw = new BufferedWriter(new OutputStreamWriter(sck.getOutputStream()));
        os = new PrintStream(sck.getOutputStream());

      
      
        System.out.println("Client");
        
       // while(true)
       // {
      
        
        System.out.println("Enter your id : ");
        String sid=scanner.nextLine();
        os.println(sid);
        
        os.flush();
        System.out.println("You are logged in");
        
         while(true)
        {
        System.out.println("Type \"send\" if you want to send a file or \"rec\" if You want to receive a file" );
        
        
        System.out.println("Operation : ");
        String op=scanner.nextLine();
        os.println(op);
        
        os.flush();
        
      //  if(op != null)
      //  {
            String fn="";
            if(op.equals("send"))
            {
                System.out.println("Enter the filename : ");
                fn = scanner.nextLine();
                os.println(fn);
                
                os.flush();
                
                
               
                
                
                System.out.println("Enter the receiver : ");
                String rid = scanner.nextLine();
                os.println(rid);
                
                os.flush();
                
                readFile_ack();
                
               // send1File();
               
                //receiveFile2(fn);
                
                
            }
            else
            {
                System.out.println("Enter the Filename to receive : ");
                String fr=scanner.nextLine();
                os.println(fr);
                
                os.flush();
                
              //  receiveFile1(fr);
                receiveFile2(fr);
                
               
                
                
            }
            
      //  }
        
         sck.close();  //here

        }
        
       
      
    }

    
    
    
    

    
    
    
     
     
     
     public  static void receiveFile2(String fileName) {
        try {
            
          
            
            System.out.println("hello");
            
            
            
            BufferedReader br=new BufferedReader(new InputStreamReader(sck.getInputStream()));
            
            BufferedInputStream bis = new BufferedInputStream(sck.getInputStream());
            
           DataInputStream clientData = new DataInputStream(bis);
           
           pw = new PrintWriter(sck.getOutputStream(),true);

       
        
         //int l = Integer.parseInt(br.readLine());
         int l = clientData.readInt();
         byte[] buffer = new byte[l];
           System.out.println(l);
            
            FileOutputStream output = new FileOutputStream(("received_from_server_" + fileName));
            BufferedOutputStream bos = new BufferedOutputStream(output);
          
         
         System.out.println(clientData.readUTF());
           
         
            
            
            System.out.println("Byte array declared");
            
            if(clientData.available() > 0)
          {
              System.out.println("True");
          }
          else
          {
              System.out.println("Nothing is in clientData");
          }
            
            
            
            
            
            if(clientData.available() > 0)
          {
              System.out.println("True");
          }
          else
          {
              System.out.println("Nothing is in clientData");
          }
            
            
           // long size = Long.parseLong(br.readLine().trim());
           
           long size = clientData.readLong();
            int s = (int)size;
            
           System.out.println("size "+size);
           
           
           
           
        
          
          if(bis.available() > 0)
          {
              System.out.println("True");
          }
          else
          {
              System.out.println("Nothing is in clientData");
          }
          
          
           
            
        
         int c=1;
         long total=0;
         int bRead;
      
         System.out.println("size"+s);
         
        // os.println();
      
          while (size > 0 && (bRead = clientData.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
                
                System.out.println("loop starts");
                
                
                
                output.write(buffer, 0, bRead);
                System.out.println("output written");
                
             
                
               
                size = size-bRead;
                System.out.println(bRead+" "+size);
            }
         
            System.out.println("File received");

         //   output.close();
           // in.close();
           //clientData.close();
        //   br.close();

            System.out.println("File "+fileName+" received from Server.");
            
            BufferedReader fr = new BufferedReader(new FileReader("received_from_server_"+fileName));
            String line = null;
            while ((line = fr.readLine()) != null) {
            System.out.println(line);
           }
            System.out.println("File "+fileName+" read.");
            fr.close();
            
        } catch (IOException ex) {
            
           System.out.println("Could not receive file");
        }
    }
     
     
     
     
     public static String bitStuffing1 (String str)
     {
         int i,j,k;
       
         String fixed = "";
         int l = str.length();
      int zero = 0;
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
                 fixed=fixed+"0";
                 zero++;
               //  System.out.println(fixed);
                 cnt=0;
                 
             }
         }
         
         
      
         return fixed;
     
     
         
     }
     
     public static int zeroCount (String str)
     {
         int i,j,k;
      
         String fixed = "";
         int l = str.length();
      int zero = 0;
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
                 fixed=fixed+"0";
                 zero++;
               //  System.out.println(fixed);
                 cnt=0;
                 
             }
         }
         
         
       
         return zero;
     
     
         
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
                 if((i+2)!=l)
                 {
                     fixed=fixed+str.charAt(i+2);
                     
                 }
                 else
                 {
                     fixed=fixed+"1";
                     
                 }
                 i=i+2;
                 cnt=1;
                 
             }
         }
         
         
         
         System.out.println(fixed);
        System.out.println(str);
         return fixed;
     
     
         
         
     }
     
     
     public static void readFile_okay()
     {
         try {
             
             
             
             BufferedReader bw = new BufferedReader(new InputStreamReader(sck.getInputStream()));
             
             
             
            System.out.println("Enter file name: ");
            fileName = stdin.readLine();
            String f = fileName;//scanner.nextLine();
           
            os.println(f);
            os.flush();
           
             

            File myFile = new File(f);
            
            System.out.println("Filesize : "+myFile.length());
            
            os.println(myFile.length());
            
          //  os.flush();
            
            
            long fl = myFile.length();//Files.size(myFile.toPath());//myFile.length();
            //os.println((int)fl);
            
            System.out.println("Filesize : "+myFile.length());
            
            
            
            
            int read = 0;
            
           

            FileInputStream fis = new FileInputStream(myFile);
            BufferedInputStream bis = new BufferedInputStream(fis);
            //bis.read(mybytearray, 0, mybytearray.length);

            DataInputStream dis = new DataInputStream(bis);
            
            
            
           // dis.readFully(bt, 0, bt.length);

            OutputStream ost = sck.getOutputStream();

            
            DataOutputStream dos = new DataOutputStream(ost);
            
            
            
            Random rn = new Random();
            int cnk = rn.nextInt(6)+2;
            System.out.println(cnk);
           // ps.println(chunk);
        
           
           
         
          
          
          
            byte bt[] = new byte[cnk];
            int count;
            int last = bt.length;
            long rest=fl;
            int check = 1;
          //  String sg = bw.readLine();
          //  System.out.println(sg);
            String ans ="";
            
            
            
            String mem="";
            
         
            String flag = "";
            String k = "";
            String seqNo ="";
            String ackNo="";
            
           
             
          //  Byte seq1 = null;// = new Byte[1];
          //  Byte ack3 = null;// = new Byte[1];
            
            
            String ack ="";
            String cont ="";
            while((rest > 0) && (count = dis.read(bt,0,(int)Math.min(bt.length, rest))) != -1)
            {
                String payload="";
                flag="01111110";
                
                k = Integer.toBinaryString(1);//(k1);//Integer.toBinaryString(1);
                
                int lk=k.length();
                for(int s=0;s<(8-lk);s++)
                {
                  k = "0"+k;  
                  System.out.println(k);
                }
               // byte []k1 = new byte(1);
              //  byte []k1 = k.getBytes();
             //   k = fileBits(k1[0]);
                
                
                
                seqNo=Integer.toBinaryString(1);
                int ls=seqNo.length();
                for(int s=0;s<(8-ls);s++)
                {
                  seqNo= "0"+seqNo;  
                  System.out.println(seqNo);
                }
               // byte [] seq1 = seqNo.getBytes();
               // seqNo = fileBits(seq1[0]);
                ackNo = Integer.toBinaryString(0);
                int la=ackNo.length();
                for(int s=0;s<(8-la);s++)
                {
                  ackNo = "0"+ackNo;  
                  System.out.println(ackNo);
                }
                
                
                
               // byte []ack3 = ackNo.getBytes();
               // ackNo = fileBits(ack3[0]);
                
                for(int i=0;i<count;i++)
                {
                    System.out.println(fileBits(bt[i]));
                    payload=payload+fileBits(bt[i]);
                    cont = cont + fileBits(bt[i]);
                }
                
                String partial = k+seqNo+ackNo+payload;
                
                String errchk = getChksum(partial);
                
                String bstuff = bitStuffing1(partial+errchk);
                
                int zcnt = zeroCount(partial+errchk);
                
                String fstuff = flag+bstuff+flag;
                
                String full = flag+partial+errchk+flag;
                
               // os.println(full);
                
               // os.flush();
              
                System.out.println("Current Frame is "+fstuff);
               
               
             
                read = read + count;
                System.out.println("read "+read);
                
                
                String fful=full;
              
                
               System.out.println(fstuff); //after stuffing
                
             
                
                
                
                
                byte []b = new byte[(fstuff.length()+8-1)/8];
                
                int lful = fstuff.length();
                
                for(int no=0;no<lful;no++)
                {
                    
                    if(fstuff.charAt(no) == '1')
                    {
                        
                        b[no/8] = (byte) ( b[no/8] | (0x80 >>> (no % 8)));
                        
                    }
                    
                    
                }
                
                
                
                
                System.out.println("Here dos");
                
                
               
              
              System.out.println(bw.readLine());
              
              dos.writeInt(b.length);
              dos.flush();
                
                
                
                
               dos.write(b,0,b.length);//0,b.length);//,0,count);
                
                
                
               // 
               
               
              // os.write(b);
              // os.flush();
               
               
                
               dos.flush();
                
              // dos.writeUTF(fstuff);
              
               // System.out.println(Integer.parseInt(bt));
               
               dos.writeInt(zcnt);
               
                System.out.println("dos written");
                
                dos.flush();
                
              
                
                
              
               
                System.out.println("os written");
                
               // dos.flush();
                
                
                
                for(int see=0;see<b.length;see++)
                {
                    System.out.println(fileBits(b[see]));
                }
                
                
                
              
                
                rest = rest-count;
                System.out.println("read bytes "+read +" "+count +" "+rest);
               // System.out.println(bw.readLine());
               
                check++;
            //    mem = bw.readLine();
            //    System.out.println(mem);
            
            System.out.println(cont);
            
            
            ack = bw.readLine();
            System.out.println(ack);
            
              
                
            }
            System.out.println("File sent successfully");
            
          //  System.out.println(bw.readLine());
           
            System.out.println("File "+f+" sent to Server.");
            
            //ost.close();
         //   }
            
          
          
           
           
        
        } catch (Exception e) {
            System.err.println("File does not exist!");
        }
        
         
     }
     
     
     
     public static String fileBits(byte b)
     {
         String tmp="";
        // StringBuilder binary = new StringBuilder(tmp);
         int val = b;
         for(int i=0;i<8;i++)
         {
            // binary.append((val & 128) == 0 ? 0 : 1);
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
     
     
     public static String getChksum(String s)
     {
         int sum = 0;
         int got=0;
         for(int i=0;i<s.length();i++)
         {
             if(s.charAt(i) == '1')
             {
                 got = Character.getNumericValue(s.charAt(i));
                 //System.out.println(got);
                 sum = sum + got;
                 //System.out.println(sum);
                 
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
         //System.out.println(sgot);
         return sgot;
         
     }
     
     
     public static void readFile_ack()
     {
         try {
             
             
             
             BufferedReader bw = new BufferedReader(new InputStreamReader(sck.getInputStream()));
             
             
             
            System.out.println("Enter file name: ");
            fileName = stdin.readLine();
            String f = fileName;//scanner.nextLine();
           
            os.println(f);
            os.flush();
           
             

            File myFile = new File(f);
            
            System.out.println("Filesize : "+myFile.length());
            
            os.println(myFile.length());
            
          //  os.flush();
            
            
            long fl = myFile.length();//Files.size(myFile.toPath());//myFile.length();
            //os.println((int)fl);
            
            System.out.println("Filesize : "+myFile.length());
            
            
            
            
            int read = 0;
            
           

            FileInputStream fis = new FileInputStream(myFile);
            BufferedInputStream bis = new BufferedInputStream(fis);
            //bis.read(mybytearray, 0, mybytearray.length);

            DataInputStream dis = new DataInputStream(bis);
            
            
            
           // dis.readFully(bt, 0, bt.length);

            OutputStream ost = sck.getOutputStream();

            
            DataOutputStream dos = new DataOutputStream(ost);
            
            
            
            Random rn = new Random();
            int cnk = rn.nextInt(6)+2;
            System.out.println(cnk);
           // ps.println(chunk);
        
           
           
         
          
           // int cnk = 2;//Integer.parseInt(bw.readLine());
           // System.out.println(cnk);
            
            
           
          //  byte bt[]=new byte[10000];
          
            byte bt[] = new byte[cnk];
            int count;
            int last = bt.length;
            long rest=fl;
            int check = 1;
          //  String sg = bw.readLine();
          //  System.out.println(sg);
            String ans ="";
            
            
            
            String mem="";
            
         
            
            String flag = "";
            String k = "";
            String seqNo ="";
            String ackNo="";
            
            // = new Byte[1];
             
          //  Byte seq1 = null;// = new Byte[1];
          //  Byte ack3 = null;// = new Byte[1];
            
            
           // String ack ="";
            String cont ="";
            while((rest > 0) && (count = dis.read(bt,0,(int)Math.min(bt.length, rest))) != -1)
            {
                int seqcnt = 0;
                String ack ="";
                while(ack.equals("") || ((ack.charAt(31) != '1')))
                
                {
                String payload="";
                flag="01111110";
                
                k = Integer.toBinaryString(1);//(k1);//Integer.toBinaryString(1);
                
                int lk=k.length();
                for(int s=0;s<(8-lk);s++)
                {
                  k = "0"+k;  
                  System.out.println(k);
                }
               // byte []k1 = new byte(1);
              //  byte []k1 = k.getBytes();
             //   k = fileBits(k1[0]);
                
                
                
                seqNo=Integer.toBinaryString(seqcnt);
                int ls=seqNo.length();
                for(int s=0;s<(8-ls);s++)
                {
                  seqNo= "0"+seqNo;  
                  System.out.println(seqNo);
                }
               // byte [] seq1 = seqNo.getBytes();
               // seqNo = fileBits(seq1[0]);
                ackNo = Integer.toBinaryString(0);
                int la=ackNo.length();
                for(int s=0;s<(8-la);s++)
                {
                  ackNo = "0"+ackNo;  
                  System.out.println(ackNo);
                }
                
                
                
               // byte []ack3 = ackNo.getBytes();
               // ackNo = fileBits(ack3[0]);
                
                for(int i=0;i<count;i++)
                {
                    System.out.println(fileBits(bt[i]));
                    payload=payload+fileBits(bt[i]);
                    cont = cont + fileBits(bt[i]);
                }
                
                String partial = k+seqNo+ackNo+payload;
                
                String errchk = getChksum(payload);
                
                String bstuff = bitStuffing1(partial+errchk);
                
                int zcnt = zeroCount(partial+errchk);
                
                String fstuff = flag+bstuff+flag;
                
                String full = flag+partial+errchk+flag;
                
               // os.println(full);
                
               // os.flush();
              
                System.out.println("Current Frame is "+fstuff);
               
               
             
               // read = read + count;
               // System.out.println("read "+read);
                
                
                String fful=full;
              /*  for(int c=0;c<(64-full.length());c++)
                {
                    fful="0"+fful;
                }*/
                
               // full = fful;
                
               System.out.println(fstuff); //after stuffing
                
             
                
                
                
                byte []b = new byte[(fstuff.length()+8-1)/8];
                
                int lful = fstuff.length();
                
                for(int no=0;no<lful;no++)
                {
                    
                    if(fstuff.charAt(no) == '1')
                    {
                        
                        b[no/8] = (byte) ( b[no/8] | (0x80 >>> (no % 8)));
                        
                    }
                    
                    
                }
                
                
                
                
                System.out.println("Here dos");
                
                
               
              
              System.out.println(bw.readLine());
              
              dos.writeInt(b.length);
              dos.flush();
                
                
                
              
               dos.write(b,0,b.length);//0,b.length);//,0,count);
               long st = System.nanoTime(); 
                
                
                
               // 
               
               
              // os.write(b);
              // os.flush();
               
               
                
               dos.flush();
                
              // dos.writeUTF(fstuff);
              
               // System.out.println(Integer.parseInt(bt));
               
               dos.writeInt(zcnt);
               
                System.out.println("dos written");
                
                dos.flush();
                
             
               
                
                
              
               
                System.out.println("os written");
                
               // dos.flush();
                
                
                
                for(int see=0;see<b.length;see++)
                {
                    System.out.println(fileBits(b[see]));
                }
                
                
                
              
                
                //rest = rest-count;
                System.out.println("read bytes "+read +" "+count +" "+rest);
               
            
            System.out.println(cont);
            
           
            
            ack = bw.readLine();
            long finish = System.nanoTime(); 
            if((finish-st) >= (500000000))
            {
                 System.out.println("Timeout Error");
            } 
            System.out.println(ack);
            
            
            
                seqcnt++;
            
            
            
                }
                
                
                read = read + count;
                System.out.println("read "+read);
                
                rest = rest-count;
                System.out.println("read bytes "+read +" "+count +" "+rest);
                
                check++;
            
              
                
            }
            System.out.println("File sent successfully");
            
          //  System.out.println(bw.readLine());
           
            System.out.println("File "+f+" sent to Server.");
            
            //ost.close();
         //   }
            
          
          
           
           
        
        } catch (Exception e) {
            System.err.println("File does not exist!");
        }
        
         
     }
     
     
     
     
     
     
     
     
     
     
}


 