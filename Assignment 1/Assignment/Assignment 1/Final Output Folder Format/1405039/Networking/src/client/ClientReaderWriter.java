package client;

import utility.ConnectionSetup;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import utility.Frame;

public class ClientReaderWriter implements Runnable{
    public ConnectionSetup connection;
    
    public ClientReaderWriter(ConnectionSetup connection) {
        this.connection = connection;
    }


    public void run() {
        while(true)
        {
            System.out.println("Press S if you want to send a file, press R if you wish to be available for receiving a file, press L to logout.");
            Scanner scan = new Scanner(System.in);
            String decision = scan.nextLine();
            connection.write(decision);
            if(decision.equalsIgnoreCase("S"))
            {
                String userfile = scan.nextLine();
                String temp[] = userfile.split(" ", 2);
                String receiverID = temp[0];
                String filepath = temp[1];
                File file = new File(filepath);
                String filename = file.getName();
                int filesize = (int)file.length();
                connection.write((Object)receiverID);
                String a = (String)connection.read();
                if(a.equalsIgnoreCase("Send filename and filesize."))
                {
                    connection.write(filename);
                    connection.write(Integer.toString(filesize));
                    String chunk_ID = (String)connection.read();
                    String tmp[] = chunk_ID.split(" ", 2);
                    int chunksize = Integer.valueOf(tmp[0]);
                    int fileID = Integer.valueOf(tmp[1]);
                    try {
                        FileInputStream fis = new FileInputStream(file);
                        int numberOfChunks = 0, numberOfBytesRead = 0, remainingBytes = filesize, index=0;
                        byte[] byteArray = new byte[filesize];
                        int offset = 0;
                        numberOfBytesRead = fis.read(byteArray);
                        Frame ackframe = null;
                        byte[] ackdata = new byte[1];
                        int seqNo = 0;
                        while(remainingBytes != 0)
                        {
                            int timeout = 0;
                            int l = chunksize;
                            if(remainingBytes < chunksize)
                            {
                                l = remainingBytes;
                            }
                            byte[] byteChunkPart = new byte[l];
                            for(int i = 0; i < l; i++)
                            {
                                byteChunkPart[i] = byteArray[offset];
                                offset++;
                            }
                            int checksum = calcCheckSum(byteChunkPart);
                            byte[] stuffedFrame = bitstuff(byteChunkPart);
                            System.out.println("Would you like to introduce an error into the frame?");
                            String errordecision = scan.nextLine();
                            if(errordecision.equalsIgnoreCase("Yes"))
                            {
                                stuffedFrame[2] = '~';
                            }
                            System.out.println("Would you like to drop a frame?");
                            String dropdecision = scan.nextLine();
                            
                            if(!(dropdecision.equalsIgnoreCase("Yes")))
                            {
                                Frame frame = new Frame('d', seqNo, stuffedFrame, checksum);
                                connection.write(frame);
                            }
                            //Frame frame = new Frame('d', seqNo, stuffedFrame, checksum);
                            //connection.write(frame);
                            ackframe = (Frame)connection.read();
                            if(ackframe == null)
                            {
                                System.out.println("Timeout.");
                                timeout = 1;
                            }
                            if(timeout == 1)
                            {
                                offset -= l;
                            }
                            else
                            {
                                int ackNo = ackframe.ackNo;
                                byte[] ackpayload = new byte[1];
                                ackdata = ackframe.payload;
                                byte[] destuffedackframe = destuff(ackdata);
                                int sentChecksum = ackframe.checksum;
                                boolean verify = verifyCheckSum(destuffedackframe, sentChecksum);
                                if(verify == true && ackdata[0] == '+')
                                {
                                    //System.out.println("In verify true region.");
                                    seqNo = 1 - seqNo ;
                                    remainingBytes = remainingBytes - l;
                                    numberOfChunks++;
                                }
                                else
                                {
                                    //System.out.println("In verify false region.");
                                    offset -= l;
                                }
                            }
                            
                            System.out.println("Sequence number in sender : " + seqNo);
                            System.out.println("chunk number in sender: " + numberOfChunks);
                        }
                        connection.write((Object)"Completion of file transfer.");
                        fis.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                else
                {
                    System.out.println("Sending failed since receiver is not logged in.");
                }
            }
            else if(decision.equalsIgnoreCase("R"))
            {
                String question = (String)connection.read();
                while(question == null)
                {
                    question = (String)connection.read();
                }
                String chunksize_fileID = (String)connection.read();
                connection.write(chunksize_fileID);
                String csfi[] = chunksize_fileID.split("_", 3);
                int chunksize = Integer.valueOf(csfi[0]);
                int fileID = Integer.valueOf(csfi[1]);
                int filesize = Integer.valueOf(csfi[2]);
                System.out.println(question);
                String recvdecision = scan.nextLine();
                connection.write((Object)recvdecision);
                Frame recvdframe;
                if(recvdecision.equalsIgnoreCase("Yes"))
                {
                    byte[] bytesInFile = new byte[filesize];
                    int maxChunks = (filesize/chunksize);
                    if(filesize%chunksize != 0)
                    {
                        maxChunks++;
                    }
                    Frame[] framearray = new Frame[maxChunks];
                    int recvdBytes = 0, numberOfChunks=0;
                    int expctSeqNo=0;
                    while(numberOfChunks != maxChunks)
                    {
                        recvdframe = (Frame)connection.read();
                        int ackNo = recvdframe.seqNo;
                        numberOfChunks++;
                        int datalength = (recvdframe.getPayload()).length;
                        byte[] framedata = new byte[datalength];
                        framedata = recvdframe.getPayload();
                        byte[] destuffedframe = destuff(framedata);
                        int pllength = destuffedframe.length;
                        int sentChecksum = recvdframe.getCheckSum();
                        boolean verify = verifyCheckSum(destuffedframe, sentChecksum);
                        byte[] ackpayload = new byte[1];
                        if(verify == true && expctSeqNo==recvdframe.seqNo)
                        {
                            System.out.println("Correct frame received.");
                            for(int i = 0; i < pllength; i++)
                            {
                                bytesInFile[recvdBytes] = destuffedframe[i] ;
                                recvdBytes++;
                                if(recvdBytes == filesize)
                                {
                                    break;
                                }
                            }
                            ackpayload = new byte[1];
                            ackpayload[0] = '+';
                            expctSeqNo = 1 - expctSeqNo;
                        }
                        else if(verify == false && expctSeqNo==recvdframe.seqNo)
                        {
                            System.out.println("Incorrect frame received.");
                            ackpayload = new byte[1];
                            ackpayload[0] = '-'; 
                        }
                        int ackchecksum = calcCheckSum(ackpayload);
                        byte[] stuffedAckFrame = bitstuff(ackpayload);
                        Frame ackframe = new Frame('a', ackNo, stuffedAckFrame, ackchecksum);
                        connection.write((Frame)ackframe);
                    }
                    String newFileName = "newfile" + "-" + connection.username ;
                    File oFile = new File(newFileName);
                    try {
                        FileOutputStream fos = new FileOutputStream(oFile);
                        fos.write(bytesInFile);
		        fos.flush();
                        fos.close();
                        fos = null;
                    } catch (Exception ex) {
                        System.out.println("Error in copying files to file at receiver end.");
                    }
                    System.out.println("File transfer complete at receiverID.");
                    connection.write((Object)"Complete");
                }
                else if(recvdecision.equalsIgnoreCase("No"))
                {
                    String cancellation = (String)connection.read();
                    System.out.println(cancellation);
                }
            }
            else if(decision.equalsIgnoreCase("L"))
            {
                connection.write((Object)"Logout.");
                break;
            }
            System.out.println("Transaction completed.");
        }
    }
    
    public int calcCheckSum(byte[] byteChunkPart)
    {
        int l = byteChunkPart.length;
        int sum = 0;
        for(int i = 0; i < l ; i++)
        {
            sum += byteChunkPart[i];
            if(sum > 127 || sum < -128)
            {
                int mask = 0x7f;
                sum = sum & mask;
                sum = sum + 1;
            }
        }
        int checksum = ~sum;
        return checksum;
    }

    public byte[] bitstuff(byte[] data) {
        BitSet bs1 = BitSet.valueOf(data);
        int length = bs1.size();
        
        for(int i = 0; i < bs1.size(); i++)
        {
            if(bs1.get(i) == true)
            {
                System.out.print("1");
            }
            else if(bs1.get(i) == false)
            {
                System.out.print("0");
            }
        }
        System.out.println();
        
        
        int cnt=0;
        int extrabits = 0;
        for(int i = 0; i < bs1.size(); i++)
        {
            if(bs1.get(i) == true)
            {
                cnt++;
            }
            else if(bs1.get(i) == false)
            {
                cnt=0;
            }
            
            if(cnt == 5)
            {
                for(int j = bs1.size(); j > i+1; j--)
                {
                    bs1.set(j, bs1.get(j-1));
                }
                bs1.set(i+1, false);
                extrabits++;
            }
        }
        
        int noOfBits = length + extrabits;
        
        for(int i = 0; i < noOfBits; i++)
        {
            if(bs1.get(i) == true)
            {
                System.out.print("1");
            }
            else if(bs1.get(i) == false)
            {
                System.out.print("0");
            }
        }
        System.out.println();
        
        
        byte[] bytearray = new byte[noOfBits];
        bytearray = bs1.toByteArray();
        /*
        for(int i = 0; i < bytearray.length; i++)
        {
            System.out.println(bytearray[i]);
        }
        */
        return bytearray;
    }

    public boolean verifyCheckSum(byte[] framedata, int sentCheckSum) 
    {
        int l = framedata.length;
        int sum = 0;
        for(int i = 0; i < l ; i++)
        {
            sum += framedata[i];
            if(sum > 127 || sum < -128)
            {
                int mask = 0x7f;
                sum = sum & mask;
                sum = sum + 1;
            }
        }
        int checksum = ~sum;
        if(checksum == sentCheckSum)
        {
            return true;
        }
        else if(checksum != sentCheckSum)
        {
            return false;
        }
        
        return false;
    }

    public byte[] destuff(byte[] framedata) 
    {
        BitSet bs = BitSet.valueOf(framedata);
        int length = bs.size();
        
        for(int i = 0; i < bs.size(); i++)
        {
            if(bs.get(i) == true)
            {
                System.out.print("1");
            }
            else if(bs.get(i) == false)
            {
                System.out.print("0");
            }
        }
        System.out.println();
        
        int dcnt=0;
        for(int i = 0; i < bs.size(); i++)
        {
            if(bs.get(i) == true)
            {
                dcnt++;
            }
            else if(bs.get(i) == false)
            {
                dcnt=0;
            }
            
            if(dcnt == 5 && bs.get(i+1)==false)
            {
                for(int j = i+1; j <bs.size(); j++)
                {
                    bs.set(j,bs.get(j+1));
                }
            }
        }
        
        for(int i = 0; i < bs.size(); i++)
        {
            if(bs.get(i) == true)
            {
                System.out.print("1");
            }
            else if(bs.get(i) == false)
            {
                System.out.print("0");
            }
        }
        
        BitSet bs1 = new BitSet(16);
        bs1 = (BitSet) bs.clone();
        
        for(int i = 0; i < 16; i++)
        {
            if(bs1.get(i) == true)
            {
                System.out.print("1");
            }
            else if(bs1.get(i) == false)
            {
                System.out.print("0");
            }
        }
        System.out.println();
        
        byte[] bytearray = new byte[16];
        bytearray = bs1.toByteArray();
        return bytearray;
    }

}
