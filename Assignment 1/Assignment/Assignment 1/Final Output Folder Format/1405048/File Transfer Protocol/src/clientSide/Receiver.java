package clientSide;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import util.ConnectionUtilities;
import util.Information;
import util.StringConstants;

import java.io.*;
import java.nio.file.Files;

/**
 * @author ANTU on 02-Nov-17.
 * @project File Transfer Protocol
 */
public class Receiver implements Serializable, Runnable {
    private ConnectionUtilities receiverConnection;
    private String fileName, studentID;
    private long fileSize;
    public TextArea incomingField;
    private final String fileStorage = "Receiver File/";
    ClientPageController clientPageController;
    Information informationFromServer;

    public Receiver(ConnectionUtilities connection) {
        this.receiverConnection = connection;
    }

    public void startWaitingForClient(){
        try {
            informationFromServer = (Information) receiverConnection.read();
            fileName = informationFromServer.getFileName();
            fileSize = informationFromServer.getFileSize();
            studentID = informationFromServer.getStudentID();

            String msg = "Attention! You have new incoming files!\n"+"Do you want to receive from " + studentID + " ?\n"+
                    "File Name : " + fileName + " \nFile Size : " + fileSize;

            incomingField.setText(msg);
            clientPageController.showReceivingSide();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public synchronized boolean isReceivingAndMergingOk(){
        boolean result = false;
        try {
            receiverConnection.write(StringConstants.START_SERVER_SENDING);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String responseFromServer = "";
        String fileName = informationFromServer.fileName;
        String studentID = receiverConnection.studentID;
        File clientDirectory = new File(fileStorage+"/"+studentID);
        clientDirectory.mkdirs();
        File into = new File(fileStorage+"/"+studentID+"/"+fileName);
        try {
            into.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (BufferedOutputStream mergingStream = new BufferedOutputStream(new FileOutputStream(into))) {
            while (true){
                try {
                    responseFromServer = (String) receiverConnection.read();
                    if (responseFromServer.equals(StringConstants.CHUNK_SENDING)) {
                        File chunkFile = (File) receiverConnection.read();

                        Files.copy(chunkFile.toPath(), mergingStream);

                        receiverConnection.write(StringConstants.KEEP_SENDING_FILE);
                    } else if (responseFromServer.equals(StringConstants.TIMEOUT_ABORT)) {
                        into.delete();
                        System.out.println("Timeout Abort!");

                        break;
                    } else if (responseFromServer.equals(StringConstants.SENDING_COMPLETED)) {
                        receiverConnection.write(StringConstants.SENT_SUCCESSFUL);
                        System.out.println("Receiving Successful");

                        result = true;
                        break;
                    } else {
                        System.out.println(getClass().getName() + " Some error in reading");
                        result = false;
                        break;
                    }
                }catch (Exception e){
                    result = false;
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            result = false;
            e.printStackTrace();
        }
        return result;
    }

    public synchronized void denyRequest(){
        try {
            receiverConnection.write(StringConstants.RECEIVING_DENIED);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        startWaitingForClient();
    }
}
