package prototype.commons;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class NetworkFx {
    public boolean noSocketException;
    public Socket socket;
    ObjectInputStream ois;
    ObjectOutputStream oos;

    public NetworkFx(){}
    public NetworkFx(Socket sock){
        noSocketException = true;
        socket = sock;
        try {
            ois = new ObjectInputStream(socket.getInputStream());
            oos = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException x) { x.printStackTrace(); noSocketException = false; }
    }

    public NetworkFx(String ip, int port) throws IOException {
        noSocketException = true;
        socket = new Socket(ip, port);
        try {
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());

        } catch (IOException x) { x.printStackTrace();noSocketException = false; }
    }

    public boolean socketIsConnected(){ return socket.isConnected(); }

    public void closeConn() throws IOException {
        if(ois!=null) ois.close();
        if(oos!=null) oos.close();
    }

    public Object readFx(){
        Object o = null;
        try{ o = ois.readObject();}
        catch (IOException x) { x.toString(); }
        catch (ClassNotFoundException x) { x.toString();  }
        return o;
    }

    public void writeFx(Object o){
        try {   oos.writeObject(o); }
        catch (IOException e) { e.printStackTrace(); }
    }

}
