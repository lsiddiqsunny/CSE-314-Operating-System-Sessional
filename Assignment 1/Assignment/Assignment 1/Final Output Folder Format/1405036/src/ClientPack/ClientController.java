/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ClientPack;

import DataPack.ConnectionUtilities;
import DataPack.DataClass;
import DataPack.FrameCreator;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
//import util.Reader;
//import util.Writer;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.TextArea;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

/**
 * FXML Controller class
 *
 * @author
 */
public class ClientController implements Initializable{

    
    
    Socket client;
    //BufferedReader reader;
    //PrintWriter writer;
    Scanner sc;
    
    @FXML
    TextArea textLogin;
    @FXML
    Button buttonLogin;
    @FXML
    TextArea textRoll;
    @FXML
    TextArea textSend;
    @FXML
    Button buttonBrowse;
    @FXML
    Button buttonSend;
    @FXML
    TextArea textShow;
    @FXML
    Button buttonShow;
    
    @FXML
    TextArea textFileName;
    @FXML
    Button buttonAccept;
    @FXML
    Button buttonReject;
    
    @FXML
    TextArea textPrev;
    @FXML
    TextArea textAfter;
    @FXML
    TextArea textServer;
    @FXML
    Button buttonError;
    @FXML
    Button buttonTimeOut;
    
    FileInputStream fin;
    FileOutputStream fout;
    
    ConnectionUtilities connectionSend;
    ConnectionUtilities connectionReceive;
    
    String contact=null;
    String username;
    
    //###############################
    File file;//=new File("RoutineJanuary17LT.pdf");//RoutineJanuary17LT.pdf
    //BufferedInputStream bin;
    String str = null;////////////////////
    //int chunkSize=-1;

    DataClass dc;
    FrameCreator fc;
    
    int amount;
    
    boolean isError=false;
    boolean isTimeout=false;
    boolean elapsedTime=false;
    Timer timer;
    
    public void go() 
    {
        connectionSend=new ConnectionUtilities("127.0.0.1",5000);
        
        System.out.println("Network Writing Established");
        
        connectionReceive = new ConnectionUtilities("127.0.0.1", 5000);

        System.out.println("Network Reading Established");
        
        logIn();

        //Thread t = new Thread(new IncomingReader(connectionReceive));
       // t.start();
        //while (true);

    }

