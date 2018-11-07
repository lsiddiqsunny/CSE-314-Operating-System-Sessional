package Client;

import Model.FileItem;
import Model.Frame;
import Model.TransmittedFile;
import util.NetworkUtil;

import javax.swing.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;


import static Client.Client.cgui;


public class SendFileClient implements Runnable{

    private Thread thr;
    private NetworkUtil nc;
    public static int response;
    volatile boolean shutDown=false;


    public SendFileClient(NetworkUtil nc) {
        this.nc = nc;
        this.thr = new Thread(this);
        thr.start();
    }


    public void run() {
        try {


            while(!shutDown) {

                nc.isSending=true;

                String ReceiverId=cgui.clickSend();

                File file=null;
                JFileChooser fileChooser = new JFileChooser();

                if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    file = fileChooser.getSelectedFile();

                }

                if(file==null){
                    //System.out.println("You didn't select any file");
                    cgui.console.setText("You didn't select any file");
                    continue;
                }

                nc.write("s");

                FileItem fileItem=new FileItem(file.getName(),(int)file.length(),ReceiverId);
                nc.write(fileItem);

                String result=(String) nc.read();

                //System.out.println(result);
                cgui.console.append(result);

                if(result.equals("Receiver Found")){
                    Object o=nc.read();

                    if(o!=null){
                        if(o instanceof TransmittedFile){
                            TransmittedFile t1=(TransmittedFile)o;

                            FileInputStream fis= new FileInputStream(file);
                            BufferedInputStream bi=new BufferedInputStream(fis);


                            int m,n;
                            m=(int)file.length()/t1.getChunkSize()+1;
                            n=t1.getChunkSize();
                            byte [][]temp=new byte[m][n];

                            for(int i=0;i<m;i++){
                                bi.read(temp[i],0,n);
                            }

                            cgui.progressBar.setVisible(true);

                            SendData(0,m,temp);


                            cgui.console.append("\nSuccessfully Sent File");
                            nc.isSending=false;
                            ShutDown();

                        }
                    }
                }
                else {

                    nc.isSending=false;

                    ShutDown();
                }
            }
        } catch(Exception e) {
            System.out.println (e);
        }
       // nc.closeConnection();
    }

    public void ShutDown(){

        cgui.sendFileButton.setEnabled(true);
        cgui.progressBar.setVisible(false);
        cgui.sendButton.setVisible(false);
        cgui.receiverId.setVisible(false);
        shutDown=true;

    }

    public void SendData(int from,int total,byte [][]temp){
        int m=total;
        for(int i=from;i<m;i++){
            cgui.console.append("\n============================================\n" +
                    "Part "+(i+1)+"\n============================================\n");

            Frame f=new Frame(temp[i],(byte)(i+1));
            f.printBit(temp[i],cgui.console);
            cgui.console.append("\n============================================\n");
            ArrayList<Boolean>l=f.getStuffedBit();
            f.printBitFromArray(l,cgui.console);
            cgui.console.append("\n============================================\n\n\n\n");
            nc.write(l.clone());
            //cgui.console.append("\n~Part "+(i+1)+ " has been sent");
            cgui.progressBar.setValue(((i+1)*100)/m);
        }

        nc.write(true);

        //get acknowledgement
        from=0;
        ArrayList<Integer>ack = (ArrayList<Integer>) nc.read();


        System.out.println(ack);
        System.out.println("AckSize :"+ack+" | m Size :"+m);


        if(ack.size()==m){
            for(int i=0;i<ack.size()-1;i++){
                if ((ack.get(i)+1)!=ack.get(i+1)){
                    from=ack.get(i)+1;
                    break;
                }
            }
        }
        else {
            for(int i=0;i<ack.size()-1;i++){
                if ((ack.get(i)+1)!=ack.get(i+1)){
                    from=ack.get(i)+1;
                    break;
                }
            }
            if(from==0){
                from=ack.size();
            }
        }

        System.out.println("From : " +from);

        if(from!=0){
            SendData(from-1,m,temp);
        }
    }
}


