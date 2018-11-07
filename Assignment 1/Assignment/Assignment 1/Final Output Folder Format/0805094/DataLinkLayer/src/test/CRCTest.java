package test;

import java.io.PrintStream;
import java.util.Vector;
import sun.misc.CRC16;
import util.Printer;

public class CRCTest
{
  public static void main_crc_test(String[] args)
  {
    CRC16 crc = new CRC16();
    
    Vector<Byte> b = new Vector();
    b.add(Byte.valueOf((byte)12));
    b.add(Byte.valueOf((byte)12));
    b.add(Byte.valueOf((byte)12));
    b.add(Byte.valueOf((byte)0));
    b.add(Byte.valueOf((byte)0));
    for (Byte byte1 : b) {
      crc.update(byte1.byteValue());
    }
    int v = crc.value;
    System.out.printf("%X\n", new Object[] { Integer.valueOf(v) });
    
    System.out.println("CRC in binary: " + Integer.toBinaryString(v));
    byte allOneBitMask = -1;
    byte crcByteTwo = (byte)(v & allOneBitMask);
    
    Printer.printByteHex("Crc Byte two: ", crcByteTwo);
    
    v >>>= 8;
    byte crcByteOne = (byte)(v & allOneBitMask);
    Printer.printByteHex("Crc Byte one: ", crcByteOne);
    
    b.remove(b.size() - 1);
    b.remove(b.size() - 1);
    b.add(Byte.valueOf(crcByteOne));
    b.add(Byte.valueOf(crcByteTwo));
    
    crc.reset();
    for (Byte byte1 : b) {
      crc.update(byte1.byteValue());
    }
    System.out.printf("%X\n", new Object[] { Integer.valueOf(crc.value) });
  }
}
