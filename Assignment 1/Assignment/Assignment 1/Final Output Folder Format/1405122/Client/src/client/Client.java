/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

/**
 *
 * @author saura

*/

import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

 
public class Client {

     public static  String timestr;
public static void main(String[] args) {
 
    try {
        Socket socket = new Socket("localhost", 9999);
        System.out.println("SERVER :  Client Connected !! " + socket);
        //Socket socket2 = new Socket("localhost", 9999);
        
        InputStream in = socket.getInputStream();
        OutputStream out = socket.getOutputStream();

        DataInputStream din = new DataInputStream(in);
        DataOutputStream dout = new DataOutputStream(out);

        BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));


        System.out.println("Enter ur student id");
        String sid = keyboard.readLine();
        dout.writeUTF(sid);
        dout.flush();
        
        String confirm = din.readUTF();
        if(confirm.equals("NOT"))
        {
            socket.close();
        }
        
        System.out.println("Do u want to send File y/n ?");
        String yn = keyboard.readLine();
        dout.writeUTF(yn);
        dout.flush();
        
        //BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        int maxchunksize =0;
        
  if(yn.equalsIgnoreCase("y"))
     {
            
        
        int x =1;
        while(x==1)
        {
        System.out.println("Enter the receiver's id");
        String rid = keyboard.readLine();
        dout.writeUTF(rid);
        dout.flush();
        
        String rconfirm = din.readUTF();
        if(rconfirm.equals("Error !!! Receiver is offline !!!"))
        {
            System.out.println(rconfirm);
        }
        else
        {
            x = 0;
            break;
        }
        }
        
        System.out.println("Enter the filename");
        String filename = keyboard.readLine();
        //filename = "C:\\Users\\saura\\Downloads\\Documents\\1.pdf";
        dout.writeUTF(filename);
        dout.flush();
        
        File file = new File(filename);
        long filesize = file.length();
        dout.writeLong(filesize);
        dout.flush();
        
        String sendconfirmmsg = din.readUTF();
        System.out.println(sendconfirmmsg);
        
        maxchunksize = din.readInt();
        System.out.println("Max Chunk Size " + maxchunksize);
        
        int fileid = din.readInt();
        System.out.println("File Id " + fileid);
            
        
        try (
                DataInputStream bis = new DataInputStream(new FileInputStream(filename));
                DataInputStream ackbis = new DataInputStream(socket.getInputStream());
                DataOutputStream os = new DataOutputStream(
                                      new BufferedOutputStream(socket.getOutputStream()))) 
                
            
        
            {
            
                int count =0, bytesread, totalbytesread = 0 ;
                String acknw;
                int seqno=0, actualseqno=0;
                int payloadsize = maxchunksize;
                int datakind = 1;
                int header = 0b01111110;
                int dxcnt=0;
                int acknwserver;
                
                byte[] mybytearraydx = new byte[maxchunksize];
                
                while ((bytesread = bis.read(mybytearraydx)) >= 0)
                {
                    
                    actualseqno++;
                    seqno = actualseqno%127;
                    
                    payloadsize = bytesread;
                    System.out.println("FRAME NUM  "+actualseqno+ "   " + seqno);
                    
                    //MAKING BYTE ARRAY HERE
                    
                    //copy how many bytes i actually need to read
                    byte[] mybytearray = new byte [bytesread];
                    for(int i=0; i<bytesread; i++)
                    {
                        mybytearray[i] = mybytearraydx[i];
                    }
                     
                    
                    int checksum=0;
                    for(int i=0; i<mybytearray.length; i++)
                    {
                        checksum = checksum^mybytearray[i];
                    }
                    //System.out.println("checksum  "+ checksum);
                    
                    
                    byte [] array2 = new byte[3+bytesread+1];
                    
                    array2[0] = (byte)datakind;
                    array2[1] = (byte)seqno;
                    array2[2] = (byte)0;
                    int k=0;
                    for(int i=3,j=0; j<bytesread; i++,j++)
                    {
                        array2[i] = mybytearray[j];
                        k=i;
                    }
                    array2[k+1] = (byte)checksum;
                    
                    
                    
                    System.out.print("BEFORE STUFFING  | ");
                    int l=0;
                    
                    while(l<=k+1)
                    {
                        for(int mask = 0x80; mask != 0x00; mask = mask>>1)
                        {
                            boolean bit = (array2[l] & mask) != 0;
                            if(bit)
                                System.out.print(1);
                            else
                                System.out.print(0);
                        }
                        
                        l++;
                        System.out.print(" ");
                    }
                    System.out.println();
                    
                    
                    
                    
                    
                    
                    ////////////////////////////////////////////////
                    ////////////////////////////////////////////////
                    //BIT STUFFING
                    ////////////////////////////////////////////////
                    ////////////////////////////////////////////////
                   
                    String bytestring, bytes="", mainbytes="";
                    for(int i=0; i<array2.length; i++)
                    {
                        bytestring=String.format( "%8s",Integer.toBinaryString(array2[i] & 0xFF)).replace(' ', '0');
                        bytes = bytes + bytestring;
                    }
                    
                    
                    String s, mains = "";
                    int cntone=0, bitcnt =0;
                    for(int i=0; i<bytes.length(); i++)
                    {
                        s = bytes.substring(i, i+1);
                        mains = mains + s;

                        if(s.equals("1"))
                            cntone++;

                        else if(s.equals("0"))
                            cntone=0;

                        if(cntone == 5)
                        {
                            mains = mains + "0";
                            bitcnt++;
                            cntone=0;
                        }

                        bitcnt++;
                    }
                    
                    //System.out.println(bitcnt);
                    
                    int mod = bitcnt%8;

                    if(mod !=0)
                    {
                        for(int i=0; i<8-mod; i++)
                             mains = mains + "0";
                    }
                    //System.out.print(mains);
                    
                    
                    String str;
                    int val;
                    int bytesize = mains.length()/8;
                    
                    byte[] array = new byte[bytesize];


                    for(int i=0, kk=0; i<mains.length(); i=i+8, kk++)
                    {
                        str = mains.substring(i, i+8);
                        val = Integer.parseInt(str, 2);
                        array[kk] = (byte)val;
                    }
                    
                    
                    
                    byte []mybytearray2 = new byte[bytesize+2];
                    mybytearray2[0] = (byte)header;
                    
                    
                    for(int ii=1,j=0; ii<bytesize+1; ii++,j++)
                    {
                        mybytearray2[ii] = array[j];
                    }
                    mybytearray2[bytesize+1] = (byte)header;
                    
                    
                    
                    System.out.print("AFTER STUFFING | ");
                    l=0;
                    while(l<bytesize+2)
                    {
                        for(int mask = 0x80; mask != 0x00; mask = mask>>1)
                        {
                            boolean bit = (mybytearray2[l] & mask) != 0;
                            if(bit)
                                System.out.print(1);
                            else
                                System.out.print(0);
                        }
                        
                        l++;
                        System.out.print(" ");
                    }
                    System.out.println();
                    System.out.println();
                    
                    os.write(mybytearray2);
                    os.flush();
                    
                    
                    
                    /////////////////////////////////////////////
                    /////////////////////////////////////////////
                    // STOP AND WAIT  IMPLEMENTATION
                    /////////////////////////////////////////////
                    
                    
                    
                    
                byte acknwbyte[] = new byte[6];    
                    
                    
                timestr = "";
                TimerTask task = new TimerTask()
                {
                    public void run()
                    {
                        if( timestr.equals("") )
                        {
                            try {
                                os.write(mybytearray2);
                                os.flush();
                            } catch (IOException ex) {
                                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                            }
                           
                        }
                    }
                };
                Timer timer = new Timer();
                timer.schedule( task, 30*1000 );
                
                
                int acknwread = din.read(acknwbyte);
                
                
                byte[] acknwarray3 = new byte [acknwread];
                for(int i=0; i<acknwread; i++)
                {
                    acknwarray3[i] = acknwbyte[i];
                }
                
                
                System.out.print("ACKNOWLEDGEMENT BEFORE DE-STUFFING | ");
               l=0;
               while(l<acknwarray3.length)
               {
                   for(int mask = 0x80; mask != 0x00; mask = mask>>1)
                   {
                       boolean bit = (acknwarray3[l] & mask) != 0;
                       if(bit)
                           System.out.print(1);
                       else
                           System.out.print(0);
                   }

                   l++;
                   System.out.print(" ");
               }

                
                
                
                
                byte[] acknwarray = new byte [acknwread-2];
                for(int i=1,j=0; j<acknwarray.length; i++,j++)
                {
                    acknwarray[j] = acknwarray3[i];
                }
                
                //////////////////////////////////////////////////////
                //////////////////////////////////////////////////////
                // ACKNOWLEDGEMENT DESTUFFING HERE
                //////////////////////////////////////////////////////
                //////////////////////////////////////////////////////
                bytes="";mainbytes="";
                
                for(int i=0; i<acknwarray.length; i++)
                {
                    bytestring=String.format( "%8s",Integer.toBinaryString(acknwarray[i] & 0xFF)).replace(' ', '0');
                    bytes = bytes + bytestring;
                }
        
        
                mains = "";
                cntone=0;
                bitcnt =0;
                for(int i=0; i<bytes.length(); i++)
                {
                    s = bytes.substring(i, i+1);
                    mains = mains + s;

                    if(s.equals("1"))
                        cntone++;

                    else if(s.equals("0"))
                        cntone=0;

                    if(cntone == 5)
                    {
                        i++;
                        cntone=0;
                    }

                    bitcnt++;
                }
                bitcnt = bitcnt/8;

                

               byte [] acknwarray2 = new byte[bitcnt];
               
               for(int i=0, kk=0; i<bitcnt*8; i=i+8, kk++)
               {
                   str = mains.substring(i, i+8);
                   val = Integer.parseInt(str, 2);
                   acknwarray2[kk] = (byte)val;
               }


               System.out.println();
               
               System.out.print("ACKNOWLEDGEMENT AFTER DE-STUFFING | ");
               l=0;
               while(l<bitcnt)
               {
                   for(int mask = 0x80; mask != 0x00; mask = mask>>1)
                   {
                       boolean bit = (acknwarray2[l] & mask) != 0;
                       if(bit)
                           System.out.print(1);
                       else
                           System.out.print(0);
                   }

                   l++;
                   System.out.print(" ");
               }

                
                
                ///////////////////////////////////////////
                
                timestr = "Acknowledgment";
                timer.cancel();
                //System.out.println( "A chunk has been sent and Acknowldgemnt recieved");

                acknwserver = (int)acknwarray2[2];
                
                
                
                //IF SEQ NO AND ACKNW DOESNT MATCH
                if(seqno != acknwserver)
                {
                    System.out.println( "ERROR SEQ AND ACKNW DOESNT MATCH");
                    os.write(mybytearray2);
                    os.flush();
                }
                    

                System.out.println();
                System.out.println();
                System.out.println();
                    count++;
                    totalbytesread += bytesread;
                    
                    
                } // MAIN WHILE ENDS HERE
                os.flush();
                
                //dout.writeUTF("");
                //dout.flush();
                //System.out.println("count " +count);
                String ack = din.readUTF();
                System.out.println("SERVER : " + ack);
                dout.writeUTF("Confirmation message");
                dout.flush();
                String sucess = din.readUTF();
                System.out.println("SERVER : " + sucess);
                os.close();
            }
        
        
        
     }   //YN ends here
        
  
  
