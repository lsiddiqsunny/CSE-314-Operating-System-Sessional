
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;

public class ConnectionUtilities
{
    public Socket socket;
    public ObjectOutputStream objectOutputStream;
    public ObjectInputStream objectInputStream;

    public ConnectionUtilities(String ip, int port)
    {
        try {
            this.socket=new Socket(ip,port);
            objectOutputStream=new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.flush();
            objectInputStream=new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public ConnectionUtilities(Socket connectionSocket)
    {
        try {
            socket=connectionSocket;
            objectOutputStream=new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.flush();
            objectInputStream=new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void write(Object o)
    {
        try {
            objectOutputStream.writeObject(o);
            objectOutputStream.flush();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    public Object read()
    {
        try {
            Object o=objectInputStream.readObject();
            return  o;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (ClassNotFoundException e)
        {
            e.getLocalizedMessage();
        }
        return  null;
    }
}