/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

/**
 *
 * @author saura
 */
import java.net.*;
import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
     public static HashMap<String, Socket> hm;
     public static HashMap<String, Users> usershm;
     public static int fileid, totalbytesread;
     public static long filesize;
     public static String rid, recyn,sid, filename;
     public final static int MAX_SIZE = 20000*1024;
    public void Serverstart(){
        try {
            hm = new HashMap<String, Socket>();
            usershm = new HashMap<String, Users>();
            fileid =0;
            System.out.println("SERVER :  Server Started");
            ServerSocket ss = new ServerSocket(9999);
            System.out.println("SERVER :  Waiting for client");

            while(true){
                Socket socket = ss.accept();
                System.out.println("SERVER :  Client Connected !! " + socket);
                
                
                
                SSocket sSocket = new SSocket(socket);
                Thread t = new Thread(sSocket);
                t.start();
            }
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


public static void main(String[] args) {
    Server s = new Server();
    s.Serverstart();
    
}
}

class SSocket implements Runnable {

private Socket socket;

public SSocket(Socket socket) {
    this.socket = socket;

}

@Override
public void run() {
    try {
        InputStream in = socket.getInputStream();
        OutputStream out = socket.getOutputStream();

        DataInputStream dIn = new DataInputStream(in);
        DataOutputStream dOut = new DataOutputStream(out);
        
        Server.sid = dIn.readUTF();
        System.out.println("CLIENT : student id : " + Server.sid);
        
        String test;
        if(Server.hm.containsKey(Server.sid)){
            socket.close();
            test = "NOT";
            System.out.println("SERVER :  Access Denied !!!!!!!!!!!!!!!");
        }
        else
        {
            test = "OK";
            Server.hm.put(Server.sid, socket);
        }
        
        dOut.writeUTF(test);
        dOut.flush();
        
        Server.recyn = dIn.readUTF();
        System.out.println("CLIENT :  Sender wants to send file ?? : " + Server.recyn);
        
       
        
       
        if(Server.recyn.equalsIgnoreCase("y")){     //Senders WORK has Started here
        //
            //
            //
            //
            //
            //
            //
            //
            //
        
            
        while(true)
        {
        Server.rid = dIn.readUTF();
        System.out.println("CLIENT :  reciever id : " + Server.rid);
        
        if(Server.hm.containsKey(Server.rid)){
           dOut.writeUTF("Receiver is online");
           dOut.flush(); 
           break;
        }
        
        else
        {
           dOut.writeUTF("Error !!! Receiver is offline !!!");
           dOut.flush(); 
        }
        }
        
        Server.filename = dIn.readUTF();
        System.out.println("CLIENT :   filename : " + Server.filename);
        
        Server.filesize = dIn.readLong();
        System.out.println("CLIENT :  filesize : " + Server.filesize);
        Server.filesize = (int)Server.filesize;
        
        if((Server.totalbytesread + Server.filesize) > Server.MAX_SIZE)
        {
            System.out.println("SERVER :  ERROR OVERFLOW !!!! ");
            socket.close();
        }
        
        


        //Generate random values here
        Random rand = new Random();
        int  n = rand.nextInt(10) + 1;
        
        dOut.writeUTF("You can now send the file");
        dOut.flush(); 
        
       // n = (int)Server.filesize/125;
        n = 256;
        dOut.writeInt(n);
        dOut.flush(); 
        
        Server.fileid++;
        dOut.writeInt(Server.fileid);
        dOut.flush(); 
        
        
        Users u = new Users(Server.fileid,Server.filename, Server.rid, Server.hm.get(Server.rid));
        Server.usershm.put(Server.sid, u);
        
        int maxbyte = 2*n;
        byte[] receivedData = new byte[maxbyte];
        //InputStream bis = socket.getInputStream();
        String filepath = "C:\\Users\\saura\\Downloads\\Server\\MPTOH_"+Server.sid+"_"+Server.rid+".pdf";
        File file = new File(filepath);
        FileOutputStream fos = new FileOutputStream(file);
        DataOutputStream ackbos = new DataOutputStream(
                                      new BufferedOutputStream(socket.getOutputStream())); 
        //BufferedOutputStream bos = new BufferedOutputStream(fos);
        try (DataInputStream bis = new DataInputStream(socket.getInputStream())) {
            DataOutputStream bos = new DataOutputStream(
                    new BufferedOutputStream(fos));
            int count=0, dataread=0, currentbytesread =0;
            int dxcnt=0, acknwcnt=0;    
            int seqnoclient;
             
            System.out.println();
            
            while ((dataread = bis.read(receivedData)) > 0 && receivedData[0] !=0)
            {
                System.out.println("FRAME NUM  "+ ++dxcnt + "   " + (dxcnt)%127);
                
                System.out.print("BEFORE DE-STUFFING  | ");
                    int l=0;
                    while(l<dataread)
                    {
                        for(int mask = 0x80; mask != 0x00; mask = mask>>1)
                        {
                            boolean bit = (receivedData[l] & mask) != 0;
                            if(bit)
                                System.out.print(1);
                            else
                                System.out.print(0);
                        }
                        
                        l++;
                        System.out.print(" ");
                    }
                    
                    System.out.println();
                
                
                
                byte[] for_destuffing = new byte[dataread-2];
                
                for(int k=1; k<dataread-1; k++)
                {
                    for_destuffing[k-1] = receivedData[k];
                }
                
                ////////////////////////////////////////////////////////
                ////////////////////////////////////////////////////////
                // destuffing here
                ////////////////////////////////////////////////////////
                ////////////////////////////////////////////////////////
                
                String bytes="", mainbytes="", bytestring;
                
                for(int i=0; i<for_destuffing.length; i++)
                {
                    bytestring=String.format( "%8s",Integer.toBinaryString(for_destuffing[i] & 0xFF)).replace(' ', '0');
                    bytes = bytes + bytestring;
                }
        
        
                String mains = "",s;
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
                        i++;
                        cntone=0;
                    }

                    bitcnt++;
                }
                bitcnt = bitcnt/8;

                
               String str;
               int val;
               byte [] array2 = new byte[bitcnt];
               for(int i=0, k=0; i<bitcnt*8; i=i+8, k++)
               {
                   str = mains.substring(i, i+8);
                   val = Integer.parseInt(str, 2);
                   array2[k] = (byte)val;
               }


               l=0;
               System.out.print("AFTER DE-STUFFING  | ");
               while(l<bitcnt)
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

                
                byte[] destuffedfinal2 = new byte[bitcnt-4];
                
                for(int k=3, q=0; k<bitcnt-1; k++,q++)
                {
                    destuffedfinal2[q] = array2[k];
                }
                
                
                /////////////////////////////////////////////////
                ////////////////////////////////////////////////
                ///////CHECKSUM CALCULATION AND VERIFICATION
                /////////////////////////////////////////////////
                /////////////////////////////////////////////////
                
                
                
                
                int checksumserver=0;
                for(int k=0; k<destuffedfinal2.length; k++)
                {
                        checksumserver = checksumserver^destuffedfinal2[k];
                }
                
                
                int checksum;
                checksum = array2[bitcnt-1];
                
                System.out.println();
                System.out.println("CHECKSUM CLIENT  " + checksum + "   CHECKSUM SERVER " + checksumserver);
                
                
                seqnoclient = (int)array2[1];
                
                
                if(seqnoclient == (count+1)%127 && checksum == checksumserver)
                {
                    bos.write(destuffedfinal2);
                    System.out.println("FRAME SUCCESSFULLY ACCEPTED !!");
                }
                    
                
                else
                {
                    count--;
                    if(checksum != checksumserver)
                        System.out.println("ERROR!!!!!!!!!!!! CHECKSUM ERROR");
                    System.out.println("ERROR !!!!!!!!!!!!!1 SENDING SAME ACKNOWLEDGMENT AGAIN");
                }
                    
                
                
                
                
                
                /////////////////////////////
                
                
                
                
                
                
                System.out.println();
                
                
                
                byte acknwbyte[] = new byte[3];
                count++;
                acknwcnt = (count)%127;
                //acknwbyte[0] = (byte)0b01111110;
                acknwbyte[0] = (byte)0;
                acknwbyte[1] = (byte)0;
                acknwbyte[2] = (byte)acknwcnt;
                
                
                
                
                //////////////////////////////////////////////////////////////
                //////////////////////////////////////////////////////////////
                //BITSTUFFING HERE FOR ACKNOWLEDGEMENT
                /////////////////////////////////////////////////////////////
                /////////////////////////////////////////////////////////////
                
                System.out.print("ACKNOWLEDGEMENT BEFORE STUFFING | ");
                l=0;
                while(l<acknwbyte.length)
                {
                    for(int mask = 0x80; mask != 0x00; mask = mask>>1)
                    {
                        boolean bit = (acknwbyte[l] & mask) != 0;
                        if(bit)
                            System.out.print(1);
                        else
                            System.out.print(0);
                    }

                    l++;
                    System.out.print(" ");
                }
                System.out.println();
                
                
                
                
                
                bytes=""; mainbytes="";
                for(int i=0; i<acknwbyte.length; i++)
                {
                    bytestring=String.format( "%8s",Integer.toBinaryString(acknwbyte[i] & 0xFF)).replace(' ', '0');
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
                
                int bytesize = mains.length()/8;
                byte[] array = new byte[bytesize+2];

                array[0] = (byte)0b01111110;
                array[array.length-1] = (byte)0b01111110;
                
                for(int i=0, kk=1; i<mains.length(); i=i+8, kk++)
                {
                    str = mains.substring(i, i+8);
                    val = Integer.parseInt(str, 2);
                    array[kk] = (byte)val;
                }

                
                
                System.out.print("ACKNOWLEDGEMENT AFTER STUFFING | ");
                l=0;
                while(l<array.length)
                {
                    for(int mask = 0x80; mask != 0x00; mask = mask>>1)
                    {
                        boolean bit = (array[l] & mask) != 0;
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
                System.out.println();

                
                dOut.write(array);
                //dOut.writeUTF("Acknowledgement "+ ++count);
                dOut.flush();

                currentbytesread += dataread;

                if(dataread < n)
                {
                   // System.out.println("entered break");
                    break;
                }

            } 
                
                
                
                
                
                ackbos.flush();
                bos.flush();
                Server.totalbytesread += currentbytesread;
                dOut.writeUTF("Acknowledgement for last Chunk !!!!!");
                dOut.flush();
                String confr = dIn.readUTF();
                System.out.println("CLIENT :" + confr);
                //if(currentbytesread == (int)Server.filesize)
                
                dOut.writeUTF("Success Message");
                dOut.flush();
                
                     
                bos.close();
                ackbos.close();
            }
        System.out.println(" Done ");
        
        }  //YN ends here
        //FIlE SENDING TO SERVER FINISH
        //NOW SERVER WILL SEND FILE TO THE RECEIVER
        //
        //
        //
        //
        //
        //
        //
        //
        
if(Server.recyn.equalsIgnoreCase("n")){
        
        System.out.println("Receiver ID: " + Server.fileid);    
        socket = Server.hm.get(Server.rid);
        System.out.println("Receiver SOCKET: " + socket);
        
        
        
        InputStream rin = socket.getInputStream();
        OutputStream rout = socket.getOutputStream();

        DataInputStream rdIn = new DataInputStream(rin);
        DataOutputStream rdOut = new DataOutputStream(rout);
        
        
        rdOut.writeUTF("Do u want to receive file filename = " + Server.filename + " of filesize = " + Server.filesize + " from studnet id" + Server.sid);
        rdOut.flush();
        
        
        String conreceive = dIn.readUTF();
        System.out.println("CLIENT : confirm recieve: " + conreceive);
        
        
if(conreceive.equalsIgnoreCase("y"))
{            
        
        
       
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream("C:\\Users\\saura\\Downloads\\Server\\MPTOH_"+Server.sid+"_"+Server.rid+".pdf"));
        BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream( ));
        byte[] byteArray = new byte[1024];
        
        int count =0, bytesread, totalbytesread =0;
        String acknw;
        while ((bytesread = bis.read(byteArray)) >= 0)
        {
            bos.write(byteArray,0,bytesread);
            
            count++;
            totalbytesread += bytesread;
        }
        
        bos.flush();

        bos.flush();
        
        bis.close();
        bos.close();
        System.out.println("File Successfully Sent by Server" );
}      
        //
        
        }   

    } catch (Exception e) {
    }
}
}

class Users {
    public int fileid;
    public String fileName; 
    public String receiverid; 
    public Socket socket;
    
    public Users(int fileid,String fileName, String receiverid, Socket recsocket)
    {
        this.fileid = fileid;
        this.fileName = fileName;
        this.receiverid = receiverid;
        this.socket = recsocket;
    }
}