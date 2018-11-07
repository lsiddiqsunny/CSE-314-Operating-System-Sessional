package offline1_server;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.lang.*;
import java.util.Arrays;
import java.util.List;



class common {
   
    public static int  max_size = 999999999;
    public static int  fileid = 0;
    public static int  counter = 0;
    public static Frame[] buffer =  new Frame[12];
    public static ArrayList<Frame> objlist = new ArrayList<Frame>();
    public static int  bufferfree = 102400;
    public static List<String> filelists = new ArrayList<String>();
    public static HashMap<Integer, Socket> hmap = new HashMap<Integer, Socket>();
    public static HashMap<Integer, String> info = new HashMap<Integer, String>();
}
public class offline1_server 
{
 
    private static Socket socket;
   
    public static void main(String[] args)
    { 
        try
        {   
            int port = 25000;
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server Started and listening to the port 25000");
 
            while(true)
            {
                socket = serverSocket.accept();
                InputStream is = socket.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                int number = Integer.parseInt(br.readLine());
                System.out.println("Student id received  is "+number);
                Socket getsocket = common.hmap.get(number);
                if(getsocket != null){
                        System.out.println("You are already logged in ");
                        socket.close();
                        break;
                }
                else{
                         common.hmap.put(number, socket);
                         ClientThread wt = new ClientThread(socket, number);
				Thread t = new Thread(wt);
				t.start();
                }
             }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                socket.close();
            }
            catch(Exception e){}
        }
    }
}

class ClientThread implements Runnable
{
        private Socket socket;
	private InputStream is;
	private OutputStream os;
        private HashMap hmap; 
        private int id = 0;
       
        public ClientThread(Socket s, int number)
	{
		this.socket = s;
                try
		{
			this.is = this.socket.getInputStream();
			this.os = this.socket.getOutputStream();
                        
		}
		catch(Exception e)
		{
			System.err.println("Sorry. Cannot manage client [" + id + "] properly.");
		}
		
		this.id = number;
	}
	
	public void run(){
        
                BufferedReader br = new BufferedReader(new InputStreamReader(this.is));
		PrintWriter pr = new PrintWriter(this.os);
		
		String str;
               
                while(true)
		{
                    try
			{
				if( (str = br.readLine()) != null )
				{
					if(str.equals("BYE"))
					{
						System.out.println("[" + id + "] says: BYE. ");
						break; 
					}
					else if(str.equals("SEND"))
					{
                                            try
                                                {
                                                        String rcver = br.readLine();
                                                        String file_name = br.readLine();
                                                        String strRecv = br.readLine();					//These two lines are used to determine
                                                        int filesize=Integer.parseInt(strRecv);	
                                                        
//                                                        if(filesize > common.bufferfree*1024){
//                                                            System.out.println("Too large");
//                                                        }
//                                                        else{
                                                            //file recv
                                                            common.fileid++;
                                                            pr.println(common.fileid);
                                                            pr.flush();
                                                            byte[] contents = new byte[40];
                                                            String[] words=file_name.split("\\.");
                                                            String newfilename = new StringBuilder(words[0]).append("_").append(rcver).append(".").append(words[1]).toString();
                                                            FileOutputStream fos = new FileOutputStream(newfilename);
                                                            BufferedOutputStream bos = new BufferedOutputStream(fos);
                                                            InputStream is = socket.getInputStream();

                                                            int bytesRead = 0; 
                                                            int total=0;
                                                             //how many bytes read
                                                            int chunksize = (int)filesize/10;
                                                            int chunks = (filesize%chunksize == 0)? filesize/chunksize :(filesize/chunksize)+1;
                                                            while(total!=filesize)	//loop is continued until received byte=totalfilesize
                                                            {
                                                                    bytesRead=is.read(contents);
                                                                    total+=bytesRead-10;
                                                                    System.out.println("[" + bytesRead + "] says. "+contents);
                                                                    ////save frame in array
                                                                    byte[] contentswflag = Arrays.copyOfRange(contents, 1, bytesRead-1);
                                                                    System.out.println("[" + bytesRead + "] says. "+contentswflag);
                                                                    
                                                                    
                                                                    String stuffedm = CommonServer.bitDeStuff(contentswflag);
                                                                     
                                    
                                                                    //----------------
                                                                    byte [] newstuff=new byte[stuffedm.length()/8];
                                                                    for(int i=0;i<stuffedm.length()/8;i++){
                                                                       int first=i*8;
                                                                       int last=(i+1)*8;
                                                                       if(last >stuffedm.length()) last = stuffedm.length() -first;
                                                                       String substr=stuffedm.substring(first, last);
                                                                       newstuff[i]= (byte)Integer.parseInt(substr,2);
                                                                    }
                                                                    Frame f = new Frame(newstuff);
                                                                   
                                                                    System.out.println("[" + bytesRead + "] says. "+newstuff);
                                                                    if (f.hasCheckSumError()) {
                                                                       System.out.println("Error"); 
                                                                    }
                                                                    else{
                                                                        
                                                                        common.objlist.add(f);
                                                                        //common.buffer[common.counter] = f;
                                                                        common.counter++;
                                                                        common.bufferfree -=  f.payload.length;
                                                                        bos.write(f.getPayload(), 0, f.payload.length); 
                                                                    }
                                                                    ///------------------
                                                                    
                                                            }
                                                            bos.flush(); 

                                                            common.filelists.add(newfilename);
                                                        
                                                        //}
                                                        
                                                  
                                                }
                                                catch(Exception e)
                                                {
                                                        System.err.println(e);
                                                }
                                            
                                        }else if(str.equals("GET")){
                                                StringBuilder  stringfile = new StringBuilder();
                                                for (String item : common.filelists) { 
                                                    if (item.contains(Integer.toString(this.id))) {
                                                        stringfile.append(item);
                                                    }
                                               }  
                                                String finalString = stringfile.toString();
                                                pr.println(finalString);
                                                pr.flush();
                                                
                                        }
                                        //here we want to send file to receiver 
                               }
                        }
                        catch(Exception e)
			{
				System.err.println("Problem in communicating with the client");
				break;
			}
                        
                }
        }
}
