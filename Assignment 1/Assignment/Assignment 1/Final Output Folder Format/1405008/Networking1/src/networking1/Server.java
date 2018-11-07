/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package networking1;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author Arup
 */
public class Server {

    public byte[] arr = new byte[102400];
    public static int available = 102400;
    public static String[] server_temp = new String[1000];
    public static int transmission_no = 1;
    public static ArrayList<String> currentUser = new ArrayList<String>();
    public static ArrayList<WorkerThread> currentThread = new ArrayList<WorkerThread>();

    public synchronized static int byteArrayToInt(byte[] b) {
        return b[3] & 0xFF
                | (b[2] & 0xFF) << 8
                | (b[1] & 0xFF) << 16
                | (b[0] & 0xFF) << 24;
    }

    public synchronized static byte[] intToByteArray(int a) {
        return new byte[]{
            (byte) ((a >> 24) & 0xFF),
            (byte) ((a >> 16) & 0xFF),
            (byte) ((a >> 8) & 0xFF),
            (byte) (a & 0xFF)
        };
    }

    public static void main(String[] args) throws Exception {
        int id = 0;
        int count = 0;
        String s1;
        String s2;
        String s3;

        ServerSocket welcomeSocket = new ServerSocket(5678);
        while (true) {
            Socket connectionSocket = welcomeSocket.accept();

            BufferedReader inFromClient = new BufferedReader(
                    new InputStreamReader(connectionSocket.getInputStream()));
            DataOutputStream toClient = new DataOutputStream(connectionSocket.getOutputStream());

            s1 = inFromClient.readLine();

            if (Server.currentUser.contains(s1)) {

                toClient.writeBytes("You are already logged in from another account" + '\n');
            } else {
                toClient.writeBytes("yes" + '\n');
                currentUser.add(s1);
                WorkerThread wt = new WorkerThread(connectionSocket, id, s1);
                currentThread.add(wt);
                Thread t = new Thread(wt);
                t.start();
                count++;
                System.out.println("Client : " + id + "got connected. Total threads : " + count);
                id++;

            }

        }
        // TODO code application logic here
    }

}

class WorkerThread implements Runnable {

    public int id;
    public Socket connectionSocket;
    public String roll;
    public int receiver;

    public BufferedReader inFromClient;
    DataOutputStream toClient;
    DataInputStream ClientPacket;

    public WorkerThread(Socket connectionSocket, int id, String roll) {
        this.id = id;
        this.connectionSocket = connectionSocket;
        this.roll = roll;

    }

    public int getBit(byte ID, int position) {
        return (ID >> position) & 1;
    }

    public int checkSum(byte[] bytes) {
        int count = 0;
        for (int i = 0; i < bytes.length; i++) {
            byte num = bytes[i];
            for (int j = 0; j < 8; j++) {
                if (getBit(num, j) == 1) {
                    count++;
                }
            }
        }
        return count;
    }

    public byte[] bitStaffing(byte[] bytes) {
        int count = 0;
        int j = 0;
        int local_count = 0;
        int last = 0;
        byte[] byte2 = new byte[100];
        for (int i = 0; i < bytes.length; i++) {
            for (int x = 0; x < 8; x++) {
                if (x == 7 && i == bytes.length - 1) {
                    last = 1;
                }
                if (getBit(bytes[i], x) == 1) {
                    count++;
                    if (count == 5) {
                        byte2[j] = (byte) (byte2[j] | (1 << local_count));
                        local_count++;
                        if (local_count == 8) {
                            j++;
                            local_count = 0;
                        }

                        local_count++;
                        if (local_count == 8 && last != 1) {
                            j++;
                            local_count = 0;
                        }
                        count = 0;
                    } else {
                        byte2[j] = (byte) (byte2[j] | (1 << local_count));
                        local_count++;
                        if (local_count == 8 && last != 1) {
                            j++;
                            local_count = 0;
                        }

                    }
                } else {
                    local_count++;
                    if (local_count == 8 && last != 1) {
                        j++;
                        local_count = 0;
                    }
                    count = 0;
                }
            }
        }

        byte[] byte3 = new byte[j + 1];
        for (int x = 0; x < j + 1; x++) {
            byte3[x] = byte2[x];
        }
        return byte3;
    }

