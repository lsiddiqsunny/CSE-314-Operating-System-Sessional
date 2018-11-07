/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package messenger;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

/**
 *
 * @author TIS
 */
public class Read implements Runnable{
    public ConnectionUtillities connection;
    public boolean active = true;
    public String clientId ;
    public FileOption fileOption ;
    
    public Read(ConnectionUtillities con, String id){
        connection=con;
        clientId = id ;
    }

    @Override
    public void run() {
        
        Scanner in=new Scanner(System.in);
        
        while(active){
            
            
            System.out.println("Press ( Y ) to send file , to logout press ( L )");
            String text=in.nextLine(); 
            
        if(text.equals("y")){
            System.out.println("Enter receiver ID - file name");
            text = in.nextLine();
            
            String msg[]=text.split("-",2);
            //make folder
            File folder = new File(System.getProperty("user.home")+"\\Documents\\SendingItem");
            folder.mkdir();
            //
            
            String fileDir = System.getProperty("user.home")+"\\Documents\\SendingItem\\"+msg[1];
            
            File f=new File(fileDir);
            FileOption fileOption = new FileOption ( msg[0] , clientId , fileDir) ;
           // connection.write("SENDING INPUT");
            Frame fm = new Frame();
            sendFrame(fm,0,0,0,"SENDING INPUT") ;

            try {
                connection.write(fm.getFrame());
                connection.write(msg[0]+"-"+fileDir+"-"+Integer.toString((int) f.length()));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        else if (text.equals("l")){

            try {
                connection.write("logout");
            } catch (IOException e) {
                e.printStackTrace();
            }
            active=false;
            System.exit(0);
        }

        
        }
        
    }
    
    public void sendFrame(Frame f,int kind,int seq,int ack,String msg){
     
            f.setFrameKind(kind);
            f.setSeqNo(seq);
            f.setAckNo(ack);
            f.setPayLoad(msg.getBytes()) ; 
            f.setCheckSum();
     
     
 }
    public int random(int min,int max){
        int randomNum = ThreadLocalRandom.current().nextInt(min, max + 1);

        return randomNum;
    }
    
}
