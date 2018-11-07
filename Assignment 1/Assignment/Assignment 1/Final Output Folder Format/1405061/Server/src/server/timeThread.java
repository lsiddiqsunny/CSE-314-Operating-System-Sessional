/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.DataOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ASUS
 */
public class timeThread implements Runnable{

    
    boolean flag=false;
   
    
    long startTime;
    long estimatedTime=0;
    int timeout=0;
    boolean comSend=false;
    int test=0;
    
    
    //new
    
    
    watch s;
    Thread t;
 
    
    timeThread()
    {
        flag=false;
         s = new watch();
        s.startThread();
        t=new Thread(this);
       
    }
    @Override
     public void run() {
        
        while(true)
        {
            
            if(flag){
                //Thread.sleep(50);
              //  System.out.println("Time thread test updated "+test);
                
                if(comSend)
            {
                s=null;
                comSend=false;
                break;
            }
                //new 
                
            int[] curTime = s.getTime();
            System.out.println(curTime[0] + " : " + curTime[1] + " : " + curTime[2] + " : " + curTime[3]);
            if(curTime[2]>1)
            {
                System.out.println("\nTime is up!!");
                flag=false;
                break;
            }
            }
            
        }
        t=null;
       }
    
    synchronized public void startTime()
    {
   
        t=new Thread(this);
        flag=true;
        startTime=System.currentTimeMillis();
        System.out.println("timeThread started");
        t.start();

    }
    synchronized public void finishTime()
    {
        flag=false;
        System.out.println("TimeThread finished");
    }
    public void completeSending()
    {
        comSend=true;
        s=null;
    }
}