    public byte[] bitDeStaffing(byte[] bytes) {
        int count = 0;
        int local_count = 0;
        int j = 0;
        int last = 0;
        int stuffedBit = 0;

        byte[] byte2 = new byte[bytes.length + 1];

        for (int i = 0; i < bytes.length; i++) {
            for (int x = 0; x < 8; x++) {
                if (x == 7 && i == bytes.length - 1) {
                    last = 1;
                }
                if (getBit(bytes[i], x) == 1) {
                    count++;
                    if (count == 5) {
                        byte2[j] = (byte) (byte2[j] | (1 << local_count));
                        local_count++;
                        if (local_count == 8 && last != 1) {
                            j++;
                            local_count = 0;
                        }
                        count = 0;

                        x++;
                        if (x == 8) {
                            x = 0;
                            i++;
                        }
                        stuffedBit++;
                    } else {
                        byte2[j] = (byte) (byte2[j] | (1 << local_count));
                        local_count++;
                        if (local_count == 8 && last != 1) {
                            j++;
                            local_count = 0;
                        }

                    }
                } else {
                    local_count++;
                    if (local_count == 8 && last != 1) {
                        j++;
                        local_count = 0;
                    }
                    count = 0;
                }
            }
        }

        int numToRemove;

        if ((stuffedBit % 8) != 0) {
            numToRemove = (stuffedBit / 8) + 1;
        } else {
            numToRemove = (stuffedBit / 8);
        }
        byte[] byte3 = new byte[bytes.length - numToRemove];
        for (int x = 0; x < bytes.length - numToRemove; x++) {
            byte3[x] = byte2[x];
        }
        return byte3;

    }

    public byte[] arrayConcatenate(byte[] byte1, byte[] byte2, byte[] byte3) {
        int length = byte1.length + byte2.length + byte3.length;
        byte[] newArray = new byte[length];
        for (int i = 0; i < byte3.length; i++) {
            newArray[i] = byte3[i];
        }
        int j = 0;
        for (int i = byte3.length; i < byte3.length + byte2.length; i++) {
            newArray[i] = byte2[j];
            j++;
        }
        j = 0;
        for (int i = byte3.length + byte2.length; i < length; i++) {
            newArray[i] = byte1[j];
            j++;
        }
        return newArray;
    }

    public byte[] removeFlag(byte[] bytes) {
        byte[] byte2 = new byte[bytes.length];
        int j = 0;
        for (int i = 1; i < bytes.length; i++) {
            if ((bytes[i] & 0xFF) != 126) {
                byte2[j] = bytes[i];
                j++;
            } else {
                break;
            }
        }
        byte[] byte3 = new byte[j];
        for (int x = 0; x < j; x++) {
            byte3[x] = byte2[x];
        }
        return byte3;
    }

    public byte[][] ExtractData(byte[] bytes) {
        byte[][] byte2 = new byte[10][100];
        int j = -1;
        int k = 0;
        int size[] = new int[100];
        int count = 0;
        for (int i = 0; i < bytes.length; i++) {
            if (count % 2 == 0) {
                if ((bytes[i] & 0xFF) != 126) {
                    break;
                } else {
                    count++;
                    j++;
                    k = 0;
                }

            } else if ((bytes[i] & 0xFF) == 126) {
                count++;
                size[j] = k;
            } else {
                byte2[j][k] = bytes[i];
                k++;
            }
        }
        byte[][] byte3 = new byte[j + 1][];
        for (int i = 0; i < j + 1; i++) {
            byte3[i] = new byte[size[i]];
            for (int x = 0; x < size[i]; x++) {
                byte3[i][x] = byte2[i][x];
            }
        }
        return byte3;

    }

    public void Print(byte[] bytes) {

        for (int i = 0; i < bytes.length; i++) {
            System.out.println(Integer.toBinaryString(bytes[i] & 255 | 256).substring(1));
        }
        System.out.println("");
    }

