package ServerSide;

import ServerSide.ConnectionInfo;
import ServerSide.CreateServer;

import java.io.*;
import java.net.Socket;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

public class FetchAddress implements Runnable {
    Socket socket;
    String[][] data;
    String stID;
    boolean istransmitting;
    int fileID; //file index in th arraylist

    int frameID; //FrameID

    ObjectInputStream in;
    ObjectOutputStream out;
    ObjectOutputStream receiverOut;


    FetchAddress(Socket s, String [][] d) throws IOException {
        socket = s;
        data = d;
        istransmitting = false;
        //fileInfos = f;

        in = new ObjectInputStream(socket.getInputStream());
        out = new ObjectOutputStream(socket.getOutputStream());

        new Thread(this).start();

    }

    @Override
    public void run() {
        try {

            Object o = in.readObject();
            String clientIP = o.toString();


            o = in.readObject();
            stID = o.toString();


            System.out.println("IP Address: " + clientIP + " Student ID: " + stID);
            boolean flag = true;
            for (int j = 0; j < 5; j++) {
                if (data[j][0].compareTo(clientIP) != 0) {
                    for (int k = 0; k < 10 && data[j][k] != ""; k++) {
                        if (data[j][k].compareTo(stID) == 0) {
                            flag = false;
                            out.writeObject("Login Denied");
                        }
                    }
                }
            }
            if (flag) {
                int i;
                for (i = 0; i < 5; i++) {
                    if (data[i][0].compareTo(clientIP) == 0) {
                        for (int j = 1; j < 10; j++) {
                            if (data[i][j].compareTo(stID) == 0) {
                                break;
                            } else if (data[i][j].compareTo("") == 0 || data[i][j].compareTo("-1") == 0) {
                                data[i][j] = stID;
                                break;
                            }
                        }
                        break;
                    } else if (data[i][0].compareTo("") == 0) {
                        data[i][0] = clientIP;
                        data[i][1] = stID;
                        break;
                    }
                }
                out.writeObject("Successfully Logged In");
                print();


                ConnectionInfo newCon = new ConnectionInfo(in, out, stID);
                CreateServer.conInfos.add(newCon);


                while (true) {
                    String reply = in.readObject().toString(); //want to sent file?
                    if (reply.compareTo("y") == 0) {
                        String receiverID = in.readObject().toString();
                        if (isOnline(receiverID) == true) {
                            out.writeObject("online");

                            String fileName = in.readObject().toString();
                            System.out.println(fileName);

                            int fileSize = Integer.parseInt(in.readObject().toString());

                            for (int k = 0; k < CreateServer.conInfos.size(); k++) {
                                ConnectionInfo serverUtil = CreateServer.conInfos.get(k);
                                if (serverUtil.ID.compareTo(receiverID) == 0) {
                                    receiverOut = serverUtil.outputStream;
                                    break;
                                }
                            }
                            //request to receiver***later***
                            receiverOut.writeObject(stID);
                            receiverOut.writeObject(fileName);
                            receiverOut.writeObject(fileSize);

                            String dec = in.readObject().toString();
                            if(dec.compareTo("y")==0) {//receiver is ready to receive this file

                                Random random = new Random();
                                int chunkSize = random.nextInt(1024);
                                out.writeObject(chunkSize);
                                System.out.println("File Size: " + fileSize + " ChunkSize: " + chunkSize);

                                int noChunks, loop;
                                if (fileSize % chunkSize == 0) {
                                    noChunks = fileSize / chunkSize;
                                } else {
                                    noChunks = fileSize / chunkSize + 1;
                                }
                                if (noChunks % 8 == 0) {
                                    loop = noChunks / 8;
                                } else {
                                    loop = noChunks / 8 + 1;
                                }
                                //send receiver the number of chunks
                                receiverOut.writeObject(noChunks);

                                int totalSent = 0, tobeSent = 8;
                                String[] eightFrames = new String[8];
                                for (int k = 0; k < loop; k++) {
                                    tobeSent = Integer.parseInt(in.readObject().toString());
                                    int count = 0;
                                    for (int m = 0; m < tobeSent; m++) {
                                        //receive 8 frames
                                        eightFrames[m] = in.readObject().toString();

                                    }
                                    //send 8 frames to receiver
                                    receiverOut.writeObject(tobeSent);
                                    for (int m = 0; m < tobeSent; m++) {
                                        //send 8 frames
                                        receiverOut.writeObject(eightFrames[m]);
                                    }

                                    //sender will notify the number of acknowledgement it has received
                                    count = Integer.parseInt(in.readObject().toString());

                                    //retransmission part
                                    while (count < tobeSent) {
                                        System.out.println("Retransmitting frame no. " + (8 * k + count + 1) + " to " + (8 * k + tobeSent));
                                        for (int m = count; m < tobeSent; m++) {
                                            receiverOut.writeObject(eightFrames[m]);
                                        }
                                        int t = Integer.parseInt(in.readObject().toString());
                                        count += t;
                                    }
                                    totalSent += count;

                                }

                                System.out.println("totalsent: " + totalSent + " count: " + noChunks);

                            }
                            else {
                                System.out.println("Transmission denied by Reciever "+ receiverID);
                            }

                        } else {// receiver is offline
                            System.out.println(receiverID + " is offline now. File transmission unsuccessful.");
                            out.writeObject(receiverID + " is offline now. File transmission unsuccessful.");
                        }
                    } else if (reply.compareTo("l") == 0) {
                        //log her out
                        removeID(stID);
                        System.out.println(stID + " logged out");
                        break;

                    } else if (reply.compareTo("n") == 0) {
                        String senderID = in.readObject().toString();
                        System.out.println(senderID);
                        if(isOnline(senderID)){

                            for (int k = 0; k < CreateServer.conInfos.size(); k++) {
                                ConnectionInfo serverUtil = CreateServer.conInfos.get(k);
                                if (serverUtil.ID.compareTo(senderID) == 0) {
                                    receiverOut = serverUtil.outputStream;
                                    System.out.println("sender ID found");
                                    break;
                                }
                            }
                        }
                        String decision = in.readObject().toString();
                        receiverOut.writeObject(decision);
                        if (decision.compareTo("y") == 0) {

                            //number of loop
                            if (isOnline(senderID)) {
                                int loop = Integer.parseInt(in.readObject().toString());
                                for (int j = 0; j < loop; j++) {
                                    int tobeSent = Integer.parseInt(in.readObject().toString());
                                    for (int m = 0; m < tobeSent; m++) {
                                        String ack = in.readObject().toString();
                                        receiverOut.writeObject(ack);
                                    }
                                }
                            } else {
                                System.out.println("Sender is Offline");
                            }
                        }
                    }
                }

            }
        }catch(IOException e){
                System.out.println("IO Exception");
                System.out.println("Error message: " + e.getMessage());
                if (e.getMessage().compareTo("Connection reset") == 0) {
                    //System.out.println(e.getMessage());
                    removeID(stID);

                }

                //e.printStackTrace();
        } catch(ClassNotFoundException e){
            System.out.println("Client not found");
        }

    }



