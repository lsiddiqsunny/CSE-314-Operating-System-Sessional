package networkingftpassignment;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import static networkingftpassignment.FTPServer.LookIfOnline;
import static networkingftpassignment.FTPServer.onlineList;

/**
 *
 * @author HP
 */

public class FTPServerUtilities implements Runnable{
    
    private Socket myClientSocket;
    private DataInputStream dataIn;
    private DataOutputStream dataOut;
    String stId;
    Path currentDirectory = Paths.get("temp").toAbsolutePath();
    private long fileSize;
    private String fileName;
    private String fileId;
    private String receiverId;
    boolean receivedCorrectly = false;
    int count;
    

    public FTPServerUtilities(Socket myClientSocket) {
        this.myClientSocket = myClientSocket;
        //this.stId = stId;
        try {
            dataIn = new DataInputStream(this.myClientSocket.getInputStream());
            dataOut = new DataOutputStream(this.myClientSocket.getOutputStream());
        } catch (IOException ex) {
            Logger.getLogger(FTPServerUtilities.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    @Override
    public void run() {
        try {
            //getting the student id
            dataOut.writeUTF("Student Id: ");
            stId = dataIn.readUTF();
            
            //checking if multiple login attempt
            Boolean b = LookIfOnline(stId);
            
            if(b){
                System.out.println("Trying to login multiple times.");
                dataOut.writeUTF("Multiple login attempt, connection refused.");
                System.out.println("Connection refused.");
                myClientSocket.close();
                return;
            }else{
                System.out.println("Connection accepted from : " + myClientSocket);
                onlineList.add(new MemberInformation(stId, myClientSocket));
                
                //dataOut.writeUTF("Welcome to ftp service.\nType \"send\" to send files.\nType \"get\" to receive file.\nType \"close\" to close the connection.");
                dataOut.writeUTF("Welcome to ftp service. Do you want to send files? yes/no : ");
                
            }
            while(myClientSocket.isConnected()){
                receivedCorrectly = false;
                try {
                    String option = dataIn.readUTF();
                    
                    if(option.equalsIgnoreCase("yes")){
                        //receive file
                        count = FTPServer.count++;
                        receiveFile();
                    }else if(option.equalsIgnoreCase("no")){
                        //send files
                        int p=sendFile();
                        if(p==0){
                            //System.out.println("Comes3 with p : "+p);
                            dataOut.writeUTF("No file to receive");
                        }
                    }else if(option.equalsIgnoreCase("close")){
                        //close the connection
                        FTPServer.removeUser(stId);
                        myClientSocket.close();
                        return;
                    }
                } catch (IOException ex) {
                    //Logger.getLogger(FTPServerUtilities.class.getName()).log(Level.SEVERE, null, ex);
                    System.out.println("Connection closed of user id : "+stId);
                    FTPServer.removeUser(stId);
                    //FTPServer.printOnlineUsers();
                    break; 
                }
            }
        } catch (IOException ex) {
            //Logger.getLogger(FTPServerUtilities.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Connection closed before login.");
        }
    }
    
    public void receiveFile(){
        FileOutputStream fout = null;
        File rFile = null;
        try {
            dataOut.writeUTF("Receiver Student Id: ");
            receiverId = dataIn.readUTF();
            Boolean b = FTPServer.LookIfOnline(receiverId);
            
            //checking if receiver is online
            if(b){
                //next step
                dataOut.writeUTF("File name and size : ");
                fileName = dataIn.readUTF();
                fileSize = dataIn.readLong();
                
                long currentServerSpace = FTPServer.getUnusedCapacity();
                
                //check if server has enough space
                if(currentServerSpace>=fileSize){
                    FTPServer.setServerUsed(fileSize);
                    
                    //generating random chunk size and file id
                    fileId = "file"+count;
                    Random random = new Random();
                    int chunkSize = random.nextInt(2)+1;
//                    if(chunkSize>fileSize)
//                        chunkSize = (int)fileSize;
                    dataOut.writeUTF("Send file now...");
                    dataOut.writeUTF(fileId);
                    dataOut.writeInt(chunkSize);
                    
                    //message
                    System.out.println("=====================================\n");
                    System.out.println("Receiving file from "+stId);
                    System.out.println("Buffer size : "+ chunkSize);
                    System.out.println("File size : "+ fileSize);
                    
                    //start receiving file
                    rFile = new File(currentDirectory+"\\"+fileId+fileName);
                    fout = new FileOutputStream(rFile);
                    String fMess = "Finished sending file. Thanks for being a part.";
                    byte[] fm = new byte[chunkSize];
                    fm = fMess.getBytes();
                    
                    
                    byte[] receivedChunk = new byte[chunkSize];
                    String frame = dataIn.readUTF();
                    DLLHelper.printFrameDetails(frame);
                    //dataIn.read(receivedChunk);
                    //int bytesSent = dataIn.readInt();
                    frame = DLLHelper.removeOverhead(frame);
                    int bytesSent = DLLHelper.getSize(frame);
                    String checkSum = DLLHelper.extCheckSum(frame);
                    while(true){
                        //end of file
                        if(bytesSent == 125){
                            fout.close();
                            break;
                        }
                        //timed out message
                        else if(bytesSent == 126){
                            fout.close();
                            rFile.delete();
                            FTPServer.freeServer(fileSize);
                            System.out.println("Timed out occured, deleting incomplete file");
                            return;
                        }
                        receivedChunk = DLLHelper.getPayload(frame);
                        
                        //checking for error
                        if(!DLLHelper.errorDetector(receivedChunk, checkSum)){
                            dataOut.writeUTF("error");
                        }else{
                            dataOut.writeUTF("got");
                            fout.write(receivedChunk, 0, bytesSent);
                            fout.flush();
                        }
                        //System.out.println(new String(receivedChunk));
                        //fout.write(receivedChunk, 0, bytesSent);
                        //dataOut.writeUTF("got");
                        //System.out.println("Got a chunk");
                        //fout.flush();
                        
                        frame = dataIn.readUTF();
                        DLLHelper.printFrameDetails(frame);
                        frame = DLLHelper.removeOverhead(frame);
                        bytesSent = DLLHelper.getSize(frame);
                        checkSum = DLLHelper.extCheckSum(frame);
                        //dataIn.read(receivedChunk);
                        //bytesSent = dataIn.readInt();
                        
                    }
                    
                    System.out.println("Finished receiving file");
                    if(rFile.length()==fileSize){
                        FTPServer.addBuffer(stId, receiverId, fileName, fileSize, fileId);
                        System.out.println("Received file correctly");
                        dataOut.writeUTF("Received file correctly");
                        receivedCorrectly = true;
                        FTPServer.printCurrentBuffer();
                        
                        boolean bd = FTPServer.LookIfOnline(receiverId);
                        if(!bd){
                            //receiver is offline, delete the file
                            System.out.println("Receiver offline, delete the file");
                            FTPServer.freeServer(fileSize);
                            //free up buffer
                            fout.close();
                            rFile.delete();
                            
                            for(int i=0; i<FTPServer.currentBuffer.size(); i++){
                                if(FTPServer.currentBuffer.get(i).getFileId().equalsIgnoreCase(fileId)){
                                    FTPServer.currentBuffer.remove(i);
                                }
                            }
                        }
                        fout.close();
                    }else{
                        dataOut.writeUTF("File size mismatch, deleting file from server");
                        System.out.println("File size mismatch, deleting file from server");
                        FTPServer.freeServer(fileSize);
                        fout.close();
                        rFile.delete();
                    }
                    
                }else{
                    dataOut.writeUTF("Not enough server space...");
                }
                
            }else{
                //close
                dataOut.writeUTF("Receiver is Offline...");
            }
        } catch (IOException ex) {
            //Logger.getLogger(FTPServerUtilities.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Exception occured during receiving file from user id : "+stId);
            if(!receivedCorrectly){
                FTPServer.freeServer(fileSize);
                try {
                    fout.close();
                    rFile.delete();
                } catch (IOException ex1) {
                    //Logger.getLogger(FTPServerUtilities.class.getName()).log(Level.SEVERE, null, ex1);
                    System.out.println("Unable to delete temporary file : "+fileId+fileName);
                }
            }
        }
        System.out.println("=====================================\n");
        
    }
    
    public int sendFile(){
        int xx = 0;
        long size = 0;
        String filId = null;
        File f = null;
        FileInputStream ffileIn = null;
        //System.out.println("a");
        for(int i=0; i<FTPServer.currentBuffer.size(); i++){
            //System.out.println("b");
            if(FTPServer.currentBuffer.get(i).receivertStudentId.equalsIgnoreCase(stId)){
                String sender = FTPServer.currentBuffer.get(i).senderStudentId;
                String fileNm = FTPServer.currentBuffer.get(i).fileName;
                size = FTPServer.currentBuffer.get(i).fileSize;
                filId = FTPServer.currentBuffer.get(i).fileId;
                System.out.println("Currently unused server space: "+FTPServer.getUnusedCapacity());
                FTPServer.currentBuffer.remove(i);
                
                //System.out.println("c");
                try {
                    dataOut.writeUTF("Student id "+sender+" wants to send you a file.\nFile name : "+fileNm+" size : "+size+"\nDo you want to receive it? yes/no :");
                    //System.out.println("d");
                    dataOut.writeUTF(filId);
                    //System.out.println("e");
                    dataOut.writeUTF(fileNm);
                    //System.out.println("f");
                    String ans = dataIn.readUTF();
                    //System.out.println(ans);
                    
                    if(ans.equalsIgnoreCase("yes")){
                        //send file
                        System.out.println("========================================\nSending file to receiver id: "+stId+"\n=========================================");
                        f = new File(currentDirectory+"\\"+filId+fileNm);
                        try (FileInputStream fileIn = new FileInputStream(f)) {
                            int a;
                            ffileIn = fileIn;
                            byte[] chunk = new byte[100];
                            int seqCounter = 0;
                            String frame;
                            
                            a = fileIn.read(chunk);
                            while(a!=-1){
                                seqCounter++;
                                frame = DLLHelper.createFrame(chunk, seqCounter, a, 1);
                                dataOut.writeUTF(frame);
                                
                                long startTime = System.currentTimeMillis();
                        
                                while(dataIn.available()==0){
                                    if((System.currentTimeMillis()-startTime)>30*1000){
                                        //buff = "Timed out".getBytes();
                                        //frame = DLLHelper.createFrame(buff, 0, 126, 1);
                                        dataOut.writeUTF(frame);
                                        //dataOut.write(buff);
                                        //dataOut.writeInt(-50);
                                        //System.out.println("Timed out, no server response for 30 seconds...");
                                        System.out.println("Timed out, no client response for 30 seconds, possible frame loss... Resending frame");
                                        startTime = System.currentTimeMillis();
                                        //fin.close();
                                        //return;
                                    }
                                }

                                String ack = dataIn.readUTF();
                                while(ack.equalsIgnoreCase("error")){
                                    System.out.println("Resending frame upon error request...");
                                    dataOut.writeUTF(frame);
                                    ack = dataIn.readUTF();
                                }
                                //dataOut.write(chunk);
                                //dataOut.writeInt(a);
                                //System.out.println("e");
                                a = fileIn.read(chunk);
                            }
                            
                        }
                        f.delete();
                        System.out.println("File sending success...");
                        
                        i--;
                        FTPServer.freeServer(size);
                        System.out.println("Currently unused server space: "+FTPServer.getUnusedCapacity());
                        xx = 1;
                        break;
                    }else{
                        System.out.println("Receiving refused by "+stId+".\nDeleting file from server.");
                        File file = new File(currentDirectory+"\\"+filId+fileNm);
                        
                        FTPServer.freeServer(file.length());
                        file.delete();
                        //FTPServer.currentBuffer.remove(i);
                        i--;
                        xx = 1;
                        break;
                    }
                } catch (IOException ex) {
                    //Logger.getLogger(FTPServerUtilities.class.getName()).log(Level.SEVERE, null, ex);
                    System.out.println("Exception occured while sending file to user id : "+stId);
                    try {
                        ffileIn.close();
                        f.delete();
                    } catch (IOException ex1) {
                        //Logger.getLogger(FTPServerUtilities.class.getName()).log(Level.SEVERE, null, ex1);
                        System.out.println("Cannot delete temporary file : "+filId+fileNm);
                    }catch(Exception e){
                        System.out.println("Wnknown exception.");
                    }
                    
                    FTPServer.freeServer(size);
                }
                
            }
            
        }
        System.out.println("=====================================\n");
        return xx;
    }
    
//    public void sendfile(){
//        try {
//            //check if receiver is still online
//            Boolean b = FTPServer.LookIfOnline(receiverId);
//            DataOutputStream out;
//            DataInputStream in;
//            Socket rSocket = null;
//            
//            if(b){
//                //if online, get the socket
//                for(int i=0; i<FTPServer.onlineList.size(); i++){
//                    if(FTPServer.onlineList.get(i).getStudentId().equalsIgnoreCase(receiverId)){
//                        rSocket = FTPServer.onlineList.get(i).getSocket();
//                    }
//                }
//                
//                out = new DataOutputStream(rSocket.getOutputStream());
//                in = new DataInputStream(rSocket.getInputStream());
//                
//                out.writeUTF(stId);
//                
//                out.writeUTF("Student id "+stId+" wants to send you a file.\nFile name : "+fileName+" size : "+fileSize+"\nDo you want to receive it? yes/no :");
//                String ans = in.readUTF();
//                if(ans.equalsIgnoreCase("yes")){
//
//                }else{
//                    System.out.println("Receiving refused by "+receiverId+".\nDeleting file from server.");
//                    File file = new File(currentDirectory+"\\"+fileId+fileName);
//                    file.delete();
//                }
//            }else{
//                System.out.println("Receiver is offline.\nDeleting file from server.");
//                File file = new File(currentDirectory+"\\"+fileId+fileName);
//                file.delete();
//            }
//            
//        } catch (IOException ex) {
//            Logger.getLogger(FTPServerUtilities.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }

}
