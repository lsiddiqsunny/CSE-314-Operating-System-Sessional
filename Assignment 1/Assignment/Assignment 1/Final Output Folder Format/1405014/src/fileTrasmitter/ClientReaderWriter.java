/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fileTrasmitter;

import transmissionUtilities.BitManipulator;
import transmissionUtilities.ConnectionUtilities;

import java.io.*;
import java.util.Scanner;

class ClientReaderWriter implements Runnable {

    private final ConnectionUtilities connection;

    private static final int N = 8;

    ClientReaderWriter (ConnectionUtilities con) {
        connection = con;
    }




    private void sendFileByChunks (String filename) throws IOException {

        File file = new File (filename);
        long fileLength = file.length ();

        BitManipulator bitManipulator = new BitManipulator ();

        int chunkSize, fileId;

        String serverMessage;

        System.out.println ("Receiving fileId and chunksize from server.");

        synchronized (connection) {
            fileId = Integer.parseInt (connection.readString ());
            chunkSize = Integer.parseInt (connection.readString ());
        }

        byte[] byteArray = new byte[chunkSize];
        byte[][] fileDataStorage = new byte[((int) fileLength / chunkSize) + 1][];
        long[] fileDataLengthArray = new long[((int) fileLength / chunkSize) + 1];
        InputStream in = new FileInputStream (file);
        OutputStream out = connection.sc.getOutputStream ();

        int amountOfDataRead, sequenceNumber = 0, lengthChecker = 0, goBackN_flag = 0;

        synchronized (connection)
        {
            connection.writeString ("Ready to receive?");
        }

        Loop:
        while (true) {

            connection.sc.setSoTimeout (30000);
            synchronized (connection) {
                System.out.println ("Waiting for server ...");
                serverMessage = connection.readStringTimeout ();
                connection.sc.setSoTimeout (0);
            }

            if (serverMessage.equals ("Transmission canceled."))
            {
                synchronized (connection) {
                    connection.writeString ("Transmission canceled.");
                }
                in.close ();
                return;
            }
            else if (serverMessage.equals ("Ready to receive."))
            {
                synchronized (connection) {
                    connection.writeString ("Transmission continued.");
                }

                for (int i = goBackN_flag; i < goBackN_flag + N && i < ((int) fileLength / chunkSize) + 1; i++) {
                    if (fileDataStorage[i] == null) {
                        amountOfDataRead = in.read (byteArray);
                        fileDataStorage[i] = byteArray;
                        fileDataLengthArray[i] = amountOfDataRead;
                    } else {
                        byteArray = fileDataStorage[i];
                        amountOfDataRead = byteArray.length;
                    }

                    //bitManipulator.showByteFrameBitDeStuffed (byteArray);

                    System.out.println ("Stuffing payload of chunk number " + (i + 1));

                    byte[] stuffedByteArray = bitManipulator.bitStuffer (byteArray, amountOfDataRead, i);

                    //bitManipulator.showByteFrameBitStuffed (stuffedByteArray);

                    System.out.println ("Sending stuffed byte array size of chunk number " + (i + 1) + " which is " + stuffedByteArray.length + ".");

                    synchronized (connection) {
                        connection.writeString (Integer.toString (stuffedByteArray.length));
                    }


                    out.write (stuffedByteArray, 0, stuffedByteArray.length);
                    out.flush ();

                    System.out.println ("Sending chunk number " + (i + 1) + " amount " + amountOfDataRead + " bytes.");
                }

                connection.sc.setSoTimeout (30000);

                for (int i = goBackN_flag; i < goBackN_flag + N && i < ((int) fileLength / chunkSize) + 1; i++)
                {
                    sequenceNumber = i + 1;
                    System.out.println ("Waiting for the " + (i + 1) + "th acknowledgement.");
                    synchronized (connection)
                    {
                        byte[] response = (byte[])connection.read();
                        if (response[2] == (byte)0b11111111)
                        {
                            System.out.println ("Error occurred during file transmission. Going back to N.");
                            break;
                        }
                        System.out.println ("Server received chunk successfully.");

                        lengthChecker += fileDataLengthArray[i];
                        System.out.println ("Amount of total data sent is " + lengthChecker + " and total length of file is " + fileLength + ".");
                        if (lengthChecker == fileLength) {
                            break Loop;
                        }
                    }
                }

                connection.sc.setSoTimeout (0);

                goBackN_flag = sequenceNumber;

                synchronized (connection) {
                    serverMessage = connection.readString ();
                    if (serverMessage.equals ("Chunks received.")) connection.writeString ("Ready to receive?");
                }

            }

        }

        in.close ();

        synchronized (connection) {
            serverMessage = connection.readString ();
        }

        if (serverMessage.equals ("File received.")) System.out.println ("File sent to server successfully.");
        else if (serverMessage.equals ("File not received.")) System.out.println ("File transmission failed.");
    }



