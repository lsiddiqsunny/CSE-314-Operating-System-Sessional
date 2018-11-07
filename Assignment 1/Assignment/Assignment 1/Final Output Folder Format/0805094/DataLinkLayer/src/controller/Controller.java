/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Tanzim Ahmed
 */
package controller;

import dll.DLLLogger;
import dll.DLLLogger;
import dll.DataLinkLayer;
import dll.DataLinkLayer;
import gui.UserInterface;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.SocketException;
import java.net.UnknownHostException;
import nl.NetworkLayer;
import phy.SimPhy;

public class Controller
  implements DLLLogger
{
  private SimPhy physicalLayer;
  private DataLinkLayer dataLinkLayer;
  private NetworkLayer networkLayer;
  private UserInterface userInterface;
  private byte sourceMac;
  private byte destinationMac;
  
  public Controller(byte sourceMac, byte destinationMac, UserInterface userInterface)
    throws UnknownHostException, SocketException, IOException
  {
    this.sourceMac = sourceMac;
    this.destinationMac = destinationMac;
    this.userInterface = userInterface;
    
    this.physicalLayer = new SimPhy(sourceMac, destinationMac);
    this.networkLayer = new NetworkLayer();
    this.dataLinkLayer = new DataLinkLayer(this.physicalLayer, this.networkLayer, sourceMac, destinationMac, this);
  }
  
  public void setSendFile(File file)
    throws IOException
  {
    this.networkLayer.setSendFile(file);
    System.out.println("setting receive file...");
  }
  
  public void sendFrames()
  {
    this.dataLinkLayer.sendFrames();
  }
  
  public void addSendLog(String logMessage)
  {
    this.userInterface.addSendLog(logMessage);
  }
  
  public void addReceivedLog(String logMessage)
  {
    this.userInterface.addReceivedLog(logMessage);
  }
  
  public void setReceiveFile(File receiveFile)
  {
    this.networkLayer.setReceiveFile(receiveFile);
  }
  
  public void saveReceivedFile()
    throws FileNotFoundException, IOException
  {
    this.networkLayer.saveReceivedFile();
  }
  
  public void sendEOFSuccessful()
  {
    this.userInterface.sendEOFSuccessful();
  }
  
  public void receivedEOFSuccessful()
  {
    this.userInterface.receivedEOFSuccessful();
  }
  
  public void appendPacket(byte[] packet)
  {
    this.userInterface.appendPacket(packet);
  }
}