    private void logIn()
    {
            connectionSend.roll = LogInController.username;
            connectionReceive.roll=LogInController.username;
            DataClass dc = new DataClass();
            dc.setData(LogInController.username);
            connectionReceive.write(dc);

            dc = new DataClass();
            dc = (DataClass) connectionReceive.read();
            if (dc.isRead)
            {
                System.out.println("Logged in Successfully");
                
                Thread t = new Thread(new IncomingReader(connectionReceive));
                t.start();
                System.out.println("Incoming Thread starts!");
            } 
            else 
            {
                System.out.println("Multiple Log in found!\n Connection is closed");
                try
                {
                    connectionReceive.getSocket().close();
                    connectionSend.getSocket().close();
                    System.exit(0);
                } 
                catch (IOException ex) 
                {
                    Logger.getLogger(LogInController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            //textLogin.setText("");
    }

    
    public void talkServer(ConnectionUtilities connection) 
    {
        try 
        {
            String message;
            dc = new DataClass();
            dc = (DataClass) connection.read();
            //while((dc=(DataClass)connection.read())!=null)
            //{
            //str=message;
            //System.out.println("" + dc);//$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
            str = dc.command;
            // System.out.println("" + str);//$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$

            // System.out.println("reply: " + str);//$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
        } 
        catch (Exception ex) 
        {
            System.out.println("Error Client talkSender(): " + ex);
        }
    }
    
    private void updateStatus(String message) 
    {
        if (Platform.isFxApplicationThread())
        {
            textSend.appendText(message);
        } 
        else 
        {
            Platform.runLater(() -> textSend.appendText(message));
        }
    }
    
    @FXML
    private void browse(ActionEvent event) throws IOException, Exception 
    {
        try
        {
            FileChooser fChooser=new FileChooser();
            fChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
            File choosenFile=fChooser.showOpenDialog(null);
            if(choosenFile!=null)
            {
                String fName=choosenFile.getAbsolutePath();
                textSend.setText(fName);
            }
        }
        catch(Exception ex)
        {
            System.out.println("Error Method Browse:");
            ex.printStackTrace();
        }
        
    }


     @FXML
    private void send(ActionEvent event) throws IOException, Exception 
    {
        try
        {
            if(connectionSend==null)
            {
                System.out.println("CLOSED");
            }
                      
            OutGoing writerThread=new OutGoing(connectionSend); 
            writerThread.start();
            
            
            
        }
        catch(Exception ex)
        {
            System.out.println("Error Method Send:");
            ex.printStackTrace();
        }
        
    }
     @FXML
    private void request(ActionEvent event) throws IOException, Exception 
    {
        try
        {
            dc=new DataClass();
            dc.setData("receiver");
            if(event.getSource()==(Button)buttonAccept)
            {
                dc.isRead=true;
            }
            else if(event.getSource()==(Button)buttonReject)
            {
                dc.isRead=false;
            }
            connectionReceive.write(dc);
        }
        catch(Exception ex)
        {
            System.out.println("Error Method Request:");
            ex.printStackTrace();
        }
        
    }
     @FXML
    private void showError(ActionEvent event) throws IOException, Exception 
    {
        try
        {
            dc=new DataClass();
            dc.setData("receiver");
            if(event.getSource()==(Button)buttonError)
            {
                isError=true;
            }
            else if(event.getSource()==(Button)buttonTimeOut)
            {
                isTimeout=true;
            }
        }
        catch(Exception ex)
        {
            System.out.println("Error Method showError:");
            ex.printStackTrace();
        }
        
    }

    
    public class IncomingReader implements Runnable
   {
        ConnectionUtilities con;
        public IncomingReader(ConnectionUtilities connect)
        {
            con=connect;
        }

        @Override
        public void run() 
        {
            while(true)
            {
                try 
        {
            String message;
            dc = new DataClass();
            dc = (DataClass) con.read();
            //while((dc=(DataClass)connection.read())!=null)
            //{
            //str=message;
            //System.out.println("" + dc);//$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
            //System.out.println("" + str);//$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
            str = dc.command;
            

            if (str.compareTo("newFile") == 0) 
            {
                byte[] b = new byte[dc.binData.length];
                String fName=textFileName.getText();
                System.arraycopy(dc.binData, 0, b, 0, dc.binData.length);
                OutputStream of = new FileOutputStream(fName, true);
                of.write(dc.binData);
                amount+=dc.binData.length;
                System.out.println("Received: "+amount);
                of.close();
            } 
            else 
            {
                textShow.appendText(str+"\n");
                System.out.println(str);
                /*if(str.compareTo("finishReceiving")==0)
                {
                    String ss=textShow.getText();
                    String strs[]=ss.split("\n");
                    if(strs[0]!=null)
                    {
                        te
                    }
                }*/
            }
            //}
        }
        catch (Exception ex) 
        {
            System.out.println("Error Client Receiving File: " + ex);
        }
            
        }
    
    }
    }
    
    public void showTextArea(FrameCreator fc,String msg,int len)
    {
        textPrev.appendText(msg);
        textAfter.appendText(msg);
        textPrev.appendText(fc.printString(fc.frameDeStuff));
        textAfter.appendText(fc.printString(fc.frameBitStuff));
    }
    
    public class OutGoing
    {

        private int chunkNo;
        private int nextFrame;
        private int totalChunk;
        private int chunkSize = -1;
        private int fileId;
        byte[] bin;
        private int seq;
        FileInputStream fis;
        //Timer timer;

        public ConnectionUtilities connection;

        public OutGoing(ConnectionUtilities con) 
        {
            chunkNo = 0;
            connection = con;
            timer=new Timer();
        }
        public void start()
        {
                textPrev.setText("");
                textAfter.setText("");
                str = null;
                boolean isRead = false;
                    dc = new DataClass();
                    dc.setData("sender");
                    connection.write(dc);

                    System.out.println("Enter Your Id:");
                    String sender = connection.roll;
                    System.out.println("Enter Receiver's Id:");
                    String receiver = textRoll.getText();
                    System.out.println("Enter File Location:");
                    String fPath = textSend.getText();
                    textSend.setText("");
                    isRead = false;
                    file = new File(fPath);
                    int fileSize = (int) file.length();
                    System.out.println(fileSize);

                    str = null;//################################
                    dc = new DataClass();
                    dc.command = file.getName() + ":" + fileSize + ":" + sender + ":" + receiver + ":";
                    connection.write(dc);
                    //str=null;
                    int off = 0;
                    int cnt = 0;
                    try 
                    {

                        while (true && !isRead)
                        {
                            talkServer(connection);
                            //System.out.println("STR: " + str);//$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
                            if (str == null) 
                            {
                                //System.out.println("str NULL");//$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
                            }
                            if (str != null) 
                            {
                                System.out.println(str);
                                String[] strs = str.split(":");
                                if (strs[0].compareTo("sorry") == 0) 
                                {
                                    System.out.println(strs[1]);
                                    dc = new DataClass();
                                    dc.setData(connection.roll);
                                    //connection.write(dc);
                                    isRead = true;
                                    //throw new Exception();
                                    textRoll.setText("Sorry Receiver is in Offline\n");
                                    /*System.out.println("Client Closing");
                                client.close();*/
                                    //break;
                                } 
                                else if (strs[1].compareTo("ready") == 0) 
                                {
                                    // System.out.println("AAAAAAAAA " + str);//$$$$$$$$$$$$$$$$$$$$$$$
                                    chunkSize = Integer.parseInt(strs[0]);
                                    bin = new byte[chunkSize + 200];
                                    fileId = Integer.parseInt(strs[2]);
                                    totalChunk = (int) Math.ceil((double) file.length() / chunkSize);
                                    chunkNo=0;
                                    nextFrame=1;
                                    seq=0;
                                    System.out.println("Total Chunk: " + totalChunk);
                                    System.out.println("Server Not Ready Yet");
                                    System.out.println("Server is ready to receive");
                                    try 
                                    {
                                        fis = new FileInputStream(file);
                                    } 
                                    catch (FileNotFoundException ex) 
                                    {
                                        Logger.getLogger(ClientController.class.getName())
                                                .log(Level.SEVERE, null, ex);
                                    }
                                    break;
                                }
                            }
                        }
                        while (true && !isRead) 
                        {
                            //System.out.println("F I L E     I D: " + fileId);//$$$$$$$$$$$$$$$
                            cnt = fis.read(bin, 0, chunkSize);
                            if (chunkNo >= totalChunk || cnt == -1) 
                            {
                                System.out.println("Termination sending to server");
                                System.out.println("TESTING: ChunkNo: "+chunkNo+
                                        " TotalChunk: "+totalChunk+"Cnt: "+cnt);
                                fis.close();
                                dc = new DataClass();
                                fc=new FrameCreator();
                                byte[] end=new byte[1];
                                end[0]=0;
                                dc.setData(fileId, fc.sendPacket(end,0,0,3,isError));
                                showTextArea(fc,new String("EOF SENT\n"), bin.length);
                                connection.write(dc);
                                break;
                            }
                            else 
                            {
                                // System.out.println("CNT: " + cnt);//$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$

                                dc = new DataClass();
                                byte[] b = new byte[cnt];
                                System.arraycopy(bin, 0, b, 0, cnt);
                                seq=0;
                                fc=new FrameCreator();
                                dc.isTimeOut=isTimeout;
                                dc.setData(fileId, fc.sendPacket(b,nextFrame,0,0,isError));////////
                                //System.out.println("binData sending: "+new String(dc.binData));
                                //////////////////////////
                                chunkNo++;
                                showTextArea(fc,new String("Frame No: " + nextFrame + "\n"), b.length);
                                //System.out.println("PayLOad: "+FrameCreator.payLoad);
                                
                                connection.write(dc);
                                
                                isTimeout=false;
                                isError=false;
                                off += cnt;
                                System.out.println("CHUNK " + chunkNo);
                                elapsedTime=false;
                                timer = new Timer();
                                timer.schedule(new Waiting(fileId,nextFrame,cnt,bin,connection),10000);
                                /*if(off>=fileSize)
                            {
                                break;
                            }*/
                                //System.out.println(off);
                            }
                            while (true && !isRead && !elapsedTime) 
                            {
                                dc=new DataClass();
                                dc=(DataClass) connection.read();
                                //str=receiveFromServer();
                                //System.out.println("Chunk no:" + chunkNo);//$$$$$$$$$$$$$$$$$$$$$
                                //textServer.setText("str");
                                String removeString=fc.removeHeadTail(new String(dc.binData));
                                String deStuff=fc.deStuffing(removeString);
                                int seq=fc.getSequence(deStuff);
                                int ack=fc.getAck(deStuff);
                                //System.out.println("ACK IN LOOP: "+ack);
                                if (ack == 1) 
                                {
                                    System.out.println("Server received Frame:" + nextFrame);
                                    timer.cancel();
                                    nextFrame++;
                                    break;
                                }
                                
                            }
                            
                        }
                        if (!isRead) 
                        {
                            talkServer(connection);
                            if (str.compareTo("receiveComplete") == 0) 
                            {
                                System.out.println("Server received File successfully");
                            } 
                            else 
                            {
                                System.out.println("Server receiving File Failed");
                            }
                        }

                    } 
                    catch (Exception ex) 
                    {
                        System.out.println("Error Client Writing File: " + ex);
                    }
                }
            }
        

    
    @Override
    public void initialize(URL url, ResourceBundle rb) 
    {
        go();
           
    }
    
    class Waiting extends TimerTask {
        byte[] bin;
        int seq;
        int cnt;
        int fileId;
        ConnectionUtilities connection;
        
        Waiting(int fId,int s,int c,byte[] bin,ConnectionUtilities con)
        {
            fileId=fId;
            seq=s;
            cnt=c;
            this.bin=bin;
            connection=con;
        }

        public void run()
        {
            try 
            {
                    dc = new DataClass();
                    byte[] b = new byte[cnt];
                    System.out.println("resending From Waiting Class: ");
                    System.arraycopy(bin, 0, b, 0, cnt);
                    //++seq;
                    fc=new FrameCreator();
                    dc.setData(fileId, fc.sendPacket(b,seq,0,0,isError));
                    //textPrev.appendText(FrameCreator.payLoad+"\n");
                    //System.out.println("PayLOad: "+FrameCreator.payLoad);
                    showTextArea(fc,new String("Resending for No Acknowledgement\n"), 
                                                b.length);
                    connection.write(dc);
                    //timer.cancel();
                    timer=new Timer();
                    timer.schedule(new Waiting(fileId,seq,cnt,bin,connection),10000);

            } 
            catch (Exception ex) 
            {
                System.out.println("Error Waiting Class: " + ex);
                ex.printStackTrace();
            }
        }
    }
    
}