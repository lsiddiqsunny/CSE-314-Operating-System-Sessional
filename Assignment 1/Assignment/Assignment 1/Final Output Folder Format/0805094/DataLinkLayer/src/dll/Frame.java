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

import sun.misc.CRC16;
import util.Printer;

public class Frame
{
  public static final byte PREAMBLE = 126;
  private byte sourceMac;
  private byte destinationMac;
  private byte controlByte;
  private byte[] payLoad;
  private byte[] checkSum;
  private byte[] bitUnStuffedFrame;
  private byte[] bitStuffedFrame;
  private CRC16 crcCalculator;
  public static final int NO_PACKET_NUMBER = Integer.MAX_VALUE;
  private int packetNumber = Integer.MAX_VALUE;
  public static final Frame CRC_MISMATCHED_FRAME = new Frame((byte)0, (byte)0, (byte)0, null);
  private FrameType frameType;
  
  public static enum FrameType
  {
    DATA_FRAME,  ACK_FRAME,  NAK_FRAME,  PIGGYBACK_FRAME;
    
    private FrameType() {}
  }
  
  public Frame(byte sourceMac, byte destinationMac, byte controlByte, byte[] payLoad)
  {
    this.sourceMac = sourceMac;
    this.destinationMac = destinationMac;
    this.controlByte = controlByte;
    this.payLoad = (payLoad == null ? new byte[0] : payLoad);
    this.crcCalculator = new CRC16();
    produceFrame();
    bitStuff();
    calculateType();
  }
  
  public Frame(byte[] bitStuffedFrame)
    throws Exception
  {
    this.bitStuffedFrame = bitStuffedFrame;
    this.crcCalculator = new CRC16();
    bitUnStuff();
    calculateType();
  }
  
  @Deprecated
  public void makePiggyBackFrame(int ackSequence)
  {
    if (!isDataFrame()) {
      return;
    }
    this.controlByte = ((byte)(this.controlByte & 0xFF | ackSequence << 2 | 0x3));
    
    produceFrame();
    bitStuff();
    calculateType();
  }
  
  @Deprecated
  public void makeDataFrame()
  {
    if (!isPiggyBackFrame()) {
      return;
    }
    this.controlByte = ((byte)(this.controlByte & 0xE0));
    produceFrame();
    bitStuff();
    calculateType();
  }
  
  private void produceFrame()
  {
    this.bitUnStuffedFrame = new byte[this.payLoad.length + 7];
    this.crcCalculator.reset();
    this.crcCalculator.update(this.sourceMac);
    this.crcCalculator.update(this.destinationMac);
    this.crcCalculator.update(this.controlByte);
    for (int i = 0; i < this.payLoad.length; i++) {
      this.crcCalculator.update(this.payLoad[i]);
    }
    this.crcCalculator.update((byte)0);
    this.crcCalculator.update((byte)0);
    
    int crc = this.crcCalculator.value;
    
    byte allOneBitMask = -1;
    byte crcByteTwo = (byte)(crc & allOneBitMask);
    
    crc >>>= 8;
    byte crcByteOne = (byte)(crc & allOneBitMask);
    this.bitUnStuffedFrame[0] = 126;
    this.bitUnStuffedFrame[1] = this.sourceMac;
    this.bitUnStuffedFrame[2] = this.destinationMac;
    this.bitUnStuffedFrame[3] = this.controlByte;
    for (int i = 0; i < this.payLoad.length; i++) {
      this.bitUnStuffedFrame[(4 + i)] = this.payLoad[i];
    }
    int tempCounter = this.payLoad.length + 4;
    this.bitUnStuffedFrame[(tempCounter++)] = crcByteOne;
    this.bitUnStuffedFrame[(tempCounter++)] = crcByteTwo;
    this.bitUnStuffedFrame[tempCounter] = 126;
    
    this.checkSum = new byte[2];
    this.checkSum[0] = crcByteOne;
    this.checkSum[1] = crcByteTwo;
  }
  
