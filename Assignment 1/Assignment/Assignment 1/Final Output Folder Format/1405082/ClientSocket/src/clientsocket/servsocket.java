/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientsocket;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Dipannoy_Dip
 */
public class servsocket {
 
    public static int workerThreadCount = 0;
    public static int  writethreadcount = 0;
    public static int cnt=0;
    public static char [] array = new char[5000];
    
    
    
    public static ArrayList<WorkerThread> list = new ArrayList<WorkerThread>();
    public static ArrayList<WriteThread> write_list = new ArrayList<WriteThread>();
    public static ArrayList<Long> caps_array=new ArrayList<Long>();
    public static ArrayList<FileInfo> File_array =new ArrayList<FileInfo>();
    public static long capacity = 1000000000;
    
    private static BufferedReader br = null;
    private static PrintWriter pr = null;
    
    
    public static String de_stuff(String s)
    {
        String out=new String();
        int counter=0;
        
        for(int i=0;i<s.length();i++)
        {
            if(s.charAt(i)=='1')
            {
                out=out+'1';
                counter++;
            }
            else
            {
                out=out+'0';
                counter=0;
            }
            if(counter==5)
            {
                if((i+2)!=s.length())
                {
                    out=out+s.charAt(i+2);
                    
                }
                else
                {
                    out=out+'1';
                    
                }
                i=i+2;
                counter=1;
            }
                
        }
        return out;
        
    }
    
    public static int count_one(String s)
    {
        int cnt=0;
        for(int i=0;i<s.length();i++)
        {
            if(s.charAt(i)=='1')
            {
                cnt++;
            }
        }
        return cnt;
    }
   
    
    
    

    public static void main(String args[]) {
        int id = 1;
         

        try {
            ServerSocket ss = new ServerSocket(5555);
            System.out.println("Server has been started successfully.");

            while (true) {
                String tsk;
                Socket s = ss.accept();
               
                //tsk=br.readLine();
                if(id%2==1)
                {
                    
                    WorkerThread wt = new WorkerThread(s);
                    list.add(wt);
                    Thread t = new Thread(wt);
                    t.start();
                    workerThreadCount++;
                    System.out.println("Client [" + id + "] is now connected. No. of worker threads = " + workerThreadCount);
                id++;
                }
                
                else if(id%2==0)
                {
                    WriteThread wt = new WriteThread(s);
                    write_list.add(wt);
                    Thread t = new Thread(wt);
                    t.start();
                    writethreadcount++;
                    System.out.println("Client [" + id + "] is now connected. No. of worker threads = " + writethreadcount);
                id++;
                }
                else
                {
                    continue;
                }
                
                //WorkerThread wt = new WorkerThread(s);
                //list.add(wt);
                
                
                
                
            }
        } catch (Exception e) {
            System.err.println("Problem in ServerSocket operation. Exiting main.");
        }
        //System.out.println("ddddd");
    }
    
    
    public static class FileInfo
    {
        public static String Filename;
        public static int fileid;
        public static String recvrid;
        public static String sendid;
        public static long filesz;
        
        
        
        
        public FileInfo(String Filename,int fileid,String recvrid,String sendid,long filesz)
        {
            this.Filename=Filename;
            this.fileid=fileid;
            this.recvrid=recvrid;
            this.sendid=sendid;
            this.filesz=filesz;
        }
    }

    
    
   public static class WorkerThread implements Runnable
{
	public Socket socket;
	private InputStream is;
	private OutputStream os;
	
	public String sender_id ;
        public String receiver_id;
        private String con_port;
        private String filename;
        private long filesize;
        
        
        public WorkerThread(Socket s)
        {
            this.socket = s;
            try
		{
			this.is = this.socket.getInputStream();
			this.os = this.socket.getOutputStream();
		}
		catch(Exception e)
		{
			//System.err.println("Sorry. Cannot manage client [" + id + "] properly.");
		}
        }
	
	public WorkerThread(Socket s, String sender_id,String receiver_id,String con_port)
	{
		this.socket = s;
                //this.con_port=5555;
                
                //this.id=idd;
		
		
		
		//this.id = id;
	}
	
	public void run()
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(this.is));
		PrintWriter pr = new PrintWriter(this.os);
                for(int i=0;i<5000;i++)
                {
                    array[i]='0';
                }
                String chkstr=new String(array);
                StringBuilder myName = new StringBuilder(chkstr);
		
		//pr.println("Your id is: " + this.id);
		pr.flush();
		
