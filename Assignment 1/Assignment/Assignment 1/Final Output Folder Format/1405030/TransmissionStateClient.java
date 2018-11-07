package Client;


import util.NetworkUtil;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Scanner;

import static Client.Client.cgui;

public class TransmissionStateClient {

    public NetworkUtil nc;
    public String token;


    TransmissionStateClient(NetworkUtil nc) {
        this.nc = nc;
        //this.thread=new Thread(this);
        //thread.start();


        cgui.connectServerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (cgui.connectServerButton.getText().equals("Disconnect")) {
                    cgui.connectServerButton.setText("Connect Server");
                    nc.closeConnection();
                    System.exit(0);
                }
            }
        });


        cgui.sendFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                //token="s";
                cgui.receiverId.setVisible(true);
                cgui.sendButton.setVisible(true);
                //nc.write("s");
                cgui.sendFileButton.setEnabled(false);
                //cgui.progressBar.setVisible(true);
                new SendFileClient(nc);


            }
        });

        cgui.receiveFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                //token="r";
                //cgui.progressBar.setVisible(true);
                new ReceiveFileClient(nc);

            }
        });

    }


}