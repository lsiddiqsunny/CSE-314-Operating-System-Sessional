import java.io.*;
import java.net.Socket;

class WkerThread implements Runnable
{
    private Socket socket;
    private File file;
    private int id = 0;
    NetworkUtil nc;
    int roll;
    ObjectInputStream inputStream;
    Server server;
    public WkerThread(Server server)
    {
        this.server=server;
        //this.roll=rr;
    }

    public void run()
    {
        server.downloadFiles(server.socket);
        try
        {
            nc.closeConnection();
        }
        catch(Exception e)
        {

        }

        server.workerThreadCount--;
        System.out.println("Client [" + id + "] is now terminating. No. of worker threads = "
                + server.workerThreadCount);
    }
}
