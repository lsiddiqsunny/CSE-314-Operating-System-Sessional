import java.io.*;
import java.net.Socket;

/**
 * Created by samia hossain on 10/1/2017.
 */
public class ReceiveClass extends  Thread {
    public DataInputStream dis;
    public DataOutputStream dos;
    public Socket socket;

    ReceiveClass(DataInputStream dis,DataOutputStream dos,Socket socket)
    {
        this.dis=dis;
        this.dos=dos;
        this.socket=socket;
    }
    public synchronized void  run() {
        try {
            while (true) {
               // System.out.println("socket" + socket.isClosed());


                if(dis.available()!=0)System.out.println(dis.readUTF());
                else continue;
                FileOutputStream fos = new FileOutputStream("C:\\Users\\samia hossain\\desktop\\receivefolder\\idm.txt");
                //        BufferedOutputStream bos = new BufferedOutputStream(fos);
                //       InputStream is = socket.getInputStream();
                byte[] contents = new byte[1000];
                //No of bytes read in one read() call
                int bytesRead = 0;
                int id = 1;
                int n = 0;
                byte[] buf = new byte[1000];
                System.out.println("connection " + socket.isConnected());
                long size = dis.readLong();

                while (size > 0 && (n = dis.read(buf, 0, (int) Math.min(buf.length, size))) != -1) {

                    fos.write(buf, 0, n);
                    System.out.print("receiving file ... " + id + " complete!");
                    id++;
                    size -= n;
                }
                System.out.println("File saved successfully!");
               // fos.close();
            }

          //

        }
        catch(Exception e)
                {
                    e.printStackTrace();
                }


    }
}
