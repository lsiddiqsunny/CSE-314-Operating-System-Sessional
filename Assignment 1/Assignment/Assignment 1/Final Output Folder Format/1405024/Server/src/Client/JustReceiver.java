package Client;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Created by Ashiqur Rahman on 9/21/2017.
 */
public class JustReceiver implements Runnable{
    DataInputStream din;
    DataOutputStream dout;
    Socket socket;
    Thread thread;

    public JustReceiver( Socket socket){
        try {
            this.din = new DataInputStream(socket.getInputStream());
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
        String msg;
        Scanner scan=new Scanner(System.in);
        BufferedOutputStream bfout=null ;
        FileOutputStream fout=null;
        while(true) {
            try {

                byte[] bytes = new byte[1000]; // so far largest file .
                int ss=din.read(bytes);
                System.out.println(ss);
                String msgs = new String(bytes).trim();
                System.out.println("--"+msgs+" --");
                String msgarray[] = msgs.split("#");
                if(msgs.substring(0,3).equals("111"))
                {

                }
                else if (msgarray[0].indexOf("101")>=0) {
                    System.out.println(msgarray[1] + " size " + msgarray[2] + " from " + msgarray[3]);
                    SenderReciver.w.mylabel.setText(msgarray[1] + " size " + msgarray[2] + " from " + msgarray[3]);
                   // msg = scan.nextLine();
                    while(window.msg.equals("--"));
                    msg=window.msg;
                    window.msg="--";
                    SenderReciver.w.mylabel.setText("File came");
                    if (msg.equals("y")) {
                        dout.writeUTF("ok send");
                        dout.flush();
                        String filename=msgarray[1];
                        if(bfout!=null) bfout.close();
                        fout = new FileOutputStream("newdownload\\"+filename);
                        bfout = new BufferedOutputStream(fout);

                    }
                    else {
                        dout.writeUTF("no send");
                        dout.flush();
                    }

                }
                else if( msgs.equals("**completed**"))
                {
                    bfout.close();
                    fout.close();
                }
                else {

                    bfout.write(bytes,0,ss);
                }

            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                System.out.println("server is down");
                break;
                //e.printStackTrace();
            }
        }
    }
}