        //FILE WILL BE RECEIVED FROM THE SERVER NOW
        //
        //
        //
        //
        //
        //
        //
        //
      
  else if(yn.equalsIgnoreCase("n"))
  {
      
        String recyn = din.readUTF();
        System.out.println(recyn);
        
         Scanner sc = new Scanner(System.in);
         System.out.println("Enter y/n");
         String confirmrec = sc.nextLine();
         
         dout.writeUTF(confirmrec);
        dout.flush();
      
    if(confirmrec.equalsIgnoreCase("y"))
        {      
        byte[] receivedData = new byte[1024];
        InputStream rbis = socket.getInputStream();
        String filepath = "C:\\Users\\saura\\Downloads\\Client\\MPTOH_"+sid+".pdf";
        File file = new File(filepath);
        FileOutputStream fos = new FileOutputStream(file);
        BufferedOutputStream rbos = new BufferedOutputStream(fos);
        
        int count=0, dataread, currentbytesread =0;
        while ((dataread = rbis.read(receivedData)) >= 0)
        {
            rbos.write(receivedData,0,dataread);
            
            currentbytesread += dataread;
            count++;
            
            if(dataread != 1024)
            {
                break;
            }
            
        }
        rbos.flush();
    }  } 
        
    } catch (Exception e) {
    }
}
}