    public byte[] sendAbleByte(byte[] bytes, int i) {

        int c_sum = checkSum(bytes);
        byte[] serial = new byte[1];
        serial[0] = (byte) i;
        byte[] check_sum = new byte[1];
        check_sum[0] = (byte) c_sum;
        byte[] unstuffed = arrayConcatenate(serial, bytes, check_sum);
        byte[] stuffed = bitStaffing(unstuffed);
        int p = 126;
        byte[] flag = new byte[1];
        flag[0] = (byte) p;
        byte[] send = arrayConcatenate(flag, stuffed, flag);
        return send;

    }
    //this function is responsible for sending file from server to receiver

    public synchronized int send(int trans_id, String filename, int siz, String sender, int receiver, int chunk_size) throws FileNotFoundException, IOException {

        toClient.writeBytes(sender + '\n');
        toClient.writeBytes(filename + '\n');
        toClient.writeBytes("" + siz + '\n');
        String str = inFromClient.readLine();
        if (str.equals("yes")) {

            toClient.writeBytes("" + chunk_size + '\n');

            FileInputStream in = new FileInputStream(filename);
            //sending by chunk
            DataInputStream clientAck = new DataInputStream(connectionSocket.getInputStream());
            connectionSocket.setSoTimeout(30000);  //set time out to 30 second

            //sending by chunk
            long numSent = 0;
            int loop = (int) (siz / chunk_size);
            if (siz % chunk_size != 0) {
                loop++;
            }

            int goBackN = 3;
            byte[][] bytesArray = new byte[loop][];
            int sendThisTime;

            for (int i = 1; i <= loop; i++) {
                long numThisTime = siz - numSent;
                if (numThisTime > chunk_size) {
                    numThisTime = chunk_size;
                }
                byte[] bytes = new byte[(int) numThisTime];
                //bytesArray[arraySerial]=new byte[(int) numThisTime];
                int numRead = in.read(bytes, 0, (int) numThisTime);

                bytesArray[i - 1] = sendAbleByte(bytes, i);

                numSent = numSent + numRead;
            }

            int sent = 0;
            while (true) {
                if ((sent >= loop)) {
                    break;
                } else {
                    sendThisTime = (loop - sent);
                    if (sendThisTime > goBackN) {
                        sendThisTime = goBackN;
                    }
                    sendThisTime = sendThisTime + sent;
                }

                for (int i = sent; i < sendThisTime; i++) {
                    try {
                        toClient.write(bytesArray[i], 0, bytesArray[i].length);
                    } catch (IOException ex) {

                    }
                }
                try {
                    for (int i = sent; i < sendThisTime; i++) {
                        byte[] b = new byte[3];
                        int byteRead = clientAck.read(b, 0, 3);
                        //b = removeFlag(b);
                        sent++;
                        int num = b[1] & 0xFF;
                        System.out.println("paichi " + num);
                    }
                } catch (IOException ex) {

                }
            }

            in.close();
            connectionSocket.setSoTimeout(Integer.MAX_VALUE);

            new File(filename).delete();
            return 1;
        } else {
            return 0;
        }

    }

