package Server;

import Model.FileItem;
import Model.Frame;
import Model.TransmittedFile;
import util.NetworkUtil;


import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Random;

import static Server.Server.*;

public class ReceiveFileServer implements Runnable {


    private Thread thr;
    private NetworkUtil nc;
    private static int MIN_CHUNK_SIZE=10000;
    private static int MAX_CHUNK_SIZE=1000000;
    int byteReceived=0;
    String id;
    String receiverId;

    FileItem item=null;
    File file=null;
    volatile boolean shutDown=false;

    public ReceiveFileServer(NetworkUtil nc, String id) {
        this.nc = nc;
        this.thr = new Thread(this);
        this.id=id;
        thr.start();
        //System.out.println(id);
    }


    public  String generateRandomChars(int length) {

        String candidateChars="ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(candidateChars.charAt(random.nextInt(candidateChars
                    .length())));
        }

        return sb.toString();
    }

    public  int generateInt(int l) {

       /* Random rand=new Random();

        int randomNum = rand.nextInt((MAX_CHUNK_SIZE - MIN_CHUNK_SIZE) + 1) + MIN_CHUNK_SIZE;*/

        int randomNum=l/99;

        return Math.abs(randomNum);
    }

    public void run() {
        try {
            while(!shutDown) {

                if(nc.isClosed){
                    //table.remove(nc);
                    //System.out.println("here");
                    if(file!=null && item !=null && byteReceived<item.getFileLength()){

                        System.out.println("-0");
                        nc.isReceiving=false;
                        CURRENT_BUFFER_SIZE-=file.length();
                        file.delete();
                        ShutDown();
                        continue;
                    }
                }

                nc.isReceiving=true;
                //System.out.println("receiving ");
                sgui.console.setText("Preparing to Receive file from user "+id);
                Object o = nc.read();
                //System.out.println(o);


                if(o!= null) {

                    if(o instanceof FileItem) {
                       // System.out.println(o);

                        item=(FileItem)o;
                        //System.out.println(item.getFileName());
                        //System.out.println(item.getFileLength());
                        sgui.console.setText("\nFile Name: "+item.getFileName()+"\nFile Size: "+
                                item.getFileLength());
                        System.out.println("\nFile Name: "+item.getFileName()+"\nFile Size: " +
                                        item.getFileLength());

                        /*
                            check maximum size of all buffer and take decision
                        */

                        int cz=CURRENT_BUFFER_SIZE+=item.getFileLength();


                        receiverId=item.getReceiverId();

                        if(table.containsKey(receiverId) && cz<=MAX_BUFFER_SIZE){
                            nc.write("Receiver Found");


                            String fileId=generateRandomChars(20);

                            int chunkSize=generateInt(item.getFileLength());

                            //send fileId and chunkSize
                            nc.write(new TransmittedFile(fileId,chunkSize));
                            file=new File("//home//sadiq//Desktop//"+item.getFileName());

                            FileOutputStream fo=new FileOutputStream(file);
                            int i=0;


                            boolean timeout=false;
                            byteReceived=0;
                            int m=item.getFileLength()/chunkSize+1;
                            byte [][]received=new byte[m][chunkSize];
                            ArrayList<Integer>ack=new ArrayList<>();

                            ReceiveData(received,ack,m);

                            for(int j=0;j<m;j++){
                                fo.write(received[j]);
                            }
                            sgui.console.append("\nFile Received SuccessFully");
                            new SendFileServer(file, fileId, id, receiverId);

                            nc.isReceiving = false;
                            ShutDown();

                        }





                        else {
                            if(cz>MAX_BUFFER_SIZE){
                                nc.write("There is Not enough space in Server");
                                CURRENT_BUFFER_SIZE-=item.getFileLength();
                                sgui.console.append("\nThere is Not enough space in Server");
                            }

                            else {
                                nc.write("Receiver Not Logged In");
                                sgui.console.append("\nReceiver not Found");
                            }

                        }

                    }

                }

            }
        } catch(Exception e) {
            //System.out.println (e+ "Thread dead");
            nc.isReceiving=false;

        }
        //nc.closeConnection();
        }

    public void ShutDown() {
        shutDown=true;
    }

    private void ReceiveData(byte [][]received,ArrayList<Integer> ack,int m){
        while (!nc.isClosed){
            Object data = nc.read();

            if (data != null) {
                if (data instanceof ArrayList) {

                    ArrayList<Boolean>list=(ArrayList<Boolean>)data;
                    Frame f=new Frame(list);
                    sgui.console.append("\n======================================\n"+
                    "Seq num "+f.getSeqNum()+"\n=====================================\n");
                    f.printBitFromArray(list,sgui.console);
                    sgui.console.append("\n======================================\n");
                    if(f.getStatus()) {
                        received[f.getSeqNum() - 1] = f.getDeStuffedBit();
                        ack.add((int)f.getSeqNum());
                        f.printBit(received[f.getSeqNum()-1],sgui.console);
                        sgui.console.append("\n======================================\n\n\n\n");

                    }

                }

                if(data instanceof Boolean){

                    nc.write(ack);
                    if(ack.size()==m){
                        for(int i=0;i<m-1;i++){
                            if(ack.get(i)+1==ack.get(i+1)){
                                return;
                            }
                            else {
                                ReceiveData(received,ack,m);
                            }
                        }
                    }
                    else {
                        ReceiveData(received,ack,m);
                    }

                }


            }

        }
    }
}



