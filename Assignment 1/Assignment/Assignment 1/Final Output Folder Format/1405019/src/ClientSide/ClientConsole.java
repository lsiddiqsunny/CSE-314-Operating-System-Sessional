package ClientSide;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class ClientConsole {
    public static void main(String[] args) throws IOException {
        Scanner in = new Scanner(System.in);
        System.out.println("Enter the IP Address: ");
        String ip = in.nextLine();
        System.out.println("Enter the Port: ");
        int port = in.nextInt();
        Socket client = new Socket(ip,port);
        System.out.println("Connected to the server");
        System.out.println("Enter the Student ID :");
        in.nextLine();
        String studentID = in.nextLine();

        InetAddress localhost = InetAddress.getLocalHost();
        String addrss = localhost.getHostAddress().trim();//client pc address

        ObjectOutputStream outputStream = new ObjectOutputStream(client.getOutputStream());
        ObjectInputStream inputStream = new ObjectInputStream(client.getInputStream());
        outputStream.writeObject(addrss);
        outputStream.writeObject(studentID);


        //outputStream.writeObject(client);

        ClientReceiver cr = new ClientReceiver(client,outputStream,inputStream,studentID);

    }
}
