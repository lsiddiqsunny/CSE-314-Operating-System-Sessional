package myassignment;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;
import sun.awt.X11.XConstants;

public class SendFile implements Runnable {

    Socket socket;
    ObjectInputStream ois;
    ObjectOutputStream oos;
    FileInfo fileToBeSent;
    Timer timer;

    public SendFile(Socket socket, ObjectInputStream in, ObjectOutputStream out, FileInfo info) {
        this.socket = socket;
        ois = in;
        oos = out;
        fileToBeSent = info;
    }

    @Override
    public void run() {
        Scanner reader;
        long existingSize = fileToBeSent.length;
        int sendAmount;
        int uploadedAmount = 0;
        byte[] array;
        byte[] tempArray;
        byte[] fileArray;
        FileSending uploadedFile;
        int serverAmount = 0;
        AcknowledgementTimerTask timerTask;
        int seqNo = 0, ackNo = 0;
        int uploaded = 0;
        String header = "01111110";
        Acknowledgement ackment;

        try {
            oos.writeObject(fileToBeSent);

            System.out.println("In the sendFile class");
            Response response = (Response) ois.readObject();
            if (response.response.equals("successful")) {

                Path path = Paths.get(fileToBeSent.file);
                fileArray = Files.readAllBytes(path);
                array = new byte[response.size];
                serverAmount = response.size;
                int k = 0;
                reader = new Scanner(System.in);
                long sentSize = fileToBeSent.length;

                while (sentSize > 0) {
                    k = 0;
                    existingSize = sentSize;
                    uploadedAmount = uploaded;
                    while (k < 8 && existingSize > 0) {

                        if (existingSize > serverAmount) {
                            sendAmount = serverAmount;
                        } else {
                            sendAmount = (int) existingSize;
                        }
                        System.out.println("existing size" + existingSize + "uploadedAmount" + uploadedAmount + "array " + fileArray[uploadedAmount]);

                        System.arraycopy(fileArray, uploadedAmount, array, 0, sendAmount);

                        //Added Code
                        StringBuffer strBuilder = new StringBuffer();

                        char[] charArray;

                        for (int i = 0; i < sendAmount; i++) {
                            int number = (int) (array[i] & 0xff);
                            String binaryString = Integer.toBinaryString(number);
                            charArray = new char[8 - binaryString.length()];
                            Arrays.fill(charArray, '0');
                            String str = new String(charArray);
                            strBuilder.append(str + binaryString);

                        }

                        strBuilder = DLLAssignment.rtnWithCheckSum(strBuilder);
                        System.out.println("The original Frame with Cheksum");
                        System.out.println(strBuilder.toString());
                        System.out.println("Do you want error Y/N");
                        int x = reader.nextInt();
                        if (x == 1) {
                            strBuilder = DLLAssignment.toggleBits(strBuilder);
                        }

                        strBuilder = DLLAssignment.bitStuffedString(strBuilder);
                        System.out.println("After BitStuffing ");
                        System.out.println(strBuilder.toString());

                        if (strBuilder.length() % 8 != 0) {
                            charArray = new char[8 - (strBuilder.length() % 8)];
                            Arrays.fill(charArray, '0');
                            String strToAlign = new String(charArray);
                            strBuilder.append(strToAlign);

                        }

                        strBuilder = new StringBuffer(header + DLLAssignment.intToBinString(1) + DLLAssignment.intToBinString(seqNo) + DLLAssignment.intToBinString(ackNo) + strBuilder.toString() + header);

                        
                        tempArray = DLLAssignment.stringToByte(strBuilder.toString());
                        System.out.println(tempArray[tempArray.length - 1]);

                        uploadedFile = new FileSending((int) (strBuilder.length() / 8), tempArray);

                        oos.writeObject(uploadedFile);
                        //timer code
                        /* timerTask = new AcknowledgementTimerTask();
                        timer = new Timer(true);
                        timer.scheduleAtFixedRate(timerTask, 0, 10 * 1000);*/
                        uploadedAmount += sendAmount;
                        existingSize -= sendAmount;

                        seqNo++;
                        k++;
                        //System.out.println("Value of K : " + k);
                    }
                    System.out.println("Going to acknowledgement portion");

                    for (int j = 0; j < k; j++) {

                        timerTask = new AcknowledgementTimerTask();
                        timer = new Timer(true);
                        timer.scheduleAtFixedRate(timerTask, 0, 10 * 1000);
                        System.out.println("In the loop");
                        while (ois.available() > 0 || timerTask.timerFlag == 1) {
                            /*while ((ois.available() != 0) && timerTask.timerFlag == 0 ) { */
                            //System.out.println("I love you " + ois.available());
                        }
                        if (timerTask.timerFlag == 1) {
                            System.out.println("The website can not be reached");
                            timer.cancel();
                            break;
                        } else {
                            System.out.println("In the loop");
                            ackment = (Acknowledgement) ois.readObject();                            

                            seqNo = (int) (ackment.ackArray[2] & 0xff);
                            System.out.println("Sequence No : " + seqNo);
                            if (ackment.ackArray[3] == 1) {
                                System.out.println("Error acknowledged from server" );
                                break;
                            } else {
                                sentSize -= 32;
                                uploaded += 32;
                                System.out.println("SentSize" + sentSize);
                                timer.cancel();
                            }
                        }
                    }
                }
                socket.close();

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
