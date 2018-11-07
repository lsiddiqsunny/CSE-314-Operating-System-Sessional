import com.sun.corba.se.spi.activation.Server;

import java.io.*;

/**
 * Created by Ashiqur Rahman on 9/20/2017.
 */
public class Filesender implements Runnable {
    String fileid;
    String filename;
    String reciver;
    String sender;
    Thread thread;

    public Filesender(String fileid, String filename, String reciver,String sender) {
        this.fileid = fileid;
        this.filename = filename;
        this.reciver = reciver;
        this.sender=sender;
        thread=new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        File file=new File(Start.tempdir+fileid);
        if(Start.studentList.containsKey(reciver))
        {
            try {
                while (Start.studentList.get(reciver).state.equals("0")) {
                    try {
                        thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }catch (NullPointerException e)
            {
                return ;
            }

            DataOutputStream dout= Start.studentList.get(reciver).dout;
            DataInputStream din=  Start.studentList.get(reciver).din;
            Start.studentList.get(reciver).change();


            byte [] filechunk=new byte[(int)file.length()];
            try {
                dout.writeUTF("101"+"#"+filename+"#"+file.length()+"#"+sender);
                dout.flush();
                String mgs=din.readUTF();
                if(mgs.equals("ok send"))
                {
                    FileInputStream fileInputStream=new FileInputStream(file);
                    BufferedInputStream fin=new BufferedInputStream(fileInputStream);
                    while( fin.read(filechunk)>0)
                    {
                        dout.write(filechunk);
                        dout.flush();
                    }
                    try {
                        thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    dout.writeUTF("**completed**");
                    dout.flush();
                    fin.close();
                    fileInputStream.close();

                }else System.out.println("Receiver does not want file");

                Start.studentList.get(reciver).change();
            }
            catch (NullPointerException e)
            {
                Start.studentList.remove(reciver);
                System.out.println(reciver+" went off line");
                e.printStackTrace();
            }
            catch (Exception e) {
                Start.studentList.remove(reciver);
                System.out.println(reciver+" went off line");
                e.printStackTrace();
            }


        }
        System.out.println(Start.SIZE);
        Start.inc((int) file.length());
        System.out.println(Start.SIZE);
        file.delete();

    }


}
