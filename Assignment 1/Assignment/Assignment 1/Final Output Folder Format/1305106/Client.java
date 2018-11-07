import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Scanner;

import static javax.swing.JFileChooser.*;

public class Client extends Component {
    private Socket socket = null;
    private boolean isConnected = false;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream = null;

    int roll;
    File yourFolder;
    private String sourceDirectory ;
    int portNumber;

    private String destinationDirectory ;
    private int fileCount = 0;
    NetworkUtil nc;
    static long time;
    String serv;
    public Client() {

        sourceDirectory="E://codes";

    }

    public void connect() {
        //while (!isConnected) {
        String serv = JOptionPane.showInputDialog("Enter Server Address:", null);
        //serverAddress = Integer.parseInt(s1);

        //String s1 = JOptionPane.showInputDialog("Enter Port Number:", null);
        //portNumber = Integer.parseInt(s1);

        String s2 = JOptionPane.showInputDialog("Enter Reciver's Roll Number:", null);
        roll = Integer.parseInt(s2);
        portNumber =50;
        //roll =110;




        try {
            socket = new Socket(serv, portNumber);
            nc=new NetworkUtil(socket);
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
            //objectInputStream=new ObjectInputStream(socket.getInputStream());

            //nc.write(roll);
            nc.write(1);
            //System.out.println("written");




        } catch (IOException e) {
            e.printStackTrace();
        }
        //}
    }

    public void sendRoll(){


        nc.write(roll);

    }
    public void locateFiles() {
        //for(int i=0;i<a.size();i++) System.out.println(a.get(i));
        File[] files=null;
        JFileChooser fileChooser = new JFileChooser("E://Codes");
        fileChooser.setMultiSelectionEnabled(true);
        JOptionPane parent = new JOptionPane();
        int result = fileChooser.showOpenDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            // user selects a file
            files = fileChooser.getSelectedFiles();
        }

        fileCount = files.length;
        //System.out.println(fileCount+" yo length");


        if(fileCount == 0)return;



        for (int i = 0; i < fileCount; i++) {
            System.out.println("Sending " + files[i].getAbsolutePath());
            sendFile(files[i].getAbsolutePath());
            System.out.println(files[i].getAbsolutePath());
        }
    }

    public void sendFile(String fileName) {
        //System.out.println("reached here");

        //roll=110;
        nc.write(roll);
        File file = new File(fileName);
        System.out.println("files name " +file.getName());
        nc.write(file.getName());


        DataInputStream diStream = null;
        try {
            diStream = new DataInputStream(new FileInputStream(file));



            byte[] contents;
            long fileLength = file.length();
            nc.write(fileLength);


            long current = 0;

            int size =(int)nc.read();
            //System.out.println("Size " +size);
            int seq=1;

            while (current != fileLength) {


                if (fileLength - current >= size)
                    current += size;
                else {
                    size = (int) (fileLength - current);
                    current = fileLength;
                }
                contents = new byte[size];



                diStream.read(contents,0,size);
                String fn="";
                for(int i=0;i<size;i++)
                {
                    byte sum=contents[i];
                    String fromByteToString = String.format("%8s", Integer.toBinaryString(sum & 0xFF)).replace(' ', '0');
                    fn+=fromByteToString;
                }
                //System.out.println("size "+size+" string size"+fn.length());
                //byte[] hhh = new BigInteger(fn, 2).toByteArray();
                //i+=8;
                //String hello=new String(hhh);
                //System.out.println("processed string "+hh.length+" "+contents.length+" "+size);

                System.out.println("sending frame with sequence no  :"+seq);
                System.out.println("frame before stuffing   : "+fn);

                int counter=0;
                int bitfills=0;
                String fst="";
                char cc='0';
                for(int i=0;i<fn.length();i++)
                {
                    char r=fn.charAt(i);
                    if(r=='0')
                        counter=0;
                    else
                        counter++;
                    fst=fst+r;
                    if(counter==5)
                    {
                        counter=0;
                        fst+=cc;
                        bitfills++;
                    }
                }
                //System.out.println("actual string "+fn);
                //System.out.println("actual string "+fst.length());
                System.out.println("frame  after stuffing   : "+fst);
                int ex=bitfills%8;
                ex=8-ex;
                if(ex==8)
                    ex=0;
                for(int i=1;i<=ex;i++)
                    fst+=cc;
                //System.out.println("after string "+fst.length());
                byte[] bval = new BigInteger(fst, 2).toByteArray();
                int acsize=bval.length;






                byte ck=0;
                for(byte b:bval)
                {
                    ck^=b;
                }
                byte[] type={1};
                byte[] ckk={ck};
                byte[] sq={(byte) seq};
                byte[] ult=new byte[acsize+4];
                //byte[] one = getBytesForOne();
                //byte[] two = getBytesForTwo();
                //byte[] combined = new byte[one.length + two.length];

                System.arraycopy(type,0,ult,0,1);
                System.arraycopy(sq,0,ult,1,1);
                System.arraycopy(sq,0,ult,2,1);
                System.arraycopy(bval,0,ult,3,acsize);
                System.arraycopy(ckk,0,ult,3+acsize,1);
                //System.arraycopy(two,0,combined,one.length,two.length);

                //System.out.println("pack "+seq+" sent with size "+size +" and cksum "+ck);
                //System.out.println("pack "+seq+"len "+fn.length()+" sent string "+fn);

                outputStream.writeObject(ult);
                outputStream.flush();
                //seq++;
                socket.setSoTimeout(10000);   // set the timeout in millisecounds.

                while(true){        // recieve data until timeout
                    try {
                        byte[] con;
                        con=(byte[])inputStream.readObject();
                        int rs=(int) con[2];

                        if(rs<0)//wrong data recieved
                        {
                            System.out.println("Negetive ack came in reply for frame no "+seq);
                            System.out.println("resending frame no "+seq);

                            outputStream.writeObject(ult);
                            outputStream.flush();
                            continue;
                        }
                        else if(rs==seq) {
                            System.out.println("positive ack came in reply for frame no "+seq);

                            break;

                        }
                        else
                        {
                            System.out.println(" ack for previous frame came in reply for frame no "+seq);

                        }
                    }
                    catch (SocketTimeoutException e) {

                        outputStream.writeObject(ult);
                        outputStream.flush();
                        //continue;

                    }
                }

                seq++;


            }
            //System.out.println("roll "+roll);

            System.out.println("File sent successfully! for roll "+ roll);
        }
        catch (Exception e) {
            e.printStackTrace();
        }


    }

    public static void main(String[] args) {
        Client client = new Client();
        client.connect();
        //client.sendRoll();

        while(true) {
            //if(flag==1 && (tend-time)/1000>=sync)client.sync();
            System.out.println("press y if you want to upload");
            Scanner scanner = new Scanner(System.in);
            String g = scanner.nextLine();
            if (g.equals("y")) client.locateFiles();
        }
    }
}