
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author H M Tanjil
 */
public class from_client {
    public static void main(String args[]){
        try {
            OutputStream out=null;
            InputStream in =null;
            InputStream in2=null;
            Scanner scan=new Scanner(System.in);
            Socket sc=new Socket("127.0.0.1",1201);
            System.out.println("In client connection established.....");
            
            File file=new File("G:\\mydocu.txt");
            long len=file.length();
            
            
            System.out.println((len));
            int chunk=(int)(len/100)+1;
            System.out.println("number of chunk"+chunk);
            int id,toid;
            int ara[]=new int[122];
            System.out.println("For log in");
            System.out.println("Enter Your Student Id");
            id=scan.nextInt();
            ara[id%1000]=1;
            System.out.println("Enter the Student Id whom you want to send");
            toid=scan.nextInt();
            
            byte[] element=new byte[1024*16];
            
            out=sc.getOutputStream();
            //int l=(int) len;
            out.write((int)len);
            out.write(chunk);
            in2=sc.getInputStream();
            int flag=in2.read();
            System.out.println(len);
            in=new FileInputStream(file);
            in2=sc.getInputStream();
            int cnk=100;
            int start=0;
            int end=100;
            int count;
            while((count=in.read(element))>0){
                
                while(in2.read()==1){
                out.write(element,start,count);}
                //out.write(element, start, end);
                start=end+1;
                end+=100;
            }
            /*int count;
            while((count=in.read(element))>0){
                out.write(element,0,count);
            }*/
            System.out.println("done");
        } catch (IOException ex) {
            Logger.getLogger(from_client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
}
