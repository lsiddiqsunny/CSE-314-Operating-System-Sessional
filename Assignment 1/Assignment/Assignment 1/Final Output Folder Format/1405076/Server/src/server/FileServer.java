/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Shariar Kabir
 */
public class FileServer implements Runnable {

    /**
     * @param args the command line arguments
     */
    public final int buffersize = 15000 * 1024;//153176;
    public static FileServer thisServer = new FileServer();
    public static ArrayList<User> userList = new ArrayList<>();
    public static ArrayList<User> recipients = new ArrayList<>();
    public static ArrayList<Files> files = new ArrayList<>();
    public static byte[] buffer;
    public static int bufferEnd;
    Socket currentSocket;

    BufferedReader inToServer;
    DataOutputStream outStream;
    DataInputStream inStream;
    int id = 0;

    public FileServer(Socket sock) {
        try {
            currentSocket = sock;
            inToServer = new BufferedReader(new InputStreamReader(currentSocket.getInputStream()));
            outStream = new DataOutputStream(currentSocket.getOutputStream());
            inStream = new DataInputStream(currentSocket.getInputStream());
            buffer = new byte[buffersize];
            bufferEnd = 0;
        } catch (IOException ex) {
            System.out.println("server.FileServer.<init>()");
        }
    }

    private FileServer() {

    }

    public String userName(Socket socket) {
        for (User u : userList) {
            if (u.socket == socket) {
                return u.userID;
            }
        }
        return null;
    }

    public boolean userExists(String user, ArrayList<User> alist) {
        for (User u : alist) {
            if (u.userID.equals(user)) {
                return true;
            }
        }
        return false;
    }

    public Socket getSocket(String user) {
        for (User u : userList) {
            if (u.userID.equals(user)) {
                return u.socket;
            }
        }
        return null;
    }

    public void printUsers(ArrayList<User> aList) {

        for (User u : aList) {
            System.out.println(u.userID);
        }

    }

