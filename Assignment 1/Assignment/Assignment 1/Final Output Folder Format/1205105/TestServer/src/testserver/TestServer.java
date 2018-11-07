/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testserver;

import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;

public class TestServer {

    public static int workerThreadCount = 0;
    public static WorkerThread[] thrd = new WorkerThread[50];

    public static void main(String args[]) {
        int id = 0;

        try {
            ServerSocket ss = new ServerSocket(2222);
            System.out.println("Server has been started successfully.");

            while (true) {
                Socket s = ss.accept();
                thrd[workerThreadCount] = new WorkerThread(s, id);
                Thread t = new Thread(thrd[workerThreadCount]);
                t.start();
                workerThreadCount++;
                System.out.println("Client [" + id + "] is now connected. No. of worker threads = " + workerThreadCount);
                id++;
            }
        } catch (Exception e) {
            System.err.println("Problem in ServerSocket operation. Exiting main.");
        }
    }

}

class WorkerThread implements Runnable {

    private Socket socket;
    private InputStream is;
    private OutputStream os;
    //
    public PrintWriter prr;
    public BufferedReader reader;

    public static String[][] data = new String[20][3];
    public static int count = 0;
    
    
    //
    public static ArrayList<String> users=new ArrayList<String>();
    
    private int id = 0;
    int a = 0;
    //int fll = 0;
    int b = 0;
    //int lin = 0;
    
    int max_size=5000;
    int size=0;
    int chunksize=0;
    int fileid=0;
    String name="";
    String recver="";

    String msg="";
    boolean filesend=false;
    public WorkerThread(Socket s, int id) {
        this.socket = s;
        try {
            this.is = this.socket.getInputStream();
            this.os = this.socket.getOutputStream();
        } catch (Exception e) {
            System.err.println("Sorry. Cannot manage client [" + id + "] properly.");
        }

        this.id = id;
    }
      public static byte[] bitDeStuff(byte[] a) {
        //write code for destuffing here
        String s="";
        for(int i=0;i<a.length;i++)
        {
        s +=("0000000" + Integer.toBinaryString(0xFF & a[i])).replaceAll(".*(.{8})$", "$1");
       
        }
         //System.out.println(s);
        
        String n=s.replaceAll("111110", "11111");
        // System.out.println(n);
         
         int ii=(n.length()/8)*8;
         String o=n.substring(0, ii);
         //System.out.println(o);
         byte[] bval = new BigInteger(o, 2).toByteArray();
        // System.out.println(bval);
         //System.out.println(bval.length);
         //String aaa = new String(bval);
        //System.out.println(aaa);
         return bval;
        //return b;
    }

    public void run() {
        BufferedReader br = new BufferedReader(new InputStreamReader(this.is));
        PrintWriter pr = new PrintWriter(this.os);

        pr.println("Your id is: " + this.id);
        pr.flush();
        //OutputStream osos = this.socket.getOutputStream();
        prr = new PrintWriter(this.os);

        String str;

        while (true) {
            try {
                if ((str = br.readLine()) != null) {
                    //eta lage
                    if (str.equals("log")) {
                        a = 1;
                        continue;
                    }
                    //eta lage
                    if (str.equals("done")) {
                        int cor=0;
                        if(size>max_size){
                            pr.println("overflow");
                            pr.flush();
                            continue;
                        }
                        else
                        {
                            for(int i=0;i<users.size();i++)
                            {
                                String online=users.get(i);    
                                String [] info =online.trim().split(",");
                                String rec=info[0];
                                
                                if(rec.equals(recver)){
                                    cor=1;
                                    break;
                                }
                            }
                            if(cor==0){
                                pr.println("notlogged");
                                pr.flush();
                                continue;
                            }
                        }
                        pr.println("correct");
                        pr.flush();
                        
                        fileid++;
                        pr.println(fileid);
                        pr.flush();
                        
                        Random rn = new Random();
                        chunksize= rn.nextInt()+1; 
                               chunksize=chunksize % size;
                        if(chunksize<0)chunksize=-chunksize;
                        pr.println(chunksize);
                        pr.flush();
                    }
                        
                    if(str.equals("file giving")){
                        filesend=true;
                        msg="";
                        while(true)
                        {
                            str=br.readLine();
                            byte[] new_byts = str.getBytes();
                            byte[]byts=bitDeStuff(new_byts);
                            str=new String(byts);
                           // System.out.println("sizeee "+byts.length);
                            if(str.equals("comp"))
                            {
                                byte[] bytes = msg.getBytes();
                                System.out.println("sizeee "+bytes.length);
                                if(bytes.length<size-10){
                                    pr.println("error");
                                    pr.flush();
                                    msg="";
                                }
                                else{filesend=false;}
                                break;
                            }
                            if (str.equals("timeout")) {
                                  msg="";
                                  break;
                             }
                            msg=msg+str;    
                            //System.out.println("yt "+msg);
                            pr.println("ack");
                            pr.flush();
                        }
                        System.out.println("msg "+msg);
                    }
                    
                    //file name receiver size ekhane neya hoi 
                    if (str.equals("fileinfo")) {
                        str = br.readLine();
                        name= str;
                        //System.out.println(str);
                        str = br.readLine();
                        recver= str;
                        //System.out.println(str);
                        str = br.readLine();
                        size = Integer.parseInt(str);
                       // System.out.println(str);
                        count++;
                        continue;
                    }
                    //eta lage
                    if (a == 1 && !"log".equals(str)) {
                        a = 0;
                        //Path file_pp = Paths.get("C:\\Users\\User\\Desktop\\id.txt");
                        FileReader file_pp = new FileReader("id.txt");
                        reader = new BufferedReader(file_pp);
                        try {
                            String lines;
                            //String data="";
                            // read each line
                            while ((lines = reader.readLine()) != null && !lines.startsWith("*")) {
                                System.out.println(lines);
                                if (lines.equals(str)) {
                                    if(users.contains(lines))
                                    {
                                        pr.println("alreadyloged");
                                        pr.flush();
                                        break;
                                    }
                                    users.add(lines);
                                    pr.println("hasID");
                                    pr.flush();
                                    System.out.println(lines + "loged in");
                                    break;
                                }
                            }
                            if (lines.equals("*")) {
                                pr.println("log in not successful.");
                                 pr.flush();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        continue;
                    }
                    if (str.equals("BYE")) {
                        if(filesend==true){
                            filesend=false;
                            msg="";
                        }
                        str = br.readLine();
                        int i=0;
                        for(i=0;i<users.size();i++)
                        {
                            String online=users.get(i);    
                            String [] info =online.trim().split(",");
                            String rec=info[0];
                            if(rec.equals(str)){
                                    break;
                                }
                        }
                        users.remove(i);
                        System.out.println("[" + id + "] says: BYE. Worker thread will terminate now.");
                        pr.println("TERMINATE");
                        pr.flush();
                        break; // terminate the loop; it will terminate the thread also

                    }
                } else {
                    //System.out.println("[" + id + "] terminated connection. Worker thread will terminate now.");
                    break;
                }
            } catch (Exception e) {
                System.err.println("Problem in communicating with the client [" + id + "]. Terminating worker thread.");
                break;
            }
        }

        try {
            this.is.close();
            this.os.close();
            this.socket.close();
        } catch (Exception e) {

        }

        //TestServer.workerThreadCount--;
        //System.out.println("Client [" + id + "] is now terminating. No. of worker threads = " 
        //		+ TestServer.workerThreadCount);
    }
}
