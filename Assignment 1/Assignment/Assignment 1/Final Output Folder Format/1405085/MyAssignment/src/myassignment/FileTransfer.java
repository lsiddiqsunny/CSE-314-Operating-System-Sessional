
package myassignment;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class FileTransfer {

    static int flag = 0;
    static Socket clientSocket;
    static ObjectInputStream ois;
    static ObjectOutputStream oos;
    static JFrame frame;
    static JPanel panel;
    static String fileName = null;
    static String receiver = null;

    public static void setReceivedView(FileInfo f) {
        JTextField field1;
        JButton okBtn, noBtn;
        JButton logOutButton;

        String str = "Would you like to receive a file from :" + f.from + " name: " + f.file;
        frame = Singleton.getInstance();
        frame.getContentPane().removeAll();
        frame.repaint();

        panel = new JPanel();
        panel.setBounds(61, 11, 81, 140);
        panel.setLayout(new GridLayout(8, 1));

        field1 = new JTextField(str);
        okBtn = new JButton("Yes");
        noBtn = new JButton("No");
        logOutButton = new JButton("Log Out");
        panel.add(field1);
        panel.add(okBtn);
        panel.add(noBtn);
        panel.add(logOutButton);

        frame.add(panel);

        okBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                Response response = new Response(1, frame.getTitle());
                try {
                    FileTransfer.oos.writeObject(response);
                } catch (IOException ex) {
                    Logger.getLogger(FileTransfer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        });
        noBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Response response = new Response(7, frame.getTitle());
                try {
                    FileTransfer.oos.writeObject(response);
                } catch (IOException ex) {
                    Logger.getLogger(FileTransfer.class.getName()).log(Level.SEVERE, null, ex);
                }
                frame.removeAll();
                frame.repaint();
                FileTransfer.setHomeView(response);

            }

        });
        logOutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                FileTransfer.flag = 1;

                Response response = new Response(2, frame.getTitle());
                try {
                    FileTransfer.oos.writeObject(response);
                } catch (IOException ex) {
                    Logger.getLogger(FileTransfer.class.getName()).log(Level.SEVERE, null, ex);
                }
                frame.removeAll();

            }

        });

    }

    public static void setHomeView(Response response) {
        JButton browseBtn;
        JButton sendButton;
        JButton receiveButton;
        JButton logOutButton;

        JTextArea ta1, ta2;
        JTextField field1;
        JTextField field2;
        if (response.response.equals("successful")) {
            frame.setTitle(response.id);
            frame.getContentPane().removeAll();
            frame.repaint();

            panel = new JPanel();
            panel.setBounds(61, 11, 81, 140);
            panel.setLayout(new GridLayout(8, 1));

            ta1 = new JTextArea("File Location");
            field1 = new JTextField(20);
            ta2 = new JTextArea("Recipient id:");
            field2 = new JTextField(20);

            browseBtn = new JButton("Browse");
            sendButton = new JButton("Send");
            receiveButton = new JButton("Receive");
            logOutButton = new JButton("Log Out");

            panel.add(ta1);
            panel.add(field1);
            panel.add(browseBtn);
            panel.add(ta2);
            panel.add(field2);
            panel.add(sendButton);
            panel.add(receiveButton);
            panel.add(logOutButton);
            frame.add(panel);

            browseBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JFileChooser fc = new JFileChooser();
                    JButton openBtn = new JButton();
                    fc.setCurrentDirectory(new File("/home/antu"));
                    fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    if (fc.showOpenDialog(openBtn) == JFileChooser.APPROVE_OPTION) {
                    }
                    fileName = fc.getSelectedFile().getAbsolutePath();
                    field1.setText(fileName);

                }

            });

            sendButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    receiver = field2.getText();
                    FileInfo fInfo = new FileInfo(frame.getTitle(), receiver, fileName);

                    SendFile sendFile = new SendFile(clientSocket, ois, oos, fInfo);
                    Thread t = new Thread(sendFile);
                    t.start();

                }
            });

            receiveButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {

                    // FileTransfer.setReceivedView(f);                    
                    ReceiveFile rcvFile = new ReceiveFile(FileTransfer.clientSocket, frame.getTitle(), ois, oos);
                    Thread receiveThread = new Thread(rcvFile);
                    receiveThread.start();

                }

            });

            logOutButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Response response = new Response(2, frame.getTitle());
                    try {
                        FileTransfer.oos.writeObject(response);
                    } catch (IOException ex) {
                        Logger.getLogger(FileTransfer.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }

            });

        }

    }

    public static void main(String[] args) {

        JTextField txtField;
        JButton button;

        frame = Singleton.getInstance();

        panel = new JPanel();
        panel.setLayout(null);

        txtField = new JTextField();
        txtField.setBounds(50, 50, 150, 20);
        button = new JButton("Log In");

        button.setBounds(50, 100, 60, 30);
        panel.add(txtField);
        panel.add(button);
        frame.setSize(400, 400);

        frame.add(panel);
        frame.setVisible(true);

        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String text = txtField.getText();
                ClientInfo client = new ClientInfo(text);
                NetworkThread nt = new NetworkThread(client);
                Thread t = new Thread(nt);
                t.start();

            }

        });

    }
}

class NetworkThread implements Runnable {

    ClientInfo clientObj;

    public NetworkThread(ClientInfo client) {

        this.clientObj = client;

    }

    @Override
    public void run() {
        String serverName = "localhost";
        int port = 6066;
        try {
            System.out.println("Connecting to " + serverName + " on port " + port);
            FileTransfer.clientSocket = new Socket(serverName, port);

            System.out.println("Just connected to " + FileTransfer.clientSocket.getRemoteSocketAddress());
            OutputStream outToServer = FileTransfer.clientSocket.getOutputStream();
            FileTransfer.oos = new ObjectOutputStream(outToServer);
            FileTransfer.oos.writeObject(clientObj);

            InputStream inFromServer = FileTransfer.clientSocket.getInputStream();
            FileTransfer.ois = new ObjectInputStream(inFromServer);

            Response r = (Response) FileTransfer.ois.readObject();

            if (r.response.equals("successful")) {
                System.out.println("LOG IN Successful!");
                FileTransfer.setHomeView(r);

            } else if (r.response.equals("logged_in")) {
                System.out.println("Already Logged In!");

            } else {
                System.out.println("LOG IN UnSuccessful!");

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
