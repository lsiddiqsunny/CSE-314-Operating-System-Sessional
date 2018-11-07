package ServerSide;

import ServerSide.ConnectionInfo;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class CreateServer implements Runnable{
    ServerSocket serverSocket;
    Socket client;

    String[][] data;

    static ArrayList<ConnectionInfo> conInfos;
    //static ArrayList<Frame> frames;

    CreateServer(ServerSocket ss) throws IOException {
        serverSocket = ss;
       // fileInfos = new ArrayList<FileInfo>(10);
        conInfos = new ArrayList<ConnectionInfo>(10);
        //frames
        //frames =new ArrayList<Frame>(100);


        data = new String[5][10];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 10; j++) {
                data[i][j] = "";
            }
        }

        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        while(true) {
            try {
                client = serverSocket.accept();
                new FetchAddress(client,data);
                } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
