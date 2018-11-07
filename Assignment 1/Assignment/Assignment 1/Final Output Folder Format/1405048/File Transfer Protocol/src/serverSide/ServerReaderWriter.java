/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package serverSide;


import util.ConnectionUtilities;
import util.HelperClass;
import util.Information;
import util.StringConstants;

import java.io.File;
import java.util.HashMap;

/**
 *
 * @author Antu
 */
public class ServerReaderWriter implements Runnable{

    public HashMap<String, Information> clientList;
    public ConnectionUtilities senderConnection;
    public static long fileID = 0;
    private Information information;
    private final String fileStorage = "Server File/";
    private File fileDirectory;
    private HelperClass helperClass = new HelperClass();
    public final long MAXIMUM_SIZE_CHUNKS = (1024 * 1024 * 1024);// 1 GB
    public long SIZE_CONSUMED = 0;
    private int lostFrameNo;


    public ServerReaderWriter(Information information, HashMap<String, Information> list){
        clientList=list;
        this.information = information;
        senderConnection = information.senderConnection;
    }

    private void showToDebug(String line){
        System.out.println(line);
    }

    @Override
    public void run() {
        while(true){
            try {
                System.out.println("Server Start");
                Object o = senderConnection.read();
                String id = o.toString();
                System.out.println("Current ID : " + id);
                if (clientList.containsKey(id)) {
                    senderConnection.write(StringConstants.ID_EXISTS);

                    senderConnection.write(StringConstants.SEND_FILENAME_AND_SIZE);
                    o = senderConnection.read();
                    String data = o.toString();
                    String[] nameAndSize = data.split(":", 2);
                    String fileName = nameAndSize[0];
                    information.fileName = fileName;
                    long fileSize = 0;
                    try {
                        String sizeString = nameAndSize[1];
                        fileSize = Long.parseLong(sizeString);
                    } catch (Exception e) {
                        System.out.println(getClass().toString() + " : Invalid FileSize");
                        e.printStackTrace();
                    }
                    if (!hasEnoughSpace(fileSize)) {
                        //make exception or anything that terminates the transmission
                        senderConnection.write(StringConstants.NOT_ENOUGH_SPACE_IN_SERVER);
                    }
                    else {
                        senderConnection.write(StringConstants.SPACE_AVAILABLE_IN_SERVER);

                        incrementFileID();
                        setSIZE_CONSUMED(-fileSize);
                        long randomChunkSize = HelperClass.getRandomNumber(fileSize);
                        information.fileSize = fileSize;
                        senderConnection.write(StringConstants.START_SENDING_FILE);
                        senderConnection.write(randomChunkSize + ":" + getFileID());
                        fileDirectory = new File(fileStorage + "/" + getFileID());
                        setFileDirectory(fileDirectory);
                        getFileDirectory().mkdirs();

                        lostFrameNo = (int) senderConnection.read();
                        showToDebug("Lost Frame no : "+lostFrameNo);

                        String checkResponse = (String) senderConnection.read();

                        if (checkResponse.equals(StringConstants.SENDING_FIRST_CHUNK)) {

                            helperClass.readUntilFinished(senderConnection, getFileDirectory(), fileSize,lostFrameNo);
                        }
                        System.out.println("Completed IN SERVER receiving");

                        checkResponse = (String) senderConnection.read();
                        if(!checkResponse.equals(StringConstants.SERVER_START_SENDING)) continue;
                        else{
                            System.out.println("Starting sending to client "+id);
                        }

                        Information toReceiverInformation = clientList.get(id);
                        ConnectionUtilities toReceiverConnection = toReceiverInformation.receiverConnection;
                        toReceiverConnection.write(information);

                        String responseFromReceiver = "";
                        responseFromReceiver = (String) toReceiverConnection.read();
                        if (responseFromReceiver.equals(StringConstants.RECEIVING_DENIED)) {

                            helperClass.deleteFiles(getFileDirectory());
                            setSIZE_CONSUMED(fileSize);
                            System.out.println("receiving denied");
                            continue;
                        }


                        helperClass.sendFiles(getFileDirectory(), toReceiverConnection);

                        responseFromReceiver = (String) toReceiverConnection.read();

                        if (responseFromReceiver.equals(StringConstants.SENT_SUCCESSFUL)) {
                            System.out.println("Successfully sent to client!");
                        } else {
                            System.out.println("Sending failed!");
                        }

                        helperClass.deleteFiles(getFileDirectory());
                        setSIZE_CONSUMED(fileSize);
                        System.out.println("Server Sending Completed");
                    }
                } else {
                    //tell client that student id is not logged in
                    senderConnection.write(StringConstants.NO_STUDENT_ID);
                }
            }catch (Exception e){
                System.out.println(information.studentID+" has logged out");
                clientList.remove(information.studentID);
                helperClass.deleteFiles(getFileDirectory());
                return;
            }
            
        }
    }



    public synchronized void setFileDirectory(File fileDirectory){
        this.fileDirectory = fileDirectory;
    }

    public synchronized File getFileDirectory(){
        return fileDirectory;
    }

    public synchronized boolean hasEnoughSpace(long fileSize){
        return (MAXIMUM_SIZE_CHUNKS - SIZE_CONSUMED - fileSize) > 0;
    }

    public synchronized void setSIZE_CONSUMED(long fileSize){
        SIZE_CONSUMED+= fileSize;
    }

    public synchronized void incrementFileID(){
        fileID++;
    }
    public synchronized long getFileID(){
        return fileID;
    }


    
}
