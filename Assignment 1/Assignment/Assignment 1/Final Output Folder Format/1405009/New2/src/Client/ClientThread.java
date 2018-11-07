package Client;

import Utilities.ClientInfo;
import Utilities.ClientUtilities;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by Rupak on 9/29/2017.
 */
public class ClientThread implements Runnable {
    public ClientInfo clientInfo;
    public Socket socket;
    public ClientUtilities client;

    public ClientThread(int ID,String path) {
        try {
            socket = new Socket("localhost", 33330);
            client = new ClientUtilities(socket);
        } catch (IOException e) {
            e.printStackTrace();
        }

        clientInfo = new ClientInfo();
        clientInfo.setClientPort(socket.getLocalPort());
        clientInfo.setIPaddrress(socket.getInetAddress());
        clientInfo.setSid(ID);
        try {
            clientInfo.setPath(new File("./"+path).getCanonicalPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        new Thread(this).start();
    }

    @Override
    public void run() {
        String s="";
        while(true) {
            s = String.valueOf(clientInfo.getSid());
            try {
                client.outToServer.writeBytes(s + "\n");
                System.out.println("1st step : Passing SID to server"); //..............
                client.outToServer.writeBytes(clientInfo.getPath() + "\n");
                int x = client.inFromServer.read();
                System.out.println(x);
                if (x == 1) {
                    System.out.println("Passing SID successful");
                    break;
                }
                else {
                    System.out.println("This ID is already used. ");
                    Scanner sc =new Scanner(System.in);
                    System.out.print("Enter Student ID : ");
                    int ID = sc.nextInt();
                    clientInfo.setSid(ID);
                    System.out.print("Enter Your Folder Name :");
                    String path = sc.next();
                    clientInfo.setPath(new File("./"+path).getCanonicalPath());
                }
            } catch (IOException e) {
                System.out.println("Error in logIn");
            }
        }
        System.out.println("Client connected :" + s + " IP : " + clientInfo.getIPaddrress() + " Port: " + clientInfo.getClientPort() + " is now connected ");


        while (true) {
            System.out.println("Want to send?(Press 1)");
            Thread m = new Thread(new readFromUser(client));
            Thread n = new Thread(new readFromServer(client, clientInfo));
            m.start();
            n.start();
            while(m.isAlive() || n.isAlive()) {continue;}
        }
    }

    public static void main(String[] args) {
        Scanner sc =new Scanner(System.in);
        System.out.print("Enter Student ID : ");
        int ID = sc.nextInt();
        System.out.print("Enter Your Folder Name :");
        String s = sc.next();
        new ClientThread(ID,s);
    }
}





