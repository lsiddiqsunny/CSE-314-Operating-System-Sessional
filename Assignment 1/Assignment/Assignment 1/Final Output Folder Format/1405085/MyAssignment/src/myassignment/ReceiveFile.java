package myassignment;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReceiveFile implements Runnable {

    int errorFlag = 0;
    Socket socket;
    ObjectInputStream ois;
    ObjectOutputStream oos;
    String id;

    public ReceiveFile(Socket socket, String id, ObjectInputStream in, ObjectOutputStream out) {

        this.socket = socket;
        System.out.println("Receive Socket" + this.socket.getLocalSocketAddress());
        this.oos = out;
        this.ois = in;
        this.id = id;
    }

    @Override
    public void run() {
        Response response;
        FileSending receivedFile;
        int downloadedAmount = 0;
        String directory = "/home/antu/Desktop/";
        byte[] tempArray;
        try {

            response = new Response(3, id);

            oos.writeObject(response);

            response = (Response) ois.readObject();

            if (response.response.equals("receive")) {
                //response = new Response(6,id);
                //oos.writeObject(response);

                FileInfo fInfo = (FileInfo) ois.readObject();
                FileTransfer.setReceivedView(fInfo);
                String fileName = fInfo.file;

                int index = 0;
                while (index != -1) {
                    index = fileName.indexOf("/");
                    fileName = fileName.substring(index + 1);

                }

                System.out.println(fileName);
                directory = directory + fileName;
                System.out.println("directory :" + directory);

                byte[] array = new byte[(int) fInfo.length];
                long existingSize = fInfo.length;
                Object fileChunk;
                int k = 0, errorAck = (int) Double.POSITIVE_INFINITY, errFlag = 0, lBound, ackNo = 0;
                Acknowledgement acknowledgement;
                long receivedSize = fInfo.length;

                while (receivedSize > 0) {
                    k = 0;
                    errFlag = 0;
                    existingSize = receivedSize;

                    while (k < 8 && existingSize > 0) {

                        fileChunk = ois.readObject();

                        if (fileChunk.getClass().toString().equals("class myassignment.FileSending")) {

                            receivedFile = (FileSending) fileChunk;
                            System.out.println("my nigga");
                            System.out.println("Chunk Length " + receivedFile.length);

                        } else {
                            errorFlag = 1;
                            break;

                        }
                        //System.out.println("data to receiver" + receivedFile.data[0]);

                        StringBuffer strBuilder = new StringBuffer();

                        char[] charArray;
                        receivedFile = (FileSending) fileChunk;
                        if (receivedFile.length == 0) {
                            System.out.println("The file should be deleted ");
                            break;

                        } else {
                            System.out.println(receivedFile.data[0] + " header " + receivedFile.data[receivedFile.length - 1]);
                            if (receivedFile.data[0] != 126 || receivedFile.data[receivedFile.length - 1] != 126) {

                                System.out.println("Error in header ");
                                break;
                            }

                            String result = "";

                            for (int j = 4; j < receivedFile.length - 1; j++) {
                                int number = (int) (receivedFile.data[j] & 0xff);
                                String binaryString = Integer.toBinaryString(number);
                                charArray = new char[8 - binaryString.length()];
                                Arrays.fill(charArray, '0');
                                String str = new String(charArray);
                                strBuilder.append(str + binaryString);

                            }
                            //System.out.println(strBuilder);

                            strBuilder = DLLAssignment.destuffingBits(strBuilder);
                            System.out.println("Destuffed length" + strBuilder.length());
                            if (strBuilder.length() % 8 != 0) {
                                result = strBuilder.substring(0, strBuilder.length() - (strBuilder.length() % 8));

                            } else {
                                result = strBuilder.toString();

                            }
                            strBuilder = new StringBuffer(result);
                            System.out.println("at last " + strBuilder.length());
                            //System.out.println(strBuilder);

                            boolean error = DLLAssignment.hasCheckSumError(strBuilder);
                            System.out.println("Payload + CheckSum" + strBuilder.length());
                            tempArray = DLLAssignment.stringToByte(strBuilder.substring(0, strBuilder.length() - 8));
                            if (error == false) {

                                System.out.println("TempArray length :" + tempArray.length);

                                System.arraycopy(tempArray, 0, array, downloadedAmount, tempArray.length);
                                // System.out.println("data" + receivedFile.data[0]);
                                downloadedAmount += tempArray.length;
                                existingSize = existingSize - tempArray.length;
                                ackNo++;
                                /*if (FileTransfer.flag == 0) {
                                    response = new Response(1, id);
                                    //oos.writeObject(response);

                                } else {
                                    response = new Response(2, id);
                                    // oos.writeObject(response);
                                }
                                oos.writeObject(response);*/
                            } else {

                                existingSize -= tempArray.length;
                                errorAck = Math.min(errorAck, k);
                                System.out.println("Error occured at :" + errorAck + " frame ");
                                errFlag = 1;

                            }
                            k++;
                        }

                        System.out.println("Downloading The file");

                    }

                    if (errFlag == 1) {
                        lBound = errorAck;
                    } else {
                        lBound = k - 1;
                    }
                    for (int lCounter = 0; lCounter <= lBound; lCounter++) {
                        // if(errFlag == 1 && lCounter)
                        if (errFlag == 1 && lCounter == lBound) {
                            acknowledgement = new Acknowledgement(ackNo, ackNo, 1);
                            acknowledgement.createAcknowledgement();
                            oos.writeObject(acknowledgement);
                            System.out.println("error occurred frame" + lBound);
                        } else {
                            acknowledgement = new Acknowledgement(ackNo, ackNo, 0);
                            acknowledgement.createAcknowledgement();
                            receivedSize -= 32;
                            oos.writeObject(acknowledgement);
                            System.out.println("ack No : " + ackNo + " lBound :" + lBound);
                        }
                    }

                }
                System.out.println("Writing to the file" + errorFlag);
                if (errorFlag == 0) {

                    File file = new File(directory);
                    if (!file.exists()) {
                        file.createNewFile();
                    }

                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                    bos.write(array);
                    bos.flush();
                    bos.close();
                }
            } else {
                System.out.println("There is no file to receive !");
            }
            response = (Response) ois.readObject();
            socket.close();

        } catch (Exception ex) {
            Logger.getLogger(ReceiveFile.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
