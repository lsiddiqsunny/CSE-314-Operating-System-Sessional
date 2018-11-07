/**
 * Created by istiak on 9/30/2017.
 */
//Example 26

import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import org.omg.PortableInterceptor.INACTIVE;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

/*
 * A chat server that delivers public and private messages.
 */
public class MyFileServer {

    // The server socket.
    private static ServerSocket serverSocket = null;
    // The client socket.

    // This chat server can accept up to maxClientsCount clients' connections.
    private static final int maxClientsCount = 10;
    private    static int BUFFER_SIZE=20*1000000;
    private static final clientThread[] threads = new clientThread[maxClientsCount];
    private  static  final int studentId[]=new int[maxClientsCount];
    private static int FILE_ID=1;

    public  static  String [][]buffers=new String[100][];

    public static void main(String args[]) {
        for(int i=0;i<100;i++){
            buffers[i]=new String[1000];
        }


        Socket clientSocket = null;

        // The default port number.
        int portNumber = 2222;
        System.out.println("Starting Server ......");

    /*
     * Open a server socket on the portNumber (default 2222). Note that we can
     * not choose a port less than 1023 if we are not privileged users (root).
     */
        try {
            serverSocket = new ServerSocket(portNumber);
        } catch (IOException e) {
            System.out.println(e);
        }

        System.out.println("Server Started");

    /*
     * Create a client socket for each connection and pass it to a new client
     * thread.
     */
        while (true) {
            //System.out.println("Waiting for new client");
            try {
                clientSocket = serverSocket.accept();
                //System.out.println(clientSocket.getPort());
                //validate();
                int i = 0;
                for (i = 0; i < maxClientsCount; i++) {
                    if (threads[i] == null) {
                        System.out.println("Creating child at "+i+"   port "+clientSocket.getPort());
                        (threads[i] = new clientThread(clientSocket, threads,studentId,i,BUFFER_SIZE,FILE_ID
                            ,buffers)).start();
                        break;
                    }
                }
                if (i == maxClientsCount) {
                    //PrintStream os = new PrintStream(clientSocket.getOutputStream());
                    //os.println("Server too busy. Try later.");
                   // os.close();
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }

}

/*
 * The chat client thread. This client thread opens the input and the output
 * streams for a particular client, ask the client's name, informs all the
 * clients connected to the server about the fact that a new client has joined
 * the chat room, and as long as it receive data, echos that data back to all
 * other clients. When a client leaves the chat room this thread informs also
 * all the clients about that and terminates.
 */
class clientThread extends Thread {
    public   ObjectOutputStream oos=null;
    public   ObjectInputStream ois=null;

    private Socket clientSocket =null;
    private final clientThread[] threads;
    private final  int maxClientsCount;
    private  static int BUFFER_SIZE;

    private static  int FILE_ID;
    private  Object o=null;
    private String str=null;
    private final    int[] studentId;
    private final int pos;
    private final String CHUNK_SIZE="100";
    private int this_file_size=0;

    private static String [][]buffers;

    private int now_cnt=0;
    private int now_fid=0;
    private int delivary_id=0;
    private String f_name=new String();


    public clientThread(Socket clientSocket, clientThread[] threads,int[] studentId,int ii,int bf,int FILE_ID,String [][]buffers) {
        this.clientSocket = clientSocket;
        this.threads = threads;
        maxClientsCount = threads.length;
        this.studentId=studentId;
        this.pos=ii;
        BUFFER_SIZE=bf;
        this.FILE_ID=FILE_ID;
        this.buffers=buffers;
    }

    public  boolean login(){
        try {
            String str="Enter Your Student ID: ";
            oos.writeObject(str);
            o = ois.readObject();
            String ss=(String)o;
            int id=Integer.parseInt(ss);
            System.out.println("Received Login request from id: "+id+" ip: "+clientSocket.getInetAddress());
            System.out.println(id);
                synchronized (this) {
                    for (int i = 0; i < maxClientsCount; i++) {
                        if (threads[i] != null && threads[i] != this) {
                            if(studentId[i]==id){
                                return  false;
                                /*
                                if(threads[i].clientSocket.getInetAddress() !=clientSocket.getInetAddress())
                                    return  false;
                                threads[i].disconnect();
                                studentId=id;
                                return  true;*/
                            }
                        }
                    }
                }
                studentId[pos]=id;
                return  true;




        }catch (Exception e){
            System.out.println("exception");
            return  false;
        }

    }

    public void run() {
        //int maxClientsCount = this.maxClientsCount;
        //clientThread[] threads = this.threads;

        try {
      /*
       * Create input and output streams for this client.
       */
            oos = new ObjectOutputStream(clientSocket.getOutputStream());
            ois = new ObjectInputStream(clientSocket.getInputStream());
            boolean flag=login();
            if(flag==false){
                System.out.println("Duplicate login for id : "+studentId[pos]);
                str="Duplicate";
                oos.writeObject(str);
                disconnect();
            }
            else{
                System.out.println("Successful login for id : "+studentId[pos]);
                str="success";
                oos.writeObject(str);
            }

            while (true) {
                o=ois.readObject();
                str=(String)o;
                if(str.equalsIgnoreCase("send")){
                    boolean fflag=send_file();
                    if(fflag==false){
                        delete_chunk();
                    }
                    transfer();
                }
            }

        } catch (Exception e) {
        }
    }

    private void transfer()throws Exception{
        int sender_id=0;
        for(int i=0;i<maxClientsCount;i++){
            if(threads[i]==this){
                sender_id=studentId[i];
                break;
            }
        }
        for(int i=0;i<maxClientsCount;i++){
            String ss;
            if(studentId[i]==delivary_id){
                threads[i].oos.writeObject("receive_Call");
                System.out.println("-->>here<<-<<");
                //Object o=new Object();
                //ss=(String) threads[i].ois.readObject();
                //System.out.println(ss);
                //if(ss.equalsIgnoreCase("yes")==false)return;
                System.out.println("sending file name");
                threads[i].oos.writeObject(f_name);
                System.out.println("sending file size");
                threads[i].oos.writeObject(this_file_size);
                System.out.println("sending sender");
                threads[i].oos.writeObject(sender_id);;


                //oo=threads[i].ois.readObject();
                //ss=(String)oo;
               // if(ss.equalsIgnoreCase("yes")){
                    for(int j=0;j<now_cnt;j++){
                        threads[i].oos.writeObject(buffers[now_fid][j]);
                        System.out.println("Sending "+buffers[now_fid][j].length()+"  bytes");
                    }
               // }
                delete_chunk();
            }
        }
    }



    private void delete_chunk(){
        System.out.println("Deleting all chunks ");
        if(now_fid==0)return;
        for(int i=0;i<now_cnt;i++)
            buffers[now_fid][i]=null;
    }

    private Pair<Integer> deStuff(byte[] buff,int bytesRead){
        Pair<Integer> pair = new Pair<Integer>(-1,-1);
        byte flag=126;
        int i=-1,j=-1;
        for(i=0;i<bytesRead;i++){
            if(buff[i]==flag)break;
        }
        if(i==-1)return pair;

        for(j=i+1;j<bytesRead;j++){
            if(buff[j]==flag)break;
        }
        if(i==-1)return pair;
        pair.p1=i+1;
        pair.p2=j-1;
        return  pair;
    }


    private boolean send_file(){

        try {
            str="Enter receiver ID: ";
            oos.writeObject(str);
            o=ois.readObject();
            str=(String)o;
            int id= Integer.parseInt(str);
            delivary_id=id;
            boolean ff=false;
            synchronized (this) {
                for (int i = 0; i < maxClientsCount; i++) {
                    if (threads[i] != null) {
                        if(studentId[i]==id){
                            ff=true;
                            break;
                        }
                    }
                }
            }
            if(!ff){
                str="Receiver not logged in";
                oos.writeObject(str);
                now_fid=0;
                return false;
            }
            oos.writeObject("Found Receiver");
            oos.writeObject("FileTransferStarted");
            oos.writeObject("Enter ok to continue");
            o=ois.readObject();

            //-----------clients go fun started--------------

            oos.writeObject("Enter File name absulate path");


            o=ois.readObject();
            str=(String)o;
            f_name=str;
            System.out.println("File Path : "+str);
            oos.writeObject("Enter File size");

            o=ois.readObject();
            String s2=(String)o;
            System.out.println("File Size : "+s2);
            this_file_size=Integer.parseInt(s2);
            int fid=0;
            synchronized (this){
                if(this_file_size<=BUFFER_SIZE){
                    BUFFER_SIZE-=this_file_size;
                    oos.writeObject("OKK");

                }
                else{
                    oos.writeObject("Too_Large_file");
                    now_fid=0;
                    return false;
                }
            }
            oos.writeObject(CHUNK_SIZE);
            synchronized (this){
                fid=FILE_ID++;
                if(FILE_ID>=100)FILE_ID=0;
            }
            oos.writeObject("Your File Id is "+fid);
            System.out.println("Receiving file id: "+fid);
            oos.writeObject("type 'ok' to start file transfer");
            o=ois.readObject();


            Integer bytesRead = 0;
            int total_read=0;
            int sequence_no=0;
            int random_lost=0;
            do {
                o = ois.readObject();
                bytesRead=(Integer) o;
                //System.out.println("First readBytes : "+bytesRead);
                o = ois.readObject();
                random_lost++;
                if(random_lost==3 || random_lost ==8){
                    System.out.println("*********************************");
                    System.out.println("################################");
                    System.out.println();
                    System.out.println("****** RANDOM LOST FRAME *******");
                    System.out.println();
                    System.out.println("################################");
                    System.out.println("*********************************");
                    bytesRead = Integer.parseInt(CHUNK_SIZE);
                    continue;
                }

                //*********************************DeStuff**************************************************
                byte [] buff = new byte[bytesRead];
                buff = (byte[])o;
                String ss11=new String(buff);
                System.out.println("**************Original Received data start******************");
                System.out.println(ss11);
                System.out.println("**************Original Received data end******************");
                Pair<Integer>pair=deStuff(buff,bytesRead);
                if(pair.p1==-1 || pair.p2==-1){
                    System.out.println("Full frame not received & DISCARDING");
                    bytesRead = Integer.parseInt(CHUNK_SIZE);
                    continue;
                }
                bytesRead=pair.p2-pair.p1+1;
                //System.out.println("After DeStuff , bytes Read: "+bytesRead);
                if(bytesRead <= 3){
                    System.out.println("Empty or error data & DISCARDING");
                    bytesRead = Integer.parseInt(CHUNK_SIZE);
                    continue;
                }
                byte[] buff1=new byte[bytesRead];
                System.arraycopy(buff,pair.p1,buff1,0,bytesRead);

                String ss1=new String(buff1);
                System.out.println("**************After Removing Stuff bit start******************");
                //System.out.println("After Removing Stuff bit : buff1: ");
                System.out.println(ss1);
                System.out.println("**************After Removing Stuff bit end******************");

                System.out.println("--------------------------------------------------");
                System.out.println(">>> expected Sequence no. : "+(sequence_no+1)+"  &&& found : "+(int)buff1[1]);
                System.out.println("--------------------------------------------------");

                if(buff1[1] <(byte)sequence_no+1){
                    System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
                    System.out.println("Duplicate Frame && sending Acknowlegment Again");
                    System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
                    byte [] ack = new byte[4];
                    ack[0]=126;
                    ack[1]='A';
                    ack[2]=buff1[1];
                    ack[3]=126;
                    str="ACK";
                    //System.out.println("===========Sending Acknowledgement for Seq. no :  "+sequence_no+" =============");
                    oos.writeObject(Arrays.copyOf(ack, ack.length));


                    bytesRead = Integer.parseInt(CHUNK_SIZE);
                    continue;
                }

                if(buff1[1] !=(byte)sequence_no+1){
                    System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
                    System.out.println("Sequence NO. not matched & DISCARDING");
                    System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
                    bytesRead = Integer.parseInt(CHUNK_SIZE);
                    continue;
                }

                byte[] buff2=new byte[bytesRead-3];
                System.arraycopy(buff1,2,buff2,0,bytesRead-3);
                String ss2=new String(buff2);
                System.out.println("Main data , buff2: ");
                System.out.println(ss2);
                byte var=(byte) countSetBits(buff2);
                String ss=new String(buff2);
                //System.out.println(ss);
                if(buff2.length != 100)var=buff1[bytesRead-1];
                System.out.println("--------------------------------------------------");
                System.out.println(">>>> CHECK SUM is : "+(int)buff1[bytesRead-1]+" &&&&& Found  : "+(int)var);
                System.out.println("--------------------------------------------------");
                if(var != buff1[bytesRead-1]){
                    System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
                    System.out.println("Check sum not matched & DISCARDING");
                    System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
                    bytesRead = Integer.parseInt(CHUNK_SIZE);
                    continue;
                }
                buffers[fid][sequence_no]=ss;
                sequence_no++;
                total_read+=(bytesRead-3);

                byte [] ack = new byte[4];
                ack[0]=126;
                ack[1]='A';
                if(random_lost != 15)ack[2]=(byte) (sequence_no);
                else{
                    System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
                    System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
                    System.out.println("Randomly generating ERROR ACKnowlegment");
                    System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
                    System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
                    ack[2]=67;
                }
                ack[3]=126;
                str="ACK";
                System.out.println("===========Sending Acknowledgement for Seq. no :  "+sequence_no+" =============");
                oos.writeObject(Arrays.copyOf(ack, ack.length));
                bytesRead-=3;


            } while (bytesRead == Integer.parseInt(CHUNK_SIZE) && total_read<this_file_size);
            System.out.println("--->>>>>>>><<<<<<<<-----");

            o = ois.readObject();
            str=(String)o;
            if(str.equalsIgnoreCase("complete")){
                System.out.println("End of transmission");
            };
            if(this_file_size != total_read){
                System.out.println(total_read+"    "+this_file_size);
                now_fid=0;
                return false;
            }
            System.out.println("File transfer success");

            System.out.println(sequence_no);
            System.out.println(fid);

            now_cnt=sequence_no;
            now_fid=fid;

/*
            for(int i=0;i<sequence_no;i++)
                System.out.println(buffers[fid][i]);
*/


            return true;
        }catch (Exception e){
            return false;
        }

    }

    void disconnect(){
        threads[pos]=null;
        studentId[pos]=0;

        try {
            oos.close();
            ois.close();
            clientSocket.close();
        }catch (Exception e){

        }
    }
    public static byte setBit(int index, final byte b) {
        return (byte) ((1 << index) | b);
    }
    public static boolean getBit(int index, final byte b) {
        byte t = setBit(index, (byte) 0);
        return (b & t) > 0;
    }
    public static int countSetBits(byte[] array) {
        int setBits = 0;
        if (array != null) {
            for (int byteIndex = 0; byteIndex < array.length; byteIndex++) {
                for (int bitIndex = 0; bitIndex < 7; bitIndex++) {
                    if (getBit(bitIndex, array[byteIndex])) {
                        setBits++;
                        //System.out.println("Bit count : "+setBits);
                    }
                }
            }
        }
        return setBits;
    }
}

class Pair<T> {
    T p1, p2;

    Pair(T p1, T p2) {
        this.p1 = p1;
        this.p2 = p2;
    }
}


