package prototype.commons;

import java.net.InetAddress;

public class OnlineUsers{
    public InetAddress ip; public int sid; public NetworkFx nfx; //public Socket clientSocket;
    public OnlineUsers()
    { ip = null; sid = 0; nfx = null; //clientSocket = null;
     }

    public OnlineUsers(InetAddress ip, int sid, NetworkFx nfx)
    { this.ip = ip; this.sid = sid; this.nfx = nfx;  }

}