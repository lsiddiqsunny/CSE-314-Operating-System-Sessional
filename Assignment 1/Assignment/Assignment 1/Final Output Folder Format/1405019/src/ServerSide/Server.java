package ServerSide;

import ServerSide.CreateServer;

import java.io.IOException;
import java.net.ServerSocket;

public class Server  {
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(11111);
            new CreateServer(serverSocket);
            System.out.println("socket started successfully");

            while (true);


        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

}
//172.16.219.184
//192.168.0.102