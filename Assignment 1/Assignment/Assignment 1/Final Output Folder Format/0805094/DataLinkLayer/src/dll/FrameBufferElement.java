package dll;

public class FrameBufferElement
  extends Frame
{
  private Status stats;
  
  public static enum Status
  {
    NOT_SEND,  SEND;
    
    private Status() {}
  }
  
  public FrameBufferElement(byte[] bitStaffedFrame, Status status)
    throws Exception
  {
    super(bitStaffedFrame);
    this.stats = status;
  }
  
  public FrameBufferElement(byte sourceMac, byte destinationMac, byte controlByte, byte[] payLoad, Status status)
  {
    super(sourceMac, destinationMac, controlByte, payLoad);
    this.stats = status;
  }
  
  public Status getStats()
  {
    return this.stats;
  }
  
  public void setStats(Status stats)
  {
    this.stats = stats;
  }
  
  public String toString()
  {
    StringBuffer sb = new StringBuffer();
    sb.append(super.toString());
    sb.append("Status: ");
    sb.append(this.stats);
    sb.append('\n');
    return sb.toString();
  }
}
