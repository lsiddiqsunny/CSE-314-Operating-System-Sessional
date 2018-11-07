package sample;
import javafx.concurrent.Service;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;
import java.util.StringTokenizer;

public class Controller {


    public TextArea textArea1;
    public Button bsend;
    public TextField textInput;
    public TextArea textArea2;
    Socket clientSocket;
    DataInputStream dis;
    DataOutputStream dos;
    Scanner scn;
    String path;
    int k;
    int o;

    public static int power(int a, int b)
    {
        int powerx = 1;
        for(int c=0;c<b;c++)
            powerx*=a;
        return powerx;
    }

    public static String changeCharInPosition(int position, char ch, String str){
        char[] charArray = str.toCharArray();
        charArray[position] = ch;
        return new String(charArray);

    }
    public static String intToString(int number) {
        StringBuilder result = new StringBuilder();

        for(int i = 7; i >= 0 ; i--) {
            int mask = 1 << i;
            result.append((number & mask) != 0 ? "1" : "0");

            //if (i % groupSize == 0)
            //result.append(" ");
        }
        //result.replace(result.length() - 1, result.length(), "");

        return result.toString();
    }
    public  static int stringToInt(String s)
    {
        int res=0;

        for (int i = s.length()-1; i>=0 ; i--) {
            if(s.charAt(i)=='1') res+=power(2,s.length()-i-1);

        }
        return res;
    }

    public static int chesksum(String st)
    {
        int i = 0;
        int count=0;
        while(i!=st.length())
        {
            if(st.charAt(i)=='1') count+=1;

            i++;
        }


        return count%256;
    }
    public static String byteToString(byte[] cmp)
    {
        String asas="";
        for (int i = 0; i <cmp.length ; i++) {
            //System.out.println((int) cmp[i]);
            asas+=intToString((int) cmp[i]);

        }
        return asas;

    }











