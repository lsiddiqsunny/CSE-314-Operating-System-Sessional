/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package messenger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author TIS
 */
public class ConnectionUtillities {
    public Socket sc;
    public ObjectOutputStream oos;
    public ObjectInputStream ois;
    
    
    public ConnectionUtillities(String host, int port){
        try {
            sc=new Socket(host,port);
            oos=new ObjectOutputStream(sc.getOutputStream());
            ois=new ObjectInputStream(sc.getInputStream());
        } 
        catch(IOException e)
        {
            System.out.println("Connection error : "+e);
        }
        
    }
    
    public ConnectionUtillities(Socket socket){
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
    
 
    public void write(Object ob) throws IOException {

        oos.flush();
        oos.reset();
        oos.writeObject(ob);
        

    }
    
    public Object read() throws IOException, ClassNotFoundException {

            return ois.readObject();
    }
}
