/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package transmissionUtilities;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author uesr
 */
public class ConnectionUtilities {

    public Socket sc;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    public boolean isClosed = true;

    public ConnectionUtilities (String host, int port) {
        try {
            sc = new Socket (host, port);
            ois = new ObjectInputStream (sc.getInputStream ());
            oos = new ObjectOutputStream (sc.getOutputStream ());
            isClosed = false;
        }
        catch (Exception e) {
            e.printStackTrace ();
        }

    }

    public ConnectionUtilities (Socket socket) {
        try {
            sc = socket;
            oos = new ObjectOutputStream (sc.getOutputStream ());
            ois = new ObjectInputStream (sc.getInputStream ());
            isClosed = false;
        }
        catch (Exception e) {
            e.printStackTrace ();
        }
    }

    public void close () {
        try {
            ois.close();
            oos.close();
            sc.close();
            isClosed = true;
        } catch (Exception e)
        {
            System.out.println("Exception occurred when trying to close client socket.");
        }
    }

    public void write (Object o) {
        try {
            oos.writeObject (o);
        }
        catch (IOException ex) {
            System.out.println ("Error writing a message in the socket. Most probable cause is client disconnected.");
            Logger.getLogger (ConnectionUtilities.class.getName ()).log (Level.SEVERE, null, ex);
        }
    }

    public Object read () throws SocketTimeoutException {
        try {
            return ois.readObject ();
        }
        catch (IOException | ClassNotFoundException ex) {
            System.out.println ("Timeout occurred during acknowledgement reception.");
            return new byte[]{(byte) 0b01111110, (byte) 0b11111111, (byte) 0b01111110};
        }
    }

    public void writeString (String str) {
        try {
            oos.writeObject (str);
        }
        catch (IOException ex) {
            System.out.println ("Error writing a message to the client. Most probable cause is client disconnected.");
        }
    }

    public String readString () {
        try {
            return ois.readObject ().toString ();
        }
        catch (IOException | ClassNotFoundException ex) {
            return "disconnect";
        }
    }

    public String readStringTimeout () throws SocketTimeoutException {
        try {
            return ois.readObject ().toString ();
        }
        catch (IOException | ClassNotFoundException ex) {
            System.out.println ("File transmission timeout has occurred.");
            return "Transmission canceled.";
        }
    }


}
