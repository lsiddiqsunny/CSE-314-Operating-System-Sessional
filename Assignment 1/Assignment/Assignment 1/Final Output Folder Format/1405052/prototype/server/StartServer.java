package prototype.server;

import prototype.commons.AuxiliaryMethods;
import prototype.commons.Message;
import prototype.commons.NetworkFx;
import prototype.commons.OnlineUsers;

import java.net.Socket;

public class StartServer {
    static Server s;
    static void detectConnection(){
        try{
            Socket cs = s.serverSocket.accept();
            System.out.println("New client request!!!");
            NetworkFx nfx = new NetworkFx(cs);

            nfx.writeFx("Hello client!");
            boolean b = (boolean) nfx.readFx();
            if(b){ // true, this means client sends, server receives!
                Message m = (Message) nfx.readFx();
                AuxiliaryMethods.onlineSenders.add(new OnlineUsers(m.ip, m.sid, nfx));
                System.out.println("Added to onlineSenders: "+m.sid+" "+m.ip+" "+nfx);

                ThreadServerReceive tsr = new ThreadServerReceive(s, nfx);
                System.out.println("New Server receive thread created!");
            }else{// client receives, server sends!!!
                Message m = (Message) nfx.readFx();
                AuxiliaryMethods.onlineReceivers.add(new OnlineUsers(m.ip, m.sid, nfx));
                System.out.println("Added to onlineReceivers: "+m.sid+" "+m.ip+" "+nfx);

                ThreadServerSend tss = new ThreadServerSend(s, m.sid, nfx); // (s, r_id)
                System.out.println("Create new ServerSendThread!");
            }


        }catch (Exception x){
            System.out.println("Here!!"+x.toString());
            x.printStackTrace();
        }
    }

    public static void main(String[] args) {
        s = new Server();
        while(true){
            detectConnection();
        }

    }
}