    void print(){
        System.out.println("Print data:");
        for(int i=0; i<5 && data[i][0]!=""; i++){
            for(int j=0; j<10 && data[i][j]!="";j++){
                System.out.print(data[i][j]+" ");
            }
            System.out.println();

        }
    }

    boolean isOnline(String id){
        for(int i=0; i<5 ; i++){
            for(int j=0; j<10 ;j++){
                if(data[i][j].compareTo(id)==0){
                    return true;
                }
            }
        }
        return false;
    }
    boolean removeID(String id){
        for(int i=0; i<5 ; i++){
            for(int j=0; j<10 ;j++){
                if(data[i][j].compareTo(id)==0){
                    data[i][j] = "-1";
                    print();
                    //remove from connection Infos
                    for (int k = 0; k < CreateServer.conInfos.size(); k++) {
                        ConnectionInfo serverUtil = CreateServer.conInfos.get(k);
                        if (serverUtil.ID.compareTo(id) == 0) {
                            CreateServer.conInfos.remove(k);
                        }
                    }

                    return true;
                }
            }
        }
        return false;
    }

    void deleteFile(String fileID) throws IOException {
        //delete the file
        File file = new File(fileID);
        Path path = FileSystems.getDefault().getPath(fileID);
        boolean b = Files.deleteIfExists(path);
        if(b)
            System.out.println("deleted successfully");
        else
            System.out.println("couldnot delete");
    }

}
//1405019