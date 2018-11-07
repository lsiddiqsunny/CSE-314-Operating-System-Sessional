/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.Pair;






class ServerThread implements Runnable{

    private Socket socket;
    private DataInputStream din;
    private DataOutputStream dout;
    private Thread thread;
    private String username;
    private String password;
    private static ServerStorage serverStorage;
    
    private InputStream inputStream;
    
    
    public ServerThread(Socket socket) throws IOException{
        serverStorage = ServerStorage.getInstance();
        
        this.socket = socket;
        din = new DataInputStream(socket.getInputStream());
        dout = new DataOutputStream(socket.getOutputStream());
        inputStream = socket.getInputStream();
        thread = new Thread(this);
        thread.start();
    }
    
    @Override
    public void run() {
        while(true){
            try{
              
                    String workType = din.readUTF(); // 1
                    
                    
                    
                    if(workType.equals("login")){
                            try{
                                    username = din.readUTF(); // 2
                                    password = din.readUTF(); // 3

                                    String confirm = "no";

                                    if(!username.equals("") && !password.equals("") && 
                                            serverStorage.getPassword(username).equals(password)){
                                        confirm = "yes"; 
                                        serverStorage.setOnline(username, socket);
                                        dout.writeUTF(confirm); // 4
                                    }else{
                                        dout.writeUTF(confirm); // 4
                                        break;
                                    }
                                    
                              
                                    
                            }catch(Exception e){

                            }
                    }
                    else if(workType.equals("logout")){
                            serverStorage.setOffline(username);
                            break;
                    }
                    else if(workType.equals("sendfile")){
                        
                            String receiver = din.readUTF();
                           
                            if(serverStorage.getOnline(receiver) && !receiver.equals(username)){ // RECEIVER IS ONLINE
                               
                                dout.writeUTF("yes");
                               
                                String fileName = din.readUTF();                                // GETTING FILE NAME
                                int sizeOfFileToBeReceived = Integer.parseInt(din.readUTF());   // GETTING FILE SIZE
                                
                                if(serverStorage.getCurrentSize() - sizeOfFileToBeReceived > 0) {
                                    dout.writeUTF("UPLOADING DATA ...");

                                    int chunkSize, totalChunks, excess;

                                    if(sizeOfFileToBeReceived >= 31) {
                                        chunkSize = 31;
                                        excess = sizeOfFileToBeReceived % 31;
                                        totalChunks = (excess == 0) ? 
                                                (sizeOfFileToBeReceived / 31) 
                                                : ((sizeOfFileToBeReceived / 31)+ 1) ;
                                    }else{
                                        chunkSize = sizeOfFileToBeReceived;
                                        excess = 0;
                                        totalChunks = 1;
                                    }

                                    int fileId = serverStorage.genFileId();
                                    dout.writeUTF(String.valueOf(fileId));
                                    dout.writeUTF(String.valueOf(chunkSize));
                                    dout.writeUTF(String.valueOf(excess));
                                    dout.writeUTF(String.valueOf(totalChunks));

                                    serverStorage.setCurrentSize(serverStorage.getCurrentSize() 
                                            - sizeOfFileToBeReceived);

                                    
                                    String serverStorageLocation = serverStorage.getServerStorageLocation();
                                    String chunksLocation = serverStorageLocation + "\\FILE"
                                            + String.valueOf(fileId) + "x";
                                    File folder = new File(chunksLocation);
                                    folder.mkdir();
                                    
                                    
                                    
                                    int tot = 0 ;
                                    boolean istimeout = false;
                                    try {
                                        int i = 0;
                                        while(true) {
                                            String response = din.readUTF();
                                            if(response.equals("done")) break;
                                            
                                            
                                            int len = Integer.parseInt(response);
                                            
                                            
                                            
                                            
                                            
                                            
                                            
                                            
                                            /******************************************************************
                                            * DATA FRAME STARTS HERE *****************************************
                                            * ****************************************************************
                                            */
                                            
                                            String firstDelimiter = din.readUTF();      // DELIMITER
                                            String frameType = din.readUTF();           // DATA FRAME
                                            String sequenceNumber = din.readUTF();      // SEQ NO.
                                            String acknowledgeNumber = din.readUTF();   // ACK NO.
                                            
                                            String payLoad = din.readUTF();             // PAYLOAD
                                            System.out.print("RECEIVED FRAME --> " +(i+1) + ": " );
                                            
                                            
                                            String checkSum = din.readUTF();    // CHECKSUM
                                            String secondDelimiter = din.readUTF();    // DELIMITER
                                            
                                            /******************************************************************
                                            * DATA FRAME ENDS HERE *****************************************
                                            * ****************************************************************
                                            */
                                            
                                            
                                            
                                            
                                            
                                            
                                            
                                            

                                            dout.writeUTF("< FRAME " + (i + 1) + " uploaded to server for USER "
                                                    + "= " + receiver + " >\n");
                                            
                                            String timeout = din.readUTF();
                                            if(timeout.equalsIgnoreCase("yes")){
                                                deleteDirectory(chunksLocation);
                                                istimeout = true;
                                                break;
                                            }
                                            
                                            
                                            
                                            
                                            
                                            bitDestuff( payLoad, i , Integer.parseInt(checkSum), chunksLocation);
                                            
                                            tot += len;
                                            i++;
                                        }
                                        if(istimeout){
                                            deleteDirectory(chunksLocation);
                                            dout.writeUTF("----------- TIMEOUT ------------\n");
                                            continue;
                                        }
                                    }catch (Exception e){
                                        deleteDirectory(chunksLocation);
                                        serverStorage.setOffline(username);
                                        break;
                                    }

                                    
                                    
                                    
                                    serverStorage.addFile(fileId , new FileInfo(String.valueOf(fileId),
                                            username ,receiver , tot, fileName, 
                                            totalChunks, chunkSize), receiver);
                                    dout.writeUTF("-------------- FILE SUCCESSFULLY UPLOADED --------------\n"); 


                                }
                                else{
                                    dout.writeUTF("SERVER LOADED");
                                }
                            }
                            
                            else {
                                dout.writeUTF("no");  // RECEIVER IS NOT ONLINE TRY LATER
                                System.out.println("OFFLINE");
                            }        
                        
            
                    }
            }catch(Exception e){}
    
            
    
  
        }
        
        try{
            inputStream.close();din.close();
            dout.close();
            socket.close();
        }catch(Exception e){}
    }
    
