package Student;

import java.net.SocketException;

/**
 * Created by Toufik on 10/29/2017.
 */
public class ReadAck implements Runnable{
    WorkingThread t;
    Thread th;
    ReadAck(WorkingThread t)
    {
        this.t=t;
        th = new Thread(this);
        th.start();
    }
    @Override
    public void run() {
        long time= System.currentTimeMillis();
        long end = time+1000;
        while(System.currentTimeMillis() < end) {
            try {
                t.netUtil.socket.setSoTimeout((int) (end-System.currentTimeMillis()));
                t.obj = t.netUtil.tread();
            } catch (Exception e) {
                //e.printStackTrace();
                break;
            }

        }
        t.endFlag=1;
    }
}
