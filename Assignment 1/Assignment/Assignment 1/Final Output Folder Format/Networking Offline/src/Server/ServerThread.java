package Server;

import Utility.NetworkUtil;
import Utility.ProcessBytes;
import javafx.util.Pair;

import java.io.File;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class ServerThread implements Runnable{
    private Thread t;
    private NetworkUtil nc;
    private Server SERVER;
    private String UserName;

    public ServerThread(NetworkUtil nc, Server SERVER, String UserName){
        this.nc=nc;
        this.SERVER=SERVER;
        this.UserName=UserName;
        t=new Thread(this);
        t.start();
    }

    @Override
    public void run() {

        try {
            while (true) {
                //Server->Recipient
                String ClientResponse = (String) nc.read();

                if (ClientResponse.equals("Client : Ready to Receive")) {
                    Queue<ArrayList<byte[]>> now = SERVER.FileStorage.get(UserName);

                    if (now.size() == 0) nc.write("Error : Nothing to Send");
                    else{
                        nc.write("OK : File Transmission begining");
                        ArrayList<byte[]> Full = now.remove();
                        nc.write(Full.size());

                        int Size=0;
                        nc.socket.setSoTimeout(30000);

                        //Go-Back 8 protocol
                        int lo=0;
                        int hi=Math.min(8,Full.size());

                        while(lo!=hi){
                            for(int i=lo;i<hi;i++) nc.write(ProcessBytes.BitStuff(i,Full.get(i),false));
                            nc.socket.setSoTimeout(10000);
                            try{ for(int i=lo;i<hi;i++) {nc.read(); lo++;} }
                            catch (SocketTimeoutException e) {}

                            if(lo==hi) hi=Math.min(hi+8,Full.size());
                        }
                        SERVER.UsedBuffer-=Size;
                    }
                }

                //Sender->Server
                nc.write("Server : Ready to Receive");
                String ClientQueueResponse = (String) nc.read();

                if (ClientQueueResponse.equals("OK : File Transmission begining")) {
                    String Recipient = (String) nc.read();
                    Long FileSIze = (Long) nc.read();
                    String FileName = (String) nc.read();

                    if (SERVER.UsedBuffer + FileSIze > SERVER.BufferSize) nc.write("Error : Not Enough Space");
                    else {
                        nc.write("OK : Start transmission");
                        SERVER.UsedBuffer += FileSIze;

                        //Sending Chunk SIze and FileID
                        Long ChunkSize = FileSIze / 1000+1;
                        Long FileID = ++SERVER.FileCount;
                        nc.write(ChunkSize);
                        nc.write(FileID);

                        ArrayList<byte[]> Full = new ArrayList<>();
                        String Command;

                        boolean Interrupted = false;
                        Long TotalChunkSize=0L;
                        int LastChunkRecieved=0;

                        while (true) {
                            Command = (String) nc.read();
                            if (Command.equals("#Write Completed")) break;

                            //Read & DeStuff (by Server)
                            byte[] Contents = (byte[]) nc.read();

                            ArrayList<Boolean>Map=new ArrayList<>();
                            ArrayList<Boolean>Frame=new ArrayList<Boolean>();
                            Map.clear();
                            for(byte now : Contents) for(int i=7;i>=0;i--) Map.add((now & (1 << i)) != 0);

                            int Flag=0;
                            int Sum;

                            for(int i=8;i<Map.size()-8;i++){
                                Sum=0;
                                for(int j=0;j<8;j++) if(Map.get(i-1-j)==true) Sum+=1<<j;
                                if(Sum==126) {Flag++; Frame.clear();}

                                if(Flag%2==1) Frame.add(Map.get(i));

                                Sum=0;
                                for(int j=0;j<8;j++) if(Map.get(i+8-j)==true) Sum+=1<<j;
                                if(Sum==126) {
                                    Flag++;
                                    Pair<Integer,byte[]> now=ProcessBytes.DeStuff(Frame);
                                    Frame.clear();

                                    if(now.getKey().intValue()==LastChunkRecieved) {
                                        LastChunkRecieved++;
                                        TotalChunkSize += now.getValue().length;
                                        Full.add(now.getValue());
                                        nc.write(true);
                                    }
                                    else System.out.println("Error in Frame");
                                }
                            }
                        }


                        if(TotalChunkSize.equals(FileSIze)) nc.write("Success : Transmission Completed");
                        else {nc.write("Error : File Corrupted"); Interrupted=true;}

                        Queue<ArrayList<byte[]>> now;

                        if (SERVER.FileStorage.containsKey(Recipient)) now = SERVER.FileStorage.get(Recipient);
                        else now = new LinkedList<>();

                        if (!Interrupted) now.add(Full);
                        SERVER.FileStorage.put(Recipient, now);
                    }
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }

        SERVER.table.remove(UserName);
    }

}
