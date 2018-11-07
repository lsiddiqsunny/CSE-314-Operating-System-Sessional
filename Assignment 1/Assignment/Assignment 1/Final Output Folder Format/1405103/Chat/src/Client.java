/**
 * Created by ksyp on 9/28/17.
 */
import java.io.*;
import java.net.*;
import java.util.BitSet;
import java.util.Scanner;
import java.util.StringTokenizer;

public class Client {
    final static int ServerPort = 1030;

    public static void main(String args[]) throws IOException {

        Scanner myScanner = new Scanner(System.in);
        InetAddress ip = InetAddress.getByName("localhost");

        System.out.println("Enter your roll");
        String myRoll = myScanner.nextLine();
        System.out.println(myRoll);

        Socket s = null;
        DataInputStream dis;
        DataOutputStream dos;


        s = new Socket(ip, ServerPort);
        dis = new DataInputStream(s.getInputStream());
        dos = new DataOutputStream(s.getOutputStream());

        dos.writeUTF(myRoll);

        String serv = dis.readUTF();
        if (serv.equals("Accepted")) {

            Socket finalS = s;
            Thread fileTransfer = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {

                        try {

                            myExp bb = new myExp(dis, dos, myScanner);
                            String S = dis.readUTF();

                            if (S.equals("beReceiver")) {
                                System.out.println("Receiving File");
                                bb.receive();
                            } else if (S.equals("beSender")) {
                                System.out.println("Inbox empty");
                                System.out.println("reload inbox or send file or logout");
                                String inp = myScanner.nextLine();
                                if (inp.equals("reload inbox")) {
                                    dos.writeUTF("R");
                                    continue;
                                } else if (inp.equals("logout")) {
                                    dos.writeUTF("logout");
                                    break;

                                } else {
                                    dos.writeUTF("S");
                                    bb.send();
                                }
                            }
                        }
                        catch(IOException e){
                            System.out.println("Exeption in sending file");
                        }


                    }

                    myScanner.close();
                    try {
                        dos.close();
                        dis.close();
                        finalS.setReuseAddress(true);
                        finalS.close();
                    } catch (Exception e) {

                    }

                }


            });

            fileTransfer.start();
        }
        else{
            System.out.println("You are already connected with different Port");
            dis.close();
            dos.close();
            s.close();
        }
    }

}

class myExp{

    public DataOutputStream dos;
    public DataInputStream dis;
    public Scanner scn;

    public myExp(DataInputStream dis,DataOutputStream dos,Scanner scn){
        this.dis = dis;
        this.dos = dos;
        this.scn = scn;
    }

    public String bytesTObits(byte[] myBytes){
        BitSet bits = BitSet.valueOf(myBytes);
        String str ="";
        for(int i=0;i<bits.length();i++){
            if(bits.get(i)) str+="1";
            else str+="0";

        }
        
        if(bits.length()%8!=0){
            for(int i=0;i<8-bits.length()%8;i++) {
                str+="0";
            }
        }
        //return bitset.toString();
        return str;
    }


    public String bytesTObits(byte[] myBytes , int len){
        String str="";
        for(int i=0;i<len;i++){
            for(int j=7;j>=0;j--){
                int mask = 0b00000001<<j;
                if((mask&myBytes[i])==0){
                    str+="0";
                }
                else{
                    str+="1";
                }
            }
        }
        return str;
    } 


    public byte[] bitsTObytes(String myBits){
        int len = (myBits.length()+7)/8;
        byte[] temp = new byte[len];
        for(int i=0;i<len;i++){
            for(int j=0;j<8;j++){
                if(myBits.charAt(8*i+j)=='1'){
                    temp[i]|=0b10000000>>j;
                }
            }
            //System.out.println(temp[i]);
        }
        return temp;
    }

    public String checksum(String str){
        String s="";
        for(int i=0;i<8;i++){
            int cnt=0;
            for(int m=i;m<str.length();m+=8){
                if(str.charAt(i)=='1') cnt++;
            }
            if(cnt%2==1) s+="1";
            else s+="0";
        }
        return s;
    }

    public String makeError(String payload){
        System.out.println("Do you want to make error in this frame??(Y/n)");
        String temp = payload;
        while(true){

            String reply = scn.nextLine();
            System.out.println(reply);
            if(reply.equals("n")||reply.equals("N")){
                break;
            }
            //System.out.println(temp);
            System.out.println("Enter position");
            int pos=Integer.parseInt(scn.nextLine());
            System.out.println(temp);
            String x = "0";
            if(temp.charAt(pos)=='0') x="1";
            temp=temp.substring(0,pos)+x+temp.substring(pos+1);
            System.out.println(temp);
            System.out.println("Do you want to make more error in this frame??(Y/n)");
        }

        return temp;
    }