    public void writeFile(String fileName, int startInd, int filesize) {
        try {
            FileOutputStream fos = new FileOutputStream(new File(fileName));
            fos.write(buffer, startInd, filesize);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FileServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(FileServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public int getStartIndex(String file) {
        for (Files f : files) {
            if (f.fileName.equals(file)) {
                return f.startInd;
            }

        }

        return -1;
    }

    public String getFileName(String receiver) {
        for (Files f : files) {
            if (f.receiver.equals(receiver)) {
                return f.fileName;
            }

        }

        return null;
    }

    public long getFileSize(String fileName) {
        for (Files f : files) {
            if (f.fileName.equals(fileName)) {
                return f.fileSize;
            }

        }

        return -1;
    }

    public void removeUser(String userID, ArrayList<User> aList) {
        for (int i = 0; i < aList.size(); i++) {
            if (aList.get(i).userID.equals(userID)) {
                aList.remove(i);
            }
        }
    }

    public void removeFile(String fileName) {

        int stInd = getStartIndex(fileName);
        long size = getFileSize(fileName);
        for (int i = stInd; i < buffersize - size; i++) {
            buffer[i] = buffer[i + (int) size];
        }
        bufferEnd -= size;
        int fl = 0;
        for (int i = 0; i < files.size(); i++) {
            if (fl == 1) {
                files.get(i).startInd -= size;
            }
            if (files.get(i).fileName.equals(fileName)) {
                fl = 1;
            }
        }
        for (int i = 0; i < files.size(); i++) {
            if (files.get(i).fileName.equals(fileName)) {
                files.remove(i);
            }
        }
    }

 /*   public void deleteBitfiles(int seq) {
        int cur = 0;
        System.out.println("number of bitfiles : "+seq);
        while(cur < seq) {
            File file = new File("Bitfile" + cur + ".txt");
            file.delete();
            cur++;
        }
    }*/

    public void FilefromFrame(int seq, String filename) throws FileNotFoundException, IOException {

        File file = new File(filename);
        file.delete();
        
        FileOutputStream fos = new FileOutputStream(file, true);
        int cur = 0;
        while (cur < seq) {
            File bitFile = new File("Bitfile" + cur + ".txt");

            FileReader fr = new FileReader(bitFile);
            BufferedReader br = new BufferedReader(fr);
            String line = br.readLine();
            
            System.out.println("Read bits " + line.length());
            int size = line.length() / 8;
            byte[] buffer = new byte[size];
            int st = 0;
            int en = 8;
            int temp;
            for (int i = 0; i < size; i++) {
                temp = Integer.parseInt(line.substring(st, en), 2);
                buffer[i] = (byte) temp;
                st = en;
                en += 8;

            }

            fos.write(buffer);

            fos.flush();
            cur++;
        }
        fos.close();

    }
    public int getFrameSequence(String frame){
        int seq = Integer.parseInt(frame.substring(8, 16), 2);
        return seq;
    }
    public boolean writeBites(String frame) throws FileNotFoundException, IOException {
        //CheckSum
        int num = 0;
        for (int i = 0; i < frame.length() - 8; i++) {
            if (frame.charAt(i) == '1') {
                num++;
            }
        }
        String checksum = frame.substring(frame.length() - 8, frame.length());
        System.out.println("1's : " + num);
        int chksm = Integer.parseInt(checksum, 2);
        System.out.println(chksm);

        num += chksm;
        byte ch = (byte) ((num % 256) + (num >> 8));

        String check = String.format("%8s", Integer.toBinaryString(~ch & 0xFF)).replace(' ', '0');
        System.out.println("CheckSum: " + check);
        
        byte res = (byte) Integer.parseInt(check, 2);

        
        if (res == 0) {
            String st = frame.substring(24, frame.length() - 8);
            int seq = Integer.parseInt(frame.substring(8, 16), 2);
            FileOutputStream fos = new FileOutputStream(new File("Bitfile" + seq + ".txt"));
            fos.write(st.getBytes());
            fos.flush();
            return true;
        } else {
            return false;
        }
    }

    public String deStuff(String frame) {

        int count = 0;

        for (int i = 0; i < frame.length() - 2; i++) {
            if (frame.charAt(i) == '0') {
                count = 0;
            } else {
                count++;
            }
            if (count == 5) {
                if (frame.charAt(i + 1) == '1' && frame.charAt(i + 2) == '0') {
                    break;
                }
                frame = frame.substring(0, i + 1) + frame.substring(i + 2, frame.length());
                count = 0;
            }
        }
        //
        frame = frame.substring(0, frame.length() - 8);
        System.out.println("Frame Length after deStuffing : " + frame.length());
        return frame;
    }

    public void getFile(Socket socket) throws IOException {

        String recipientID = inToServer.readLine();
        System.out.println("Reciever ID " + recipientID);
        printUsers(userList);
        if (userExists(recipientID, userList)) {
            outStream.writeBytes("1" + '\n');

            String filename = inStream.readLine();
            long filesize = inStream.readLong();
            System.out.println("FileName" + filename + "filesize : " + filesize);

            if (bufferEnd + filesize < buffersize) {
                outStream.writeBytes("1" + '\n');
                recipients.add(new User(recipientID, getSocket(recipientID)));
                files.add(new Files(filename, recipientID, bufferEnd, filesize));
                int startInd = bufferEnd;
                int read = 0;

                int chunkSize = (int) Math.max(100, filesize/126);//random.nextInt(15*1024) + min;
                outStream.writeInt(chunkSize);
                long remaining = filesize;
                int expectedseq = 0;
                while (true) {
                    String frame = inStream.readLine();
                    if (!frame.equals("end")) {
                        
                        System.out.println("Frame length: " + frame.length());
                        frame = deStuff(frame);
                        int currentseq=getFrameSequence(frame);
                        try {
                            TimeUnit.SECONDS.sleep(1);
                            if(writeBites(frame)&&currentseq==expectedseq){
                                outStream.writeBytes("frameOk"+'\n');
                                expectedseq++;
                            }else{
                                outStream.writeBytes("error"+'\n');
                            }
                            outStream.writeInt(expectedseq);

                        } catch (Exception e) {
                            Logger.getLogger(FileServer.class.getName()).log(Level.SEVERE, null, e);
                        }
                        read += frame.length() / 8;
                        remaining -= read;
                        
                    } else {
                        break;
                    }
                }
                String s = inStream.readLine();
                //System.out.println("Client says" + s);
                try {
                    FilefromFrame(expectedseq, filename);
                } catch (Exception ex) {
                    System.out.println("server.FileServer.sendFile()");
                }
            //    deleteBitfiles(expectedseq);
                System.out.println("File recieved.");
                System.out.println("Total size: " + bufferEnd);

                String st = inStream.readLine();
                //System.out.println("Client says" + st);
            } else {
                outStream.writeBytes("0" + '\n');
            }

        } else {
            outStream.writeBytes("0" + '\n');
        }

    }

    public void sendFile(Socket s) throws IOException {

        outStream = new DataOutputStream(s.getOutputStream());
        String receiver = userName(s);
        if (userExists(receiver, recipients)) {

            outStream.writeBytes("1" + '\n');

            String fileName = getFileName(receiver);

            int fileSize = (int) getFileSize(fileName);
            
            outStream.writeBytes(fileName + '\n');
            outStream.writeLong(fileSize);
            
            String msg = inStream.readLine();
            System.out.println("msg in server " + msg);
            if (msg.equals("Y") || msg.equals("y")) {

                int sent = 0;
                int chunkSize = 100;
                int seq = 0;
                byte[] sendingBuffer=new byte[chunkSize];
                FileInputStream fis = new FileInputStream(fileName);
                while (fis.read(sendingBuffer) > 0) {
                    sent += sendingBuffer.length;
                    outStream.write(sendingBuffer);
                    System.out.println("sent : " + sent);
                }
                outStream.flush();

                outStream.writeBytes("ok" + '\n');

                System.out.println("Sending " + fileName + " complete");
                removeFile(fileName);
                removeUser(receiver, recipients);
                outStream.writeBytes("Done" + '\n');
            } else if (msg.equals("N") || msg.equals("n")) {
                removeFile(fileName);
            }

        } else {
            outStream.writeBytes("0" + '\n');
        }
    }

    @Override
    public void run() {
        String clientID = null;
        String actDecision = null;

        try {
            inToServer = new BufferedReader(new InputStreamReader(currentSocket.getInputStream()));
            outStream = new DataOutputStream(currentSocket.getOutputStream());
            clientID = inToServer.readLine();
            if (userExists(clientID, userList)) {
                outStream.writeBytes("0" + '\n');
            } else {
                outStream.writeBytes("1" + '\n');
                userList.add(new User(clientID, currentSocket));
                printUsers(userList);
                while (true) {
                    actDecision = inToServer.readLine();
                    if (actDecision.equals("S") || actDecision.equals("s")) {
                        getFile(currentSocket);
                    } else if (actDecision.equals("R") || actDecision.equals("r")) {
                        sendFile(currentSocket);
                    } else {
                        //  System.out.println("...........");
                        //removeUser(clientID, userList); //Remove the user if any malicious command is found
                        //removeUser(clientID, recipients);
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("Client " + userName(currentSocket) + " has left");
            removeUser(clientID, userList);
            removeUser(clientID, recipients);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) throws IOException {

        ServerSocket server = new ServerSocket(7789);
        while (true) {
            Socket current = server.accept();

            Thread t = new Thread(new FileServer(current));
            t.start();

            System.out.println("Client [" + thisServer.id + "] is now connected.");

            thisServer.id++;

        }
    }

}
