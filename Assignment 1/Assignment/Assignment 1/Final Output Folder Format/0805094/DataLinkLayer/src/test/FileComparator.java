package test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.TreeSet;

public class FileComparator
{
  private File firstFile;
  private File secondFile;
  
  public File getFirstFile()
  {
    return this.firstFile;
  }
  
  public void setFirstFile(File firstFile)
  {
    this.firstFile = firstFile;
    try
    {
      this.firstFileByte = getBytesFromFile(firstFile);
    }
    catch (IOException ex)
    {
      System.out.println("First file too big");
      return;
    }
  }
  
  public File getSecondFile()
  {
    return this.secondFile;
  }
  
  public void setSecondFile(File secondFile)
  {
    this.secondFile = secondFile;
    try
    {
      this.secondFileByte = getBytesFromFile(secondFile);
    }
    catch (IOException ex)
    {
      System.out.println("Second file too big");
      return;
    }
  }
  
  public static final IOException FILE_TOO_BIG = new IOException("File too big");
  
  private byte[] getBytesFromFile(File sendFile)
    throws IOException
  {
    InputStream is = new FileInputStream(sendFile);
    
    long length = sendFile.length();
    if (length > 2147483647L) {
      throw FILE_TOO_BIG;
    }
    byte[] sendFileBytes = new byte[(int)length];
    
    int offset = 0;
    int numRead = 0;
    while ((offset < sendFileBytes.length) && ((numRead = is.read(sendFileBytes, offset, sendFileBytes.length - offset)) >= 0)) {
      offset += numRead;
    }
    if (offset < sendFileBytes.length) {
      throw new IOException("Could not completely read file " + sendFile.getName());
    }
    is.close();
    return sendFileBytes;
  }
  
  byte[] secondFileByte = null;
  byte[] firstFileByte = null;
  
  public TreeSet compare()
  {
    if ((this.firstFile == null) || (this.secondFile == null)) {
      return null;
    }
    if (this.firstFileByte.length != this.secondFileByte.length)
    {
      System.out.println("File size mismatched");
      System.out.println("First File size: " + this.firstFile.length());
      System.out.println("Second File size: " + this.secondFile.length());
      return null;
    }
    int mismatchCount = 0;
    TreeSet<Integer> t = new TreeSet();
    for (int i = 0; i < this.firstFileByte.length; i++) {
      if (this.secondFileByte[i] != this.firstFileByte[i])
      {
        mismatchCount++;
        t.add(Integer.valueOf(i / 64));
        
        System.out.printf("Mismatched at Byte index: %d in First File: %02X in Second File: %02X\n", new Object[] { Integer.valueOf(i), Byte.valueOf(this.firstFileByte[i]), Byte.valueOf(this.secondFileByte[i]) });
      }
    }
    if (mismatchCount != 0)
    {
      System.out.println("Total Mismatch Byte: " + mismatchCount);
      System.out.println("Total Mismatch Frame: " + mismatchCount / 64);
      System.out.println("Mismatched Frames: " + t);
      return t;
    }
    System.out.println("No mismatch found");
    return null;
  }
  
  private boolean compareByteArray(byte[] a, byte[] b)
  {
    if (a.length != b.length) {
      return false;
    }
    for (int i = 0; i < a.length; i++) {
      if (a[i] != b[i]) {
        return false;
      }
    }
    return true;
  }
  
  public void findMatch(int packetCount)
  {
    byte[] a = new byte[64];
    System.arraycopy(this.secondFileByte, packetCount * 64, a, 0, 64);
    for (int i = 0; i < this.secondFileByte.length / 64; i++)
    {
      byte[] b = new byte[64];
      System.arraycopy(this.secondFileByte, i * 64, b, 0, 64);
      if (compareByteArray(a, b)) {
        System.out.println("Matched " + i);
      }
    }
  }
}
