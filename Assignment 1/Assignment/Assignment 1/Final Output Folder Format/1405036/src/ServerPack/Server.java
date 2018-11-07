/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerPack;

import DataPack.ConnectionUtilities;
import DataPack.DataClass;
import DataPack.FrameCreator;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import static java.lang.Integer.max;
import static java.lang.Integer.min;
import static java.lang.Math.ceil;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;
import javax.net.ssl.SSLServerSocket;

/**
 *
 * @author USER
 */
public class Server {

    ArrayList clients;
    Hashtable<String, ClientInfo> clientList = new Hashtable<String, ClientInfo>();

    Hashtable<Integer, FileInfo> fileList = new Hashtable<Integer, FileInfo>();
    public int fileId = 0;
    byte[][] chunks;
    int totalBuff;
    int buffCount;
    Hashtable<Integer, ArrayList<Integer>> fileArr;

    DataClass dc;
    FrameCreator fc;
    
    ServerSocket serverSocket;

    public Server() 
    {
        //chunks=new byte[1000000][];
        buffCount = 0;
        totalBuff = 1000000000;
        fileArr = new Hashtable<>();
  
    }

    public FileInfo searchChunk(int id) 
    {
        FileInfo fInfo = fileList.get(id);
        if (fInfo != null) 
        {
            return fInfo;
        }

        System.out.println("NULL chunk NOT found");
        return null;
    }

    public boolean checkId(ConnectionUtilities con, Socket server) throws IOException 
    {
        if (clientList.get(con.roll) != null
                && server.getInetAddress().toString().compareTo(clientList.get(con.roll).ip) == 0) 
        {
            System.out.println("User still logged in");
            return true;
        }
        //System.out.println(server.getInetAddress().toString()+",,,"+clientList.get(con.roll).ip);
        dc = new DataClass();
        dc = (DataClass) con.read();
        String roll = dc.command;
        System.out.println(roll);

        if (clientList.get(roll) != null) 
        {
            dc = new DataClass();
            dc.isRead = false;
            con.write(dc);
            con.getSocket().close();
            return false;
        }

        String ip = server.getInetAddress().toString();
        int port = server.getPort();
        con.roll = roll;

        clientList.put(roll, new ClientInfo(ip, port, con));
        System.out.println("" + clientList.get(roll));
        dc = new DataClass();
        dc.isRead = true;
        con.write(dc);
        return true;
    }

    public void go() 
    {
        clients = new ArrayList();
        try 
        {
            serverSocket = new ServerSocket(5000);
            while (true)
            {
                Socket sock=serverSocket.accept();
                ConnectionUtilities connectSender=new ConnectionUtilities(sock);
                
                Socket clientSocket = serverSocket.accept();
                //System.out.println("Connection Established");
                System.out.println("" + clientSocket.getPort());

                ConnectionUtilities connectionReceiver = new ConnectionUtilities(clientSocket);
                
                Thread t = new Thread(new ClientHandler(connectSender,connectionReceiver));
                t.start();
            }

        } 
        catch (Exception ex) 
        {
            System.out.println("Exception Server Socket " + ex);
        }

    }

    public void tellSpeific(ConnectionUtilities con, String message)
    {

        //while(it.hasNext())
        //{
        try 
        {
            System.out.println("" + clientList.keySet());

            dc = new DataClass();
            dc.setData(message);
            con.write(dc);

        } 
        catch (Exception ex)
        {
            System.out.println("Cannot Write tellSpecific(): " + ex);
        }
        //}
    }

