
import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.TimeLimiter;
import com.google.common.util.concurrent.UncheckedTimeoutException;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.swing.*;
import java.awt.*;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;
import java.awt.event.*;

class  Client extends Frame implements ActionListener {
   public static JFrame f= new JFrame("Label Example");
    public static JTextField tf,tf2;
    public static JLabel l,l1,l2,l3;
    public static JButton b;
    Client() {

    }



    public  static void main(String argv[]) throws Exception {
        String filepath="";
        final List<Integer> holder = new LinkedList<>();
        l1=new JLabel("Your Student ID");
        l1.setBounds(50,50, 100,30);
        tf = new JTextField();
        tf.setBounds(50, 80, 150, 20);
        l2=new JLabel("Receiver Student Id");
        l2.setBounds(50,120, 100,30);
        tf2 = new JTextField();
        tf2.setBounds(50, 150, 150, 20);


        l = new JLabel();
        l.setBounds(50, 210, 250, 20);
        l3 = new JLabel();
        l3.setBounds(50, 240, 250, 20);
        b = new JButton("Send");
        b.setBounds(50, 180, 95, 30);

        f.add(tf2);
        f.add(l1);f. add(l2);


        tf.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                synchronized (holder) {
                    holder.add(Integer.parseInt(tf.getText()));
                    holder.notify();
                }

            }
        });
        tf2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                synchronized (holder) {
                    holder.add(Integer.parseInt(tf.getText()));
                    holder.notify();
                }

            }
        });
        f.add(b);
       f.add(tf);
       f.add(l);
       f. setSize(400, 400);
       f. setLayout(null);
       f. setVisible(true);
        // BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        Socket clientSocket = new Socket("localhost", 6732);
        System.out.println("Enter your student id...\n");
        Scanner sc;
      //  sc = new Scanner(System.in);
        //int sid = sc.nextInt();
        int stdid,rcvid;
        synchronized (holder) {

            // wait for input from field
            while (holder.isEmpty())
                holder.wait();

           stdid = holder.remove(0);
            System.out.println(stdid);
            //....
        }
        synchronized (holder) {

            // wait for input from field
            while (holder.isEmpty())
                holder.wait();

          rcvid = holder.remove(0);
            System.out.println(rcvid);
            //....
        }
         //int sid=Integer.parseInt(tf.getText());
         //int rcvid=Integer.parseInt(tf2.getText());
        DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
        DataInputStream dis = new DataInputStream(clientSocket.getInputStream());

        dos.write(stdid);
        dos.flush();
        int k = dis.read();
       // dis.close();

        if (k == 1) {

           // System.out.println("connection established...");
            l3.setText("connection established...");
        } else {
          //  System.out.println("login prohibited");
            l3.setText("login prohibited");
        }
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{

                    JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());

                    int returnValue = jfc.showOpenDialog(null);
                    // int returnValue = jfc.showSaveDialog(null);

                    if (returnValue == JFileChooser.APPROVE_OPTION) {
                        File selectedFile = jfc.getSelectedFile();
                        System.out.println(selectedFile.getAbsolutePath());

                        l.setText(selectedFile.getAbsolutePath());
                       // filepath=l.getText();
                        // System.out.println("done");
                        SendClass send= new SendClass(dis,dos,clientSocket,rcvid,selectedFile.getAbsolutePath());

                        send.start();

                    }

                }catch(Exception ex){System.out.println(ex);}
            }
        });


        Thread.sleep(5000);

       // ReceiveClass receive=new ReceiveClass(dis,dos,clientSocket);
      // receive.start();

      // eta send er jnne

     /*   File file = new File("C:\\Users\\samia hossain\\desktop\\idm.txt");
        dos.writeUTF(file.getName());
        dos.flush();

        System.out.println("give receiver stdid...");
        Scanner scanner=new Scanner(System.in);
        int receiverStdid =scanner.nextInt();
        dos.write(receiverStdid);
         dos.flush();

        send(file, clientSocket);
/*
        FileOutputStream fos = new FileOutputStream("C:\\Users\\samia hossain\\desktop\\fil222.txt");
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        InputStream is = clientSocket.getInputStream();
        byte[] contents = new byte[1000];
        //No of bytes read in one read() call
        int bytesRead = 0;
        int id = 1;

        while ((bytesRead = is.read(contents)) != -1) {
            bos.write(contents, 0, bytesRead);
            System.out.print("receiving file ... " + id + " complete!");
            id++;
        }


        bos.flush();
        clientSocket.close();

        System.out.println("File saved successfully!");*/

        // }
    }

    public static void send(File files, Socket socket) {


        try {
            int d = 0;
            DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            //   System.out.println(files.length());
            //write the number of files to the serve//
            System.out.println(files.length());
            dos.writeLong(files.length());
            dos.flush();

            //write file names

            //  dos.writeUTF(files.getName());
            dos.flush();


            //buffer for file writing, to declare inside or outside loop?
            int n = 0;
            byte[] buf = new byte[1000];
            //outer loop, executes one for each file


            System.out.println(files.getName());

            //create new fileinputstream for each file
            FileInputStream fis = new FileInputStream(files);
            //write file to dos
            //BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            TimeLimiter timeLimiter = new SimpleTimeLimiter();
            Callable<Boolean> callable;
            int x=1;
            while ((n = fis.read(buf)) != -1) {
                int c = 0;
                dos.write(buf, 0, n);
                dos.flush();

                try {
                    callable = new Callable<Boolean>() {
                        @Override
                        public Boolean call() throws Exception {


                            return dis.readBoolean();

                        }
                    };
                    boolean line = timeLimiter.callWithTimeout(callable, 4, TimeUnit.SECONDS, true);
                    System.out.println("line :"+ line);


                } catch (TimeoutException | UncheckedTimeoutException e) {
                    // timed out
                    System.out.println("time out");
                    socket.close();
                    break;
                } catch (Exception e) {
                    // something bad happened while reading the line
                }

                //should i close the dataoutputstream here and make a new one each time?

                //or is this good?

            }

            dos.close();


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }
}