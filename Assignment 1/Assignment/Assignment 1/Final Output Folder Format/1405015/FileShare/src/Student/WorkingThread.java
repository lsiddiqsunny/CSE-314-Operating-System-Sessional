package Student;

import Tools.NetworkUtil;

import javax.swing.JOptionPane;
import java.io.*;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.Timer;

/**
 * Created by Toufik on 9/27/2017.
 */
public class WorkingThread implements Runnable {
    public NetworkUtil netUtil;
    StudentLogInController controller;
    Thread thread;
    public int flag=0;
    public int endFlag =0;
    public String status ="";
    public Object obj = null;
    int frameloss =0;
    public WorkingThread(NetworkUtil nu,StudentLogInController sc)throws IOException
    {
        this.netUtil = nu;
        this.controller = sc;
        thread = new Thread(this);
        thread.start();
    }
    @Override
    public void run() {
        outer:while(true)
        {
            String str = (String) netUtil.read();
            if(str==null)
            {

            }
            else if(str.equals("send"))
            {
                String msz = controller.Receiver.getText();
                netUtil.write(msz);
                msz =(String) netUtil.read();
                if(msz.equals("yes"))
                {
                    File file = new File(controller.filePath.getText());
                    long size = file.length();
                    netUtil.write(size);
                    msz=(String)netUtil.read();
                    if(msz.equals("yes"))
                    {
                        int i;
                        ArrayList<byte[]> chunks = new ArrayList<>();
                        ArrayList<byte[]> stuffed=new ArrayList<>();
                        controller.log.appendText("File sending started\n");
                        String name = file.getName();
                        netUtil.write(name);
                        long chunk = (long)netUtil.read();
                        controller.log.appendText("Chunk size: "+chunk+"B\n");
                        InputStream in = null;
                        try {
                            in = new FileInputStream(file);
                            while(true)
                            {
                                byte[] bytes;
                                if(!(in.available()<chunk))
                                {
                                    bytes = new byte[(int)chunk];
                                }
                                else
                                {
                                    bytes = new byte[in.available()];
                                }
                                in.read(bytes);
                                //System.out.println(i++);
                                //System.out.println(in.available());
                                chunks.add(bytes);
                                if (in.available()<=0) break;
                            }
                        } catch (Exception e) {

                        }

                        for (i = 0;  i<chunks.size() ; i++)
                        {
                            stuffed.add(stuff(chunks.get(i),i,(byte)1));
                            /*controller.log.appendText("Payload: ");
                            for(int k=0;k<chunks.get(i).length;k++)
                            {
                                controller.log.appendText(String.valueOf(chunks.get(i)[k])+" ");
                            }
                            controller.log.appendText("\n");
                            controller.log.appendText("Stuffed: ");
                            for(int k=0;k<stuffed.get(i).length;k++)
                            {
                                controller.log.appendText(String.valueOf(stuffed.get(i)[k])+" ");
                            }
                            controller.log.appendText("\n");*/
                        }
                        if(chunks.get(0).length>=10)
                        {
                            controller.log.appendText("Payload: ");
                            for(int k=0;k<10;k++)
                            {
                                controller.log.appendText(String.valueOf(chunks.get(0)[k])+" ");
                            }
                            controller.log.appendText("\n");
                        }
                        if(stuffed.get(0).length>=10)
                        {
                            controller.log.appendText("Stuffed: ");
                            for(int k=0;k<10;k++)
                            {
                                controller.log.appendText(String.valueOf(stuffed.get(0)[k])+" ");
                            }
                            controller.log.appendText("\n");
                        }


                        /*for(i=0;i<stuffed.size();i++)
                        {
                            netUtil.write(stuffed.get(i));
                            String ack = "";
                            try {
                                netUtil.socket.setSoTimeout(30000);
                                ack = (String)netUtil.tread();
                            } catch (Exception e) {
                                if(e instanceof InterruptedIOException)
                                {
                                    String reply ="time out";
                                    netUtil.write(reply);
                                    break;
                                }
                            }
                            if(ack.equals("nob"))
                            {
                                controller.log.appendText("Server buffer is full, try later\n");
                                break;
                            }
                        }
                        if (i==stuffed.size())
                        {
                            String rep = "ok";
                            netUtil.write(rep);
                            rep = (String) netUtil.read();
                            controller.log.appendText(rep+"\n");
                        }*/
                        i=0;
                        int ackNo;
                        flag=0;
                        obj = null;
                        endFlag=0;
                        new ReadAck(this);
                        Timer timer = new Timer();
                        timer.schedule(new TimerStudent(this),1000);
                        while(true)
                        {
                            if(i<stuffed.size())
                            {
                                netUtil.write(stuffed.get(i));
                                i++;
                            }
                            if(flag==1)
                            {
                                flag=0;
                                String ack = "";
                                //String getack="sendack";
                                byte[] temp = new byte[4];
                                while(endFlag==0)
                                {
                                }
                                if(obj instanceof String)
                                {
                                    ack=(String)obj;
                                }
                                else
                                {
                                    temp=(byte[])obj;
                                }
                                obj=null;
                                endFlag=0;
                                //netUtil.write(getack);
                                /*try {
                                    netUtil.socket.setSoTimeout(30000);
                                    Object o =netUtil.tread();
                                    if(o instanceof String)
                                    {
                                        ack=(String)o;
                                    }
                                    else
                                    {
                                        temp=(byte[])o;
                                    }
                                } catch (Exception e) {
                                    if(e instanceof InterruptedIOException)
                                    {
                                        String reply ="time out";
                                        netUtil.write(reply);
                                        break;
                                    }
                                }*/
                                if(ack.equals("nob"))
                                {
                                    controller.log.appendText("Server buffer is full, try later\n");
                                    break;
                                }
                                else
                                {
                                    //i=Integer.parseInt(ack)+1;
                                    i=temp[2]+1;
                                    controller.log.appendText("Received ack No: "+(i-1)+"\n");
                                    if(i==stuffed.size())
                                    {
                                        String rep = "ok";
                                        netUtil.write(rep);
                                        rep = (String) netUtil.read();
                                        controller.log.appendText(rep+"\n");
                                        break;
                                    }
                                    else
                                    {
                                        new ReadAck(this);
                                        timer.schedule(new TimerStudent(this),1000);
                                    }
                                }
                            }
                            try {
                                Thread.sleep(50);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    else
                    {
                        controller.log.appendText("Server buffer is full, try later\n");
                    }
                }
                else
                {
                    controller.log.appendText("Receiver is Offline\n");
                }
            }
            else if(str.equals("receive"))
            {
                String fileID = (String) netUtil.read();
                String msz = "send";
                netUtil.write(msz);
                netUtil.write(fileID);
                System.out.println(fileID);
                String[] info = fileID.split(":");
                String title = "Do you want to download\n";
                title+=info[2]+"\n";
                title+="sent by: "+info[0]+"\n";
                int a=JOptionPane.showConfirmDialog(null,title);
                String result;
                if(a==JOptionPane.YES_OPTION)
                {
                    result="ok";
                }
                else
                {
                    result="no";
                }

                if (result.equals("ok"))
                {
                    String rep ="yes";
                    netUtil.write(rep);
                    System.out.println(rep);
                    ArrayList<byte[]> fileChunks =new ArrayList<>();
                    ArrayList<byte[]> destuffed = new ArrayList<>();
                    int receivedSeqNo=-1;
                    while(true)
                    {
                        Object object=netUtil.read();
                        if(object instanceof byte[])
                        {
                            //fileChunks.add((byte[])object);

                            byte[] b = (byte[]) object;
                            //System.out.println(b[2]);
                            if((int)b[2]==(receivedSeqNo+1))
                            {
                                //System.out.println("ok");
                                ++receivedSeqNo;
                                //System.out.println("seq:" + receivedSeqNo);
                                fileChunks.add(b);
                            }
                        }
                        else if (object instanceof String )
                        {
                            rep = (String) object;
                            //System.out.println(rep);
                            if(rep.equals("sendack"))
                            {
                                if(frameloss==1)
                                {
                                    Random random = new Random();
                                    int lost=random.nextInt(receivedSeqNo);
                                    for(int j=fileChunks.size()-1;j>lost;j--)
                                    {
                                        fileChunks.remove(j);
                                    }
                                    receivedSeqNo=lost;
                                    frameloss=0;
                                }
                                byte[] temp = new byte[4];
                                temp[0]=temp[3]=126;
                                temp[1]=0;
                                temp[2]=(byte)receivedSeqNo;
                                netUtil.write(temp);
                            }
                            else if(rep.equals("complete"))
                            {
                                for(int i=0;i<fileChunks.size();i++)
                                {
                                    destuffed.add(destuff(fileChunks.get(i)));
                                    /*controller.log.appendText("Destuffed: ");
                                    for(int k=0;k<destuffed.get(i).length;k++)
                                    {
                                        controller.log.appendText(String.valueOf(destuffed.get(i)[k])+" ");
                                    }
                                    controller.log.appendText("\n");*/
                                }
                                if(destuffed.get(0).length>=10)
                                {
                                    controller.log.appendText("Destuffed: ");
                                    for(int k=0;k<10;k++)
                                    {
                                        controller.log.appendText(String.valueOf(destuffed.get(0)[k])+" ");
                                    }
                                    controller.log.appendText("\n");
                                }

                                for(int i=0;i<destuffed.size();i++)
                                {
                                    if(checkError(fileChunks.get(i),destuffed.get(i))!=true)
                                    {
                                        controller.log.appendText("Checksum error");
                                    }
                                }
                                File folder = new File("Downloads");
                                if(!folder.exists())
                                {
                                    folder.mkdir();
                                }
                                File newFile =new File(folder.getAbsoluteFile()+File.separator+info[2]);
                                try {
                                    newFile.createNewFile();
                                    OutputStream out = new FileOutputStream(newFile);
                                    for(int j=0;j<destuffed.size();j++)
                                    {
                                        out.write(destuffed.get(j));
                                        out.flush();
                                    }
                                    out.close();
                                    controller.log.appendText("File Successfully Downloaded\n");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                break;
                            }
                        }
                    }
                }
                else
                {
                    String rep ="no";
                    netUtil.write(rep);
                }
            }
            else if(str.equals("cDisconnect"))
            {
                netUtil.closeConnection();
                break outer;
            }
        }
    }
    public static byte[] stuff(byte[] arg,int seq,byte type)
    {
        ArrayList<Byte> frame = new ArrayList<Byte>();
        byte[] ret = null;
        byte temp = 0b01111110;
        byte ck1,ck2;
        int count=0;
        int j=0;
        String original="";
        String stuffed="";
        frame.add(temp);
        frame.add(type);
        frame.add((byte)seq);

        for(int i=0;i<arg.length;i++)
        {
            ck2 = arg[i];
            original += String.format("%8s", Integer.toBinaryString(ck2 & 0xFF)).replace(' ', '0');
        }
        for(int i =0;i<original.length();i++)
        {
            stuffed+=original.charAt(i);
            j++;
            if(j==8)
            {
                frame.add((byte)Integer.parseInt(stuffed, 2));
                j=0;
                stuffed="";
            }
            if(original.charAt(i)=='1')
            {
                count++;
                if(count==5)
                {
                    count=0;
                    stuffed+="0";
                    j++;
                    if(j==8)
                    {
                        frame.add((byte)Integer.parseInt(stuffed, 2));
                        j=0;
                        stuffed="";
                    }
                }
            }
            else
            {
                count =0;
            }
        }

        if(j>0)
        {
            while(j<8)
            {
                stuffed+="0";
                j++;
            }
            frame.add((byte)Integer.parseInt(stuffed, 2));
        }

        ck1=arg[0];
        for(int i=1;i<arg.length;i++)
        {
            ck1= (byte) (ck1 ^ arg[i]);
        }
        frame.add(ck1);
        temp = 0b01111110;
        frame.add(temp);
        ret = new byte[frame.size()];
        for(int i=0;i<frame.size();i++)
        {
            ret[i] = frame.get(i);
        }
        return ret;
    }


    public static byte[] destuff(byte[] arg)
    {
        byte[] ret = null;
        int j=0;
        int count=0;
        ArrayList<Byte> payload = new ArrayList<>();
        String stuffed="";
        String original="";
        byte ck2;
        for(int i=3;i<arg.length-2;i++)
        {
            ck2 = arg[i];
            stuffed += String.format("%8s", Integer.toBinaryString(ck2 & 0xFF)).replace(' ', '0');
        }

        for(int i =0;i<stuffed.length();i++)
        {
            original+=stuffed.charAt(i);
            j++;
            if(j==8)
            {
                payload.add((byte)Integer.parseInt(original, 2));
                j=0;
                original="";
            }
            if(stuffed.charAt(i)=='1')
            {
                count++;
                if(count==5)
                {
                    count=0;
                    i++;
                }
            }
            else
            {
                count =0;
            }
        }

        ret = new byte[payload.size()];
        for(int i=0;i<payload.size();i++)
        {
            ret[i] = payload.get(i);
        }
        return ret;
    }

    public static boolean checkError(byte[] arg,byte[] dearg)
    {
        if(arg[0]!=126||arg[arg.length-1]!=126) return false;
        byte cheksum;
        cheksum=dearg[0];
        for(int i=1;i<dearg.length;i++)
        {
            cheksum= (byte) (cheksum ^ dearg[i]);
        }
        if(cheksum!=arg[arg.length-2]) return false;
        return true;
    }
}