    public synchronized int talkReceiver(int id) 
    {

        //while(it.hasNext())
        //{
        try 
        {
            FileInfo fInfo = fileList.get(id);
            ClientInfo cInfo = clientList.get(fInfo.receiver);
            if (cInfo == null) 
            {
                System.out.println("NULL!!!!!!!!!!!");
            }
            ConnectionUtilities con = cInfo.client;
            Socket sock = con.getSocket();
            if (sock.isClosed()) 
            {
                System.out.println("Server is closed in tellSpecific()!!!!!!!");
            }
            String message = "";
            byte[] total = new byte[fInfo.fileSize + 200];
            int len = 0;
            //System.out.println("TTTTTTT: " + id);//$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
            FileInputStream fin = new FileInputStream(fInfo.newFile);

            //ArrayList<Integer> arr = fileArr.get(id);
            int c;
            byte[] bin = new byte[1000];
            //ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
            while ((c = fin.read(bin, 0, 1000)) != -1)
            {
                dc = new DataClass();
                byte[] bin2 = new byte[c];
                System.arraycopy(bin, 0, bin2, 0, c);
                dc.setData(id, "newFile", bin2);
                //System.out.println("CCC: " + c);//$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
                con.write(dc);

            }
            fin.close();
            dc = new DataClass();
            dc.setData("write complete");
            con.write(dc);

            
        } 
        catch (Exception ex) 
        {
            System.out.println("Cannot Write talkReceiver(): ");
            ex.printStackTrace();
            clearChunk(id);//######################
        }
        //}
        return 1;
    }

    public void talkReceiverAll(ConnectionUtilities con,boolean isRead)
    {
        try 
        {
            Set<Integer> keys = fileList.keySet();
            FileInfo f=new FileInfo();
            for (Iterator<Integer> it = keys.iterator(); it.hasNext();) 
            {
                int id = it.next();
                System.out.println("" + fileList.keySet());
                f = fileList.get(id);
                if (f.receiver.compareTo(con.roll) == 0) 
                {

                    if(isRead)
                    {
                        talkReceiver(id);
                    }
                    else
                    {
                        System.out.println("Client don't want to read it");
                    }
                    clearChunk(id);
                    it.remove();
                    break;
                }
            }
            dc = new DataClass();
            dc.setData("finishReceiving file "+f.fileName);
            con.write(dc);
            
            System.out.println("finishSending");

        } 
        catch (Exception ex) 
        {
            System.out.println("Error in talkReceiverAll(): " + ex);
            ex.printStackTrace();
        }
    }

    public void clearChunk(int id) 
    {
        System.out.println("C  L  E  A  R  I  N  G  C  H  U  N  K  S///////////////////////////");
        //while(it.hasNext())
        //{
        try 
        {
            FileInfo fInfo;
            if ((fInfo = fileList.get(id)) != null) 
            {
                if (fInfo.newFile.exists())
                {
                    totalBuff = totalBuff + (int) fInfo.newFile.length();
                    fInfo.newFile.delete();
                    System.out.println("C  L  E  A  R  E  D///////////////////////////");
                }
            }

        } 
        catch (Exception ex)
        {
            System.out.println("Cannot Write clearChunk(): " + ex);
        }
        //}
    }
    

    public class ClientHandler implements Runnable
    {

        ConnectionUtilities conReceiver;
        ConnectionUtilities conSender;
        int chunkNo;

        public ClientHandler(ConnectionUtilities conS,ConnectionUtilities conR) throws IOException 
        {
            conSender = conS;
            conReceiver = conR;

        }

