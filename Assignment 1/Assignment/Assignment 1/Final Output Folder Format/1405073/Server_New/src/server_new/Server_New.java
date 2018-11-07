/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server_new;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/**
 *
 * @author user
 */
public class Server_New {

    
    static ArrayList<Integer> online=new ArrayList<>();
    static ArrayList<Integer> pending=new  ArrayList<>();//reciever der list thakbey
    static byte [] bufferArray=new byte[1024*1024*5];
    static long buffStart=0;
    
    
    
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        online.add(2);
        ServerSocket server = new ServerSocket(6789);
        System.out.println("ServerSocket created, port no 6789");
        
        Socket giveAway;
        while(true)
        {
            giveAway=server.accept();
            new ServerThread(giveAway).start();
        }
    }
    
    synchronized static void InsertIntoOnline(int id)
    {
        online.add(id);
    }
    synchronized static void RemoveFromOnline(int id)
    {
        online.remove(id);
    }
    synchronized static boolean FindInOnline(int id)
    {
        return online.contains(id);
    }
    
}
