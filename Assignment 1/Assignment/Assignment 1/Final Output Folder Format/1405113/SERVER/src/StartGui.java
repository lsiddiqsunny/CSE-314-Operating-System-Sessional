import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class StartGui implements ActionListener{
    private JPanel panelMain;
    private JButton receiveButton;
    private JTextArea textArea1;
    private JTextField textField1;
    private JButton sendButton;

    public StartGui(){
        sendButton.addActionListener(this);
        receiveButton.addActionListener(this);
    }

    public  void actionPerformed(ActionEvent e){
        if(e.getSource()==sendButton){

        }

        if(e.getSource()==receiveButton){

        }
    }

    //public void static main()

}
