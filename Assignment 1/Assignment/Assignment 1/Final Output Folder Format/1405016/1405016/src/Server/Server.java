package Server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Server implements Runnable{

    public static final int CHUNK_SIZE = 24*1024*1024;
    public static int REDUCED_CHUNK = 0;
    private ServerSocket welcomeSocket;
    private HashMap<String,Socket> clientList = new HashMap<>();
    private HashMap<Integer,byte[][]> files = new HashMap<>();

    Server(){
        try {
            welcomeSocket = new ServerSocket(8010);
            System.out.println("Server is open now");
        }catch (Exception e){

        }
        new Thread(this).start();
    }


    @Override
    public void run() {

        try{
            while (true){
                Socket clientSocket = welcomeSocket.accept();
                new ConnectionThread(clientSocket,clientList,files);
                //clientSocket.close();
            }
        }catch (Exception e){

        }
    }

    public static void main(String[] args) { new Server(); }

}


