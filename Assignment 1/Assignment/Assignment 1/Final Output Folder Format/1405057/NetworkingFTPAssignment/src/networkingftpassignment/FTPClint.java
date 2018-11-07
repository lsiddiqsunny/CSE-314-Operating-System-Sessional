package networkingftpassignment;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author HP
 */

public class FTPClint {
    private static Socket mySocket;
    private static DataInputStream dataIn;
    private static DataOutputStream dataOut;
    private static Scanner sc = new Scanner(System.in);
    private static int state = 0;
    private static int err = 0;
   
    //0=undefined, 1=sent, 2=timed out, 3=receiver offline 4=out of space
    
    public static void main(String[] args){
        //DLLHelper.getChecksum(DLLHelper.byteToBinString("a".getBytes()));
        //return;
        //DLLHelper.createFrame("Kaykobad".getBytes(), 1, 1, 1);
        Scanner consoleReader = new Scanner(System.in);
        try {
            System.out.println("Connecting to server...");
            mySocket = new Socket("localhost", 12345);
            System.out.println("Connected to server...");
            
            dataIn = new DataInputStream(mySocket.getInputStream());
            dataOut = new DataOutputStream(mySocket.getOutputStream());
            
            System.out.println(dataIn.readUTF());
            dataOut.writeUTF(consoleReader.nextLine());
            
            String serverResponse = dataIn.readUTF();
            
            if(serverResponse.equals("Multiple login attempt, connection refused.")){
                System.out.println("Connection refused for multiple login attempt.");
                mySocket.close();
            }else{
                while(true){
                    System.out.println(serverResponse);
                    System.out.println("Press \"close\" to close the connection.");
                    String option = consoleReader.nextLine();
                    dataOut.writeUTF(option);

                    if(option.equalsIgnoreCase("yes")){
                        //send files
                        System.out.println("Generate random error on random frame? yes/no: ");
                        String ans = consoleReader.nextLine();
                        if(ans.equalsIgnoreCase("yes"))
                            err = 1;
                        sendFile();
                    }else if(option.equalsIgnoreCase("no")){
                        //get file
                        receiveFile();
                    }else if(option.equalsIgnoreCase("close")){
                        //close the connection
                        mySocket.close();
                        return;
                    }
                }
                //dataIn.readUTF();
            }
        } catch (IOException ex) {
            Logger.getLogger(FTPClint.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    public static void sendFile(){
        Random random = new Random();
        
        try {
            System.out.println(dataIn.readUTF());
            dataOut.writeUTF(sc.nextLine());
            String serverResponse = dataIn.readUTF();
            
            //check if receiver is online
            if(serverResponse.equals("Receiver is Offline...")){
                System.out.println(serverResponse);
            }else{
                //if online, choose the file
                String filePath = null;
                File file = null;
                FileInputStream fin = null;
                int foundFile = 0;
                
                while(foundFile == 0){
                    try{
                        System.out.println("File with full path address: ");
                        filePath = sc.nextLine();
                        file = new File(filePath);
                        fin = new FileInputStream(new File(filePath));
                        foundFile = 1;
                    }catch(FileNotFoundException notFound){
                        System.out.println("File path is not correct.");
                    }
                }
                
                String fileName = filePath.substring(filePath.lastIndexOf("\\")+1);
                long fileSize = file.length();
                
                dataOut.writeUTF(fileName);
                dataOut.writeLong(fileSize);
                
                serverResponse = dataIn.readUTF();
                
                if(serverResponse.equals("Not enough server space...")){
                    System.out.println(serverResponse);
                }else{
                    System.out.println("Introduce random frame loss? yes/no: ");
                    String floss = sc.nextLine();
                    
                    String fileId = dataIn.readUTF();
                    int buffSize = dataIn.readInt();
                    byte[] buff = new byte[buffSize];
                    
                    int fNo = random.nextInt((int)fileSize/buffSize)+1;
                    
                    System.out.println("=====================================\n");
                    System.out.println("Sending file...");
                    
                    //start sending files
                    String acknowledgement;
                    int a = fin.read(buff);
                    String frame = "", backupFrame;
                    int seqCounter = 0;
                    while(a != -1){
                        seqCounter++;
          
                        //System.out.println("Sending chunk");
                        backupFrame = frame = DLLHelper.createFrame(buff, seqCounter, a, 1);
                        if(err == 1 && random.nextBoolean()){
                            err = 0;
                            frame = DLLHelper.errorGenerator(frame);
                        }
                        
                        if(floss.equalsIgnoreCase("yes") && fNo==seqCounter){
                            //drop the frame
                            floss = "no";
                        }else{
                            dataOut.writeUTF(frame);
                        }
                        
                        //dataOut.write(buff);
                        //dataOut.writeInt(a);
                        //wait for acknowledgement
                        long startTime = System.currentTimeMillis();
                        
                        while(dataIn.available()==0){
                            if((System.currentTimeMillis()-startTime)>30*1000){
                                //buff = "Timed out".getBytes();
                                //frame = DLLHelper.createFrame(buff, 0, 126, 1);
                                dataOut.writeUTF(frame);
                                //dataOut.write(buff);
                                //dataOut.writeInt(-50);
                                //System.out.println("Timed out, no server response for 30 seconds...");
                                System.out.println("Timed out, no server response for 30 seconds. Possible frame loss... Resending frame");
                                startTime = System.currentTimeMillis();
                                //fin.close();
                                //return;
                            }
                        }
                        acknowledgement = dataIn.readUTF();
                        
                        while(acknowledgement.equalsIgnoreCase("error")){
                            System.out.println("Resending previous frame on error request");
                            dataOut.writeUTF(backupFrame);
                            acknowledgement = dataIn.readUTF();
                        }
                        
                        a = fin.read(buff);
                    }
                    
                    System.out.println("Sending finishing message");
                    String finish = "Finished sending file";
                    //byte[] finis = finish.getBytes();
                    buff = finish.getBytes();
                    frame = DLLHelper.createFrame(buff, 0, 125, 1);
                    dataOut.writeUTF(frame);
                    //dataOut.write(buff);
                    //dataOut.writeInt(-1405057);
                    
                    System.out.println(dataIn.readUTF());
                    fin.close();
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(FTPClint.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("=====================================\n");
    }
    
    public static void receiveFile(){
        try {
            String res = dataIn.readUTF();
            if(res.equalsIgnoreCase("No file to receive")){
                System.out.println(res);
                return;
            }
            String fId = dataIn.readUTF();
            String fname = dataIn.readUTF();
            System.out.println(res);
            String yesNo = sc.nextLine();
            Path p = Paths.get("received").toAbsolutePath();
            dataOut.writeUTF(yesNo);
            
            
            if(yesNo.equalsIgnoreCase("yes")){
                File f = new File(p+"\\"+fId+fname);
                FileOutputStream ffout = new FileOutputStream(f);
                System.out.println("=====================================\n");
                System.out.println("Receiving file...");
            
                int b;
                byte[] buff = new byte[100];
                String frame;
                
                //dataIn.read(buff);
                //b = dataIn.readInt();
                
                while(true){
                    frame = dataIn.readUTF();
                    DLLHelper.printFrameDetails(frame);
                    frame = DLLHelper.removeOverhead(frame);
                    b = DLLHelper.getSize(frame);
                    buff = DLLHelper.getPayload(frame);
                    String checkSum = DLLHelper.extCheckSum(frame);
                    
                    //checksum verify
                    if(DLLHelper.errorDetector(buff, checkSum)){
                        dataOut.writeUTF("got");
                    }else{
                        System.out.println("Errornious frame, requesting resend...");
                        dataOut.writeUTF("error");  
                        continue;
                    }
                    
                    //checking eof
                    if(b<100){
                        ffout.write(buff, 0, b);
                        break;
                    }
                    ffout.write(buff, 0, b);
                    ffout.flush();
//                    
//                    frame = dataIn.readUTF();
//                    DLLHelper.printFrameDetails(frame);
//                    frame = DLLHelper.removeOverhead(frame);
//                    b = DLLHelper.getSize(frame);
//                    buff = DLLHelper.getPayload(frame);
                    //dataIn.read(buff);
                    //System.out.println(new String(buff));
                    //b = dataIn.readInt();
                    //System.out.println(b);
                }
                
                ffout.close();
                System.out.println("File location : "+p+"\\"+fId+fname);
               
            }else{
                System.out.println("Receiving file refused.");
            }
        } catch (IOException ex) {
            Logger.getLogger(FTPClint.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("=====================================\n");
    }
    
}
