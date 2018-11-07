package server;

import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.util.Objects;

public class commThread extends Thread {

    private Socket socket;
    private String clientID;

    commThread(Socket clientSocket){
        this.socket = clientSocket;
    }

    public void run(){
        DataInputStream inputStream = null;
        DataOutputStream outputStream = null;
        PrintWriter serverLog = null;

        //File received = null;
        //setup connection
        while(inputStream == null){
            try {
                outputStream = new DataOutputStream(socket.getOutputStream());
                inputStream = new DataInputStream(socket.getInputStream());
            } catch (IOException e) {
                System.out.println(e);
            }
        }

        try {
            serverLog = new PrintWriter(new FileWriter("serverLog.txt"));
        } catch (IOException e) {
            System.out.println(e);
        }

        //login
        try {
            clientID = inputStream.readUTF();
            while(server.activeList.contains(clientID)){
                outputStream.writeUTF(clientID + " is already logged in\nEnter correct ID:");
                clientID = inputStream.readUTF();
            }
            outputStream.writeUTF("Confirmed");
            System.out.println(clientID + " signed in");
            serverLog.println(clientID + " signed in");
            server.activeList.addElement(clientID);

        } catch (IOException e) {
            System.out.println(e);
        }


        String message;
        //running
        while(true){

            //System.out.println("waiting to receive mode");
            try {
                message = inputStream.readUTF();
                //file send block
                if(Objects.equals(message, "send")){
                    System.out.println(clientID + " wants to send file");
                    String fileName = inputStream.readUTF();
                    long fileSize = inputStream.readLong();
                    System.out.println(clientID + " wants to send " + fileName + " of size " + fileSize);
                    serverLog.println(clientID + " wants to send " + fileName + " of size " + fileSize);

                    if(server.available - fileSize >= 0){
                        server.available -= fileSize;
                        outputStream.writeUTF("ready");
                        System.out.println("ready");
                        //String fileString = utils.frameUtils.receiveFile(fileSize, inputStream, outputStream, serverLog);
                        byte[] fileData = utils.frameUtils.receiveFile(fileSize, inputStream, outputStream, serverLog);
                        FileOutputStream fileOutputStream = new FileOutputStream(clientID+ "-" + fileName);
                        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);

                        //byte[] fileData = new BigInteger(fileString, 2).toByteArray();

                        if(fileData[0] !=0 )bufferedOutputStream.write(fileData, 0, (int)fileSize);
                        else{
                            bufferedOutputStream.write(fileData, 1, (int)fileSize);
                        }

                        serverLog.println("File received");
                        bufferedOutputStream.flush();
                        fileOutputStream.flush();
                        bufferedOutputStream.close();
                        fileOutputStream.close();
                    }
                    else outputStream.writeUTF("Not enough server space");

                }

                //logout
                else if(Objects.equals(message, "exit")){
                    break;
                }

            } catch (IOException e) {
                System.out.println(e);
            }

            //System.out.println("out of try catch");

        }

        server.activeList.remove(clientID);
        //close connection
        try {
            outputStream.close();
            inputStream.close();
            socket.close();
        } catch (IOException e) {
            System.out.println(e);
        }

    }

}
