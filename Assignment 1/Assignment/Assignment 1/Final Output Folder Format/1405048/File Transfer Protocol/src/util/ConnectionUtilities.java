/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;

/**
 *
 * @author Antu
 */
public class ConnectionUtilities implements Serializable{
    public transient Socket sc;
    public transient ObjectInputStream ois;
    public transient ObjectOutputStream oos;
    public String studentID;    //will not keep it here later, to another class
    
    public ConnectionUtilities(String host, int port){
        try {
            sc=new Socket(host,port);
            oos=new ObjectOutputStream(sc.getOutputStream());
            ois=new ObjectInputStream(sc.getInputStream());
        } 
        catch(Exception e)
        {
            e.printStackTrace();
        }
        
    }

    public Socket getSc() {
        return sc;
    }

    public ConnectionUtilities(Socket socket){
        try {
            sc=socket;
            oos=new ObjectOutputStream(sc.getOutputStream());
            ois=new ObjectInputStream(sc.getInputStream());
        } 
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public synchronized void write(Object o) throws IOException {
        oos.flush();
        oos.reset();
        oos.writeObject(o);
    }
    
    public synchronized Object read() throws IOException, ClassNotFoundException {

        Object o=ois.readObject();

        return o;
    }
}
