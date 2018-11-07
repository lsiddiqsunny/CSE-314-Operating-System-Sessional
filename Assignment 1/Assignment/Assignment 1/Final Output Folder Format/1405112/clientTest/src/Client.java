import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.BitSet;
import java.util.Scanner;

public class Client
{
    String ClientId,RecepientID;
    int FileSize;
    String choice;
    Scanner ConsoloseInput;
    String FileName;
    int chunkSize;
    int chunkCount=0;
    byte[] buffer;
    int remaining;
    byte[] extra;
    String FileId;
    File file;
    String FilePath="/home/jawad/Desktop/IdeaProjects/server/src/MultiThread.java";
    Socket ClientSocket;
    DataOutputStream outoutToServer;
    DataInputStream inputFromServer;
    ObjectInputStream objectInputStream;
    ObjectOutputStream objectOutputStream;
    ConnectionUtilities connectionUtilities;
    String temp;
    public Client()
    {
        ConsoloseInput=new Scanner(System.in);
        connectionUtilities=new ConnectionUtilities("127.0.0.1",55555);
        try
        {

            String msg="Hey i want to connect";
            connectionUtilities.write(msg);
            System.out.println(connectionUtilities.read().toString());
            ClientId = ConsoloseInput.nextLine();
            System.out.println(ClientId);
            connectionUtilities.write(ClientId);
            System.out.println(connectionUtilities.read().toString());//Successfully connected
            while (true)
            {
                System.out.println(connectionUtilities.read().toString());//send or receieve
                choice = ConsoloseInput.nextLine();
                connectionUtilities.write(choice);
                if (choice.equals("y"))
                {
                    System.out.println(connectionUtilities.read().toString());
                    RecepientID = ConsoloseInput.nextLine();
                    connectionUtilities.write(RecepientID);
                    String a=connectionUtilities.read().toString(); //recepient online or not
                    if(a.equals("Enter filename with extention"))
                    {
                        System.out.println(a);
                        FileName = ConsoloseInput.nextLine();
                        FilePath = FileName;
                        file = new File(FilePath);
                        FileName = file.getName();
                        System.out.println(FileName);
                        FileSize = (int) file.length();
                        System.out.println(file.length() + " in integer " + FileSize);
                        FileDescription fileDescription = new FileDescription(ClientId, RecepientID, FileName, FileSize);
                        connectionUtilities.write(fileDescription);
                        String big = connectionUtilities.read().toString();
                        if (big.equals("File is too big to transfer at this moment")) {
                            System.out.println(big);
                            continue;
                        }
                        System.out.println(big); //not larger than memory
                        chunkSize = (int) connectionUtilities.read();
                        System.out.println("Chunksize is " + chunkSize);
                        FileId = connectionUtilities.read().toString();
                        //chunkSize = 3 * 1024;
                        FileInputStream fileInputStream = new FileInputStream(FilePath);
                        System.out.println(chunkSize);
                        chunkCount = (int) Math.ceil((float) FileSize / chunkSize);
                        System.out.println(chunkCount);
                        buffer = new byte[chunkSize];
                        remaining = FileSize;
                /*
                String reply;
                int x;
                while ((x=(fileInputStream.read(buffer)))>0)
                {
                    System.out.println("Read "+ x +" bytes");
                    outoutToServer.write(buffer);
                }
                */
                        try
                        {
                            int nextframe = 0;
                            for (int i = 0; i < chunkCount; i++)
                            {
                                Chunk chunk;
                                if (remaining >= chunkSize) {
                                    chunk = new Chunk((byte) 1, nextframe, chunkSize, FileId);
                                } else {
                                    chunk = new Chunk((byte) 1, nextframe, remaining, FileId);
                                }
                                fileInputStream.read(chunk.data);
                                chunk.cks = chunk.checksum();
                                System.out.println("Checksum: "+chunk.cks);
                                String finalString = chunk.toBitArray();
                                System.out.println(finalString);

                                String stuffed = chunk.stuffBit(finalString);
                                System.out.println("Stuffed");
                                System.out.println(stuffed);

                                chunk.frame = chunk.toBitset(stuffed);
                                System.out.println(chunk.frame);

                                connectionUtilities.write(chunk);
                                nextframe = 1 - nextframe;
                                connectionUtilities.socket.setSoTimeout(300000);
                                while (true)
                                {
                                    Chunk next = (Chunk) connectionUtilities.read();
                                    System.out.println(next.FileId);
                                    if (next.sn!=nextframe)
                                    {
                                        chunk.sn = nextframe;
                                        connectionUtilities.write(chunk);
                                    }
                                    else
                                    {
                                        break;
                                    }
                                }
                                remaining = remaining - chunkSize;
                                System.out.println(connectionUtilities.read().toString());
                            }
                            String temp = connectionUtilities.read().toString();
                            System.out.println(temp);
                            connectionUtilities.socket.setSoTimeout(0);
                        } catch (SocketException e)
                        {
                            System.out.println("Packet lost");
                            e.printStackTrace();
                        }
                    }
                }
                else
                {
                    /*
                    System.out.println("----wait----");
                    System.out.println(connectionUtilities.read().toString());
                    connectionUtilities.write(ConsoloseInput.nextLine());
                    System.out.println(connectionUtilities.read().toString());
                    FileOutputStream fileOutputStream=new FileOutputStream("Receieved/test");
                    for (int i = 0; i <chunkCount ; i++) {
                        Chunk y=(Chunk) connectionUtilities.read();
                        fileOutputStream.write(y.data);
                        System.out.println(connectionUtilities.read().toString());
                    }
                    System.out.println(connectionUtilities.read().toString());
                    */

                    System.out.println(connectionUtilities.read().toString());//wait
                    System.out.println((int)connectionUtilities.read());
                    System.out.println((int)connectionUtilities.read());
                    chunkCount=(int)connectionUtilities.read();
                    System.out.println(connectionUtilities.read().toString());//prompt
                    connectionUtilities.write(ConsoloseInput.nextLine());
                    System.out.println(connectionUtilities.read().toString());//starting
                    FileId=connectionUtilities.read().toString();
                    FileOutputStream fos=new FileOutputStream("Receieved/"+FileId);
                    for (int i = 0; i <chunkCount ; i++)
                    {
                        Chunk y=(Chunk) connectionUtilities.read();
                        fos.write(y.data);
                        System.out.println(connectionUtilities.read().toString());
                    }
                    System.out.println(connectionUtilities.read().toString());

                }
            }


        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
