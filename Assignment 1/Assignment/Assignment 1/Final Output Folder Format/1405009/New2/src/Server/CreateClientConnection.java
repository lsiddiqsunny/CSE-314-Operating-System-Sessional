package Server;
import StuffingPackage.FrameInfo;
import StuffingPackage.Stuffing;
import Utilities.ClientInfo;
import Utilities.ServerUtilities;
import Utilities.createID;

import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;


/**
 * Created by Rupak on 9/26/2017.
 */
public class CreateClientConnection implements Runnable{

    public Socket socket;
    public HashMap<Integer, ClientInfo>clientInfo;
    public HashMap<Integer, FileTransmission>fileInfo;
    public ServerUtilities server;
    public final int maxBufferSize;

    public CreateClientConnection(Socket sc, HashMap<Integer, ClientInfo>clientInfoHashMap, HashMap<Integer, FileTransmission>fileInfo, int x) {
        socket=sc;
        clientInfo=clientInfoHashMap;
        this.fileInfo = fileInfo;
        maxBufferSize = x;
        try {
            server = new ServerUtilities(socket);
        } catch (IOException e) {
            e.printStackTrace();
        }
        new Thread(this).start();
    }

    @Override
    public void run() {
        int ClientID=-1;
        while(true) {
            try {
                System.out.println("2nd step : Creating ClientInfo"); //...................
                String createdClient = server.inToServer.readLine();
                ClientID = Integer.parseInt(createdClient);
                String path = server.inToServer.readLine();
                ClientInfo client = new ClientInfo(ClientID, server.socket.getInetAddress(), server.socket.getPort(), server, server.socket, path);
                if (!clientInfo.containsKey(client.getSid())) {
                    clientInfo.put(client.getSid(), client);
                    server.outFromServer.writeByte(1);
                    System.out.println("Created : " + client.getSid() + " " + client.getPath());
                    break;
                }
                else server.outFromServer.writeByte(2);
            } catch (IOException e) {
                if(clientInfo.containsKey(ClientID))
                {
                    clientInfo.remove(ClientID);
                }
                e.printStackTrace();
            }
        }

        while (true) {
            int x = 0;
            try {
                x = server.inToServer.read();
                System.out.println("Starting thread for "+ClientID+" "+" "+x);
            } catch (IOException e) {
                System.out.println("Client "+ClientID+" is disconnected");
                clientInfo.remove(ClientID);
                return;
            }
            if (x == 1) {
                try {
                    String s = "";
                    String receivingClient = server.inToServer.readLine();
                    //System.out.println(receivingClient);
                    int receivingClientID = Integer.parseInt(receivingClient);
                    if (clientInfo.containsKey(receivingClientID)) {

                        server.outFromServer.writeByte(2);

                        System.out.println("Receive from Client is Starting");
                        String fileName = server.inToServer.readLine();

                        int fileSize = Integer.parseInt(server.inToServer.readLine());
                        System.out.println("File length : " + fileSize);

                        int usedBuffer=0;
                        for(int key:fileInfo.keySet()) {usedBuffer += fileInfo.get(key).fileSize;}
                        System.out.println("Already used buffer : " + usedBuffer);
                        if(usedBuffer+fileSize>= maxBufferSize)
                        {
                            server.outFromServer.writeByte(4);
                            System.out.println("Insufficient buffer");
                            continue;
                        }
                        server.outFromServer.writeByte(8);

                        //int chunkSize = ThreadLocalRandom.current().nextInt(512, 65536 + 1);
                        int chunkSize=200;
                        System.out.println("Sent from Server: " + chunkSize);
                        server.outFromServer.writeBytes(String.valueOf(chunkSize)+"\n");

                        Stuffing stuffWork = new Stuffing();
                        byte[] received = new byte[chunkSize+30];
                        byte[] destuffed = new byte[chunkSize+4];
                        byte[] payload = new byte[chunkSize];

                        int fileID = createID.getID();
                        FileTransmission fileTransmission = new FileTransmission(receivingClientID, fileName, 5, fileSize, chunkSize);
                        fileInfo.put(fileID,fileTransmission);//.......1

                        FileOutputStream f = new FileOutputStream("./Server/" + fileName);
                        DataInputStream fileInput = new DataInputStream(server.socket.getInputStream());
                        int fileLeft = fileSize;
                        int flag = 0;

                        byte seqExpected=0;
                        byte ackNo=0;
                        byte[] dummy_ack_payload = new byte[1];
                        byte[] ackFrame = new byte[5];
                        byte[] stuffedAck = new byte[8];
                        int i=0;
                        while(fileLeft>0)
                        {
                            //FrameInfo ackFrame = new FrameInfo();


                            fileInput.read(received);
                            int readDone = stuffWork.deStuff(received,destuffed);
                            FrameInfo frame = stuffWork.getFrame(destuffed,payload,readDone);
                            System.out.print(frame.getSeqNo()+" :(received)  ");
                            stuffWork.printArray(received,received.length);
                            System.out.print(frame.getSeqNo()+" :(destuffed) ");
                            stuffWork.printArray(destuffed,readDone);


                            if(stuffWork.hasCheckSumError(destuffed,readDone)){
                                System.out.println("Frame no. "+frame.getSeqNo()+" has checksum error");
                                continue;
                            }




                            if(frame.getSeqNo()==seqExpected)
                            {
                                System.out.print(i+" :(main data) ");
                                stuffWork.printArray(payload,frame.getFrameSize());
                                int stuffLen = stuffWork.makeFrame(dummy_ack_payload,ackFrame,(byte)0,(byte)0,seqExpected,1);
                                stuffWork.stuff(ackFrame,stuffedAck,stuffLen);
                                server.outFromServer.write(stuffedAck);
                                System.out.println();
                                System.out.println("Ack. of Frame no. "+i+" has been sent");
                                System.out.println();
                                fileInfo.get(fileID).add(payload,frame.getFrameSize());//.......1
                                f.write(payload, 0, frame.getFrameSize());
                                f.flush();
                                fileLeft-=frame.getFrameSize();
                                seqExpected++;
                                i++;
                            }

                            else
                                System.out.println("\nFrame no. "+frame.getSeqNo()+" has been  discarded\n");
                        }
                        f.close();
                        /*
                        byte[] array = new byte[chunkSize];
                        fileLeft = fileSize;
                        while (fileLeft > chunkSize) {
                            int readDone = fileInput.read(array, 0, chunkSize);
                            fileLeft -= readDone;
                            System.out.println(readDone + " " + fileLeft);
                            fileInfo.get(fileID).add(array,readDone);//.......1
                            f.write(array, 0, readDone);
                            f.flush();
                            if (readDone == chunkSize) {
                                server.outFromServer.writeByte(1);
                            }
                            else{
                                System.out.println("Data lost from Sender side");
                                server.outFromServer.writeByte(2);
                                fileInfo.remove(fileID);
                                clientInfo.remove(ClientID);
                                System.out.println("Client "+ClientID+" is disconnected.");
                                f.close();
                                return;
                            }
                        }

                        int readDone = fileInput.read(array, 0, fileLeft);
                        f.write(array, 0, readDone);
                        f.flush();
                        fileInfo.get(fileID).add(array, fileLeft);//.......1
                        fileLeft -= readDone;
                        System.out.println(readDone + " " + fileLeft);
                        if (fileLeft == 0) {
                            server.outFromServer.writeByte(1);
                            System.out.println("Read finishing" + "");
                        } else {
                            System.out.println("Data is corrupted.");
                            server.outFromServer.writeByte(2);
                            fileInfo.remove(fileID);
                            clientInfo.remove(ClientID);
                            System.out.println("Client "+ClientID+" is disconnected.");
                            f.close();
                            return;
                        }
                        f.close();
*/
                        System.out.println("Data is successfully received from client");
                        System.out.println("Sending Thread begin");

                        writeFromSender(receivingClientID,fileID);

                    } else {
                        server.outFromServer.writeByte(3);
                        System.out.println("Receiving client not found");
                    }
                } catch (IOException e) {
                    System.out.println(e);

                }
            }
            else if(x == 2){
                String sFileID = "";
                try {
                    server.outFromServer.writeByte(5);
                    sFileID = server.inToServer.readLine();
                    writeFromReceiver(ClientID,Integer.parseInt(sFileID));
                    System.out.println(ClientID+" write done from server ");
                } catch (IOException e) {
                    if (fileInfo.containsKey(Integer.parseInt(sFileID)))fileInfo.remove(Integer.parseInt(sFileID));
                    e.printStackTrace();
                }
            }
        }
    }


