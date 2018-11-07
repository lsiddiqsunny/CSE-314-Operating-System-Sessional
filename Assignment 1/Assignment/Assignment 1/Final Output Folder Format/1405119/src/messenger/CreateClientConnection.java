package messenger;

import java.util.HashMap;
import util.ConnectionUtillities;

public class CreateClientConnection implements Runnable{
    public HashMap<String,Information> clientList;
    public ConnectionUtillities connection;
    public CreateClientConnection(HashMap<String,Information> list, ConnectionUtillities con){
        clientList=list;
        connection=con;
    }
    
    @Override
    public void run() {
        
        String username = connection.read();
        //System.out.println(username);
        clientList.put(username, new Information(connection, username));
        new Thread(new ServerReaderWriter(username,connection, clientList)).start();
        
    }
}