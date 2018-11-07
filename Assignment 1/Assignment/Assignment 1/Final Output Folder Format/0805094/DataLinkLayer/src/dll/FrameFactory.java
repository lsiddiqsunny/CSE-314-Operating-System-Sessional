package dll;

public class FrameFactory
{
  public static synchronized Frame getFrameFromPacket(byte sourceMac, byte destinationMac, byte controlByte, byte[] payLoad)
  {
    return new Frame(sourceMac, destinationMac, controlByte, payLoad);
  }
  
  public static synchronized Frame getFrameFromBitStaffedArray(byte[] bitStaffedFrame)
  {
    Frame f = Frame.CRC_MISMATCHED_FRAME;
    try
    {
      f = new Frame(bitStaffedFrame);
    }
    catch (Exception ex)
    {
      if (ex.equals(Frame.CRC_MISMATCHED)) {
        return Frame.CRC_MISMATCHED_FRAME;
      }
    }
    return f;
  }
  
  public static synchronized Frame getFrameFromBitStaffedArrayWithSize(byte[] bitStaffedFrame, int size)
  {
    byte[] tempArray = new byte[size];
    System.arraycopy(bitStaffedFrame, 0, tempArray, 0, size);
    
    Frame f = Frame.CRC_MISMATCHED_FRAME;
    try
    {
      f = new Frame(tempArray);
    }
    catch (Exception ex)
    {
      if (ex.equals(Frame.CRC_MISMATCHED)) {
        return Frame.CRC_MISMATCHED_FRAME;
      }
    }
    return f;
  }
  
  public static synchronized Frame getDataFrame(byte sourceMac, byte destinationMac, int frameSequence, byte[] payLoad)
  {
    if ((frameSequence < 0) || (frameSequence > 7)) {
      return null;
    }
    byte controlByte = (byte)(frameSequence << 5);
    return new Frame(sourceMac, destinationMac, controlByte, payLoad);
  }
  
  public static synchronized Frame getAckFrame(byte sourceMac, byte destinationMac, int ackSequence)
  {
    if ((ackSequence < 0) || (ackSequence > 7)) {
      return null;
    }
    byte controlByte = (byte)(ackSequence << 2 | 0x1);
    
    return new Frame(sourceMac, destinationMac, controlByte, null);
  }
  
  public static synchronized Frame getNakFrame(byte sourceMac, byte destinationMac, int ackSequence)
  {
    if ((ackSequence < 0) || (ackSequence > 7)) {
      return null;
    }
    byte controlByte = (byte)(ackSequence << 2 | 0x2);
    
    return new Frame(sourceMac, destinationMac, controlByte, null);
  }
  
  public static FrameBufferElement getFrameBufferElementByFrameSequence(byte sourceMac, byte destinationMac, int frameSequence, byte[] payLoad, FrameBufferElement.Status status)
  {
    if ((frameSequence < 0) || (frameSequence > 7)) {
      return null;
    }
    byte controlByte = (byte)(frameSequence << 5);
    return new FrameBufferElement(sourceMac, destinationMac, controlByte, payLoad, status);
  }
  
  public static FrameBufferElement getEOFFrameBufferElement(byte sourceMac, byte destinationMac, int frameSequence, FrameBufferElement.Status status)
  {
    return getFrameBufferElementByFrameSequence(sourceMac, destinationMac, frameSequence, null, status);
  }
  
  public static Frame getPiggyBackFrame(Frame dataFrame, Frame ackFrame)
  {
    int frameSequence = dataFrame.getFrameSequence();
    int ackSequence = ackFrame.getAckSequence();
    byte controlByte = (byte)(frameSequence << 5 & 0xFF | ackSequence << 2 | 0x3);
    return new Frame(dataFrame.getSourceMac(), dataFrame.getDestinationMac(), controlByte, dataFrame.getPayLoad());
  }
  
  public static Frame[] getDataAndAckFrame(Frame piggyBackFrame)
  {
    Frame[] result = new Frame[2];
    result[0] = getDataFrame(piggyBackFrame.getSourceMac(), piggyBackFrame.getDestinationMac(), piggyBackFrame.getFrameSequence(), piggyBackFrame.getPayLoad());
    result[1] = getAckFrame(piggyBackFrame.getSourceMac(), piggyBackFrame.getDestinationMac(), piggyBackFrame.getAckSequence());
    return result;
  }
}
