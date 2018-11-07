/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package phy;

/**
 *
 * @author Tanzim Ahmed
 */

import java.io.IOException;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class SimPhy
{
  public static final int PL_BUFFER_SIZE = 256;
  private InetAddress switchIP;
  private int switchPort;
  private int localPort;
  private byte localMac;
  private byte partnerMac;
  private DatagramSocket dSocket;
  private DatagramPacket dPacket;
  private byte[] myRecvBuffer = new byte['?'];
  private byte[] myBuffer = new byte['?'];
  private int receivedLen;
  
  private class PhyReceiver
    implements Runnable
  {
    private PhyReceiver() {}

        private PhyReceiver(Object object) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    
    public void run()
    {
      try
      {
        for (;;)
        {
          SimPhy.this.dSocket.receive(SimPhy.this.dPacket);
          synchronized (SimPhy.this.myRecvBuffer)
          {
            if (SimPhy.this.receivedLen != 0)
            {
              System.out.println("Buffer overflow. Discarding Frame");
              continue;
            }
            SimPhy.this.receivedLen = SimPhy.this.dPacket.getLength();
            System.arraycopy(SimPhy.this.dPacket.getData(), 0, SimPhy.this.myRecvBuffer, 0, SimPhy.this.receivedLen);
            SimPhy.this.myRecvBuffer.notifyAll();
          }
        }
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
    }
  }
  
  public SimPhy(byte _myMac, byte _partnerMac)
    throws UnknownHostException, SocketException, IOException
  {
    this.switchIP = InetAddress.getLocalHost();
    
    this.switchPort = 22222;
    this.localMac = _myMac;
    this.partnerMac = _partnerMac;
    this.localPort = (_myMac << 8);
    synchronized (this.myRecvBuffer)
    {
      this.receivedLen = 0;
    }
    this.dSocket = new DatagramSocket(this.localPort);
    this.dPacket = new DatagramPacket(this.myBuffer, this.myBuffer.length);
    
    byte[] buff = new byte[4];
    
    buff[0] = 126;
    buff[1] = this.localMac;
    buff[2] = 0;
    buff[3] = 126;
    
    DatagramPacket pkt = new DatagramPacket(buff, 4, this.switchIP, this.switchPort);
    this.dSocket.send(pkt);
    
    new Thread(new PhyReceiver(null)).start();
  }
  
  public void send(byte[] data, int len)
    throws IOException
  {
    DatagramPacket pkt = new DatagramPacket(data, len, this.switchIP, this.switchPort);
    this.dSocket.send(pkt);
  }
  
  public int recv(byte[] data)
  {
    int len = 0;
    try
    {
      synchronized (this.myRecvBuffer)
      {
        while (this.receivedLen == 0) {
          this.myRecvBuffer.wait();
        }
        System.arraycopy(this.myRecvBuffer, 0, data, 0, this.receivedLen);
        len = this.receivedLen;
        this.receivedLen = 0;
      }
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
    }
    return len;
  }
}

