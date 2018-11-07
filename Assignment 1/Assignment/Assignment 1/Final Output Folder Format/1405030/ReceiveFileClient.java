package Client;

import Model.FileItem;
import Model.TransmittedFile;
import util.NetworkUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Scanner;

import static Client.Client.cgui;

public class ReceiveFileClient implements Runnable {

    NetworkUtil nc;
    Thread thread;
    volatile boolean shutdown=false;
    String fileId;
    File fileToReceive;
    int totalLenght;
    public String response;


    ReceiveFileClient(NetworkUtil nc){
        this.nc=nc;
        this.thread=new Thread(this);
        thread.start();
    }


    @Override
    public void run() {
        try {
            while (!shutdown && !nc.isClosed){
                nc.write("r");
                nc.isReceiving=true;
                cgui.console.setText("Searching Incoming File Request");

                //Object o=nc.read();
                Object o=nc.readForReceive();

                if(o==null){
                    cgui.console.setText("No Incoming File Request Found");
                    nc.isReceiving=false;
                    Shutdown();
                    continue;
                }


                if(o instanceof TransmittedFile){
                    TransmittedFile description=(TransmittedFile)o;
                    fileId=description.getFileId();

                    String message="User "+description.getItem().getSenderId()+" wants to " +
                            "send you a file named "+description.getItem().getFileName() +"(" +
                            description.getItem().getFileLength()/1024+"kb";
                    //System.out.println(message);

                    //System.out.println("here");
                    cgui.status.setVisible(true);
                    cgui.status.setText(message);
                    cgui.console.setText("Found!!");
                    cgui.acceptButton.setVisible(true);
                    cgui.declineButton.setVisible(true);

                    //Scanner scanner=new Scanner(System.in);
                    //String response=scanner.nextLine();
                    response="unknown";

                    while (response.equals("unknown")){
                        cgui.acceptButton.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent actionEvent) {
                               response="y";
                            }
                        });

                        cgui.declineButton.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent actionEvent) {
                                response="n";
                            }
                        });
                    }

                    if(!response.equals("y")){
                        nc.write(new FileItem("n",0));
                        nc.isReceiving=false;
                        Shutdown();
                        cgui.status.setVisible(false);
                        cgui.acceptButton.setVisible(false);
                        cgui.declineButton.setVisible(false);

                    }
                    else {

                        cgui.status.setVisible(false);
                        cgui.acceptButton.setVisible(false);
                        cgui.declineButton.setVisible(false);

                        JFileChooser fileChooser = new JFileChooser();
                        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                        File folderPath=null;

                        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                            //System.out.println(fileChooser.getSelectedFile());

                            folderPath=fileChooser.getSelectedFile();
                            System.out.println(folderPath.getPath());
                        }




                        fileToReceive=new File(description.getItem().getFileName());
                        totalLenght=description.getItem().getFileLength();

                        System.out.println(totalLenght);

                        System.out.println(fileToReceive.length());

                        nc.write(new FileItem("y",(int)fileToReceive.length()));

                        FileOutputStream fo=new FileOutputStream(fileToReceive);
                        fileId=description.getFileId();



                        if((int)fileToReceive.length()>0) {
                            BufferedInputStream bi = new BufferedInputStream(
                                    new FileInputStream(fileToReceive));

                            byte[] temp = new byte[(int)fileToReceive.length()];
                            bi.read(temp,0,temp.length);
                            bi.close();

                            fo.write(temp);
                        }

                        /*
                        * may also be done by using offset field of fo.write();
                        * */


                        cgui.progressBar.setVisible(true);
                        while (fileToReceive.length()<totalLenght && !nc.isClosed){
                            Object data=nc.read();

                            //System.out.println(data);

                            if(data!=null && data instanceof TransmittedFile){
                                TransmittedFile myBytes=(TransmittedFile)data;
                                if(myBytes.getFileId().equals(fileId)){

                                    fo.write(myBytes.getFileData());
                                    //System.out.println(fileToReceive.length()+"/"+totalLenght);
                                    cgui.progressBar.setValue((int)(fileToReceive.length()*100)/totalLenght);

                                }

                            }

                        }

                        if(totalLenght<=fileToReceive.length()){
                            //System.out.println("file Received");
                            cgui.console.setText("File Received");
                            cgui.progressBar.setVisible(false);
                            fileToReceive.renameTo(new File(folderPath.getPath()+"//"+
                            fileToReceive.getName()));
                            nc.isReceiving=false;
                            Shutdown();

                        }

                    }



                    }

                }


            }catch (Exception e){
                System.out.println(e);
        }
    }

    public void Shutdown(){
        shutdown=true;
    }
}
