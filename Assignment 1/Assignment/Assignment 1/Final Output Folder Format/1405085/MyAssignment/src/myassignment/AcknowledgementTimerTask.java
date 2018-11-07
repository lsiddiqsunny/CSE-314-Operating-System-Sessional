
package myassignment;

import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AcknowledgementTimerTask extends TimerTask{
    public int timerFlag = 0;
    public void run(){
        try {    
            Thread.sleep(1000);
            timerFlag = 1;
        } catch (InterruptedException ex) {
            Logger.getLogger(AcknowledgementTimerTask.class.getName()).log(Level.SEVERE, null, ex);
        }
  
    }
}