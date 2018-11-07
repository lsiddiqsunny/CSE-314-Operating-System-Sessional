import com.sun.org.apache.xpath.internal.SourceTree;

import javax.sound.midi.Soundbank;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class Main {

    public static void main(String[] args) {
        System.out.println("Enter ID to get connected: ");
        Scanner scanner = new Scanner(System.in);
        String ID = scanner.nextLine();
        Socket connectionSocket = null;
        Socket receiverSocket = null;
        try {
            connectionSocket = new Socket("localhost",6789);
            receiverSocket = new Socket("localhost",6789);
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            DataOutputStream toServer = new DataOutputStream(connectionSocket.getOutputStream());


            //System.out.println(inFromServer.readLine());
            toServer.writeBytes(ID+'\n');
            String msg = inFromServer.readLine();
            System.out.println(msg);
            if(msg.equals("error")){
                System.out.println("ID already logged in.");
                return;
            }
            else System.out.println("Connected");
            ClientThread consoleComm = new ClientThread("L",connectionSocket,receiverSocket);
            ClientThread serverComm = new ClientThread("s",connectionSocket,receiverSocket);
            Thread t1 = new Thread(serverComm);
            Thread t2 = new Thread(consoleComm);
            //boolean sending = false;
            t1.start();
            t2.start();




        } catch (IOException e) {
            e.printStackTrace();
        }



    }
}

class ClientThread implements Runnable{
    String type,userMsg = null, serverMsg = null;
    Socket connectionSocket;
    Socket receiverSocket;
    String input,str="";
    Thread otherThread;
    InputStream in;
    int ackCnt, timeOut;

