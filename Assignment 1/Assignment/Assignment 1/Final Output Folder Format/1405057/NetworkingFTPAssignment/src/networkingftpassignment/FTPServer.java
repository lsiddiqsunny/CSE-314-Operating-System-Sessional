package networkingftpassignment;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author HP
 */

public class FTPServer {
    //sockets
    private static ServerSocket myServerSocket;
    private static Socket myClientSocket;
    public static ArrayList<MemberInformation> onlineList = new ArrayList<MemberInformation>();
    public static long serverCapacity;
    public static long serverUsed = 0;
    private static DataInputStream dataIn;
    private static DataOutputStream dataOut;
    public static int count = 0;
    public static ArrayList<FileBufferList> currentBuffer = new ArrayList<FileBufferList>();
    
    public static void main(String[] args){
        
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter Max Server Capacity (bytes) : ");
        serverCapacity = sc.nextLong();

        try {
            System.out.println("Starting Server with port number 12345 and capacity " + serverCapacity + " bytes ...");
            myServerSocket = new ServerSocket(12345);
            System.out.println("Server started with port number 12345 and capacity " + serverCapacity + " bytes ...");
        } catch (IOException ex) {
            Logger.getLogger(FTPServer.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Server aborted...");
        }
        
        while(true){
            System.out.println("Waiting for clients...");
            try {
                myClientSocket = myServerSocket.accept();
                
                FTPServerUtilities threadObj = new FTPServerUtilities(myClientSocket);
                Thread t = new Thread(threadObj);

                t.start();
                
            } catch (IOException ex) {
                Logger.getLogger(FTPServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        
    }
    
    public static long getUnusedCapacity(){
        return serverCapacity-serverUsed;
    }
    
    public static void setServerUsed(long size){
        serverUsed += size;
    }
    
    public static void freeServer(long size){
        serverUsed -= size;
    }

    public static Boolean LookIfOnline(String stId) {
        
        //returns true if one student is trying to connect multiple times
        //else return false
        
        for(int i=0; i<onlineList.size(); i++){
            if(onlineList.get(i).getStudentId().equals(stId)){
                return true;
            }
        }
        return false;
    }
    
    public static void addBuffer(String senderStudentId, String receivertStudentId, String fileName, long size, String fileId){
        currentBuffer.add(new FileBufferList(senderStudentId, receivertStudentId, fileName, size, fileId));
    }
    
    public static void removeUser(String stId){
        for(int i=0; i<onlineList.size(); i++){
            if(onlineList.get(i).getStudentId().equals(stId)){
                onlineList.remove(i);
            }
        }
    }
    
    public static void printCurrentBuffer(){
        for(int i=0; i<currentBuffer.size(); i++){
            System.out.println("Sender : "+currentBuffer.get(i).getSenderStudentId()+" Receiver : "+currentBuffer.get(i).getReceivertStudentId());
        }
    }

    public static void printOnlineUsers(){
        for(int i=0; i<onlineList.size(); i++){
            System.out.println(onlineList.get(i).studentId);
        }
    }
}