		String idd1;
                String idd2;
                String prt;
                String flnm;
                String flen;
                int file_id = 0;
                int max_chunk = 0;
                long flnth;
                int flag=0;
                Random rn =new Random();
                
                
            try {
                 br = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
		pr = new PrintWriter(this.socket.getOutputStream());
                //pr.println("provide necessary inputs");
                //pr.flush();
                
                idd1=br.readLine();
                this.sender_id=idd1;
                idd2=br.readLine();
                this.receiver_id=idd2;
                prt=br.readLine();
                this.con_port=prt;
                System.out.println(idd1);
                String msg=null;
                int i;
                Scanner input = new Scanner(System.in);
           
                
                for( i=0;i<list.size()-1;i++)
                {
                    //System.out.println(list.get(i).con_port);
                    //System.out.println("hi");
                    //System.out.println(list.get(i).sender_id);
                  if(idd1.equals(list.get(i).sender_id))
                  {
                      //System.out.println("kkk");
                      if(con_port != list.get(i).con_port)
                      {
                           msg="logged in";
                           list.remove(i);
                           
                      }
                      else
                      {
                           msg="enter file name";
                      }
                  }
                       //System.out.println(list.size());
                }
                if(i==list.size()-1)
                {
                  msg="enter file name";  
                }
                
               
                pr.println(msg);
                pr.flush();
                
                
                flnm=br.readLine();
                this.filename=flnm;
                flen=br.readLine();
                flnth=Long.parseLong(flen);
                System.out.println(flnth);
                
                long l=0;
                
                
                for(int b=0;b<caps_array.size();b++)
                {
                   l=l+caps_array.get(b);
                    
                }
                
                //int file_id;
                
                if((l+flnth)<capacity)
                {
                    file_id=rn.nextInt()%1000000;
                     max_chunk=rn.nextInt()%10000;
                     
                    pr.println("You can start sending file entering receiver id");
                    pr.println(file_id);
                    pr.println(1000);
                    
                    pr.flush();
                }
                
        
                
                System.out.println(file_id);
                
                String strln=br.readLine();
                long stln=Long.parseLong(strln);
                long sz=stln/16;
                
                
                
                for(int k=0;k<8;k++)
                {
                    String rcv=br.readLine();
                    int lt=rcv.length();
                    int ll=lt-37;
                    int seqn=Integer.parseInt(rcv.substring(8,24), 2);
                    if(seqn==cnt+1)
                    {
                        String dstf=de_stuff(rcv.substring(24, 24+ll));
                        if(count_one(dstf)==Integer.parseInt(rcv.substring(24+ll,24+ll+5),2))
                        {
                            myName.setCharAt(seqn-1,'1');
                            cnt++;
                        }
                        
                        else
                            break;
                    }
                    else
                        break;
                    
                }
                
                
                
                
                try {
                    //strRecv = br.readLine();					//These two lines are used to determine
                    int filesize = Integer.parseInt(flen);		//the size of the receiving file
                    byte[] contents = new byte[1000];

                    FileOutputStream fos = new FileOutputStream("cttt.jpg");
                    BufferedOutputStream bos = new BufferedOutputStream(fos);
                    InputStream is = socket.getInputStream();

                    int bytesRead = 0;
                    int total = 0;			//how many bytes read

                    while (total != filesize) //loop is continued until received byte=totalfilesize
                    {
                        bytesRead = is.read(contents);
                        total += bytesRead;
                        bos.write(contents, 0, bytesRead);
                        
                        //flag=1;
                        
                        
                        pr.println(flag);
                        pr.println("The chunk has been received successfully");
                        pr.flush();
                        
                    }
                    bos.flush();
                    
                    
                    

                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println("Could not transfer file.");
                }
                
                FileInfo fi=new FileInfo(flnm,file_id,idd2,idd1,flnth);
                File_array.add(fi);
                
                
                //thread_receiver tr=new  thread_receiver(idd2,flnm, flen,idd1);
                //System.out.println("dddd");
                //System.out.println(tr.get_soc());
                
           
                    
                    
                        
                       
                   

                


              
                //list.add(new WorkerThread(socket,sender_id,receiver_id,con_port) );
                
                //System.out.println(idd);
            } catch (IOException ex) {
                Logger.getLogger(WorkerThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
		
}
  public static class WriteThread implements Runnable
  {
      public static Socket socket;
      private InputStream is;
      private OutputStream os;
      public String sender_id ;
      public String receiver_id;
      private String con_port;
      public WriteThread(Socket s)
        {
            this.socket = s;
            try
		{
			this.is = this.socket.getInputStream();
			this.os = this.socket.getOutputStream();
		}
		catch(Exception e)
		{
			//System.err.println("Sorry. Cannot manage client [" + id + "] properly.");
		}
        }

        @Override
        public void run() {
            BufferedReader br = new BufferedReader(new InputStreamReader(this.is));
            PrintWriter pr = new PrintWriter(this.os);
            System.out.println("okkkk");
            
            pr.flush();
            String receiver_id;
            try
            {
               receiver_id=br.readLine();
               for(int i=0;i<=File_array.size()-1;i++)
                {
                    if(File_array.get(i).recvrid.equals(receiver_id))
                    {
                        System.out.println("dipppp");
                        String fle_name=File_array.get(i).Filename;
                        System.out.println(fle_name);
                        File fle=new File(fle_name);
                        long filelength=fle.length();
                        System.out.println(filelength);
                        pr.println(filelength);
                        pr.flush();
                        
                        
                        try {
                            
                            //File file = new File("capture.jpg");
                            FileInputStream fis = new FileInputStream(fle);
                            BufferedInputStream bis = new BufferedInputStream(fis);
                            
                            OutputStream os = socket.getOutputStream();
                            System.out.println("dibya");
                            byte[] contents;
                            
                            /*long fileLength = file.length();
            pr.println(String.valueOf(fileLength));		//These two lines are used
            pr.flush();	*/								//to send the file size in bytes.

                            long current = 0;
                            //String flag="1";

                            //long start = System.nanoTime();
                            //while (flag.equals("1")) {
                            //flag="0";
                            while (current != filelength) {
                                int size = 10000;
                                if (filelength - current >= size) {
                                    current += size;
                                } else {
                                    size = (int) (filelength - current);
                                    current = filelength;
                                }
                                contents = new byte[size];
                                bis.read(contents, 0, size);
                                os.write(contents);

                                //timer.
                                //timer.scheduleAtFixedRate(task,1000,1000);
                                //System.out.println("Sending file ... "+(current*100)/fileLength+"% complete!");
                            }
                            //}

                            os.flush();
                            System.out.println("File sent successfully!");
                        } catch (Exception e) {
                            System.err.println("Could not transfer file.");
                        }
                    }
                }
               
            }
            catch(Exception e)
            {

            }
                    
            
            
            
            //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
      
  }
}
