/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DataPack;

/**
 *
 * @author USER
 */
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author uesr
 */
public class ConnectionUtilities {
    public Socket sc;
    public ObjectInputStream ois;
    public ObjectOutputStream oos;
    public String roll="";
    
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
    public Socket getSocket()
    {
        return sc;
    }
    
    public void write(Object o){
        try {
            oos.writeObject(o);
            oos.flush();
        } catch (IOException ex) {
            Logger.getLogger(ConnectionUtilities.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public Object read(){
        try {
            Object o=ois.readObject();
            return o;
        } catch (IOException ex) {
            //Logger.getLogger(ConnectionUtilities.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            //Logger.getLogger(ConnectionUtilities.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex)
        {
            System.out.println("Object reading Closed");
        }
        return null;
    }
}
