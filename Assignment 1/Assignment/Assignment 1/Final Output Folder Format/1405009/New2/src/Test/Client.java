package Test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

public class Client {
    public static void main(String[] args) throws IOException, InterruptedException {
        Socket client = new Socket("localhost",40000);
        DataInputStream dis = new DataInputStream(client.getInputStream());
        DataOutputStream dos = new DataOutputStream(client.getOutputStream());
        for (int i = 0; i < 5; i++) {


            System.out.println(dis.read());
            TimeUnit.SECONDS.sleep(2);
        }
    }
}
