package prototype.client;

import prototype.commons.Message;
import prototype.commons.NetworkFx;

import java.io.FileNotFoundException;
import java.io.IOException;

public class ThreadClientReceive implements Runnable{
    Client client;
    Thread thread;
    boolean b;
    ThreadClientReceive(Client client){
        this.client = client;
        this.b = b;
        this.thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {

        try {
            client.nfxReceive = new NetworkFx(client.serverAddress, client.serverPort);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(client.nfxReceive.readFx());

        client.nfxReceive.writeFx(false);
        Message m = new Message(); m.sid = client.sid; m.ip = client.ip;
        client.nfxReceive.writeFx(m);

        while(true){


            if(client.nfxReceive.noSocketException==false){ break; }
            if(client.nfxReceive.socketIsConnected() == false){ break; }

            try {
                client.checkIfClientHasNewMsg();
                client.clientReceiver();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}