package Offline;


import java.io.File;
import java.io.FileInputStream;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.net.Socket;
import java.util.ArrayList;


class Client{  
    static ObjectInputStream din;
    static ObjectOutputStream dout;
    public static void main(String args[])throws Exception{  
        String filename;
        filename="C:\\Users\\ASUS\\Desktop\\BCS.PNG";
      
        Socket s=new Socket("127.0.0.1",50000); 
        
         dout=new ObjectOutputStream(s.getOutputStream()); 
        din=new ObjectInputStream(s.getInputStream());
        
        
        
      
        
        try{
          
                System.out.println("Sending File: "+filename);
                dout.writeUTF(filename);  
                dout.flush();  

                File f=new File(filename);
                FileInputStream fin=new FileInputStream(f);
                long sz=(int) f.length();

                byte b[]=new byte [1024];

                int read;

                dout.writeUTF(Long.toString(sz)); 
                dout.flush(); 

                System.out.println ("Size: "+sz);
                System.out.println ("Buf size: "+s.getReceiveBufferSize());
                   int size=0;
                ArrayList<Boolean> output;
                byte[] a=new byte[1024];
                int seq=0;
                int ack=0;
                while( size< f.length()){
                    
                    fin.read(b,0,1024);
                    seq++;
                    Stuffing stuffing=new Stuffing(b,seq);
                    stuffing.printBit(b);
                    output=stuffing.performStuffing();
                    System.out.println("");
                    System.out.println("Staffed bits:");
                    stuffing.printStaffedbits();
                    //System.out.println("");
                    
                    
                    dout.writeObject(output);
                    dout.flush(); 
                    
                    ack=din.readInt();
                    System.out.println(" ");
                    System.out.println(ack);
                    System.out.println(" ");
                    
                    if (ack==0) {
                        
                        stuffing.printBit(b);
                        output=stuffing.performStuffing();
                        System.out.println("");
                        System.out.println("Staffed bits:");
                        stuffing.printStaffedbits();
                    //System.out.println("");
                    
                    
                        dout.writeObject(output);
                        dout.flush(); 
                        
                    }
                    size+=1024;
                }
                
                
                System.out.println("..ok"); 
                //dout.flush(); 
            
            //dout.writeUTF("stop");  
            System.out.println("Send Complete");
            //dout.flush();  
            
        }
        catch(Exception e)
        {
            e.printStackTrace();
            System.out.println("An error occured");
        }
        din.close();  
        s.close();   
    }
} 
