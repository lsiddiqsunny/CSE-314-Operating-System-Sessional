/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filesenderserver;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;


/**
 *
 * @author hp
 */

public class WorkThread extends Thread {
    Scanner scn = new Scanner(System.in);
    private DataInputStream dis;
    private DataOutputStream dos;
    Socket s;
    public static HashMap<Integer,Integer> activeuser = new HashMap<>();
    public static HashMap<String,File> savedfile= new HashMap<>();
    public static HashMap <Integer,ArrayList<Integer>> filesenders= new HashMap<>();
    //1405091-1405122 is the range
     
    
    public WorkThread(Socket s,DataInputStream dis, DataOutputStream dos) {
        this.dis = dis;
        this.dos = dos;
        this.s = s;
        
    }
    
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
    
    static byte[] converter(String str) {
        int i,j=0;
        String a;
        byte[] sendbuffer=new byte[1000000];
        for(i=0;i<str.length();i=i+8) {
          //      System.out.println(str.substring(i,i+8));
                a=str.substring(i,i+8);
                sendbuffer[j]=createbyte(a);
            //    System.out.println(sendbuffer[j]);
                j++;
            }
        return sendbuffer;
    }
    
    public static byte Checksum (byte[] buf) {
        byte count=0x00;
        int i;
        
        for (i=0;buf[i]!=0;i++) {
            count=(byte) (count^buf[i]);
        }
       return count;
    }
    
    public byte[] bitdestuffing(byte[] receivedByte,int count) {
    //    for (int i=0;i<count;i++) {
    //        System.out.print(receivedByte[i]+" ");
    //    }
        String temp = "",temp1;
        int j=0,i;
        for (i=0;i<count;i++) {
            String s1 = String.format("%8s", Integer.toBinaryString(receivedByte[i] & 0xff)).replace(' ','0');
            temp=temp+s1;
//            System.out.println(temp);
        }
    //    System.out.println("");
    //    System.out.println(temp);
        String filedata;
        byte check,index;
        filedata=temp.replaceAll("0111110","011111");
        int restzeroes=(filedata.length())%8;
        temp1=filedata.substring(0,filedata.length()-restzeroes);
    //    System.out.println("after processing "+temp1);
        byte[] filebuf=converter(temp1);
 //       System.out.println("");
        return filebuf;
    }
    
    
    String rec;
    int received;
    static int size1,size2;
    