    public void deleteDirectory(String location){
        File folder = new File(location);
        File[] list = folder.listFiles();
        for(File f : list) {
            f.delete();
        }
        folder.delete();
    }
    
    
    public static void bitDestuff(String bitStuffedString, int chunkSerial, int givenCheckSum, String chunksLocation){
        String bigString = "";  
        
        int count = 0;
        int checkSum = 0;
        int bitLen = bitStuffedString.length();

        
        for(int i = 0 ; i < bitLen; i++){
            if(bitStuffedString.charAt(i) == '0'){
                bigString += '0';
                count = 0;
            }
            else{
                count++;
                checkSum++;
                bigString += '1';
                
                if(count == 5){
                    i++;
                    count = 0;
                }
            }
        }
        
        
        int bigLen = bigString.length();
        byte[] frameData = new byte[bigLen >> 3];
        int k = 0;
        for(int i = 0 ; i < bigLen; i+=8){
            byte x = (byte)Integer.parseInt(bigString.substring(i, i + 8) , 2);
            frameData[k] = x;
            k++;
        }
        
        
        try{
            FileOutputStream fStream = new FileOutputStream(chunksLocation
                                                    + "\\" + "CHUNK_" + String.valueOf(chunkSerial) );
            if(!hasChecksumError(givenCheckSum, checkSum)){
                
                System.out.println(" DESTUFFING FRAME ");
                fStream.write(frameData);
                fStream.close();
            }else{
                System.out.println(" !!! DISCARDING FRAME !!! ");
                fStream.close();
            }

         
        }catch(Exception e){
            
        }
     
    }
    
    
    public static boolean hasChecksumError(int receivedChecksum, int calculatedChecksum){
        if(receivedChecksum != calculatedChecksum) return true;
        else return false;
    }
    
}







class ReceiverThread implements Runnable{
    
    
    
    private Socket socket;
    private DataInputStream din;
    private DataOutputStream dout;
    private Thread thread;
    private String username;
    private String password;
    private static ServerStorage serverStorage;
    
