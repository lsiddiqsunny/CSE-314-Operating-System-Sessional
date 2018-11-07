package Client;


import DataLinkLayer.GoBackN_Protocol;
import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by rafid on 23/9/2017.
 */
public class Client {
    private Socket socket;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private int chunkSize;
    private ClientLogWindow clientLogWindow;


    boolean connect(String ID) {
        try {
            socket = new Socket("127.0.0.1", 30010);
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());
            objectOutputStream.writeObject(ID);
            String msg = (String)objectInputStream.readObject();
            if(msg.compareTo("Log In successful.") == 0) {
                return true;
            }
            else if (msg.compareTo("User already logged in.") == 0) {
                return false;
            }
        } catch (IOException e) {
            clientLogWindow.appendToLog(e.toString() + ": cannot write to output stream" +
                    " or read from input stream\n");
            return false;
        } catch (ClassNotFoundException e) {
            clientLogWindow.appendToLog(e.toString() + ": cannot cast to the desired type\n");
            return false;
        }
        return false;
    }

    void closeConnection() {
        try {
            objectOutputStream.writeObject("Disconnecting");
            objectOutputStream.close();
            objectInputStream.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    boolean sendFile(String recipientID, File file) {
        try {
            objectOutputStream.writeObject("Check LogIn " + recipientID);
            String msg = (String)objectInputStream.readObject();
            clientLogWindow.appendToLog("Server: " + msg + "\n");
            if(msg.compareTo("Recipient is not logged in. Stop.") == 0) {
                clientLogWindow.appendToLog("File sending failed\n");
                return false;
            }
            objectOutputStream.writeObject("File Name " + file.getName());
            clientLogWindow.appendToLog("File Name " + file.getName() + "\n");
            objectOutputStream.writeObject("File Size " + file.length());
            clientLogWindow.appendToLog("File Size " + file.length() + "\n");
            msg = (String)objectInputStream.readObject();
            clientLogWindow.appendToLog("Server: " + msg + "\n");
            if(msg.compareTo("No available space in server.") == 0) {
                clientLogWindow.appendToLog("File sending failed\n");
                return false;
            }
            msg = (String)objectInputStream.readObject();
            clientLogWindow.appendToLog("Server: " + msg + "\n");
            if(msg.startsWith("Random Chunk ")){
                chunkSize = Integer.parseInt(msg.substring(13));
            }
            msg = (String)objectInputStream.readObject();
            clientLogWindow.appendToLog("Server: " + msg + "\n");
            if(msg.compareTo("Start Sending File") == 0) {
                msg = (String)objectInputStream.readObject();
                clientLogWindow.appendToLog("Server: " + msg + "\n");
                String fileID = msg.substring(8);
                return new SendFileClient(file, fileID, chunkSize,
                        objectInputStream, objectOutputStream, socket, clientLogWindow).send();
            }
        } catch (IOException e) {
            clientLogWindow.appendToLog(e.toString() + ": cannot write to output stream" +
                    " or read from input stream\n");
            return false;
        } catch (ClassNotFoundException e) {
            clientLogWindow.appendToLog(e.toString() + ": cannot cast to the desired type\n");
            return false;
        }
        return false;
    }

    void receiveFile() {
        try {
            objectOutputStream.writeObject("Want to receive file");
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (true) {
            try {
                String msg = (String) objectInputStream.readObject();
                if (msg.compareTo("No file to receive") == 0){
                    clientLogWindow.appendToLog("Server: " + msg + "\n");
                    return;
                }
                else if (msg.contains("wants to send a file")) {
                    String fileNameMsg = (String)objectInputStream.readObject();

                    //get the file name
                    String currentFileName = fileNameMsg.substring(11);

                    //ask if want to receive the file
                    String optionPaneMsg = msg + " named " + currentFileName;
                    int ans = JOptionPane.showConfirmDialog(null, optionPaneMsg);
                    if(ans == JOptionPane.YES_OPTION) {
                        //get the destination directory
                        JFileChooser jFileChooser = new JFileChooser();
                        jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                        jFileChooser.showOpenDialog(null);
                        File directory = jFileChooser.getSelectedFile();
                        String directoryPath = directory.getPath();


                        objectOutputStream.writeObject("Start Sending");

                        // get file ID
                        String receivingFileIDMsg = (String)objectInputStream.readObject();
                        String receivingFileID = receivingFileIDMsg.substring(9);


                        //get frames
                        GoBackN_Protocol gbn = new GoBackN_Protocol(objectInputStream, objectOutputStream,
                                clientLogWindow, receivingFileID);
                        int showFrame = JOptionPane.showConfirmDialog(null,
                                "Do you want to display frames?", null, JOptionPane.YES_NO_OPTION);
                        if(showFrame == JOptionPane.YES_OPTION) gbn.setShowFrames(true);
                        else gbn.setShowFrames(false);
                        ArrayList<byte[]> frames = gbn.receiveFrames();

                        //construct complete path
                        String pathName;
                        if(directoryPath.charAt(directoryPath.length() - 1) != '\\')
                            pathName = directoryPath + "\\" + currentFileName;
                        else pathName = directoryPath + currentFileName;

                        //constructing the file
                        FileOutputStream fileOutputStream = new FileOutputStream(pathName, true);
                        for(byte[] frame : frames) {
                            fileOutputStream.write(frame);
                            fileOutputStream.flush();
                        }
                        fileOutputStream.close();
                    }
                    else if(ans == JOptionPane.NO_OPTION){
                        objectOutputStream.writeObject("Do not send this file");
                    }
                    else {
                        objectOutputStream.writeObject("Cancel this file");
                    }
                }
            } catch (IOException e) {
                clientLogWindow.appendToLog(e.toString() + ": cannot write to output stream" +
                        " or read from input stream\n");
            } catch (ClassNotFoundException e) {
                clientLogWindow.appendToLog(e.toString() + ": cannot cast to the desired type\n");
            }
        }
    }


    void setClientLogWindow(ClientLogWindow clientLogWindow) {
        this.clientLogWindow = clientLogWindow;
    }
}
