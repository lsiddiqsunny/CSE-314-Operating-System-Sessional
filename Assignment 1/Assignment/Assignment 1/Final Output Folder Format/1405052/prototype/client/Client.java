package prototype.client;

import prototype.commons.Chunk;
import prototype.commons.Message;
import prototype.commons.NetworkFx;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {
    NetworkFx nfxSend, nfxReceive;
    int fileID;
    long chunkSize, totalFileSize, numOfChunks, remainingBytes;
    FileInputStream fis;
    BufferedInputStream bis;
    byte[] buff;
    Scanner sc;
    int sid;
    InetAddress ip;
    String serverAddress; int serverPort;
    boolean activateReceivingProcess;

    //boolean valueFlag;

    public Client() throws IOException {
        //valueFlag=true;
        activateReceivingProcess = false;
        sc = new Scanner(System.in);

        ip=null;
        try {    ip = InetAddress.getLocalHost();
            System.out.println(ip); }
        catch (UnknownHostException e) {     e.printStackTrace(); }

        serverAddress = "127.0.0.1";
        serverPort = 44444;
        System.out.println("Enter the Server IP: ");
        serverAddress = sc.next();

        System.out.println("Now enter your Student Id: ");
        sid = sc.nextInt();

        //Message m = new Message(); m.sid = sid; m.ip = ip;

    }
    

    public synchronized void writeBuff(FileOutputStream fos){
        byte[] buff = (byte[]) nfxReceive.readFx();
        try {
            fos.write(buff);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clientReceiver(){}

    public synchronized void checkIfClientHasNewMsg(){
        nfxReceive.writeFx("Do I have a new msg?"); /** 0*/
        boolean b = (boolean) nfxReceive.readFx();
        if(b)
        {
            nfxReceive.writeFx("Send details"); /** 1*/
            Message msg = (Message) nfxReceive.readFx(); /** 2*/
            double fileSize = msg.fileSize; String GMK=" Byte";
            if(msg.fileSize>1024*1024*1024){fileSize = fileSize/(1024*1024*1024); GMK = " GB"; }
            else if(msg.fileSize>1024*1024){fileSize = fileSize/(1024*1024); GMK = " MB";}
            else if(msg.fileSize>1024){fileSize = fileSize/1024; GMK = " KB"; }

            while(true){
                String s = "you have a new msg from "+msg.sid+", File name: "+msg.msg+", File Size: "+fileSize+GMK+". Do you wanna receive it? Press y/n ?";
                String s1 = sc.next();

                if(s1.equals("n")){
                    nfxReceive.writeFx("n");/** 3a*/ /**End of story */
                    break;
                }else if(s1.equals("y")){
                    nfxReceive.writeFx("n"); activateReceivingProcess = true;
                    break;
                }else{
                    System.out.println("Wrong choice! try again!");
                }
            }



        }
    }

    public void ClientSender() throws FileNotFoundException {

        boolean b; String fileToBeUploadedStr = null; int receiversId;
        System.out.println("Enter rceiver's id and file's absolute path:");

        receiversId = sc.nextInt();
        fileToBeUploadedStr = sc.next();

        //System.out.println(fileToBeUploadedStr+" "+receiversId);
        File fileToBeUploaded =  new File(fileToBeUploadedStr);
        System.out.println("File name:"+fileToBeUploaded.getName());

        Message msg = new Message();
        msg.sid = receiversId; msg.fileSize = fileToBeUploaded.length(); msg.msg = fileToBeUploaded.getName();

        nfxSend.writeFx(msg); /** 1 */
        nfxSend.writeFx(sid);
        b = (boolean) nfxSend.readFx(); /** 2 */
        System.out.println(b);
        if(b){
            long chunkSize = (long) nfxSend.readFx(); /** 3a */
            int fileId = (int) nfxSend.readFx();/** 4a */

            totalFileSize = fileToBeUploaded.length();
            numOfChunks = totalFileSize/chunkSize;
            remainingBytes = totalFileSize%chunkSize;

            fis = new FileInputStream(fileToBeUploaded);
            bis = new BufferedInputStream(fis);

            buff = new byte[(int) chunkSize];

            for(int j=0; j<numOfChunks;j++){
                try
                {    sendChunk(j); } /** 5a*/
                catch (IOException e)
                {    e.printStackTrace(); }
            }
            if(remainingBytes>0){
                try
                {    sendChunk(numOfChunks, remainingBytes); }  /** 6a*/
                catch (IOException e)
                {    e.printStackTrace(); }
            }

            System.out.println(nfxSend.readFx()); /** 7a*/
        }else{
            System.out.println("Receiver is offline OR server out of space. Plz try again later!");
        }

    }

    public synchronized void sendChunk(int j) throws IOException {

        int i = bis.read(buff,0, (int) chunkSize);
        Chunk chunk = new Chunk();
        chunk.buff = buff;
        chunk.chunkName = "a"+Integer.toString(fileID)+"_"+Integer.toString(j)+".txt";
        nfxSend.writeFx(chunk);

    }

    public synchronized void sendChunk(long j, long remainingBytes) throws IOException {
        buff = new byte[(int) remainingBytes];
        int i = bis.read(buff,0, (int) remainingBytes);
        Chunk chunk = new Chunk();
        chunk.buff = buff;
        chunk.chunkName = "a"+Integer.toString(fileID)+"_"+Long.toString(j)+".txt";
        nfxSend.writeFx(chunk);

    }
}
