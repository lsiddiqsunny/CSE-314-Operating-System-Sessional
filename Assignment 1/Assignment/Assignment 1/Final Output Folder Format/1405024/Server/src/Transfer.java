import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import Frame.*;
/**
 * Created by Ashiqur Rahman on 9/19/2017.
 */
public class Transfer implements  Runnable{
    DataOutputStream dout;
    DataInputStream din;
    Socket socket;
    Thread thread;
    String id;

    public Transfer(DataOutputStream dout, DataInputStream din, Socket socket,String id) {
        this.dout = dout;
        this.din = din;
        this.socket = socket;
        this.id=id;
        thread=new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        File tempfile=null;
            try {
                while(true) {
                    String s = din.readUTF();
                    if (s.equals("wanttosendfile")) {
                        System.out.println("sending files.....");

                        String reciver = din.readUTF();

                        int size = Integer.valueOf(din.readUTF());

                        String filename = din.readUTF();

                        Random r = new Random();
                        int cchunk = size/10;
                        if (cchunk <= 0) cchunk = size;
                        if (Start.check(size) && Start.studentList.containsKey(reciver)) {
                            dout.writeUTF(String.valueOf(cchunk));
                            dout.flush();

                            //saving temporary files
                            String fileId = Start.getfile();
                            fileId = fileId + filename;
                            tempfile = new File(Start.tempdir + fileId);
                            BufferedOutputStream fout =new BufferedOutputStream(new FileOutputStream(tempfile));
                            int totalsize = size;
                            ObjectInputStream objectInputStream=new ObjectInputStream(socket.getInputStream());
                            ObjectOutputStream doout=new ObjectOutputStream(socket.getOutputStream());
                            int frameno= (int) Math.ceil(size*1.0/cchunk);
                            Frame frames[]=new Frame[frameno];
                            int count=0;
                            int flag=1;
                            while (true) {

                                String msg="";
                                Bytes abyte=new Bytes();
                                Object obj= objectInputStream.readObject();
                                if(obj.getClass() != String.class )  abyte=(Bytes) obj;
                                else  msg =(String)obj;
                                //System.out.println("printing msg " + msg);//debug code

                                if (msg.trim().equals("completed")) {
                                    break;

                                }
                                else if (msg.equals("time out")) {
                                    if (Start.studentList.containsKey(id)) Start.studentList.get(id).change();
                                    fout.close();
                                    tempfile.delete();
                                    return;
                                }
                                else {

                                    Frame frame=new Frame();
                                    boolean b;

                                     b=frame.decodemsg(abyte.bytes);
                                    System.out.println("geting  frame "+ frame.seqno);

                                    if(!b) System.out.println("problem in checksum of frame no "+ frame.seqno);
                                    if(b && count==frame.seqno) {
                                        System.out.println("receiving frame no "+ count);
                                        frames[frame.seqno] = frame;
                                        //dout.writeInt(count);
                                        Frame f=new Frame();
                                        byte ack[]={(byte)1};
                                        f.creatFrame((byte)count,(byte)1,ack);
                                        doout.writeObject(f.data);
                                        doout.flush();
                                        count++;
                                    }

                                }
                            }

                            System.out.println("out size of loop");
                            for( Frame f: frames)
                            {
                                if(f.array!=null)
                                {
                                    fout.write(f.array);
                                }
                            }
                            fout.close();
                            new Filesender(fileId, filename, reciver, id);
                        } else {
                            System.out.println("not sending files");
                            dout.writeUTF("donotsend");
                            dout.flush();
                        }

                    }
                    else {
                        // not sending file
                        break;

                    }
                }
            } catch (IOException e) {
                if(tempfile!=null){ Start.inc((int) tempfile.length());tempfile.delete();
                }
                Start.studentList.remove(id);
                System.out.println(id+" went off line");
                //e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        if(Start.studentList.containsKey(id)) Start.studentList.get(id).change();
    }






}
