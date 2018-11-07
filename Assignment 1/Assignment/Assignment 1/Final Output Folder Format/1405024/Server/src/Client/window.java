package Client;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * Created by Ashiqur Rahman on 10/22/2017.
 */
public class window {
    public static int state=-1;
    public static String msg="--";
    public static  File file;
    public static JFrame frame;
    public JPanel pane;
    private JButton IDButton;
    private JTextArea ID;
    private JButton chooseFileButton;
    private JTextField receiver;
    private JButton senderButton;
    private JButton frameLossButton;
    private JButton biterrorButton;
    private JTextField textField2;
    private JTextField textField3;
    private JButton yesButton;
    private JButton acceptButton;
    private JButton declineButton;
    private JButton Nobutton;
    public JLabel mylabel;
    public JLabel Status;
    public  JLabel Filename;

    public window() {
        IDButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
              msg= ID.getText();
            }
        });
        chooseFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
                int result = fileChooser.showOpenDialog(frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    file= fileChooser.getSelectedFile();
                    System.out.println("file is"+ file.getName());
                    state=3;
                    msg="-";

        }
    }
        });
        Nobutton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                msg="no";

            }
        });
        yesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                msg="yes";

            }
        });
        senderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                msg=receiver.getText();
            }
        });
        frameLossButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                msg=textField2.getText();
            }
        });
        biterrorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                msg=textField3.getText();
            }
        });
        acceptButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                msg="y";
            }
        });
        declineButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                msg="n";
            }
        });
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
    void event()
    {
        return ;
    }
    public static void main(String[] args) {
        frame = new JFrame("Networking");
        frame.setContentPane(new window().pane);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        while(state==-1 ) ;

        System.out.println("id is "+msg);
        msg="--";
        while(msg.equals("--")) ;

        System.out.println("want to send file"+ msg);
        msg="--";
        while(msg.equals("--")) ;
        System.out.println("file is"+ file.getName());



    }
}
