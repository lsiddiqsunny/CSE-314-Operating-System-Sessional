import java.awt.*;
import java.io.*;
import java.net.Inet4Address;
import java.net.Socket;
import java.net.SocketException;
import java.security.Key;
import java.util.*;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ServerTask implements Runnable
{
    Socket ClientSocket;
    int ClientNumber;
    int Clientcount;
    HashMap<String,ConnectionUtilities> ClientList;
    String ClientId;
    Thread ConnectionThread;
    ConnectionUtilities connectionUtilities;
    DataOutputStream outputFromServer;
    DataInputStream inputToServer;
    ObjectInputStream objectInputStream;
    FileDescription fileDescription;
    String temp="Client Number ["+ ClientNumber+ "]";
    int serverSize=1000*1024*1024;


    String RecepientId;
    int FileSize;
    String FileType;
    String FileName;
    String FileId;
    HashMap<String,ArrayList<Chunk>> files;
    HashMap<String,String> recepientList;

    HashMap<String,FileDescription> fileDescriptionHashMap =new HashMap<String, FileDescription>();

    ArrayList<Integer> ChunkSizes=new ArrayList<Integer>();

    int chunkSize;
    byte[] buffer;
    int chunkCount;
    Random randomizer = new Random();

    int remaining;
    byte[] extra;
    int complete;
    //Integer complete;
    extra ext;

    public ServerTask(Socket ClientSocket, String clientId, HashMap<String,ConnectionUtilities> ClientList)
    {
        this.ClientSocket=ClientSocket;
        this.ClientId=clientId;
        this.ClientList=ClientList;
        for(int i=1;i<=64;i++)
        {
            ChunkSizes.add(1024*1024*i);
        }
        files=new HashMap<String, ArrayList<Chunk>>();
        ConnectionThread=new Thread(this,temp);
        System.out.println("thread"+ ConnectionThread);
        ConnectionThread.start();
    }
    public ServerTask(String clientId,ConnectionUtilities connectionUtilities, HashMap<String,ConnectionUtilities> clientList,
                      HashMap<String,ArrayList<Chunk>> files, HashMap<String,String> recepientList,
                      HashMap<String,FileDescription> fileDescriptionHashMap,ArrayList<Integer> chunkSizes,extra ext)
    {
        this.ClientId=clientId;
        this.connectionUtilities=connectionUtilities;
        this.ClientList=clientList;
        this.files=files;
        this.recepientList=recepientList;
        this.fileDescriptionHashMap=fileDescriptionHashMap;
        this.ChunkSizes=chunkSizes;
        this.chunkCount=chunkCount;
        this.ext=ext;
    }

    @Override
    public void run()
    {
        try
        {
            while (true)
            {
                connectionUtilities.write("Do you want to send files?(Y/n)"); //prompt
                if ((connectionUtilities.read().toString()).equals("y")) {
                    connectionUtilities.write("Enter Recepient ID:");
                    RecepientId = connectionUtilities.read().toString();
                    if (ClientList.containsKey(RecepientId)) {
                        connectionUtilities.write("Enter filename with extention");
                        fileDescription = (FileDescription) connectionUtilities.read();
                        FileName = fileDescription.name;
                        FileSize = fileDescription.FileSize;
                        System.out.println(FileName);
                        FileId = ClientId + RecepientId + FileName;
                        recepientList.put(RecepientId, FileId);
                        fileDescriptionHashMap.put(FileId, fileDescription);   //if users goes online
                        FileName = "server/" + FileId;
                        if (usedMemory() + FileSize > serverSize) {
                            connectionUtilities.write("File is too big to transfer at this moment");
                        } else {
                            chunkSize = ChunkSizes.get(randomizer.nextInt(ChunkSizes.size()));
                            System.out.println("Used memory: " + usedMemory());
                            connectionUtilities.write("Divide your file into chunk of " + chunkSize + " bytes");
                        }
                        connectionUtilities.write(chunkSize);
                        connectionUtilities.write(FileId);
                        chunkCount = (int) Math.ceil((float) FileSize / chunkSize);
                        ext.chunkCount = chunkCount;
                        buffer = new byte[chunkSize];
                        remaining = FileSize;
                        ArrayList<Chunk> file = new ArrayList<>();
                        int expectedFrame=0;

                        try
                        {
                            for (int i = 0; i < chunkCount; i++)
                            {
                                //if (remaining >= chunkSize) {
                                connectionUtilities.socket.setSoTimeout(3000000);
                                Chunk chunk;
                                String originalData="";
                                while (true)
                                {
                                    chunk = (Chunk) connectionUtilities.read();
                                    System.out.println(chunk.frame);
                                    String stuffedString = chunk.toStringFromBit(chunk.frame);
                                    System.out.println("Stuffed");
                                    System.out.println(stuffedString);

                                    String origin = chunk.deStuff(stuffedString);
                                    System.out.println("DeStuffed");
                                    System.out.println(origin);


                                    String acnbit = origin.substring(0, 1);
                                    System.out.println(acnbit);

                                    origin = origin.substring(1);
                                    System.out.println("Without acknowledge bit");
                                    System.out.println(origin);

                                    String seqBit = origin.substring(0, 1);
                                    System.out.println(seqBit);

                                    origin = origin.substring(1);
                                    System.out.println("Without sequence no");
                                    System.out.println(origin);

                                    String chk = origin.substring(origin.length() - 8, origin.length());
                                    System.out.println(chk);
                                    originalData = origin.substring(0, origin.length() - 8);
                                    byte chksNew=chunk.checksum();

                                    if(chunk.sn!=expectedFrame || chunk.hasChecksumError(chksNew))
                                    {
                                        Chunk next = new Chunk((byte) 0, expectedFrame, 0, "acknowledgement");
                                        connectionUtilities.write(next);
                                    }
                                    else
                                    {

                                        break;
                                    }
                                }

                                System.out.println("final data");
                                System.out.println(originalData);

                                BitSet bitSet = chunk.toBitset(originalData);
                                System.out.println(bitSet);
                                chunk.finalData = bitSet.toByteArray();
                                file.add(chunk);

                                remaining = remaining - chunkSize;

                                expectedFrame = 1 - expectedFrame;
                                Chunk next=new Chunk((byte)0,expectedFrame,1,"acknowledgement");
                                connectionUtilities.write(next);

                                connectionUtilities.write(chunk.size + " bytes receieved");
                                //}
                                //else
                                //{
                                //    Chunk chunk = (Chunk) connectionUtilities.read();
                                //    file.add(chunk);
                                //    connectionUtilities.write(chunkSize + " bytes receieved");
                                //}
                            }
                            files.put(FileId, file);
                            connectionUtilities.socket.setSoTimeout(0);
                            connectionUtilities.write("Complete file sent");
                        }
                        catch (SocketException e)
                        {
                            System.out.println("Sender Logs Out");
                            file.clear();
                        }
                        int receievedSize = 0;
                        ArrayList<Chunk> retreve = files.get(FileId);
                        for (Chunk x : retreve) {
                            receievedSize = receievedSize + x.data.length;
                        }
                        System.out.println("Receieved Size: " + receievedSize + "and File Size: " + FileSize);
                        if (receievedSize == FileSize)
                        {
                            connectionUtilities.write("----Complete file receieved----");
                            ext.complete = 1;
                            FileOutputStream fos=new FileOutputStream("server/"+FileId);
                            for (int i = 0; i <file.size() ; i++)
                            {
                                Chunk y=file.get(i);
                                fos.write(y.finalData);
                                System.out.println("written in server/"+FileId);
                            }
                            /*
                            ConnectionUtilities receiever=ClientList.get(RecepientId);
                            receiever.write("Do you want to receieve a " + FileSize + " byte file from " +ClientId + "?(y/n)");
                            if(receiever.read().toString().equals("y"))
                            {
                                receiever.write("----Starting file transfer----");
                                ArrayList<Chunk> forClient = files.get(FileId);
                                for (Chunk y : forClient) {
                                    receiever.write(y);
                                    receiever.write("Sent " + y.size + " bytes from " + ClientId);
                                }
                                receiever.write("Comlete file sent");

                            }
                            */
                            synchronized (ext) {
                                try {
                                    ext.notifyAll();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            complete = 1;
                            System.out.println(complete);

                        } else {
                            connectionUtilities.write("here was an error in file transfer start again");
                        }
                        System.out.println("File name is " + FileName + "File Id id " + FileId + " File size is " + FileSize + " bytes");
                    } else {
                        connectionUtilities.write("Recepient is not online currently");
                    }
                } else {
                    connectionUtilities.write("------WAIT------");

                    synchronized (ext) {
                        try {
                            ext.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    connectionUtilities.write(ext.complete);
                    connectionUtilities.write(ext.complete);
                    connectionUtilities.write(ext.chunkCount);
                    System.out.println(complete);
                    if (recepientList.containsKey(ClientId)) {
                        String fId = recepientList.get(ClientId);
                        FileDescription fd = fileDescriptionHashMap.get(fId);
                        System.out.println();
                        connectionUtilities.write("Do you want to receieve a " + fd.FileSize + " byte file from " + fd.sender + "?(y/n)");
                        if (connectionUtilities.read().toString().equals("y")) {
                            connectionUtilities.write("----Starting file transfer----");
                            connectionUtilities.write(fId);
                            ArrayList<Chunk> forClient = files.get(fId);
                            for (Chunk y : forClient) {
                                connectionUtilities.write(y);
                                connectionUtilities.write("Sent " + y.size + " bytes from " + ClientId);
                            }
                            connectionUtilities.write("Comlete file sent");

                        }


                    }
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    public void filTransmission()
    {

    }
    int usedMemory()
    {
        int size=0;
        for (ArrayList<Chunk> x: files.values())
        {
            if(x.size()!=0)
            {
                size=size+x.size()*x.get(0).size;
                System.out.println(size);
            }
        }
        return size;
    }
    void cancel()
    {

    }
}
