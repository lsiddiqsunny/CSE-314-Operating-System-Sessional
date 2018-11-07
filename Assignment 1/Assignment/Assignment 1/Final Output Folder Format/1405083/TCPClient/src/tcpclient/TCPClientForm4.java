package tcpclient;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TCPClientForm4 {
    public JPanel panel1;
    public JProgressBar progressBar1;
    public JButton cancelButton;
    public JFrame jF;
    public TCPClientForm4(String text) {
        jF = new JFrame(text);
        jF.setContentPane(panel1);
        jF.pack();
        jF.setVisible(true);
    }
    public void close(){
        jF.dispose();//return 0;
    }
    public void update(){
        progressBar1.update(progressBar1.getGraphics());
    }
}
