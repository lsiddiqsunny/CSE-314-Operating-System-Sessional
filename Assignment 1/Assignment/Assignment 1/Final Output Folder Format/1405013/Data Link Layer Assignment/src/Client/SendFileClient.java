package Client;




import DataLinkLayer.GoBackN_Protocol;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by Rafid on 25/9/2017.
 */
class SendFileClient {


    private File file;
    private String fileID;
    private int chunkSize;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private Socket socket;
    private ClientLogWindow clientLogWindow;

    SendFileClient(File file, String fileID, int chunkSize, ObjectInputStream ois,
                   ObjectOutputStream oos, Socket socket, ClientLogWindow clientLogWindow) {
        this.file = file;
        this.fileID = fileID;
        this.chunkSize = chunkSize;
        this.clientLogWindow = clientLogWindow;
        objectInputStream = ois;
        objectOutputStream = oos;
        this.socket = socket;
    }

    boolean send() {
        try {
            ArrayList<byte[]> chunks = new ArrayList<>();
            FileInputStream fileInputStream = new FileInputStream(file);
            long fileSize = file.length();
            long j = fileSize / chunkSize, k;
            //
            //  putting the chunks of given chunkSize in ArrayList
            //
            for (k = 1; k <= j; ++k) {
                byte[] chunk = new byte[chunkSize];
                if(fileInputStream.read(chunk) != chunkSize) {
                    clientLogWindow.appendToLog("Error while reading file\n");
                    return false;
                }
                chunks.add(chunk);
            }
            //
            //  putting the chunk of size less than chunkSize in ArrayList;
            //
            if(fileSize%chunkSize != 0) {
                byte[] chunk = new byte[(int) (fileSize % chunkSize)];
                if (fileInputStream.read(chunk) != fileSize % chunkSize) {
                    clientLogWindow.appendToLog("Error while reading file\n");
                    return false;
                }
                chunks.add(chunk);
            }
            //
            //
            //
            objectOutputStream.writeObject("Sending file with File ID " + fileID);
            GoBackN_Protocol gbn = new GoBackN_Protocol(objectInputStream, objectOutputStream, 8,
                    chunks, socket, clientLogWindow, fileID, 5000);
            gbn.setIntroduce_bad_lost_frame(true);
            int showFrame = JOptionPane.showConfirmDialog(null,
                    "Do you want to display frames?", null, JOptionPane.YES_NO_OPTION);
            if(showFrame == JOptionPane.YES_OPTION) gbn.setShowFrames(true);
            else gbn.setShowFrames(false);
            boolean sentProperly = gbn.sendFrames();
            if (!sentProperly) {
                return false;
            }
            objectOutputStream.writeObject("No chunk left. Task complete.");
            String msg = (String) objectInputStream.readObject();
            clientLogWindow.appendToLog("Server: " + msg + "\n");
            if (msg.compareTo("File received successfully.") == 0) {
                return true;
            } else if (msg.compareTo("File size don't match.") == 0) {
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
}