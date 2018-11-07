import java.io.*;
import java.net.Inet4Address;
import java.net.Socket;
import java.net.SocketException;
import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class CreateConnection implements Runnable
{
    Socket ClientSocket;
    int ClientNumber;
    int Clientcount;

    String ClientId;
    Thread ConnectionThread;
    DataOutputStream outputFromServer;
    DataInputStream inputToServer;
    ObjectInputStream objectInputStream;
    FileDescription fileDescription;
    String temp="Client Number "+ ClientNumber;
    int serverSize=10*1024*1024;
    ConnectionUtilities connectionUtilities;

    //server data;
    HashMap<String,ConnectionUtilities> ClientList;
    HashMap<String,ArrayList<Chunk>> files;
    HashMap<String,String> recepientList;
    HashMap<String,FileDescription> fileDescriptionHashMap =new HashMap<String, FileDescription>();
    ArrayList<Integer> ChunkSizes;
    extra ext;

    public CreateConnection(Socket ClientSocket, int ClientNumber, HashMap<String,ConnectionUtilities> ClientList)
    {
        this.ClientSocket=ClientSocket;
        this.ClientNumber=ClientNumber;
        this.ClientList=ClientList;
        try{
            outputFromServer=new DataOutputStream(this.ClientSocket.getOutputStream());
            inputToServer=new DataInputStream(this.ClientSocket.getInputStream());
            objectInputStream=new ObjectInputStream(this.ClientSocket.getInputStream());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        ConnectionThread=new Thread(this,temp);
        System.out.println("thread"+ ConnectionThread);
        ConnectionThread.start();
    }
    public CreateConnection(ConnectionUtilities connectionUtilities,HashMap<String,ConnectionUtilities> ClientList,
                            HashMap<String,ArrayList<Chunk>> files, HashMap<String,String> recepientList,
                            HashMap<String,FileDescription> fileDescriptionHashMap,ArrayList<Integer> chunkSizes,extra ext)
    {
        this.ClientList=ClientList;
        this.connectionUtilities=connectionUtilities;
        this.files=files;
        this.recepientList=recepientList;
        this.fileDescriptionHashMap=fileDescriptionHashMap;
        this.ChunkSizes=chunkSizes;
        this.ext=ext;

        //ConnectionThread=new Thread(this);
        //ConnectionThread.start();
    }

    @Override
    public void run()
    {
        ClientNumber++;
        Clientcount++;
        String s="Enter you User id";
        //try {
            Object o=connectionUtilities.read(); //strting prompt
            System.out.println(o.toString());
            //System.out.println(inputToServer.readUTF());
            //outputFromServer.writeUTF(s);
            connectionUtilities.write(s);
            //ClientId = inputToServer.readUTF();
            ClientId=connectionUtilities.read().toString();
            if (ClientList.containsKey(ClientId)) {
                //outputFromServer.writeUTF("You can't login from two different devices.");
                String x="You can't Log In from two different devices";
                connectionUtilities.write(x);
            } else
            {
                String x="----Succesfully logged In----";
                connectionUtilities.write(x);
                ClientList.put(ClientId, connectionUtilities);
                System.out.println(ClientId + " logged in");
                new Thread(new ServerTask(ClientId,connectionUtilities,ClientList,files,recepientList,fileDescriptionHashMap,ChunkSizes,ext)).start();
            }

        //}
        /*
        catch(IOException e)
        {
            e.printStackTrace();

        }*/
    }

}
