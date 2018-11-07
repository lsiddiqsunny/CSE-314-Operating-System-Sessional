import java.net.Socket;

/**
 * Created by samia hossain on 9/30/2017.
 */
public class Myfile {
   public int fileid;
    public  String filename;
    public long chunksize;
    public Socket reveiversocket;

    Myfile(int a, String b, long c,Socket ip)
    {
        fileid=a;
        filename=b;
        chunksize=c;
        reveiversocket=ip;

    }
    Myfile()
    {
        fileid=-1;
        filename="null";
        chunksize=0;
        reveiversocket=null;

    }
}
