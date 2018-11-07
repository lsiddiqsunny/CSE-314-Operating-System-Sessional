package filetransmission;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import static java.lang.Thread.sleep;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    
    public static int fileid = 1;
    public static int maxFileSize = 0;
    public static int onlineStudentCount = 0;
    public static Vector<String> onlineStudents = new Vector<String>();
    public static Vector<Packet> todolist = new Vector<Packet>();
   
    
    public static void main(String[] args) {
        
        try {
            
            int id = 0;
            ServerSocket serverSocket = new ServerSocket(22222);
            System.out.println("Server has been started successfully.");
            
            System.out.print("Enter maximum allowed file size : ");
            Scanner sc = new Scanner(System.in);
            maxFileSize = sc.nextInt();
            
            while(true){
                Socket socket = serverSocket.accept();
                ClientThread clientThread = new ClientThread(socket,++id);
                Thread t = new Thread(clientThread);
                t.start();
                onlineStudentCount++;
                System.out.println("Client [" + id + "] is now connected. Total online student number is " + onlineStudentCount + "." );
            }
            
        } catch (IOException ex) {
            System.err.println("Server starting failed! Exiting main.");
        }
    }
    
}

class Packet{
    String sender;
    String receiver;
    String filename;
    int filesize;
    int fileid;
    
    Packet(String sender, String receiver, String filename, int filesize, int fileid){
        this.sender = sender;
        this.receiver = receiver;
        this.filename = filename;
        this.filesize = filesize;
        this.fileid = fileid;
    }
};

class ClientThread implements Runnable{
    
    private int id;
    private String sid;
    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private BufferedReader br;
    private PrintWriter pw;
    
    ClientThread(Socket socket, int id){
        
        this.id = id;
        this.socket = socket;
        try {
            this.inputStream = socket.getInputStream();
            this.outputStream = socket.getOutputStream();
            br = new BufferedReader(new InputStreamReader(this.inputStream));
            pw = new PrintWriter(this.outputStream);
        } catch (IOException ex) {
            System.err.println("Managing Client [" + id + "] failed!");
        }
    }

    @Override
    public void run() {

        try {
            sid = br.readLine();
            
            if(Server.onlineStudents.contains(sid)){
               System.out.println("Student with sid " + sid + " is already connected with another browser.");
               pw.println("close");
               pw.flush();
               Server.onlineStudents.add(sid);
               closeAll();
            }
            
            else{
                System.out.println("Client with id " + id + " has sid " + sid + ".");
                pw.println("open");
                pw.flush();
                Server.onlineStudents.add(sid);
                
                System.out.println("----------------------- Online Students List -----------------------------------");
                for(int i = 0;i<Server.onlineStudents.size();i++) System.out.println(Server.onlineStudents.elementAt(i));
                System.out.println("--------------------------------------------------------------------------------");
                
                
                doSomething();
                
                closeAll();
            }
        } catch (IOException ex) {
            System.err.println("Error in communicating with client [" + id + "]. Client thread will terminate now.");
        }
    }
    
    private void doSomething() {
        while(true){
            try {
                String ans = br.readLine();

                if(ans.equals("receive")){
                    sendfiles();
                }

                ans = br.readLine();

                if(ans.equals("send")){
                    receivefiles();
                }
            } catch (IOException ex) {

            } 
        }
    }
    
    private void closeAll(){
        try {
                this.br.close();
                this.pw.close();
                this.inputStream.close();
                this.outputStream.close();
                this.socket.close();
        } catch (IOException ex) {
            System.err.println("Error in closing client thread socket.");
        }
        
        Server.onlineStudents.remove(sid);
        
        System.out.println("----------------------- Online Students List -----------------------------------");
        for(int i = 0;i<Server.onlineStudents.size();i++) System.out.println(Server.onlineStudents.elementAt(i));
        System.out.println("--------------------------------------------------------------------------------");
        
        Server.onlineStudentCount--;
        
        System.out.println("Client [" + id + "] is terminating. Total online student number is " + Server.onlineStudentCount + ".");
    }

