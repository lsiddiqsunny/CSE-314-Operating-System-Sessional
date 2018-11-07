package simswitch;

import java.io.IOException;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class SimSwitch
  implements Runnable
{
  public static final int SWITCH_LISTEN_PORT = 22222;
  public static final int SWITCH_NUMBER_OF_PORTS = 32;
  public static final int SWITCH_BUFFER_SIZE = 256;
  public static final int SWITCH_MAJOR_VERSION = 1;
  public static final int SWITCH_MINOR_VERSION = 0;
  public static final double SWITCH_DEFAULT_ERROR_RATE = 2.0E-4D;
  public static final double SWITCH_PACKET_MISSING_RATE = 0.01D;
  private byte[] macAddress = new byte[32];
  private InetAddress[] remoteIP = new InetAddress[32];
  private int[] remotePort = new int[32];
  private long packetID;
  private DatagramSocket dSocket;
  private DatagramPacket dPacket;
  private byte[] myBuffer = new byte['?'];
  private byte[] mask = { Byte.MIN_VALUE, 64, 32, 16, 8, 4, 2, 1 };
  private byte[] mask2 = { Byte.MIN_VALUE, -64, -32, -16, -8, -4, -2, -1 };
  private byte[] bdp = new byte['?'];
  int endpos;
  int lenOriginal;
  int lenDestuffed;
  
  public SimSwitch()
  {
    try
    {
      this.dSocket = new DatagramSocket(22222);
      this.dPacket = new DatagramPacket(this.myBuffer, this.myBuffer.length);
      
      this.packetID = 0L;
      //System.out.println("SimSwitch 1.0 : BUET");
      //System.out.println("Send bug reports to tanviralamin@gmail.com");
      System.out.println("Initiated at port " + this.dSocket.getLocalPort());
    }
    catch (SocketException e)
    {
      System.err.println("Can't start switch on port 22222");
      e.printStackTrace();
    }
  }
  
  private int checkValidFrame(byte[] b, byte[] cc, int len)
  {
    if (b[0] != 126) {
      return 0;
    }
    this.endpos = uptoFlag(b, len, 1);
    this.lenOriginal = (this.endpos + 1);
    if (this.lenOriginal == 0) {
      return 0;
    }
    this.lenDestuffed = destuff(b, cc, this.lenOriginal);
    
    return this.lenDestuffed;
  }
  
  private void addError(byte[] b, int len)
  {
    for (int i = 0; i < len; i++)
    {
      byte temp = b[i];
      byte mask = 1;
      for (int j = 0; j < 8; j++)
      {
        mask = (byte)(mask << j);
        double randomNumber = Math.random();
        if (randomNumber < 2.0E-4D) {
          temp = (byte)(temp ^ mask);
        }
      }
      b[i] = temp;
    }
  }
  
  public void print_packet(byte[] b, int len)
  {
    System.out.printf("[ ", new Object[0]);
    for (int i = 0; i < len; i++) {
      System.out.printf("%X ", new Object[] { Byte.valueOf(b[i]) });
    }
    System.out.printf("]\n", new Object[0]);
  }
  
  public void run()
  {
    try
    {
      byte[] bdpp;
      int i;
      for (;;)
      {
        this.dSocket.receive(this.dPacket);
        this.packetID += 1L;
        
        System.out.printf("\n" + this.packetID + " : " + this.dPacket.getAddress().getHostAddress() + " : " + this.dPacket.getPort() + " ", new Object[0]);
        int len1 = this.dPacket.getLength();
        bdpp = this.dPacket.getData();
        print_packet(bdpp, len1);
        int length = checkValidFrame(bdpp, this.bdp, len1);
        if (length == 0)
        {
          System.out.println(this.packetID + ": DRP FNM");
        }
        else if (this.bdp[1] == 0)
        {
          System.out.println(this.packetID + " : Source mac can't be zero");
        }
        else
        {
          if (this.bdp[2] == 0) {
            System.out.println(this.packetID + " : Physical layer control message");
          } else {
            System.out.printf(this.packetID + " : FRM %X : TO %X\n", new Object[] { Byte.valueOf(this.bdp[1]), Byte.valueOf(this.bdp[2]) });
          }
          for (i = 0; i < 32; i++) {
            if ((this.macAddress[i] == this.bdp[1]) || (this.macAddress[i] == 0)) {
              break;
            }
          }
          if (i == 32)
          {
            System.out.println("More connection than capacity...");
            System.out.println("Dropping frame...");
          }
          else
          {
            if (this.macAddress[i] == 0)
            {
              this.macAddress[i] = this.bdp[1];
              
              this.remoteIP[i] = this.dPacket.getAddress();
              this.remotePort[i] = this.dPacket.getPort();
              System.out.printf(this.packetID + " : LRN %X = " + this.remoteIP[i].getHostAddress() + ": %d\n", new Object[] { Byte.valueOf(this.macAddress[i]), Integer.valueOf(this.remotePort[i]) });
            }
            if (this.bdp[2] != 0)
            {
              double dpp = Math.random();
              if (dpp < 0.01D)
              {
                System.out.println(this.packetID + " : DRP");
              }
              else
              {
                addError(bdpp, this.lenOriginal);
                System.out.printf(this.packetID + " : ", new Object[0]);
                print_packet(bdpp, this.lenOriginal);
                if (this.bdp[2] == -1) {
                  for (i = 0; i < 32; i++)
                  {
                    if (this.macAddress[i] == 0) {
                      break;
                    }
                    DatagramPacket pkt = new DatagramPacket(bdpp, this.lenOriginal, this.remoteIP[i], this.remotePort[i]);
                    this.dSocket.send(pkt);
                    System.out.printf(this.packetID + " : BFWD %X\n", new Object[] { Byte.valueOf(this.macAddress[i]) });
                  }
                }
                for (i = 0; i < 32; i++) {
                  if ((this.macAddress[i] == this.bdp[2]) || (this.macAddress[i] == 0)) {
                    break;
                  }
                }
                if ((i == 32) || (this.macAddress[i] == 0)) {
                  for (i = 0; i < 32;) {
                    if (this.macAddress[i] != this.bdp[1])
                    {
                      if (this.macAddress[i] != 0)
                      {
                        DatagramPacket pkt = new DatagramPacket(bdpp, this.lenOriginal, this.remoteIP[i], this.remotePort[i]);
                        this.dSocket.send(pkt);
                        System.out.printf(this.packetID + " : FWD %X\n", new Object[] { Byte.valueOf(this.macAddress[i]) });
                      }
                    }
                    else
                    {
                      i++;
                      
                      DatagramPacket pkt = new DatagramPacket(bdpp, this.lenOriginal, this.remoteIP[i], this.remotePort[i]);
                      this.dSocket.send(pkt);
                      System.out.printf(this.packetID + " : FWD %X\n", new Object[] { Byte.valueOf(this.macAddress[i]) });
                      pkt = null;
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    catch (IOException e)
    {
      System.out.println("IOException in reception thread");
      e.printStackTrace();
    }
  }
  
  public int bitstuff(byte[] src, byte[] dest, int len)
  {
    int srcpos = 0;int destpos = 0;int numones = 0;int srcind = 0;int destind = 0;
    for (int j = 0; j < len; j++) {
      for (int i = 0; i < 8; i++) {
        if ((src[srcind] & this.mask[srcpos]) != 0)
        {
          numones++;
          srcpos++;
          if (srcpos == 8)
          {
            srcind++;
            srcpos = 0;
          }
          int tmp71_69 = destind; byte[] tmp71_68 = dest;tmp71_68[tmp71_69] = ((byte)(tmp71_68[tmp71_69] | this.mask[destpos]));
          destpos++;
          if (destpos == 8)
          {
            destind++;
            destpos = 0;
            dest[destind] = 0;
          }
          if (numones == 5)
          {
            numones = 0;
            destpos++;
            if (destpos == 8)
            {
              destind++;
              destpos = 0;
              dest[destind] = 0;
            }
          }
        }
        else
        {
          numones = 0;
          srcpos++;
          if (srcpos == 8)
          {
            srcind++;
            srcpos = 0;
          }
          destpos++;
          if (destpos == 8)
          {
            destind++;
            destpos = 0;
            dest[destind] = 0;
          }
        }
      }
    }
    if (destpos == 0) {
      return destind;
    }
    return destind + 1;
  }
  
  public int destuff(byte[] src, byte[] dest, int len)
  {
    int srcpos = 0;int destpos = 0;int numones = 0;int srcind = 0;int destind = 0;
    dest[destind] = 0;
    for (int j = 0; j < len; j++) {
      for (int i = 0; i < 8; i++) {
        if ((src[srcind] & this.mask[srcpos]) != 0)
        {
          numones++;
          srcpos++;
          if (srcpos == 8)
          {
            srcind++;
            srcpos = 0;
          }
          int tmp76_74 = destind; byte[] tmp76_73 = dest;tmp76_73[tmp76_74] = ((byte)(tmp76_73[tmp76_74] | this.mask[destpos]));
          destpos++;
          if (destpos == 8)
          {
            destind++;
            dest[destind] = 0;
            destpos = 0;
          }
        }
        else
        {
          if (numones != 5)
          {
            destpos++;
            if (destpos == 8)
            {
              destind++;
              destpos = 0;
              dest[destind] = 0;
            }
          }
          srcpos++;
          if (srcpos == 8)
          {
            srcind++;
            srcpos = 0;
          }
          numones = 0;
        }
      }
    }
    if (destpos == 0) {
      return destind;
    }
    return destind + 1;
  }
  
  public int uptoFlag(byte[] src, int upto, int startFrom)
  {
    int srcpos = 0;int numones = 0;int srcind = startFrom;int state = 0;
    while (srcind < upto) {
      for (int i = 0; i < 8; i++)
      {
        boolean gotone = (src[srcind] & this.mask[srcpos]) != 0;
        switch (state)
        {
        case 0: 
          if (!gotone) {
            state = 1;
          }
          break;
        case 1: 
          if (gotone) {
            state = 2;
          }
          break;
        case 2: 
          if (gotone) {
            state = 3;
          } else {
            state = 1;
          }
          break;
        case 3: 
          if (gotone) {
            state = 4;
          } else {
            state = 1;
          }
          break;
        case 4: 
          if (gotone) {
            state = 5;
          } else {
            state = 1;
          }
          break;
        case 5: 
          if (gotone) {
            state = 6;
          } else {
            state = 1;
          }
          break;
        case 6: 
          if (gotone) {
            state = 7;
          } else {
            state = 1;
          }
          break;
        case 7: 
          if (!gotone) {
            state = 8;
          } else {
            state = 1;
          }
          break;
        }
        if (state == 8)
        {
          int tmp234_232 = srcind; byte[] tmp234_231 = src;tmp234_231[tmp234_232] = ((byte)(tmp234_231[tmp234_232] & this.mask2[srcpos]));
          return srcind;
        }
        srcpos++;
        if (srcpos == 8)
        {
          srcind++;
          srcpos = 0;
        }
      }
    }
    return -1;
  }
  
  public static void main(String[] args)
  {
    SimSwitch simSwitch = new SimSwitch();
    new Thread(simSwitch).start();
  }
}

