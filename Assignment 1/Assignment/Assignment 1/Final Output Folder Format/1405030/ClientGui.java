package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ClientGui {
    public JPanel panel;
    public JPanel panel1;
    public JTextField id;
    public JButton connectServerButton;
    public JButton sendFileButton;
    public JButton receiveFileButton;
    public JTextField receiverId;
    public JButton sendButton;
    public JProgressBar progressBar;
    public JTextArea console;
    public JTextArea status;
    public JButton acceptButton;
    public JButton declineButton;


    boolean flage1;
    String  text1;

    boolean flage2;
    String  text2;

    public ClientGui() {


        /*connectServerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                System.out.println("clicked");
            }
        });*/

    }

    public String clickConnectButton(){
        flage1=true;

        while (flage1) {

            connectServerButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    //System.out.println("clicked");
                    text1 = id.getText().trim();
                    flage1=false;
                }
            });


        }
        return text1;
    }




    public String clickSend(){
        flage2=true;
        while (flage2) {

            sendButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    //System.out.println("clicked");
                    text2 = receiverId.getText().trim();
                    flage2=false;

                    //sendButton.setEnabled(false);
                    //receiverId.setEnabled(false);

                    console.setText("ReceiverId: "+text2);
                    //console.setText(console.getText()+"\nReceiver Id: "+text2);
                }
            });


        }
        return text2;
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}
