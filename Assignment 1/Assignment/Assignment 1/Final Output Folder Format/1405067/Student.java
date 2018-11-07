
package file.transmission;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;


public class Student{
    int chunkSize = 0;
    int studentId=0;
    Socket socket=null;
    int last_ack;
    int numOfChunks;
    boolean generate_error=false;
    ObjectInputStream is = null;
    ObjectOutputStream os = null;
    Vector<String> history=null;
    JTextArea payload;
    JLabel labelSending;
    JLabel acknowledgeField;
    
    public Student()
    {
        try {
            socket = new Socket("localHost", 3333);
            System.out.println("Connected");
            
            os = new ObjectOutputStream(socket.getOutputStream());
            is = new ObjectInputStream(socket.getInputStream());
            
            String id=JOptionPane.showInputDialog(null,"Please give your student id ....");
            if(id==null)System.exit(0);
            studentId=Integer.parseInt(id);
            os.writeObject(studentId);
            int code=(int) is.readObject();
            if(code == -1)
            {
                JOptionPane.showMessageDialog(null,"This Student ID is already logged in");
                System.exit(0);
            }
            else if(code == 1)
            {
                downloadRequest();
            }
            createGui();
            while(socket.isConnected())
            {
                Object obj = checkReceiveInterrupt();
                
                boolean b=(obj instanceof Boolean)?(boolean)obj : false;
                

                 
                if(b)
                {
                    chunkSize = (int)checkReceiveInterrupt();
                    int fileId = (int)checkReceiveInterrupt();
                    JOptionPane.showMessageDialog(null,"File Id : "+fileId+"\nMaximum file chunk size :"+chunkSize+"Bytes");
                    uploadFile();
                }
                else
                {
                    String msg = (String)checkReceiveInterrupt();
                    JOptionPane.showMessageDialog(null,msg);
                }
            }
            
        } catch (IOException ex) {
            System.out.println("IO Exception");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Student.class.getName()).log(Level.SEVERE, null, ex);
        }

        
    }
    private Object checkReceiveInterrupt() throws IOException, ClassNotFoundException
    {
        Object obj = is.readObject();
        if(obj instanceof String && ((String)obj).equalsIgnoreCase("incoming file request interrupt"))
        {
            downloadRequest();
            obj = checkReceiveInterrupt();
        }            
        
        return obj;
    }

    private void createGui() {
        JFrame frame = new JFrame("Student Id : "+studentId);
        JLabel labelFileName = new JLabel("File Name : ");
        JLabel labelFileSize = new JLabel("File Size : ");
        JLabel labelReceiver = new JLabel("Receiver Id : ");
        JLabel labelAcknowledge = new JLabel("AcknowledgeMent : ");
        labelSending = new JLabel("");
        JTextField fileSizeField = new JTextField();
        JTextField fileNameField = new JTextField();
        JTextField receiverField = new JTextField();
        acknowledgeField = new JLabel();
        JButton sendButton = new JButton(" Send ");
        JButton logOutButton = new JButton(" Log out ");
        JButton errorButton = new JButton(" Generate Error");
        JButton sendFrameButton = new JButton(" Send Frame");
        payload=new JTextArea("");
        payload.setBounds(10,30, 200,200); 
        
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Vector<Object> vec= new Vector<Object>(3);
                    vec.addElement(fileNameField.getText());
                    vec.addElement(Integer.parseInt(fileSizeField.getText().trim()));
                    vec.addElement(Integer.parseInt(receiverField.getText().trim()));
                    os.writeObject(new String("send request"));
                    os.writeObject(vec);
                    fileNameField.setText("");
                    fileSizeField.setText("");
                    receiverField.setText("");
                } catch (IOException ex) {
                    System.out.println("IO Exception");
                }
            }
        });
        
        logOutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    os.writeObject(new String("log out performed"));
                    System.exit(0);
                } catch (IOException ex) {
                    Logger.getLogger(Student.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
        errorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generate_error=true;
            }
        });
        
        sendFrameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generate_error=false;
            }
        });
        
        JPanel MainPane = new JPanel();
        GridLayout layoutMain = new GridLayout(0,2);
        layoutMain.setVgap(50);
        MainPane.setLayout(layoutMain);
        
        JPanel contentPane1 = new JPanel();
        contentPane1.setBorder(new EmptyBorder(50,50,50,50));
        GridLayout layout1 = new GridLayout(0,2);
        layout1.setVgap(50);
        contentPane1.setLayout(layout1);
        contentPane1.add(labelFileName);
        contentPane1.add(fileNameField);
        contentPane1.add(labelFileSize);
        contentPane1.add(fileSizeField);
        contentPane1.add(labelReceiver);
        contentPane1.add(receiverField);
        contentPane1.add(sendButton);
        contentPane1.add(logOutButton);
        
        JPanel contentPane2 = new JPanel();
        contentPane2.setBorder(new EmptyBorder(50,50,50,50));
        BoxLayout blayout = new BoxLayout (contentPane2, BoxLayout.Y_AXIS);
        contentPane2.setLayout (blayout); 
        JPanel acknowledgePane = new JPanel();
        acknowledgePane.setBorder(new EmptyBorder(50,50,50,50));
        acknowledgePane.setLayout(layout1);
        acknowledgePane.add(labelAcknowledge);
        acknowledgePane.add(acknowledgeField);
        contentPane2.add(acknowledgePane);
        contentPane2.add(labelSending);
        contentPane2.add(payload);
        JPanel buttonPane = new JPanel();
        buttonPane.setBorder(new EmptyBorder(50,50,50,50));
        buttonPane.setLayout(layout1);
        buttonPane.add(errorButton);
        buttonPane.add(sendFrameButton);
        contentPane2.add(buttonPane);
        
        MainPane.add(contentPane1);
        MainPane.add(contentPane2);
        frame.getContentPane().add(MainPane);
        frame.setPreferredSize(new Dimension(1000,800));
        frame.addWindowListener(new WindowAdapter(){
                public void windowClosing(WindowEvent e){
                    int i=JOptionPane.showConfirmDialog(null, "Are you sure to log out ?");
                    if(i==0)
                        try {
                            os.writeObject(new String("log out performed"));
                            Boolean b = (Boolean) is.readObject();
                    } catch (IOException ex) {
                        Logger.getLogger(Student.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (ClassNotFoundException ex) {
                        Logger.getLogger(Student.class.getName()).log(Level.SEVERE, null, ex);
                    }
                        System.exit(0);
                }
            });


        frame.setVisible(true);
        frame.pack();
        frame.show();
        
    }
    
    boolean uploadFile()
    {
        String filePath = JOptionPane.showInputDialog(null,"Please give the file path ....");
        DLL d = new DLL();
        try {
            File f=new File(filePath);
            int fileSize = (int) f.length();
            numOfChunks = (int) Math.ceil((double)fileSize/(double)chunkSize);
            FileInputStream fis= new FileInputStream(f);
            history = new Vector<String>();
            last_ack=0;
            
            for(int i=1; i <= numOfChunks ; i++)
            {
                FileChunk fc;
                if(i*chunkSize < fileSize) fc = new FileChunk(chunkSize);
                else fc = new FileChunk(fileSize-(i-1)*chunkSize);
                fc.readChunkFromFile(fis);
                payload.setText("Please wait...\n\nReading Chunk : "+i+" from file");
                String s = d.makeFrame(d.data,(byte)i,fc.chunkByte);
                history.add(s);
            }
            fis.close();
            
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                                    
                        long time_limit = System.currentTimeMillis()+5000;
                        for(int i=1; i <= numOfChunks ; i++)
                        {
                            if(System.currentTimeMillis()>time_limit)
                            {
                                time_limit = System.currentTimeMillis()+5000;
                                System.out.println("Time out");
                                if(last_ack != numOfChunks)i=last_ack+1;
                            }
                            String s=history.get(i-1);
                            payload.setText(s);
                            labelSending.setText("Sending  chunk   : "+i+"  out of  "+numOfChunks);
                            Thread.sleep(500);
                            while(generate_error)
                            {
                                System.out.println("generating error");   
                            }
                            s=payload.getText();
                            os.writeObject(s);
                            System.out.println(i+" Chunks sent successfully out of "+numOfChunks);
                            if( i == numOfChunks )
                            {
                                i=last_ack;
                                Thread.sleep(500);
                            }
                        }
                        String complete=(String)checkReceiveInterrupt();
                        JOptionPane.showMessageDialog(null,complete);

                    } catch (IOException ex) {
                        Logger.getLogger(Student.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (ClassNotFoundException ex) {
                        Logger.getLogger(Student.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Student.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
            t.join();
            t.start();

            while(last_ack < numOfChunks)
            {
                String acknowledge=(String)checkReceiveInterrupt();
                            
                if(d.checkError(acknowledge))
                {
                    if(!d.isData())
                    {
                        last_ack=d.getSeqNo();
                        acknowledgeField.setText("   "+last_ack);
                        System.out.println("acknowledgement   "+last_ack);
                    }
                }
            }
            
                    
            
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Student.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (IOException ex) {
            Logger.getLogger(Student.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (InterruptedException ex) {
            Logger.getLogger(Student.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Student.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }
    
    public void downloadRequest() throws IOException, ClassNotFoundException
    {
        JOptionPane.showMessageDialog(null,"You have incoming file request");
        os.writeObject(new String("download request"));

        int s = (int)checkReceiveInterrupt();
            for(int i=0; i<s ; i++)
            {
                String fileName = (String)checkReceiveInterrupt();
                int studentId = (int)checkReceiveInterrupt();
                int fileSize = (int)checkReceiveInterrupt();
                int response = JOptionPane.showConfirmDialog(null,"Do you receive file : "+fileName+"("+fileSize/1000+"Kb) from "+studentId+" ?", "Confirm",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                
                if(response == JOptionPane.OK_OPTION)
                {
                    os.writeObject(new Boolean(true));System.out.println(studentId+" says Yes");
                    MyChunkedFile f=(MyChunkedFile)checkReceiveInterrupt();
                    if(f.writeFile(fileName))JOptionPane.showMessageDialog(null,"file : "+fileName+" downloaded successfully");
                    else JOptionPane.showMessageDialog(null,"file : "+fileName+" download Failed");
                }
                else
                {
                    os.writeObject(new Boolean(false)); 
                }
            }
    }
    

    
    public static void main(String args[])
    {
        Student student = new Student();
    }
    
}
