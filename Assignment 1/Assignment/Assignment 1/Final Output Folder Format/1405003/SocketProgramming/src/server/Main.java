package server;


import util.NetworkUtil;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by user on 9/22/2017.
 */
public class Main {

    private ServerSocket serverSocket;

    //private static ArrayList<Integer> usersOnline= new ArrayList<>();
    private static ArrayList<Integer> usersOnlineServer= new ArrayList<>();
    private static HashMap<Integer,NetworkUtil> connections = new HashMap<>();
    //private static HashMap<Integer,NetworkUtil> connectionClients = new HashMap<>();
    //private static HashMap<Integer,byte[]> tempFiles = new HashMap<>();
    private static long maxSize = 419430400;
    private static long currentSize = 0;
    private static int fileId=0;

    private Main(){

        try{
            serverSocket = new ServerSocket(44444);
            //ServerSocket serverSocketAnother = new ServerSocket(44445);
            while(true){
                Socket clientSocket = serverSocket.accept();
                NetworkUtil nc = new NetworkUtil(clientSocket);

                //Socket clientSocketAnother = serverSocket.accept();
                //NetworkUtil ncAnother = new NetworkUtil(clientSocket);
                //ObjectInputStream ois=new ObjectInputStream(clientSocketAnother.getInputStream());


                //NetworkUtil client=null;
                //while(client==null)client = Client.recent;

                //System.out.println("lala");

                int id = (int)nc.read();

                if(usersOnlineServer.contains(id))
                {
                    nc.write("u");
                    nc.closeConnection();
                    continue;
                }
                else nc.write("a");
                System.out.println("Client id " + id + " logged in successfully");
                //System.out.println("Debug");
                //client.set(id);
                //connectionClients.put(id,client);
                nc.set(id);
                //if(connections.containsKey(id))connections.remove(id);

                usersOnlineServer.add(id);
                Socket clientSocketAnother = serverSocket.accept();
                //OutputStream os = clientSocketAnother.getOutputStream();
                //ObjectOutputStream oos = new ObjectOutputStream(os);
                //oos.flush();
                NetworkUtil ncAnother = new NetworkUtil(clientSocketAnother);
                connections.put(id,ncAnother);
                //ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
                //NetworkUtil ncAnother = new NetworkUtil(clientSocket);
                //System.out.println("Allah");
                new readThreadServer(nc);
            }

        }
        catch(IOException e){

        }
    }

    public static void main(String[] args){
        new Main();
    }

    public static boolean findUser(int id){
        return usersOnlineServer.contains(id);
    }

    //public static void put(int id){
    //usersOnlineServer.add(id);
    // }


    public static void remove(int id){
        usersOnlineServer.remove((Object)id);
        //NetworkUtil nc = connections.get(id);
        //nc.closeConnection();
        connections.remove(id);
        //usersOnlineServer.remove((Helper)id);
    }

    public static boolean checkSpace(long l){

        if(l+currentSize>maxSize)return false;
        else return true;
    }

    public static int getId(){return fileId++;}

    //sender,receiver,fileName,fileSize,fileId,byte[]

    public static void process(int sender,int receiver,String fileName,long fileSize,int fileId,byte[] data){
        //currentSize+=fileSize;
        //tempFiles.put(receiver,data);
        //System.out.println(receiver);
        if(usersOnlineServer.contains(receiver)==false){
            //System.out.println("lala");
            currentSize-=fileSize;
        }
        else{
            //tempFiles.remove(receiver);


            NetworkUtil nc = connections.get(receiver);

            /*int c = nc.read(1);
            if(c==-1){
                currentSize-=fileSize;
            }*/


            nc.write(sender);
            nc.write(fileName);
            nc.write(fileSize);

            String msg = (String)nc.read();
            System.out.println(msg);
            if(msg.equals("a")) {
                //System.out.println("yuyu");
                //System.out.println(data);
                nc.writeByte(data,data.length);

            }
            currentSize-=fileSize;


            //nc.write(fileName);

        }


    }
    static void increase(int l){
        currentSize+=l;
    }
}
