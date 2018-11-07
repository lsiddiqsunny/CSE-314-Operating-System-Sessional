package util;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;


public class ConnectionUtillities {
    public Socket sc;
    public ObjectInputStream ois;
    public ObjectOutputStream oos;
    public String s;
    public char c;
    public long s1;
    public int bread=0;
    public Object a;
    
    public ConnectionUtillities(String host, int port){
        try {
            sc=new Socket(host,port);
            //in=sc.getInputStream();
            //out=sc.getOutputStream();
            oos=new ObjectOutputStream(sc.getOutputStream());
            ois=new ObjectInputStream(sc.getInputStream());
         //   dos=new DataOutputStream(out);
         //   dis=new DataInputStream(in);
            System.out.println("Connection successfully created");
        } 
        catch(Exception e)
        {
            System.out.println("Connection couldnt be successfully created");
        }
        
    }
    
    public ConnectionUtillities(Socket socket){
        try {
            sc=socket;
      //      in=sc.getInputStream();
      //      out=sc.getOutputStream();
            oos=new ObjectOutputStream(sc.getOutputStream());
            ois=new ObjectInputStream(sc.getInputStream());
           // dos=new DataOutputStream(out);
           // dis=new DataInputStream(in);
            System.out.println("Connection successfully created");
        }
        catch(Exception e)
        {
            System.out.println("Connection couldnt be successfully created");
        }
    }
    
    public void write(Object o){
        try {
            oos.writeObject(o);
            oos.flush();
            //dos.writeUTF(str);
           // System.out.println(str);
        } catch (IOException ex) {
        //    System.out.println("string write  e somossa");
        }
    }
    
    
    public void write(byte [] b,int a,int c){
        try {
            //System.out.println(new String(b));
            
            sc.getOutputStream().write(b,0,c);
            sc.getOutputStream().flush();
        //    out.write(b,0,c);
        //    out.flush();
        //    System.out.println(new String(b));
        } catch (IOException ex) {
            
        }
    }
    
    public String read(){
        try {
            a=ois.readObject();
            
           //s=dis.readUTF();
           //System.out.println(s);
        } 
        catch (IOException ex) {
           //System.out.println("io ex");
        }catch (ClassNotFoundException ex){
           // System.out.println("c ex");
        }
       //System.out.println(String.valueOf(a));
       return String.valueOf(a);
       //return s;
    }
    
      public long readd(){
        try {
            a=ois.readObject();
            
           //s1=dis.readLong();
        } 
        catch (IOException ex) {
          //  System.out.println("string read  e somossa");
        }catch(ClassNotFoundException ex){
            
        }
 
        return Long.parseLong(String.valueOf(a));
    }
    
    public void writee(Object b){
        try {
            oos.writeObject(b);
            oos.flush();
            //oos.reset();
          // dos.writeLong(a);
        } 
        catch (IOException ex) {
          //  System.out.println("string read  e somossa");
        }
    }
    public int read(byte [] b,int c,int d){
        try {
            
            bread = sc.getInputStream().read(b,c,d);
            
            // bread = in.read(b,c,d);
        } 
        catch (IOException ex) {
            
        }
        return bread;
    }
}