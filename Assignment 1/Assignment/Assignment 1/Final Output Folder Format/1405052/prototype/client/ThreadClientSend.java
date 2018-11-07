package prototype.client;

import prototype.commons.Message;
import prototype.commons.NetworkFx;

import java.io.FileNotFoundException;
import java.io.IOException;

public class ThreadClientSend implements Runnable{
    Client client;
    Thread thread;
    boolean b;
    ThreadClientSend(Client client){
        this.b = false;
        this.client = client;
        this.thread = new Thread(this);
        thread.start();

    }
    @Override
    public void run() {
        try {
            client.nfxSend = new NetworkFx(client.serverAddress, client.serverPort);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(client.nfxSend.readFx());
        client.nfxSend.writeFx(true);
        Message m = new Message(); m.sid = client.sid; m.ip = client.ip;
        client.nfxSend.writeFx(m);
        while(true){
            if(client.nfxSend.noSocketException==false){ break; }
            if(client.nfxSend.socketIsConnected() == false){ break; }

            try {
                client.ClientSender();
                //thread.suspend();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } /*catch (InterruptedException e) {
                e.printStackTrace();
            }*/

        }
    }
}