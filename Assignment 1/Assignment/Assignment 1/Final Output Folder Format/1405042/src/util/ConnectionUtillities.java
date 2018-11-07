/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.io.*;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author uesr
 */
public class ConnectionUtillities {
    public Socket sc;
    public ObjectInputStream ois;
    public InputStream is;
    public ObjectOutputStream oos;
    public OutputStream os;
    public DataInputStream dis;
    public DataOutputStream dos;
    
    public ConnectionUtillities(String host, int port){
        try {
            sc=new Socket(host,port);
            oos=new ObjectOutputStream(sc.getOutputStream());
            ois=new ObjectInputStream(sc.getInputStream());
            dis = new DataInputStream(sc.getInputStream());
            dos = new DataOutputStream(sc.getOutputStream());
        } 
        catch(Exception e)
        {
            e.printStackTrace();
        }
        
    }
    
    public ConnectionUtillities(Socket socket){
        try {
            sc=socket;
            oos=new ObjectOutputStream(sc.getOutputStream());
            ois=new ObjectInputStream(sc.getInputStream());
            is = sc.getInputStream();
            os = sc.getOutputStream();
            dis = new DataInputStream(sc.getInputStream());
            dos = new DataOutputStream(sc.getOutputStream());
        } 
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public void write(Object o){
        try {
            oos.writeObject(o);
            oos.flush();
        } catch (IOException ex) {
            Logger.getLogger(ConnectionUtillities.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void write(byte[] bytes, int offset, int limit){
        try {
            oos.write(bytes, offset, limit);
            //oos.reset();
            oos.flush();
        } catch (IOException ex) {
            Logger.getLogger(ConnectionUtillities.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    public void write(byte[] bytes){
        try {
            oos.write(bytes);
            //oos.reset();
            oos.flush();
        } catch (IOException ex) {
            Logger.getLogger(ConnectionUtillities.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    
    public Object read(){
        try {
            Object o=ois.readObject();
            return o;
        } catch (IOException ex) {
            Logger.getLogger(ConnectionUtillities.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ConnectionUtillities.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }


    public int read(byte[] bytes, int offset, int len) {

        int bytesread = 0;
        try {
            //System.out.println("aschi vai");

            //int bytesread = ois.available();

            //System.out.println("check kortesi " + bytesread);

            bytesread = ois.read(bytes, offset, len);

            //System.out.println("aschi vai2");

        } catch (IOException ex) {
            Logger.getLogger(ConnectionUtillities.class.getName()).log(Level.SEVERE, null, ex);
        }

        return  bytesread;

    }


    public void read(byte[] bytes) {
        try {
            //System.out.println("aschi vai");

            //int bytesread = ois.available();

            //System.out.println("check kortesi " + bytesread);

            int bytesread = ois.read(bytes);

            //System.out.println("aschi vai2");

        } catch (IOException ex) {
            Logger.getLogger(ConnectionUtillities.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void close(){
        try {
            sc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
