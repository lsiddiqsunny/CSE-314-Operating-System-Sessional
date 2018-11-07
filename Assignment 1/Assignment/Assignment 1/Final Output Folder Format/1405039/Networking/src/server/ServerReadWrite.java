package server;

import java.util.*;
import java.net.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import utility.ConnectionSetup;
import utility.Frame;

public class ServerReadWrite implements Runnable{

    public ConnectionSetup connection;
    public String myID;
    
    public ServerReadWrite(ConnectionSetup con, String username){
        connection=con;
        myID = username;
    }
    
    public void run()
    {
        while(true)
        {
            String srdecision = (String)connection.read();
            if(srdecision.equalsIgnoreCase("S"))
            {
                String receiverID = (String)connection.read();
                if(Server.clientList.containsKey(receiverID))
                {
                    connection.write((Object)"Send filename and filesize.");
                    String filename = (String)connection.read();
                    System.out.println(filename);
                    String sfilesize = (String)connection.read();
                    int filesize = Integer.parseInt(sfilesize);
                    System.out.println("Filename " + filename + ", filsize = " + filesize);
                    int emptyspace = Server.bufferSize - Server.totalBufferOccupied;
                    int chunkNumber = 0;
                    int fileID=0, chunkSize=0;
                    Frame[] framearray;
                    if(emptyspace >= filesize)
                    {
                        System.out.println("File transfer possible.");
                        chunkSize = 16;

                        fileID = Server.fileIDs + 1;
                        Server.fileIDs++;
                        Server.fileList.put(fileID, filename);
                        connection.write((Object)(chunkSize + " " + fileID));
                        int bytesReceived = 0;
                        int maxChunks = (filesize/chunkSize);
                        if(filesize%chunkSize != 0)
                        {
                            maxChunks++;
                        }
                        framearray = new Frame[maxChunks];
                        Frame recvdframe ;
                        Frame ackframe = null ;
                        String timeout = "" ;
                        int arraysize=0;
                        int expctSeqNo=0;
                        byte[] ackpayload = new byte[1];
                        while(chunkNumber != maxChunks)
                        {
                            recvdframe = (Frame)connection.read();
                            int ackNo = recvdframe.seqNo;
                            System.out.println("expecting, found sequence number in sender : " + expctSeqNo +" " + recvdframe.seqNo);
                            if(expctSeqNo == recvdframe.seqNo)
                            {
                                int seqNo = recvdframe.seqNo;
                                int datalength = (recvdframe.payload).length;
                                byte[] framedata = new byte[datalength];
                                framedata = recvdframe.payload;
                                byte[] destuffedframe = destuff(framedata);
                                int sentChecksum = recvdframe.checksum;
                                boolean verify = verifyCheckSum(destuffedframe, sentChecksum);
                                if(verify == true)
                                {
                                    System.out.println("Correct frame received.");
                                    ackpayload = new byte[1];
                                    ackpayload[0] = '+';
                                    arraysize += recvdframe.getSize();
                                    framearray[chunkNumber++] = recvdframe;
                                    expctSeqNo = 1 - expctSeqNo;
                                }
                                else if(verify == false)
                                {
                                    System.out.println("Incorrect frame received.");
                                    ackpayload = new byte[1];
                                    ackpayload[0] = '-';
                                }
                            }
                            else
                            {
                                System.out.println("Frame dropped.");
                                ackpayload = new byte[1];
                                ackpayload[0] = '+';
                            }
                            int ackchecksum = calcCheckSum(ackpayload);
                            byte[] stuffedAckFrame = bitstuff(ackpayload);
                            ackframe = new Frame('a', ackNo, stuffedAckFrame, ackchecksum);
                            connection.write((Frame)ackframe);
                            System.out.println("chunk number in server: " + chunkNumber);
                        }
                        boolean temp = true;
                        if(timeout.equalsIgnoreCase("Timeout."))
                        {
                            temp = false;
                        }
                        Server.fileBuffer.put(fileID, framearray);
                        Server.totalBufferOccupied += arraysize ;
                        String completion = (String)connection.read();
                        System.out.println(completion);
                        String successfail = "" ;
                        if(chunkNumber == maxChunks && temp == true)
                        {
                            successfail = "SUCCESS" ;
                        }
                        else
                        {
                            successfail = "FAILURE" ;
                            Server.fileBuffer.remove(fileID);
                            Server.totalBufferOccupied -= arraysize ;
                        }
                        connection.write(successfail);
                        System.out.println(successfail);
                    }
                    ConnectionSetup con = Server.clientList.get(receiverID);
                    con.write((Object)"Would you like to receive a file from " + this.myID + "? The filename is " + filename +" and the size of the file is "+ filesize + ".");
                    String sfileID = Integer.toString(fileID);
                    String data = Integer.toString(chunkSize) + "_" + Integer.toString(fileID) + "_" + Integer.toString(filesize);
                    con.write((Object)data);
                }
                else
                {
                    connection.write((Object)"The receiver is not logged in.");
                }
            }
            else if(srdecision.equalsIgnoreCase("R"))
            {
                String chunksize_fileID = (String)connection.read();
                String csfi[] = chunksize_fileID.split("_", 3);
                int chunkSize = Integer.valueOf(csfi[0]);
                int fileID = Integer.valueOf(csfi[1]);
                int filesize = Integer.valueOf(csfi[2]);
                String receiverdecision = (String) connection.read();
                int seqNo = 0;
                byte[] ackdata = new byte[1];
                if(receiverdecision.equalsIgnoreCase("Yes"))
                {
                    Frame[] fileFrames = Server.fileBuffer.get(fileID);
                    for(int i = 0; i < fileFrames.length; i++)
                    {
                        int timeout = 0;
                        Frame frame = fileFrames[i];
                        connection.write((Object)frame);
                        Frame ackframe = (Frame)connection.read();
                        if(ackframe == null)
                        {
                            System.out.println("Timeout.");
                            timeout = 1;
                        }
                        if(timeout == 1)
                        {
                            i--;
                        }
                        else
                        {
                            int ackNo = ackframe.ackNo;
                            ackdata = ackframe.payload;
                            byte[] destuffedackframe = destuff(ackdata);
                            int sentChecksum = ackframe.checksum;
                            boolean verify = verifyCheckSum(destuffedackframe, sentChecksum);
                            if(verify == true && ackdata[0] == '+')
                            {
                                //System.out.println("In verify true region.");
                                seqNo = 1 - seqNo ;
                            }
                            else
                            {
                                //System.out.println("In verify false region.");
                                i--;
                            }
                        }
                    }
                    String confirmation = (String)connection.read();
                    if(confirmation.equalsIgnoreCase("Complete"))
                    {
                        Server.fileBuffer.remove(fileID);
                        System.out.println("Files removed from storage in buffer.");
                    }
                }
                else if(receiverdecision.equalsIgnoreCase("No"))
                {
                    connection.write((Object)"The receiver does not wish to receive the file.");
                }
            }
            else if(srdecision.equalsIgnoreCase("L"))
            {
                String logout = (String)connection.read();
                System.out.println(logout);
                Server.clientList.remove(myID);
                System.out.println("ID removed from clientList.");
            }
            for (Map.Entry entry : Server.clientList.entrySet()) {
                System.out.println(entry.getKey() + ", " + entry.getValue());
            }
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
        for(int i = 0; i < bytearray.length; i++)
        {
            System.out.println(bytearray[i]);
        }
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
                //System.out.println(sum);
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
        
        //System.out.println(bs1.size());
        //System.out.println(bs1.length());
        
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
        /*
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
        */
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
        /*
        for(int i = 0; i < bytearray.length; i++)
        {
            System.out.println(bytearray[i]);
        }
        */
        return bytearray;
    }
    
    
}


