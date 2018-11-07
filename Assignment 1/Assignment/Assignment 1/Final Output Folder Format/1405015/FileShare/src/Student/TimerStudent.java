package Student;

import java.util.TimerTask;

/**
 * Created by Toufik on 10/28/2017.
 */
public class TimerStudent extends TimerTask {
    WorkingThread t;
    TimerStudent(WorkingThread t)
    {
        this.t=t;
    }
    @Override
    public void run() {
        t.flag=1;
        //System.out.println(flag);
    }
}
