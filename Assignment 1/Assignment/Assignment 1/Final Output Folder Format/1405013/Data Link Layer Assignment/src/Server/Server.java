package Server;

import HybridChunk.HybridChunk;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;

/**
 * Created by rafid on 22/9/2017.
 */
public class Server implements Runnable {
    private static long maxSize;
    private static Hashtable<String, Socket> connections;
    private static int startFileID;
    private static Hashtable<String, HybridChunk> fileBuffer;
    private static long currentStoredSize;
    private static Hashtable<String, ArrayList<String>> receivers;
    private static ServerSocket serverSocket;
    private ServerLogWindow serverLogWindow;

    Server(ServerLogWindow serverLogWindow) {
        try {
            serverSocket = new ServerSocket(30010);
        } catch (IOException e) {
            e.printStackTrace();
        }
        connections = new Hashtable<>();
        fileBuffer = new Hashtable<>();
        receivers = new Hashtable<>();
        startFileID = 0;
        currentStoredSize = 0;
        maxSize = 400000000;
        this.serverLogWindow = serverLogWindow;
        Main.getWindow().setOnCloseRequest(event -> {
            try {
                serverSocket.close();
            } catch (IOException e1) {
                serverLogWindow.appendToLog("Error closing server\n");
            }
        });
        Thread thread = new Thread(this);
        thread.start();
    }

    synchronized static void addToReceiver(String recipientID, String fileID) {
        if(receivers.containsKey(recipientID))
            receivers.get(recipientID).add(fileID);
        else {
            ArrayList<String> tmp = new ArrayList<>();
            tmp.add(fileID);
            Server.receivers.put(recipientID, tmp);
        }
    }

    synchronized static void removeFromReceivers(String receiverID, String fileID) {
        receivers.get(receiverID).remove(fileID);
    }

    synchronized static void removeFromReceivers(String receiverID) {
        receivers.remove(receiverID);
    }

    synchronized static ArrayList<String> getFileIDsFromReceiver(String receiverID) {
        return receivers.get(receiverID);
    }

    synchronized static ArrayList<byte[]> getChunks(String fileID) {
        return fileBuffer.get(fileID).getChunk();
    }

    synchronized static boolean checkAllChunkPresent(String fileID) {
        return fileBuffer.get(fileID).isAllChunksPresent();
    }

    synchronized static void setAllChunkPresent(String fileID) {
        fileBuffer.get(fileID).setAllChunksPresent(true);
    }

    synchronized static void removeConnection(String ID){
        connections.remove(ID);
    }

    synchronized static int getStartFileID() {
        return startFileID;
    }

    synchronized static void setStartFileID(int startFileID) {
        Server.startFileID = startFileID;
    }

    synchronized static void addToBuffer(String fileID, HybridChunk hybridChunk) {
        fileBuffer.put(fileID, hybridChunk);
    }

    synchronized static void addToBuffer(String fileID, byte[] chunk){
        fileBuffer.get(fileID).getChunk().add(chunk);
    }

    synchronized static void removeFromBuffer(String fileID){
        Server.fileBuffer.remove(fileID);
    }

    synchronized static Hashtable<String, HybridChunk> getFileBuffer() {
        return fileBuffer;
    }

    synchronized static long getCurrentStoredSize() {
        return currentStoredSize;
    }

    synchronized static void setCurrentStoredSize(long currentStoredSize) {
        Server.currentStoredSize = currentStoredSize;
    }

    static Hashtable<String, ArrayList<String>> getReceivers() {
        return receivers;
    }
    synchronized static boolean checkLogIn(String ID) {
        return connections.containsKey(ID);
    }

    synchronized static boolean checkOverflow(long size) {
        return (size + currentStoredSize > maxSize);
    }

    synchronized static long calculateSize(ArrayList<byte[]> fileChunks) {
        long size = 0;
        for (byte[] fileChunk : fileChunks) {
            size += fileChunk.length;
        }
        return size;
    }

    synchronized static int randomChunkSize() {
        Random random = new Random();
        return random.nextInt()%100000 + 100000;
    }


    @Override
    public void run() {
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                String ID = (String) objectInputStream.readObject();
                if (checkLogIn(ID)) {
                    objectOutputStream.writeObject("User already logged in.");
                    serverLogWindow.appendToLog("Connection " +
                            "denied to " + socket.getInetAddress() + ", "
                            + socket.getPort() + "\n");
                } else {
                    objectOutputStream.writeObject("Log In successful.");
                    connections.put(ID, socket);
                    serverLogWindow.appendToLog(
                            socket.getInetAddress() + ", " + socket.getPort() + " connected" +
                                    " with ID: " + ID + "\n"
                    );
                    new ServerReadThread(socket, objectInputStream, objectOutputStream, ID, serverLogWindow);
                }

            } catch (IOException e) {
                serverLogWindow.appendToLog(e.toString() + ": cannot write to output stream" +
                        " or read from input stream\n");
            } catch (ClassNotFoundException e) {
                serverLogWindow.appendToLog(e.toString() + ": cannot cast to the desired type\n");
            }
        }
    }
}
