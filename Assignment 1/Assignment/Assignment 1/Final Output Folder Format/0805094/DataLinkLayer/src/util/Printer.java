package util;

import dll.Frame;
import dll.FrameBufferElement;
import java.io.PrintStream;

public class Printer
{
  public static void printByteArray(byte[] array)
  {
    System.out.print("[ ");
    for (int i = 0; i < array.length; i++) {
      System.out.print(array[i] + " ");
    }
    System.out.println("]");
  }
  
  public static void printByteArrayHex(byte[] array)
  {
    System.out.print("[ ");
    for (int i = 0; i < array.length; i++) {
      System.out.printf("%02X ", new Object[] { Byte.valueOf(array[i]) });
    }
    System.out.println("]");
  }
  
  public static void printByteHex(byte value)
  {
    printByteHex("", value, "");
  }
  
  public static void printByteHex(String pre, byte value)
  {
    printByteHex(pre, value, "");
  }
  
  public static void printByteHex(String pre, byte value, String post)
  {
    System.out.printf(pre + " %X " + post + "\n", new Object[] { Byte.valueOf(value) });
  }
  
  public static String byteArrayToHexString(byte[] array)
  {
    StringBuffer sb = new StringBuffer("[ ");
    for (int i = 0; i < array.length; i++) {
      sb.append(String.format("%02X ", new Object[] { Byte.valueOf(array[i]) }));
    }
    sb.append("]");
    return sb.toString();
  }
  
  public static synchronized String getBasicFrameInfo(Frame frame)
  {
    StringBuffer sb = new StringBuffer();
    if (frame.isCRCMismatchedFrame())
    {
      sb.append("CRC_MISMATCHED_FRAME");
    }
    else
    {
      sb.append("[Frame: ");
      if (frame.isDataFrame())
      {
        sb.append("DATA_FRAME ");
        sb.append(frame.getFrameSequence());
      }
      else if (frame.isAckFrame())
      {
        sb.append("ACK_FRAME ");
        sb.append(frame.getAckSequence());
      }
      else if (frame.isNakFrame())
      {
        sb.append("NAK_FRAME ");
        sb.append(frame.getAckSequence());
      }
      else if (frame.isPiggyBackFrame())
      {
        sb.append("PIGGYBACK_FRAME ");
        sb.append(", FrameSeq: ");
        sb.append(frame.getFrameSequence());
        sb.append(", AckSeq: ");
        sb.append(frame.getAckSequence());
      }
      if (frame.getPacketNumber() != Integer.MAX_VALUE)
      {
        sb.append(", PacketNumber: ");
        sb.append(frame.getPacketNumber());
      }
      sb.append("]");
      if (frame.isEOFFrame()) {
        sb.append(" EOF_FRAME");
      }
    }
    return sb.toString();
  }
  
  public static synchronized String getFrameBufferElementStatus(FrameBufferElement fbe)
  {
    StringBuffer sb = new StringBuffer();
    if (fbe == null)
    {
      sb.append("NULL");
    }
    else if (fbe.isCRCMismatchedFrame())
    {
      sb.append("CRC_MISMATCHED_FRAME");
    }
    else
    {
      sb.append("[FrameBufferElement: ");
      if (fbe.isDataFrame())
      {
        sb.append("DATA_FRAME ");
        sb.append(fbe.getFrameSequence());
      }
      else if (fbe.isAckFrame())
      {
        sb.append("ACK_FRAME ");
        sb.append(fbe.getAckSequence());
      }
      else if (fbe.isNakFrame())
      {
        sb.append("NAK_FRAME ");
        sb.append(fbe.getAckSequence());
      }
      else if (fbe.isPiggyBackFrame())
      {
        sb.append("PIGGYBACK_FRAME ");
        sb.append(fbe.getAckSequence());
      }
      if (fbe.getPacketNumber() != Integer.MAX_VALUE)
      {
        sb.append(", PacketNumber: ");
        sb.append(fbe.getPacketNumber());
      }
      sb.append(", Status: ");
      sb.append(fbe.getStats());
      sb.append("]");
      if (fbe.isEOFFrame()) {
        sb.append(" EOF_FRAME");
      }
    }
    return sb.toString();
  }
}
