package sample;

import javafx.scene.control.ListView;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by DELL on 21-Sep-17.
 */
public class SecondPage implements Runnable{

    private Main main;

    public ListView<String> OnlineList;

    public List<String> StudentIds ;
    public List<String> StudentIds2;

    public HashMap<String, ObjectInputStream> mapinput;
    public HashMap<String, ObjectOutputStream> mapoutput;

    public void setMain(Main m){
        main = m;
        StudentIds = new ArrayList<>();
        StudentIds2 = new ArrayList<>();

        mapinput = new HashMap<>();
        mapoutput = new HashMap<>();


        Thread t = new Thread(this);
        t.start();
    }


    @Override
    public void run() {
        int ii = 1;
        try{
            ServerSocket serverSocket = new ServerSocket(5566);

            while (true){
                Socket socket = serverSocket.accept();

                if(ii == 1) {
                    int u = socket.getPort();
                    ClientThread ct = new ClientThread(main, socket, this, u);
                    ii = 0;
                }
                else if(ii == 0){
                    new SendToClient(main, socket, this);
                    ii = 1;
                }

            }


        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
