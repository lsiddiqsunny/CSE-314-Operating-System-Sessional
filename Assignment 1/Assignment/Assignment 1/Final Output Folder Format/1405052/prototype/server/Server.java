package prototype.server;

import prototype.commons.*;

import java.io.*;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class Server {
    Timer timer;
    int fileID;
    ServerSocket serverSocket;
    long MaxServerStorage;
    long freeSpace;
    AuxiliaryMethods am;
    File serverStorage;

    public Server(){
        try {
            serverSocket = new ServerSocket(44444);
        } catch (IOException e) {
            e.printStackTrace();
        }

        timer = new Timer();
        fileID = 0;
        am = new AuxiliaryMethods();
        MaxServerStorage = 1024*1024*50;
        /*Setup a folder where temp *.txt files will be stored!*/
        am.createFolder("ServerStorage");
        serverStorage = new File("ServerStorage");
        File[] f = serverStorage.listFiles();
        fileID = f.length;
        freeSpace = MaxServerStorage - serverStorage.length();
        System.out.println("Free space = "+ freeSpace + " bytes");

        AuxiliaryMethods.onlineSenders = new ArrayList<>();
        AuxiliaryMethods.onlineReceivers = new ArrayList<>();
        AuxiliaryMethods.receiverInfos = new ArrayList<>();

        AuxiliaryMethods.onlineSenders.add(new OnlineUsers(null, 0, null));
        AuxiliaryMethods.onlineReceivers.add(new OnlineUsers(null, 0, null));

    }

    public synchronized void checkIfClientHasNewMsg(){
        String s1 = null;
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
               // s1 =
            }
        }, 1);

    }

    public synchronized void receiveChunk(String location, NetworkFx networkFx ) throws IOException {
        Chunk temp = (Chunk) networkFx.readFx();
        File f =  new File(location+"/"+temp.chunkName);
        FileOutputStream fos = new FileOutputStream(f, true);
        fos.write(temp.buff);
        fos.close();
        networkFx.writeFx("received!");
    }

    public void ServerReceive(NetworkFx nfx) throws IOException {
        boolean b1 = false;
        NetworkFx networkFx = nfx;

        /**server receive code begins here*/
        Message msg = (Message) nfx.readFx();/** 1*/
        int senderId = (int) nfx.readFx(); /** 1.1*/
        System.out.println(msg.sid+ " "+ msg.msg+ " "+msg.fileSize);

        freeSpace = MaxServerStorage - serverStorage.length();
        if(AuxiliaryMethods.receiverIsAlreadySignedIn(msg.sid) && (msg.fileSize<freeSpace)){ b1 = true;}

        if(b1){
            nfx.writeFx(true);  /** 2a*/

            int FileID = (fileID++)%100;
            AuxiliaryMethods.receiverInfos.add(new ReceiverInfo(senderId, msg.sid,msg.msg, FileID, msg.fileSize));
            long chunkSize = am.myRandomNum();

            networkFx.writeFx(chunkSize); /** 3a*/
            networkFx.writeFx(FileID);    /** 4a*/

            long totalFileSize = msg.fileSize;
            long numOfChunks = totalFileSize/chunkSize;
            long remainingBytes = totalFileSize%chunkSize;

            String outputLocation = "ServerStorage/"+Integer.toString(FileID);
            am.createFolder(outputLocation);

            for(int j=0; j<numOfChunks;j++){
                try
                {   //byte[] buff = new byte[(int) chunkSize];
                    receiveChunk( outputLocation, networkFx); }  /** 5a*/
                catch (IOException e)
                {    e.printStackTrace(); }
            }
            if(remainingBytes>0){
                try
                {    receiveChunk(outputLocation, networkFx); }  /** 6a*/
                catch (IOException e)
                {    e.printStackTrace(); }
            }

            System.out.println("new File(outputLocation).length() = "+am.getFolderSize(new File(outputLocation)));
            System.out.println("totalFileSize"+totalFileSize);
            if(am.getFolderSize(new File(outputLocation))==totalFileSize){networkFx.writeFx(true);} /** 7aa*/
            else{ networkFx.writeFx(false);} /** 7ab*/


        }
        else{ nfx.writeFx(false);}/** 2b*/



        /**server reeive code ends here*/


    }


    public synchronized void sendBuff(File f, NetworkFx nfx) throws IOException {
        byte[] buff = new byte[(int) f.length()];
        FileInputStream fis=null; BufferedInputStream bis=null;
        try {
            fis = new FileInputStream(f);
            bis = new BufferedInputStream(fis);

            bis.read(buff);

            nfx.writeFx(buff);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(fis!=null){ fis.close();}
            if(bis!=null){ bis.close();}
        }
    }

}
