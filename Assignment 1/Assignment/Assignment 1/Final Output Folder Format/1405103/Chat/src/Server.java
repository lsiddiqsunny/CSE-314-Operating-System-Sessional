/**
 * Created by ksyp on 9/28/17.
 */

import java.io.*;
import java.util.*;
import java.net.*;

public class Server
{
    static Vector<Handler> ar = new Vector<>();
    public static int maxSize= 100000;
    public static int sizeBusy = 0;
    public static Vector<myFile> myFiles = new Vector<>();
    public static Hashtable<String,Integer> clients = new Hashtable<>();
    /**
     *
     */
    public static String serverDirectory;


    public static void main(String[] args) throws IOException
    {
        ServerSocket ss = new ServerSocket(1030);
        Scanner myScn = new Scanner(System.in);
        System.out.println("Enter Server Directory");
        Server.serverDirectory = myScn.nextLine();
        System.out.println("Enter ServerMax Size");
        Server.maxSize=Integer.parseInt(myScn.nextLine());
        Socket s;
        System.out.println("Server is ready");

        while (true) {

            s = ss.accept();
            System.out.println("New client request received : " + s);

            DataInputStream dis = new DataInputStream(s.getInputStream());
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());

            String newClient = dis.readUTF();
            if (clients.containsKey(newClient)) {
                dos.writeUTF("rejected");
                s.close();
                dis.close();
                dos.close();
            } else {

                dos.writeUTF("Accepted");
                System.out.println(newClient);
                clients.put(newClient, s.getLocalPort());

                Handler clientHandler = new Handler(s, newClient, dis, dos);
                Thread handlerThread = new Thread(clientHandler);

                ar.add(clientHandler);
                handlerThread.start();


            }
        }
    }
}

// ClientHandler class
class Handler implements Runnable
{
    Scanner scn = new Scanner(System.in);
    public String clientName;
    public DataInputStream dis;
    public DataOutputStream dos;
    Socket s;
    boolean isloggedin;
    public Vector<myFile> pendingFiles = new Vector<>();

    // constructor
    public Handler(Socket s, String name,
                         DataInputStream dis, DataOutputStream dos) {
        this.dis = dis;
        this.dos = dos;
        this.clientName = name;
        this.s = s;
        this.isloggedin=true;
    }

    @Override
    public void run() {

        while (true)
        {
            try
            {
                if(this.isloggedin) {


                    if(existPendingFile(this.clientName)){

                        dos.writeUTF("beReceiver");
                        myFile temp = pendingFile(this.clientName);

                        dos.writeUTF(temp.fileSender);
                        dos.writeUTF(temp.fileName);
                        dos.writeInt((int) temp.fileSize);

                        System.out.println(temp.fileSender+": :"+temp.fileName+": :"+temp.fileSize);

                        String start = dis.readUTF();
                        if(start.equals("Y") || start.equals("y")){
                            // System.out.println("start downloading");
                            long fileSize = temp.fileSize;
                            int chankSize = temp.chank;
                            int chankNo = temp.chankNo;
                            int sentSize = 0;

                            File file = null;

                            FileInputStream fis = null;
                            BufferedInputStream bis = null;

                            byte[] myBytes;

                            dos.writeInt(chankSize);
                            dos.writeInt(chankNo);

                            file = new File(temp.filePath);
                            fis=new FileInputStream(file);
                            bis = new BufferedInputStream(fis);


                            while (sentSize < fileSize) {

                                if ((int) fileSize - sentSize < chankSize) {
                                    chankSize = (int) fileSize - sentSize;
                                    // System.out.println(chankNo + "," + chankSize);
                                }

                                myBytes = new byte[chankSize];
                                bis.read(myBytes, 0, chankSize);
                                dos.write(myBytes);
                                dos.flush();
                                String ss = null;
                                ss = dis.readUTF();

                                if (ss.equals("Yes")) {
                                    sentSize +=chankSize;
                                    chankNo--;
                                    // System.out.println(chankNo);
                                    // System.out.println(sentSize + " " + fileSize + " " + chankSize);
                                }
                                else {
                                    continue;
                                }

                            }
                            //System.out.println("");

                            try {
                                bis.close();
                                fis.close();
                            }
                            catch (Exception E) {
                                System.out.println("Error in closing :(");
                            }

                        }
                        else if (start.equals("N") || start.equals("n")){
                            System.out.println("download cancelled");
                        }

                        File mf = new File(temp.filePath);
                        mf.delete();
                        Server.sizeBusy-=temp.fileSize;
                        Server.myFiles.remove(temp);

                    }

                    else{
                        dos.writeUTF("beSender");
                        String sss = dis.readUTF();
                        if(sss.equals("S")) {
                            myFile uploadingFile = receiveServer(dos, dis);
                            if (uploadingFile != null) {
                                Server.myFiles.add(uploadingFile);
                            }
                        }
                        else if(sss.equals("logout")){
                            this.isloggedin=false;
                            Server.clients.remove(this.clientName);
                            break;
                        }
                        else if(sss.equals("R")){
                            // System.out.println(1212121212);
                            continue;
                        }
                    }


                }
            } catch (IOException e) {
                System.out.println("In Server");
                this.isloggedin=false;
                Server.clients.remove(this.clientName);
                break;
               // e.printStackTrace();
            }

        }
        try
        {
            // closing resources
            this.s.setReuseAddress(true);
            this.s.close();
            this.dis.close();
            this.dos.close();

        }catch(IOException e){
            System.out.println("Closing");
           // e.printStackTrace();
        }
    }

