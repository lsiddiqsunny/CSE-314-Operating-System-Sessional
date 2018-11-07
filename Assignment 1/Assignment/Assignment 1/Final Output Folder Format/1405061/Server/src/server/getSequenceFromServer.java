/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ASUS
 */
public class getSequenceFromServer implements Runnable{
    
    DataInputStream byteFromServer;
    clientProcessByte cpb;
    int getSeq=0;
    static boolean finishFile=false;
    Thread t;
    static int c=0;
    getSequenceFromServer(DataInputStream bfs) throws IOException
    {
        byteFromServer = bfs;
        cpb=new clientProcessByte();
        t=new Thread(this);
        //System.out.println("get sequence from server created");
        t.start();
        
    }
    
    

    @Override
    public void run() {
        byte []x=new byte[6];
        byte []y=new byte[4];
        while(true){
           // System.out.println("Hello");
            try {
                byteFromServer.read(x);
               // System.out.println("x byte Array");
                if(finishFile)
                {
                  finishThread();
                  break;
                }
               // cpb.printByteArray(x);
            } catch (IOException ex) {
                Logger.getLogger(getSequenceFromServer.class.getName()).log(Level.SEVERE, null, ex);
            }
            y=cpb.extractFlag(x);
          //  System.out.println("Y byte array");
          //  cpb.printByteArray(y);
            ByteBuffer wr=ByteBuffer.wrap(y);
            int g = wr.getInt();
          //  System.out.println("received sequence "+g);
            if( g == ( getSeq + 1 ))
            {
                c=1;
                getSeq++;
            }
                } //To change body of generated methods, choose Tools | Templates.
    }
    
    
    public void finishThread()
    {
        if(t.isAlive())
        {
            t = null;
        }
    }
    
}
