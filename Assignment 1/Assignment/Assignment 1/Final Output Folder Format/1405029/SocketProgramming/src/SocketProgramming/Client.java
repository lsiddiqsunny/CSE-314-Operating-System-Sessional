/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SocketProgramming;




import com.sun.glass.events.ViewEvent;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javax.print.DocFlavor;
import javax.swing.JFileChooser;
import jdk.nashorn.internal.ir.ContinueNode;

/**
 *
 * @author hasan
 */
public class Client extends Application{
    
    
    private DataInputStream din;
    private DataOutputStream dout;
    private DataInputStream msgRead;
    private DataOutputStream msgWrite;
    private Socket socket; 
    private String stdtId1;
    private String stdtId2;
    private String sendFileName;
    private int isFileChoose;
    private String filePath;
    private boolean isEmpty=true;
    private TextArea notification;
    private TextField studentId;
    private TextField receiverId;
    private Server server;
    private TextField senderId;
    private int fileId;
    private int chunkSize;
    private boolean isIntroduceLostFrame;
    
    private Long fileSize;
    
    
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        
        grid.setPadding(new Insets(25, 25, 25, 25));
        
        
        grid.add(new Label("Student Id : "), 0, 0);
        

        studentId = new TextField();
        grid.add(studentId, 1, 0);
        
        Button login = new Button("Login");
        grid.add(login, 0, 1);
        
        Button logout = new Button("logout");
        grid.add(logout, 1, 1);
        
        Label intro = new Label("Check lost frame?");
        Button yes = new Button("Yes");
        Button no = new Button("No");
        
        grid.add(intro, 0, 2, 2, 1);
        grid.add(yes,0,3);
        grid.add(no,1,3);
        
        grid.add(new Label("Receiver Id : "), 0, 4);
        
            
        receiverId = new TextField();
        grid.add(receiverId, 1, 4);
        
        Button sendFile = new Button("Send File");
        grid.add(sendFile, 1, 5);
        
        
        notification = new TextArea();
        notification.setWrapText(true);
        notification.setEditable(false);
        
        grid.add(notification, 0, 6, 2, 1);
        
        grid.add(new Label("Sender Id:"),0,7);
        senderId = new TextField();
        grid.add(senderId, 1, 7);
        
        Button receiveFile = new Button("Receive File");
        grid.add(receiveFile, 1, 8);
        
        
        Scene scene = new Scene(grid,500,400);
        primaryStage.setTitle("Client");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        try {
            socket = new Socket("localhost",5000);
            
            msgRead = new DataInputStream(socket.getInputStream());
            msgWrite = new DataOutputStream(socket.getOutputStream());
            
            
                    
        } catch (Exception e) {
            //System.out.println("1");
        }
        
        yes.setOnAction(e->{
            isIntroduceLostFrame = true;
        });
        
        no.setOnAction(e->{
            isIntroduceLostFrame = false;
        });
        login.setOnAction(e -> {
            stdtId1 = studentId.getText();
            studentId.setEditable(false);
            System.out.println("stdntId1 : " +stdtId1 );
            
            try {
                msgWrite.writeUTF(stdtId1);
                //msgWrite.flush();
            } catch (IOException ex) {
                //System.out.println("2");
            }
            
            try {
                String msg = msgRead.readUTF();
                //String msg = bufferReader.readLine();
                if(msg.equals("you can't login")){
                    notification.appendText("This Id is already logged in!\n");
                    senderId.setEditable(false);
                    receiverId.setEditable(false);
                    //if(bufferReader != null) bufferReader.close();
                    //if(printWriter != null) printWriter.close();
                    socket.close();
                    //primaryStage.close();
                }
                else{
                    new Thread(new ReadingFromServer()).start();
                    //System.out.println("3");
                }
            } catch (IOException ex) {
                //System.out.println("4");
            }
            
            
            
            
        });
        
