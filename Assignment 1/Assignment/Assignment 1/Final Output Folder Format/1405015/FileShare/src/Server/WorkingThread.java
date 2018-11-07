package Server;

import Tools.NetworkUtil;
import javafx.stage.FileChooser;
import sun.nio.ch.Net;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;

/**
 * Created by Toufik on 9/27/2017.
 */
public class WorkingThread implements Runnable{
    ServerThread serverThread;
    Thread thread;
    String ID;
    NetworkUtil netUtil;
    public int flag =0;
    public WorkingThread(ServerThread serverThread, String id, NetworkUtil nu) throws IOException
    {
        this.serverThread = serverThread;
        this.ID = id;
        this.netUtil = nu;
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        outer:while(true)
        {
            String str = (String )netUtil.read();
            if(str==null)
            {

            }
            else if(str.equals("receive"))
            {
                String msz = "send";
                netUtil.write(msz);
                msz = (String) netUtil.read();
                if(serverThread.studentConnectionList.containsKey(msz))
                {
                    String receiver = msz;
                    msz = "yes";
                    netUtil.write(msz);
                    long size =(long) netUtil.read();
                    if(size<=serverThread.Avialable())
                    {
                        netUtil.write(msz);
                        String fileName =(String) netUtil.read();
                        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                        String fileID = ID+ ":"+receiver+":";
                        fileID += fileName+ ":"+timestamp.toString();
                        serverThread.controller1.log.appendText("FileID: "+fileID+"is being received\n");
                        long noOfchunks;
                        long chunksize;
                        long receivedSize=0;
                        Random random = new Random();
                        noOfchunks =(random.nextInt(127)+1);
                        chunksize = (size/noOfchunks)+1;
                        netUtil.write(chunksize);
                        ArrayList<byte[]> chunks = new ArrayList<>();
                        serverThread.fileChunk.put(fileID,chunks);
                        int receivedSeqNo=-1;
                        String status ="";
                        int frameloss =0;
                        while(true)
                        {
                            Object obj = netUtil.read();
                            if(obj instanceof String )
                            {
                                String rep = (String) obj;
                                if(rep.equals("time out"))
                                {
                                    serverThread.controller1.log.appendText(fileID+" Transmission timed out\n");
                                    serverThread.fileChunk.remove(fileID);
                                    serverThread.Increase(receivedSize);
                                    break;
                                }
                                else if(rep.equals("sendack"))
                                {
                                    if(status.equals(""))
                                    {
                                        if(frameloss==1)
                                        {
                                            int lost=random.nextInt(receivedSeqNo);
                                            for(int j=chunks.size()-1;j>lost;j--)
                                            {
                                                chunks.remove(j);
                                            }
                                            receivedSeqNo=lost;
                                            frameloss=0;
                                        }

                                        byte[] temp = new byte[4];
                                        temp[0]=temp[3]=126;
                                        temp[1]=0;
                                        temp[2]=(byte)receivedSeqNo;
                                        netUtil.write(temp);
                                        //String seq = String.valueOf(receivedSeqNo);
                                        //netUtil.write(seq);
                                    }
                                    else if(status.equals("nob"))
                                    {
                                        netUtil.write(status);
                                        break;
                                    }
                                }
                                else if(rep.equals("cDisconnect"))
                                {
                                    serverThread.controller1.log.appendText("File Transmisson aborted for: "+fileID+"\n");
                                    //System.out.println("disconnected");
                                    serverThread.fileChunk.remove(fileID);
                                    serverThread.Increase(receivedSize);
                                    break;
                                }
                                else if(!rep.equals("ok"))
                                {

                                    serverThread.fileChunk.remove(fileID);
                                    serverThread.Increase(receivedSize);
                                    serverThread.controller1.log.appendText("File Transmisson aborted for: "+fileID+"\n");
                                    String ack = "File Transmisson aborted for: "+fileID+"\n";
                                    netUtil.write(ack);
                                    break;
                                }
                                else
                                {
                                    String ack = "Successfully uploaded "+fileID;
                                    serverThread.controller1.log.appendText(ack+"\n");
                                    netUtil.write(ack);
                                    NetworkUtil recepient;
                                    recepient = serverThread.studentConnectionList.get(receiver);
                                    String knock ="receive";
                                    recepient.write(knock);
                                    recepient.write(fileID);
                                    break;
                                }
                            }
                            else if(obj instanceof byte[])
                            {
                                byte[] b = (byte[]) obj;
                                //System.out.println(b[2]);
                                if(((int)b[2]==(receivedSeqNo+1))&&status.equals(""))
                                {
                                    //System.out.println("ok");
                                    ++receivedSeqNo;
                                    //System.out.println("seq:" + receivedSeqNo);
                                    chunks.add(b);
                                    if((serverThread.Avialable()-b.length)>0)
                                    {
                                        serverThread.Decrese(b.length);
                                        receivedSize+=b.length;
                                        byte[] temp = new byte[4];
                                        temp[0]=temp[3]=126;
                                        temp[1]=0;
                                        temp[2]=(byte)receivedSeqNo;
                                        netUtil.write(temp);
                                    }
                                    else
                                    {
                                        serverThread.fileChunk.remove(fileID);
                                        serverThread.Increase(receivedSize);
                                        status = "nob";
                                        netUtil.write(status);
                                        serverThread.controller1.log.appendText("File Transmisson aborted for: "+fileID+"\n");
                                        break;

                                    }
                                }
                            }
                        }
                    }
                    else
                    {
                        msz = "no";
                        netUtil.write(msz);
                    }
                }
                else
                {
                    msz = "no";
                    netUtil.write(msz);
                }
            }

            else if(str.equals("send"))
            {
                String fileID =(String) netUtil.read();
                System.out.println(fileID);
                Object obj = netUtil.read();
                while(! (obj instanceof String) )
                {
                    obj =  netUtil.read();
                }
                String report = (String) obj;

                System.out.println(report);
                ArrayList<byte[]> arrayList = serverThread.fileChunk.get(fileID);
                long size =0;
                for(int i=0;  i<arrayList.size();i++)
                {
                    size+=arrayList.get(i).length;
                }
                if(!report.equals("no"))
                {
                    /*for(int i=0;i<arrayList.size();i++)
                    {
                        netUtil.write(arrayList.get(i));
                    }
                    rep="complete";
                    netUtil.write(rep);*/
                    int i=0;
                    flag=0;
                    Timer timer = new Timer();
                    timer.schedule(new TimerServer(this),1000);
                    while(true) {
                        if (i < arrayList.size()) {
                            netUtil.write(arrayList.get(i));
                            i++;
                        }
                        if (flag == 1) {
                            flag = 0;
                            String ack = "";
                            String getack = "sendack";
                            byte[] temp = new byte[4];
                            netUtil.write(getack);
                            try {
                                netUtil.socket.setSoTimeout(1000);
                                Object o = netUtil.tread();
                                if (o instanceof String) {
                                    ack = (String) o;
                                } else {
                                    temp = (byte[]) o;
                                }
                            } catch (Exception e) {
                                if (e instanceof InterruptedIOException) {
                                    String reply = "time out";
                                    netUtil.write(reply);
                                    break;
                                }
                            }
                            if (ack.equals("nob")) {
                                //serverThread.controller1.log.appendText("Server buffer is full, try later\n");
                                break;
                            } else {
                                //i=Integer.parseInt(ack)+1;
                                i = temp[2] + 1;
                                serverThread.controller1.log.appendText("Received ack No: " + (i - 1) + "\n");
                                if (i == arrayList.size()) {
                                    String reply = "complete";
                                    netUtil.write(reply);
                                    //reply = (String) netUtil.read();
                                    //serverThread.controller1.log.appendText(rep + "\n");
                                    break;
                                } else {
                                    timer.schedule(new TimerServer(this), 1000);
                                }
                            }
                        }
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    /*try
                    {
                        for(int i=0;i<arrayList.size();i++)
                        {
                            netUtil.ewrite(arrayList.get(i));
                        }
                        rep="complete";
                        netUtil.write(rep);
                    }
                    catch (Exception e)
                    {
                        serverThread.controller1.log.appendText("File sending Failed\n");
                    }*/
                }
                serverThread.fileChunk.remove(fileID);
                serverThread.Increase(size);
            }
            else if(str.equals("cDisconnect"))
            {
                netUtil.write(str);
                netUtil.closeConnection();
                serverThread.studentConnectionList.remove(ID);
                break outer;
            }
        }
    }
}