    @FXML
    public void initialize() throws IOException {
        textArea1.setEditable(false);
        textArea1.appendText("\n");
        String id = null;
        try {
            System.out.println("Put an IP address");
            //Scanner scn=new Scanner(System.in);


            clientSocket = new Socket("localhost", 6789);
            //System.out.println(clientSocket.getLocalAddress());

            textArea1.appendText("Connected\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        scn = new Scanner(System.in);
        dis = new DataInputStream(clientSocket.getInputStream());
        dos = new DataOutputStream(clientSocket.getOutputStream());
        id=scn.nextLine();
        dos.writeUTF(id);

        int i=-1;

        while (i<0)
        {
             String reply=dis.readUTF();
             //System.out.println(reply);
            if(reply.equals("T"))
            {
                System.out.println("Put another ip address");
                id=scn.nextLine();
                dos.writeUTF(id);

            }
            else
            {
                //System.out.println("invalid id");
                break;
            }
        }



        Thread sendMessage = new Thread(new Runnable()
        {
            @Override
            public  void run() {
                while (true) {


                    //sentence = scn.nextLine();
                    String msg = scn.nextLine();


                    try {
                        // write on the output stream
                        textArea1.appendText("From Client : " + msg + "\n");
                        if(msg.charAt(0)=='&'){
                            int l=msg.length();
                            String actual=msg.substring(1,l);
                            StringTokenizer p= new StringTokenizer(actual,"#");
                             path=p.nextToken();
                            String client=p.nextToken();

                            File fl =new File(path);
                            String name=fl.getName();
                            //System.out.println(name);
                            long size= fl.length();
                            //System.out.println(fl.length());
                            String send="&"+name+"#"+size+"#"+client;
                            dos.writeUTF(send);
                        }

                        else dos.writeUTF(msg);




                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if(msg=="logout") {
                        k = 1;
                        //clear();

                        break;                    }
                }

            }
        });


        Thread readMessage = new Thread(new Runnable()
        {
            @Override
            public  void run() {

                while (true) {
                    if(k==1) { break;}
                    try {
                        // read the message sent to this client




                        String msg = dis.readUTF();

                            if(msg.charAt(0)=='&')
                            {
                                int l=msg.length();
                                String actual=msg.substring(1,l);
                                int chunkSize=Integer.parseInt(actual);
                                System.out.println(chunkSize);
                                File fi=new File(path);
                                int size= (int) fi.length();
                                System.out.println(size);
                                FileInputStream fin = new FileInputStream(fi);
                                BufferedInputStream bis=new BufferedInputStream(fin);
                                byte[] component;
                                int current=0;
                                int i=0;

                                while(current<size)
                                {
                                    i=i+1;
                                    System.out.println("file uploading... chunk no:"+i);
                                    if(size-current<chunkSize)
                                    {
                                        chunkSize=size-current;
                                    }
                                    component=new byte[chunkSize];
                                    bis.read(component,0,chunkSize);
                                    String ac_playload=byteToString(component);
                                    String ch_playload="";
                                    System.out.println("Actual PlayLoad len: "+ ac_playload.length()+"   "+ac_playload);
                                    ch_playload=ac_playload;

                                    while(true){










                                        int j = -1;
                                        while(j<0){
                                            System.out.println("There are " + ch_playload.length() + " bits , do u want to Change?[reply 'y'/'n']");
                                            Scanner scn = new Scanner(System.in);

                                            //String rply =scn.nextLine();

                                            String rply ="n";
                                            System.out.println(rply);
                                            if(rply.equals("y")){
                                                System.out.println("Put the bit no");
                                                int bit = scn.nextInt();
                                                if (ch_playload.charAt(bit) == '0') {
                                                    ch_playload = changeCharInPosition(bit, '1', ch_playload);
                                                    //System.out.println(ch_playlaod);
                                                }
                                                else {
                                                    ch_playload = changeCharInPosition(bit, '0', ch_playload);
                                                    //System.out.println(ch_playload);
                                                }
                                            }
                                            else break;
                                        }

                                        System.out.println("Changed PlayLoad len: " + ch_playload.length() + "   " + ch_playload);
                                        String head = "01111110";
                                        String seqqNo = intToString(i);
                                        int ch = chesksum(ac_playload);
                                        String chcksum = intToString(ch);
                                        System.out.println("CheckSum==> "+ch);
                                        String load = seqqNo + ch_playload + chcksum;
                                        String payload = load.replaceAll("11111", "111110");
                                        String frame = head + payload + head;

                                        if((frame.length() % 8) != 0) {
                                            for(int k = (frame.length() % 8) + 1; k <= 8; k++) {
                                                frame += "0";
                                            }
                                        }
                                        System.out.println("Frame len: " + frame.length() + "   " + frame);
                                        byte[] cmpd = new byte[frame.length()/8];
                                        int m = 0;
                                        for (int x = 0; x < frame.length()/8; x++) {
                                            int k;
                                            String s = frame.substring(m, m + 8);
                                            k = stringToInt(s);
                                            cmpd[x] = (byte) k;
                                            m += 8;
                                        }

                                        dos.write(cmpd);

                                        String ack = "";

                                        ack = dis.readUTF();
                                        System.out.println(ack);
                                        if(ack.equals("0")) break;
                                        else {
                                            continue;
                                        }

                                    }

                                    //String response=dis.readUTF();
                                    //System.out.println("From Server response: "+response);
                                    //if(response.equals("end")) break;
                                    current+=chunkSize;


                                }

                            }

                            else if(msg.equals("f"))
                            {


                                File fnew=new File("F:\\J\\test\\src\\abc5.txt");
                                FileOutputStream fos=new FileOutputStream(fnew);
                                BufferedOutputStream bos = new BufferedOutputStream(fos);
                                String s=dis.readUTF();
                                System.out.println(s);
                                o=Integer.parseInt(s);
                                byte[] p=new byte[1000];
                                int current=0;
                                //int f=0;
                                while(current<o)

                                {
                                    System.out.println("Chunk received...");
                                    int q=dis.read(p);
                                    bos.write(p,0,q);
                                    current+=q;
                                }
                                System.out.println(current);
                                bos.flush();
                                bos.close();
                                fos.close();
                                //
                                //System.out.println(q);


                            }


                            else System.out.println(msg);


                            //textArea1.appendText("Received from another Client : " + msg + "\n");


                    } catch (IOException e) {
                        e.printStackTrace();
                        k=1;
                        break;
                    }
                }
            }
        });

        sendMessage.start();

        readMessage.start();
   if(k==1)
   {

   }



    }
    private  void clear()
    {
        try
        { System.out.println("offlined");
            dis.close();
            dos.close();
            clientSocket.close();
            System.out.println("offlined");

        }
        catch(Exception e)
        {

        }
    }
    public void pl() throws IOException {
        File fnew=new File("F:\\J\\test\\src\\abc4.txt");
        FileOutputStream fos=new FileOutputStream(fnew);
        BufferedOutputStream bos = new BufferedOutputStream(fos);




        //bos.write(p,0,q);
        bos.flush();
        bos.close();
        fos.close();

    }


    public void bSendClicked(ActionEvent actionEvent) throws IOException {
        //Scanner scn = new Scanner(System.in);
         //sentence = textInput.getText();


    }



}
