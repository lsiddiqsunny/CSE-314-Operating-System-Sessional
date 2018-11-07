
package tcpclient;

import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TableSelectionModel;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Vector;

public class TCPClient extends  TCPClientForm
{
    public static Socket skt;
    public static Socket skt2;
    public static Socket skt3;
    public static Socket skt4;
    public static DataOutputStream toServer;
    public static DataOutputStream toServer2;
    public static DataOutputStream toServer3;
    public static DataOutputStream toServer4;
    public static BufferedReader inFromServer;
    public static BufferedReader inFromServer2;
    public static BufferedReader inFromServer3;
    public static BufferedReader inFromServer4;
    public static File file = null;


    public static int toBytes(String s){
        //System.out.println("toBytes received : "+s+'\n');
        int res=0;
        for(int i=0;i<s.length();i++){
            if(s.charAt(i)=='1'){
                res=res+(int)Math.pow(2,s.length()-i-1);
            }
        }
        //System.out.println("toBytes creates : "+res+'\n');
        return res;
    }
    public static String bitDestuff(String s) {
        int total1 = 0;int con1 = 0;String res = "";
        for (int i = 0; i < s.length(); i++) {
            if (con1 == 5 && s.charAt(i) == '0') {
                con1 = 0;
            } else if (s.charAt(i) == '1') {
                con1++;
                total1++;
                res = res + "1";
            } else {
                con1 = 0;
                res = res + "0";
            }


        }
        //System.out.println("bitDestuff creates : "+res+'\n');
        return res;
    }
    public static String getPayload(String s,int ft,int sq,int ak){

        //s has 0th char as frametype
        //then 4 chars as frame seqNo
        //then 4 chars as frame ackNo
        //s has last 4 chars as checkSum
        int frameType=-3,seqNo=-3,ackNo=-3,checkSum=-3;
        String res="";int no1=0;String tmp="";
        frameType = Integer.parseInt("0"+s.charAt(0));
        if(frameType != ft){
            System.out.println("Frametype mismatched for the Frame with ftype "+ft+" sqno "+sq+
                    " ackno "+ak+'\n'); return"E";
        }
        for(int i=1;i<5;i++){
            tmp = tmp+s.charAt(i);

        }
        seqNo = Integer.parseInt(tmp);
        if(sq != seqNo){
            System.out.println("SeqNo mismatched for the Frame with ftype "+ft+" sqno "+sq+
                    " ackno "+ak+'\n');return "E";
        }
        tmp = "";
        for(int i=5;i<9;i++){
            tmp = tmp+s.charAt(i);
        }

        ackNo = Integer.parseInt(tmp);
        if(ackNo != ak){
            System.out.println("AckNo mismatched for the Frame with ftype "+ft+" sqno "+sq+
                    " ackno "+ak+'\n'); return "E";
        }
        tmp="";
        for(int i=9;i<s.length()-4;i++){

            res = res+s.charAt(i);
            if(s.charAt(i)=='1'){
                no1++;
            }
        }
        for(int i=s.length()-4;i<s.length();i++){
            tmp = tmp+s.charAt(i);
        }
        checkSum = Integer.parseInt(tmp);
        if(no1!=checkSum){
            System.out.println("Checksum mismatched for the Frame with ftype "+ft+" sqno "+sq+
                    " ackno "+ak+'\n');return "E";
        }
        System.out.println("getPayload creates : "+res+'\n');
        return res;
    }
    public static int unFrame(String frame,int ft,int sq,int ak){

        String res = "";
        for(int i =8;i< frame.length()-8;i++){
            res = res+frame.charAt(i);

        }
        System.out.println("unFrame creates : "+res+'\n');
        res = getPayload(res,ft,sq,ak);
        if(res.compareTo("E")==0){
            return -1;
        }
        res=bitDestuff(res);
        int x = toBytes(res);
        return x;
    }
    public static int writeN(int n, byte[]Array, BufferedReader BR){
        try{
            String in = BR.readLine();String res="";
            int last1=0;int g=0;int bytesrec=0;

            for(int i=0;i<in.length();i++){
                res=res+in.charAt(i);
                if(in.charAt(i)=='1'){last1++;}
                else {last1=0;}
                if(last1==6){last1=0;g++;i++;res=res+'0';}
                if(g==2){
                    g=0;
                    System.out.println("Frame  : "+res+'\n');
                    int x = unFrame(res,1,n,n);
                    if(x==-1) break;
                    else {
                        Array[n]=(byte)x;
                        n++;//bytesrec++;
                        //System.out.println("bytes  rec : "+bytesrec+'\n');
                    }
                    res="";
                }
            }
            return n;
        }
        catch (Exception e){return n;}
    }

