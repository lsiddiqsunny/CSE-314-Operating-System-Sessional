package Client;

import util.NetworkUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.util.Scanner;

/**
 * Created by sadiq on 9/19/17.
 */
public class Client {

    public static ClientGui cgui;



    static boolean  LogIn(String Id,NetworkUtil nc){

        nc.write(Id);
        String result=(String) nc.read();
        System.out.println(result);

        if(result.equals("Connected")){
            return true;
        }

        return false;
    }







    public static void setNimbusLookAndFeel(){
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // If Nimbus is not available, fall back to cross-platform
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ex) {
                // not worth my time
            }
        }
    }


     static void createUIComponent(){
        JFrame frame = new JFrame("Client");
        cgui = new ClientGui();
        frame.setContentPane(cgui.panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setSize(550, 300);
    }


    public static void main(String args[]) {

            setNimbusLookAndFeel();
            createUIComponent();


            cgui.id.setText("Type Your Id Here");
            String id;
            NetworkUtil nc;
            //Scanner scanner = new Scanner(System.in);
            //System.out.printf("Id: ");
            //id=scanner.nextLine();


            boolean con=false;

            while (!con) {

                id = cgui.clickConnectButton();


                try {

                    String hostAdress = "127.0.0.1";
                    //String hostAdress="172.16.193.21";
                    int serverPort = 33333;
                    nc = new NetworkUtil(hostAdress, serverPort);


                    //[Try to LogIn]

                    boolean LogInResult;


                    LogInResult = LogIn(id, nc);

                    if (!LogInResult) {
                        cgui.console.setText("Already Connected");
                        nc.closeConnection();

                    } else {
                        con = true;
                        cgui.connectServerButton.setText("Disconnect");
                        cgui.console.setText("Connection Established Successfully");
                        new TransmissionStateClient(nc);
                        con=true;
                    }


                } catch (Exception e) {
                    System.out.println("In main Client: " + e);
                    //return;
                }
            }
    }
}
