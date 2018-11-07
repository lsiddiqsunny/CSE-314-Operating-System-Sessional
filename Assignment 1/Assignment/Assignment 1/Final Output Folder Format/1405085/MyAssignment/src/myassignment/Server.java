/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package myassignment;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {

    public static Hashtable<String, Socket> hashMap = new Hashtable<>();

    public static Hashtable<String, FileCopy> fileIdMap = new Hashtable<>();

    public static Hashtable<String, FileInfo> fileMap = new Hashtable<>();

    public static final long MAX_SIZE = 10000;

    public static long availableMemory = MAX_SIZE;

    public static synchronized long changeAvlMemory(long x) {

        availableMemory -= x;
        return availableMemory;

    }

    public static synchronized void addUsedMemory(long x) {
        availableMemory += x;
    }

    public static synchronized long getAvailableMemory(long x) {
        return availableMemory;
    }

    public static void main(String args[]) throws Exception {

        ServerSocket welcomeSocket = new ServerSocket(6066);
        while (true) {
            Socket connectionSocket = welcomeSocket.accept();
            ServerWork wt = new ServerWork(connectionSocket);

            Thread t = new Thread(wt);
            t.start();

        }

    }

}

class ServerWork implements Runnable {

    Socket connectionSocket;
    byte[] tempArray;

    public ServerWork(Socket socket) {

        this.connectionSocket = socket;

    }

