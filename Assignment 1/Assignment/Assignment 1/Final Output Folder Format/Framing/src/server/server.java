package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class server {
    private static final int PORT = 8888;
    static Vector<String> activeList = new Vector<>();
    static long available = 10000000;

    public static void main(String agrs[]){

        ServerSocket serverSocket = null;
        Socket socket = null;

        //setup server
        while(serverSocket == null) {
            try {
                serverSocket = new ServerSocket(PORT);
            } catch (IOException e) {
                System.out.println(e);
            }
        }

        while(true){
            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                System.out.println(e);
            }

            new commThread(socket).start();
        }
    }
}
