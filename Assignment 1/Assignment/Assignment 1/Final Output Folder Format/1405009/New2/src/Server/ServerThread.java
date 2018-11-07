package Server;

import Utilities.ClientInfo;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Rupak on 9/29/2017.
 */
public class ServerThread implements Runnable {

    public HashMap<Integer, ClientInfo> clientList;
    public HashMap<Integer,FileTransmission>fileList;
    public final int maxBufferSize;
    int port;
    ServerSocket serverSocket;

    public ServerThread()
    {
        maxBufferSize = 1024*1024*10;
        clientList = new HashMap();
        fileList = new HashMap();
        port = 33330;

        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.out.println(e);
        }
        new Thread(this).start();
    }

    @Override
    public void run() {
        System.out.println("Server has started");
        while(true) {
            try {
                Socket connectionSocket = serverSocket.accept();
                new CreateClientConnection(connectionSocket,clientList,fileList,maxBufferSize);
                System.out.println("Client [" + connectionSocket.getPort() + "] is now connected.\n");
            } catch (IOException ex) {
                Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static void main(String[] args) {
        new ServerThread();
    }
}