  private void bitStuff()
  {
    StringBuffer resultStringBuffer = new StringBuffer();
    StringBuffer tempStringBuffer = new StringBuffer();
    for (int i = 1; i < this.bitUnStuffedFrame.length - 1; i++)
    {
      tempStringBuffer.delete(0, tempStringBuffer.length());
      
      String tempString = Integer.toBinaryString(this.bitUnStuffedFrame[i] & 0xFF);
      for (int j = 0; j < 8 - tempString.length(); j++) {
        tempStringBuffer.append('0');
      }
      tempStringBuffer.append(tempString);
      resultStringBuffer.append(tempStringBuffer);
    }
    int tempCounter = 0;
    int totalBitStuffing = 0;
    for (int i = 0; i < resultStringBuffer.length(); i++)
    {
      if (resultStringBuffer.charAt(i) == '1') {
        tempCounter++;
      } else {
        tempCounter = 0;
      }
      if (tempCounter == 5)
      {
        resultStringBuffer.insert(i + 1, '0');
        tempCounter = 0;
        totalBitStuffing++;
      }
    }
    int modCount = totalBitStuffing % 8;
    if (modCount != 0) {
      for (int i = 0; i < 8 - modCount; i++) {
        resultStringBuffer.append('0');
      }
    }
    this.bitStuffedFrame = new byte[resultStringBuffer.length() / 8 + 2];
    
    this.bitStuffedFrame[0] = 126;
    for (int i = 1; i < this.bitStuffedFrame.length - 1; i++) {
      this.bitStuffedFrame[i] = ((byte)Integer.parseInt(resultStringBuffer.substring((i - 1) * 8, i * 8), 2));
    }
    this.bitStuffedFrame[(this.bitStuffedFrame.length - 1)] = 126;
  }
  
  private void bitUnStuff()
    throws Exception
  {
    StringBuffer resultStringBuffer = new StringBuffer();
    StringBuffer tempStringBuffer = new StringBuffer();
    for (int i = 1; i < this.bitStuffedFrame.length - 1; i++)
    {
      tempStringBuffer.delete(0, tempStringBuffer.length());
      String tempString = Integer.toBinaryString(this.bitStuffedFrame[i]);
      if (tempString.length() > 8) {
        tempString = tempString.substring(tempString.length() - 8);
      }
      for (int j = 0; j < 8 - tempString.length(); j++) {
        tempStringBuffer.append('0');
      }
      tempStringBuffer.append(tempString);
      resultStringBuffer.append(tempStringBuffer);
    }
    int tempCounterResult = 0;
    int totalBitStuffingResult = 0;
    for (int i = 0; i < resultStringBuffer.length(); i++)
    {
      if (resultStringBuffer.charAt(i) == '1') {
        tempCounterResult++;
      } else {
        tempCounterResult = 0;
      }
      if (tempCounterResult == 5)
      {
        resultStringBuffer.deleteCharAt(i + 1);
        tempCounterResult = 0;
        totalBitStuffingResult++;
      }
    }
    int modCountResult = totalBitStuffingResult % 8;
    if (modCountResult != 0)
    {
      int tobeRemoved = 8 - modCountResult;
      resultStringBuffer.delete(resultStringBuffer.length() - tobeRemoved, resultStringBuffer.length());
    }
    this.bitUnStuffedFrame = new byte[resultStringBuffer.length() / 8 + 2];
    
    this.bitUnStuffedFrame[0] = 126;
    for (int i = 1; i < this.bitUnStuffedFrame.length - 1; i++) {
      this.bitUnStuffedFrame[i] = ((byte)Integer.parseInt(resultStringBuffer.substring((i - 1) * 8, i * 8), 2));
    }
    this.bitUnStuffedFrame[(this.bitUnStuffedFrame.length - 1)] = 126;
    
    this.sourceMac = this.bitUnStuffedFrame[1];
    this.destinationMac = this.bitUnStuffedFrame[2];
    this.controlByte = this.bitUnStuffedFrame[3];
    
    int unStuffedLength = this.bitUnStuffedFrame.length;
    this.checkSum = new byte[2];
    this.checkSum[0] = this.bitUnStuffedFrame[(unStuffedLength - 3)];
    this.checkSum[1] = this.bitUnStuffedFrame[(unStuffedLength - 2)];
    
    this.crcCalculator.reset();
    for (int i = 1; i < this.bitUnStuffedFrame.length - 1; i++) {
      this.crcCalculator.update(this.bitUnStuffedFrame[i]);
    }
    if (this.crcCalculator.value != 0) {
      throw CRC_MISMATCHED;
    }
    this.payLoad = new byte[this.bitUnStuffedFrame.length - 7];
    for (int i = 0; i < this.payLoad.length; i++) {
      this.payLoad[i] = this.bitUnStuffedFrame[(i + 4)];
    }
  }
  
