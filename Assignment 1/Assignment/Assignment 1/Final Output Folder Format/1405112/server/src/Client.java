import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
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
    Client()
    {
        ConsoloseInput=new Scanner(System.in);

        try {
            ClientSocket=new Socket("localhost",55555);
            inputFromServer = new DataInputStream(ClientSocket.getInputStream());
            outoutToServer=new DataOutputStream(ClientSocket.getOutputStream());
            outoutToServer.writeUTF("Hey i want to connect");

            //ClientSocket.setSoTimeout(30000);
            objectInputStream=new ObjectInputStream(ClientSocket.getInputStream());
            objectOutputStream = new ObjectOutputStream(ClientSocket.getOutputStream());
            //System.out.println("Enter user ID:");
            //System.out.println(objectInputStream.read());
            while (true)
            {
                //System.out.println(objectInputStream.read());
                System.out.println(inputFromServer.readUTF());
                ClientId=ConsoloseInput.nextLine();
                System.out.println(ClientId);
                outoutToServer.writeUTF(ClientId);
                System.out.println(inputFromServer.readUTF());
                choice=ConsoloseInput.nextLine();
                outoutToServer.writeUTF(choice);
                System.out.println(inputFromServer.readUTF());
                RecepientID=ConsoloseInput.nextLine();
                //System.out.println(FileSize);
                outoutToServer.writeUTF(RecepientID);
                System.out.println(inputFromServer.readUTF());
                FileName=ConsoloseInput.nextLine();
                FilePath=FileName;
                file=new File(FilePath);
                FileName=file.getName();
                System.out.println(FileName);
                FileSize=(int)file.length();
                System.out.println(file.length() +" in integer "+FileSize);
                outoutToServer.writeUTF(FileName);
                outoutToServer.writeInt(FileSize);
                //outoutToServer.writeUTF(Files.probeContentType(file.toPath()));
                System.out.println(inputFromServer.readUTF());
                chunkSize=inputFromServer.readInt();
                FileId=inputFromServer.readUTF();
                chunkSize=3*1024;
                FileDescription fileDescription=new FileDescription(ClientId,RecepientID,FileName,FileSize);
                objectOutputStream.writeObject(fileDescription);
                FileInputStream fileInputStream=new FileInputStream(FilePath);
                System.out.println(chunkSize);
                chunkCount=(int)Math.ceil((float)FileSize/chunkSize);
                System.out.println(chunkCount);
                buffer=new byte[chunkSize];
                remaining=FileSize;
                /*
                String reply;
                int x;
                while ((x=(fileInputStream.read(buffer)))>0)
                {
                    System.out.println("Read "+ x +" bytes");
                    outoutToServer.write(buffer);
                }
                */

                for (int i = 0; i <chunkCount ; i++)
                {
                    if(remaining>=chunkSize)
                    {
                        Chunk chunk=new Chunk((byte)1,i,chunkSize,FileId);
                        fileInputStream.read(chunk.data);
                        //chunk.data=buffer;
                        //outoutToServer.write(buffer);
                        //outoutToServer.write(buffer);
                        objectOutputStream.writeObject(chunk);
                        System.out.println(inputFromServer.readUTF());
                    }
                    else
                    {
                        extra=new byte[remaining];
                        Chunk chunk=new Chunk((byte)1,i,remaining,FileId);
                        fileInputStream.read(chunk.data);
                        //Chunk chunk=new Chunk(remaining,FileId);
                        objectOutputStream.writeObject(chunk);
                        //outoutToServer.write(extra);
                        //outoutToServer.write(buffer,0,remaining);
                        System.out.println(inputFromServer.readUTF());
                    }
                    remaining=remaining-chunkSize;
                }

                System.out.println(inputFromServer.readUTF());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