    @Override
    public void run() {

        String s1;
        String s2;
        String filename;
        int size;
        int trans_id;
        try {
            inFromClient = new BufferedReader(
                    new InputStreamReader(connectionSocket.getInputStream()));
            toClient = new DataOutputStream(connectionSocket.getOutputStream());
            ClientPacket = new DataInputStream(connectionSocket.getInputStream());
            while (true) {
                System.out.println("server started");
                s1 = inFromClient.readLine();  //client's reply of Do you want to send file? type yes to send file, else type no
                System.out.println(s1);
                if (s1.equals("yes")) {
                    s1 = inFromClient.readLine(); //server get receiver ID
                    System.out.println(s1);
                    if (Server.currentUser.contains(s1) && !Server.currentThread.get(Server.currentUser.indexOf(s1)).connectionSocket.isClosed()) {
                        toClient.writeBytes("yes" + '\n');
                        receiver = Server.currentUser.indexOf(s1);

                        filename = inFromClient.readLine();
                        s2 = inFromClient.readLine();    //gets file size
                        size = Integer.parseInt(s2);

                        //check file size and generate chunk size
                        if (size < Server.available) {
                            Random rand = new Random();
                            int chunk_size;
                            if(size>50)chunk_size= rand.nextInt(31) +1;
                            else chunk_size= rand.nextInt(size/4) +2;
                                 
                            System.out.println("chunk: " + chunk_size);
                            toClient.writeBytes("" + chunk_size + '\n');
                            toClient.writeBytes("" + Server.transmission_no + '\n');
                            trans_id = Server.transmission_no;
                            Server.transmission_no++;

                            //temporarily store file in server file storage
                            FileOutputStream fos = new FileOutputStream(filename);
                            int bytesReceived = 0;

                            int loop = (int) (size / chunk_size);
                            if (size % chunk_size != 0) {
                                loop++;
                            }
                            int byteRead = -1;
                            connectionSocket.setSoTimeout(30000);
                            int dataExpected = 1;

                            int received = 0;
                            while (true) {
                                if (received >= loop) {
                                    break;
                                }

                                byte[] bytes = new byte[120];
                                try {
                                    System.out.println("waiting");

                                    byteRead = ClientPacket.read(bytes, 0, 150);
                                    System.out.println("paichi");

                                } catch (InterruptedIOException iioe) {
                                    System.out.println(iioe);
                                    continue;
                                }
                                byte[][] allData = ExtractData(bytes);
                                for (int x = 0; x < allData.length; x++) {

                                    byte[] stuffed = allData[x];
                                    Print(stuffed);

                                    byte[] deStuffed = bitDeStaffing(stuffed);
                                    byte b = deStuffed[0];
                                    //Print(deStuffed);
                                    int checksum = b & 0xFF;
                                    //System.out.println("checksum:"+checksum);
                                    b = deStuffed[deStuffed.length - 1];
                                    int serialNo = b & 0xFF;
                                    // System.out.println("serial:"+serialNo);
                                    byte[] data = new byte[deStuffed.length - 2];
                                    for (int p = 1; p < deStuffed.length - 1; p++) {
                                        data[p - 1] = deStuffed[p];
                                    }
                                    // String s = new String(data);
                                    // System.out.println("data: "+s);
                                    if (data.length <= chunk_size) {
                                        if (serialNo == dataExpected) {
                                            if (checksum == checkSum(data)) {
                                                fos.write(data);
                                                bytesReceived = bytesReceived + data.length;
                                                Server.available = Server.available - data.length;
                                                int fg = 126;
                                                byte[] flag = new byte[1];
                                                flag[0] = (byte) fg;
                                                byte[] serial = new byte[1];
                                                serial[0] = b;
                                                byte[] acknowledge = arrayConcatenate(flag, serial, flag);
                                                toClient.write(acknowledge, 0, acknowledge.length);
                                                Print(acknowledge);
                                                byteRead = -1;
                                                dataExpected++;
                                                received++;
                                            } else {
                                                System.out.println("Bit is corrupted in packet: " + serialNo);

                                            }
                                        } else {
                                            System.out.println("seial: " + dataExpected + " not reached");
                                        }
                                    } else {
                                        new File(filename).delete();
                                        break;
                                    }
                                }

                            }
                            fos.close();
                            connectionSocket.setSoTimeout(Integer.MAX_VALUE);

                            String st = inFromClient.readLine();
                            System.out.println(bytesReceived);

                            //checks file size is as told before
                            if (bytesReceived != size) {
                                //file.delete();
                                new File(filename).delete();
                                toClient.writeBytes("failed" + '\n');
                                continue;
                            } else {
                                toClient.writeBytes("success" + '\n');

                                WorkerThread wthread = Server.currentThread.get(receiver);
                                wthread.send(trans_id, filename, size, roll, receiver, chunk_size);

                                new File(filename).delete();

                                Server.available = Server.available + bytesReceived;
                                System.out.println("transfer completed");
                                toClient.writeBytes("yes" + '\n');
                            }

                        } else {
                            toClient.writeBytes("There is not enough storage" + '\n');
                        }
                    } else {
                        if (Server.currentUser.contains(s1)) {
                            int i = Server.currentUser.indexOf(s1);
                            Server.currentUser.set(i, null);
                            Server.currentThread.set(i, null);
                        }
                        toClient.writeBytes("Client offline" + '\n');
                    }
                } else {

                    break;
                }
                System.out.println("currently connected: ");
                for (int i = 0; i < Server.currentUser.size(); i++) {
                    System.out.println(Server.currentUser.get(i));
                }
            }
        } catch (Exception e) {

        }
    }

}
