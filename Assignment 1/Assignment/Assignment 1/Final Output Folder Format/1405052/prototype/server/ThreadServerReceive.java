package prototype.server;

import prototype.commons.NetworkFx;

import java.io.IOException;

public class ThreadServerReceive implements Runnable{

    boolean b;
    Server server;
    NetworkFx nfx;//Socket clientSock1, clientSock2;
    Thread thread;

    public ThreadServerReceive(Server server, NetworkFx nfx){
        this.server = server;
        this.b = b;
        this.nfx = nfx;
        this.thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {

        try {
            server.ServerReceive(nfx);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
