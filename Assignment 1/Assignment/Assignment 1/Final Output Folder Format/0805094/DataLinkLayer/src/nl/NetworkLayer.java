/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl;

/**
 *
 * @author Tanzim Ahmed
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Vector;

public class NetworkLayer
{
  private File sendFile;
  private byte[] sendFileBytes;
  private Vector<Byte> receivedFileBytes;
  private File receiveFile;
  private int sendPacketCount;
  public static final int PACKET_SIZE = 64;
  public static final Exception NO_MORE_PACKET = new Exception("No more packet");
  
  public NetworkLayer()
  {
    this.sendPacketCount = 0;
    
    this.receivedFileBytes = new Vector();
  }
  
  public File getSendFile()
  {
    return this.sendFile;
  }
  
  public void setSendFile(File sendFile)
    throws IOException
  {
    this.sendFile = sendFile;
    getBytesFromFile();
  }
  
  public File getReceiveFile()
  {
    return this.receiveFile;
  }
  
  public void setReceiveFile(File receiveFile)
  {
    this.receiveFile = receiveFile;
  }
  
  public byte[] getNextPacket()
    throws Exception
  {
    int startPosition = this.sendPacketCount * 64;
    int endPosition = (this.sendPacketCount + 1) * 64 - 1;
    int returnArrayLength = 64;
    if (startPosition > this.sendFileBytes.length)
    {
      System.out.println("Network Layer throwing NO_MORE_PACKET exception");
      throw NO_MORE_PACKET;
    }
    System.out.println("Network Layer sending packet [Packet No: " + this.sendPacketCount + ", Start Position: " + startPosition + ", End Position: " + endPosition + "]");
    if (endPosition > this.sendFileBytes.length)
    {
      endPosition = this.sendFileBytes.length - 1;
      returnArrayLength = endPosition - startPosition + 1;
    }
    this.sendPacketCount += 1;
    
    byte[] returnArray = new byte[returnArrayLength];
    
    System.arraycopy(this.sendFileBytes, startPosition, returnArray, 0, returnArrayLength);
    
    return returnArray;
  }
  
  public static final IOException FILE_TOO_BIG = new IOException("File too big");
  
  private void getBytesFromFile()
    throws IOException
  {
    InputStream is = new FileInputStream(this.sendFile);
    
    long length = this.sendFile.length();
    if (length > 2147483647L) {
      throw FILE_TOO_BIG;
    }
    this.sendFileBytes = new byte[(int)length];
    
    int offset = 0;
    int numRead = 0;
    while ((offset < this.sendFileBytes.length) && ((numRead = is.read(this.sendFileBytes, offset, this.sendFileBytes.length - offset)) >= 0)) {
      offset += numRead;
    }
    if (offset < this.sendFileBytes.length) {
      throw new IOException("Could not completely read file " + this.sendFile.getName());
    }
    is.close();
  }
  
  public int getLastSendPacketNumber()
  {
    return this.sendPacketCount;
  }
  
  public void appendNextPacket(byte[] bytes)
  {
    for (int i = 0; i < bytes.length; i++) {
      this.receivedFileBytes.add(Byte.valueOf(bytes[i]));
    }
  }
  
  public void saveReceivedFile()
    throws FileNotFoundException, IOException
  {
    if (this.receiveFile == null) {
      return;
    }
    this.receiveFile.delete();
    
    FileOutputStream fos = new FileOutputStream(this.receiveFile, true);
    Object[] outArrayByte = this.receivedFileBytes.toArray();
    byte[] outArray = new byte[outArrayByte.length];
    for (int i = 0; i < outArrayByte.length; i++) {
      outArray[i] = ((Byte)outArrayByte[i]).byteValue();
    }
    fos.write(outArray);
    fos.close();
  }
  
  @Deprecated
  public void setReceivedFileByet(Vector<Byte> b)
  {
    this.receivedFileBytes = b;
  }
}

