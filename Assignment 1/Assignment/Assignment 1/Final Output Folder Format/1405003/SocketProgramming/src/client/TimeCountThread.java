package client;

import ByteCalculation.Bytes;
import sun.nio.ch.Net;
import util.NetworkUtil;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.SocketException;
import java.net.SocketTimeoutException;

/**
 * Created by user on 10/19/2017.
 */
public class TimeCountThread implements Runnable {
    public int flag = 0;
    private NetworkUtil nc;
    private int curr;
    private int time = 20*1000;
    private Thread thr;
    public int start;

    TimeCountThread(NetworkUtil nc,int curr,int start){
        this.nc = nc;
        this.curr = curr;
        this.start = start;
        thr = new Thread(this);
        thr.start();
    }


    public void run() {
        while(true){
            curr--;
            //System.out.println("hayhay");
            long start=0;
            try {
                start = System.currentTimeMillis();
                nc.socket.setSoTimeout(time);
                //System.out.println("zaa");
                //System.out.println("ohho");
                /*Helper oo = nc.read();
                if(oo==null){
                    System.out.println("timeout");
                    break;
                }*/
                byte[] buf = new byte[4];
                int co = nc.readByte(buf);
                System.out.println("Header , Type of the frame , no of frame , Trailer");
                Bytes.print(buf);
                if(co==-5){
                    System.out.println("timeout");
                    break;
                }

                if(buf[2]==(byte)this.start)this.start++;
                //System.out.println((String)oo);
                //System.out.println("ohho");
                //System.out.println("ra");
            }
            catch (SocketException e){}
            catch (IOException e){}
            time-=(System.currentTimeMillis()-start );

            if(curr == 0)break;
        }

        flag = 1;
    }
}