    public static String toBits(int b){
        String s = "";
        while(b!=0){
            int i = (b&1);
            if(i==1)s="1"+s;
            else s="0"+s;
            b=b>>1;
        }
        while(s.length()<8){
            s="0"+s;
        }
        //System.out.println("t0Bits creates : "+s+'\n');
        return s;
    }
    public static int getcheckSum(int data){
        String s = toBits(data);
        int no1=0;
        for(int i=0;i<s.length();i++){
            if(s.charAt(i)=='1'){
                no1++;
            }
        }
        return no1;
    }
    public static String makePayload(int data){
        String res =toBits(data);
        res = bitSuff(res);
        //System.out.println("makePayload creates : "+res+'\n');
        return res;
    }
    public static String bitSuff(String s){
        int total1 =0; int con1 = 0; String res="";
        for(int i =0;i<s.length();i++) {
            if (s.charAt(i) == '1') {
                con1++;
                total1++;
                res = res + "1";
            } else {
                con1 = 0;
                res = res + "0";
            }
            if (con1 == 5) {
                res = res + "0";
                con1 = 0;
            }

        }
        //System.out.println("bitStuff creates : "+s+'\n');
        return res;
    }
    public static String makeFrame(int frameType,int seqNo,int ackNo,String payLoad,int checkSum){
        String res="";
        String seq = Integer.toString(seqNo);
        while(seq.length()<4){ seq="0"+seq; }

        String ack = Integer.toString(ackNo);
        while(ack.length()<4){ ack="0"+ack; }

        String ckS = Integer.toString(checkSum);
        while(ckS.length()<4){ ckS="0"+ckS; }

        String fT = Integer.toString(frameType);
        res="01111110"+fT+seq+ack+payLoad+ckS+"01111110";
        //System.out.println("makeFrmae creats : "+res+'\n');
        return res;
    }
    public static int gobackN(int n, byte[]Array, DataOutputStream DOS){
        int length=Array.length; String data="";int i=0;
        for(i=0;i<5;i++){
            if(n+i<Array.length){
                int sq=i+n; int ack=i+n;
                data=data+makeFrame(1,sq,ack,makePayload((int)Array[n+i]),getcheckSum((int)Array[n+i]));
            }
            else break;
        }
        try{
            DOS.writeBytes(data+'\n');return i+n-1;
        }
        catch (Exception p){return n;}
    }
    public static void Exit(JFrame frame){
        frame.dispose();
    }
    public static void errorDialogue(String message,String title){
        JOptionPane.showMessageDialog(null,message,title,JOptionPane.INFORMATION_MESSAGE);

    }

