package tcpclient;

import javax.swing.*;

public class TCPClientForm5 {
    public JPanel panel1;
    public JButton acceptButton;
    public JButton declineButton;
    public JButton cancelButton;
    public JFrame jF;
    public TCPClientForm5() {
        jF = new JFrame("Choose");
        jF.setContentPane(panel1);
        jF.pack();
        jF.setVisible(false);
    }
    public void close(){
        jF.dispose();
    }
    public void Show(){
        jF.setVisible(true);
    }
}
