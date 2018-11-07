package Server;

import java.util.TimerTask;

/**
 * Created by Toufik on 10/29/2017.
 */
public class TimerServer extends TimerTask{
    WorkingThread t;
    TimerServer(WorkingThread t)
    {
        this.t=t;
    }
    @Override
    public void run() {
        t.flag=1;
        //System.out.println(flag);
    }
}
