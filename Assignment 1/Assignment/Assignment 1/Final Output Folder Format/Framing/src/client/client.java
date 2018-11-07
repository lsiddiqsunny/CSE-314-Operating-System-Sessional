package client;

import java.io.*;
import java.net.Socket;
import java.util.Objects;
import java.util.Scanner;

public class client {

    public static void main(String args[]) {

        Socket socket = null;
        String clientID;

        PrintWriter clientLog = null;

        try {
            clientLog = new PrintWriter(new FileWriter("clientLog.txt"));
        } catch (IOException e) {
            System.out.println(e);
        }

        DataInputStream dataInputStream = null;
        DataOutputStream dataOutputStream = null;
        Scanner scanner = new Scanner(System.in);


        //connect socket
        while (socket == null){
            try {
                socket = new Socket("localhost", 8888);
                socket.setSoTimeout(5000);

            } catch (Exception e) {
                //System.out.println(e);
                clientLog.println(e);
            }
        }

        //setup I/O stream
        while(dataInputStream == null || dataOutputStream == null){
            try {
                dataInputStream = new DataInputStream(socket.getInputStream());
                dataOutputStream = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                //System.out.println(e);
                clientLog.println(e);
            }
        }

        //login
        try {
            System.out.println("Enter ClientID: ");
            clientID = scanner.nextLine();
            dataOutputStream.writeUTF(clientID);
            String confirmation = dataInputStream.readUTF();
            System.out.println(confirmation);
            while(!Objects.equals(confirmation, "Confirmed")){
                clientID = scanner.nextLine();
                dataOutputStream.writeUTF(clientID);
                confirmation = dataInputStream.readUTF();
                System.out.println(confirmation);
            }

        }catch (Exception e){
            //System.out.println(e);
            clientLog.println(e);
        }

        //send file
        while (true){
            String input, message;
            System.out.println("Enter 'send' to send file or 'exit' to log out");
            input = scanner.nextLine();
            try {
                dataOutputStream.writeUTF(input);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(Objects.equals(input, "send")){
                System.out.println("Enter file name: ");
                String fileName;
                fileName = scanner.nextLine();

                File file = new File(fileName);
                long fileSize = file.length();

                try {
                    dataOutputStream.writeUTF(fileName);
                    dataOutputStream.writeLong(fileSize);
                    message = dataInputStream.readUTF();
                    if(Objects.equals(message, "ready")){
                        utils.frameUtils.sendFile(file, dataOutputStream, dataInputStream, clientLog);
                    }
                    else System.out.println(message);
                } catch (Exception e) {
                    System.out.println(e);
                    clientLog.println(e);
                }


            }
            else if(Objects.equals(input, "exit")){
                break;
            }
        }

        clientLog.close();
        try {
            dataInputStream.close();
            dataOutputStream.close();
            socket.close();
        } catch (IOException e) {
            clientLog.println(e);
        }
    }

}