        @Override
        public void run() 
        {
            try 
            {
                if (!checkId(conReceiver, conReceiver.getSocket()))
                {
                    System.out.println("Multiple Login");
                    throw new IOException();
                } 
                else 
                {
                    Thread t2 = new Thread(new SendFile(conReceiver));
                    t2.start();
                }
                while (true) 
                {
                    System.out.println("Server Process Begins");
                    
                    
                    System.out.println("" + clientList.keySet());
                    if (conSender.getSocket().isClosed())
                    {
                        System.out.println("closed");
                        throw new IOException();
                    }
                    dc = new DataClass();

                    if ((dc = (DataClass) conSender.read()) == null)
                    {
                        throw new IOException();
                    }
                    System.out.println("" + dc.command);
                    if (dc.command.compareTo("sender") == 0)
                    {
                        String message = "";
                        chunkNo = 0;
                        System.out.println("File Type Reading from Sender:");

                        dc = new DataClass();
                        //System.out.println(""+connect.read().getClass());
                        if ((dc = (DataClass) conSender.read()) == null) 
                        {
                            throw new IOException();
                        }
                        message = dc.command;
                        System.out.println(message);
                        String[] strs;
                        strs = message.split(":");
                        if (clientList.get(strs[3]) == null) 
                        {
                            System.out.println("sorry:Receiver in Offline:");
                            dc = new DataClass();
                            dc.setData("sorry:Receiver in Offline:");
                            conSender.write(dc);
                            //sock.close();
                        } 
                        else if (totalBuff - Integer.valueOf(strs[1]) < 0) 
                        {
                            System.out.println("sorry:No Available Space:");
                            dc = new DataClass();
                            dc.setData("sorry:No Available Space:");
                            conSender.write(dc);
                            //sock.close();
                            //throw new Exception();
                        } 
                        else 
                        {
                            System.out.println("Used Buffer: " + (1000000000 - totalBuff)
                                    + " Available Space: " + totalBuff);
                            ++fileId;
                            FileInfo fInfo = new FileInfo(fileId, strs[0],
                                    Integer.valueOf(strs[1]), strs[2], strs[3]);
                            fileList.put(fileId, fInfo);

                            ArrayList<Integer> fileIdArr = new ArrayList<>();
                            fileArr.put(fileId, fileIdArr);
                            //tellSpeific("20",fInfo.sender);
                            Random ran = new Random();
                            int fileSize = Integer.valueOf(strs[1]);
                            int i = ran.nextInt(min(fileSize,500));
                            i = max(1,(int) ceil(fileSize/100));
                            tellSpeific(conSender, i + ":ready:" + fileId + ":");
                            int c = 0, total = 0, off = 0, id = -1;
                            message = "";
                            byte[] cin = new byte[200];
                            
                            int expectedFrame=1;
                            //System.out.println("SERVER OK BEFORE LOOP");
                            
                            
                            while (true) 
                            {
                                dc = new DataClass();
                                if ((dc = (DataClass) conSender.read()) == null)
                                {
                                    throw new Exception();
                                }
                                // System.out.println("SERVER OK");//$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
                                String str = dc.command;
                                id = dc.fileId;
                                
                                boolean isTimeout=dc.isTimeOut;
                                // System.out.println("SERVER OK");//$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
                                message += dc.command;
                                //System.out.println("SERVER OK");//$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
                                //System.out.println("C: " + c);
                                //System.out.println("Used by Socket:" + conSender.getSocket().getPort());
                                chunkNo++;
                                //System.out.println("New CHUNK Received: "+chunkNo);
                                System.out.println("EXPECTED FRAME: "+expectedFrame);
                                fc=new FrameCreator();
                                System.out.println("Received data: " + fc.printString(
                                        new String(dc.binData)));
                                String removeString=fc.removeHeadTail(new String(dc.binData));
                                String deStuff=fc.deStuffing(removeString);
                                int seq=fc.getSequence(deStuff);
                                int ack=fc.getAck(deStuff);
                                String checkSum=fc.getCheckSum(deStuff);
                                String mainString=fc.getPayLoad(deStuff);
                                boolean checkSumVerification=fc.checkSumVerification(
                                        mainString, checkSum);
                                
                                
                                FileOutputStream ofNew = new FileOutputStream(fInfo.newFile, true);

                                if (ack == 3) 
                                {
                                    ofNew.close();
                                    System.out.println("eof received");
                                    ///fInfo = searchChunk(id);
                                    System.out.println("Total Received: " + fInfo.fileSize);
                                    System.out.println("" + fileSize);
                                    if ((int) (fInfo.fileSize) != fileSize)
                                    {
                                        tellSpeific(conSender, "receivedFailed");
                                        throw new Exception();
                                    } 
                                    else 
                                    {
                                        tellSpeific(conSender, "receiveComplete");
                                    }
                                    break;
                                }
                                //message+=ss[1];

                                //##########################################################
                                //fInfo = searchChunk(id);
                                System.out.println("CheckSumVerification:" +
                                                checkSumVerification+"\n");
                                if (checkSumVerification) 
                                {
                                    
                                    if(expectedFrame==seq)
                                    {
                                        System.out.println("Expected FRAME Matched:" +
                                                expectedFrame);
                                        //System.out.println("Received: " + str);
                                        byte[] bArr = fc.stringToByte(mainString);
                                        ofNew.write(bArr);
                                        ofNew.flush();
                                        totalBuff = totalBuff - bArr.length;
                                        ofNew.close();
                                        System.out.println("Cumulative fLength " + 
                                                fInfo.newFile.length());

                                        //System.out.println(""+String.valueOf(chunks[index],0,c));
                                        //total+=c;
                                        off += c;
                                        //System.out.println(total);
                                        //tellSpeific(conSender, "received");
                                        expectedFrame++;
                                    }
                                    else
                                    {
                                        System.out.println("DUPLICATE FRAME FOUND");
                                    }
                                    
                                }
                                
                                if(checkSumVerification && !isTimeout)
                                {
                                    System.out.println("SERVER Replying: ");
                                    dc=new DataClass();
                                    fc = new FrameCreator();
                                    byte[]dummy=new byte[1];
                                    dc.setData(fileId, fc.sendPacket(dummy, expectedFrame, 1, 1, false));
                                    conSender.write(dc);
                                }
                                else
                                {
                                    System.out.print("Server NOT REPLYING DUE TO: ");
                                    
                                    if(!checkSumVerification)
                                    {
                                        System.out.println("CheckSumVerification: "+checkSumVerification);
                                    }
                                    else if(isTimeout)
                                    {
                                        System.out.println("Ack LOST");
                                    }
                                    
                                }
                                //System.out.println(total);

                            }
                            System.out.println("Done receiving by server");
                            //System.out.println("MMMMMMMMMMM");
                            tellSpeific(clientList.get(fInfo.receiver).client,
                                    "want to receive "+fInfo.fileName+" from "+fInfo.sender+"?\n");
                            //talkReceiverAll(clientList.get(fInfo.receiver).client);

                        }
                    }
                    /*else 
                    {
                        System.out.println("SSSSSSSSSSSSS");
                        talkReceiverAll(conReceiver);
                    }*/
                }
            } 
            catch (IOException ex) 
            {
                System.out.println("Error I/O Server Distributing File: ");
                //ex.printStackTrace();
                //clientList.put(connect.roll,null);
                clientList.remove(conReceiver.roll);
            } 
            catch (Exception ex)
            {
                System.out.println("Error Server Distributing File: ");
                //ex.printStackTrace();
                //clientList.put(connect.roll,null);
                //clientList.remove(connect.roll);
                clearChunk(fileId);
                fileList.remove(fileId);
            }
        }
    }
    public class SendFile implements Runnable
    {
        ConnectionUtilities conReceiver;
        
