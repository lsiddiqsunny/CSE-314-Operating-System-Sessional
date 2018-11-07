package util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author ANTU on 25-Sep-17.
 * @project Socket
 */
public class HelperClass {
    public void showDebugComment(String line){
        //System.out.println(line);
    }
    public  void readUntilFinished(ConnectionUtilities connection, File fileDirectory, long fileSize, int lostFrameNo){
        int _count = 0,kind = 1, seqNo = 0, ackNo = 0;

        while (true) {
            try {
                _count++;
                showDebugComment("Before Reading");
                String headsUp = (String) connection.read();

                showDebugComment(_count + " Heads Up : " + headsUp);
                if (headsUp.equals(StringConstants.CHUNK_SENDING)) {
                    if (_count == lostFrameNo) {
                        System.out.println("Frame is now going to get lost.");
                        Object missingChunk = connection.read();
                        Object missingFileName = connection.read();
                    }

                    Object object = connection.read();
                    byte[] stuffedChunkFile = (byte[]) object;
                    FrameManager payloadManager = new FrameManager(stuffedChunkFile);
                    byte[] payloadBytes = payloadManager.getPayloadBytes();
                    int seqFromSender = payloadManager.getSeqNo();

                    byte[] stuffedFileName = (byte[]) connection.read();
                    FrameManager fileNameManager = new FrameManager(stuffedFileName);
                    String fileName = fileNameManager.getFileName();

                    showDebugComment(connection.studentID + " ###########################" +
                            "##FileName from framemanager : "+fileName);

                    if(payloadManager.hasCheckSumError()){
                        showDebugComment(_count + " ************ERROR DETECTED**********");
                    }
                    else{
                        showDebugComment(_count + " ************ERROR NAAI**********");
                    }
                    if(!payloadManager.hasCheckSumError() && seqFromSender == seqNo){

                        System.out.println("In Receiver frame with stuffed bits : \n"+payloadManager.withStuffedBit);
                        System.out.println("In Receiver frame without stuffed bits : \n"+payloadManager.withoutStuffedBit);

                        createAndMoveFileFromClient(fileName, payloadBytes, fileDirectory);
                        seqNo^=1;
                    }
                    String dontCare = "X";
                    ackNo = seqNo^1;// = seqFromSender
                    showDebugComment("Ack No in Server : "+ackNo);


                    FrameManager toSenderManager = new FrameManager(dontCare, kind, seqNo, ackNo);
                    byte[] dontCareBytes = toSenderManager.getMainFrame();

                    showDebugComment("Before writing");
                    if(dontCareBytes == null){
                        showDebugComment("SErver eo NULLLLLLLLL");
                    }
                    System.out.println("Receiver is now sending acknowledgement. Acknowledge No : "+ackNo +
                            " Sequence No : "+seqNo);
                    connection.write(dontCareBytes);
                    showDebugComment("After Writing");

                } else if (headsUp.equals(StringConstants.TIMEOUT_ABORT)) {
                    //delete all the files and Decrease the size of SIZE_CONSUMED
                    deleteFiles(fileDirectory);
                    break;
                } else if (headsUp.equals(StringConstants.SENDING_COMPLETED)) {
                    long chunkTotal = calculateChunkSize(fileDirectory);
                    if (chunkTotal != fileSize) {
                        System.out.println("File Size and Chunk Total sizes did not match!");
                        System.out.println("Chunk total : " + chunkTotal + " File Size : " + fileSize);
                        deleteFiles(fileDirectory);
                        connection.write(StringConstants.CHUNKS_DID_NOT_MATCH);
                        break;
                    }
                    connection.write(StringConstants.CHUNKS_MATCHED);
                    break;
                } else {
                    System.out.println(HelperClass.class.getName() + " Some error in reading");
                    break;
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }





    public long calculateChunkSize(File fileDirectory){
        long size = 0;
        File[] listFiles = fileDirectory.listFiles();
        if(listFiles == null) return 0;
        for(File file: listFiles){
            size+= file.length();
        }
        return size;
    }
    public void deleteFiles(File fileDirectory){
        if(fileDirectory == null) return;
        File[] listFiles = fileDirectory.listFiles();
        if(listFiles == null) return;
        for(File file: listFiles){
            if(file.isFile()) file.delete();
        }
        fileDirectory.delete();
    }

    public  void createAndMoveFileFromClient(String fileName, byte[] fromFile, File fileDirectory){
        try {
            File userStorageFile = new File(fileDirectory,fileName);
            if(!userStorageFile.createNewFile()){
                System.out.println(HelperClass.class.getName()+" : in createAndMoveFileFromClient file could not be " +
                        "created.");
            }
            /*
            try (BufferedOutputStream mergingStream = new BufferedOutputStream(new FileOutputStream(userStorageFile))) {
                Files.copy(fromFile.toPath(), mergingStream);
            }
*/
            try (FileOutputStream fos = new FileOutputStream(userStorageFile)){
                fos.write(fromFile);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            System.out.println(HelperClass.class.getName()+" Problem in creating new file");
        }
    }
    public synchronized void sendFiles(File fileDirectory, ConnectionUtilities connection){
        File[] listOfFiles = fileDirectory.listFiles();
        System.out.println(fileDirectory.getAbsolutePath());
        if(listOfFiles == null) {
            System.out.println("Helper class: sendFiles(): LIST OF FILES E NULL");
            return;
        }
        for(File f : listOfFiles){

            try {
                connection.write(StringConstants.CHUNK_SENDING);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                connection.write(f);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String response = null;
            try {
                response = (String) connection.read();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(!response.equals(StringConstants.KEEP_SENDING_FILE)){
                /*
                * kahiini
                * */
                System.out.println("Helper e sendFiles e genjam");
                break;
            }
        }
        try {
            connection.write(StringConstants.SENDING_COMPLETED);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static long getRandomNumber(long fileSize){
        long max = fileSize / 5 +1; // in case division turns it to 0, add 1 to keep it at least 1
        long min = fileSize / 10;

        if(min == 0) min++;
        long random = ThreadLocalRandom.current().nextLong(min,max);

        return random;
    }
}
