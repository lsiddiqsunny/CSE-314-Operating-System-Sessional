package com.company;


import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Scanner;

public class ReadThread implements Runnable{

    private Thread thr;
    ServerInstance si;
    String name;
    ObjectInputStream ois;
    ObjectOutputStream oos;
    String ip;
    int chunkSize;
    ArrayList<String> iplist;
    ArrayList<String> chunkList=new ArrayList<String>();
    String fileName;
    long fileSize;
    long maxBufferSize=100000;
    long curBufferSize=0;
    long files=1; //fileId;
    int sendPort;

    public ReadThread(String name, ObjectInputStream ois, ObjectOutputStream oos, ArrayList < String > iplist, ServerInstance se,int aPort) {

        this.iplist=iplist;
        this.name=name;
        this.sendPort=aPort;
        this.thr = new Thread(this);
        //thr.start();
        this.ois=ois;
        this.oos=oos;
        getInstance(se);
        this.si.flag++;
        thr.start();
    }

    public void run() {
        try {
            //checking if is to send
                String choice=(String) ois.readObject();
                if(choice.equals("n")){
                    System.out.println("Send Port:::::" +sendPort);
                    int llen=this.si.chunkListLen[sendPort-33333];

                    System.out.println("total chunk "+llen);
                    oos.writeObject(llen);
                    oos.writeObject(chunkSize);
                    for(int i=0;/*i<llen*/;i++){
                        String c=this.si.chunkList[sendPort-33333][i];
                        int ilen=c.length();
                        oos.writeObject(c);
                        System.out.println((i+1)+" no Chunk sent to 130509"+(sendPort-33333));
                        String d="....";
                        System.out.println(c);
                        if(c==d) break;
                        Thread.sleep(1000);
                    }
                    this.si.chunkListLen[sendPort-33333]=0;
                    while (true);
                }// Receiving file
                String ip = (String) ois.readObject();
                System.out.println("Requested sender Student IP: " + ip);
                String rIp = (String) ois.readObject();
                System.out.println("Requested Receiver IP: " + rIp);
                if (iplist.contains(ip)) {
                    System.out.println("Already Logged in");
                }
                else {
                    oos.writeObject("Maximum Chunk Size:");
                    oos.writeObject(chunkSize);
                    oos.writeObject(maxBufferSize);
                    this.si.chunkSize = chunkSize;
                    fileName = (String) ois.readObject();
                    this.si.fileList.add(fileName);
                    fileSize = (long) ois.readObject();
                    System.out.println("File name: " + fileName + " Size:" + fileSize);
                    iplist.add(ip);
                    if (fileSize + curBufferSize <= maxBufferSize) {
                        //generate fileId;
                        int fileId = this.si.fileList.indexOf(fileName);
                        oos.writeObject((long) fileId);
                        FileOutputStream fos = new FileOutputStream("F:\\L4T1\\Networking_cse_\\server\\test" + (fileId) + ".txt");
                        byte[] chunk = new byte[chunkSize+10];
                        int count = 0;
                        int cnt = 0;
                        String msg;
                        int timeOut=0;
                        int frame_cnt=0;
                        int errorFlag=0;
                        while (true) {
                            oos.writeObject("ACK " + frame_cnt + " received");
                            msg = (String) ois.readObject();
                            if(msg.equals("Time out")){
                                System.out.println(msg);
                                int portNumber = rIp.charAt(6);
                                portNumber -= 48;
                                this.si.chunkListLen[portNumber]=0;
                                System.out.println("All Chunks Discarded");
                                while(true);
                            }
                            int portNumber = rIp.charAt(6);
                            portNumber -= 48;
                            this.si.chunkList[portNumber][this.si.chunkListLen[portNumber]++]=msg;
                            this.si.chunkSize=this.chunkSize;
                            //destuffing
                            String payload="";
                            int len1=msg.length();
                            //System.out.println("len1:"+len1+" msg:"+msg);

                            if(msg.equals("Transfer Completed")!=true) {
                                int a=-1,b=-1;
                                for(int k=0;k<len1;k++){
                                    if(msg.charAt(k)=='~' && a==-1) {a=k;}
                                    if(a!=-1 && msg.charAt(k)=='~') b=k;
                                }
                                System.out.println("Without Frame: "+msg);
                                String Check_Sum,frame_no;
                                Check_Sum=msg.substring(b+1,msg.length());
                                frame_no=msg.substring(4,a);
                                //System.out.println("Check Sum: "+Check_Sum);
                                int checkSum=Integer.parseInt(Check_Sum);
                                System.out.println("Check Sum: "+checkSum);
                                msg=msg.substring(a+1,b);
                                frame_cnt=Integer.parseInt(frame_no);
                                System.out.println("With    Frame: "+msg);
                                // Detecting Error
                                int len_msg=msg.length();
                                int bit_count=0;
                                for(int j=0;j<len_msg;j++){
                                    int p=msg.charAt(j);
                                    String byte_val=Integer.toBinaryString(p);
                                    int k=byte_val.length();
                                    for(int l=0;l<k;l++) {
                                        if(byte_val.charAt(l)=='1') bit_count++;
                                    }
                                }
                                if(bit_count==checkSum){
                                    System.out.println("No error");
                                }
                                else {
                                    System.out.println("Error");
                                    errorFlag=1;
                                }
                                len1=msg.length();
                                for (int k = 0; k < len1; k++) {
                                    int val = msg.charAt(k);
                                    String byte_val = Integer.toBinaryString(val);
                                    int len_byte_val = byte_val.length();
                                    if(len_byte_val<8) for(int l=0;l<8-len_byte_val;l++) byte_val="0"+byte_val;
                                    payload += byte_val;
                                }

                                System.out.println("Before Destuffed:" + payload);

                                len1=payload.length();
                                int stuffed[]=new int [len1],st_len=0;
                                for(int j=0;j<len1;){
                                    if(payload.charAt(j)=='1' && j+4<len1){
                                        if(payload.charAt(j+1)=='1' && payload.charAt(j+2)=='1' && payload.charAt(j+3)=='1' && payload.charAt(j+4)=='1'){
                                            stuffed[st_len++]=j+5;
                                            j+=5;
                                            continue;
                                        }
                                    }
                                    j++;

                                }
                                System.out.print("Destuffing: ");
                                String vis_payload=payload;

                                for(int j=0;j<st_len;j++) {
                                    int x=stuffed[j]+j;
                                    if (x != 0) {
                                        payload = payload.substring(0,x) + payload.substring(x+1, payload.length());
                                       // vis_payload=vis_payload.substring(0,x)+"."+vis_payload.substring(x+1,vis_payload.length());
                                    }
                                }
                                if(st_len!=0){
                                    payload=payload.substring(0,payload.length()-8)+payload.substring(payload.length()-st_len,payload.length());
                                }
                                System.out.println("\nAfter DeStuffed: "+payload);
                                len1=payload.length();
                                String buffer_temp="";

                                for(int j=0;j<len1;){
                                    int en=j+8;
                                    if(en>len1) en=len1;
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
                                System.out.println("Original: "+buffer_temp);

                            }


                            System.out.print(msg);
                            System.out.println(cnt+" no chunk"+" received");
                            cnt++;
                            int len = 0;
                            int chs=0;
                            if (msg != null && msg.equals("Transfer Completed")==false) {
                                len = msg.length();
                                if(cnt==1) chs=len;
                                for (int i = 0; i < len; i++) chunk[i] = (byte) msg.charAt(i);
                            }
                            fos.write(chunk);/// for test
                            String chunkEntry = msg + " " + (fileId) + " " + ip + " " + rIp; /// CHUNK ENTRY RETRIEVED
                            this.si.chunks.add(chunkEntry);
                            count += len;
                            if (msg.equals("Transfer Completed")==true/*count >= fileSize*/) {
                                oos.writeObject("Chunk " + cnt + "received");
                                String a="....";
                                chunkEntry=a + " " + (fileId) + " " + ip + " " + rIp;
                                this.si.chunks.add(chunkEntry);
                                this.si.chunkList[portNumber][this.si.chunkListLen[portNumber]++]=a;
                                this.si.chunkSize=this.chunkSize;
                                break;
                            }
                            try {
                                if(cnt==3 || errorFlag==1) {Thread.sleep(3000); errorFlag=0;}        // time out option
                                else Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                       //String cmsg = (String) ois.readObject();
                       //System.out.println(cmsg);
                        if (count > fileSize) {
                            for (String x : this.si.chunks) {
                                if (x.endsWith(ip) == true) this.si.chunks.remove(x); // file size exceeds
                            }
                        }
                        fos.close();
                        this.si.flag--;
                        // Receiving file from client

                    } else {
                        System.out.println("Buffer size full");
                        //this.thr.start();
                    }


                this.si.receiverList.add(rIp);
                this.si.senderList.add(ip);
                this.ip = ip;

                //sending files to receiver from server
                int portNumber = rIp.charAt(6);
                portNumber -= 48;    // get portnumber
                this.si.sendIt[portNumber] = 1;
                this.ois.close();
                this.oos.close();

        }
        } catch(SocketException e){
            System.out.println("User Logged of file transfer failed\nFile discarded");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }
    public void getInstance(ServerInstance se){
        si=se;
        si.fileName=fileName;
        si.fileSize=fileSize;
        si.ois=ois;
        si.oos=oos;
        si.iplist=iplist;
    }
    public void chunkSize(int n) {
        chunkSize=n; /// defining maximum chunk size
    }
}
