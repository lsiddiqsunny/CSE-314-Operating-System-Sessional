package Utilities;

import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by Rupak on 9/21/2017.
 */
public class ClientInfo {
    private int Sid;
    private InetAddress IPaddrress;
    private int clientPort;
    private Socket socket;
    private ServerUtilities server;
    private String path;

    public ClientInfo(){};
    ClientInfo(int id){ Sid = id;}
    public ClientInfo(int id, InetAddress IPaddrress, int port, ServerUtilities server, Socket socket, String path)
    {
        Sid = id;
        this.IPaddrress = IPaddrress;
        clientPort = port;
        this.server = server;
        this.socket = socket;
        this.path = path;
    }
    ClientInfo(int id,int clientPort)
    {
        Sid = id;
        this.clientPort = clientPort;
    }

    public int getSid() {
        return Sid;
    }

    public void setSid(int sid) {
        Sid = sid;
    }

    public void setIPaddrress(InetAddress IPaddrress) {
        this.IPaddrress = IPaddrress;
    }

    public void setClientPort(int clientPort) {
        this.clientPort = clientPort;
    }

    public InetAddress getIPaddrress() {

        return IPaddrress;
    }

    public int getClientPort() {
        return clientPort;
    }

    public ServerUtilities getServer() {return server;}

    public void setServer(ServerUtilities server) {
        this.server = server;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