    synchronized public boolean allocateSpace(int fileSize){
        if(Server.sizeBusy+fileSize<=Server.maxSize){
            Server.sizeBusy+=fileSize;
            return true;
        }
        return false;
    }


    public boolean isOnline( String clientID){
        for (Handler mc : Server.ar) {
            if (mc.clientName.equals(clientID) && mc.isloggedin == true) {
                return true;
            }
        }
        return false;
    }

    public boolean existPendingFile(String clientID){
        for (myFile mf : Server.myFiles) {
            if (mf.fileReceiver.equals(clientID)) {
                return true;
            }
        }
        return false;
    }

    public myFile pendingFile(String clientID){
        for (myFile mf : Server.myFiles) {
            if (mf.fileReceiver.equals(clientID)) {
                return mf;
            }
        }
        return null;
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

    public String deStuffBits(String frame){
        return frame.replace("111110","11111");
    }

    public boolean hasError(String payload,String checkSum){
        if(checkSum.equals(checksum(payload))) return false;
        else return true;
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
    public String myParser(String frame){
        return frame.substring(8,frame.lastIndexOf("01111110"));
    }


    public myFile receiveServer(DataOutputStream dos, DataInputStream dis) throws IOException {

        myFile temp = null;
        String recipient = dis.readUTF();

        if(isOnline(recipient)) {
            dos.writeUTF("Continue");


            String MsgToSend = dis.readUTF();
            int fileSize = Integer.parseInt(dis.readUTF());
            if (allocateSpace(fileSize)) {
                try {
                    //System.out.println(Server.sizeBusy);
                    dos.writeUTF("SizeOK");

                    temp = new myFile(this.clientName, recipient, MsgToSend, fileSize);
                    dos.writeUTF(temp.chank + "::" + temp.chankNo);

                    String S = Server.serverDirectory + temp.fileID;
                    File file = new File(S);
                    temp.filePath = S;
                    FileOutputStream fos = new FileOutputStream(file);
                    BufferedOutputStream bos = new BufferedOutputStream(fos);
                    int chankSize = temp.chank;
                    System.out.println(chankSize);
                    int chankNo = temp.chankNo;
                    int sizeReceived = 0;

                    byte[] myBytes = new byte[chankSize];


                    while (chankNo != 0) {

                        try {
                            int szz = Integer.parseInt(dis.readUTF());
                            byte[] myBytes2=new byte[szz];
                            // String myFrame = bytesTObits(myBytes2);
                            // System.out.println(myFrame);
                            // String frame1=myParser(myFrame);
                            // System.out.println(frame1);
                            //String myFrame2 = dis.readUTF();
                            //System.out.println(myFrame2);
                            int sizeReceived2 = dis.read(myBytes2);
                            String myFrame = bytesTObits(myBytes2,szz);
                            System.out.println("Received frame :: "+myFrame);
                            //myFrame = new StringBuilder(myFrame).reverse().toString();
                            //System.out.println(myFrame);
                            String frame1=myParser(myFrame);
                            System.out.println("Payload+checksum :: "+frame1);
                            String deStuffedFrame = deStuffBits(frame1);
                            System.out.println("Destuffed frame :: "+deStuffedFrame);
                            String payload = deStuffedFrame.substring(0,frame1.length()-8);
                            String checkSum = deStuffedFrame.substring(frame1.length()-8);
                            System.out.println("Payload :: "+payload);
                            //System.out.println(checkSum);

                            if(!hasError(payload,checkSum)){
                                int tempppp=payload.length()/8;
                                myBytes=bitsTObytes(payload);
                                bos.write(myBytes, 0, tempppp);
                                sizeReceived += sizeReceived2;
                                dos.writeUTF("Yes");
                                chankNo--;
                            }
                            else{
                                dos.writeUTF("Stop");
                                System.out.println("There is error in frame.");
                            }
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
                        //if(fileSize==sizeReceived) {
                          //  System.out.println("Receiving complete");
                            bos.flush();
                        //}
                        //else{
                          //  System.out.println("Error in receiving");
                        //}

                        bos.close();
                        fos.close();
                    } catch (Exception e) {
                        System.out.println("Error in cloasing fos bos etc");
                    }

                    return temp;
                }
                catch(Exception E){
                    Server.sizeBusy-= fileSize;
                    dos.writeUTF("asas");
                    return null;
                }
            }
            else {
                dos.writeUTF("SizeNOK");
                return null;
            }
        }
        else{
            dos.writeUTF("Break");
            return null;
        }
    }



}