package sample;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class ThreadForRead implements Runnable {

    private Main main;
    private SecondPage secondPage;

    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;

    private byte[] AckFrame;

    private long start;
    private long end;
    private long diff;

    private int noOfIteration;
    private int index;

    public ThreadForRead(SecondPage sp, Socket skt, ObjectInputStream oi, ObjectOutputStream oo, Main mm, int noOfIter, int ii){
        secondPage = sp;
        main = mm;

        socket = skt;
        ois = oi;
        oos = oo;
        AckFrame = new byte[10];

        noOfIteration = noOfIter;
        index = ii;


        Thread t = new Thread(this);
        t.start();
    }




    @Override
    public void run() {
        long time = 30000;

        try {
            int loopno=0;

            if(noOfIteration<8) loopno = noOfIteration;
            else loopno = 8;
            //System.out.println("ind: "+index+" "+loopno);
            for(int i=index;i<=loopno;i++) {
                socket.setSoTimeout((int) time);

                System.out.println("Time Left: "+(time/1000)+" seconds");

                start = System.currentTimeMillis();
                ois.read(AckFrame, 0, 7);
                end = System.currentTimeMillis();

                byte pp = AckFrame[3];
                int temp = (int) AckFrame[4];

                int ii = (int) pp;
                //System.out.println("II "+ii);

                diff = end - start;
                time = time - diff;
                if(ii==1 && temp==i) {
                    main.sharedVar = i;
                    main.fileNo++; //no of times acknowledgement is being received

                    //System.out.println("File No: " + main.fileNo);
                    //System.out.println("In thread: "+main.sharedVar);

                    if (main.fileNo == noOfIteration) {
                        //System.out.println("BReak cond: "+main.fileNo+" "+noOfIteration + " "+main.sharedVar);
                        break;
                    }
                }
                /*main.sharedVar = i;
                main.fileNo++; //no of times acknowledgement is being received

                System.out.println("File No: " + main.fileNo);
                //System.out.println("In thread: "+main.sharedVar);

                if (main.fileNo == noOfIteration) {
                    //System.out.println("BReak cond: "+main.fileNo+" "+noOfIteration + " "+main.sharedVar);
                    break;
                }*/

            }

            main.permit = 1;




        } catch (Exception e) {
            main.permit = 1;
            System.out.println("Exception thrown in thread");
            //e.printStackTrace();

        }


    }
}
