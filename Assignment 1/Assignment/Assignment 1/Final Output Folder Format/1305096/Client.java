package com.company;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;


public class Client {
    public static String assignIP(int id){
        int c=id%1000;
        int b=(id/1000)%100;
        int a=(id/100000);
        String ip=String.valueOf(a)+"."+String.valueOf(b)+"."+String.valueOf(c)+"."+String.valueOf(0);
        return ip;
    }

  //  public static class clientInst {
        static int maxChunkSize;
        static int id=-1;//=input.nextInt();
        static ObjectOutputStream oos;
        static ObjectInputStream ois;
        static String ip;
        static int receiverId;
        static String rIp;
        static String filename;//= input.nextLine();
        static String file;//=filename;
        static File f;
        static long fileSize;//=f.length();
        static Socket sc;
        static String msg;
        static long fileId;
        static int send=0;
  //  }
    public static void main(String[] args) throws IOException {

        System.out.println("Do you want to send files [Y/N]");
        String c;
        Scanner input=new Scanner(System.in);
        c=input.nextLine();
        if(c.equals("y")) {
            send=1;
            new ClientWriteThread("Client");
        }
        else {
            new ClientWriteThread("Client");
        }

    }


    public static class ClientWriteThread implements Runnable {
        private Thread thr;
        Client ct;
        ObjectInputStream ois;
        ObjectOutputStream oos;
        String ip;
        String rIp;
        String name;
        public ClientWriteThread(String name) {

            this.thr = new Thread(this);
            this.name=name;
            thr.start();
        }

