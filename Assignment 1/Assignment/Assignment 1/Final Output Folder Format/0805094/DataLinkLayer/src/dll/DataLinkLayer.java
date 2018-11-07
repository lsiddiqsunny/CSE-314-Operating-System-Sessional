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
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.NetworkLayer;
import phy.SimPhy;

public class DataLinkLayer
{
  SimPhy physicalLayer;
  NetworkLayer networkLayer;
  Controller controller;
  byte sourceMac;
  byte destinationMac;
  DLLThread senderInstance;
  
  public DataLinkLayer(SimPhy physicalLayer, NetworkLayer networkLayer, byte sourceMac, byte destinationMac)
  {
    this(physicalLayer, networkLayer, sourceMac, destinationMac, null);
  }
  
  public DataLinkLayer(SimPhy physicalLayer, NetworkLayer networkLayer, byte sourceMac, byte destinationMac, Controller controller)
  {
    this.physicalLayer = physicalLayer;
    this.networkLayer = networkLayer;
    this.sourceMac = sourceMac;
    this.destinationMac = destinationMac;
    this.controller = controller;
    
    this.senderInstance = new DLLThread(this);
    
    Thread senderThread = new Thread(this.senderInstance);
    senderThread.start();
  }
  
  public void sendFrames()
  {
    try
    {
      Thread.sleep(1000L);
    }
    catch (InterruptedException ex)
    {
      Logger.getLogger(DataLinkLayer.class.getName()).log(Level.SEVERE, null, ex);
    }
    this.senderInstance.startSending();
  }
}
