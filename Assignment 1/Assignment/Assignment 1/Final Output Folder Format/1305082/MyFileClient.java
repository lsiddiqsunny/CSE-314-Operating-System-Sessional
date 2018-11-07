/**
 * Created by istiak on 9/30/2017.
 */
//Example 25

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.*;

public class MyFileClient implements Runnable {

    // The client socket
    private static Socket clientSocket = null;
    // The output stream
    private static PrintStream os = null;
    // The input stream
    private static DataInputStream is = null;

    private   static ObjectInputStream ois=null;
    private   static  ObjectOutputStream oos=null;

    //private static BufferedReader inputLine = null;
    private static boolean closed = false;
    private  static  String responseLine;

    private static  Scanner scanner = new Scanner(System.in);
    private static  boolean spacial_flag=true;
    private static int BUFFER_SIZE=100;

    private  static String file_name;
    private  static int file_size;
    private static int sender;


    public static void main(String[] args) {

        // The default port.
        int portNumber = 2222;
        // The default host.
        String host = "localhost";


    /*
     * Open a socket on a given host and port. Open input and output streams.
     */
        try {
            clientSocket = new Socket(host, portNumber);
            clientSocket.setSoTimeout(1000);
            ois = new ObjectInputStream(clientSocket.getInputStream());
            oos = new ObjectOutputStream(clientSocket.getOutputStream());

        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + host);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to the host "
                    + host);
        }

    /*
     * If everything has been initialized then we want to write some data to the
     * socket we have opened a connection to on the port portNumber.
     */
        if (clientSocket != null && ois != null && oos != null) {
            try {

        /* Create a thread to read from the server. */
                new Thread(new MyFileClient()).start();
                while (!closed) {
                    String str=scanner.nextLine();
                    //System.out.println(str);
                    if(str.equalsIgnoreCase("ok") && !spacial_flag){
                        oos.writeObject("ok");
                        go();
                        continue;
                    }
                    oos.writeObject(str);
                }
        /*
         * Close the output stream, close the input stream, close the socket.
         */
               // os.close();
                //is.close();
                //clientSocket.close();
            } catch (Exception e) {
                System.err.println("IOException:  " + e);
            }
        }
    }

    private static void receive()throws Exception{
        System.out.println("receive function");
            FileOutputStream fos = new FileOutputStream("mytest.txt");

            byte[] buffer = new byte[100];
            int read = 0;
            int totalRead = 0;
            int remaining = file_size;
            while((read = ois.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
                totalRead += read;
                remaining -= read;
                System.out.println("Reading "+read+" bytes");
                System.out.println("read " + totalRead + " bytes.");
                fos.write(buffer, 0, read);
            }
        //}
        System.out.println("Read Complete");
        spacial_flag=true;
        return;
    }




    /*
     * Create a thread to read from the server. (non-Javadoc)
     *
     * @see java.lang.Runnable#run()
     */
    public void run() {
    /*
     * Keep on reading from the socket till we receive "Bye" from the
     * server. Once we received that then we want to break.
     */
    while(true) {
        try {
            while ((responseLine = (String) ois.readObject()) != null) {
                System.out.println(responseLine);
                if (responseLine.equalsIgnoreCase("FileTransferStarted")) {
                    spacial_flag = false;
                    System.out.println((String) ois.readObject());
                    while (!spacial_flag) {
                        Thread.sleep(500);
                        //System.out.println("again sleep");
                    }
                    System.out.println("awaking from sleep");
                }

                if (responseLine.equalsIgnoreCase("receive_Call")) {
                    file_name = (String) ois.readObject();
                    file_size = (Integer) ois.readObject();
                    sender = (Integer) ois.readObject();
                    System.out.println("file name : " + file_name);
                    System.out.println("file size: " + file_size);
                    System.out.println("Sender: " + sender);

                    // System.out.println("awaking from sleep");
                }

                if (responseLine.equalsIgnoreCase("Duplicate"))
                    break;
            }
            closed = true;
        } catch (Exception e) {
            //System.out.println("time fact");
            //System.err.println("IOException:  " + e);
        }
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
                    }
                }
            }
        }
        return setBits;
    }

    private static byte[] bitStuff(byte[] buffer,byte sequence_no,int bytesRead,int random_seq_los){
        byte [] head = new byte[3];
        byte [] tail = new byte[2];
        byte[] frame=new byte[bytesRead+5];
        head[0]=126;
        head[1]='D';
        if(random_seq_los!=5)head[2]=sequence_no;
        else {
            System.out.println("************************************************");
            System.out.println("################################################");
            System.out.println();
            System.out.println(">>> Randomly generating error sequence No. for "+sequence_no);
            System.out.println();
            System.out.println("################################################");
            System.out.println("************************************************");
            head[2]=77;
        }
        int z=head[2];

        byte var=(byte)countSetBits(buffer);
        System.out.println("CHECK SUM of seq. "+sequence_no+" is : "+(int)var);
        if(random_seq_los!=10)tail[0]=var;
        else{
            System.out.println("************************************************");
            System.out.println("################################################");
            System.out.println();
            System.out.println(">>> Randomly generate ERROR CHECK SUM for "+sequence_no);
            System.out.println();
            System.out.println("################################################");
            System.out.println("************************************************");
            tail[0]=0;
        }
        tail[1]=126;
        System.arraycopy(head, 0, frame, 0, head.length);
        System.arraycopy(buffer, 0, frame, head.length, bytesRead);
        System.arraycopy(tail, 0, frame,head.length+bytesRead, tail.length);

        return  frame;
    }


    private   static void go() throws Exception
    {


        String str;
        Object o=new Object();
        o=ois.readObject();//ENTER FILE PATH
        System.out.println((String)o);
        str=scanner.nextLine();
        File f=new File(str);
        System.out.println(f.length());
        FileInputStream fis = new FileInputStream(str);

        oos.writeObject(str);//giving file path
        o=ois.readObject();
        str=(String)o;//ENTER FILE SIZE
        //System.out.println(str);
        //str=scanner.nextLine();
        str=Integer.toString((int)f.length());
        oos.writeObject(str);//giving file size

        o=ois.readObject();
        str=(String)o;
        if(str.equalsIgnoreCase("Too_Large_file")){
            System.out.println(str);
            System.out.println("Aborting Transmission");
            return;
        }
        o=ois.readObject();
        str=(String)o;//BUFFER SIZE
        System.out.println("Server sends BUFFER_SIZE : "+str);
        BUFFER_SIZE=Integer.parseInt(str);
        //BUFFER_SIZE=10;

        System.out.println((String)ois.readObject());//FILE ID
        System.out.println((String)ois.readObject());//press ok to continue
        oos.writeObject(scanner.nextLine());

        byte [] buffer = new byte[BUFFER_SIZE];
        Integer bytesRead = 0;
        int cc=1;
        int random_seq_lost=0;

        while ((bytesRead = fis.read(buffer)) > 0) {

            String ori=new String(buffer);
            System.out.println("-----------------Original Data in this frame start: -----------------------");
            System.out.println(ori);
            System.out.println("-----------------Original Data in this frame end: -----------------------");
            byte[] frame=bitStuff(buffer,(byte)cc,bytesRead,random_seq_lost);
            String aaaa=new String(frame);
            System.out.println("-----------------Suffed Frame:  start: -----------------------");
            System.out.println(aaaa);
            System.out.println("-----------------Suffed Frame:  end: -----------------------");
            boolean fflag=true;
            while (fflag) {
                random_seq_lost++;
                frame=bitStuff(buffer,(byte)cc,bytesRead,random_seq_lost);
                fflag=false;
                try{
                    System.out.println(">>>>>>>>Sending frame Seq. no : " + cc);
                    oos.writeObject(bytesRead + 5);
                    oos.writeObject(Arrays.copyOf(frame, frame.length));
                    o = ois.readObject();
                    byte[] ack=new byte[4];
                    ack=(byte[])o;
                    Pair<Integer>pair=deStuff(ack,4);
                    if(pair.p1==-1 || pair.p2==-1){
                        System.out.println("Full frame not received & DISCARDING");
                        fflag=true;
                    }
                    byte[] now_ack=new byte[2];
                    System.arraycopy(ack,pair.p1,now_ack,0,2);

                    System.out.println("<><><><><> Acknowledgement for Sequence No : "+(int)now_ack[1]+"  <><><><><><>");
                    if(now_ack[0]!='A'){
                        fflag=true;
                        System.out.println("Not acknowlegment fram && RETRANSMIT");
                    }
                    if(now_ack[1] !=(byte)cc){
                        fflag=true;
                        System.out.println("Ack. Seq. now match && RETRANSMIT");
                    }
                    //str = (String) o;
                    //System.out.println(str);
                }catch (Exception e){
                    fflag=true;
                    System.out.println("!!!!!!!Timout exception for Seq. no "+cc+"  !!!!!!!!!!!!!");
                    System.out.println("************ReTransmission for Seq: "+cc+"**************");
                }

            }


            //str = (String) o;
            //System.out.println(str);
            cc++;
        }
        System.out.println("completed file sending");
        oos.writeObject("complete");
        spacial_flag=true;

    }
    private static Pair<Integer> deStuff(byte[] buff,int buff_len){
        Pair<Integer> pair = new Pair<Integer>(-1,-1);
        byte flag=126;
        int i=-1,j=-1;
        for(i=0;i<buff_len;i++){
            if(buff[i]==flag)break;
        }
        if(i==-1)return pair;

        for(j=i+1;j<buff_len;j++){
            if(buff[j]==flag)break;
        }
        if(i==-1)return pair;
        pair.p1=i+1;
        pair.p2=j-1;
        return  pair;
    }
}