package Server;

import DataLinkLayer.GoBackN_Protocol;
import HybridChunk.HybridChunk;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by rafid on 23/9/2017.
 */
public class ServerReadThread implements Runnable {
    private Socket socket;
    private String ID;
    private String recipientID;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;
    private String currentFileName;
    private long currentFileSize;
    private ServerLogWindow serverLogWindow;
    ServerReadThread(Socket socket, ObjectInputStream ois, ObjectOutputStream oos, String ID,
                     ServerLogWindow serverLogWindow) {
        this.socket = socket;
        this.objectInputStream = ois;
        this.objectOutputStream = oos;
        this.ID = ID;
        this.serverLogWindow = serverLogWindow;
        Thread thread = new Thread(this);
        thread.start();
    }
    @Override
    public void run() {
        try {
            while (true) {
                socket.setSoTimeout(0);
                String msg = (String) objectInputStream.readObject();
                serverLogWindow.appendToLog("Client(ID: " + ID + "): " + msg + "\n");
                if(msg.compareTo("Disconnecting") == 0) {
                    serverLogWindow.appendToLog(socket.getInetAddress() + ", " +
                            socket.getPort() + " with ID: "
                            + ID + " disconnected\n");
                    Server.removeConnection(ID);
                    objectInputStream.close();
                    objectOutputStream.close();
                    socket.close();
                    break;
                }
                else if(msg.startsWith("Check LogIn")) {
                    recipientID = msg.substring(12);

                    if(Server.checkLogIn(recipientID)) {
                        objectOutputStream.writeObject("Recipient is logged in. Proceed.");
                    }
                    else {
                        objectOutputStream.writeObject("Recipient is not logged in. Stop.");
                    }
                }
                else if(msg.startsWith("File Name")){
                    currentFileName = msg.substring(10);
                }
                else if(msg.startsWith("File Size")){
                    currentFileSize = Long.parseLong(msg.substring(10));
                    serverLogWindow.appendToLog(
                            "ID: " + ID + " wants to send file " + currentFileName
                                    + " with size " + currentFileSize + "\n"
                    );
                    if(Server.checkOverflow(currentFileSize))
                        objectOutputStream.writeObject("No available space in server.");
                    else {
                        objectOutputStream.writeObject("Enough space available in server.");
                        objectOutputStream.writeObject("Random Chunk " + Server.randomChunkSize());
                        objectOutputStream.writeObject("Start Sending File");
                        String fileID = recipientID + "_" + Server.getStartFileID() + "_" + ID;
                        Server.setStartFileID(Server.getStartFileID() + 1);
                        Server.setCurrentStoredSize(Server.getCurrentStoredSize() + currentFileSize);
                        objectOutputStream.writeObject("File ID " + fileID);
                        HybridChunk hybridChunk = new HybridChunk();
                        hybridChunk.setFileName(currentFileName);
                        Server.addToBuffer(fileID, hybridChunk);
                    }
                }
                else if(msg.startsWith("Sending file with File ID")) {
                    String fileID = msg.substring(26);

                    GoBackN_Protocol gbn = new GoBackN_Protocol(objectInputStream, objectOutputStream,
                            serverLogWindow, fileID);

                    int showFrame = JOptionPane.showConfirmDialog(null,
                            "Do you want to display frames?", null, JOptionPane.YES_NO_OPTION);
                    if(showFrame == JOptionPane.YES_OPTION) gbn.setShowFrames(true);
                    else gbn.setShowFrames(false);
                    ArrayList<byte[]> fileChunks = gbn.receiveFrames();
                    if(currentFileSize == Server.calculateSize(fileChunks)) {
                        Server.setAllChunkPresent(fileID);
                        Server.addToReceiver(recipientID, fileID);
                        objectOutputStream.writeObject("File received successfully.");
                        for(byte[] payloads : fileChunks) {
                            Server.addToBuffer(fileID, payloads);
                        }
                    }
                    else {
                        objectOutputStream.writeObject("File size don't match.");
                        Server.removeFromBuffer(fileID);
                        Server.setCurrentStoredSize(Server.getCurrentStoredSize() - currentFileSize);
                    }
                }
                else if(msg.compareTo("Want to receive file") == 0) {
                    if(Server.getReceivers().containsKey(ID)){

                        new SendFileServer(objectInputStream, objectOutputStream,
                                socket, ID, serverLogWindow).send();
                    }
                    else {
                        objectOutputStream.writeObject("No file to receive");
                    }
                }
            }
        } catch (IOException e) {
            serverLogWindow.appendToLog(e.toString() + ": cannot write to output stream" +
                    " or read from input stream\n");
        } catch (ClassNotFoundException e) {
            serverLogWindow.appendToLog(e.toString() + ": cannot cast to the desired type\n");
        }
    }
}


