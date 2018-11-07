package Client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by Ashiqur Rahman on 9/19/2017.
 */
public class ClinetMain {
    public static void main(String[] args) {
        Socket socket;
        try {
            socket=new Socket();
            socket.connect( new InetSocketAddress(InetAddress.getLocalHost(),9000),30000);
            new SenderReciver(socket);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
