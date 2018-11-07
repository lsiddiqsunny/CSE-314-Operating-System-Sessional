package com.company;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Timer implements Runnable {
    Thread thr;
    int start=0;
    ObjectOutputStream oos;
    Socket sc;
    int stop=0;
    int lost_f_f=0;
    String cmsg="";
    Timer(ObjectOutputStream oos){
        this.thr=new Thread(this);
        this.oos=oos;
        this.thr.start();
    }
    Timer(Socket sc,int lost_f_f,String cmsg){
        this.thr=new Thread(this);
        this.sc=sc;
        this.lost_f_f=lost_f_f;
        this.cmsg=cmsg;
        this.thr.start();
    }
    @Override
    public void run() {
        while(true){
            start++;
            try {
                Thread.sleep(1000);
                if(stop==1) break;
                if(start==30) {
                    break;
                }
                if(start==3){

                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }/* catch (IOException e) {
                e.printStackTrace();
            }*/

        }

    }
}
