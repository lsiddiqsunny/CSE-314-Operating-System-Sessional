import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import static java.lang.System.in;

/**
 * Created by Ashiqur Rahman on 9/19/2017.
 */
public class Acceptor implements Runnable{

    ServerSocket serverSocket;
    Thread thread;
    Confirmation confirmation;

    public Acceptor(ServerSocket serverSocket) {

        this.serverSocket = serverSocket;
        thread=new Thread(this);
        confirmation=new Confirmation();
        thread.start();
    }

    @Override
    public void run() {

        while(true)
        {
            try {
                Socket socket=serverSocket.accept();
                confirmation=new Confirmation();
                confirmation.Valid(socket);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
