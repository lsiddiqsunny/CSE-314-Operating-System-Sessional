package ServerPackage;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Server {
    ServerSocket theServer;
    long maxbuf;
    long rembuf;
    long fileIdgen;
    ServerInfo inf;

    Server(){
        try {
            theServer = new ServerSocket(33333);
            Scanner sc = new Scanner(System.in);
            System.out.println("Enter Maximum Buffer Size:(Bytes)");
            maxbuf = sc.nextLong();
            rembuf=maxbuf;
            fileIdgen=0;
            inf = new ServerInfo();
            System.out.println(theServer.getInetAddress().getLocalHost());
            System.out.println("Server has started successfully!");

        } catch (IOException e) {
            System.out.println("Error Starting Server");
        }

        while (true){

            try {
                Socket clientSocket = theServer.accept();
                System.out.println("A client found.");
                clientThread ct = new clientThread(this,clientSocket);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }


    public synchronized long getFileIdgen(){
        fileIdgen++;
        return fileIdgen;
    }

    public synchronized boolean inspectRembuf(long filesize){
        if(filesize<=rembuf){
          rembuf=rembuf-filesize;
          return true;
        }
        return false;
    }

    public synchronized void incRembuf(long filesize){
        rembuf=rembuf+filesize;
    }

    public static void main(String[] args) {
        new Server();
    }


}
