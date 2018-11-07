package offline1_client;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;
import java.util.*;
import java.net.*;
import java.io.*;
 
public class Offline1_client
{
 
    private static Socket socket;
    private static BufferedReader br = null;
    private static PrintWriter pr = null;
    
    
    public static String FILE_TO_SEND = "";  
    public static int id  ; 
    public  int FILE_SIZE = 6022386; 
        
    public static void main(String args[])
    {
        try
        {
            String host = "localhost";
            int port = 25000;
            InetAddress address = InetAddress.getByName(host);
            socket = new Socket(address, port);
 
            OutputStream os = socket.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os);
            BufferedWriter bw = new BufferedWriter(osw);
            
            Scanner input = new Scanner(System.in);
            System.out.print("student id: ");
            String number = input.nextLine();
            id = Integer.parseInt(number);
            String sendMessage = number + "\n";
            bw.write(sendMessage);
            bw.flush();
            
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
       try
		{
			br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			pr = new PrintWriter(socket.getOutputStream());
		}
		catch(Exception e)
		{
			System.err.println("Problem in connecting with the server. Exiting main.");
			System.exit(1);
		}
		
		Scanner input = new Scanner(System.in);
		String strSend = null, strRecv = null;

        
        while(true)
		{
			try
			{
				strSend = input.nextLine();
			}
			catch(Exception e)
			{
				continue;

                        }
			
			pr.println(strSend);
			pr.flush();
			if(strSend.equals("BYE"))
			{
				System.out.println("Client wishes to terminate the connection. Exiting main.");
				break;
			}
                        else if(strSend.equals("SEND")){
                           try{
                            
                            System.out.print("Enter id of receiver ");
                            String recver =  input.nextLine();
                            pr.println(recver);
                            pr.flush();
                            System.out.print("Enter file path ");
                            
                            FILE_TO_SEND = input.nextLine();
                            File file = new File(FILE_TO_SEND);
                          
                            long fileLength = file.length();
                            pr.println(FILE_TO_SEND);		//These two lines are used
                            pr.flush();

                            pr.println(String.valueOf(fileLength));		//These two lines are used
                            pr.flush();									//to send the file size in bytes.
                            int fileid = Integer.parseInt(br.readLine());
                            System.out.println("file length "+fileLength+" "+ fileid);
                            //////////file send
                            FileInputStream fis = new FileInputStream(file);
                            BufferedInputStream bis = new BufferedInputStream(fis);
                            OutputStream os = socket.getOutputStream();
                            byte[] contents;
                            long current = 0;
                            
                            int sNo = 0;
                            while(current!=fileLength){ 
                                    int size = 30;
                                    sNo++;
                                    if(fileLength - current >= size)
                                            current += size;    
                                    else{ 
                                            size = (int)(fileLength - current); 
                                            current = fileLength;
                                    } 
                                    System.out.println("size"+size);
                                    contents = new byte[size]; 
                                    bis.read(contents, 0, size); 
                                    Packet pak = new Packet(contents, file, fileid, size,Integer.parseInt(recver),id);
                                    Frame f = new Frame (sNo,0,pak.getBytes(),0);
                                    String stuffedm = CommonCodes.bitStuff(f.getBytes());
                                    
                                    //----------------
                                    byte [] newstuff=new byte[stuffedm.length()/8];
                                    for(int i=0;i<stuffedm.length()/8;i++){
                                       int first=i*8;
                                       int last=(i+1)*8;
                                       String substr=stuffedm.substring(first, last);
                                       newstuff[i]= (byte)Integer.parseInt(substr,2);
                                    }
                                    
                                    //-----------------
                                    System.out.println(f.getString());
                                    System.out.println(stuffedm);
                                    System.out.println(newstuff);
                                    //to add 126 both side 
                                    ByteArray b = new ByteArray(newstuff.length + 2);
                                    b.setByteVal(0, (byte) 126); //starting flag byte
                                    b.setAt(1, newstuff);
                                    b.setByteVal(newstuff.length + 1, (byte) 126);//ending flag byte
                                    //System.out.println(b);
                                    System.out.println(b.getBytes());
                                    os.write(b.getBytes());
                                    
                                    //os.write(contents);
                                    //System.out.println("Sending file ... "+(current*100)/fileLength+"% complete!");
                            }   
                            os.flush(); 
                            System.out.println("File sent successfully!");
                        }  
                           catch(Exception e){
                              System.out.println(e);  
                           }
                            
                            
                        }else if(strSend.equals("GET")){
                            try{
                                String filelist = br.readLine();
                                System.out.println(filelist);
                                System.out.println("Enter file name to download");
                                while(true){
                                    String filenametodl = input.nextLine();
                                    if(filenametodl != "END"){

                                        //String  = input.nextLine() ;
                                         
                                    }
                                    
                                }
                            }
                            catch(Exception e){
                                
                           }
                        }
                }
        
        
    }
    
    
   
}