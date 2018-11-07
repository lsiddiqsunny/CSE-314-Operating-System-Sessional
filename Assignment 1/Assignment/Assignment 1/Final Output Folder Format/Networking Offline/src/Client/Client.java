package Client;

import Utility.NetworkUtil;
import Utility.ProcessBytes;
import javafx.util.Pair;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.*;

public class Client
{
    private NetworkUtil nc;
    ArrayList<byte[]> FileStorage=new ArrayList<>();
    public Queue< Pair<String,String> >TobeSent=new LinkedList<>();

    private Client() throws IOException {
        try {

            String serverAddress="127.0.0.1";
            int serverPort=33333;
            nc = new NetworkUtil(serverAddress,serverPort);

            //Sending Username
            System.out.print("-----Log In-----\nUsername : ");
            String UserName=new Scanner(System.in).nextLine();
            nc.write(UserName);

            //Receiving LogIn Response
            String LogInResponse= (String) nc.read();
            System.out.println(LogInResponse);

            if(LogInResponse.equals("Error : LogIn from Multiple IP denied")) nc.closeConnection();
            else {
                new InputThread(this);

                while (true){
                    //Server->Recipient
                    nc.write("Client : Ready to Receive");
                    String ServerQueueResponse = (String) nc.read();

                    if (ServerQueueResponse.equals("OK : File Transmission begining")) {

                        FileOutputStream fos = new FileOutputStream(UserName+".txt");
                        BufferedOutputStream bos = new BufferedOutputStream(fos);

                        int MaxSize=(int) nc.read();
                        int LastChunkRecieved=0;

                        while (true){
                            if(LastChunkRecieved==MaxSize) break;
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
                                        bos.write(now.getValue(), 0, now.getValue().length);
                                        bos.flush();
                                        nc.write(true);
                                    }
                                    else System.out.println("Error in Frame");
                                }
                            }
                        }
                    }


                    //Sender->Server
                    String ServerResponse = (String) nc.read();
                    if (ServerResponse.equals("Server : Ready to Receive")) {
                        if (TobeSent.size() == 0) nc.write("Error : Nothing to Send");
                        else {
                            nc.write("OK : File Transmission begining");
                            Pair<String, String> now = TobeSent.remove();
                            String Recipient = now.getKey();
                            String FilePath = now.getValue();
                            File FILE = new File(FilePath);


                            //Sending Recipient Name,File Size and FIle Name
                            nc.write(Recipient);
                            Long Size = FILE.length();
                            String FileName = FILE.getName();
                            nc.write(Size);
                            nc.write(FileName);


                            //Receiving Size Response
                            String SizeResponse = (String) nc.read();
                            if (SizeResponse.equals("OK : Start transmission")) {

                                //Beginning File Transmission
                                Long ChunkSize = (Long) nc.read();
                                Long FileID = (Long) nc.read();

                                FileInputStream fis = new FileInputStream(FILE);
                                BufferedInputStream bis = new BufferedInputStream(fis);
                                long FileSize = FILE.length();
                                long current = 0;

                                //Read the entire file and store
                                FileStorage.clear();
                                boolean ErrorIntroduced=false;
                                int ErrorIndex=new Random().nextInt(3)+3;

                                while (current != FileSize) {
                                    if (FileSize-current >= ChunkSize) current += ChunkSize;
                                    else {ChunkSize = FileSize - current; current = FileSize;}
                                    byte[] contents = new byte[ChunkSize.intValue()];
                                    bis.read(contents, 0, ChunkSize.intValue());
                                    FileStorage.add(contents);
                                }

                                    nc.socket.setSoTimeout(30000);

                                    //Go-Back 8 protocol
                                    int lo=0;
                                    int hi=Math.min(8,FileStorage.size());

                                    while(lo!=hi){
                                        for(int i=lo;i<hi;i++){
                                            nc.write("#Write Ongoing");

                                            if(i==ErrorIndex && !ErrorIntroduced){
                                                ErrorIntroduced=true;
                                                nc.write(ProcessBytes.BitStuff(i,FileStorage.get(i),true));
                                            }
                                            else nc.write(ProcessBytes.BitStuff(i,FileStorage.get(i),false));
                                        }

                                        nc.socket.setSoTimeout(10000);
                                        try{ for(int i=lo;i<hi;i++) {nc.read(); lo++;} }
                                        catch (SocketTimeoutException e) {}

                                        if(lo==hi) hi=Math.min(hi+8,FileStorage.size());
                                    }

                                    nc.write("#Write Completed");
                                    nc.read();

                                    nc.socket.setSoTimeout(0);
                            }
                            else TobeSent.add(new Pair<>(Recipient, FilePath));
                        }
                    }
                }
            }

        } catch(Exception e) {
            System.out.println (e);
        }
    }

    public static void main(String[] args) throws IOException { Client C=new Client(); }
}