    private void receiveFileByChunks (String fileName, long fileLength, int chunkSize) throws IOException
    {
        InputStream in = connection.sc.getInputStream ();
        OutputStream out = new FileOutputStream ("Received File " + fileName.replaceFirst("Server file ", ""));

        BitManipulator bitManipulator = new BitManipulator ();

        byte[] byteArray;
        boolean[] byteArrayFlag = new boolean[((int)fileLength / chunkSize) + 1];

        int amountOfDataRead, sequenceNumber = 0, lengthChecker = 0, chunkChecker, goBackN_flag = 0;

        String serverMessage;
        boolean isFileReceived;


        Loop:
        while (true) {

            synchronized (connection) {
                serverMessage = connection.readString ();

                switch (serverMessage) {
                    case "File sent.":
                        if (lengthChecker == fileLength) {
                            synchronized (connection) {
                                connection.writeString ("File received.");
                            }
                            isFileReceived = true;
                        } else {
                            synchronized (connection) {
                                connection.writeString ("File not received.");
                            }
                            isFileReceived = false;
                        }
                        break Loop;
                    case "Ready to receive?":
                        connection.writeString ("Ready to receive.");
                        serverMessage = connection.readString ();
                        switch (serverMessage) {
                            case "Transmission canceled.":
                                System.out.println ("File transmission cancelled due to server timeout.");
                                return;
                            case "Transmission continued.":
                                System.out.println ("Waiting for server ...");
                                break;
                            default:
                                System.out.println ("You screwed up the loop.");
                                break;
                        }

                }

            }

            for (int i = goBackN_flag; i < goBackN_flag + N && i < ((int) fileLength / chunkSize) + 1; i++) {

                int stuffedByteArraySize;

                synchronized (connection)
                {
                    stuffedByteArraySize = Integer.parseInt (connection.readString ());
                    System.out.println ("Receiving stuffed byte array size of loop " + (i + 1) + " which is " + stuffedByteArraySize);
                }

                byte[] stuffedByteArray = new byte[stuffedByteArraySize];

                chunkChecker = 0;
                do {
                    amountOfDataRead = in.read (stuffedByteArray, chunkChecker, stuffedByteArraySize - chunkChecker);
                    System.out.println ("Receiving stuffed byte array of size " + amountOfDataRead);

                    chunkChecker += amountOfDataRead;
                } while (chunkChecker % stuffedByteArraySize != 0);

                byteArray = bitManipulator.bitDestuffer (stuffedByteArray, stuffedByteArraySize, i);
                amountOfDataRead = byteArray.length;

                byte[] chunkInformation = bitManipulator.getChunkInformation ();
                bitManipulator.resetValues ();

                //bitManipulator.showByteFrameBitDeStuffed (byteArray);

                System.out.println ("Receiving chunk number " + (i + 1) + " amount " + amountOfDataRead + " bytes");

                System.out.println ("Sending acknowledgement frame to server of chunk number " + (i + 1) + ".");

                byte[] responseByteArray = new byte[3];

                responseByteArray[0] = (byte) 0b01111110;
                responseByteArray[2] = (byte) 0b01111110;

                if (bitManipulator.hasSequenceError (chunkInformation[2], chunkInformation[3])) {
                    System.out.println ("Sequence error occurred. Discarding frame.");
                    responseByteArray[1] = (byte) 0b11111111;
                    sequenceNumber = i;
                    break;
                } else if (bitManipulator.hasCheckSumError (chunkInformation[0], chunkInformation[1])) {
                    System.out.println ("Checksum error occurred. Discarding frame.");
                    responseByteArray[1] = (byte) 0b11111111;
                    sequenceNumber = i;
                    break;
                } else if (byteArrayFlag[i]) {
                    System.out.println ("Frame already received.");
                    responseByteArray[1] = (byte) i;
                } else {
                    System.out.println ("No error detected. Updating frame storage.");
                    responseByteArray[1] = (byte) i;
                    sequenceNumber = i + 1;
                    byteArrayFlag[i] = true;

                    synchronized (connection)
                    {
                        connection.write(responseByteArray);
                    }

                    System.out.println ("Parsing data to file.");

                    out.write (byteArray, 0, amountOfDataRead);
                    out.flush ();

                    lengthChecker += amountOfDataRead;

                    System.out.println ("Amount of total data received is " + lengthChecker + " and total length of file is " + fileLength + ".");

                    if (lengthChecker == fileLength) {
                        System.out.println ("File transmission completed.");
                        connection.writeString ("File received.");
                        isFileReceived = true;
                        break Loop;
                    }
                }
            }

            goBackN_flag = sequenceNumber;

            synchronized (connection) {
                connection.writeString ("Chunks received.");
            }

        }


        out.close ();

        if (isFileReceived){
            System.out.println ("File received from server successfully.");
        } else {
            System.out.println ("File transmission cancelled.");
        }

    }



