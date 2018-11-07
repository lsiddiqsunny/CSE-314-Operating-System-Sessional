package Offline_1.Server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

/**
 * Created by Shahriar Sazid on 22-Sep-17.
 */
public class StreamConnection {
    public Socket sc;
    public ObjectOutputStream os;
    public ObjectInputStream is;
    public StreamConnection(String host, int port) throws IOException {
        sc = new Socket(host, port);
        os = new ObjectOutputStream(sc.getOutputStream());
        is = new ObjectInputStream(sc.getInputStream());
    }
    public StreamConnection(Socket sc) throws IOException {
        this.sc = sc;
        os = new ObjectOutputStream(sc.getOutputStream());
        is = new ObjectInputStream(sc.getInputStream());
    }
    public Object read() {
        try {
            return is.readObject();
        } catch (SocketTimeoutException e) {
            return new Integer(-1);
        } catch (IOException e) {
            e.printStackTrace();
            return new Integer(-2);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return new Integer(-3);
        }
    }
    public void write(Object o) throws IOException {
        os.reset();
        os.writeObject(o);
    }
    public void setTimer(int milis){
        try {
            sc.setSoTimeout(milis);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
    public void exit_connection(){
        try {
            os.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
