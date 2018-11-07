/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl;

import util.Printer;

public class Packet
{
  private byte[] packetData;
  private int packetCount;
  public static final int NO_PACKET_NUMBER = Integer.MAX_VALUE;
  
  public Packet(byte[] packetData, int packetCount)
  {
    this.packetData = packetData;
    this.packetCount = packetCount;
  }
  
  public Packet(byte[] packetData)
  {
    this(packetData, Integer.MAX_VALUE);
  }
  
  public int getPacketCount()
  {
    return this.packetCount;
  }
  
  public void setPacketCount(int packetCount)
  {
    this.packetCount = packetCount;
  }
  
  public byte[] getPacketData()
  {
    return this.packetData;
  }
  
  public void setPacketData(byte[] packetData)
  {
    this.packetData = packetData;
  }
  
  public String toString()
  {
    return Printer.byteArrayToHexString(this.packetData) + " Packet Count: " + (this.packetCount == Integer.MAX_VALUE ? "INF" : Integer.valueOf(this.packetCount));
  }
}

