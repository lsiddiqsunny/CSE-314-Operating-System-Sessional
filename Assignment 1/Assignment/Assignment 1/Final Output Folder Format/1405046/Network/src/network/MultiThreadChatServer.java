package network;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import static network.MultiThreadChatClient.file_id;
import static network.MultiThreadChatServer.filled_storage;
import static network.MultiThreadChatServer.id_number;
import static network.MultiThreadChatServer.max_storage;
import static network.MultiThreadChatServer.reciever_id;

public class MultiThreadChatServer {
  private static ServerSocket serverSocket = null;
  private static Socket clientSocket = null;
  private static final int maxClientsCount = 10;
  private static final clientThread[] threads = new clientThread[maxClientsCount];
  public static Map<String, Integer> active = new HashMap<String, Integer>();
  public static Map<String, Socket> connected = new HashMap<String, Socket>();
  public static String reciever_id = null;
  public static String id_number = null;
  public int file_id = 0;
  public static int max_storage = 50000;
  public static int filled_storage = 0; 
  public static void main(String args[]) {
    int portNumber = 44444;
    if (args.length < 1) {
      System.out.println("Usage: java MultiThreadChatServer <portNumber>\n" + "Now using port number=" + portNumber);
    } else {
      portNumber = Integer.valueOf(args[0]).intValue();
    }
    try {
      serverSocket = new ServerSocket(portNumber);
    } catch (IOException e) {
      System.out.println(e);
    }
    while (true) {
      try {
        clientSocket = serverSocket.accept();
        int i = 0;
        for (i = 0; i < maxClientsCount; i++) {
          if (threads[i] == null) {
            (threads[i] = new clientThread(clientSocket, threads, (HashMap<String, Integer>) active, (HashMap<String, Socket>) connected)).start();
            break;
          }
        }
        if (i == maxClientsCount) {
          PrintStream os = new PrintStream(clientSocket.getOutputStream());
          os.println("Server too busy. Try later.");
          os.close();
          clientSocket.close();
        }
      } catch (IOException e) {
        System.out.println(e);
      }
    }
  }
}

class clientThread extends Thread {

  private DataInputStream is = null;
  private PrintStream os = null;
  private Socket clientSocket = null;
  private final clientThread[] threads;
  private int maxClientsCount;
  public int chunk_size = 1500;
  
  private String reciever_id = null;
  public String file_name;
  public int file_size = 0;
  Map<String, Integer> active = new HashMap<String, Integer>();
  Map<String, Socket> connected = new HashMap<String, Socket>();
    clientThread(Socket clientSocket, clientThread[] threads, HashMap<String, Integer> hashMap,HashMap<String, Socket> hashMap2 ) {
    this.clientSocket = clientSocket;
    this.threads = threads;
    maxClientsCount = threads.length;
    this.active = hashMap;
    this.connected = hashMap2;
    }
    
  public void run() {
    int maxClientsCount = this.maxClientsCount;
    clientThread[] threads = this.threads;
    String send = null;
    try {
      is = new DataInputStream(clientSocket.getInputStream());
      os = new PrintStream(clientSocket.getOutputStream());
      os.println("Enter your id");
      id_number = is.readLine().trim();
      os.flush();
      
      if(active.get(id_number)!= null){
          os.println("Sorry can not log in. Already logged in. closing connection");
          closeconnection();
      }
      else{
          active.put(id_number,1);
          connected.put(id_number, clientSocket);
          os.println("Connected and logged in");
          os.println("want to send files??");
          os.flush();
          send = is.readLine().trim();
      }
      if(send.equalsIgnoreCase("yes")){
          os.println("Enter reciever id");
          reciever_id = is.readLine().trim();
          if(active.get(reciever_id) == null){
              os.println("Reciever is Not online");
          }
          else{
              os.println("Enter File Name: ");
              //here is going to be file sending start
              file_name = is.readLine().trim();
              String s = is.readLine().trim();
              System.out.println("in line 133 server file size = "+s);
              for(int i = 0; i < s.length(); i++){
                  file_size *=10;
                  file_size += s.charAt(i)-'0';
              }
              if(filled_storage+file_size<= max_storage){
                RecieveFile(file_name);
              }
              else{
                  System.out.println("sorry not enough space");
                  closeconnection();
              }
          }
      }
      
        String line = is.readLine();
        if (line.startsWith("/quit")) {
         
      for (int i = 0; i < maxClientsCount; i++) {
        if (threads[i] == this) {
          threads[i] = null;
        }
      }
      closeconnection();
        }
    } catch (IOException e) {
    }
  }

