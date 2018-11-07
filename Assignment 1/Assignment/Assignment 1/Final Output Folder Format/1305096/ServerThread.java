package com.company;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ServerThread implements Runnable {
    private Thread thr;
    ServerInstance si;
    int port=33333;
    int n;
    ArrayList<String> iplist = new ArrayList<String>();

    public ServerThread(ServerInstance si,int i,int n,ArrayList<String> iplist){
        port+=i;
        this.si=si;
        this.n=n;
        this.iplist=iplist;
        thr=new Thread(this);
        thr.start();
    }
    @Override
    public void run() {
        ServerSocket ss = null;
        Socket sc=null;
        ObjectInputStream ois=null;
        ObjectOutputStream oos=null;

        int flag=0;
        while (true) {
            try {
              //  System.out.println("Port1: "+port);
                ss = new ServerSocket(port);
                sc = ss.accept();
                ois = new ObjectInputStream(sc.getInputStream());
                oos = new ObjectOutputStream(sc.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }

            ReadThread rt = new ReadThread("server", ois, oos, iplist, si,port);
            rt.chunkSize(n);

            while (true) {
                if (si.flag == 0) {

                    for (String x : si.chunks) {
                        //System.out.println("Chunks : " + x);
                    }

                    int send = 0;
                    for (String x : si.receiverList) {
                      //  System.out.println("receiver :" + x + " sender: " + si.senderList.get(send));
                        send++;
                    }
                    flag=1;
                    break;
                }
            }
            if(flag==1) break;
        }


    }
}
