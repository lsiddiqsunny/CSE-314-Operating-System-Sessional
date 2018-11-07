/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
/**
 *
 * @author User
 */
public class networkutil {
    
  public Socket socket;
        public BufferedOutputStream bos;
	public BufferedInputStream bis;
 public ObjectInputStream ois;
        public ObjectOutputStream oos;
        public networkutil(){}
	public networkutil(String s, int port) {
		try {
			this.socket=new Socket(s,port);  this.socket.setSoTimeout(8000);
                         oos=new ObjectOutputStream(socket.getOutputStream());
			ois=new ObjectInputStream(socket.getInputStream());
			bos=new BufferedOutputStream(socket.getOutputStream());
			bis=new BufferedInputStream(socket.getInputStream());
                       
		} catch (Exception e) {
			System.out.println("In NetworkUtil : " + e.toString());
		}
	}

	public networkutil(Socket s) {
		try {
			this.socket = s; this.socket.setSoTimeout(8000);
                       oos=new ObjectOutputStream(socket.getOutputStream());
			ois=new ObjectInputStream(socket.getInputStream());
			bos=new BufferedOutputStream(socket.getOutputStream());
			bis=new BufferedInputStream(socket.getInputStream());
		} catch (Exception e) {
			System.out.println("In NetworkUtil : " + e.toString());
		}
	}

	public Object read() {
		Object o = null;
		try {
			o=(Object)ois.readObject();
		} catch (Exception e) {
		  //System.out.println("Reading Error in network : " + e.toString());
		}
		return o;
	}
	
	public void write(Object o) {
		try {
			oos.writeObject(o);  
                        oos.flush();
		}catch (IOException e) {
			System.out.println("Writing  Error in network : " + e.toString());
		}
               
	}

	public void closeConnection() {
		try {
			bos.close();
			bis.close();
                        socket.close();
		} catch (Exception e) {
			System.out.println("Closing Error in network : "  + e.toString());
		}
	}
    
}


