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
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client {
    
    private static Socket socket;
    private static BufferedReader br;
    private static PrintWriter pw;
    private static Scanner sc;
    private static String sid;
    private static InputStream is;
    private static OutputStream os;
        
    public static void main(String[] args) {
        
        try {

            socket = new Socket("localhost",22222);
            is = socket.getInputStream();
            os = socket.getOutputStream();
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            pw = new PrintWriter(socket.getOutputStream());
            sc = new Scanner(System.in);
            
        } catch (IOException ex) {

            System.err.println("Error in connecting with server. Exiting main.");
            return;
        }
        
        System.out.print("Enter Student Id : ");
        sid = sc.nextLine();
        pw.println(sid);
        pw.flush();

        try {
            if(br.readLine().equals("close")){
                System.out.println("Student with sid " + sid + " is already connected with another browser.");
            }
            
            else{
                System.out.println("Log in with sid " + sid + " is successful.");
                while(true){
                    doSomething();
                }
                
            }
            
            is.close();
            os.close();
            br.close();
            pw.close();
            sc.close();
            socket.close();
            
        } catch (IOException ex) {
            System.err.println("Error in communicating with server.");
        }
    }
    
    private static void doSomething() {

            System.out.println("Do you want to receive files?");
            String ans = sc.nextLine();

            if(ans.toLowerCase().equals("yes")){
                pw.println("receive");
                pw.flush();

                receivefiles();
            }
            else{
                pw.println("don't receive");
                pw.flush(); 
            }

            System.out.println("Do you want to send files?");
            ans = sc.nextLine();

            if(ans.toLowerCase().equals("yes")){
                pw.println("send");
                pw.flush();

                sendfiles();
            }
            else{
                pw.println("don't send");
                pw.flush(); 
            }
            
            //while(true) {}
    }
    
    private static void receivefiles() {
        while(true){
            try {
                String mes = br.readLine();
                if(mes.equals("you have no more request.")){
                    System.out.println(mes);
                    break;
                }
                else{
                    System.out.println(mes);
                    System.out.println("Do you want to download? ");
                    String ans1 = sc.nextLine();
                    if(ans1.toLowerCase().equals("yes")){
                        pw.println("download file.");
                        pw.flush();
                        
                        int chunksize = Integer.parseInt(br.readLine());
                        int filesize = Integer.parseInt(br.readLine());
                        String filename = sid + "_" + br.readLine();

                        
                        
                        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filename));
                        

                        int bytesread = 0;
                        int total = 0;
                        
                        System.out.println("A file named " + filename + " with filesize " + filesize +
                                "bytes is being created with chunksize " + chunksize + " !!!");
                        
                        pw.println("send me");
                        pw.flush();
                        
                        while(total != filesize){
                            
                            byte[] contents = new byte[chunksize];
                            bytesread=is.read(contents);
                            
                            
                            
                            total+=bytesread;
                            System.out.println(total + " bytes received!!!");
                            bos.write(contents, 0, bytesread);
                            bos.flush();
                            
                        }
                        
                        
                        pw.println("go ahead");
                        pw.flush();
                    }
                    else{
                        pw.println("Don't download.");
                        pw.flush();
                    }
                }
                        
                        
            } catch (IOException ex) {
                       
            }
        }
    }
    
    public static void sendfiles(){
        System.out.print("Enter the Receiver Id: ");
        String receiver = sc.nextLine();
            
        pw.println(receiver);
        pw.flush();
            
        try {
            String msg = br.readLine();
            if(msg.equals("Receiver is not online.")){
                System.out.println("Receiver is not online.");
            }
            else{
                System.out.println("Receiver is online. Choose file.");
                    
                System.out.print("Enter the Filename: ");
                String filename = sc.nextLine();
                    
                File file = new File(filename);
                long filesize = file.length();
                
                pw.println(filename);
                pw.flush();
                    
                pw.println(String.valueOf(filesize));
                pw.flush();
                    
                String config = br.readLine();
                if(config.equals("File transfer is starting.")){
                        
                    int chunksize = Integer.parseInt(br.readLine());
                    System.out.println("File transfer is starting with chunk size " + chunksize);

                    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
                    

                    byte[] contents;

                    long current = 0;
                    
                    Boolean timeout = false;
                    
                    int seq_no = 0;
                    String saved_frame = "";
                    Boolean retrans = false;
                    
                    while(current != filesize){
                        if(filesize - current >= chunksize){
                            current+=chunksize;
                        }
                        else{
                            chunksize = (int)(filesize - current);
                            current = filesize;
                        }
                        
                        if(retrans){
                            pw.println(saved_frame);
                            pw.flush();
                            retrans = false;
                        }
                        else{
                            contents = new byte[chunksize];
                            bis.read(contents,0,chunksize);
                            
                            String ex = DLL.bitstuff(1, ++seq_no, 0, contents);
                            saved_frame = ex;
                            pw.println(ex);
                            pw.flush();
                            
                        }    
                            //os.write(contents);
                            //os.flush();
                            
                            socket.setSoTimeout(30000);
                            

                            
                            while(true) {
                                try {
                                    long t1 = System.currentTimeMillis();
                                    //System.out.println(t1);
                                    String str = br.readLine();
                                    long t2 = System.currentTimeMillis();
                                    //System.out.println(t2);
                                    System.out.println("chunk file of size " + chunksize + " send within " + Long.toString(t2-t1) + " ms." );
                                    
                                    frame fm = DLL.debitstuff(str);
                                    if(fm.seq_no != seq_no || fm.type != 0){
                                        System.out.println("Acknowledgement error. Retransmission activated."); 
                                        current -= chunksize;
                                        retrans = true;
                                    }
                                    break;
                                }
                                catch(SocketTimeoutException e) { 
                                    System.out.println("Timeout Exception occured. Retransmission activated."); 
                                    current -= chunksize;
                                    retrans = true;
                                    break; 
                                }
                            }
                            
                            socket.setSoTimeout(0);
                    }
                    
                    
                    
                    if(timeout) System.out.println("Sorry. File Transmission terminated as Chunk file transmission time exceeded 30s.");
                    
                    else System.out.println("File sent successfully.");
                    
                }
                    
                else{
                    System.out.println(config);
                }                    
            }
        } catch (IOException ex) {
            System.err.println("Problem is msg.");
        }
    }
}
