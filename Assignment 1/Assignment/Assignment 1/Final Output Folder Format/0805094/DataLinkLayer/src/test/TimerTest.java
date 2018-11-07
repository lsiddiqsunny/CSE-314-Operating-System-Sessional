package test;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintStream;
import javax.swing.Timer;

public class TimerTest
  implements ActionListener
{
  public TimerTest()
    throws InterruptedException
  {
    Timer t = new Timer(200, this);
    
    t.restart();
    t.setActionCommand("timer");
    Thread.sleep(1000L);
  }
  
  public void actionPerformed(ActionEvent e)
  {
    System.out.println("in action performed " + e.getActionCommand());
  }
}
