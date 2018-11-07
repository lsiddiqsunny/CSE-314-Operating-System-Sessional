package Client;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client implements Runnable {
    private String clientAddress;
    private String studentID;
    private int port;
    private Socket socket;
    public Client(){
        try {
            clientAddress = InetAddress.getLocalHost().toString();
            this.socket=new Socket("127.0.0.1",8010);
            this.port = socket.getPort();
        } catch (Exception e) {

        }
        new Thread(this).start();
    }


    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        BufferedReader b = null;
        PrintWriter pw = null;
        try {
            b = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            pw = new PrintWriter(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        String s="";
        while (true) {
            System.out.print("Enter studentID : ");
            s = scanner.next();
            studentID = s;
            pw.println(s);
            pw.flush();
            String s1 = null;
            try {
                s1 = b.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (s1.equals("-1")) {
                System.out.println("User already logged in\nTry with different ID");
            }
            else break;
        }
        Thread t1 = new Thread(new FileReceive(socket,b, pw));
        Thread t2 = new Thread(new FileSend(socket,pw, b));
        System.out.println("Press S to send a file");

    }

    public static void main(String[] args) { new Client(); }
}