    private OutputStream outputStream;
    
    
    public ReceiverThread(Socket socket) throws IOException{
        serverStorage = ServerStorage.getInstance();
        
        this.socket = socket;
        din = new DataInputStream(socket.getInputStream());
        dout = new DataOutputStream(socket.getOutputStream());
        outputStream = socket.getOutputStream();
        thread = new Thread(this);
        thread.start();
    }
    
    @Override
    public void run(){
        try{
            while(true){
                String msg = din.readUTF();
                System.out.println(msg);
                
                
                if(msg.equals("logout")){
                    break;
                }
                else if(msg.equals("showlist")){
                    String id = din.readUTF();
                    Set<Integer> pendingFileid = serverStorage.getPendingFileId(id);
                    
                    System.out.print("CLIENT REQUESTS TO SEE FILE IDS: ");
                    serverStorage.showFileIDS(id);
                    
                    FileInfo info;
                    for(Integer x: pendingFileid){
                        info = serverStorage.getFileInfo(x);
                        dout.writeUTF("info");
                        dout.writeUTF("            "+ info.getFileId());
                        dout.writeUTF("            "+info.getFileName());
                        dout.writeUTF("            "+String.valueOf(info.getFileSize()));
                        dout.writeUTF("            "+info.getSenderId());
                        
                    }
                    dout.writeUTF("done");
                }
                else if(msg.equals("receive")){
                    
                   
                    String fileid = din.readUTF();
                    
                    FileInfo info = serverStorage.getFileData(Integer.parseInt(fileid));
                    dout.writeUTF(info.getFileName());
                    
                    int totalChunks = info.getTotalChunks();
                    dout.writeUTF(String.valueOf(totalChunks));
                    int baseChunkSize = info.getChunkSize();  
                    String sendingFileLocation = serverStorage.getServerStorageLocation()
                            + "\\FILE" + info.getFileId() + "x";
                    
                    
                    for (int i = 0; i < totalChunks; i++) {
                        
                        
                        byte[] fileChunk = Files.readAllBytes(Paths.get( sendingFileLocation 
                                + "\\CHUNK_" + String.valueOf(i) ));
                        
                        
                        
                        serverStorage.setCurrentSize(serverStorage.getCurrentSize() + fileChunk.length);
                        dout.writeUTF(String.valueOf(fileChunk.length));
                        outputStream.write(fileChunk);
                        outputStream.flush();
                        dout.writeUTF("< FRAME " + (i + 1) + " from USER " + info.getSenderId() + " downloaded >\n");
                    
                    }
                    
                    
                    serverStorage.deleteSpecificFileofaReceiver(info.getReceiverId(),
                            Integer.parseInt(fileid));
                    deleteDirectory(sendingFileLocation);
                }
                else if(msg.equals("decline")){
                    String fileid = din.readUTF();
                    FileInfo info = serverStorage.getFileData(Integer.parseInt(fileid));
                    serverStorage.deleteSpecificFileofaReceiver(info.getReceiverId(), Integer.parseInt(fileid));
                    String sendingFileLocation = serverStorage.getServerStorageLocation()
                            + "\\FILE" + info.getFileId() + "x";
                    System.out.println(sendingFileLocation);
                    deleteDirectory(sendingFileLocation);
                    dout.writeUTF("deleted");
                }
            }
        }catch(Exception e){
            
        }
        
        
        try{
            outputStream.close();
            din.close();
            dout.close();
            socket.close();
        }catch(Exception e){
            
        }
        
    }
    
    private void deleteDirectory(String location){
        File folder = new File(location);
        File[] list = folder.listFiles();
        for(File f : list) {
            f.delete();
        }
        folder.delete();
    }
}











public class Server {
    public static void main(String[] args){
        ServerStorage serverStorage = ServerStorage.getInstance();
        System.out.println("******** SERVER ONLINE **********");
        
        new Thread(){
            @Override
            public void run() {
                ServerSocket serverSocket=null;
                try {
                    serverSocket = new ServerSocket(1234);
                } catch (IOException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }
                Socket requestSocket;
                while(true){
                    try {
                        requestSocket = serverSocket.accept();
                        new ServerThread(requestSocket);
                    } catch (Exception e) {
                    
                    }
                    System.out.println("SEND TO SERVER THREAD ON");
                }
            }
        }.start();
        
        
        
        
        
        new Thread(){
            @Override
            public void run() {
                
                    ServerSocket serverSocket=null;
                try {
                    serverSocket = new ServerSocket(2345);
                } catch (IOException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }
                    Socket requestSocket;
                    while(true){
                        try {

                            requestSocket = serverSocket.accept();
                            new ReceiverThread(requestSocket);

                            System.out.println("CONNECTED TO RECEIVE THREAD");
                        } catch (Exception e) {}
                    }
                
        }}.start();
        
        
        
        
    }
}





