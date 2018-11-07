package Client;

import StuffingPackage.FrameInfo;
import StuffingPackage.Stuffing;
import Utilities.ClientInfo;
import Utilities.ClientUtilities;

import java.io.*;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeoutException;

import static java.lang.System.exit;

/**
 * Created by Rupak on 10/16/2017.
 */
public class readFromServer implements Runnable {
    ClientUtilities Client;
    ClientInfo clientInfo;

    readFromServer(ClientUtilities c, ClientInfo clientInfo) {
        Client = c;
        this.clientInfo = clientInfo;
    }

    @Override
    public void run() {
        {
            while (true) {
                try {
                    int x = Client.inFromServer.read();
                    if (x == 3) {
                        System.out.println("Receiver is not online.\nTry again later!!!");
                    } else if (x == 2) {
                        System.out.println("Read from Client to server is starting");

                        System.out.print("Enter File Name : ");
                        String fileName = Client.inFromUser.readLine();
                        Client.outToServer.writeBytes(fileName + "\n");

                        File file = new File(clientInfo.getPath() + "\\" + fileName);
                        System.out.println("File path : " + file.getAbsolutePath() + " File Length : " + file.length());
                        String length = Long.toString(file.length());
                        Client.outToServer.writeBytes(length + "\n");

                        int isBufferAvailable = Client.inFromServer.read();
                        if (isBufferAvailable == 4) {
                            System.out.println("Insufficient buffer available!!!\nTry again later.");
                            return;
                        }

                        int chunkSize = Integer.parseInt(Client.inFromServer.readLine());
                        System.out.println("Received from Server: " + chunkSize);
                        //byte[] array = new byte[chunkSize];

                        int fileSize = (int) file.length();
                        byte[][] mainFile = new byte[fileSize / 200 + 1][200];
                        byte[] framed = new byte[204];
                        byte[] data_stuffed = new byte[230];
                        byte[] ack_destuffed = new byte[5];
                        byte[] dummy_array = new byte[5];
                        Stuffing stuffWork = new Stuffing();
                        int flag = 0;
                        int lostFrame=3;

                        FileInputStream inFromFile = new FileInputStream(file);
                        int fileLeft = fileSize;
                        int i = 0;
                        while (fileLeft > chunkSize) {
                            inFromFile.read(mainFile[i]);
                            i++;
                            fileLeft -= chunkSize;
                        }
                        inFromFile.read(mainFile[i], 0, fileLeft);

                        byte seq = 0;
                        byte ack_expected = 0;
                        int buffered = 0;

                        int max_index = i+1;
                        int index = 0;
                        byte data = 1;
                        fileLeft = fileSize;
                        int left = chunkSize;
                        byte[] ack = new byte[8];
                        DataInputStream dis = new DataInputStream(Client.socket.getInputStream());

                        while (true) {
                            if (ack_expected == max_index) break;

                            for (int j = 0; j < 5; j++) {

                                if (index == max_index) break;

                                if (fileLeft < chunkSize) {
                                    left = fileLeft;
                                }
                                int frameLength = stuffWork.makeFrame(mainFile[index], framed, data, seq, (byte) 0, left);
                                System.out.print(index + " :(main data)    ");
                                stuffWork.printArray(mainFile[index], left);
                                System.out.print(index + " :(framed data)  ");
                                stuffWork.printArray(framed, left + 4);
                                int stuffedLength = stuffWork.stuff(framed, data_stuffed, frameLength);
                                System.out.print(index + " :(stuffed data) ");
                                stuffWork.printArray(data_stuffed, stuffedLength);
                                fileLeft -= left;
                                if(index!=lostFrame) {
                                    Client.outToServer.write(data_stuffed);
                                }
                                else if(flag==0)
                                    flag++;
                                else
                                    Client.outToServer.write(data_stuffed);

                                buffered++;
                                index++;
                                seq++;
                                System.out.println("Frame no. " + (index - 1) + " has been sent\n");
                            }
                            Client.socket.setSoTimeout(15000);
                            while (true) {
                                try {
                                    dis.read(ack);
                                    int deStuffedLength = stuffWork.deStuff(ack, ack_destuffed);
                                    stuffWork.hasCheckSumError(ack_destuffed, deStuffedLength);
                                    FrameInfo frameInfo = stuffWork.getFrame(ack_destuffed, dummy_array, deStuffedLength);
                                    int acknowledged = frameInfo.getAckNo();
                                    if (acknowledged == ack_expected) {
                                        System.out.println("Ack no. " + acknowledged + " has been received\n");
                                        ack_expected++;
                                        buffered--;
                                        if (ack_expected == max_index) break;
                                        if (index == max_index) continue;
                                        int frameLength = stuffWork.makeFrame(mainFile[index], framed, data, seq, (byte) 0, left);
                                        System.out.print(index + " :(main data)    ");
                                        stuffWork.printArray(mainFile[index], left);
                                        System.out.print(index + " :(framed data)  ");
                                        stuffWork.printArray(framed, left + 4);
                                        int stuffedLength = stuffWork.stuff(framed, data_stuffed, frameLength);
                                        System.out.print(index + " :(stuffed data) ");
                                        stuffWork.printArray(data_stuffed, stuffedLength);
                                        fileLeft -= left;
                                        Client.outToServer.write(data_stuffed);
                                        buffered++;
                                        index++;
                                        seq++;
                                        System.out.println("Frame no. " + (index - 1) + " has been sent");
                                    }
                                } catch (SocketTimeoutException e) {
                                    index = ack_expected;
                                    buffered = 0;
                                    seq = (byte) index;
                                    break;
                                }
                            }

                            System.out.println();
                        }

/*
                    while (fileLeft>0)
                    {
                        if(fileLeft<chunkSize){left=fileLeft;}
                        int frameLength = stuffWork.makeFrame(mainFile[index],framed,data,seq,(byte)0,left);
                        System.out.print(index+" : ");
                        stuffWork.printArray(mainFile[index],left);
                        stuffWork.printArray(framed,left+4);
                        //System.out.println(frameLength);
                        int stuffedLength = stuffWork.stuff(framed,stuffed,frameLength);
                        stuffWork.printArray(stuffed,stuffedLength);
                        //Client.outToServer.write(stuffed,0,stuffedLength);
                        Client.outToServer.write(stuffed);
                        index++;
                        seq++;
                        seq %=8;
                        int deStuffedLength = stuffWork.deStuff(stuffed,destuffed);
                        stuffWork.printArray(destuffed,deStuffedLength);
                        fileLeft-=left;

                        System.out.println();
                    }*/
/*

                    int fileLeft = (int)file.length();

                    FileInputStream inFromFile = new FileInputStream(file);

                    while (fileLeft>chunkSize)
                    {
                        int readDone = inFromFile.read(array);
                        fileLeft -= readDone;
                        System.out.println(readDone+" "+fileLeft);
                        Client.outToServer.write(array,0,readDone);

                        int received = Client.inFromServer.read();
                        if(received == 2){
                            inFromFile.close();
                            throw new Exception("Data transmission failed");
                        }

                    }

                    int readDone = inFromFile.read(array,0,fileLeft);
                    fileLeft -= readDone;
                    System.out.println(readDone+" "+fileLeft);
                    Client.outToServer.write(array,0,readDone);

                    System.out.println("Write from client to server is done");

                    int wait = Client.inFromServer.read();
                    if(wait == 2){
                        inFromFile.close();
                        throw new Exception("Error in  transmission");
                    }
                    inFromFile.close();
*/
                        System.out.println("Write from client to server is done");
                    } else if (x == 4) {

                        System.out.println("Receive from server thread is starting");
                        System.out.println("Want to receive?(Press 2)");

                        String sFileID = Client.inFromServer.readLine();

                        x = Client.inFromServer.read();

                        if (x == 5) {
                            Client.outToServer.writeBytes(sFileID + "\n");
                            String str = Client.inFromServer.readLine();
                            String msg[] = str.split(":");
                            System.out.println(msg[0] + " " + msg[1] + " " + msg[2]);

                            String fileName = msg[0];
                            int chunkSize = Integer.parseInt(msg[1]);
                            int fileSize = Integer.parseInt(msg[2]);


                            int fileLeft = fileSize;

                            File newFile = new File(clientInfo.getPath() + "/" + fileName);
                            FileOutputStream f = new FileOutputStream(newFile);
                            DataInputStream dis = new DataInputStream(Client.socket.getInputStream());

                            byte[] array = new byte[chunkSize];

                            int isError = 0;
                            while (fileLeft > chunkSize) {
                                int readDone = dis.read(array, 0, chunkSize);
                                fileLeft -= readDone;
                                System.out.println(readDone + " " + fileLeft);
                                f.write(array, 0, readDone);
                                f.flush();

                                if (readDone == chunkSize) {
                                    Client.outToServer.writeByte(1);
                                } else {
                                    isError = 1;
                                    Client.outToServer.writeByte(2);
                                    System.out.println("Data is lost during transmission");
                                    break;
                                }
                            }
                            if (isError != 1) {
                                int readDone = dis.read(array, 0, fileLeft);
                                f.write(array, 0, readDone);
                                f.flush();
                                fileLeft -= readDone;
                                System.out.println(readDone + " " + fileLeft);
                                if (fileLeft != 0)
                                    isError = 1;
                            }
                            if (isError == 1) {
                                System.out.println("Error in receiving");
                                f.close();
                                newFile.delete();
                                exit(0);
                            } else {
                                System.out.println("Received successfully");
                                f.close();
                            }
                        }
                    } else System.out.println(x);
                    break;

                } catch (FileNotFoundException e) {
                    System.out.println("File not found");
                    break;
                } catch (SocketTimeoutException e) {
                    continue;
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                } catch (Exception e) {
                    System.out.println(e);
                    break;
                }
            }
        }


    }
}