        /*logout.setOnAction(e -> {
            try {
                printWriter.println("logout");
                printWriter.flush();
                
                if(din != null) din.close();
                if(dout != null) dout.close();
                if(printWriter != null) printWriter.close();
                if(bufferReader != null) bufferReader.close();
                socket.close();
                primaryStage.close();
            } catch (IOException ex) {
                System.out.println("exception");
            }
            
        });*/
        
        
        sendFile.setOnAction(e -> {
            
            
            stdtId2 = receiverId.getText();
            if(!isEmpty){
                notification.appendText("This Id is busy with sending or receivg file!try Later!\n");
            }
            else{
                isEmpty = false;

                receiverId.setEditable(false);
                
                try {
                    
                    msgWrite.writeUTF("Initiating sending");
                    //msgWrite.flush();
                } catch (IOException ex) {
                    //System.out.println("5");
                }
                
                //System.out.println("Initiating sending");

                JFileChooser chooser = new JFileChooser();
                chooser.setAcceptAllFileFilterUsed(true);
                //FileNameExtensionFilter filter = new FileNameExtensionFilter("all files", "*");
                //chooser.setFileFilter(filter);
                int returnVal = chooser.showOpenDialog(null);
                if(returnVal == JFileChooser.APPROVE_OPTION){
                    filePath = chooser.getSelectedFile().getAbsolutePath();
                }
                //System.out.println("filePath " + filePath);
                String fileName = getFileName(filePath);
                String fileExtension = getFileExtension(filePath);

                fileSize = new File(filePath).length();

                //System.out.println("print koro " + fileName + " " + fileExtension + " " + fileSize);
                

                sendFileName = stdtId1 + "_"+stdtId2 + "_" + fileName + "." + fileExtension;

                try {
                    
                    
                    msgWrite.writeUTF(sendFileName);
                    //msgWrite.flush();
                    msgWrite.writeUTF(Long.toString(fileSize));
                    //msgWrite.flush();
                    msgWrite.writeUTF(stdtId2);
                    //msgWrite.flush();
                } catch (IOException ex) {
                    //System.out.println("6");
                }
                //System.out.println("ai porjnto ashce");

                //System.out.println("fileName :" + sendFileName);
            }
            
        });
        
