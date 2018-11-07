package com.company;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;


public class Server {

    public static void main(String[] args) throws IOException {

        ArrayList<String> iplist = new ArrayList<String>();
        ServerInstance si = new ServerInstance();
        System.out.println("Server:");
        Random rand = new Random();
        int n = rand.nextInt(10) + 1;
        n=10;
        System.out.println("Maximum Cunk Size: " + n);


        for(int i=0;i<10;i+=1) {
            new ServerThread(si,i,n,iplist);
        }



    }
}
