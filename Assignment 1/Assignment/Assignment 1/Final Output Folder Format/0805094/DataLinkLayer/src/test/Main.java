package test;

import dll.DLLThread;
import dll.DataLinkLayer;
import java.io.File;
import java.io.PrintStream;
import nl.NetworkLayer;
import phy.SimPhy;

public class Main
{
  public static void main(String[] args)
    throws Exception
  {
    byte sourceMac = 26;
    byte destinationMac = 42;
    SimPhy physicalLayer = new SimPhy(sourceMac, destinationMac);
    NetworkLayer networkLayer = new NetworkLayer();
    networkLayer.setSendFile(new File("/home/s/Desktop/send"));
    DataLinkLayer dataLinkLayer = new DataLinkLayer(physicalLayer, networkLayer, sourceMac, destinationMac);
    
    DLLThread senderInstance = new DLLThread(dataLinkLayer);
    Thread senderThread = new Thread(senderInstance);
    senderThread.start();
    
    byte sourceMac2 = 42;
    byte destinationMac2 = 26;
    SimPhy physicalLayer2 = new SimPhy(sourceMac2, destinationMac2);
    NetworkLayer networkLayer2 = new NetworkLayer();
    
    DataLinkLayer dataLinkLayer2 = new DataLinkLayer(physicalLayer2, networkLayer2, sourceMac2, destinationMac2);
    
    DLLThread receiverInstance = new DLLThread(dataLinkLayer2);
    Thread receiverThread = new Thread(receiverInstance);
    System.out.println("Strting receiver thread");
    receiverThread.start();
    
    Thread.sleep(1000L);
    senderInstance.startSending();
  }
}

