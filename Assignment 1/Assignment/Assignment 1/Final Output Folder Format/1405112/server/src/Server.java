import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Server
{
    int ClientCount;
    int ClientNumber;
    ConnectionUtilities connectionUtilities;
    String ClientID;
    ServerTask serverTask;

    HashMap<String,ConnectionUtilities> ClientList;
    FileDescription fileDescription;
    HashMap<String,ArrayList<Chunk>> files;
    HashMap<String,String> recepientList;
    HashMap<String,FileDescription> fileDescriptionHashMap =new HashMap<String, FileDescription>();
    ArrayList<Integer> ChunkSizes;
    extra ext;

    public Server() throws Exception
    {
        ClientCount=0;
        ClientNumber=1;
        ClientList = new HashMap<String, ConnectionUtilities>();
        files=new HashMap<String, ArrayList<Chunk>>();
        recepientList=new HashMap<String,String>();
        fileDescriptionHashMap=new HashMap<String, FileDescription>();
        ChunkSizes=new ArrayList<Integer>();
        ext=new extra(0,0);
        for(int i=1;i<=64;i++)
        {
            ChunkSizes.add(1024*i);
        }
        ServerSocket server = new ServerSocket(55555);
        server.setReceiveBufferSize(100);
        while (true)
        {
            Socket ClientSocket=server.accept();
            connectionUtilities=new ConnectionUtilities(ClientSocket);
            System.out.println("Client [ "+ClientNumber+" ] is connected");
            //new DataOutputStream(ClientSocket.getOutputStream()).writeUTF("test");

            //new CreateConnection(ClientSocket,ClientNumber,ClientList);
            new Thread(new CreateConnection(connectionUtilities,ClientList,files,recepientList,fileDescriptionHashMap,ChunkSizes,ext)).start();
            //serverTask=new ServerTask(ClientSocket,ClientNumber,ClientList);
            ClientCount++;
            ClientNumber++;
        }


    }
}