    public static void main(String[] args) throws Exception
    {
        try{
            skt = new Socket("localhost",5678);
            skt2 = new Socket("localhost",5679);
            skt3 = new Socket("localhost",5680);
            skt4 = new Socket("localhost",5681);
            inFromServer = new BufferedReader(new InputStreamReader(skt.getInputStream()));
            inFromServer2 = new BufferedReader(new InputStreamReader(skt2.getInputStream()));
            inFromServer3 = new BufferedReader(new InputStreamReader(skt3.getInputStream()));
            inFromServer4 = new BufferedReader(new InputStreamReader(skt4.getInputStream()));
            toServer = new DataOutputStream(skt.getOutputStream());
            toServer2 = new DataOutputStream(skt2.getOutputStream());
            toServer3 = new DataOutputStream(skt3.getOutputStream());
            toServer4 = new DataOutputStream(skt4.getOutputStream());
            String s2 = inFromServer.readLine();
            System.out.println("Server says : "+s2+".\n");
        }
        catch (Exception e){
            System.out.println("Caught Exception connecting to server.\n");
        }
        JFrame jF = new JFrame("FileXfer");
        TCPClientForm tcp = new TCPClientForm();
        JPanel p = tcp.panel1;
        jF.setContentPane(p);
        jF.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jF.pack();
        jF.setVisible(true);
        tcp.exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    Exit(jF);
                }
                catch (Exception E){System.out.println("Caught Exception Exitting.\n");}
            }
        });

        JFrame jF2 = new JFrame("FileXfer");
        TCPClientForm2 tcp2 = new TCPClientForm2();
        JTabbedPane p2 = tcp2.tabbedpane;
        jF2.setContentPane(p2);
        jF2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jF2.pack();
        tcp.submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    toServer.flush();
                    String s1 = tcp.textField1.getText();
                    toServer.writeBytes(s1+'\n');
                    String s2 = inFromServer.readLine();
                    String A[]=s2.split(" ");
                    if(A[0].compareTo("Connected")==0){
                        jF.dispose();
                        jF2.setVisible(true);
                    }
                    else{
                        errorDialogue(s2,"Error");
                    }
                }
                catch (Exception E){System.out.println("Caught Excepton logging in.\n");}
            }
        });



        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("File Name");
        model.addColumn("File Size");
        model.addColumn("Sender");
        model.addColumn("File ID");
        tcp2.jScrollPane.setViewportView(tcp2.table1);
        tcp2.jScrollPaneList.setViewportView(tcp2.list1);

        tcp2.table1.setModel(model);

        TCPClientForm5 tcp5 = new TCPClientForm5();
        tcp2.table1.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent te) {

                int i = te.getFirstIndex();
                tcp5.Show();
                tcp5.acceptButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        tcp5.close();
                        Object obj1 = (Object)model.getValueAt(i,0);
                        Object obj2 = (Object) model.getValueAt(i,1);
                        Object obj3 =  (Object)model.getValueAt(i,3);
                        String fileName= obj1.toString();
                        String fileS=obj2.toString();
                        int fileSize = Integer.parseInt(fileS);
                        String fileID = obj3.toString();
                        int fileid = Integer.parseInt(fileID);
                        try{
                            toServer3.writeBytes(fileID+" "+"Y"+" "+fileName+'\n');
                            //System.out.println("Acce[tance Sent to Server\n");
                            String in =inFromServer3.readLine();

                            //System.out.println("for Acc server says: "+fileID+"\n");
                            if(in!=null && in.compareTo("")!=0 && in.compareTo("Z")!=0){
                                byte[]byteArray = new byte[fileSize];int n=0;
                                TCPClientForm4 tcp4 = new TCPClientForm4("Receiving....");
                                while(n<fileSize){
                                    int progress = n*100/fileSize;
                                    tcp4.progressBar1.setValue(progress);
                                    tcp4.progressBar1.update(tcp4.progressBar1.getGraphics());
                                    n = writeN(n,byteArray,inFromServer3);
                                    System.out.println("inside loop\n");
                                    //System.out.println("Data loss : "+loss+'\n');
                                    toServer3.writeBytes(Integer.toString(n)+'\n');
                                }
                                tcp4.close();
                                File file = new File(fileName);
                                FileOutputStream FO = new FileOutputStream(file);
                                FO.write(byteArray);FO.close();

                                errorDialogue("File Received Successfully!","Success!");
                                model.removeRow(i);
                                tcp2.table1.update(tcp2.table1.getGraphics());
                            }
                            else{
                                errorDialogue("Error 105 : Receiver has gone offline.","Error!");
                            }
                        }
                        catch (Exception er){

                        }
                    }
                });
                tcp5.declineButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {

                        tcp5.close();
                        int i = te.getFirstIndex();
                        Object obj =(Object)model.getValueAt(i,3);
                        String s = obj.toString();Object name= (Object)model.getValueAt(i,0);
                        String fileName = name.toString();
                        try{
                            toServer3.writeBytes(s+" "+"N"+" "+fileName+'\n');
                            //System.out.println("decline Sent to Server\n");
                            String in =inFromServer3.readLine();
                            if(in!=null && in.compareTo("T")==0){
                                errorDialogue("File Removed.","Success!");
                                model.removeRow(i);
                                tcp2.table1.update(tcp2.table1.getGraphics());
                            }
                            else{
                                errorDialogue("File Removed.","Success!");
                                model.removeRow(i);
                                tcp2.table1.update(tcp2.table1.getGraphics());
                            }
                        }
                        catch (Exception et){}
                    }
                });
                tcp5.cancelButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        tcp5.close();
                    }
                });
            }
        });

        Thread getOnlineList = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try{
                        String s = inFromServer2.readLine();
                        String Empty[]={""};
                        if(s!=null && s.compareTo("")!=0  && s.compareTo("Z")!=0){
                            String[]People=s.split(" ");
                            Object[]obj=(Object[])People;
                            tcp2.list1.update(tcp2.list1.getGraphics());
                            tcp2.list1.setListData(obj);

                        }
                        else{
                            Object obj[] = (Object[]) Empty;
                            tcp2.list1.update(tcp2.list1.getGraphics());
                            tcp2.list1.setListData(obj);
                        }
                        tcp2.list1.update(tcp2.list1.getGraphics());
                    }
                    catch (Exception e){
                        //e.printStackTrace();
                    }
                    try {
                        Thread.sleep(5000);
                    }
                    catch (Exception e){
                        //80e.printStackTrace();
                    }
                }
            }
        });
        getOnlineList.start();
        Thread getInbox = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try{
                        String s = inFromServer4.readLine();
                        if(s!=null && s!="" && s.compareTo("Z")!=0){
                            String[]row=s.split(" ");
                            model.addRow(row);
                            tcp2.table1.update(tcp2.table1.getGraphics());
                        }
                    }
                    catch (Exception e){
                        e.printStackTrace();break;
                    }
                }
            }
        });
        getInbox.start();

        tcp2.textField1.setEditable(false);
        tcp2.textField1.enableInputMethods(false);
        tcp2.textField1.setText("No File Selected");
        tcp.exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    Exit(jF);
                }
                catch (Exception E){System.out.println("Caught Exception Exitting.\n");}
            }
        });
        tcp2.exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    Exit(jF2);
                }
                catch (Exception E){System.out.println("Caught Exception Exitting.\n");}
            }
        });
        tcp2.browseFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    JFileChooser jFc = new JFileChooser();
                    jFc.setFileSystemView(FileSystemView.getFileSystemView());
                    jFc.showOpenDialog(null);
                    file  = jFc.getSelectedFile();
                    tcp2.textField1.setText(file.getName()+" "+file.length()+"Bytes");
                }
                catch (Exception E){System.out.println("Caught Exception Choosing File.\n");}
            }
        });
        tcp2.sendFileButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    if(file==null && tcp2.textField2.getText().compareTo("")==0){
                        JOptionPane.showMessageDialog(null,"File not selected.\nReceiver not selected.","Error",JOptionPane.INFORMATION_MESSAGE);
                    }
                    else if(file==null || tcp2.textField2.getText().compareTo("")==0){
                        JOptionPane.showMessageDialog(null,"File not selected or Receiver not selected.","Error",JOptionPane.INFORMATION_MESSAGE);
                    }
                    else{
                        String Receiver = tcp2.textField2.getText();
                        String fileName = file.getName();
                        int fileSize = (int)file.length();
                        String s1;
                        String s2;
                        int maxChunkSize;int ID;
                        try{
                            toServer.flush();
                            toServer.writeBytes("-3 "+Receiver+" "+fileName+" "+Integer.toString(fileSize)+'\n');
                            //System.out.println(Receiver+" "+fileName+" "+Integer.toString(fileSize));
                            s2 = inFromServer.readLine();
                            String A[]= s2.split(" ");
                            if(A[0].compareTo("Error")==0){
                                errorDialogue(s2,"Error");
                            }
                            else{
                                maxChunkSize = Integer.parseInt(A[0]);
                                ID = Integer.parseInt(A[1]);
                                FileInputStream F = new FileInputStream(file);
                                BufferedInputStream FI = new BufferedInputStream(F);
                                int fSize = (int)file.length();
                                byte[]completeFile = new byte[fSize];
                                FI.read(completeFile);
                                int n=0;int p=0;
                                TCPClientForm4 tcp4 = new TCPClientForm4("Sending...");
                                while(n<fSize){
                                    //skt.setSoTimeout(350);
                                    try{
                                    int sent = gobackN(n,completeFile,toServer);
                                    String s = inFromServer.readLine();
                                    int x = Integer.parseInt(s);
                                    int progress=Math.abs(x*100/fSize);
                                    System.out.println("progress: "+progress+'\n');
                                    tcp4.update();
                                    tcp4.progressBar1.setValue(progress);
                                    //Thread.sleep(100);
                                    int loss = (Math.abs(x-1-sent)/(sent-n))*100;
                                    System.out.println("Data Loss : "+loss+"%");
                                    n = x;
                                    }
                                    catch (SocketTimeoutException ste){
                                        tcp4.close();
                                        errorDialogue("Error 404 : Server timedout.","Error");
                                        p=1;
                                    }

                                }

                                if(p==0){
                                    tcp4.close();
                                    errorDialogue("File sent successfully.","Successful");
                                }
                            }
                        }
                        catch(Exception P){
                            errorDialogue("Error 501 : Stopped Sending file.","Error");
                        }

                    }
                }
                catch (Exception E){
                    errorDialogue("Error 502 : Caught Exception Sending file.","Error");
                }
            }
        });

    }
}



