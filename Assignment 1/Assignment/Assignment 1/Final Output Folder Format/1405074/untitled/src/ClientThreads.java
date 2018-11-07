import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;

public class ClientThreads {
    private static ClientThreads clientThreads = new ClientThreads();
    HashMap<String,Pair<Integer,WorkerThread>> clients = new HashMap<String,Pair<Integer,WorkerThread>>();
    HashMap<String,String> files = new HashMap<String,String>();
    Integer size;
    private ClientThreads(){
        size = 0;
    }

    public static ClientThreads getClientThreads() {
        return clientThreads;
    }

    public void add(int ip, WorkerThread t,String ID){
        Pair<Integer,WorkerThread> p = new Pair<Integer,WorkerThread>(ip,t);
        clients.put(ID,p);

    }


    public void remove(String ID){
        clients.remove(ID);
    }

    public boolean checkAvailable(String fileSize){
        if(size+Integer.parseInt(fileSize)<100*1024*1024){
            size = size + Integer.parseInt(fileSize);
            return true;
        }
        return false;
    }

    public void resetSize(int s){
        size = size - s;

    }

    public WorkerThread get(String ID){
        if(clients.containsKey(ID)){
            return clients.get(ID).getValue();
        }
        else return null;
    }
}
