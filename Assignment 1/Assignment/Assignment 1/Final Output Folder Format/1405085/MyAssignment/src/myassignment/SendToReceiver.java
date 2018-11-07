package myassignment;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SendToReceiver implements Runnable {

    int flagVar = 0;
    Socket receiverSocket;
    FileInfo file;
    String id, fId;
    FileCopy fCopy;
    ObjectInputStream ois;
    ObjectOutputStream oos;
    Response response;
    AcknowledgementTimerTask timerTask;
    Timer timer;
    Acknowledgement ackment;

    public SendToReceiver(Response response, ObjectInputStream in, ObjectOutputStream out) {
        receiverSocket = Server.hashMap.get(response.id);
        id = response.id;
        System.out.println("addresss :" + receiverSocket.getRemoteSocketAddress());
        oos = out;
        ois = in;

        this.response = response;
    }

    @Override
    public void run() {

        int stdId;

        try {

            //response = (Response) ois.readObject();
            id = response.id;

            //stdId = Integer.parseInt(id.substring(4));
            if (response.response.equals("receive")) {
                file = Server.fileMap.get(response.id);
                fId = file.fileId;
                fCopy = Server.fileIdMap.get(fId);

                if (file != null) {

                    response = new Response(3, id);
                    oos.writeObject(response);
                    //response = (Response)ois.readObject();

                    oos.writeObject(file);

                    response = (Response) ois.readObject();

                    if (response.response.equals("acknowledged")) {
                        System.out.println("Acknowledged Message has been caught");
                        long existingSize = file.length;
                        //int randomSize = (int) file.length / 10;
                        int randomSize = 32;
                        if (file.length < 32) {
                            randomSize = (int) file.length;
                        }

                        int uploadedAmount = 0;
                        FileSending fSending;
                        int sendAmount = randomSize;
                        byte[] array = new byte[randomSize];
                        String header = "01111110";
                        int seqNo = 0, ackNo = 0;
                        byte[] tempArray;
                        long sentSize = file.length;
                        int uploaded = 0, k = 0;

                        //should be replaced
                        //byte[] serverArray = new byte[20000];
                        while (sentSize > 0) {
                            k = 0;
                            uploadedAmount = uploaded;
                            existingSize = sentSize;
                            while (k < 8 && existingSize > 0) {
                                if (existingSize >= randomSize) {
                                    sendAmount = randomSize;

                                } else {
                                    sendAmount = (int) existingSize;

                                }

                                System.arraycopy(fCopy.dataArray, uploadedAmount, array, 0, sendAmount);

                                StringBuffer strBuilder = new StringBuffer();
                                //added code
                                char[] charArray;
                                int number;

                                for (int i = 0; i < sendAmount; i++) {
                                    number = (int) (array[i] & 0xff);
                                    String binaryString = Integer.toBinaryString(number);
                                    charArray = new char[8 - binaryString.length()];
                                    Arrays.fill(charArray, '0');
                                    String str = new String(charArray);
                                    strBuilder.append(str + binaryString);

                                }

                                strBuilder = DLLAssignment.rtnWithCheckSum(strBuilder);

                                strBuilder = DLLAssignment.bitStuffedString(strBuilder);

                                if (strBuilder.length() % 8 != 0) {
                                    charArray = new char[8 - (strBuilder.length() % 8)];
                                    Arrays.fill(charArray, '0');
                                    String strToAlign = new String(charArray);
                                    strBuilder.append(strToAlign);

                                }

                                strBuilder = new StringBuffer(header + DLLAssignment.intToBinString(1) + DLLAssignment.intToBinString(seqNo) + DLLAssignment.intToBinString(ackNo) + strBuilder.toString() + header);

                                System.out.println(strBuilder.length() + " Length");
                                tempArray = DLLAssignment.stringToByte(strBuilder.toString());
                                System.out.println(tempArray[tempArray.length - 1]);

                                fSending = new FileSending((int) (strBuilder.length() / 8), tempArray);

                                System.out.println("Chunk Length server :" + sendAmount);

                                existingSize -= sendAmount;
                                uploadedAmount += sendAmount;

                                oos.writeObject(fSending);
                                k++;
                                seqNo++;
                                /*response = (Response) ois.readObject();
                                if (!response.response.equals("acknowledged")) {
                                    System.out.println("Error Occurred");
                                    flagVar = 1;
                                    Server.hashMap.remove(response.id);
                                    break;

                                }*/

                            }
                            
                            
                            for (int j = 0; j < k; j++) {

                                timerTask = new AcknowledgementTimerTask();
                                timer = new Timer(true);
                                timer.scheduleAtFixedRate(timerTask, 0, 10 * 1000);
                                System.out.println("In the loop");
                                while (ois.available() > 0 || timerTask.timerFlag == 1) {
                                    /*while ((ois.available() != 0) && timerTask.timerFlag == 0 ) { */
                                    System.out.println("I love you " + ois.available());
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
                                        System.out.println("Error acknowledged from server");
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
                        if (flagVar == 0) {
                            // Server.serverMem[stdId] = null;
                            Server.fileIdMap.remove(fId);
                            Server.fileMap.remove(id);
                            Server.addUsedMemory((long) file.length);

                        } else if (flagVar == 1) {
                            System.out.println("we have to store the content");

                        }

                    } else if (response.response.equals("unsuccessful")) {
                        FileInfo fInfo = Server.fileMap.get(response.id);
                        String fId = fInfo.fileId;
                        Server.fileMap.remove(response.id);
                        Server.fileIdMap.remove(response.id);
                        Server.hashMap.remove(response.id);

                    } else {
                        System.out.println("You are logging out");
                        Server.hashMap.remove(response.id);
                    }

                } else {
                    response = new Response(7, id);
                    oos.writeObject(response);

                }

            }

            response = new Response(3, file.from);

            oos.writeObject(response);
            receiverSocket.close();

        } catch (Exception ex) {
            Logger.getLogger(SendToReceiver.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
