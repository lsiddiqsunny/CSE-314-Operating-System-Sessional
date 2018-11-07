import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SendFileGui extends JFrame implements ActionListener{

    public JLabel timeout_label;
    public JTextArea timeout_textarea;
    public JLabel frame_before_stuffing_label;
    public JTextArea frame_before_stuffing_textarea;
    public JScrollPane frame_before_stuffing_scrollpane;
    public JLabel frame_after_stuffing_label;
    public JTextArea frame_after_stuffing_textarea;
    public JScrollPane frame_after_stuffing_scrollpane;
    public JButton  enter_error_button;
    public JTextField frame_error_textfield;
    public JScrollPane frame_error_scrollpane;
    public JButton send_error_button;
    public int enter_error_button_clicked;
    public int send_error_button_clicked;
    public JTextArea log_msg_textarea;
    public JScrollPane log_msg_scrollpane;


    public SendFileGui(){
        timeout_label = new JLabel("Timeout");
        timeout_textarea = new JTextArea(1,10);
        frame_before_stuffing_label = new JLabel("Frame Before Bits Stuffing:");
        frame_before_stuffing_textarea = new JTextArea(10,60);
        frame_before_stuffing_scrollpane = new JScrollPane(frame_before_stuffing_textarea);
        frame_after_stuffing_label = new JLabel("Frame After Bits Stuffing:");
        frame_after_stuffing_textarea = new JTextArea(10,60);
        frame_after_stuffing_scrollpane = new JScrollPane(frame_after_stuffing_textarea);
        enter_error_button = new JButton("Enter Error");
        enter_error_button.addActionListener(this);
        frame_error_textfield = new JTextField(60);
        frame_error_scrollpane = new JScrollPane(frame_error_textfield);
        send_error_button = new JButton("Send Error");
        send_error_button.addActionListener(this);
        send_error_button.setEnabled(false);
        log_msg_textarea = new JTextArea(35,30);
        log_msg_scrollpane = new JScrollPane(log_msg_textarea);

        enter_error_button_clicked = 0;
        send_error_button_clicked = 0;

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);

        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createSequentialGroup()
                    .addGroup(layout.createParallelGroup()
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(timeout_label)
                                .addComponent(timeout_textarea))
                            .addComponent(frame_before_stuffing_label)
                            .addComponent(frame_before_stuffing_scrollpane)
                            .addComponent(frame_after_stuffing_label)
                            .addComponent(frame_after_stuffing_scrollpane)
                            .addComponent(enter_error_button)
                            .addComponent(frame_error_scrollpane)
                            .addComponent(send_error_button))
                    .addComponent(log_msg_scrollpane));

        layout.setVerticalGroup(layout.createParallelGroup()
                    .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup()
                                    .addComponent(timeout_label)
                                    .addComponent(timeout_textarea))
                            .addComponent(frame_before_stuffing_label)
                            .addComponent(frame_before_stuffing_scrollpane)
                            .addComponent(frame_after_stuffing_label)
                            .addComponent(frame_after_stuffing_scrollpane)
                            .addComponent(enter_error_button)
                            .addComponent(frame_error_scrollpane)
                            .addComponent(send_error_button))
                    .addComponent(log_msg_scrollpane));


        pack();
        setVisible(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
            Object button = e.getSource();

            if(button == enter_error_button){
                frame_error_textfield.setEditable(true);
                enter_error_button_clicked = 1;
                System.out.println("enter error button clicked from gui:"+enter_error_button_clicked);

            }
            if(button == send_error_button){
                send_error_button_clicked = 1;
                System.out.println("send error button clicked from gui:"+send_error_button_clicked);

            }
    }
}