    private void sendFileToServer (Scanner in)
    {
        System.out.println ("Enter the username you want to send the file to : ");
        String text = in.nextLine ();
        if (text.contains ("1405")) {
            System.out.println ("Asking server if " + text + " is logged in.");

            synchronized (connection) {
                connection.writeString (text);
            }

            String returnMsg;
            synchronized (connection) {
                returnMsg = connection.readString ();
            }

            if (returnMsg.equals ("Logged in.")) {
                System.out.println (text + " is logged in.");
                System.out.println ("Enter filename : ");
                String fileName = in.nextLine ();

                if (!(new File (fileName).exists ())) {
                    System.out.println("File with filename " + fileName + " doesn't exist");
                    synchronized (connection) {
                        connection.writeString ("Invalid");
                    }
                    return;
                }

                connection.writeString("Valid");

                System.out.println ("Sending file name & file size to server.");
                synchronized (connection) {
                    connection.writeString (fileName);
                    connection.writeString (Long.toString (new File (fileName).length ()));
                    String spaceMessage = connection.readString ();
                    switch (spaceMessage) {
                        case "Enough space.":
                            try {
                                System.out.println ("Sending file to server.");
                                sendFileByChunks (fileName);
                            } catch (IOException e) {
                                System.out.println ("Unknown problems occurred when sending file to server.");
                                return;
                            }
                            break;
                        case "Not enough space.":
                            System.out.println ("Not enough space in server. Try again later.");
                            return;
                        case "File already exists.":
                            System.out.println ("File already exists in server.");
                    }
                }
            } else if (returnMsg.equals ("Not logged in.")) {
                System.out.println (text + " is not logged in.");
            }
        } else {
            System.out.println ("Not a valid user.");
            synchronized (connection) {
                connection.writeString ("0000000");
            }
        }
    }




    private void receiveFileFromServer ()
    {
        String serverMessage;
        System.out.println("Asking server if there's any file for you.");

        synchronized (connection)
        {
            connection.writeString("Check file");
            serverMessage = connection.readString();
        }

        if (serverMessage.equals("File exists."))
        {
            System.out.println("Server has a file for you.");

            System.out.println("Receiving details of the file from the server.");

            String fileName, fileSender;
            long fileSize;
            int fileId, chunkSize;

            synchronized (connection)
            {
                fileName = connection.readString();
                fileSize = Long.parseLong(connection.readString());
                fileSender = connection.readString();
                fileId = Integer.parseInt(connection.readString());
            }

            System.out.println("Server has file with file name : " + fileName + ", file size : " + fileSize + ", file id : " + fileId + " from "
                    + fileSender + ".");

            System.out.println ("Receiving chunk size from server.");

            synchronized (connection)
            {
                serverMessage = connection.readString();
                chunkSize = Integer.parseInt (serverMessage);
            }

            System.out.println("Receiving file from server.");


            try {
                receiveFileByChunks(fileName, fileSize, chunkSize);
            } catch (IOException e){
                System.out.println("Error occurred when receiving file from server.");
            }

        } else
        {
            System.out.println("Server has no file for you currently. Please try again after sometime.");
        }

    }




    @Override
    public void run () {

        Scanner in = new Scanner (System.in);
        Loop:
        while (true) {

            System.out.println ("Type send, receive or logout for corresponding actions.");

            String information = in.nextLine();

            synchronized (connection)
            {
                connection.writeString(information);
            }

            switch (information) {
                case "send":
                    sendFileToServer(in);
                    break;
                case "receive":
                    receiveFileFromServer();
                    break;
                case "logout":
                    System.out.println("You've logged out successfully.");
                    break Loop;
                default:
                    System.out.println("Invalid option. Try again.");
                    break;
            }

        }

    }

}