class SendFileServer {
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private String receiverID;
    private ArrayList<String> sentAndDeniedList;
    private ServerLogWindow serverLogWindow;
    private Socket socket;

    SendFileServer(ObjectInputStream ois, ObjectOutputStream oos,
                   Socket socket, String receiverID, ServerLogWindow serverLogWindow) {
        this.ois = ois;
        this.oos = oos;
        this.socket = socket;
        this.receiverID = receiverID;
        this.serverLogWindow = serverLogWindow;
        sentAndDeniedList = new ArrayList<>();
    }
    void send() {
        ArrayList<String> fileIDs = Server.getFileIDsFromReceiver(receiverID);
        String msg;
        try {
            for (String fileID : fileIDs) {
                int secondUnderscore = fileID.lastIndexOf('_');
                String sender = fileID.substring(secondUnderscore + 1);
                oos.writeObject(sender + " wants to send a file");
                oos.writeObject("File Name: " + Server.getFileBuffer().get(fileID).getFileName());
                serverLogWindow.appendToLog(
                        sender + " wants to send a file named "
                                + Server.getFileBuffer().get(fileID).getFileName() + ", File ID " + fileID
                                + " to " + receiverID + "\n"
                );
                msg = (String)ois.readObject();

                serverLogWindow.appendToLog("Client(ID: " +
                        receiverID + "): " + msg + "\n");
                if(msg.compareTo("Do not send this file") == 0) {
                    ArrayList<byte[]> chunks = Server.getChunks(fileID);
                    Server.setCurrentStoredSize(Server.getCurrentStoredSize()
                            - Server.calculateSize(chunks));
                    Server.removeFromBuffer(fileID);
                    sentAndDeniedList.add(fileID);
                    continue;
                }
                else if(msg.compareTo("Cancel this file") == 0) {
                    continue;
                }
                oos.writeObject("File ID: " + fileID);
                ArrayList<byte[]> chunks = Server.getChunks(fileID);
                GoBackN_Protocol gbn = new GoBackN_Protocol(ois, oos,
                        8, chunks, socket, serverLogWindow, fileID, 5000);
                int showFrame = JOptionPane.showConfirmDialog(null,
                        "Do you want to display frames?", null, JOptionPane.YES_NO_OPTION);
                if(showFrame == JOptionPane.YES_OPTION) gbn.setShowFrames(true);
                else gbn.setShowFrames(false);
                gbn.sendFrames();

                oos.writeObject("No chunk left. Task complete.");
                Server.setCurrentStoredSize(Server.getCurrentStoredSize()
                        - Server.calculateSize(chunks));
                Server.removeFromBuffer(fileID);
                sentAndDeniedList.add(fileID);
            }
            oos.writeObject("No file to receive");
            for(String ID : sentAndDeniedList) {
                Server.removeFromReceivers(receiverID, ID);
            }
            if(Server.getReceivers().get(receiverID).size() == 0)
                Server.removeFromReceivers(receiverID);
        } catch (IOException e) {
            serverLogWindow.appendToLog(e.toString() + ": cannot write to output stream" +
                    " or read from input stream\n");
            for(String ID : sentAndDeniedList) {
                Server.removeFromReceivers(receiverID, ID);
            }
            if(Server.getReceivers().get(receiverID).size() == 0)
                Server.removeFromReceivers(receiverID);
        } catch (ClassNotFoundException e) {
            serverLogWindow.appendToLog(e.toString() + ": cannot cast to the desired type\n");
            for(String ID : sentAndDeniedList) {
                Server.removeFromReceivers(receiverID, ID);
            }
            if(Server.getReceivers().get(receiverID).size() == 0)
                Server.removeFromReceivers(receiverID);
        }
    }
}