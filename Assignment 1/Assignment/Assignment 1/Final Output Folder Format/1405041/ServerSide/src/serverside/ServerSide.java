/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serverside;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class ServerSide {
    
    private ServerSocket ServSock;
    private HashMap <String, Socket> students;
    private HashMap <String, FileInfo> files;
    
    ServerSide()
    {
        try {
            int id = 1;
            System.out.println("Enter maximum server size\n");
            Scanner scan = new Scanner(System.in);
            long max_size = scan.nextLong();
            
            ServSock = new ServerSocket(33333);
            students = new HashMap <String,Socket> ();
            files = new HashMap <String, FileInfo> ();
            System.out.println("Connected\n");
            
            while (true) 
            {
                Socket ClientSock = ServSock.accept();
		ServerThread m = new ServerThread(ClientSock,id,students,files,max_size);
                String text="Client [" + Integer.toString(id) + "] is now connected "+"with IP address "+ClientSock.getRemoteSocketAddress().toString()+"\n";
                System.out.println(text);
                id++;
            }
        }catch(Exception e) {
            System.out.println("Server starts:"+e);
	}
    }
    public static void main(String[] args) {
        ServerSide objServer = new ServerSide();
    }
    
}
