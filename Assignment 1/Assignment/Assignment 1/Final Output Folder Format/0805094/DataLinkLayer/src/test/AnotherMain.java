package test;

import dll.DLLThread;
import dll.DataLinkLayer;
import java.io.File;
import java.io.PrintStream;
import nl.NetworkLayer;
import phy.SimPhy;

public class AnotherMain
{
  public static void main(String[] args)
    throws Exception
  {
    byte sourceMac2 = 42;
    byte destinationMac2 = 26;
    SimPhy physicalLayer2 = new SimPhy(sourceMac2, destinationMac2);
    NetworkLayer networkLayer2 = new NetworkLayer();
    networkLayer2.setSendFile(new File("/home/s/Desktop/send3"));
    networkLayer2.setReceiveFile(new File("/home/s/Desktop/receive.temp1"));
    DataLinkLayer dataLinkLayer2 = new DataLinkLayer(physicalLayer2, networkLayer2, sourceMac2, destinationMac2);
    
    DLLThread receiverInstance = new DLLThread(dataLinkLayer2);
    Thread receiverThread = new Thread(receiverInstance);
    System.out.println("Strting receiver thread");
    receiverThread.start();
    
    Thread.sleep(4000L);
    receiverInstance.startSending();
  }
}