    void writeFromSender(int receivingID, int fileID)
    {
        ServerUtilities server = clientInfo.get(receivingID).getServer();
        try {
            server.outFromServer.writeByte(4);
            System.out.println(Integer.toString(fileID)+"\n");
            server.outFromServer.writeBytes(Integer.toString(fileID)+"\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void writeFromReceiver(int receivingID, int fileID)
    {
        int flag=0;
        ServerUtilities server = clientInfo.get(receivingID).getServer();
        try {
            FileTransmission fileTransmission = fileInfo.get(fileID);
            flag=0;

            server.outFromServer.writeBytes(fileTransmission.fileName+":"+fileTransmission.chunkSize+":"+fileTransmission.fileSize+"\n");
            for(int i=0;i<fileTransmission.chunkNumber;i++)
            {
                flag=0;
                server.outFromServer.write(fileTransmission.data[i],0,fileTransmission.chunkSize);
                int wait = server.inToServer.read();
                if(wait==2)
                {
                    flag=1;
                    break;
                }
            }
            System.out.println(fileTransmission.leftOut);
            if(flag==0)server.outFromServer.write(fileTransmission.data[fileTransmission.chunkNumber],0,fileTransmission.leftOut);

        } catch (IOException e) {
            flag=1;
            if(clientInfo.containsKey(receivingID))
            {
                clientInfo.remove(receivingID);
            }
            System.out.println("Client "+receivingID+" disconnected.");
        }
        if(flag==0)System.out.println("Write Done");
        fileInfo.remove(fileID);
        //clientInfo.remove(receivingID);
    }
}

