/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tcpsimpleconnection;

import java.net.UnknownHostException;
import util.ConnectionUtillities;
import util.Reader;
import util.Writer;

/**
 *
 * @author uesr
 */
public class Client {
    public static void main(String[] args) throws UnknownHostException {
       
        ConnectionUtillities connection=new ConnectionUtillities("127.0.0.1", 22222);
        
        new Thread(new Reader(connection)).start();
        new Thread(new Writer(connection)).start();
        
        while(true);
    }
}
