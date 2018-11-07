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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author Dipannoy_Dip
 */
public class ClientSocket {
        public static Socket s = null;
        public static int srt=0;
	private static BufferedReader br = null;
	private static PrintWriter pr = null;
        private int id;
        private static InputStream is;
	private static OutputStream os;
        public static int curr=0;
        public static int seq =1;
        public static int initial=0;
        public static int end =16;
        public static int flag=0;
        public static String string = new String();

    public ClientSocket(int id) {
        this.id=id;
    }
    
    public static int second_passed =0;
    
    public static Timer timer = new Timer();
    public static TimerTask task=new TimerTask(){
        @Override
        public void run() {
            if(second_passed > 30)
            {
                //break;
            }
            else{
                
            
             second_passed++; 
            }//To change body of generated methods, choose Tools | Templates.
        }
    };
    
    public static String bit_stuff(String s,int n)
    {
        int count =0;
        for(int i=0;i<s.length();i++)
        {
            char c=s.charAt(i);
            if(c=='1')
            {
               count++; 
            }
            
        }
        String chksum=Integer.toBinaryString(count);
        int ll=chksum.length();
        int r=5-ll;
        for(int i=0;i<r;i++){
            chksum='0'+chksum;
        }
        String st=new String();
        st=s+chksum;
        String stuff_string = new String();
        //stuff_string=st;
         int ct=0;
        for(int i=0;i<st.length();i++)
        {
           
            if(st.charAt(i)=='1')
            {
                ct++;
                stuff_string=stuff_string+'1';
            }
            else
            {
                ct=0;
                stuff_string=stuff_string+'0';
            }
            if(ct==5)
            {
                stuff_string=stuff_string+'0';
                ct=0;
            }
            
        }
        String cs=Integer.toBinaryString(n);
        int l=cs.length();
        int ext=16-l;
        for(int j=0;j<ext;j++)
        {
           cs='0'+cs; 
           
        }
        stuff_string="01111110"+cs+stuff_string+chksum+"01111110";
        
        
        return stuff_string;
        
        
        }
    
    
    public static void go_back(int st,int lt)throws IOException
    {
        
        for(int i=st;i<=lt;i++)
        {
             
            String inpt=bit_stuff(string.substring(st*16,(st*16)+16),seq);
                    pr.println(inpt);
            
        
        
        }
        String acno;
        acno=br.readLine();
        for(int j=st;j<=lt;j++)
        {
            if(acno.charAt(j)=='0')
            {
                go_back(j,lt);
            }
        }
        flag=1;
        
        
    }
    
    
    
    
    
    
        
	

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, ClassNotFoundException {
       try
		{
			s = new Socket("localhost", 5555);
			
			br = new BufferedReader(new InputStreamReader(s.getInputStream()));
			pr = new PrintWriter(s.getOutputStream());
		}
		catch(Exception e)
		{
			System.err.println("Problem in connecting with the server. Exiting main.");
			System.exit(1);
		}
                Scanner input = new Scanner(System.in);
                String task=input.next();
                if(task.equals("send"))
                {
                //pr.println(task);
                int sender_id=input.nextInt();
                pr.println(sender_id);
                int receiver_id=input.nextInt();
                pr.println(receiver_id);
                int port =input.nextInt();
                pr.println(port);
                pr.flush();
                
                is=s.getInputStream();
                os=s.getOutputStream();
                
                br = new BufferedReader(new InputStreamReader(is));
	       pr = new PrintWriter(os);
               System.out.println(br.readLine());
               
               String filename=input.next();
               File file=new File(filename);
               long filelength=file.length();
               System.out.println(filelength);
               
               pr.println(filename);
               pr.println(filelength);
               
               pr.flush();
               
                System.out.println(br.readLine());
                System.out.println(br.readLine());
                int Max_chunk = Integer.parseInt(br.readLine());
                
                long current = 0;
            Path path = Paths.get("C:\\Users\\User\\Documents\\test\\dip.txt");
            byte[] contents = Files.readAllBytes(path);
            //String flag="1";

                         long currnt = 0;
                            
                            String str,frontStr;
                            
                            int oneByte;
                            
                            for(int i = 0; i < contents.length;i++){
                                oneByte = contents[i] & 0xff;
                                str = Integer.toBinaryString(oneByte);
                                System.out.println(str.length());
                                if(str.length()% 8 != 0){
                                     frontStr = new String();
                                     for(int j = 0;j < (8 - str.length()% 8);j++){
                                         frontStr += "0";                                       
                                           
                                     }
                                     str = frontStr + str;
                                
                                }
                                string += str;  
                            }
                        
                
                
              
               try {
            
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);
            OutputStream os = s.getOutputStream();
            
            
            int ln=string.length();
            int remainder=ln%16;
            if(remainder!=0)
            {
            int turn=16-remainder;
            for(int l=0;l<turn;l++)
            {
                string='0'+string;
            }
            }
            pr.println(string.length());
            while(curr<=ln)
            {
                
                for(int i=0;i<8;i++)
                {
                    String inpt=bit_stuff(string.substring(initial, end),seq);
                    pr.println(inpt);
                    
                    
                    
                    
                    initial+=16;
                    end+=16;
                    seq++;
                    
                   
                    
                    
                }
                 String acno;
                 int rslt;
                 int k;
                 acno=br.readLine();
                 for(k=srt;k<srt+8;k++)
                 {
                     if(acno.charAt(k)=='0')
                     {
                         break;
                     }
                         
                 }
                 go_back(k,srt+7);
                 if(flag==1)
                 {
                     curr+=128;
                     continue;
                 }
                     
                 
                    
                    
                    
                
                
                
            }
            
            
            
            
            
            
            
            								//to send the file size in bytes.

                
                            
                            
                            //System.out.println("The converted String " + string);
            
                           
                           
                           
                           
                           

    
                           
                          
                       
                  
            
            os.flush();
            System.out.println("File sent successfully!");
        } catch (Exception e) {
            System.err.println("Could not transfer file.");
        }
                }
                else
                {
                    try {
                        
                       int receive_id=input.nextInt();
                       pr.println(receive_id);
                       pr.flush();
                       String flen=br.readLine();
                    //strRecv = br.readLine();					//These two lines are used to determine
                    int filesize = Integer.parseInt(flen);		//the size of the receiving file
                    byte[] contents = new byte[1000];

                    FileOutputStream fos = new FileOutputStream("dipannoy.jpg");
                    BufferedOutputStream bos = new BufferedOutputStream(fos);
                    InputStream is = s.getInputStream();
                     System.out.println("hiii");

                    int bytesRead = 0;
                    int total = 0;			//how many bytes read

                    while (total != filesize) //loop is continued until received byte=totalfilesize
                    {
                        bytesRead = is.read(contents);
                        total += bytesRead;
                        bos.write(contents, 0, bytesRead);
                        
                        //flag=1;
                        
                        
                        //pr.println(flag);
                        pr.println("The chunk has been received successfully");
                        pr.flush();
                        
                    }
                        System.out.println("hiiii3");
                    
                    bos.flush();
                    
                    
                    

                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println("Could not transfer file.");
                }
                }
               


		//pr.println("Your/ id is: " + this.id);
		//pr.flush();
                
                
                
		
    }
                            
 }
    
                            
    


    
