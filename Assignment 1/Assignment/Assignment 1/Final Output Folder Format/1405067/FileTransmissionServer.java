package file.transmission;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileTransmissionServer {

    private final int bufferCapacity=1000000000;
    public int availableBuffer;
    private ServerSocket serverSocket=null;
    public Hashtable clients = null;
    public int fileCounter = 1;
    
    public FileTransmissionServer()
    {
        clients = new Hashtable();
        availableBuffer = bufferCapacity;
        initSocket();
    }
    
    public void initSocket()
    {
        try {
            serverSocket = new ServerSocket(3333);
            while(true)
            {
                Socket socket = serverSocket.accept();
                new MyThread(this,socket).start();
                
            }
        } catch (IOException ex) {
            Logger.getLogger(FileTransmissionServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    boolean checkOverFlow(int size)
    {
        if(availableBuffer-size>0)
        return true;
        else 
        return false;
    }
    public static void main(String[] args) {
         new FileTransmissionServer();
    }
    
}
