
import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Main {
    public static void main(String argv[]) throws Exception {
        int ip = 1;
        ServerSocket welcomeSocket = new ServerSocket(6789);
        //ServerSocket anotherSocket = new ServerSocket(1234);
        while (true) {
            Socket connectionSocket = welcomeSocket.accept();
            Socket receiverSocket = welcomeSocket.accept();
            WorkerThread wt = new WorkerThread(receiverSocket, connectionSocket, ip);
            Thread t = new Thread(wt);
            t.start();
            ip++;
        }

    }
}

class WorkerThread implements Runnable {
    private Socket connectionSocket;
    private Socket receiverSocket;
    ClientThreads clientThreads = ClientThreads.getClientThreads();
    private int ip;

    public WorkerThread(Socket receiverSocket, Socket ConnectionSocket, int ip) {
        this.receiverSocket = receiverSocket;
        this.connectionSocket = ConnectionSocket;
        this.ip = ip;
    }

    public void run() {
        String ID = null;
        DataOutputStream outToClient = null;
        BufferedReader inFromClient = null;
        try {
            outToClient = new DataOutputStream(connectionSocket.getOutputStream());
            InputStream din = connectionSocket.getInputStream();
            inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            ID = inFromClient.readLine();
            if (clientThreads.get(ID) != null) {
                outToClient.writeBytes("error\n");
                System.out.println(ID);
                return;
            }
            clientThreads.add(ip, this, ID);

            outToClient.writeBytes("Connected\n");
            System.out.println("Client with ID "+ID+" is now connected.");
            while (true)

            {
                System.out.println("newLoop");
               // String rc = inFromClient.readLine();
                //System.out.println(rc);
                //WorkerThread wt = clientThreads.get(rc);
                //if (wt == null) {
                  //  outToClient.writeBytes("NO\n");
                //}
                //else {
                    //outToClient.writeBytes("OK\n");
                    String fileName, fileSize;
                    fileName = inFromClient.readLine();
                    fileSize = inFromClient.readLine();
                    //System.out.println(fileName);
                    if (Integer.parseInt(fileSize)>7900) outToClient.writeBytes("error\n");
                    else {

                        //Integer chunkSize = Integer.parseInt(fileSize) / 10;
                        outToClient.writeBytes("not error\n");
                        InputStream in = connectionSocket.getInputStream();
                        System.out.println("OK");
                        byte[] b = new byte[1];

                        int i, sz = 0, j = 0, fno = 0, ackno = 0;
                        ArrayList<Byte> frame = new ArrayList <Byte>();
                        ArrayList<byte[]> frames = new ArrayList<byte[]>();
                    //System.out.println(in.read(b));
                        OutputStream ackout = connectionSocket.getOutputStream();

                        while ((i = in.read(b)) > -1) {
                            //out.write(b, 0, i);
                            //System.out.println(b[0]);
                            if((int)b[0]==-1) break;
                            frame.add(b[0]);
                            if((int)b[0] == 126){
                                if(j==0) j=1;
                                else{
                                    j = 0;
                                    System.out.println("Received frame "+(int)frame.get(2)+":");

                                    printbits(frame);
                                    if(hasCheckSumError(frame)){
                                        System.out.println("Error");
                                    }
                                    else{
                                        byte[] ack = new byte[5];
                                        ack[0] = (byte)126;
                                        ack[1] = (byte)0;
                                        ack[2] = (byte)fno;
                                        ack[3] = (byte)ackno;
                                        ack[4] = (byte)126;
                                        if(ackno>=(int)frame.get(2)){
                                            ackno = (int)frame.get(2);
                                            ackout.write(ack);
                                            ackout.flush();
                                            ackno++;
                                            System.out.println(ack[3]);
                                        }
                                        System.out.println("No Error");

                                    }

                                    if(ackno == (int)frame.get(2)+1) frames.add(deStuff(frame));
                                    //System.out.println(frames.size());
                                    //outToClient.writeBytes("frame got");
                                    frame.clear();

                                }
                            }
                            //printbits(b);
                        }
                        outToClient.writeBytes("\n");
                    System.out.println("dekk");
                    System.out.println(frames.size());
                OutputStream out = new FileOutputStream("ServerStorage/"+fileName );
                int size = 0;
                for(int k=0;k<frames.size();k++){
                            size += frames.get(k).length;

                    //System.out.println(size);
                            //printbits(frames.get(k));
                            out.write(frames.get(k));

                         }
                        //wt.send(fileName,ID,fileSize,chunkSize);
                        out.flush();
                    }

                }
            //}

        } catch (Exception e) {
            System.out.println("dasd");
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
    public byte[] deStuff(ArrayList <Byte> b){
        int oneCnt = 0, cnt = 0 , curByte = 0;
        ArrayList <Byte> c = new ArrayList<>();
        for(int i = 4;i<b.size()-2;i++){
            int m = 1<<7;
            while(m>0){
                if(((int)b.get(i)&m)==0){
                    oneCnt = 0;
                    cnt++;
                }
                else{
                    oneCnt++;
                    cnt++;
                    curByte = curByte | 1<<(8-cnt);
                    if(oneCnt == 5){
                        oneCnt = 0;
                        cnt--;
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
        byte[] d = new byte[c.size()];
        for(int i=0;i<c.size();i++){
            d[i] = c.get(i);
        }
        return d;
    }

    public boolean hasCheckSumError(ArrayList<Byte> b){
        int oneCnt = 0;
        for(int i = 4;i<b.size()-2;i++) {
            int m = 1 << 7;
            while (m > 0) {
                if (((int) b.get(i) & m) != 0) oneCnt++;
                m = m >> 1;
            }
        }

        if (oneCnt == ((int) b.get(b.size() - 2) & 0xff)) return false;
        else return true;
    }
    public void send(String fileName, String ID, String fileSize,int chunkSize) {
        DataOutputStream outToClient = null;
        BufferedReader inFromClient = null;
        try {
            outToClient = new DataOutputStream(receiverSocket.getOutputStream());
            inFromClient = new BufferedReader(new InputStreamReader(receiverSocket.getInputStream()));
            outToClient.writeBytes("FR\n");
            outToClient.writeBytes(fileName+'\n');
            outToClient.writeBytes(fileSize+'\n');
            outToClient.writeBytes(ID+'\n');
            String reply = inFromClient.readLine();

            if(reply.equals("Y")){
                byte[] b = new byte[chunkSize];
                File file = new File("ServerStorage/" + fileName);
                System.out.println(file.exists());
                InputStream in = new FileInputStream(file);
                OutputStream dout = receiverSocket.getOutputStream();
                int i;
                while((i=in.read(b))>0){
                    dout.write(b,0,i);
                }
                //file.delete();
                clientThreads.resetSize(Integer.parseInt(fileSize));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}