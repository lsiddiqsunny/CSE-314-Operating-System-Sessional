package utility;

import java.io.*;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ConnectionSetup {
    public Socket sc;
    public ObjectInputStream ois;
    public ObjectOutputStream oos;
    public String username;
    
    public ConnectionSetup(String host, int port, String user){
        try {
            username = user;
            sc=new Socket(host,port);
            oos=new ObjectOutputStream(sc.getOutputStream());
            ois=new ObjectInputStream(sc.getInputStream());
        } 
        catch(Exception e)
        {
            System.out.println("Error in first ConnectionSetup.");
        }
        
    }
    
    public ConnectionSetup(Socket socket){
        try {
            sc=socket;
            oos=new ObjectOutputStream(sc.getOutputStream());
            ois=new ObjectInputStream(sc.getInputStream());
        } 
        catch(Exception e)
        {
            System.out.println("Error in second ConnectionSetup.");
        }
    }
    
    public void write(Object o)
    {
        try {
            oos.writeObject(o);
            oos.flush();
        } catch (IOException ex) {
            System.out.println("Error in ConnectionSetup write function.");
        }
    }
    
    public Object read()
    {
        Object o = null;
        try {
            o = ois.readObject();
        } catch (Exception ex) {
            //System.out.println("Error in connection setup read.");
        }
        return o;
    }
    
}