  public static final Exception CRC_MISMATCHED = new Exception("CRC mismatched");
  
  public byte[] getBitStuffedFrame()
  {
    return this.bitStuffedFrame;
  }
  
  public byte[] getBitUnStuffedFrame()
  {
    return this.bitUnStuffedFrame;
  }
  
  public byte[] getPayLoad()
  {
    return this.payLoad;
  }
  
  public byte[] getCheckSum()
  {
    return this.checkSum;
  }
  
  public byte getControlByte()
  {
    return this.controlByte;
  }
  
  public byte getDestinationMac()
  {
    return this.destinationMac;
  }
  
  public byte getSourceMac()
  {
    return this.sourceMac;
  }
  
  private void calculateType()
  {
    int bitMask = 3;
    int type = bitMask & this.controlByte;
    if (type == 0) {
      this.frameType = FrameType.DATA_FRAME;
    } else if (type == 1) {
      this.frameType = FrameType.ACK_FRAME;
    } else if (type == 2) {
      this.frameType = FrameType.NAK_FRAME;
    } else if (type == 3) {
      this.frameType = FrameType.PIGGYBACK_FRAME;
    }
  }
  
  public FrameType getFrameType()
  {
    return this.frameType;
  }
  
  public boolean isDataFrame()
  {
    return this.frameType.equals(FrameType.DATA_FRAME);
  }
  
  public boolean isAckFrame()
  {
    return this.frameType.equals(FrameType.ACK_FRAME);
  }
  
  public boolean isNakFrame()
  {
    return this.frameType.equals(FrameType.NAK_FRAME);
  }
  
  public boolean isPiggyBackFrame()
  {
    return this.frameType.equals(FrameType.PIGGYBACK_FRAME);
  }
  
  public boolean isEOFFrame()
  {
    return this.payLoad.length == 0 & isDataFrame();
  }
  
  public boolean isCRCMismatchedFrame()
  {
    return (this.sourceMac == 0) && (this.destinationMac == 0) && (this.controlByte == 0) && (this.payLoad.length == 0);
  }
  
  public int getAckSequence()
  {
    int bitMask = 28;
    return (bitMask & this.controlByte) >>> 2;
  }
  
  public int getFrameSequence()
  {
    int bitMask = 224;
    return (bitMask & this.controlByte) >>> 5;
  }
  
  /**
   * @deprecated
   */
  public int getPacketNumber()
  {
    return this.packetNumber;
  }
  
  /**
   * @deprecated
   */
  public void setPacketNumber(int packetNumber)
  {
    this.packetNumber = packetNumber;
  }
  
  public String toString()
  {
    StringBuffer stringBuffer = new StringBuffer();
    stringBuffer.append("Frame BitStuffed: ");
    stringBuffer.append(Printer.byteArrayToHexString(this.bitStuffedFrame));
    stringBuffer.append("BitUnStuffed: ");
    stringBuffer.append(Printer.byteArrayToHexString(this.bitUnStuffedFrame));
    
    return stringBuffer.toString();
  }
}

