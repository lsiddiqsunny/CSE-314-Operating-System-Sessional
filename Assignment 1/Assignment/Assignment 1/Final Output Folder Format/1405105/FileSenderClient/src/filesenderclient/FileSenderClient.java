/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filesenderclient;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Scanner;

/**
 *
 * @author hp
 */
public class FileSenderClient {
    
    final static int ServerPort = 1234;
    static int size;
    static byte createbyte(String a) {
        byte b=0,p=1;
        int i;
        for (i=a.length()-1;i>=0;i--) {
            if (a.charAt(i)=='1') {
                b+=p;
            }
            p*=2;
        }
        if (b>=128) {
            b=(byte) ((byte) 127-b);
        }
     //   System.out.println("creating "+b);
        return b;
    }
    
    static byte[] fileconverter(String str) {
        int i,j=0;
        String a;
        byte[] sendbuffer=new byte[10000000];
        for(i=0;i<str.length();i=i+8) {
            a=str.substring(i,i+8);
              //  a=Integer.parseInt(str.substring(i,i+8));
            sendbuffer[j]=createbyte(a);
           //     System.out.println(sendbuffer[j]);
            j++;
            }
        return sendbuffer;
    }
    
    public static byte[] bitstuff(byte buffer[],int index,int count) { 
        String ind,mainstring;
        int i;
        size=0;
     //   System.out.println("");
        ind=String.format("%8s", Integer.toBinaryString(index & 0xff)).replace(' ','0');
        String bufstring="";
        for (i=0;i<count;i++) {
            String s1 = String.format("%8s", Integer.toBinaryString(buffer[i] & 0xff)).replace(' ','0');
            bufstring=bufstring+s1;
        }
        //        for (int k=0;k<count;k++) {System.out.print(buffer[k]+" ");}
    //    System.out.println(bufstring);
        mainstring=ind+bufstring+Checksum(buffer,count);
        String temp=mainstring.replaceAll("011111","0111110");
        temp="01111110"+temp+"01111110";
    //    System.out.println("checksum: "+checks(buffer)+" ind " + ind);
        int ssize=temp.length();
       // System.out.println(size);
        while (ssize%8!=0) {
            temp+="0";
            ssize++;
             }
     //   System.out.println(":( "+temp);
        size=ssize/8;
        byte[] sendbuffer=fileconverter(temp);
    //    for (int k=0;k<size;k++) {System.out.print(sendbuffer[k]+" ");}
        return sendbuffer;
    }
    
    
    public static String Checksum (byte[] buf,int n) {
        byte count=0x00;
        int i;        
        for (i=0;i<n;i++) {
            count=(byte) (count^buf[i]);
        }
       String s=String.format("%8s", Integer.toBinaryString(count & 0xff)).replace(' ','0');
       //System.out.println("checksum "+s);
       return s;
    }
    
    
    public static void main(String args[]) throws UnknownHostException,IOException 
    {
        try {
        
         Scanner scn = new Scanner(System.in);
         int count=0;
         String temp;
        // getting localhost ip
        InetAddress ip = InetAddress.getByName("localhost");
        Socket s = new Socket(ip,ServerPort);
        DataInputStream dis = new DataInputStream(s.getInputStream());
        DataOutputStream dos = new DataOutputStream(s.getOutputStream());
        // establish the connection
        System.out.println("Enter User ID:");
        while(true) {
            int choice = 0;
            int id = scn.nextInt();
            dos.writeInt(id);
            int receiver_id;
            // If client sends exit,close this connection 
            // and then break from the while loop
            // printing date or time as requested by client
            String received = dis.readUTF();
            System.out.println(received);
        while(true) {
            if (received.equals("Login Successful")) {
                System.out.println("Enter 'send' to send a file,'download' to get received files,'logout' to sign off:");
                String select= scn.next();
              //  System.out.println("yes");
                if (select.equals("send")) {choice=1;}
                else if (select.equals("download")) {choice=2;}
                else if (select.equals("logout")) {choice=3;}
                dos.writeInt(choice);
                if (choice==1) {
                    System.out.println("Enter receiver id:");
                    receiver_id=scn.nextInt();
                    System.out.println("Enter the name of file:");
                    String Filename=scn.next();
                    String fileid=Integer.toString(receiver_id%10000)+Integer.toString(id%10000)+Filename;
                  //  System.out.println("Select the file you want to send: ");
                    File file= new File("C:\\Users\\hp\\Downloads\\"+Filename);  //Mockplus_Setup_v2.3.9.zip
                    System.out.println("Name of The file shared: "+file.getName()+"\nSize of the file shared: "+(int) file.length());
                    dos.writeInt(receiver_id);
                    dos.writeUTF("Name of The file shared: ");
                    dos.writeUTF(file.getName());
                    dos.writeUTF("\nSize of the file shared: ");
                    dos.writeInt((int) file.length());
                    dos.writeUTF(fileid);
                    int chunk=dis.readInt();
                    byte[] buffer = new byte[100];
                   // dos.writeUTF(fileid);
                    String t="";
                    OutputStream out = s.getOutputStream();
                    int index=0,ack;
                    BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
                    while ((count = in.read(buffer)) > 0) {
                        try {
                        while(true) {
                            index++;
                            for (int i=0;i<count;i++) {
                          //  System.out.print(buffer[i]+" ");
                            }
                            
                            System.out.println("\n");
                            byte[] b=bitstuff(buffer,index,count);
        //    System.out.println(q);
        //          if (size!=0) //{out.write(b,0,count);}
                //  {    
                           out.write(b,0,size);
                  //      }
                //        temp=dis.readUTF();
                //        System.out.println(temp);
                           out.flush();
                           ack=dis.readInt();
                           if (ack==1) {break;}
                            }
                        }

                        catch (Exception x) {
                            x.printStackTrace();
                            }
                    }
                   //     dos.writeUTF(t);
                   //     System.out.println(t);
                }    
                else if (choice==2) {
                    int count2,i=0;
                    dos.writeInt(id);
                    String filename=dis.readUTF();
                    int fsize=dis.readInt();
                 //  String filename=fileidstring.substring(8);
                    InputStream clientin = s.getInputStream();
                    byte clientbuffer[]= new byte[1000];
                    File clientfile=new File("F:\\practice\\"+filename);
                    FileOutputStream fos = new FileOutputStream(clientfile);
                    BufferedOutputStream out = new BufferedOutputStream(fos);
                    while ((count2=clientin.read(clientbuffer))>0 ) {
                        fos.write(clientbuffer,0,count2);
                        i+=1000;
                        System.out.println(count2);
                     //   System.out.println("chunk received");
                       // System.out.println("Chunk #"+index+" received")
                    
                 //       if (dis.readU//TF().equals("request timeout")) {
                 //           Files.deleteIfExists(Paths.get("F:\\Level-3 Term-2\\"+filename));
                 //       }
              //          dos.writeUTF("System time out");
             //           try {
              //          Files.deleteIfExists(Paths.get("F:\\Level-3 Term-2\\"+filename));
                 }
                   // System.out.println("numb");
                   // String sender=dis.readUTF();
                   // System.out.println("File received from "+"5105");
                  
                 
                }
               
                else if (choice==3) {
                    s.close();
                    dis.close();
                    dos.close();
                    break;
                    
                    }
                }
            }
        }
        /* if (s.isClosed()){
                        dos.writeUTF("System time out");
                        Files.deleteIfExists(Paths.get("F:\\Level-3 Term-2\\"+filename));
                        }*/
    //    dis.close();
    //    dos.close();
        }
        catch (Exception e) {
            System.out.println(e);
        }
    }
    
              // sendMessage thread
}
