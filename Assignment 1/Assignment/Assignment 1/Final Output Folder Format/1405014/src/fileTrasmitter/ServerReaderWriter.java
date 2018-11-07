/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fileTrasmitter;

import transmissionUtilities.BitManipulator;
import transmissionUtilities.ClientInformation;
import transmissionUtilities.ConnectionUtilities;
import transmissionUtilities.FileInformation;

import java.io.*;
import java.util.HashMap;
import java.util.Random;

public class ServerReaderWriter implements Runnable {

    private HashMap<String, ClientInformation> clientList;
    private final ConnectionUtilities connection;
    private String user;
    private long bufferSize = 200 * 1024 * 1024;
    private int fileId = 0;

    private static int N = 8;

    public ServerReaderWriter (String username, ConnectionUtilities con, HashMap<String, ClientInformation> list) {

        connection = con;
        clientList = list;
        user = username;

    }

    private void receiveFileByChunks (String fileName, String fileSize, String receiver, int chunkSize) throws IOException {

        InputStream in = connection.sc.getInputStream ();
        OutputStream out = new FileOutputStream (fileName);

        BitManipulator bitManipulator = new BitManipulator ();

        long fileLength = Long.parseLong (fileSize);
        byte[] byteArray;
        boolean[] byteArrayFlag = new boolean[((int)fileLength / chunkSize) + 1];

        int amountOfDataRead, sequenceNumber = 0, lengthChecker = 0, chunkChecker, goBackN_flag = 0;

        String clientMessage;
        boolean isFileReceived;

        Loop:
        while (true) {

            synchronized (connection) {
                clientMessage = connection.readString ();

                switch (clientMessage) {
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
                        clientMessage = connection.readString ();
                        switch (clientMessage) {
                            case "Transmission canceled.":
                                new File (fileName).delete ();
                                bufferSize += lengthChecker;
                                System.out.println ("File transmission cancelled due to server timeout.");
                                return;
                            case "disconnect":
                                new File (fileName).delete ();
                                clientList.get (user).isLoggedIn = false;
                                clientList.get (user).connection.close ();
                                System.out.println (user + " has disconnected from server.");
                                return;
                            case "Transmission continued.":
                                System.out.println ("Waiting for " + user + " ...");
                                break;
                        }
                        break;
                    case "disconnect":
                        new File (fileName).delete ();
                        clientList.get(user).isLoggedIn = false;
                        clientList.get(user).connection.close();
                        System.out.println(user + " has disconnected from server.");
                        return;
                }
            }

            for (int i = goBackN_flag; i < goBackN_flag + N && i < ((int) fileLength / chunkSize) + 1; i++) {

                int stuffedByteArraySize;

                synchronized (connection) {
                    String information = connection.readString ();
                    if (information.equals ("disconnect")) {
                        new File (fileName).delete ();
                        clientList.get (user).isLoggedIn = false;
                        clientList.get (user).connection.close ();
                        System.out.println (user + " has disconnected from server.");
                        return;
                    }
                    stuffedByteArraySize = Integer.parseInt (information);
                    System.out.println ("Receiving stuffed byte array size from " + user + " of chunk number " + (i + 1) + " which is " + stuffedByteArraySize + ".");
                }

                byte[] stuffedByteArray = new byte[stuffedByteArraySize];

                chunkChecker = 0;
                do {
                    amountOfDataRead = in.read (stuffedByteArray, chunkChecker, stuffedByteArraySize - chunkChecker);
                    System.out.println ("Receiving stuffed byte array from " + user + " of size " + amountOfDataRead);
                    chunkChecker += amountOfDataRead;
                } while (chunkChecker % stuffedByteArraySize != 0);

                byteArray = bitManipulator.bitDestuffer (stuffedByteArray, stuffedByteArraySize, i);
                amountOfDataRead = byteArray.length;

                byte[] chunkInformation = bitManipulator.getChunkInformation ();
                bitManipulator.resetValues ();

                //bitManipulator.showByteFrameBitDeStuffed (byteArray);

                System.out.println ("Receiving from " + user + " chunk number " + (i + 1) + " amount " + amountOfDataRead + " bytes");

                System.out.println ("Sending acknowledgement frame to " + user + " of chunk number " + (i + 1) + ".");

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

                    System.out.println ("Amount of total data received from " + user + " is " + lengthChecker + " and total length of file is " + fileLength + ".");

                    bufferSize -= amountOfDataRead;
                    if (lengthChecker == fileLength) {
                        System.out.println ("File transmission completed from " + user + ".");
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
            clientList.get (receiver).fileLinkedList.add (new FileInformation (new File (fileName), fileId, user, receiver));
            System.out.println ("File received from " + user + " successfully.");
        } else {
            new File (fileName).delete ();
            bufferSize += lengthChecker;
            System.out.println ("File transmission cancelled.");
        }

    }



    private void sendFileByChunks (File file, int chunkSize) throws IOException
    {
        String clientMessage;
        long fileLength = file.length ();

        byte[] byteArray = new byte[chunkSize];
        byte[][] fileDataStorage = new byte[(int) fileLength / chunkSize + 1][];
        int[] fileDataLengthArray = new int[(int) fileLength / chunkSize + 1];

        InputStream in = new FileInputStream (file);
        OutputStream out = connection.sc.getOutputStream ();

        BitManipulator bitManipulator = new BitManipulator ();

        int amountOfDataRead, sequenceNumber = 0, lengthChecker = 0,goBackN_flag = 0;

        synchronized (connection)
        {
            connection.writeString ("Ready to receive?");
        }

        Loop:
        while (true) {

            synchronized (connection) {
                clientMessage = connection.readString ();
            }

            if (clientMessage.equals ("Ready to receive.")) {
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

                    System.out.println ("Stuffing payload for " + user + " of chunk number " + (i + 1));

                    byte[] stuffedByteArray = bitManipulator.bitStuffer (byteArray, amountOfDataRead, i);

                    //bitManipulator.showByteFrameBitDeStuffed (stuffedByteArray);

                    System.out.println ("Sending stuffed byte array size to " + user + " of chunk number " + (i + 1) + " which is " + stuffedByteArray.length + ".");

                    synchronized (connection) {
                        connection.writeString (Integer.toString (stuffedByteArray.length));
                    }

                    out.write (stuffedByteArray, 0, stuffedByteArray.length);
                    out.flush ();

                    System.out.println ("Sending chunk number to " + user + " " + (i + 1) + " amount " + amountOfDataRead + " bytes.");
                }

                connection.sc.setSoTimeout (30000);

                for (int i = goBackN_flag; i < goBackN_flag + N && i < ((int) fileLength / chunkSize) + 1; i++)
                {
                    sequenceNumber = i + 1;
                    System.out.println ("Waiting for the " + (i + 1) + "th acknowledgement from " + user + ".");
                    synchronized (connection)
                    {
                        byte[] response = (byte[])connection.read();
                        if (response[2] == (byte)0b11111111)
                        {
                            System.out.println ("Error occurred during file transmission with " + user + ". Going back to N.");
                            break;
                        }
                        System.out.println (user + " received chunk successfully.");

                        lengthChecker += fileDataLengthArray[i];
                        System.out.println ("Amount of total data sent to " + user + " is " + lengthChecker + " and total length of file is " + fileLength + ".");
                        if (lengthChecker == fileLength) {
                            break Loop;
                        }
                    }
                }

                connection.sc.setSoTimeout (0);

                goBackN_flag = sequenceNumber;

                synchronized (connection) {
                    clientMessage = connection.readString ();
                    if (clientMessage.equals ("Chunks received.")) connection.writeString ("Ready to receive?");
                }

            } else if (clientMessage.equals ("disconnect")) {
                clientList.get (user).isLoggedIn = false;
                clientList.get (user).connection.close ();
                System.out.println (user + " has disconnected from server.");
                throw new IOException ();
            }

        }

        in.close ();

        synchronized (connection) {
            clientMessage = connection.readString ();
        }

        switch (clientMessage) {
            case "File received.":
                file.delete ();
                System.out.println ("File sent to " + user + " successfully.");
                bufferSize += file.length ();
                break;
            case "File not received.":
                System.out.println ("File transmission failed.");
                throw new IOException ();
            case "disconnect":
                clientList.get (user).isLoggedIn = false;
                clientList.get (user).connection.close ();
                System.out.println (user + " has disconnected from server.");
                throw new IOException ();
        }

    }




    private void receiveFileFromClient ()
    {
        System.out.println("File receiving from " + user + " has started.");
        String clientMessage;
        synchronized (connection) {
            clientMessage = connection.readString ();
        }
        if (clientMessage.contains ("1405")){
            System.out.println (user + " asked if " + clientMessage + " is logged in.");
            if (clientList.containsKey (clientMessage)){
                System.out.println (clientMessage + " is logged in.");
                synchronized (connection) {
                    connection.writeString ("Logged in.");
                }

                synchronized (connection)
                {
                    String fileMessage = connection.readString();
                    if (fileMessage.equals("Invalid"))
                    {
                        System.out.println(user + " tried to send a nonexistent file.");
                        return;
                    }
                    else if (fileMessage.equals("Valid"))
                    {
                        System.out.println(user + " is sending a valid file.");
                    }
                }

                System.out.println (user + " is sending file name and file size.");

                String information, fileName, fileSize;

                synchronized (connection) {
                    information = connection.readString ();
                    if (information.equals ("disconnect"))
                    {
                        clientList.get(user).isLoggedIn = false;
                        clientList.get(user).connection.close();
                        System.out.println(user + " has disconnected from server.");
                        return;
                    }
                    fileName = "Server file Sender " + user + " Receiver " + clientMessage + " " + information;
                    fileSize = connection.readString ();
                }

                for (FileInformation fileInformation : clientList.get(clientMessage).fileLinkedList) {
                    if (fileInformation.file.getName().equals(fileName))
                    {
                        System.out.println("File already exists. Transmission cancelled.");
                        synchronized (connection)
                        {
                            connection.writeString ("File already exists.");
                        }
                        return;
                    }
                }

                if (Long.parseLong (fileSize) <= bufferSize){
                    System.out.println ("Enough space in server, proceeding to file transmission.");
                    try {
                        synchronized (connection)
                        {
                            connection.writeString ("Enough space.");
                        }
                        System.out.println ("Generating random chunk size.");

                        Random random = new Random ();
                        int chunkSize = (random.nextInt (10) + 11) * 1024;

                        System.out.println ("Sending fileId and chunk size to " + user);
                        synchronized (connection) {
                            connection.writeString (Integer.toString (++fileId));
                            connection.writeString (Integer.toString (chunkSize));
                        }
                        System.out.println ("Receiving file from " + user + ".");
                        receiveFileByChunks (fileName, fileSize, clientMessage, chunkSize);
                    }
                    catch (IOException e) {
                        System.out.println("Unknown problems occurred when receiving file from " + user + ".");
                    }
                } else {
                    synchronized (connection) {
                        connection.writeString ("Not enough space.");
                        System.out.printf ("File transmission didn't occur due to low space.");
                    }
                }
            } else {
                synchronized (connection) {
                    connection.writeString ("Not logged in.");
                }
                System.out.println (clientMessage + " is not logged in.");
            }
        } else {
            System.out.println("File transmission cancelled due to unknown reasons.");
        }
    }




    private void sendFileToClient ()
    {
        String clientMessage;
        synchronized (connection) {
            clientMessage = connection.readString ();
        }

        System.out.println(user + " is asking if there's any file for him/her.");

        if (clientMessage.equals("Check file"))
        {
            if (!clientList.get(user).fileLinkedList.isEmpty())
            {
                System.out.println("File exists in server for " + user + ".");

                synchronized (connection) {
                    connection.writeString ("File exists.");
                }

                System.out.println("Sending " + user + " details of the file.");

                FileInformation fileInformation = clientList.get(user).fileLinkedList.pollFirst();

                synchronized (connection) {
                    connection.writeString (fileInformation.file.getName());
                    connection.writeString (Long.toString(fileInformation.file.length()));
                    connection.writeString (fileInformation.sender);
                    connection.writeString (Integer.toString(fileInformation.fileId));
                }

                System.out.println ("Generating random chunk size.");

                Random random = new Random ();
                int chunkSize = (random.nextInt (10) + 11) * 1024;

                System.out.println ("Sending chunk size to " + user);

                synchronized (connection) {
                    connection.writeString (Integer.toString (chunkSize));
                }

                System.out.println("Sending file to " + user + ".");

                try {
                    sendFileByChunks(fileInformation.file, chunkSize);
                } catch (IOException e) {
                    System.out.println("Error occurred when trying to send file to " + user + ".");
                    System.out.println("Putting file back to the list.");
                    clientList.get(user).fileLinkedList.addFirst(fileInformation);
                }

            } else
            {
                System.out.println("There's no file in server for " + user + ".");
                synchronized (connection) {
                    connection.writeString ("File doesn't exist.");
                }
            }

        } else
        {
            System.out.println("Wrong message from " + user + ".");
        }

    }




    @Override
    public void run () {
        Loop:
        while (true){
            String clientMessage;

            synchronized (connection)
            {
                clientMessage = connection.readString();
            }

            switch (clientMessage) {
                case "send":
                    receiveFileFromClient();
                    if (clientList.get (user).connection.isClosed)
                    {
                        break Loop;
                    }
                    break;
                case "receive":
                    sendFileToClient();
                    break;
                case "logout":
                    clientList.get(user).isLoggedIn = false;
                    clientList.get(user).connection.close();
                    System.out.println(user + " has logged out successfully.");
                    break Loop;
                case "disconnect":
                    clientList.get(user).isLoggedIn = false;
                    clientList.get(user).connection.close();
                    System.out.println(user + " has disconnected from server.");
                    break Loop;
                default:
                    System.out.println(user + " typed invalid option.");
                    break;
            }

        }

    }

}