    public ClientThread(String type, Socket connectionSocket,Socket receiverSocket ){
        this.type = type;
        this.connectionSocket = connectionSocket;
        this.receiverSocket = receiverSocket;
        try {
            in = receiverSocket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    @Override
    public void run() {
        try {
            while(true){
                if(type.equals("s")) {
                    BufferedReader inFromServer = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                    DataOutputStream toServer = new DataOutputStream(connectionSocket.getOutputStream());
                    Scanner sc = new Scanner(System.in);
                    System.out.println("Send File to: ");
                    //String receiver = sc.nextLine();

                    //toServer.writeBytes(receiver+'\n');
                    //String reply = inFromServer.readLine();
                    String reply = "yes";
                    if(reply.equals("NO")){
                        System.out.println("Receiver not online.");
                    }
                    else{
                        System.out.println("File Name: ");
                        String fileName = sc.nextLine();
                        File file = new File("SenderFile/"+fileName);
                        long fileSize = file.length();
                        System.out.println(file.length());
                        toServer.writeBytes(fileName+'\n');
                        toServer.writeBytes(Long.toString(fileSize)+'\n');
                        reply = inFromServer.readLine();
                        System.out.println(reply);
                        if(reply.equals("error")) System.out.println("Size limit exceeded.");
                        else{
                            Integer chunkSize;
                            if(fileSize>10) chunkSize = (int)fileSize / 10;
                            else chunkSize = (int)fileSize;
                            if(chunkSize>31) chunkSize = 31;
                            //int chunkSize = Integer.parseInt(reply);
                            byte[] b = new byte[chunkSize];

                            InputStream in = new FileInputStream(file);
                            OutputStream out = connectionSocket.getOutputStream();
                            //in.read(b);
                            //printbits(b);
                            //ArrayList <Byte> c = bitStuffing(b);
                            //printbits(c);
                            ArrayList <byte[]> frames = new ArrayList <byte[]>();
                            int i,j = 0, frameCnt = 0;
                            boolean errorDone = false;
                            while ((i = in.read(b)) > 0) {
                                System.out.println("Payload "+j+":");
                                printbits(b);
                                ArrayList <Byte> c = bitStuffing(b,i);

                                System.out.println("After BitStuffing "+j+":");
                                printbits(c);
                                byte []frame =getFrame(c,j);
                                System.out.println("Frame "+j+":");
                                j++;
                                frames.add(frame);
                                printbits(frames.get(frameCnt));
                                if(frameCnt==4 && errorDone==false){
                                    frame = genError(frame);
                                    errorDone = true;
                                }
                                out.write(frame);
                                if(frameCnt==4 && errorDone==true){
                                    frame = correctError(frame);
                                    errorDone = true;
                                }
                                frameCnt++;
                                if(frameCnt%3==0) {
                                    try {
                                        getInput(connectionSocket);
                                        if(ackCnt!=3){
                                            frameCnt = frameCnt - (3-ackCnt);
                                            for(;frameCnt<frames.size();frameCnt++){
                                                System.out.println("Resending Frame "+frameCnt);
                                                printbits(frames.get(frameCnt));
                                                out.write(frames.get(frameCnt));
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                           byte[] v = new byte[1];
                            v[0] = (byte)255;
                            out.write(v);
                            in.close();
                        }
                    }
                }
                else{
                    BufferedReader inFromServer = new BufferedReader(new InputStreamReader(receiverSocket.getInputStream()));
                    DataOutputStream toServer = new DataOutputStream(receiverSocket.getOutputStream());
                    Scanner sc = new Scanner(System.in);
                    String notification = inFromServer.readLine();
                    if(notification.equals("FR")){
                        String fileName,fileSize,from;
                        fileName = inFromServer.readLine();
                        fileSize = inFromServer.readLine();
                        from = inFromServer.readLine();
                        System.out.println("You have Received a file from "+from+".\nFile Name: "+fileName+"\tFile Size: "+fileSize);
                        String accept = "Y";
                        if(accept.equals("N")||accept.equals("n")){
                            toServer.writeBytes("N\n");
                        }
                        else if (accept.equals("Y")||accept.equals("y")){
                            toServer.writeBytes("Y\n");
                            Integer chunkSize = Integer.parseInt(fileSize)/10;
                            in = receiverSocket.getInputStream();
                            OutputStream out = new FileOutputStream("ReceivedFiles/" + fileName);
                            byte[] b = new byte[chunkSize];
                            int i = 0, sz = 0, j = 0;
                            while((i = in.read(b))>0){
                                out.write(b,0,i);
                                sz += i;
                                if(sz==Integer.parseInt(fileSize)) break;
                            }
                            //out.write(b);
                        }
                    }
                    System.out.println("Send File to: ");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void printbits(byte[] b){
        for(int i=0;i<b.length;i++){
            int m = 1<<7;
            while(m>0){
                if(((int)b[i]&m)==0) System.out.print("0");
                else System.out.print("1");
                m=m>>1;
            }
            System.out.print(" ");
        }
        System.out.println("");
    }
    public void printbits(ArrayList<Byte> b){
        for(int i=0;i<b.size();i++){
            int m = 1<<7;
            while(m>0){
                if(((int)b.get(i)&m)==0) System.out.print("0");
                else System.out.print("1");
                m=m>>1;
            }
            System.out.print(" ");
        }
        System.out.println("");
    }

    public byte[] getFrame(ArrayList<Byte> c,int x){
        int l = c.size() +6;
        byte[] frame = new byte[l];
        frame[0] = (byte)126;
        frame[1] = (byte)0;
        frame[2] = (byte)x;
        frame[3] = (byte)0;
        frame[l-1] = (byte)126;
        frame[l-2] = (byte)checkSum(c);
        for(int i =0;i<c.size();i++){
            frame[i+4] = c.get(i);
        }
        return frame;
    }

    public int checkSum(ArrayList<Byte> c){
        int oneCnt = 0;
        for(int i=0;i<c.size();i++){
            int m = 1<<7;
            while(m>0){
                if(((int)c.get(i)&m)!=0) oneCnt++;
                m = m>>1;
            }
        }
        return oneCnt;
    }

    public ArrayList <Byte> bitStuffing(byte[] b, int x){
        int cnt = 0,oneCnt = 0,curByte;
        ArrayList <Byte> c = new ArrayList<Byte>();
        curByte = 0;
        for(int i=0;i<x;i++){
            int m = 1<<7;
            while(m>0){
                if(((int)b[i]&m)==0){
                    oneCnt = 0;
                    cnt++;
                }
                else{
                    oneCnt++;
                    cnt++;
                    curByte = curByte | 1<<(8-cnt);
                    if(oneCnt == 5){
                        oneCnt = 0;
                        cnt++;
                        if(cnt==9){
                            c.add((byte)curByte);
                            curByte = 0;
                            cnt = 1;
                        }
                    }
                }
                if(cnt == 8){
                    c.add((byte)curByte);
                    cnt = 0;
                    curByte = 0;
                }
                m = m>>1;
            }
        }
        if(cnt%8!=0) c.add((byte)curByte);
        return c;
    }


   /* public void getInput(Socket connectionSocket) throws Exception
    {

        str = "";
        TimerTask task = new TimerTask()
        {
            public void run()
            {
                if( str.equals("") )
                {
                    System.out.println( "Server timeout" );
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return;

            }
        };
        Timer timer = new Timer();
        timer.schedule( task, 10*1000 );

        InputStream in = connectionSocket.getInputStream();
        String str1;
        ackCnt = 0;
        for(int k=0;k<3;k++){
            byte [] b = new byte[1];
            ArrayList <Byte> acks = new ArrayList<Byte>();
            int i,j=0;
            while ((i = in.read(b)) > -1){
                //  System.out.println("ekhane dhuke to");
                acks.add(b[0]);
                if(b[0]==126){
                    if(j==0) j = 1;
                    else break;
                }
            }
            //System.out.println("ekhaneo Ashe");
            str1 = "Acknowledge no " + Integer.toString((int)acks.get(3)) + " received";
            System.out.println(str1);
            ackCnt++;
        }
        str = "N acks found";
        timer.cancel();
        System.out.println( "A chunk has been sent");
    }*/

    public byte[] genError(byte[] frame){
        frame[frame.length-2] = (byte)(frame[frame.length-2] +1) ;
        return frame;
    }

    public byte[] correctError(byte[] frame){
        frame[frame.length-2] = (byte)(frame[frame.length-2] -1) ;
        return frame;
    }

    public void getInput(Socket connectionSocket) throws Exception
    {
        timeOut = 0;
        str = "";
        TimerTask task = new TimerTask()
        {
            public void run()
            {
                if( str.equals("") )
                {
                    System.out.println( "Server timeout" );
                    timeOut = 1;
                }

            }
        };
        Timer timer = new Timer();
        timer.schedule( task, 4*1000 );

        InputStream in = connectionSocket.getInputStream();
        String str1;
        ackCnt = 0;
        for(int k=0;k<3;k++){
            while(timeOut==0 && in.available()==0){}
            if(timeOut == 1) break;
            byte [] b = new byte[1];
            ArrayList <Byte> acks = new ArrayList<Byte>();
            int i,j=0;
            while ((i = in.read(b)) > -1){
                //  System.out.println("ekhane dhuke to");
                acks.add(b[0]);
                if(b[0]==126){
                    if(j==0) j = 1;
                    else break;
                }
            }
            //System.out.println("ekhaneo Ashe");
            str1 = "Acknowledge no " + Integer.toString((int)acks.get(3)) + " received";
            System.out.println(str1);
            ackCnt++;
        }
        str = "N acks found";
        timer.cancel();

    }


}