        @Override
        public void run() {
            Scanner input=new Scanner(System.in);
            System.out.println("Enter student ID:");
            id=input.nextInt();

            int port=33333+(id%10);
            //int maxChunkSize;

            try {

                sc = new Socket("127.0.0.1", port);
                oos = new ObjectOutputStream(sc.getOutputStream());
                ois = new ObjectInputStream(sc.getInputStream());

                } catch (IOException e) {
                    e.printStackTrace();
            }

            //// THIS FOR CLIENT RECEIVING

//


            if(send==0) {
                try {
                    oos.writeObject("n");
                    int totalChunk=(int)ois.readObject();
                    System.out.println("Total chunk "+totalChunk);
                    int ChunkSize=(int)ois.readObject();
                    FileOutputStream fos=new FileOutputStream("F:\\L4T1\\Networking_cse_\\client\\130509"+(port-33333)+"\\test.txt");
                    byte[] chunk = new byte[ChunkSize];
                    int cnt=1;
                    while(true){
                        String msg=(String)ois.readObject();
                        System.out.println(msg);
                        System.out.println("Chunk "+cnt+ " received");

                        String d="....";
                        int flag=0;
                        for(int i=0;i<4;i++)  if(msg.charAt(i)!=d.charAt(i)){flag=1;break;}
                        if(flag==0) {
                            System.out.println("File Received complete...");
                            break;
                        }

                        int len=0;
                        if (msg!= null) {
                            len= msg.length();
                            for (int i = 0; i < len; i++) chunk[i] = (byte) msg.charAt(i);

                        }
                        fos.write(chunk);
                        Thread.sleep(1000);

                        cnt++;
                    }
                    while(true);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            ip=assignIP(id); //String.valueOf(a)+"."+String.valueOf(b)+"."+String.valueOf(c)+"."+String.valueOf(0);
            System.out.println("Your Assigned ip:"+ip);
            System.out.println("Enter Receiver id:");


            input=new Scanner(System.in);
            receiverId = input.nextInt();
            rIp=assignIP(receiverId);//String.valueOf(a)+"."+String.valueOf(b)+"."+String.valueOf(c)+"."+String.valueOf(0);
            System.out.println("Your assigned receiver IP:" +rIp);
            input=new Scanner(System.in);

            //    Socket sc= new Socket("127.0.0.1",33333);
//        ObjectOutputStream oos=new ObjectOutputStream(sc.getOutputStream());
            try {
                if(send==1)    oos.writeObject("y");
                oos.writeObject(ip);
                oos.writeObject(rIp);
            } catch (IOException e) {
                e.printStackTrace();
            }

//        ObjectInputStream ois=new ObjectInputStream(sc.getInputStream());
            try {
                msg= (String) ois.readObject();
                maxChunkSize=(int) ois.readObject();
                long maxFileSize=(long)ois.readObject();
                System.out.println("Max file Size "+maxFileSize+" bytes");
                System.out.println(msg+maxChunkSize);
                System.out.println("Enter File Name:");
                input=new Scanner(System.in);
                filename= input.nextLine();
                file=filename;
                filename="F:\\L4T1\\Networking_cse_\\"+filename;
                f= new File(filename);
                fileSize=f.length();
                System.out.println("File Size: " + f.length());
                oos.writeObject(file);
                oos.writeObject(fileSize);
                fileId= (long) ois.readObject();
                System.out.println("Your File Id:"+fileId);
                FileInputStream fis=new FileInputStream(filename);
                int offset=0,readB=maxChunkSize,co=0;
                int remainder=(int)fileSize%maxChunkSize;
                int iter=(int)fileSize/maxChunkSize;
                String buffer="";
                String wait_buffer="";
                int wait_flag=0;
                int timeOut=0;
                for(int i=0;i<iter;i++){
                    Timer ob=new Timer(oos);
                    String cmsg="";
                    if(wait_flag==0) {
                        //Timer lost_f_o=new Timer(ois,1,cmsg);
                        cmsg=(String)ois.readObject();
                    }
                    if(ob.start>=2){
                        System.out.println("Time out");
                        //oos.writeObject("Time out");
                        //protocol to be implemented   Stop and Wait
                        wait_flag=1;
                        i--;
                        continue;

                    }
                    else {
                        System.out.println(cmsg);
                        buffer = "";
                        if(wait_flag==0){
                            for (int j = 0; j < maxChunkSize; j++) buffer += (char) fis.read();
                            wait_buffer=buffer;
                        }
                        else{
                            buffer=wait_buffer;
                            wait_flag=0;
                            System.out.println("Retransmitting...");
                        }
                        System.out.println("Chunk data from file: "+buffer);
                        int len=buffer.length();
                        String payload="";           /// BIT STUFFING
                        for(int k=0;k<len;k++){
                            int val=buffer.charAt(k);
                            String byte_val=Integer.toBinaryString(val);
                            int len_byte_val=byte_val.length();
                            if(len_byte_val<8) for(int l=0;l<8-len_byte_val;l++) byte_val="0"+byte_val;
                            payload+=byte_val;
                        }
                        System.out.println("Before stuffed:"+payload);
                        len=payload.length();
                        int stuffed[]=new int [len],st_len=0;
                        for(int j=0;j<len;){
                            if(payload.charAt(j)=='1' && j+4<len){
                                if(payload.charAt(j+1)=='1' && payload.charAt(j+2)=='1' && payload.charAt(j+3)=='1' && payload.charAt(j+4)=='1'){
                                    stuffed[st_len++]=j+5;
                                    j+=5;
                                    continue;
                                }
                            }
                            j++;

                        }
                        System.out.print("stuffing: ");
                        String vis_payload=payload;
                        for(int j=0;j<st_len;j++) {
                            int x=stuffed[j]+j;
                            if (x != 0) {
                                payload = payload.substring(0,x) + "0" + payload.substring(x, payload.length());
                                vis_payload=vis_payload.substring(0,x)+"."+vis_payload.substring(x,vis_payload.length());
                            }
                        }
                        System.out.println("\nAfter Stuffed:"+payload);
                        System.out.println("\n              "+vis_payload);
                        len=payload.length();
                        String buffer_temp="";
                        for(int j=0;j<len;){
                            int en=j+8;
                            if(en>len) en=len;
                            String bYte="";
                            for(int st=j;st<en;st++){
                                bYte+=payload.charAt(st);
                            }
                            //System.out.println("byte: "+bYte);
                            int dec_val=Integer.parseInt(bYte,2);
                            //System.out.println("dec_val"+dec_val);
                            char char_val=(char)dec_val;
                            buffer_temp+=char_val;
                            j+=8;
                        }
                        System.out.println("Before Framing :"+buffer_temp);
                        // Calculating Checksum
                        len=buffer_temp.length();
                        int bit_count=0;
                        for(int j=0;j<len;j++){
                            int p=buffer_temp.charAt(j);
                            String byte_val=Integer.toBinaryString(p);
                            int k=byte_val.length();
                            for(int l=0;l<k;l++) {
                                if(byte_val.charAt(l)=='1') bit_count++;
                            }
                        }
                        if(i==4){bit_count++;} // for checking error;
                        // formatting frame
                        buffer_temp="~"+buffer_temp+"~";
                        Integer in=new Integer(i+1);
                        buffer_temp="Data"+in.toString()+buffer_temp;
                        len=buffer_temp.length();
                        Integer bit_cont=new Integer(bit_count);
                        buffer_temp=buffer_temp+bit_cont.toString();
                        oos.writeObject(buffer_temp);
                        System.out.println(buffer_temp + " chunk sent");
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();

                        }
                    }

                }
                while (true) {
                    String cmsg = (String) ois.readObject();
                    if (cmsg == null) {
                        timeOut++;
                        // time out message
                        continue;
                    }
                    else break;
                }
                buffer="";
                for(int i=0;i<remainder;i++)buffer+=(char)fis.read();
                if(remainder!=0) {
                    //oos.writeObject(buffer);
                    System.out.println(buffer + " chunk sent");
                }
                //completion message
                oos.writeObject("Transfer Completed");

                fis.close();
                oos.close();

            }catch (EOFException e){
                System.out.println("Size exceeds");
                this.thr.start();
            }
            catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }


        }
    }
}

