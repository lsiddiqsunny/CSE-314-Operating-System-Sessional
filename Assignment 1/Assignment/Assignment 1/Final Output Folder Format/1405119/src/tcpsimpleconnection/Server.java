/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tcpsimpleconnection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import util.ConnectionUtillities;
import util.Reader;
import util.Writer;

/**
 *
 * @author uesr
 */
public class Server {
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket=new ServerSocket(22222);
            
            Socket clientSocket=serverSocket.accept();
            
            ConnectionUtillities connection=new ConnectionUtillities(clientSocket);
            
            new Thread(new Reader(connection)).start();
            new Thread(new Writer(connection)).start();
            
            while(true);
                        
            
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
}
