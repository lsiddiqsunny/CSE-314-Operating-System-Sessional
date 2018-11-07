package Offline;

import java.io.BufferedReader;

import java.io.EOFException;
import java.io.File;

import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


class Server{  
    
public static void main(String args[])throws Exception{   

        ServerSocket ss=new ServerSocket(50000); 
        System.out.println ("Waiting for request");
        Socket s=ss.accept();
        System.out.println ("Connected With "+s.getInetAddress().toString());
        ObjectOutputStream dout=new ObjectOutputStream(s.getOutputStream());
        ObjectInputStream din=new ObjectInputStream(s.getInputStream());  
          
        //dout.flush();
        BufferedReader br=new BufferedReader(new InputStreamReader(System.in)); 
        String str="",filename="";  
        try{
      
	
            filename=din.readUTF(); 
            System.out.println("Receiving file: "+filename);
            filename="C:\\Users\\ASUS\\Desktop\\receivedBCS.PNG";
            System.out.println("Saving as file: "+filename);
            long sz=Long.parseLong(din.readUTF());
            System.out.println ("File Size: "+(sz));

            byte b[]=new byte [1024];
            System.out.println("Receiving file..");
            File f=new File(filename);
            f.delete();
            FileOutputStream fos=new FileOutputStream(f,true);
            long bytesRead=0;
            int i=0;
            ArrayList<Boolean> output=new ArrayList<>();
            Stuffing stuffing=new Stuffing();
            do
            {
                
                output=(ArrayList<Boolean>)din.readObject();
                i++;
                b=stuffing.performDestaffing(output);
                System.out.println("received "+i+"th chunk");
                fos.write(b);
                System.out.println("DeStaffed bits:");
                stuffing.printBit(b);
                System.out.println(" ");
                int seq=stuffing.getSeqNum(output);
                dout.writeInt(seq);
                dout.flush();
                bytesRead+=1024;
            }while(bytesRead<sz);
            
            System.out.println("Completed");
            
            long size= f.length();
            
            System.out.println ("Received Size: "+size);
            fos.close(); 
            //dout.close();  	
            s.close();  
        }
        catch(EOFException e)
        {
            e.printStackTrace();
        }
} 
}