        public SendFile(ConnectionUtilities con)
        {
            conReceiver=con;
        }

        @Override
        public void run() {
            try
            {
                while(true)
                {
                    System.out.println("SendFile Starts");
                    dc=new DataClass();
                    dc=(DataClass)conReceiver.read();
                    //System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAA: "+dc.command);
                    if(dc.command.compareTo("receiver")==0)
                    {
                        
                        talkReceiverAll(conReceiver,dc.isRead);
                        
                    }
                }
            }
            catch (Exception ex) 
            {
                System.out.println("Exception Server Sending File: Client is closed");
                //clientList.put(connect.roll,null);
                clientList.remove(conReceiver.roll);
                //ex.printStackTrace();
            } 
        }
    }
    
    public String getBits(byte bin)
    {
        StringBuilder sBits=new StringBuilder();
        for(int i=7;i>=0;i--)
        {
            if(((bin>>>i) & 1)==1)
            {
                sBits.append('1');
            }
            else
            {
                sBits.append('0');
            }
        }
        System.out.println("getBits(): "+sBits.toString());
        return sBits.toString();
    }

    public static void main(String[] args) 
    {
        //byte[] b=new String("01111110").getBytes();
        //System.out.println(""+Integer.toString(b[1],2));
        Server sc = new Server();
        sc.go();
    }
}
