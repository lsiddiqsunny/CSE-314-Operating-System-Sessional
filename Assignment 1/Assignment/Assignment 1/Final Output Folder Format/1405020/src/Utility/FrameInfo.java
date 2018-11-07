package Utility;

/**
 * Created by Asus on 10/20/2017.
 */
public class FrameInfo {
  public int type;
  public int seq;
  public int ack;
  public byte[] payload;
  public int checksum;
  public boolean hasCerror;
  public boolean hasFrameError;

  public FrameInfo(){
    hasFrameError=false;
  }

  @Override
  public String toString() {
    return "Type: "+type+"\nSeqNo.: "+seq+"\nAckNo.: "+ack+"\nchecksum "+checksum+"\nHas CheckSum Error: "+hasCerror;
  }
}

