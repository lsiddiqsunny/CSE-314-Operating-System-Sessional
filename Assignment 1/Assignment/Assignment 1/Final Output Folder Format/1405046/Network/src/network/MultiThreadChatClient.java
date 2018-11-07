package network;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import static java.lang.System.out;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MultiThreadChatClient implements Runnable {
  private static Socket clientSocket = null;
  private static PrintStream os = null;
  private static DataInputStream is = null;
  private static BufferedReader inputLine = null;
  private static boolean closed = false;
  public static int file_id = 0;
  public static String cur = null; 
  public  int chunk_size = 1000;
  public String responseLine;
  public String file_name;
  public static void main(String[] args) {

    int portNumber = 44444;
    String host = "localhost";
    if (args.length < 2) {
      System.out
          .println("Usage: java MultiThreadChatClient <host> <portNumber>\n"
              + "Now using host=" + host + ", portNumber=" + portNumber);
    } else {
      host = args[0];
      portNumber = Integer.valueOf(args[1]).intValue();
    }
    try {
      clientSocket = new Socket(host, portNumber);
      inputLine = new BufferedReader(new InputStreamReader(System.in));
      os = new PrintStream(clientSocket.getOutputStream());
      is = new DataInputStream(clientSocket.getInputStream());
    } catch (UnknownHostException e) {
      System.err.println("Don't know about host " + host);
    } catch (IOException e) {
      System.err.println("Couldn't get I/O for the connection to the host "
          + host);
    }
      if (clientSocket != null && os != null && is != null) {
          new Thread(new MultiThreadChatClient()).start();
          while (!closed) {
          }
          closeconnection();
        
    }
        
  }

    private static void closeconnection() {
      try {
          os.close();
          is.close();
          clientSocket.close();
      } catch (IOException ex) {
          Logger.getLogger(MultiThreadChatClient.class.getName()).log(Level.SEVERE, null, ex);
      }
    }

  public void run() {
    try {
      while ((responseLine = is.readLine()) != null) {
        System.out.println(responseLine);
        if(responseLine.endsWith("id")){
            os.println(inputLine.readLine().trim());
        }
        else if(responseLine.endsWith("files??")){
            os.println(inputLine.readLine().trim());
        }
        else if(responseLine.endsWith("recieved")){
            os.println("finished sending");
            System.out.println("line 88 sent finished sending");
            os.flush();
        }
        else if(responseLine.equalsIgnoreCase("Enter File Name: ")){
            file_name = inputLine.readLine().trim();
            //name = sc.nextLine();
            file_name.trim();
            System.out.println("got it name is " + file_name);
            os.println(file_name);
            os.flush();
            SendFile(file_name);
        }
        
        else if(responseLine.endsWith(" wanna recieve?")){
            os.flush();//here are some problems ans = yes
            //String ans = inputLine.readLine().trim();
            String ans = inputLine.readLine().trim();// problem here 
            os.println(ans);
            System.out.println("comes in 100 hurrah and ans  = " + ans);
            os.flush();
            if(ans.equalsIgnoreCase("yes")){
                String na = is.readLine().trim();
                file_id++;
                recievefile(na);
            }
        }
        /*
        else if(responseLine.endsWith("the chunk size")){
            String ans = responseLine;
            ans.replace("is the chunk size", "");
            chunk_size = Integer.getInteger(ans);
            System.out.println("comes for chunk size and size = "+chunk_size);
        }
        */
        else if (responseLine.indexOf("*** Bye") != -1)
          break;
      }
      //closed = true;
    } catch (IOException e) {
      System.err.println("IOException:  " + e);
    }
  }

    private void SendFile(String name) throws IOException {
        System.out.println("Yee comes here");
        File f = new File("/home/mahim/testing/client/"+file_name);
        int size = (int) f.length();
        System.out.println("got it size is " + size);
        os.flush();
        os.println(size);
        System.out.println("Size sent");
        
        File myFile = new File("/home/mahim/testing/client/"+name);
        byte[] buffer = new byte[(int) myFile.length()];
        FileInputStream fis;
        OutputStream out = null;
        os.println("chunk?");
          String ans = responseLine;
          String newans  = is.readLine().trim();
          System.out.println("line 146 ans = "+ newans);
            //chunk_size = Integer.getInteger(newans);
            chunk_size = 0;
            for(int i = 0; i <newans.length(); i++){
                chunk_size*=10;
                chunk_size+= newans.charAt(i)-'0';
                
            }
            System.out.println("comes for chunk size and size = "+chunk_size);
          
        try {
            fis = new FileInputStream(myFile);//to readd file
            BufferedInputStream in = new BufferedInputStream(fis);
            os.flush();
            try {
                in.read(buffer,0,buffer.length);//total file byte array in buffer now
                out = clientSocket.getOutputStream();
                System.out.println("comes here");
                System.out.println("in stream");
                System.out.println("chunk size is" + chunk_size);
                System.out.println("Sending files");
                
                String update = null;
                String cur = null;
                int counter = 1;
                boolean successfull = true;
                long startTime = System.currentTimeMillis();
                long elapsedTime = 0L;
                for(int block = 0; block <(buffer.length/chunk_size); block++){
                    //block != 0
                    startTime = System.currentTimeMillis();
                    if(block != 0){
                        update = block+1+"is recieved";
                        cur = is.readLine().trim();
                    }
                    if( block == 0 || update.equalsIgnoreCase(cur)){
                        out.write(buffer,(block*chunk_size),  chunk_size);
                        os.println("block "+ block+1+ "sent");
                        os.flush();
                        System.out.println("block "+ counter+" sent");
                        counter++;
                    }
                    out.flush();
                    if(elapsedTime<=15*1000){
                        elapsedTime = (new Date()).getTime() - startTime;
                        continue;
                    }
                    if(elapsedTime>15*1000){
                        successfull = false;
                        break;
                    }
                }
                is.readLine().trim();
                os.flush();
                if(successfull){
                    os.println("finished sending");
                    System.out.println("Finished sending yes line 161");
                    os.println("finished sending");
                }
                else{
                    os.println("uncomplete finished sent");
                    System.out.println("not Finished sending yes line 161");
                    os.println("uncompletefinished sending");
                }
                //closeconnection();
                //clientSocket.close();
            } catch (IOException ex) {
                Logger.getLogger(MultiThreadChatClient.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MultiThreadChatClient.class.getName()).log(Level.SEVERE, null, ex);
        }
      
    }

    private void recievefile(String nam) {
        int maxsize = 99999;
        int byteread;
        int current = 0;
        try {
            InputStream isa = clientSocket.getInputStream();
            File test = new File("/home/mahim/testing/reciever/"+file_id+nam);
            file_id++;
            test.createNewFile();
            FileOutputStream fos = new FileOutputStream(test);
            BufferedOutputStream out = new BufferedOutputStream(fos);
            //int chunk_size = 1000; + rand.nextInt(19)+1;from 10 to 30
            /*
            os.flush();
            String ck = is.readLine().trim();
            if(ck.equalsIgnoreCase("chunk?")) {
            System.out.println("in stream and size = " + chunk_size);
            os.print(chunk_size);
            System.out.println("file size sent");
            }
            */
            byte[] buffer = new byte[chunk_size];
            int chunk_id = 0;
            String update =  null, cur = null;
            while ((byteread = is.read(buffer, 0, buffer.length)) >0) {
                //out.write(buffer, chunk_id*chunk_size, byteread);
                out.write(buffer);
                out.flush();
                os.flush();
                
            }
            return;
            //closeconnection();
        } catch (IOException ex) {
            Logger.getLogger(clientThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
}