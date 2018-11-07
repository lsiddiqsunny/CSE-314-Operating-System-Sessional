/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dll;

/**
 *
 * @author Tanzim Ahmed
 */

import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import phy.SimPhy;
import util.Printer;

public class PhySendThread
  implements Runnable
{
  ConcurrentLinkedQueue<Frame> sendBuffer;
  SimPhy physicalLayer;
  DLLLogger dllLogger;
  boolean enablePiggyBacking;
  
  public PhySendThread(ConcurrentLinkedQueue<Frame> sendBuffer, SimPhy physicalLayer, DLLLogger dllLogger, boolean enablePiggyBacking)
  {
    this.sendBuffer = sendBuffer;
    this.physicalLayer = physicalLayer;
    this.dllLogger = dllLogger;
    this.enablePiggyBacking = enablePiggyBacking;
  }
  
  public void run()
  {
    for (;;)
    {
      synchronized (this.sendBuffer)
      {
        if (this.sendBuffer.isEmpty()) {
          try
          {
            System.out.println("Physical Layer Sender Waiting ....");
            this.sendBuffer.wait();
          }
          catch (InterruptedException ex)
          {
            Logger.getLogger(DLLThread.class.getName()).log(Level.SEVERE, null, ex);
          }
        }
        try
        {
          Frame temp = (Frame)this.sendBuffer.poll();
          Frame temp2 = temp;
          if ((this.enablePiggyBacking & !this.sendBuffer.isEmpty())) {
            if ((temp.isAckFrame()) && (((Frame)this.sendBuffer.peek()).isDataFrame()))
            {
              temp2 = (Frame)this.sendBuffer.poll();
              
              temp2 = FrameFactory.getPiggyBackFrame(temp2, temp);
            }
            else if ((temp2.isDataFrame()) && (((Frame)this.sendBuffer.peek()).isAckFrame()))
            {
              temp = (Frame)this.sendBuffer.poll();
              
              temp2 = FrameFactory.getPiggyBackFrame(temp2, temp);
            }
          }
          byte[] b = temp2.getBitStuffedFrame();
          System.out.println("Sending ... " + Printer.getBasicFrameInfo(temp2));
          this.dllLogger.addSendLog("Sending: " + Printer.getBasicFrameInfo(temp2));
          this.physicalLayer.send(b, b.length);
        }
        catch (IOException ex)
        {
          Logger.getLogger(DLLThread.class.getName()).log(Level.SEVERE, null, ex);
          System.out.println("Exception in Physical Layer Unexpected");
        }
        try
        {
          Thread.sleep(100L);
        }
        catch (InterruptedException ex)
        {
          Logger.getLogger(PhySendThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (!this.sendBuffer.isEmpty()) {}
      }
    }
  }
}