    public void run() {
        Response response;
        int downloadedCount;

        try {
            ObjectInputStream in = new ObjectInputStream(connectionSocket.getInputStream());

            ObjectOutputStream out = new ObjectOutputStream(connectionSocket.getOutputStream());

            ClientInfo client = (ClientInfo) in.readObject();

            if (client.id.length() == 7 && client.id.substring(0, 4).equals("1405") && Server.hashMap.get(client.id) == null) {
                Server.hashMap.put(client.id, connectionSocket);
                response = new Response(6, client.id);
                out.writeObject(response);
            } else if (Server.hashMap.get(client.id) != null) {
                response = new Response(8, client.id);
                out.writeObject(response);

            } else {
                response = new Response(7, client.id);
                out.writeObject(response);
            }

            //The code should be refactored  
            Object classObject = in.readObject();

            if (classObject.getClass().toString().equals("class myassignment.FileInfo")) {

                FileInfo fInfo = (FileInfo) classObject;

                if (Server.hashMap.get(fInfo.to) != null) {

                    FileSending sentFile;
                    long existingSize = fInfo.length;
                    String fId = fInfo.from + fInfo.to;
                    int i = 0;
                    while (Server.fileIdMap.get(fId + Integer.toString(i)) != null) {
                        i++;

                    }
                    fId = fId + Integer.toString(i);
                    fInfo.fileId = fId;

                    byte[] serverArray = new byte[(int) fInfo.length];
                    int stdId = Integer.parseInt(fInfo.to.substring(4));

                    //Server.serverMem[stdId] = new byte[(int) fInfo.length];
                    int downloadedAmount = 0,trackDownload = 0;

                    if (fInfo.length < Server.availableMemory) {

                        Server.changeAvlMemory(fInfo.length);

                        //int randomSize = (int) (fInfo.length / 10);
                        int randomSize = 32;
                        if (fInfo.length < 32) {
                            randomSize = (int) fInfo.length;
                        }
                        response = new Response(6, client.id, randomSize);

                        Object object;

                        out.writeObject(response);
                        int k = 0, errorAck = (int) Double.POSITIVE_INFINITY, errFlag = 0, lBound, ackNo = 0;
                        Acknowledgement acknowledgement;
                        long receivedSize = fInfo.length;

                        while (receivedSize > 0) {
                            k = 0;
                            errFlag = 0;
                            existingSize = receivedSize;
                            downloadedAmount = trackDownload;

                            while (k < 8 && existingSize > 0) {

                                object = in.readObject();

                                if (object.getClass().toString().equals("class myassignment.Response")) {

                                    Server.hashMap.remove(client.id);
                                    break;

                                } else {
                                    StringBuffer strBuilder = new StringBuffer();

                                    char[] charArray;
                                    sentFile = (FileSending) object;
                                    if (sentFile.length == 0) {
                                        System.out.println("The file should be deleted ");
                                        break;

                                    } else {
                                        System.out.println(sentFile.data[0] + " header " + sentFile.data[sentFile.length - 1]);
                                        if (sentFile.data[0] != 126 || sentFile.data[sentFile.length - 1] != 126) {

                                            System.out.println("Error in header ");
                                            break;
                                        }

                                        String result = "";

                                        for (int j = 4; j < sentFile.length - 1; j++) {
                                            int number = (int) (sentFile.data[j] & 0xff);
                                            String binaryString = Integer.toBinaryString(number);
                                            charArray = new char[8 - binaryString.length()];
                                            Arrays.fill(charArray, '0');
                                            String str = new String(charArray);
                                            strBuilder.append(str + binaryString);

                                        }
                                        System.out.println(strBuilder);

                                        strBuilder = DLLAssignment.destuffingBits(strBuilder);
                                        
                                        System.out.println("After Destuffing " + strBuilder.toString());
                                        if (strBuilder.length() % 8 != 0) {
                                            result = strBuilder.substring(0, strBuilder.length() - (strBuilder.length() % 8));

                                        } else {
                                            result = strBuilder.toString();

                                        }
                                        strBuilder = new StringBuffer(result);
                                        System.out.println("at last " + strBuilder.length());
                                        //System.out.println(strBuilder);

                                        boolean error = DLLAssignment.hasCheckSumError(strBuilder);
                                        
                                        tempArray = DLLAssignment.stringToByte(strBuilder.substring(0, strBuilder.length() - 8));
                                        if (error == false) {

                                            System.out.println("TempArray length :" + tempArray.length);
                                            System.out.println("DownloadedAmount " + downloadedAmount);

                                            System.arraycopy(tempArray, 0, serverArray, downloadedAmount, tempArray.length);

                                            downloadedAmount += tempArray.length;
                                            existingSize = existingSize - tempArray.length;
                                            ackNo++;
                                            //response = new Response(1, client.id);
                                            //out.writeObject(response);
                                        } else {

                                            existingSize -= tempArray.length;
                                            errorAck = Math.min(errorAck, k);
                                            System.out.println("Error occured at :" + errorAck + " frame ");
                                            errFlag = 1;

                                        }
                                        k++;

                                    }

                                }
                            }
                            if (errFlag == 1) {
                                lBound = errorAck;
                            } else {
                                lBound = k - 1;
                            }
                            for (int lCounter = 0; lCounter <= lBound; lCounter++) {
                                // if(errFlag == 1 && lCounter)
                                if (errFlag == 1 && lCounter == lBound) {
                                    acknowledgement = new Acknowledgement(ackNo, ackNo, 1);
                                    acknowledgement.createAcknowledgement();
                                    out.writeObject(acknowledgement);
                                    System.out.println("error occurred frame" + lBound);
                                } else {
                                    acknowledgement = new Acknowledgement(ackNo, ackNo, 0);
                                    acknowledgement.createAcknowledgement();
                                    trackDownload += 32;
                                    receivedSize -= 32;
                                    out.writeObject(acknowledgement);
                                    System.out.println("ack No : " + ackNo + " lBound :" + lBound);
                                }
                            }

                        }
                        /* File file = new File("/home/antu/Desktop/a.txt");
                        if (!file.exists()) {
                            file.createNewFile();
                        }

                        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                        bos.write(serverArray);
                        bos.flush();
                        bos.close();*/
                        Server.fileMap.put(fInfo.to, fInfo);
                        FileCopy fCopy = new FileCopy(fInfo.file, serverArray);

                        Server.fileIdMap.put(fId, fCopy);
                        System.out.println("Finished from server " + connectionSocket.getLocalSocketAddress());

                        connectionSocket.close();

                    }
                } else {
                    response = new Response(7, client.id);
                    out.writeObject(response);
                    System.out.println("The server is not able to receive any file");

                }
            } else if (classObject.getClass().toString().equals("class myassignment.Response")) {
                SendToReceiver sendToReceiver = new SendToReceiver((Response) classObject, in, out);
                Thread thread = new Thread(sendToReceiver);
                thread.start();

            }
        } catch (Exception ex) {
            Logger.getLogger(ServerWork.class.getName()).log(Level.SEVERE, null, ex);

        }

    }
}