    public String myParser(String frame){
        return frame.substring(8,frame.lastIndexOf("01111110"));
    }

    public String stuffBits(String frame){
        String temp = frame.replace("11111","111110");
        // String str="";
        // if(temp.length()%8!=0){
        //     for(int i=0;i<8-temp.length()%8;i++) {
        //         str+="0";
        //     }
        // }

        // return temp+str;
        return temp;
    }

    public String roundFrames(String frame){
        String str="";
        if(frame.length()%8!=0){
            for(int i=0;i<8-frame.length()%8;i++) {
                str+="0";
            }
        }
        return frame+str;       
    }

    synchronized public void send() throws IOException {
        //Declearations

        String receiverID = null;
        String filePath = null;
        String fileName = null;

        String receiverOnline = null;


        long fileSize = 0;
        int chankSize = 0;
        int chankNo = 0;
        int sentSize = 0;


        File file = null;


        FileInputStream fis = null;
        BufferedInputStream bis = null;


        byte[] myBytes;


        // Getting the receiver ID
        System.out.println("Please enter recEiver Id");
        receiverID = scn.nextLine();


        //Sending the receiver ID to server
        try {
            // write on the output stream
            dos.writeUTF(receiverID);
        } catch (IOException e) {
            //e.printStackTrace();
            System.out.println("Error in sending receiverID in sendMessage");
        }

        try {
            receiverOnline = dis.readUTF();
        } catch (IOException e) {
            System.out.println("Error in checking receiver online");
        }

        if (receiverOnline.equals("Continue")) {


            //Getting the file path
            System.out.println("Please enter the file path you want to send to " + receiverID);
            filePath = scn.nextLine();
            System.out.println(filePath);


            //opening the file and extracting file infos
            try {
                file = new File(filePath);
                fileName = file.getName();
                fileSize = file.length();
                //System.out.println(fileSize);

            } catch (Exception e) {
                System.out.println("File can't be opened :'( ");
            }

            dos.writeUTF(fileName);
            dos.writeUTF(String.valueOf(fileSize));

            String spaceAllocatiom = null;
            spaceAllocatiom = dis.readUTF();
            if (spaceAllocatiom.equals("SizeOK")) {

                //receiving chank size and chank no from server
                String abc = dis.readUTF();
                StringTokenizer st = new StringTokenizer(abc, "::");
                String MsgToSend = st.nextToken();
                String recipient2 = st.nextToken();
                chankSize = Integer.parseInt(MsgToSend);
                chankNo = Integer.parseInt(recipient2);


                //initializing FileInputStream
                fis = new FileInputStream(file);
                //initializing BufferInputStream
                bis = new BufferedInputStream(fis);
                System.out.println("Uploading started");

                while (sentSize < fileSize) {
                    if ((int) fileSize - sentSize < chankSize) {
                        chankSize = (int) fileSize - sentSize;
                        //System.out.println(chankNo + "," + chankSize);
                    }
                    myBytes = new byte[chankSize];
                    bis.read(myBytes, 0, chankSize);
                    System.out.println(chankSize);
                    String payload = bytesTObits(myBytes,chankSize);
                    System.out.println("Paylode ::"+payload);
                    String checkSum=checksum(payload);
                    System.out.println("CheckSum ::"+checkSum);
                    String errPayload = makeError(payload);
                    System.out.println("Error Payload ::"+errPayload);
                    String frame1 = errPayload+checkSum;
                    //frame1+="11111000";
                    System.out.println("temp Frame Without stuffing ::"+frame1);
                    String frameStuffed = stuffBits(frame1); 
                    System.out.println("Stuffed frame ::"+frameStuffed);
                    String frameTemp = "01111110"+frameStuffed+"01111110";
                    //System.out.println(frameTemp);
                    String myFrame = roundFrames(frameTemp);
                    System.out.println("Final Frame ::"+myFrame);
                    //System.out.println(myParser(myFrame));
                    byte[] myBytes2 = bitsTObytes(myFrame);
                    //String payload2 = bytesTObits(myBytes2);
                    //String payload3 = bytesTObits(myBytes2,myFrame.length()/8);
                    //System.out.println(myFrame);
                    //System.out.println(payload2);
                    //System.out.println(payload3);



                    dos.writeUTF(Integer.toString(myFrame.length()/8));
                    //dos.flush();
                    //dos.writeUTF(myFrame);
                    //dos.flush();
                    dos.write(myBytes2);
                    dos.flush();
                    String ss = dis.readUTF();

                    if (ss.equals("Yes")) {
                        sentSize = sentSize + chankSize;
                        chankNo--;
                        //System.out.println(chankNo);
                        //System.out.println(sentSize + " " + fileSize + " " + chankSize);
                    }
                    else if(ss.equals("Stop")){
                        while(true){
                            System.out.println("Error frame");
                            System.out.println("Paylode ::"+payload);
                            errPayload = makeError(payload);

                            System.out.println("with errors ::"+errPayload);
                            frame1 = errPayload+checkSum;
                            //frame1+="11111000";
                            //System.out.println(frame1);
                            frameStuffed = stuffBits(frame1); 
                            //System.out.println(frameStuffed);
                            frameTemp = "01111110"+frameStuffed+"01111110";
                            //System.out.println(frameTemp);
                            myFrame = roundFrames(frameTemp);
                            System.out.println("Final Frame ::"+myFrame);
                            //System.out.println(myParser(myFrame));
                            myBytes2 = bitsTObytes(myFrame);
                            //payload2 = bytesTObits(myBytes2);
                            //payload3 = bytesTObits(myBytes2,myFrame.length()/8);
                            //System.out.println(myFrame);
                            //System.out.println(payload2);
                            //System.out.println(payload3);



                            dos.writeUTF(Integer.toString(myFrame.length()/8));
                            //dos.flush();
                            //dos.writeUTF(myFrame);
                            //dos.flush();
                            dos.write(myBytes2);
                            dos.flush();
                            ss = dis.readUTF();

                            if (ss.equals("Yes")) {
                                sentSize = sentSize + chankSize;
                                chankNo--;
                                break;
                            //System.out.println(chankNo);
                            //System.out.println(sentSize + " " + fileSize + " " + chankSize);
                            }
                            else if(ss.equals("Stop")){
                                continue;
                            }
                        }
                    }
                    else{
                            continue;
                        }

                }
                System.out.println("Uploading Complete");


                try {
                    bis.close();
                    fis.close();
                } catch (Exception E) {
                    System.out.println("Error in closing :(");
                }


            }
            else{
                System.out.println("Server is Busy");
            }

        }
        else{
            System.out.println("Sorry "+receiverID+" is offline");
        }
    }


