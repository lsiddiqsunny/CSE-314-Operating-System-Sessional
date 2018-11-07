package Server;

import Tools.NetworkUtil;

import java.io.IOException;
import java.util.Hashtable;

/**
 * Created by Toufik on 9/25/2017.
 */
public class ReceivingThread implements Runnable {
    NetworkUtil netUtil;
    String ID;
    ServerGUIController1 controller1;
    public Hashtable<String ,NetworkUtil> studentConnectionList;
    Thread thread;

    public ReceivingThread(String ID, NetworkUtil nt, ServerGUIController1 sc, Hashtable<String,NetworkUtil> ht) throws IOException
    {
        this.controller1 =sc;
        this.ID = ID;
        this.netUtil =nt;
        this.studentConnectionList = ht;
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {

    }
}
