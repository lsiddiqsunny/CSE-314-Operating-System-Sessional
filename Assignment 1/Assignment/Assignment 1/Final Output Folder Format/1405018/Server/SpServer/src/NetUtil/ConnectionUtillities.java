/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package NetUtil;

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
public class ConnectionUtillities {
    public Socket sc;
    public ObjectInputStream ois;
    public ObjectOutputStream oos;
    
    public ConnectionUtillities(String host, int port){
        try {
            sc=new Socket(host,port);
            oos=new ObjectOutputStream(sc.getOutputStream());
            ois=new ObjectInputStream(sc.getInputStream());
        } 
        catch(IOException e)
        {
        }
        
    }
    
    public ConnectionUtillities(Socket socket){
        try {
            sc=socket;
            ois=new ObjectInputStream(sc.getInputStream());
            oos=new ObjectOutputStream(sc.getOutputStream());
            
        } 
        catch(IOException e)
        {
        }
    }
    
    public void write(Object o){
        try {
            oos.writeObject(o);
        } catch (IOException ex) {
            //Logger.getLogger(ConnectionUtillities.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public Object read(){
        try {
            Object o=ois.readObject();
            return o;
        } catch (IOException | ClassNotFoundException ex) {
            //Logger.getLogger(ConnectionUtillities.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public void Close() throws IOException
    {
        oos.close();
        ois.close();
        sc.close();
    }
    
    
}
