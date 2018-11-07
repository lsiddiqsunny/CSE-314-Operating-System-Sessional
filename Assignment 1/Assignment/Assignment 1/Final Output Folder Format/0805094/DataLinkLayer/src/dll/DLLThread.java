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

import controller.Controller;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintStream;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Timer;
import nl.NetworkLayer;
import phy.SimPhy;
import util.Printer;

public class DLLThread
  implements Runnable, ActionListener, DLLLogger
{
  private DataLinkLayer dll;
  private Vector<FrameBufferElement> dllSendBuffer;
  private ConcurrentLinkedQueue<Frame> plSendBuffer;
  private int windowHead;
  private static final int WINDOW_COUNT = 4;
  private static final int BUFFER_LENGTH = 8;
  private Timer[] timerArray;
  private static final int TIME_OUT_DELAY = 2000;
  private boolean noMorePacketFlag;
  private Vector<Frame> dllReceivedBuffer;
  private ConcurrentLinkedQueue<Frame> plReceivedBuffer;
  private boolean nakFlag;
  private int receiverExpectedFrameSequence;
  private PhySendThread phySendThread;
  private PhyReceiveThread phyReceiveThread;
  private boolean enablePiggyBacking;
  
  public DLLThread(DataLinkLayer dataLinkLayer)
  {
    this.dll = dataLinkLayer;
    this.dllSendBuffer = new Vector(8);
    this.plSendBuffer = new ConcurrentLinkedQueue();
    this.timerArray = new Timer[8];
    for (int i = 0; i < this.timerArray.length; i++)
    {
      this.timerArray[i] = new Timer(2000, this);
      
      this.timerArray[i].setActionCommand(String.valueOf(i));
    }
    this.dllReceivedBuffer = new Vector(8);
    for (int i = 0; i < 8; i++) {
      this.dllReceivedBuffer.add(null);
    }
    this.plReceivedBuffer = new ConcurrentLinkedQueue();
    this.receiverExpectedFrameSequence = 0;
    this.nakFlag = false;
    this.noMorePacketFlag = true;
    
    this.enablePiggyBacking = true;
    this.phyReceiveThread = new PhyReceiveThread(this.plReceivedBuffer, this.dll.physicalLayer, this, this.enablePiggyBacking);
    this.phySendThread = new PhySendThread(this.plSendBuffer, this.dll.physicalLayer, this, this.enablePiggyBacking);
  }
  
  public synchronized void sendFrameByFrameSequence(int frameSequence)
  {
    this.timerArray[frameSequence].restart();
    synchronized (this.plSendBuffer)
    {
      this.plSendBuffer.add(this.dllSendBuffer.get(frameSequence));
      ((FrameBufferElement)this.dllSendBuffer.get(frameSequence)).setStats(FrameBufferElement.Status.SEND);
      this.plSendBuffer.notify();
    }
  }
  
  public synchronized void resendFrameByFrameSequence(int frameSequence)
  {
    this.timerArray[frameSequence].restart();
    synchronized (this.plSendBuffer)
    {
      this.plSendBuffer.add(this.dllSendBuffer.get(frameSequence));
      ((FrameBufferElement)this.dllSendBuffer.get(frameSequence)).setStats(FrameBufferElement.Status.SEND);
      this.plSendBuffer.notify();
    }
  }
  
  public synchronized void slideForAckSequence(int ackSequence)
  {
    if ((ackSequence < 0) || (ackSequence > 7)) {
      return;
    }
    System.out.println("^^^ Sliding for AckSequence : " + ackSequence + " ^^^");
    System.out.println("Sending flag: " + this.noMorePacketFlag);
    
    int newWindowHead = (ackSequence + 1) % 8;
    int oldWindowHead = this.windowHead;
    if (newWindowHead < oldWindowHead)
    {
      newWindowHead += 8;
    }
    else if (newWindowHead == oldWindowHead)
    {
      System.out.println("New and Old window location is same no sliding");
      return;
    }
    System.out.println("Window head [Old head: " + oldWindowHead + ", New head: " + newWindowHead + "]");
    for (int i = oldWindowHead; i < newWindowHead; i++)
    {
      int insertLocation = i % 8;
      System.out.println("Insert Location : " + insertLocation + " No More Packet Flag " + this.noMorePacketFlag);
      System.out.println("Refreshing in send buffer");
      
      byte[] packet = null;
      if (!this.noMorePacketFlag)
      {
        FrameBufferElement fbe = null;
        try
        {
          packet = this.dll.networkLayer.getNextPacket();
          fbe = FrameFactory.getFrameBufferElementByFrameSequence(this.dll.sourceMac, this.dll.destinationMac, insertLocation, packet, FrameBufferElement.Status.NOT_SEND);
          
          fbe.setPacketNumber(this.dll.networkLayer.getLastSendPacketNumber());
        }
        catch (Exception ex)
        {
          Logger.getLogger(DLLThread.class.getName()).log(Level.SEVERE, null, ex);
          if (ex.equals(NetworkLayer.NO_MORE_PACKET))
          {
            System.out.println("Got NO_MORE_PACKET from Network Layer. Adding empty payload DATA_FRAME");
            
            fbe = FrameFactory.getEOFFrameBufferElement(this.dll.sourceMac, this.dll.destinationMac, i, FrameBufferElement.Status.NOT_SEND);
            try
            {
              int nextPacketCount = this.dll.networkLayer.getLastSendPacketNumber() + 1;
              fbe.setPacketNumber(nextPacketCount);
            }
            catch (Exception e)
            {
              e.printStackTrace();
            }
            this.noMorePacketFlag = true;
          }
        }
        System.out.println("Setting new frame in: " + insertLocation + " " + Printer.getFrameBufferElementStatus(fbe));
        
        this.timerArray[insertLocation].stop();
        
        this.dllSendBuffer.set(insertLocation, fbe);
      }
      else
      {
        System.out.println("NO_MORE_FLAG true so inserting null into buffer");
        System.out.println("Insert location: " + insertLocation);
        
        this.timerArray[insertLocation].stop();
        if (((FrameBufferElement)this.dllSendBuffer.get(insertLocation)).isEOFFrame()) {
          sendEOFSuccessful();
        }
        this.dllSendBuffer.set(insertLocation, null);
      }
    }
    this.windowHead = (newWindowHead % 8);
    System.out.println("New Window head: " + this.windowHead);
    sendNotSendFrame();
  }
  
  public synchronized void sendNotSendFrame()
  {
    System.out.println("in function sendNotSendFrame from " + this.windowHead + " to " + (this.windowHead + 4));
    for (int i = this.windowHead; i < this.windowHead + 4; i++)
    {
      int curPointer = i % 8;
      FrameBufferElement tempFBE = (FrameBufferElement)this.dllSendBuffer.get(curPointer);
      System.out.println("Rotating on position .... .... ... " + curPointer + " " + Printer.getFrameBufferElementStatus(tempFBE));
      if ((tempFBE != null) && (tempFBE.getStats().equals(FrameBufferElement.Status.NOT_SEND))) {
        sendFrameByFrameSequence(curPointer);
      }
    }
  }
  
  public synchronized void initializeDllSendBuffer()
  {
    System.out.println("Initialize Sender buffer ");
    this.windowHead = 0;
    for (int i = 0; i < 8; i++) {
      try
      {
        byte[] packet = this.dll.networkLayer.getNextPacket();
        FrameBufferElement fbe = FrameFactory.getFrameBufferElementByFrameSequence(this.dll.sourceMac, this.dll.destinationMac, i, packet, FrameBufferElement.Status.NOT_SEND);
        
        fbe.setPacketNumber(this.dll.networkLayer.getLastSendPacketNumber());
        this.dllSendBuffer.add(fbe);
      }
      catch (Exception ex)
      {
        if (ex.equals(NetworkLayer.NO_MORE_PACKET))
        {
          System.out.println("NO_MORE_PACKET in sender initialization. Adding empty Payload DATA_FRAME");
          FrameBufferElement fbe = FrameFactory.getEOFFrameBufferElement(this.dll.sourceMac, this.dll.destinationMac, i, FrameBufferElement.Status.NOT_SEND);
          
          fbe.setPacketNumber(this.dll.networkLayer.getLastSendPacketNumber() + 1);
          this.dllSendBuffer.add(fbe);
          break;
        }
      }
    }
    int size = this.dllSendBuffer.size();
    if (size < 8) {
      for (int i = 0; i < 8 - size; i++)
      {
        this.dllSendBuffer.add(null);
        System.out.println("Adding null to buffer .....");
      }
    }
    sendNotSendFrame();
  }
  
  public synchronized void printDllSendBuffer()
  {
    System.out.println(this.dllSendBuffer);
  }
  
  public void actionPerformed(ActionEvent e)
  {
    if ((e.getSource() instanceof Timer))
    {
      int timerIndex = Integer.parseInt(e.getActionCommand());
      System.out.println("Time out Occurs in : " + timerIndex);
      this.timerArray[timerIndex].stop();
      resendFrameByFrameSequence(timerIndex);
    }
  }
  
  public synchronized void startSending()
  {
    this.noMorePacketFlag = false;
    initializeDllSendBuffer();
  }
  
  public void run()
  {
    System.out.println("DataLink Layer Send Thread started");
    Thread phyST = new Thread(this.phySendThread);
    phyST.start();
    Thread phyRT = new Thread(this.phyReceiveThread);
    phyRT.start();
    for (;;)
    {
      Frame justReceived;
      synchronized (this.plReceivedBuffer)
      {
        if (this.plReceivedBuffer.isEmpty()) {
          try
          {
            System.out.println("DataLink Layer is waiting for a frame arrival");
            this.plReceivedBuffer.wait();
          }
          catch (InterruptedException ex)
          {
            Logger.getLogger(DLLThread.class.getName()).log(Level.SEVERE, null, ex);
          }
        }
        justReceived = (Frame)this.plReceivedBuffer.poll();
      }
      System.out.println("Just received frame in DLL " + Printer.getBasicFrameInfo(justReceived));
      if (justReceived.equals(Frame.CRC_MISMATCHED_FRAME)) {
        sendNak(this.receiverExpectedFrameSequence);
      } else if (justReceived.isDataFrame()) {
        dataFrameReceived(justReceived);
      } else if (justReceived.isAckFrame()) {
        slideForAckSequence(justReceived.getAckSequence());
      } else if (justReceived.isNakFrame()) {
        resendFrameByFrameSequence(justReceived.getAckSequence());
      }
    }
  }
  
  public synchronized void fillReceiverBuffer(Frame frame)
  {
    if (frame.isDataFrame())
    {
      int frameSequence = frame.getFrameSequence();
      this.dllReceivedBuffer.set(frameSequence, frame);
    }
  }
  
  int receiverPacketCounter = 0;
  Frame lastSuccessfullAck;
  
  public synchronized void slideReceiverWindow(int frameSequence)
  {
    System.out.println("In receiver slide window");
    int newreceiverExpectedFrameSequence = this.receiverExpectedFrameSequence;
    for (int i = frameSequence; i < frameSequence + 8; i++)
    {
      int curPointer = i % 8;
      
      Frame f = (Frame)this.dllReceivedBuffer.get(curPointer);
      if (f == null)
      {
        System.out.println("Is null " + curPointer);
        
        newreceiverExpectedFrameSequence = curPointer;
        System.out.println("setting new expected " + curPointer);
        
        break;
      }
    }
    int temp = newreceiverExpectedFrameSequence;
    if (newreceiverExpectedFrameSequence < this.receiverExpectedFrameSequence) {
      temp = newreceiverExpectedFrameSequence + 8;
    }
    for (int i = this.receiverExpectedFrameSequence; i < temp; i++)
    {
      int curPointer = i % 8;
      System.out.println("value of pointer: " + curPointer);
      this.receiverPacketCounter += 1;
      System.out.println("Sending to Netwrok layer packet Number: " + this.receiverPacketCounter);
      
      appendPacket(((Frame)this.dllReceivedBuffer.get(curPointer)).getPayLoad());
      if (((Frame)this.dllReceivedBuffer.get(curPointer)).isEOFFrame()) {
        receivedEOFSuccessful();
      }
      this.dllReceivedBuffer.set(curPointer, null);
    }
    this.receiverExpectedFrameSequence = newreceiverExpectedFrameSequence;
    this.nakFlag = false;
    
    int sendAckNo = this.receiverExpectedFrameSequence == 0 ? 7 : this.receiverExpectedFrameSequence - 1;
    sendAck(sendAckNo);
    System.out.println("Sending ack of " + sendAckNo);
  }
  
  /**
   * @deprecated
   */
  public synchronized void setReceiverExpectedFrameSequence(int receiverExpectedFrameSequence)
  {
    this.receiverExpectedFrameSequence = receiverExpectedFrameSequence;
  }
  
  public synchronized boolean inBetweenExpected(int x)
  {
    int end = this.receiverExpectedFrameSequence + 4;
    for (int i = this.receiverExpectedFrameSequence; i < end; i++) {
      if (x == i % 8) {
        return true;
      }
    }
    return false;
  }
  
  public synchronized void dataFrameReceived(Frame frame)
  {
    if (inBetweenExpected(frame.getFrameSequence())) {
      fillReceiverBuffer(frame);
    }
    int frameSeq = frame.getFrameSequence();
    if (frameSeq == this.receiverExpectedFrameSequence) {
      slideReceiverWindow(frameSeq);
    } else if (this.nakFlag) {
      sendLastSuccessfullAck();
    } else {
      sendNak(this.receiverExpectedFrameSequence);
    }
  }
  
  public synchronized void sendAck(int ackSequence)
  {
    this.lastSuccessfullAck = FrameFactory.getAckFrame(this.dll.sourceMac, this.dll.destinationMac, ackSequence);
    System.out.println("Ack sequence :  " + ackSequence);
    synchronized (this.plSendBuffer)
    {
      this.plSendBuffer.add(this.lastSuccessfullAck);
      this.plSendBuffer.notify();
    }
  }
  
  public synchronized void sendLastSuccessfullAck()
  {
    if (this.lastSuccessfullAck != null)
    {
      synchronized (this.plSendBuffer)
      {
        this.plSendBuffer.add(this.lastSuccessfullAck);
        this.plSendBuffer.notify();
      }
    }
    else
    {
      System.out.println("Very Interesting case. NAK before any ACK :). Sending NAK 0");
      sendNak(0);
    }
  }
  
  public synchronized void sendNak(int ackSequence)
  {
    Frame nakFrame = FrameFactory.getNakFrame(this.dll.sourceMac, this.dll.destinationMac, ackSequence);
    synchronized (this.plSendBuffer)
    {
      this.plSendBuffer.add(nakFrame);
      this.plSendBuffer.notify();
    }
    this.nakFlag = true;
  }
  
  public void addSendLog(String logMessage)
  {
    if (this.dll.controller != null) {
      this.dll.controller.addSendLog(logMessage);
    }
  }
  
  public void addReceivedLog(String logMessage)
  {
    if (this.dll.controller != null) {
      this.dll.controller.addReceivedLog(logMessage);
    }
  }
  
  public void sendEOFSuccessful()
  {
    if (this.dll.controller != null) {
      this.dll.controller.sendEOFSuccessful();
    }
  }
  
  public void receivedEOFSuccessful()
  {
    if (this.dll.controller != null) {
      this.dll.controller.receivedEOFSuccessful();
    }
  }
  
  public void appendPacket(byte[] packet)
  {
    this.dll.networkLayer.appendNextPacket(packet);
    this.dll.controller.appendPacket(packet);
  }
  
  public static void main(String[] args)
    throws Exception
  {
    byte sourceMac = 26;
    byte destinationMac = 42;
    SimPhy physicalLayer = new SimPhy(sourceMac, destinationMac);
    NetworkLayer networkLayer = new NetworkLayer();
    networkLayer.setSendFile(new File("/home/s/Desktop/send5"));
    networkLayer.setReceiveFile(new File("/home/s/Desktop/receive.temp2"));
    DataLinkLayer dataLinkLayer = new DataLinkLayer(physicalLayer, networkLayer, sourceMac, destinationMac);
    
    DLLThread senderInstance = new DLLThread(dataLinkLayer);
    Thread senderThread = new Thread(senderInstance);
    senderThread.start();
    
    Thread.sleep(1000L);
    senderInstance.startSending();
  }
}

