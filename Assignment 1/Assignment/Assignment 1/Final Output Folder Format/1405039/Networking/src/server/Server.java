package server;

import java.net.*;
import java.io.*;
import java.util.*;
import utility.ConnectionSetup;
import utility.Frame;

public class Server {
    
    public static ServerSocket servSocket;
    public static HashMap<String,ConnectionSetup>clientList;
    public static int bufferSize = 10000000;
    public static int totalBufferOccupied = 0;
    public static HashMap<Integer, String>fileList;
    public static int fileIDs=0;
    public static HashMap<Integer, Frame[]>fileBuffer;

    public static void main(String[] args) {
        clientList=new HashMap<String, ConnectionSetup>();
        fileList = new HashMap<Integer, String>();
        fileBuffer = new HashMap<Integer, Frame[]>();
        try {
            servSocket=new ServerSocket(22222);
            while(true)
            {
                Socket clientSocket=servSocket.accept();
                ConnectionSetup connection=new ConnectionSetup(clientSocket);
                new Thread(new LoginClient(connection)).start();     
            }
        } catch (Exception ex) {
            System.out.println("Error in server.");
        }
    }
}
