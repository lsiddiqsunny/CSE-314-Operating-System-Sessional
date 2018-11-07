package Client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import Frame.*;

import javax.swing.*;

/**
 * Created by Ashiqur Rahman on 9/19/2017.
 */
public class SenderReciver implements Runnable{

    DataInputStream din;
    ObjectInputStream doin;
    DataOutputStream dout;
    Socket socket;
    Thread thread;
    public  static  window w;
    public SenderReciver( Socket socket){
        try {
            this.din = new DataInputStream(socket.getInputStream());
            //this.din=new ObjectInputStream(socket.getInputStream());
            this.dout = new DataOutputStream(socket.getOutputStream());


        } catch (IOException e) {
            e.printStackTrace();
        }
        this.socket = socket;
        thread=new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        window.frame = new JFrame("Networking");
        w=new window();
        window.frame.setContentPane(w.pane);
        window.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.frame.pack();
        window.frame.setVisible(true);
        Scanner scan=new Scanner(System.in);
        //String id=scan.nextLine();
        while(window.msg.equals("--"));
        String id=window.msg;
        window.msg="--";

        try {

            dout.writeUTF(id);
            dout.flush();
            //conferming id
            String msg=din.readUTF();
            if(msg.equals("idconfirmed"))
            {
                w.Status.setText("<--LOGGED IN-->");
                while(true) {
                    System.out.println(" Want to send file ? y/n");
                    //String option = scan.nextLine();
                    while(window.msg.equals("--"));
                    String option =window.msg;
                    window.msg="--";
                    if (option.equals("yes")) {
                        dout.writeUTF("wanttosendfile");
                        dout.flush();

                        //file sending mechanism starts
                        //String filename = scan.nextLine();
                        //String receiver = scan.nextLine();
                        w.Status.setText("<--Select file-->");
                        //File file = new File(filename);
                        while(window.msg.equals("--"));
                        File file=window.file;
                        String filename=file.getName();
                        w.Filename.setText(file.getName());
                        window.msg="--";
                        w.Status.setText("<--GIVE RECEIVER-->");
                        while(window.msg.equals("--"));
                        String receiver=window.msg;
                        window.msg="--";
                        String size = String.valueOf(file.length());

                        dout.writeUTF(receiver);
                        dout.flush();
                        dout.writeUTF(size);
                        dout.flush();
                        dout.writeUTF(filename);
                        dout.flush();

                        msg = din.readUTF();
                        if (!msg.equals("donotsend")) {
                            w.Status.setText("<--OK to SEND-->");
                            System.out.println("Got permission to send ->"+ msg);
                            int chunk = Integer.valueOf(msg);

                            byte buffer[] = new byte[chunk];
                            FileInputStream fin = new FileInputStream(file);
                            BufferedInputStream bin = new BufferedInputStream(fin);


                            System.out.println("each payload size is "+chunk);//line will be removed

                            int frameno= (int) Math.ceil(file.length()*1.0/chunk);

                            Frame frames[]=new Frame[frameno];
                            int count=0;
                            int i = fin.read(buffer);
                            while (i > 0) {
                                byte [] bbuffer= new byte[i];
                                System.arraycopy(buffer, 0, bbuffer, 0, i);
                                frames[count]=new Frame();
                                frames[count].creatFrame((byte)count, (byte) 0,bbuffer);
                                count++;
                                i = fin.read(buffer);

                            }
                            // here will bw some option code to drop frame and bit
                            ObjectOutputStream objectOutputStream=new ObjectOutputStream(socket.getOutputStream());
                            doin=new ObjectInputStream(socket.getInputStream());
                            int init=0;
                            int fault=-2;
                            int bitfault=1;
                            w.Status.setText("<--Select Frame Loss-->");
                            while(window.msg.equals("--"));
                            fault=Integer.parseInt(window.msg);
                            window.msg="--";
                            w.Status.setText("<--Select BITERROR-->");
                            while(window.msg.equals("--"));
                            bitfault=Integer.parseInt(window.msg);
                            window.msg="--";
                            w.Status.setText("<--SENDING FILE -->");
                            for( int n=0; n< frames.length;n++)
                            {
                                if(n==fault)
                                {
                                    fault=-1;
                                    continue;
                                }
                                else if(bitfault==n)
                                {

                                        boolean [] f=new boolean[frames[n].data.bytes.length];
                                        for(int j=0;j<f.length;j++)f[j]=frames[n].data.bytes[j];
                                        f[32]=!f[32];
                                        Bytes b=new Bytes(f);
                                        System.out.println("doin it ");
                                        objectOutputStream.writeObject(b);
                                        objectOutputStream.flush();
                                        bitfault=-1;

                                }
                                else
                                {
                                    objectOutputStream.writeObject(frames[n].data);
                                    objectOutputStream.flush();
                                }

                                if((n+1-init)%8==0 || n==frames.length-1)
                                {
                                    int pos=-1;
                                    try{
                                        socket.setSoTimeout(1000);
                                        for(int t=init;t<=n;t++)
                                        {
                                            Bytes b=(Bytes)doin.readObject();
                                            Frame f=new Frame();
                                            f.decodemsg(b.bytes);
                                            pos=f.seqno;
                                            //pos=din.readInt();
                                            System.out.println("server received " + pos);


                                        }
                                        socket.setSoTimeout(0);
                                        init=n+1;
                                        if(n==frames.length-1){
                                            objectOutputStream.writeObject("completed");
                                            objectOutputStream.flush();
                                        }
                                        continue;
                                    }
                                    catch (java.net.SocketTimeoutException  e)
                                    {
                                        socket.setSoTimeout(0);
                                        if(pos==-1)pos=init-1;
                                        System.out.println("time out exception "+ pos);

                                        for (int index = pos+1; index <= n; index++) {
                                            System.out.println("remaking "+index);
                                            frames[index].aqno += 1;
                                            frames[index].creatFrame(frames[index].seqno, frames[index].aqno, frames[index].array);
                                        }
                                        n=pos;
                                        init=n+1;
                                        continue;
                                    } catch (ClassNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                }


                            }

                            w.Status.setText("<--LOGGED IN -->");

                        }
                        else
                        {
                            w.Status.setText("FILE TRANSMISSION DENIED");
                            // server refuse to send file
                            System.out.println("permission denied");
                        }
                        //new JustReceiver(socket);

                    }
                    else
                    {
                        // i am telling server that i am not sending file
                        dout.writeUTF("donottosendfilesend");
                        dout.flush();
                        new JustReceiver(socket);
                        break;
                    }
                }

            }
            else
            {
                w.Status.setText("<--LOG IN DENIED-->");
                //total denial of action
                System.out.println("logged in from another device");
            }

        } catch (java.net.SocketTimeoutException  e) {

            try {
                dout.writeUTF("time out");
                dout.flush();
                System.out.println("time out error . Going offline");
                //new JustReceiver(socket);
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }


    }



}
