/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clientSide;

import util.ConnectionUtilities;
import util.FrameManager;
import util.HelperClass;
import util.StringConstants;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ThreadLocalRandom;


/**
 *
 * @author Antu
 */
public class Sender implements Serializable{
    public ConnectionUtilities connection,receiverConnection;
    String chunkSize ;
    String fileID,TAG = getClass().getName();
    private final String clientFileStorage = "Client File";
    static int ID = 1;


    int lostFrameNo;

    public Sender(ConnectionUtilities con, ConnectionUtilities receiverConnection){
        connection=con;
        this.receiverConnection = receiverConnection;
        ID++;

    }
    private void showToDebug(String line){
//        System.out.println(line);
    }

    public boolean idDoesNotExist(String studentID){
        boolean result = false;
        try {
            connection.write(studentID);
            String serverResponse = (String) connection.read();
            result = !serverResponse.equals(StringConstants.NO_STUDENT_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean isSendingSuccessful(File toBeSentFile, boolean enableLostFrame){
        boolean result = false;
        try {
            String fileSizeResponse = (String) connection.read();
            if (fileSizeResponse.equals(StringConstants.START_SENDING_FILE)) {
                String data = (String) connection.read();
                String[] chunkAndID = data.split(":", 2);
                chunkSize = chunkAndID[0];
                fileID = chunkAndID[1];
                int size = Integer.parseInt(chunkSize);
                splitFile(toBeSentFile, size, fileID,enableLostFrame);

                String fileDirectory = clientFileStorage + "/" + fileID;
                File newDirectory = new File(fileDirectory);
                File[] listOfFiles = newDirectory.listFiles();
                connection.write(lostFrameNo);
                connection.write(StringConstants.SENDING_FIRST_CHUNK);

                boolean againTransfer = true, isOkTransfer = true;
                int value = 0;
                int kind = 0, seqNo = 0, ackNo = 0;

                if (listOfFiles != null) {
                    for (File f : listOfFiles) {
                        showToDebug("Writing chunk sending");
                        connection.write(StringConstants.CHUNK_SENDING);
                        Socket socket = connection.getSc();
                        String fileName = f.getName();

                        FrameManager currentFrame = new FrameManager(f,kind,seqNo,ackNo);
                        byte[] stuffedFileBytes = currentFrame.getMainFrame();
                        showToDebug("************************"+fileName);
                        FrameManager fileFrameManager = new FrameManager(fileName,kind,seqNo,ackNo);
                        byte[] fileNameBytes = fileFrameManager.getMainFrame();

                        while (againTransfer) {
                            System.out.println("Sender is now sending files. Acknowledge No. : "+ackNo+
                                    " Sequence No. : "+seqNo);
                            System.out.println("In sender, Frame without bit stuffing : \n"+currentFrame.withoutStuffedBit);
                            System.out.println("In sender, Frame with bit stuffing : \n"+currentFrame.withStuffedBit);
                            connection.write(stuffedFileBytes);
                            connection.write(fileNameBytes);

                            againTransfer = false;
                            try {

                                showToDebug("Before reading");
                                socket.setSoTimeout(10 * 1000);
                                byte[] responseFromServer = (byte[]) connection.read();
                                if(responseFromServer == null){
                                    showToDebug("NUUUUUUUUULLLLL");
                                }
                                else{
                                    showToDebug("NOT NULLLL");
                                }
                                showToDebug("After Reading");

                                FrameManager responseManager = new FrameManager(responseFromServer);
                                int ackNoFromServer = responseManager.getAckNo();
                                showToDebug("Seq No : "+seqNo+" ackFromServer : "+ackNoFromServer);
                                if(ackNoFromServer == seqNo){
                                    showToDebug("SEq no milse");
                                    seqNo^=1;
                                }
                                else{
                                    againTransfer = true;
                                }
                                showToDebug(" ************************Response received " + ++value);
                            } catch (Exception e) {
                                System.out.println("Timeout! Resending files.");
                                againTransfer = true;
                            }
                        }
                        againTransfer = true;
                    }
                } else {
                    System.out.println("Sender : list of files is null,client er file storage");
                }
                new HelperClass().deleteFiles(newDirectory);

                //********************CHECK IF NEEDED******************
                if (isOkTransfer) connection.write(StringConstants.SENDING_COMPLETED);
                else{
//                    System.out.println(isOkTransfer +" HOW");
                    connection.write(StringConstants.SERVER_STOP_SENDING);
                }

                String responseFinal = (String) connection.read();
                if (responseFinal.equals(StringConstants.CHUNKS_MATCHED)) {
                    connection.write(StringConstants.SERVER_START_SENDING);
                    result = true;
                } else {
                    connection.write(StringConstants.SERVER_STOP_SENDING);
                    result = false;
                }
            }
            else{
                showToDebug("isSendingSuccesful *****WRONG*****");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    public boolean isSpaceAvailableInServer(File toBeSentFile){
        boolean result = false;
        try {
            String serverResponse = (String) connection.read();
            if (serverResponse.equals(StringConstants.SEND_FILENAME_AND_SIZE)) {

                String fileName = toBeSentFile.getName();
                long fileSize = toBeSentFile.length();
                String nameAndSize = fileName + ":" + fileSize;
                connection.write(nameAndSize);

                String fileSizeResponse = (String) connection.read();
                result = !fileSizeResponse.equals(StringConstants.NOT_ENOUGH_SPACE_IN_SERVER);
            }
            else {
                showToDebug("isSpaceAvailableInServer ****WRONG*****");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    public  void splitFile(File mainFile, int chunkSize, String fileID, boolean enableLostFrame) throws IOException {
//        chunkSize = 10000;
        int partCounter = 1;
        byte[] container = new byte[chunkSize];
        String fileName = mainFile.getName();
        int index = fileName.lastIndexOf(".");
        String fileExtension ="";
        if(index>0){
            fileExtension = fileName.substring(index+1);
        }
        String fileDirectory = clientFileStorage+"/"+fileID;
        File newDirectory = new File(fileDirectory);
        newDirectory.mkdirs();

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(mainFile))) {
            int bytesAmount = 0;
            while ((bytesAmount = bis.read(container)) > 0) {
                String filePartName = String.format("%s.%03d."+fileExtension, fileID, partCounter++);
                File newFile = new File(newDirectory, filePartName);
                try (FileOutputStream out = new FileOutputStream(newFile)) {
                    out.write(container, 0, bytesAmount);
                }
            }
        }
//        System.out.println("******FILE PARTS:" + (partCounter - 1));
        if(enableLostFrame){
            lostFrameNo = ThreadLocalRandom.current().nextInt(1,partCounter -1 );
        }
        else{
            lostFrameNo = -1;
        }
        System.out.println("LOST FRAME NO : "+lostFrameNo);
    }
    
    
}
