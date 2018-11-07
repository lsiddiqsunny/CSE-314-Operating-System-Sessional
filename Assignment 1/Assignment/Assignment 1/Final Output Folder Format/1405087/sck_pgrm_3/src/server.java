import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

/**
 * Created by maksudul_mimon on 9/25/2017.
 */
public class server {


    public static void main(String [] args) {
        ServerSocket ser_socekt = null;
        Socket socket = null;

        HashMap<Integer, Socket> hashmap = new HashMap<Integer, Socket>();
        HashMap<Integer, Integer> hash_fId_fSize = new HashMap<Integer, Integer>();
        HashMap<Integer, String> hash_not_available = new HashMap<Integer, String>();
        HashMap<Integer, Integer> hash_fId_ftart = new HashMap<Integer, Integer>();
        HashMap<Integer, String> hash_fId_fName = new HashMap<Integer, String>();
        byte [] ser_buffer = null;
        int buffer_length;

        try {
            ser_socekt = new ServerSocket(12345);
        } catch (IOException e) {
            e.printStackTrace();
        }

        buffer_length = 3000;
        ser_buffer = new byte[buffer_length];

        while(true){
            try {
                socket= ser_socekt.accept();
            } catch (IOException e) {
                e.printStackTrace();
            }

            new serverThread(socket,hashmap,hash_fId_fSize,hash_not_available,hash_fId_ftart,hash_fId_fName,ser_buffer).start();

        }
    }
}