    private void RecieveFile(String File_name) {
        int maxsize = 99999;
        int byteread;
        int current = 0;
        //byte[] buffer = new byte[maxsize];
      try {
          InputStream isa = clientSocket.getInputStream();
          File test = new File("/home/mahim/testing/server/"+file_id+"_"+reciever_id+File_name);
          test.createNewFile();
          FileOutputStream fos = new FileOutputStream(test);
          BufferedOutputStream out = new BufferedOutputStream(fos);
          Random rand = new Random();
          chunk_size = 1100 + rand.nextInt(500)+1;//from 10 to 30
          
          os.flush();
          String ck = is.readLine().trim();
          if(ck.equalsIgnoreCase("chunk?")) {
              System.out.println("in stream and size = " + chunk_size);
              os.println(chunk_size);
              os.flush();
              System.out.println("file size sent");
          }
          byte[] buffer = new byte[chunk_size];
          int chunk_id = 0;
          String update =  null, cur = null;
          boolean successfull = true;
          long startTime = System.currentTimeMillis();
          long elapsedTime = 0L;
          while ((byteread = is.read(buffer, 0, buffer.length)) >0) {
            //out.write(buffer, chunk_id*chunk_size, byteread);
            startTime = System.currentTimeMillis();
            out.write(buffer);
            update = is.readLine().trim();
            cur = "block "+chunk_id+1+ "sent";
            if(update.equalsIgnoreCase("finished sending")){
                System.out.println("line 171 break from here ");
                break;
            }
            else if(update.equalsIgnoreCase(cur)){
                chunk_id++;
                out.flush();
                os.println(chunk_id+1+"is recieved");
                os.flush();
                System.out.println(chunk_id +" is recieved yes line 175");
                if(elapsedTime<=15*1000){
                        elapsedTime = (new Date()).getTime() - startTime;
                }
                else {
                    successfull = false;
                    break;
                }
            }
            else{
                System.out.println("sup?? ");
                break;
            }
            
        }
          System.out.println("comes here now");
          //os.println("full file recieved");
          os.flush();
          update = is.readLine().trim();
          cur = "finished sending";
          if(cur.equalsIgnoreCase(update)){
              //send to reciever
              PrintStream ps = new PrintStream(connected.get(reciever_id).getOutputStream());
              DataInputStream isn = new DataInputStream(connected.get(reciever_id).getInputStream());
              if(active.get(reciever_id) == null){
                 ps.println("Sorry reciever is offline");
                 String sn ="/home/mahim/testing/server/"+file_id+"_"+reciever_id+File_name;
                 Path path = Paths.get(sn);
                 Files.deleteIfExists(path);
              }
              else{
                ps.println("user " + id_number + " wants to send a file , wanna recieve?");
                //String ans = isn.readLine().trim();
                String ans = "yes";
                ps.flush();
                System.out.println("just before the function");
                
                if(ans.equalsIgnoreCase("yes")){
                    ps.flush();
                    ps.println(File_name);
                    ps.flush();
                    System.out.println("file sending to reciever started line 209");
                    sendToReciever(file_id, File_name);
                    file_id++;
                }
                else{
                 String sn ="/home/mahim/testing/server/"+file_id+"_"+reciever_id+File_name;
                 Path path = Paths.get(sn);
                 Files.deleteIfExists(path);
                }
              }
          }
        //closeconnection();
      } catch (IOException ex) {
          Logger.getLogger(clientThread.class.getName()).log(Level.SEVERE, null, ex);
      }
        
    }

    private void closeconnection() {
      try {
          active.remove(reciever_id);
          connected.remove(reciever_id);
          is.close();
          os.close();
          clientSocket.close();
      } catch (IOException ex) {
          Logger.getLogger(clientThread.class.getName()).log(Level.SEVERE, null, ex);
      }
    }

    public void sendToReciever(int file_id, String File_name) throws IOException {
        System.out.println("Yee comes here");
        File myFile = new File("/home/mahim/testing/server/"+file_id+"_"+reciever_id+ File_name);
        byte[] buffer = new byte[(int) myFile.length()]; // total file
        FileInputStream fis;
        OutputStream out = null;
      try {
          fis = new FileInputStream(myFile);//to readd file
          BufferedInputStream in = new BufferedInputStream(fis);
            try {
                in.read(buffer,0,buffer.length);//total file byte array in buffer now
                out = connected.get(reciever_id).getOutputStream();
                System.out.println("chunk size is" + chunk_size);
                System.out.println("Sending files");
                for(int block = 0; block <(buffer.length/chunk_size); block++){
                    out.write(buffer,(block*chunk_size),  chunk_size);
                    os.flush();
                }
                os.println("finished sending");
                System.out.println("Finished sending");//if you want to delete after sending just comment next 3 lines
                String sn ="/home/mahim/testing/server/"+file_id+"_"+reciever_id+File_name;
                Path path = Paths.get(sn);
                Files.deleteIfExists(path);
                
                   //closeconnection();
                //clientSocket.close();
            } catch (IOException ex) {
                // if reciever goes offline in middle of transmission
                String sn ="/home/mahim/testing/server/"+file_id+"_"+reciever_id+File_name;
                 Path path = Paths.get(sn);
                 Files.deleteIfExists(path);
                Logger.getLogger(MultiThreadChatClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        
      } catch (FileNotFoundException ex) {
          Logger.getLogger(MultiThreadChatClient.class.getName()).log(Level.SEVERE, null, ex);
      }
    }

}