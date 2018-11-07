package Server;

import Tools.NetworkUtil;
import com.sun.prism.shader.Solid_RadialGradient_REFLECT_AlphaTest_Loader;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Created by Toufik on 9/22/2017.
 */
public class ServerThread implements  Runnable{

    public ServerSocket serverSocket = new ServerSocket(23234);
    public ServerGUIController1 controller1;
    public long buffer;
    public long available;
    public Hashtable<String,ArrayList<byte[]>> fileChunk = new Hashtable<>();
    public Hashtable<String,NetworkUtil> studentConnectionList = new Hashtable<>();
    Thread thread;

    public ServerThread(ServerGUIController1 controller1) throws IOException {
        this.controller1 =controller1;
        this. buffer = controller1.bufferSize*1000;
        this.available = buffer;
        controller1.port.setText("23234");
        controller1.IPtext.setText(InetAddress.getLocalHost().getHostAddress());
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        try {
            while(true) {
                Socket studentSocket = serverSocket.accept();
                NetworkUtil netUlit = new NetworkUtil(studentSocket);
                String id = (String) netUlit.read();
                //String ip = (String) netUlit.read();
                if(!studentConnectionList.containsKey(id))
                {
                    String msz = "Successfully Connected";
                    studentConnectionList.put(id,netUlit);
                    netUlit.write(msz);
                    controller1.log.appendText(msz+" "+id+"\n");
                    new WorkingThread(this,id,netUlit);
                }
                else
                {
                    String msz = "Connection Failed";
                    netUlit.write(msz);
                    controller1.log.appendText("Connection rejected for "+id+"\n");
                    netUlit.closeConnection();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized long Avialable()
    {
        return available;
    }
    public synchronized void Decrese(long l)
    {
        available-=l;
    }
    public synchronized void Increase(long l)
    {
        available+=l;
    }
}