    @Override
    public void run() {
        for (int k=1405091;k<=1405122;k++) {
            activeuser.put(k,0);
        }
        while (true) 
        {
            try
            {
                //rec= dis.read();
             //   System.out.println(rec);
                received= dis.readInt();
                System.out.println(received);
          //      System.out.println(received);
                
                if (received<1405091 || received >1405122 ) {
                    dos.writeUTF("invalid User ID"+"\n");
                    }
                else if (activeuser.get(received)==1)  {
                    dos.writeUTF("Already Logged in"+"\n");
                    }
                
                else {
                    dos.writeUTF("Login Successful");
                    int choice1=dis.readInt();
                   // System.out.println(choice1);
                    activeuser.put(received,1);
                    if (choice1==1) {
                  //  System.out.println(activeuser.get(received).toString());
                    int receiver_id=dis.readInt();
                    System.out.println(receiver_id);
                    String temp1=dis.readUTF();
                    String filename=dis.readUTF();
                    String temp2=dis.readUTF();
                    int sizeoffile=dis.readInt();
                    String fileid;
                    fileid=dis.readUTF();
                //    System.out.println(fileid);
                //    StringTokenizer st = new StringTokenizer(filename,".");
                //    String filename1 = st.nextToken();
                //    String filename2= st.nextToken();
                //    filename2="."+filename2;
                //    System.out.println(filename1+filename2);
                    File file= new File("H:\\1405105\\"+filename);
//                    File file=File.createTempFile(filename1,filename2);
                    savedfile.put(fileid, file);
                //    System.out.println(file.getName());
                    int chunk=200;//(int) (sizeoffile/200+(Math.random())%(sizeoffile/40-sizeoffile/200));
                    dos.writeInt(chunk);
                    //System.out.println(filename+sizeoffile);                    
                    //dos.writeUTF(activeuser.get(received).toString());
                    //fileid=dis.readUTF();
               //     FileOutputStream fos = new FileOutputStream("F:\\Level-3 Term-2\\"+filename);
               //     System.out.println(chunk);
                    byte[] buffer = new byte[200];
                    int count,index=1;
                  //  file=(savedfile.get(fileid));
                    FileOutputStream fos = new FileOutputStream(file);
                    BufferedOutputStream out = new BufferedOutputStream(fos);
                    String t;
                    int i;
                    InputStream in = s.getInputStream();
                    int ack;
             
                while ((count=in.read(buffer))>0) {
                   try {
                   //     if (count>1) {
                        int j=0,p=0;
                        System.out.println("\nsize  "+count);
                        for (i=0;i<count;i++) {System.out.print(buffer[i]+" ");}
                        System.out.println("");
                        byte[] b=bitdestuffing(buffer,count);
                        for (i=0;i<size1;i++) {System.out.print(b[i]+" ");}
                    //    System.out.println("");
                        byte[] newb = new byte[chunk];
                        byte ind,check;
                        for (i=1;b[i+1]!=126;i++) {
                            if (i==1) {ind=b[i];}
                            else {
                                newb[p]=b[i];
                                p++;
                            }
                       }
                  //      System.out.println(b[i]);

                    //    for (i=0;i<p;i++) {System.out.print(newb[i]+" ");}
                        if (Checksum(newb)==b[i]) {
                            ack=1;
                            fos.write(newb,0,p+1);
                        }
                        else {ack=0;}
                 //       System.out.println("ack: "+ack+" "+Checksum(newb));
                        dos.writeInt(ack);
                     //  }
                       // System.out.println("Chunk #"+index+" received");
              //          dos.writeUTF("Chunk #"+index+" sent successfully");
               //         System.out.println(file.length());
               //         index++;
              //          dos.writeUTF("System time out");
              //          Files.deleteIfExists(savedfile.get(fileid));
                            
                    }
                   
                catch(Exception e) {
                    e.printStackTrace();
                    }  
                }
                    t=dis.readUTF();
                    if (t.equals("System timeout")) {
                       System.out.println(t);
                       savedfile.remove(fileid);
                 //           Files.deleteIfExists(Paths.get("F:\\Level-3 Term-2\\"+filename));
                        }
            //      }
                    break;
                }
                else if (choice1==2) {
                    int id=dis.readInt();
                    int count;
                    String idfront=Integer.toString(id%10000);
                  //  System.out.println(filesenders.get(id).size());
                    byte[] buffer = new byte[10000000];
                   // dos.writeUTF(fileid);
                   // String filestring="51035105ISD.pptx";
                    System.out.println(savedfile);
                    Set<String> st= savedfile.keySet();
                    System.out.println(st.size());
                for (String it:st) {
                    //System.out.println(it.substring(0,3)+"     "+idfront);
                    if (idfront.equals(it.substring(0,4))) {
                        File filesent=savedfile.get(it);
                        String newfile=filesent.getName();
                        dos.writeUTF(newfile);
                        dos.writeInt((int) filesent.length());
                        OutputStream fileout = s.getOutputStream();
                        BufferedInputStream in1 = new BufferedInputStream(new FileInputStream(filesent));
                        if (filesent.exists()) {
                             System.out.println("lekha ase "+filesent.length()+"\n name: "+filesent.getName());
                        }
                         while ((count = in1.read(buffer)) > 0) {
                                fileout.write(buffer,0,count);
                    }
                        System.out.println("File sent from "+it);
                        savedfile.put(it,null);
                        // dos.writeUTF("14"+it.substring(4,8));
                    }
                   }
                       
                       } 
                else if (choice1==3) {
                    activeuser.put(received,0);
                    s.close();
                    dis.close();
                    dos.close();
                    break;
                //    s.close();
                }
                }
                // break the string into message and recipient par
                // search for the recipient in the connected devices list.
                // ar is the vector storing client of active users
            } catch (Exception e) {
                 e.printStackTrace();
            }
             
        }
       try
            {
            // closing resources
        //    this.dis.close();
        //    this.dos.close();
             
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}