class ServerStorage{
    private static final ServerStorage SERVER_STORAGE = new ServerStorage();
    private Hashtable<String , Boolean> activeUsers;
    private Hashtable<String, String > passwordtable;
    private static Hashtable<Integer, FileInfo> allFiles;
    private static Hashtable<String, Set<Integer> > userFileIds;
    private int maximumCapacity = 1000000000;  // in KB
    private int currentSize;
    private static int totalFiles;
    private static int fileID;
    private  final String SERVER_STORAGE_LOCATION = "D:\\SERVER_STORAGE";
    
    
    private ServerStorage(){
        fileID = 0;
        totalFiles = 0;
        currentSize = maximumCapacity;
        activeUsers = new Hashtable<>();
        passwordtable = new Hashtable<>();
        allFiles = new Hashtable<>();
        userFileIds = new Hashtable<>();
        
        
        
        for(int i = 1;  i<= 122 ;i++){
            activeUsers.put(String.valueOf(i), false);
            passwordtable.put(String.valueOf(i), String.valueOf(i));
            userFileIds.put(String.valueOf(i), new HashSet<>());
        }
    }
    
    
    public static void addFile(int fileId , FileInfo info, String receiver){
        allFiles.put( fileId, info);
        userFileIds.get(receiver).add(fileId);
        totalFiles++;
        fileID++;
    }
    
    public static FileInfo getFileData(int id){
        return allFiles.get(id);
    }
    
    public static void deleteSpecificFileofaReceiver(String id, int fileId){
        userFileIds.get(id).remove(fileId);
        allFiles.remove(fileId);
    }
    
    public static void showFileIDS(String id){
        for(Integer x: userFileIds.get(id)){
            System.out.print(x + " ");
        }
        System.out.println("");
    }
    
    
    public static Set<Integer> getPendingFileId(String id){
        return userFileIds.get(id);
    }
    
    
    

    public static void deleteFile(int fileId ){
        allFiles.remove( fileId);
        totalFiles--;
    }

    public static FileInfo getFileInfo(int fileId){
        return allFiles.get(fileId);
    }
    
    public int getMaximumCapacity() {
        return maximumCapacity;
    }

    public int getCurrentSize() {
        return currentSize;
    }

    public void setCurrentSize(int currentSize) {
        this.currentSize = currentSize;
    }



    public static int genFileId(){return fileID; }
    
    public static ServerStorage getInstance(){
        return SERVER_STORAGE;
    }
    
    public void setOnline(String id, Socket socket){
        
        activeUsers.put(id, true);
    }
    public void setOffline(String id){
        
        activeUsers.put(id, false);
    }
    
    public boolean getOnline(String id){
        return activeUsers.get(id);
    }

    public String getPassword(String id){
        return passwordtable.get(id);
                
    }
    
    public  String getServerStorageLocation(){
        return SERVER_STORAGE_LOCATION;
    }
}







class FileInfo{
    private String fileId;
    private String senderId;
    private String receiverId;
    private int fileSize;
    private String fileName;
    private int totalChunks;
    private int chunkSize;
   
    
    public String getFileId() {
        return fileId;
    }
    public void setFileId(String fileId) {
        this.fileId = fileId;
    }
    public String getSenderId() {
        return senderId;
    }
    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }
    public String getReceiverId() {
        return receiverId;
    }
    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }
    public int getFileSize() {
        return fileSize;
    }
    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }
    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getTotalChunks() {
        return totalChunks;
    }

    public int getChunkSize() {
        return chunkSize;
    }
    
    
    
    
  
    public FileInfo(String fileId, String senderId, String receiverId,
            int fileSize, String fileName, int totalChunks, int chunkSize) {

        this.fileId = fileId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.fileSize = fileSize;
        this.fileName = fileName;
        this.totalChunks = totalChunks;
        this.chunkSize = chunkSize;
    }
}
