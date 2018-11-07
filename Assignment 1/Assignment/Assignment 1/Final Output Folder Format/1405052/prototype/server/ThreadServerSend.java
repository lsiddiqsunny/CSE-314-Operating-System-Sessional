package prototype.server;

import prototype.commons.AuxiliaryMethods;
import prototype.commons.Message;
import prototype.commons.NetworkFx;

import java.io.IOException;

public class ThreadServerSend  implements Runnable{

    boolean b;
    Server server;
    Thread thread;
    //Socket clientSock;
    NetworkFx nfx;
    int s_id, r_id;
    public ThreadServerSend(Server server, int r_id, NetworkFx nfx){
        this.b = b;
        //clientSock = cs;
        this.nfx = nfx;
        this.r_id = r_id;
        this.server = server;
        this.thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        System.out.println("ThreadServerSend started!");
        while(true){


        }


    }

    public synchronized void checkIfClientHasNewMsg(){
        String s1 = null;
        s1=(String) nfx.readFx(); /** 0*/
        System.out.println(s1);
        if(s1.equals("Do I have a new msg?")){
            //check for msg
            int sid = 0; //this.nfx = null;
            boolean receiverFound = false;
            String fileName = ""; long fileSize = 0;
            int fileId=0;
            for(int i=0; i<AuxiliaryMethods.receiverInfos.size(); i++){
                if(AuxiliaryMethods.receiverInfos.get(i).receiverid==r_id){
                    s_id = AuxiliaryMethods.receiverInfos.get(i).senderid;
                    fileName = AuxiliaryMethods.receiverInfos.get(i).fileName;
                    fileSize = AuxiliaryMethods.receiverInfos.get(i).fileSize;
                    fileId = AuxiliaryMethods.receiverInfos.get(i).fileID;
                    receiverFound = true;
                    break;
                }
            }


            nfx.writeFx(receiverFound); /** 1*/
            //boolean b = true;
            if(receiverFound){

                Message msg = new Message();
                msg.sid = s_id;
                msg.fileSize = fileSize;
                msg.msg=fileName;

                nfx.writeFx(msg); /** 2*/
            }
        }
    }
}
