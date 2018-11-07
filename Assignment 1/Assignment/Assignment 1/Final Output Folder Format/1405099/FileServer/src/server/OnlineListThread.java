package server;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class OnlineListThread implements Runnable {
    
    MainFormServer main;
    
    public OnlineListThread(MainFormServer main){
        this.main = main;
    }

    @Override
    public void run() {
        try {
            while(!Thread.interrupted()){
                String msg = "";
                for(int x=0; x < main.clientUsernameList.size(); x++){
                    msg = msg+" "+ main.clientUsernameList.elementAt(x);
                    System.out.println(msg);
                }
                
                for(int x=0; x < main.clientSocketList.size(); x++){
                    Socket tsoc = (Socket) main.clientSocketList.elementAt(x);
                    DataOutputStream dos = new DataOutputStream(tsoc.getOutputStream());
                    /** CMD_ONLINE [user1] [user2] [user3] **/
                    if(msg.length() > 0){
                        dos.writeUTF("CMD_ONLINE "+ msg);
                    }
                }
                
                Thread.sleep(1900);
            }
        } catch(InterruptedException e){
            main.appendMessage("[InterruptedException]: "+ e.getMessage());
        } catch (IOException e) {
            main.appendMessage("[IOException]: "+ e.getMessage());
        }
    }
    
    
}
