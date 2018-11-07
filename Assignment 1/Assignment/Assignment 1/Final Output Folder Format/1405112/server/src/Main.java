import java.io.BufferedInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

    public static void main(String[] args) {
        try {
            Server server= new Server();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
