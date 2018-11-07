package prototype.commons;

import java.io.Serializable;
import java.net.InetAddress;

public class Message implements Serializable {

    public String msg;
    public InetAddress ip;
    public int sid;
    public long fileSize;
    public boolean type;  // type == true => sender, false =>receiver!
    public boolean b;
    public Message(){   msg = null;  ip=null; sid = 0; fileSize = 0; type = true; }
}

