package Test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

public class Server {
    public static void main(String[] args) throws IOException, InterruptedException {
        ServerSocket serverSocket = new ServerSocket(40000);
        Socket server = serverSocket.accept();
        DataOutputStream dos = new DataOutputStream(server.getOutputStream());
        DataInputStream dis = new DataInputStream(server.getInputStream());
        for (int i = 0; i < 5; i++) {
            dos.write(3*i);
            TimeUnit.SECONDS.sleep(1);
        }

        TimeUnit.SECONDS.sleep(6);
    }
}
