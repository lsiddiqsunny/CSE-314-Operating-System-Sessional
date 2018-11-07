/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package from_sever;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author H M Tanjil
 */
public class From_sever {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            // TODO code application logic here
            InputStream in=null;
            OutputStream out=null;
            OutputStream out2=null;
            ServerSocket ss=new ServerSocket(1201);
            System.out.println("Server is waiting for connection.....");
            Socket s=ss.accept();
            System.out.println("connection Established.....");
            in=s.getInputStream();
            int l=in.read();
            int chnk=in.read();
            
            int flag=0;
            if(l>1024*16){
                System.out.println("Maximum Size Overloded");
            }
            else{
                flag=1;
            }
            System.out.println(l+"and"+chnk);
            out=s.getOutputStream();
            out.write(flag);
            out=new FileOutputStream("G:\\mudocu1.txt");
            byte[] element=new byte[1024*16];
            int start=0;
            int end=100;
            int count=1;
            out2=s.getOutputStream();
            while(count!=chnk-1){
                
                out.write(element, start, end);
                start=end+1;
                end+=100;
                out2.write(flag);
                count++;
            }
            /*int count;
            while((count=in.read(element))>0){
                out.write(element,0,count);
            }*/
            System.out.println("finish");
        } catch (IOException ex) {
            Logger.getLogger(From_sever.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
