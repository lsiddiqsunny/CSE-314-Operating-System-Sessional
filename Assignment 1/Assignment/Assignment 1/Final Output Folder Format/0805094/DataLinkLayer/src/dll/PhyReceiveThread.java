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

import java.io.PrintStream;
import java.util.concurrent.ConcurrentLinkedQueue;
import phy.SimPhy;
import util.Printer;

public class PhyReceiveThread
  implements Runnable
{
  ConcurrentLinkedQueue<Frame> receiveBuffer;
  SimPhy physicalLayer;
  DLLLogger dLLLogger;
  boolean enablePiggyBacking;
  public static final int MAX_FRAME_SIZE = 100;
  
  public PhyReceiveThread(ConcurrentLinkedQueue<Frame> receiveBuffer, SimPhy physicalLayer, DLLLogger dLLLogger, boolean enablePiggyBacking)
  {
    this.receiveBuffer = receiveBuffer;
    this.physicalLayer = physicalLayer;
    this.dLLLogger = dLLLogger;
    this.enablePiggyBacking = enablePiggyBacking;
  }
  
  public void run()
  {
    System.out.println("Physical Layer Receive Thread Starting ...");
    for (;;)
    {
      byte[] data = new byte[100];
      System.out.println("Physical Layer Receiver waiting ...");
      int frameSize = this.physicalLayer.recv(data);
      
      Frame receivedFrame = null;
      try
      {
        receivedFrame = FrameFactory.getFrameFromBitStaffedArrayWithSize(data, frameSize);
      }
      catch (Exception ex)
      {
        if (ex.equals(Frame.CRC_MISMATCHED))
        {
          System.out.println(" ## Check sum missmatched. Ignoring the packet ## ");
          this.dLLLogger.addReceivedLog("CRC Mismatched");
        }
      }
      if (receivedFrame != null)
      {
        System.out.println("**Frame Just received in Physical Layer : " + Printer.getBasicFrameInfo(receivedFrame));
        this.dLLLogger.addReceivedLog("Received : " + Printer.getBasicFrameInfo(receivedFrame));
        if ((this.enablePiggyBacking & receivedFrame.isPiggyBackFrame()))
        {
          Frame[] result = FrameFactory.getDataAndAckFrame(receivedFrame);
          synchronized (this.receiveBuffer)
          {
            this.receiveBuffer.add(result[0]);
            this.receiveBuffer.add(result[1]);
            this.receiveBuffer.notify();
          }
        }
        else
        {
          synchronized (this.receiveBuffer)
          {
            this.receiveBuffer.add(receivedFrame);
            this.receiveBuffer.notify();
          }
        }
      }
    }
  }
}

