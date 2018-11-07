package Tools;

/**
 * Created by Toufik on 9/21/2017.
 */

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class NetworkUtil
{
    public Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public NetworkUtil(String s, int port) {
        try {
            this.socket=new Socket(s,port);
            out=new ObjectOutputStream(socket.getOutputStream());
            in=new ObjectInputStream(socket.getInputStream());
        } catch (Exception e) {
            //System.out.println("In NetworkUtil : " + e.toString());
        }
    }

    public NetworkUtil(Socket s) {
        try {
            this.socket = s;
            in=new ObjectInputStream(socket.getInputStream());
            out=new ObjectOutputStream(socket.getOutputStream());
        } catch (Exception e) {
            //System.out.println("In NetworkUtil : " + e.toString());
        }
    }

    public Object read() {
        Object o = null;
        try {
            o=in.readObject();
        } catch (Exception e) {
            //System.out.println("Reading Error in network : " + e.toString());
        }
        return o;
    }

    public Object tread() throws InterruptedIOException {
        Object o = null;
        try {
            o=in.readObject();
        } catch (InterruptedIOException e) {
            throw e;
        }
        catch (IOException ioe)
        {
        }
        catch (ClassNotFoundException e) {
            //e.printStackTrace();
        }
        return o;
    }

    public void write(Object o) {
        try {
            out.writeObject(o);
        } catch (IOException e) {
            //System.out.println("Writing  Error in network : " + e.toString());
        }
    }

    public void ewrite(Object o) throws Exception {
        try {
            out.writeObject(o);
        } catch (Exception e) {
            //System.out.println("Writing  Error in network : " + e.toString());
            throw e;
        }
    }


    public void closeConnection() {
        try {
            out.close();
            in.close();
        } catch (Exception e) {
            //System.out.println("Closing Error in network : "  + e.toString());
        }
    }
}