        receiveFile.setOnAction(e -> {
            if(isEmpty){
                
                
                
                try {
                    
                    
                    
                    
                    msgWrite.writeUTF("Initiating receiving");
                    //msgWrite.flush();
                    msgWrite.writeUTF(senderId.getText());
                    //msgWrite.flush();
                    
                    
                    
                    
                    
                } catch (IOException ex) {
                    //System.out.println("some error");
                }
                
                
                
                
            }
            else{
                notification.appendText("This Student is busy with sending or receiving file!\nTry Later!\n");
            }
            
        });
        
        
    }
    
    @Override
    public void stop() throws IOException{
        
        
    }
    
    
    
    
    public static void main(String[] args) {
        Application.launch(args);
        
    }
    
    
    public class ReadingFromServer implements Runnable{
        private String receiveFilename;
        Long totalChunk = 0L;
        int frameNum = 1;
        byte [] buf;
        int avbits = 8;
        int curbyte = 0;
        int counter = 0;
        byte flag = 0x7E;
        boolean isFlagAdded = false;
        private byte[] bytes;
        private byte [][]frames;
        int seqNo = 0;
        int curInd = 0;
        int [] frameLen;
        int []seqNums;
        
        @Override
        public void run() {
            String msg="";
            while(true){
                try {


                        
                        //if(msgRead.readUTF() == null){
                            //System.out.println("null ha");
                        //}
                        msg = msgRead.readUTF();
                        //System.out.println("akhane dhukse");
                        //if(msg.length() <= 40 ) System.out.println("msg " + msg);
                        //else System.out.println("onno kisu ashce");

                        if(msg.equals("you can send")){
                            try {
                                //akhane kaj korbo
                                //System.out.println("inside you can send");
                                isEmpty = false;

                                fileId = Integer.parseInt(msgRead.readUTF());
                                chunkSize = Integer.parseInt(msgRead.readUTF());
                            } catch (IOException ex) {
                                //System.out.println("7");
                            }
                            buf = new byte[50];
                            bytes = new byte[10*1024];
                            frames = new byte[260][20];
                            frameLen = new int[260];
                            seqNums = new int[12];
                            

                            //bytes = new byte[10*1024];
                            totalChunk = fileSize/(1L*chunkSize*1024);
                            if(fileSize%(1L*chunkSize*1024) > 0) totalChunk++;


                            //System.out.println("totalChunk : " + totalChunk);
                            /////
                            //msgWrite.writeUTF(Long.toString(totalChunk));
                            //msgWrite.flush();
                            //////
                            File file = new File(filePath);

                            try {

                                din = new DataInputStream(new FileInputStream(file)); 

                            } catch (FileNotFoundException ex) {
                                //System.out.println("8");
                            } 
                        }
                        else if(msg.equals("you can't send")){

                            notification.appendText("The file size is too large for server to receive!!\n");
                            isEmpty = true;
                        }

                        else if(msg.equals("send a frame")){
                            //Long rest = Long.parseLong(msgRead.readUTF());
                            int first = 0,last = 0;
                            //System.out.println("in send a frame");

                            while(totalChunk > 0){
                                if(totalChunk == 1){
                                    int len = din.read(bytes);
                                    int countBytes = 0;
                                    String frame = "";
                                    int total = 0;

                                    for(int i = 0; i < len; i++){
                                        byte b = bytes[i];
                                        if(!isFlagAdded){
                                            isFlagAdded = true;
                                            first = i;
                                            //flag start
                                            for(int j = 7; j >= 0; j--){
                                                int bit = (1<<j)&(flag);
                                                if(bit > 0) bit = 1;
                                                else bit = 0;

                                                writeInBuffer(bit, false);
                                            }
                                            // whether data or ack
                                            int u = 1;
                                            for(int j = 7; j >= 0; j--){
                                                int bit = (1<<j)&(u);
                                                if(bit > 0) bit = 1;
                                                else bit = 0;

                                                writeInBuffer(bit, true);
                                            }
                                            //sequence num
                                            for(int j = 7; j >= 0; j--){
                                                int bit = (1<<j)&(seqNo);
                                                if(bit > 0) bit = 1;
                                                else bit = 0;

                                                writeInBuffer(bit, true);
                                            }
                                            
                                            //System.out.println("seq num : " + seqNo );
                                            seqNo++;
                                            if(seqNo > 255) seqNo = 0;
                                            //ack num
                                            int ack = 0;
                                            for(int j = 7; j >= 0; j--){
                                                int bit = (1<<j)&(ack);
                                                if(bit > 0) bit = 1;
                                                else bit = 0;

                                                writeInBuffer(bit, true);
                                            }
                                        }
                                        last = i;
                                        for(int j = 7; j >= 0; j--){
                                            int bit = (1<<j)&b;
                                            if(bit > 0) bit = 1;
                                            else bit = 0;

                                            writeInBuffer(bit, true);
                                        }
                                        
                                        total++;
                                        
                                        if(countBytes < 4){
                                            
                                            countBytes++;
                                        }
                                        if(total == len){
                                            
                                            int seq = seqNo-1;
                                            if(seq < 0) seq = 255;
                                            
                                            getCheckSum(first, last,1,0,seq);
                                            for(int j = 7; j >= 0; j--){
                                                int bit = (1<<j)&(flag);
                                                if(bit > 0) bit = 1;
                                                else bit = 0;

                                                writeInBuffer(bit, false);
                                            }
                                            
                                            //// printing
                                            System.out.print("frame " + frameNum + " : " );
                                            for(int p = 7;p >= 0;p--){
                                                int bit = (1<<p)&flag;
                                                if(bit > 0) bit = 1;
                                                else bit = 0;
                                                System.out.print(bit);
                                            }
                                            
                                            for(int p = first; p <= last; p++){
                                                for(int pp = 7; pp >= 0;pp--){
                                                    int bit = (1<<pp)&bytes[p];
                                                    if(bit > 0) bit = 1;
                                                    else bit = 0;
                                                    System.out.print(bit);
                                                }
                                            }
                                            
                                            for(int p = 7;p >= 0;p--){
                                                int bit = (1<<p)&flag;
                                                if(bit > 0) bit = 1;
                                                else bit = 0;
                                                System.out.print(bit);
                                            }
                                            System.out.println("");
                                            System.out.print("after stuffing : " );
                                            
                                            for(int p = 0; p < getLength(); p++){
                                                for(int pp = 7; pp >= 0;pp--){
                                                    int bit = (1<<pp)&buf[p];
                                                    if(bit > 0) bit = 1;
                                                    else bit = 0;
                                                    System.out.print(bit);
                                                }
                                            }
                                            
                                            System.out.println("");
                                            System.out.println("");
                                            
                                            //////////////////
                                            
                                            seqNums[curInd] = seq;
                                            for(int o = 0; o < getLength();o++){
                                                frames[seq][o] = buf[o];
                                            }
                                            frameLen[seq] = getLength();
                                            curInd++;
                                            
                                            if(isIntroduceLostFrame){
                                                buf[4] = 0;
                                            }
                                            
                                            msgWrite.writeUTF("sending last frame");
                                            //msgWrite.flush();
                                            msgWrite.write(buf,0,getLength());
                                            
                                            //msgWrite.flush();
                                            
                                            isFlagAdded = false;
                                            
                                            try {
                                                Thread.sleep(30);
                                            } catch (Exception e) {
                                                //System.out.println("sleep called 1");
                                            }
                                            
                                            //after sending last consecutive frame   
                                            
                                                
                                                
                                                //System.out.println("inside 1");
                                                
                                            
                                                int loop = 0;
                                                
                                                boolean isOk = false;
                                                Set<Integer> st = new LinkedHashSet<Integer>();
                                                for(int oo = 0; oo < curInd;oo++){
                                                    st.add(seqNums[oo]);
                                                }
                                                while(loop < 3){
                                                    try{

                                                        msgWrite.writeUTF("give ack");
                                                        socket.setSoTimeout(10000);
                                                        
                                                        
                                                        while(st.size() > 0){
                                                            String msg1 = msgRead.readUTF();
                                                            if( msg1.equals("acknowledged")){
                                                                //get ack num
                                                                int val = Integer.parseInt(msgRead.readUTF());
                                                                if(st.contains(val)) st.remove(val);//ok matched
                                                                
                                                                
                                                            }
                                                            
                                                        }
                                                    }catch(SocketTimeoutException e){

                                                            Iterator it = st.iterator();
                                                            
                                                            while(it.hasNext()){
                                                                int seqN = (int) it.next();
                                                                if(seqN == seq) msgWrite.writeUTF("sending last frame");
                                                                else msgWrite.writeUTF("sending usual frame");
                                                                //msgWrite.flush();
                                                                msgWrite.write(frames[seqN],0,frameLen[seqN]);
                                                            }
                                                            
                                                            

                                                        loop++;
                                                        continue;
                                                    }

                                                    isOk = true;
                                                    break;
                                                }
                                                //if isOk false do some work
                                                ////
                                                curInd = 0;
                                                st.clear();
                                            
                                            // isOk is for to check whether 8 frames are successfully received within 3 consecutive transfers
                                            //if isOk is false do some work
                                            //
                                            
                                            
                                            reset();
                                            ++frameNum;
                                            
                                        }
                                        else if(countBytes == 4){
                                            countBytes = 0;
                                            
                                            int seq = seqNo-1;
                                            if(seq < 0) seq = 255;
                                            getCheckSum(first, last,1,0,seq);
                                            for(int j = 7; j >= 0; j--){
                                                int bit = (1<<j)&(flag);
                                                if(bit > 0) bit = 1;
                                                else bit = 0;

                                                writeInBuffer(bit, false);
                                            }
                                            
                                            //// printing
                                            System.out.print("frame " + frameNum + " : " );
                                            for(int p = 7;p >= 0;p--){
                                                int bit = (1<<p)&flag;
                                                if(bit > 0) bit = 1;
                                                else bit = 0;
                                                System.out.print(bit);
                                            }
                                            
                                            for(int p = first; p <= last; p++){
                                                for(int pp = 7; pp >= 0;pp--){
                                                    int bit = (1<<pp)&bytes[p];
                                                    if(bit > 0) bit = 1;
                                                    else bit = 0;
                                                    System.out.print(bit);
                                                }
                                            }
                                            
                                            for(int p = 7;p >= 0;p--){
                                                int bit = (1<<p)&flag;
                                                if(bit > 0) bit = 1;
                                                else bit = 0;
                                                System.out.print(bit);
                                            }
                                            System.out.println("");
                                            System.out.print("after stuffing : " );
                                            
                                            for(int p = 0; p < getLength(); p++){
                                                for(int pp = 7; pp >= 0;pp--){
                                                    int bit = (1<<pp)&buf[p];
                                                    if(bit > 0) bit = 1;
                                                    else bit = 0;
                                                    System.out.print(bit);
                                                }
                                            }
                                            
                                            System.out.println("");
                                            System.out.println("");
                                            
                                            //////////////////

                                            seqNums[curInd] = seq;
                                            for(int o = 0; o < getLength();o++){
                                                frames[seq][o] = buf[o];
                                            }
                                            frameLen[seq] = getLength();
                                            curInd++;
                                            
                                            isFlagAdded = false;
                                            
                                            
                                            msgWrite.writeUTF("sending usual frame");
                                            //msgWrite.flush();
                                            msgWrite.write(buf,0,getLength());
                                            //msgWrite.flush();
                                            
                                            
                                            //after sending 8 consecutive frame
                                            try {
                                                Thread.sleep(30);
                                            } catch (Exception e) {
                                                //System.out.println("sleep called 2");
                                            }
                                            if(curInd == 8){
                                                
                                                
                                                //System.out.println("inside 2");
                                                
                                            
                                                int loop = 0;
                                                
                                                boolean isOk = false;
                                                Set<Integer> st = new LinkedHashSet<Integer>();
                                                for(int oo = 0; oo < curInd;oo++){
                                                    st.add(seqNums[oo]);
                                                }
                                                while(loop < 3){
                                                    try{

                                                        msgWrite.writeUTF("give ack");
                                                        socket.setSoTimeout(5000);
                                                        int count = 0;
                                                        
                                                        while(st.size() > 0){
                                                            String msg1 = msgRead.readUTF();
                                                            if( msg1.equals("acknowledged")){
                                                                //get ack num
                                                                int val = Integer.parseInt(msgRead.readUTF());
                                                                if(st.contains(val)) st.remove(val);//ok matched
                                                                
                                                                
                                                            }
                                                            
                                                        }
                                                    }catch(SocketTimeoutException e){

                                                            Iterator it = st.iterator();
                                                            while(it.hasNext()){
                                                                int seqN = (int) it.next();
                                                                msgWrite.writeUTF("sending usual frame");
                                                                //msgWrite.flush();
                                                                msgWrite.write(frames[seqN],0,frameLen[seqN]);
                                                            }
                                                            
                                                            

                                                        loop++;
                                                        continue;
                                                    }

                                                    isOk = true;
                                                    break;
                                                }
                                                //if isOk false do some work
                                                ////
                                                curInd = 0;
                                                st.clear();
                                            }
                                            
                                              
                                            
                                            
                                            

                                            //System.out.println("ack");
                                            reset();
                                            ++frameNum;

                                        }


                                    }


                                }
                                else{
                                    int len = din.read(bytes);

                                    int countBytes = 0,totalCount=0;
                                    String frame = "";
                                    for(int i = 0; i < len; i++){
                                        
                                        byte b = bytes[i];
                                        if(!isFlagAdded){
                                            isFlagAdded = true;
                                            first = i;
                                            for(int j = 7; j >= 0; j--){
                                                int bit = (1<<j)&(flag);
                                                if(bit > 0) bit = 1;
                                                else bit = 0;

                                                writeInBuffer(bit, false);
                                            }
                                            // whether data or ack
                                            int u = 1;
                                            for(int j = 7; j >= 0; j--){
                                                int bit = (1<<j)&(u);
                                                if(bit > 0) bit = 1;
                                                else bit = 0;

                                                writeInBuffer(bit, true);
                                            }
                                            //sequence num
                                            for(int j = 7; j >= 0; j--){
                                                int bit = (1<<j)&(seqNo);
                                                if(bit > 0) bit = 1;
                                                else bit = 0;

                                                writeInBuffer(bit, true);
                                            }
                                            //System.out.println("seq num : " + seqNo );
                                            seqNo++;
                                            if(seqNo > 255) seqNo = 0;
                                            //ack num
                                            int ack = 0;
                                            for(int j = 7; j >= 0; j--){
                                                int bit = (1<<j)&(ack);
                                                if(bit > 0) bit = 1;
                                                else bit = 0;

                                                writeInBuffer(bit, true);
                                            }
                                        }
                                        last = i;
                                        for(int j = 7; j >= 0; j--){
                                            int bit = (1<<j)&b;
                                            if(bit > 0) bit = 1;
                                            else bit = 0;

                                            writeInBuffer(bit, true);
                                        }
                                        
                                        totalCount++;
                                        
                                        if(countBytes < 4){
                                            
                                            countBytes++;
                                        }
                                        if(totalCount == len){
                                            countBytes = 0;
                                            
                                            int seq = seqNo-1;
                                            if(seq < 0) seq = 255;
                                            getCheckSum(first, last,1,0,seq);
                                            for(int j = 7; j >= 0; j--){
                                                int bit = (1<<j)&(flag);
                                                if(bit > 0) bit = 1;
                                                else bit = 0;

                                                writeInBuffer(bit, false);
                                            }
                                            
                                            //// printing
                                            System.out.print("frame " + frameNum + " : " );
                                            for(int p = 7;p >= 0;p--){
                                                int bit = (1<<p)&flag;
                                                if(bit > 0) bit = 1;
                                                else bit = 0;
                                                System.out.print(bit);
                                            }
                                            
                                            for(int p = first; p <= last; p++){
                                                for(int pp = 7; pp >= 0;pp--){
                                                    int bit = (1<<pp)&bytes[p];
                                                    if(bit > 0) bit = 1;
                                                    else bit = 0;
                                                    System.out.print(bit);
                                                }
                                            }
                                            
                                            for(int p = 7;p >= 0;p--){
                                                int bit = (1<<p)&flag;
                                                if(bit > 0) bit = 1;
                                                else bit = 0;
                                                System.out.print(bit);
                                            }
                                            System.out.println("");
                                            System.out.print("after stuffing : " );
                                            
                                            for(int p = 0; p < getLength(); p++){
                                                for(int pp = 7; pp >= 0;pp--){
                                                    int bit = (1<<pp)&buf[p];
                                                    if(bit > 0) bit = 1;
                                                    else bit = 0;
                                                    System.out.print(bit);
                                                }
                                            }
                                            
                                            System.out.println("");
                                            System.out.println("");
                                            //////////////////
                                            
                                            seqNums[curInd] = seq;
                                            for(int o = 0; o < getLength();o++){
                                                frames[seq][o] = buf[o];
                                            }
                                            frameLen[seq] = getLength();
                                            curInd++;


                                            isFlagAdded = false;
                                            msgWrite.writeUTF("sending usual frame");
                                            //msgWrite.flush();
                                            msgWrite.write(buf,0,getLength());
                                            //msgWrite.flush();
                                            try {
                                                Thread.sleep(30);
                                            } catch (Exception e) {
                                                //System.out.println("sleep called 3");
                                            }

                                            

                                            //after sending 8 consecutive frame
                                            
                                            if(curInd == 8){
                                                
                                                
                                                
                                                //System.out.println("inside 3");
                                            
                                                int loop = 0;
                                                
                                                boolean isOk = false;
                                                Set<Integer> st = new LinkedHashSet<Integer>();
                                                for(int oo = 0; oo < curInd;oo++){
                                                    st.add(seqNums[oo]);
                                                }
                                                while(loop < 3){
                                                    try{

                                                        msgWrite.writeUTF("give ack");
                                                        socket.setSoTimeout(5000);
                                                        
                                                        
                                                        while(st.size() > 0){
                                                            String msg1 = msgRead.readUTF();
                                                            if( msg1.equals("acknowledged")){
                                                                //get ack num
                                                                int val = Integer.parseInt(msgRead.readUTF());
                                                                if(st.contains(val)) st.remove(val);//ok matched
                                                                
                                                                
                                                            }
                                                            
                                                        }
                                                    }catch(SocketTimeoutException e){

                                                            Iterator it = st.iterator();
                                                            while(it.hasNext()){
                                                                int seqN = (int) it.next();
                                                                msgWrite.writeUTF("sending usual frame");
                                                                //msgWrite.flush();
                                                                msgWrite.write(frames[seqN],0,frameLen[seqN]);
                                                            }
                                                            
                                                            

                                                        loop++;
                                                        continue;
                                                    }

                                                    isOk = true;
                                                    break;
                                                }
                                                //if isOk false do some work
                                                ////
                                                curInd = 0;
                                                st.clear();
                                                
                                            }
                                            //System.out.println("ack");
                                            reset();
                                            ++frameNum;

                                        }
                                        else if(countBytes == 4){
                                            countBytes = 0;
                                            
                                            int seq = seqNo-1;
                                            if(seq < 0) seq = 255;
                                            getCheckSum(first, last,1,0,seq);
                                            for(int j = 7; j >= 0; j--){
                                                int bit = (1<<j)&(flag);
                                                if(bit > 0) bit = 1;
                                                else bit = 0;

                                                writeInBuffer(bit, false);
                                            }
                                            
                                            //// printing
                                            System.out.print("frame " + frameNum + " : " );
                                            for(int p = 7;p >= 0;p--){
                                                int bit = (1<<p)&flag;
                                                if(bit > 0) bit = 1;
                                                else bit = 0;
                                                System.out.print(bit);
                                            }
                                            
                                            for(int p = first; p <= last; p++){
                                                for(int pp = 7; pp >= 0;pp--){
                                                    int bit = (1<<pp)&bytes[p];
                                                    if(bit > 0) bit = 1;
                                                    else bit = 0;
                                                    System.out.print(bit);
                                                }
                                            }
                                            
                                            for(int p = 7;p >= 0;p--){
                                                int bit = (1<<p)&flag;
                                                if(bit > 0) bit = 1;
                                                else bit = 0;
                                                System.out.print(bit);
                                            }
                                            System.out.println("");
                                            System.out.print("after stuffing : " );
                                            
                                            for(int p = 0; p < getLength(); p++){
                                                for(int pp = 7; pp >= 0;pp--){
                                                    int bit = (1<<pp)&buf[p];
                                                    if(bit > 0) bit = 1;
                                                    else bit = 0;
                                                    System.out.print(bit);
                                                }
                                            }
                                            
                                            System.out.println("");
                                            System.out.println("");
                                            //////////////////

                                            seqNums[curInd] = seq;
                                            for(int o = 0; o < getLength();o++){
                                                frames[seq][o] = buf[o];
                                            }
                                            frameLen[seq] = getLength();
                                            curInd++;

                                            isFlagAdded = false;
                                            msgWrite.writeUTF("sending usual frame");
                                            //msgWrite.flush();
                                            msgWrite.write(buf,0,getLength());
                                            //msgWrite.flush();
                                            //System.out.println(buf.toString());
                                            try {
                                                Thread.sleep(30);
                                            } catch (Exception e) {
                                                //System.out.println("sleep called 4");
                                            }
                                            if(curInd == 8){
                                                
                                                //System.out.println("inside 4");
                                                
                                                
                                            
                                                int loop = 0;
                                                
                                                boolean isOk = false;
                                                Set<Integer> st = new HashSet<Integer>();
                                                for(int oo = 0; oo < curInd;oo++){
                                                    st.add(seqNums[oo]);
                                                }
                                                while(loop < 3){
                                                    try{
                                                        msgWrite.writeUTF("give ack");
                                                        
                                                        socket.setSoTimeout(10000);
                                                        int count = 0;
                                                        
                                                        while(st.size() > 0){
                                                            String msg1 = msgRead.readUTF();
                                                            if( msg1.equals("acknowledged")){
                                                                //get ack num
                                                                int val = Integer.parseInt(msgRead.readUTF());
                                                                if(st.contains(val)) st.remove(val);//ok matched
                                                                //if(t < curInd) arr[t++] = val;
                                                                
                                                            }
                                                            
                                                        }
                                                    }catch(SocketTimeoutException e){

                                                            Iterator it = st.iterator();
                                                            while(it.hasNext()){
                                                                int seqN = (int) it.next();
                                                                msgWrite.writeUTF("sending usual frame");
                                                                //msgWrite.flush();
                                                                msgWrite.write(frames[seqN],0,frameLen[seqN]);
                                                            }
                                                            
                                                            

                                                        loop++;
                                                        continue;
                                                    }

                                                    isOk = true;
                                                    break;
                                                }
                                                
                                                //if isOk false do some work
                                                ////
                                                curInd = 0;
                                                st.clear();
                                                
                                            }
                                            
                                            //System.out.println("ack");
                                            reset();
                                            ++frameNum;

                                        }

                                    }


                                }

                                totalChunk--;
                            }
                            isEmpty = true;
                            //System.out.println("finally finished sending");
                            notification.appendText("Successfully sent!\n");
                            receiverId.setEditable(true);
                        }

                        else if(msg.equals("finished sending")){

                            isEmpty = true;
                            //System.out.println("finally finished sending");
                            notification.appendText("Successfully sent!\n");
                            receiverId.setEditable(true);


                        }


                    }

                catch(SocketTimeoutException e){
                    //System.out.println("beyadob");
                    continue;
                }

                catch (IOException ex) {
                    //System.out.println("in ex " + msg);
                    //System.out.println(ex);
                } 
            }
        }
        
        private int getLength() {
            if (avbits == 8) { // A integral number of bytes
                return curbyte;
            }
            else { // Some bits in last byte
                return curbyte+1;
            }
        }
        
        private void reset() {
        
            // Reinit pointers
            curbyte = 0;
            avbits = 8;
            buf = null;
            buf = new byte[12];
        }
        
        private void writeInBuffer(int bit,boolean isData){
            buf[curbyte] |= bit << --avbits;
            if(bit == 1 && isData) counter++;
            else counter = 0;
            if(isData){
                if(avbits > 0){
                    //There is still place in current byte for next bit
                    if(counter == 5){
                        //do bit stuffing
                        counter = 0;
                        avbits--;
                    }
                    if(avbits == 0){
                        // if no place increment byte num
                        curbyte++;
                        avbits = 8;
                    }

                }
                else{
                    //no place
                    if(counter == 5){
                        //do bit stuffing
                        counter = 0;
                        avbits = 7;
                        curbyte++;
                    }
                    else{
                        curbyte++;
                        avbits = 8;
                    }
                }
            }
            else{
                if(avbits == 0){
                    avbits = 8;
                    curbyte++;
                }
            }
            if (curbyte == buf.length) {
                // We are at end of 'buf' => extend it
                byte oldbuf[] = buf;
                buf = new byte[oldbuf.length+1];
                System.arraycopy(oldbuf,0,buf,0,oldbuf.length);
            }

        }
        
        private void getCheckSum(int fir,int last,int dataOrAck,int seqNum,int AckNum){
            
            int bit = 7;
            while(bit >= 0){
                int xor = 0;
                int bitV = (1<<bit)&dataOrAck;
                if(bitV > 0) bitV = 1;
                else bitV = 0;
                xor ^= bitV;
                
                bitV = (1<<bit)&seqNum;
                if(bitV > 0) bitV = 1;
                else bitV = 0;
                xor ^= bitV;
                
                bitV = (1<<bit)&AckNum;
                if(bitV > 0) bitV = 1;
                else bitV = 0;
                xor ^= bitV;
                
                for(int i = fir;i <= last; i++){
                    int bitVal = (1<<bit)&bytes[i];
                    if(bitVal > 0) bitVal = 1;
                    else bitVal = 0;
                    xor ^= bitVal;
                }
                //System.out.print(xor);
                writeInBuffer(xor,true);
                bit--;
                
            }
            
            
        }
        
        private String getParentDirectory(String file){
            //String os = System.getProperty("os.name").toLowerCase();
            //boolean iswindows = true;
            //if(os.indexOf("win") >= 0) iswindows = true;
            //else iswindows = false;
            int len = file.length();
            len--;
            //if(iswindows){
                while(file.charAt(len) != '\\') len--;
            //}
            //else{
                //while(file.charAt(len) != '/') len--;
            //}
            
            len--;
            int i = 0;
            String files = "";
            while(i <= len){
                files += file.charAt(i);
                i++;
            }
            
            return files;
        }
        
        
        
        
    }
    
    
    
    private String getFileExtension(String fileName){
        //String os = System.getProperty("os.name").toLowerCase();
        //boolean iswindows = true;
        //if(os.indexOf("win") >= 0) iswindows = true;
        //else iswindows = false;
        int len = fileName.length();
        String file = "";
        int index = len-1;
        do{
            index--;
        }while(fileName.charAt(index) != '.' && index > 0);
        index++;
        while(index < len){
            file += fileName.charAt(index);
            index++;
        }
        return file;
        
    }
    
    private String getFileName(String fileName){
        //String os = System.getProperty("os.name").toLowerCase();
        //boolean iswindows = true;
        //if(os.indexOf("win") >= 0) iswindows = true;
        //else iswindows = false;
        int len = fileName.length();
        String file = "";
        int index = len-1;
        //if(iswindows){
            do{
                index--;
            }while(fileName.charAt(index) != '\\' && index > 0);
        //}
        //else{
            //do{
               // index--;
            //}while(fileName.charAt(index) != '/' && index > 0);
        //}
        
        index++;
        while(index < len && fileName.charAt(index) != '.'){
            file += fileName.charAt(index);
            index++;
        }
        return file;
    }
    
}