     synchronized public void receive() throws IOException {

        String fileSender = null;
        String fileName = null;
        int fileSize = 0;
        String filePath = null;

        String start =null;


        fileSender = dis.readUTF();
        fileName = dis.readUTF();
        fileSize = dis.readInt();


         System.out.println("You have a pending file Sent by :"+fileSender+" File Name :"+fileName+" and File Size "+fileSize);
         System.out.println("Do you want to download it Y/n ..");
         start = scn.nextLine();

         dos.writeUTF(start);
         if(start.equals("Y") || start.equals("y")){

             System.out.println("Give the path where you want to download it");
             filePath = scn.nextLine();
             filePath = filePath+fileSender+fileName;



             File file = new File(filePath);
             FileOutputStream fos = new FileOutputStream(file);
             BufferedOutputStream bos = new BufferedOutputStream(fos);
             int chankSize =0;
             int chankNo = 0;
             int sizeReceived = 0;


             chankSize = dis.readInt();
             chankNo = dis.readInt();

            // System.out.println(chankSize+":::::"+chankNo);


             byte[] myBytes = new byte[chankSize];


             System.out.println("downloading started");



             while (chankNo!=0) {
                 try {
                     int sizeReceived2;
                     sizeReceived2= dis.read(myBytes);
                     bos.write(myBytes, 0, sizeReceived2);
                     sizeReceived += sizeReceived2;
                     dos.writeUTF("Yes");
                     chankNo--;
                    // System.out.println(sizeReceived);

                 } catch (Exception e) {
                     dos.writeUTF("Problem");

                     System.out.println("receiving prob");
                     break;

                 }


                 //System.out.println(chankNo);
             }
             //System.out.println("broke");

             try {
                 System.out.println("Downloading Complete");
                 bos.flush();
                 bos.close();
                 fos.close();
             } catch (Exception e) {
                 System.out.println("Error in closing fos bos etc");
             }





         }
         else if(start.equals("N") || start.equals("n")){
             System.out.println("downloading cancelled");
         }

     }

    }