    private void sendfiles() {
        
        System.out.println("File Sending Starts.....");
        
        try{
        
            for(int i=0; i<Server.todolist.size(); i++){
                if(Server.todolist.elementAt(i).receiver.equals(sid)){
                        
                        pw.println("You have a request for a file " + Server.todolist.elementAt(i).filename + " of size " 
                        + String.valueOf(Server.todolist.elementAt(i).filesize) + " from " + Server.todolist.elementAt(i).sender );
                        pw.flush();
                        
                        if(br.readLine().equals("download file.")){
                            
                            Random random = new Random();
                            int chunksize = random.nextInt(Server.maxFileSize - Server.todolist.elementAt(i).filesize) 
                                            + Server.todolist.elementAt(i).filesize ;
                            
                            
                            File file = new File(Server.todolist.elementAt(i).filename);
                            long filesize = file.length();
                            
                            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
                            
                            
                            pw.println(String.valueOf(chunksize));
                            pw.flush();
                            pw.println(String.valueOf(filesize));
                            pw.flush();
                            pw.println(Server.todolist.elementAt(i).filename);
                            pw.flush();
                            System.out.println("File transfer is starting with chunk size " + chunksize);

                            byte[] contents;

                            long current = 0;
                            
                            if(br.readLine().equals("send me")) { }

                            while(current != filesize){
                                if(filesize - current >= chunksize){
                                    current+=chunksize;
                                }
                                else{
                                    chunksize = (int)(filesize - current);
                                    current = filesize;
                                }
                                contents = new byte[chunksize];
                                bis.read(contents,0,chunksize);
                                
                                outputStream.write(contents);
                                outputStream.flush();
                                
                                
                                System.out.println(chunksize + " bytes gone!!!");
                               
                                
                            }
                            
                            Server.todolist.remove(i);
                            i--;
                            Server.maxFileSize += filesize;
                            
                            System.out.println("---------------------- To Do List ----------------------------------");
                            for(int j = 0;j<Server.todolist.size();j++){
                                System.out.println(Server.todolist.elementAt(j).sender + " | " + 
                                        Server.todolist.elementAt(j).receiver + " | " + Server.todolist.elementAt(j).filename
                                        + " | " + Server.todolist.elementAt(j).filesize + " | " + 
                                        Server.todolist.elementAt(j).fileid);
                            }
                            System.out.println("--------------------------------------------------------------------");
                            

                            
                          
                            if(br.readLine().equals("go ahead")){ }
                            
                            bis.close();
                            file.delete();
                            
                            System.out.println("File sent successfully.");
                               
                        }
                    }
                }

                pw.println("you have no more request.");
                pw.flush();
        }
        catch (IOException ex) {
            System.err.println("Error in sending files.");
        }
    }

    private void receivefiles() {
        
            try {
                System.out.println("File Receiving Starts........");
                String receiver = br.readLine();
                System.out.println("Receiver Id: " + receiver);
                
                if(!Server.onlineStudents.contains(receiver)){
                    
                    System.out.println("Receiver is not online.");
                    pw.println("Receiver is not online.");
                    pw.flush();
                 
                }
                else{
                    System.out.println("Receiver is online.");
                    pw.println("Receiver is online. File sending is on progress.");
                    pw.flush();
                    
                    String filename = sid + "_" + Integer.toString(Server.fileid) + "_" + br.readLine() ;
                    int filesize = Integer.parseInt(br.readLine());
                    System.out.println(filesize);
                    
                    if(filesize > Server.maxFileSize){
                        pw.println("Can't send the file. Max " + Server.maxFileSize + " is allowed. And your file size is " + filesize );
                        pw.flush();
                    }
                    else{
                        
                        pw.println("File transfer is starting.");
                        pw.flush();
                        
                        Random random = new Random();
                        int maxchunksize = random.nextInt(Server.maxFileSize-filesize) + filesize;
                        
                        
                        
                        pw.println(String.valueOf(maxchunksize));
                        pw.flush();
                        
                        
                      
                        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filename));
                        
                        
                        int bytesread = 0;
                        int total = 0;
                        Boolean timeout = false;
                        int ack_no = 0;
                        
                        try {
                        
                            while(total != filesize){
                                
                                
                                //System.out.println("mara1");
                                
                                
                                
                                byte[] contents = new byte[maxchunksize + 100];
                                
                                String ex = br.readLine();
                                frame fm = new frame();
                                fm = DLL.debitstuff(ex);
                                
                                if(fm == null) continue;
                                
                                contents = fm.contents;
                                bytesread = fm.bytesread;
                                
                                if(DLL.hasChecksumError(contents, fm.checksum)) continue;
                               
                                //bytesread=inputStream.read(contents);
                                
                                //System.out.println("mara_ajaira");
                                
                                /*long t0,t1;
                                t0=System.currentTimeMillis();
                                do{
                                   t1=System.currentTimeMillis();
                                }while (t1-t0<10);*/
                                
                                //System.out.println("mara2");
                                
                                String ack = DLL.bitstuff(0, fm.seq_no, ++ack_no, fm.contents);

                                pw.println(ack);
                                pw.flush();

                                /*if(br.readLine().equals("stop transmission")){
                                    timeout = true;
                                    break;
                                }*/

                                total+=bytesread;
                                System.out.println(total + " bytes received!!!");
                                bos.write(contents, 0, bytesread);
                                bos.flush();
                                //System.out.println("mara3");

                            }
                        
                        } catch(IOException ex){
                            timeout = true;
                            System.err.println("Problem in communicating with client [" + id + "]. Client Thread will terminate now.");
                            
                        }
                        
                        
                        bos.close();
                        
                        if(timeout){
                            File file = new File(filename);
                            file.delete();
                            System.out.println(filename + " file deleted.");
                        }
                        
                        
                        else {
                            Server.todolist.add(new Packet(sid,receiver,filename,filesize,Server.fileid));
                            
                            System.out.println("---------------------- To Do List ----------------------------------");
                            for(int j = 0;j<Server.todolist.size();j++){
                                System.out.println(Server.todolist.elementAt(j).sender + " | " + 
                                        Server.todolist.elementAt(j).receiver + " | " + Server.todolist.elementAt(j).filename
                                        + " | " + Server.todolist.elementAt(j).filesize + " | " + 
                                        Server.todolist.elementAt(j).fileid);
                            }
                            System.out.println("--------------------------------------------------------------------");
                            
                            Server.fileid++;
                            Server.maxFileSize -= filesize;
                        }
                        
                    }
                }
            } catch (IOException ex) {
                System.err.println("Problem in communicating with client [" + id + "]. Client Thread will terminate now.");
                
            }
            
            
        }
    
    
};
