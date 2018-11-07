import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by Nayeem Hasan on 19-Sep-17.
 */

public class Client {

    private Socket socket;

    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;

    private String id;
    private File file;
    private FileDetail fileDetail;

    private void inquireID(){


        System.out.println("Enter ID");

        Scanner scanner = new Scanner(System.in);
        id = scanner.nextLine();
        try {
            objectOutputStream.writeObject(id);

            String loggedIn = (String) objectInputStream.readObject();

            if (loggedIn.equals("Already logged in")) {
                System.out.println("Already logged in, Login failed");
                return;
            } else System.out.println("Login Successful");


            System.out.println("Do you want to send any file? (Y/N)");
            String string = scanner.nextLine();
            objectOutputStream.writeObject(string);

            if (string.equals("Y") || string.equals("y")) {
                Thread senderThread = new Thread(new SendingClient());
                senderThread.start();

            } else if (string.equals("N") || string.equals("n")) {
                Thread receiverThread = new Thread(new ReceivingClient());
                receiverThread.start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void setUpNetworking(){
        try {
            String localIP = InetAddress.getLocalHost().getHostAddress();
            socket = new Socket(localIP, 10100);

            socket.setSoTimeout(10000);

            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void go(){
        setUpNetworking();
        inquireID();
    }



    public static void main(String[] args) {
        new Client().go();
    }


    public class ReceivingClient implements Runnable{

        public void receivePrompt(){

            try {
                System.out.println("Wait Till Any File Receive Request");
                String ansPrompt = (String) objectInputStream.readObject();
                System.out.println(ansPrompt);

                Scanner scanner = new Scanner(System.in);
                String ynPrompt = scanner.nextLine();
                objectOutputStream.writeObject(ynPrompt);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        public void receiveFile(){

            try {

                System.out.println("Enter File Location");

                Scanner scanner = new Scanner(System.in);
                String path = scanner.nextLine();

                File file = new File(path);
                file.getParentFile().mkdir();
                file.createNewFile();

                FileOutputStream fos = new FileOutputStream(file);

                FileDetail fileDetail1 = (FileDetail) objectInputStream.readObject();

                int iterate = fileDetail1.getFileSize() / fileDetail1.getChunkSize();

                for (int i = 0; i < iterate; i++) {
                    byte[] bytes = new byte[fileDetail1.getChunkSize()];
                    socket.getInputStream().read(bytes);
                    fos.write(bytes);
                }
                int lastChunk = fileDetail1.getFileSize() - iterate*fileDetail1.getChunkSize();

                if (lastChunk != 0){
                    byte[] lastBytes = new byte[lastChunk];
                    socket.getInputStream().read(lastBytes);
                    fos.write(lastBytes);
                }

                fos.close();
                System.out.println("File Transferred Successfully");

            } catch (Exception e){

            }
        }

        @Override
        public void run() {
            receivePrompt();
            receiveFile();
        }
    }

    public class SendingClient implements Runnable {

        Stuff stuff;
        int seqNo;

        ArrayList<String> frames;

        SendingClient(){
            stuff = new Stuff();
            seqNo = 1;
            frames = new ArrayList<>();
        }

        private void inquireFile(){
            System.out.println("Enter File Path");

            file = new File(new Scanner(System.in).nextLine());
            String fileName = file.getName();
            int fileSize = (int) file.length();

            fileDetail = new FileDetail(fileName, fileSize);
            try {
                objectOutputStream.writeObject(fileDetail);
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                String bufferOverdrawn = (String) objectInputStream.readObject();
                if (bufferOverdrawn.equals("File transmission cannot be done"))
                    System.out.println("Buffer Size Overdrawn");

                else
                    getConfirm();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void getConfirm(){
            fileDetail = null;
            try {
                while (fileDetail == null) fileDetail = (FileDetail) objectInputStream.readObject();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            System.out.println("File ID "+fileDetail.getFileId()+" chunk size "+fileDetail.getChunkSize());
            System.out.println("Start sending chunks");

            sendChunks();
        }

        private void goBackN(){
            int window = 3;
            boolean frameTimeout = false;
            String ackStr;
            int ackNo;

            int lastSent = -1;
            int lastAck = -1;

            while (true) {

                while (lastSent - lastAck < window && lastSent < frames.size()-1) {

                    try {
                        if (Math.random() > 0.25 || lastSent == frames.size()-2)
                            objectOutputStream.writeObject(frames.get(lastSent+1));
                        System.out.println("Sending frame no " + String.valueOf(lastSent+2));

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    lastSent++;
                }

                try {
                    ackStr = (String) objectInputStream.readObject();

                    if (Integer.parseInt(ackStr.substring(0,8),2) == 2 &&
                            lastAck+2 == Integer.parseInt((ackStr.substring(8,16)), 2))
                    {
                        int temp = lastAck+2;
                        System.out.println();
                        System.out.println("Acknowledge received for frame no "+ temp);
                        System.out.println();
                        lastAck++;
                    }

                    if (lastAck == frames.size()-1) break;
                } catch (SocketTimeoutException e) {

                    System.out.println();
                    System.out.println("Time out for frame no " + String.valueOf(lastAck+2));
                    System.out.println();

                    for (int i = lastAck+1; i <= Math.min(lastAck+window, frames.size()-1); i++){
                        try {
                            if (Math.random() > 0.25) objectOutputStream.writeObject(frames.get(i));
                            System.out.println("Resending frame no " + String.valueOf(i + 1));

                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        private void sendChunks(){
            try {
                FileInputStream fis = new FileInputStream(file);
                byte[] chunk = new byte[fileDetail.getChunkSize()];

                int iter = fileDetail.getFileSize() / fileDetail.getChunkSize();
                String confirm = new String("abc");

                for(int i=0; i<iter; i++){
                    fis.read(chunk);
                    String frame = stuff.makeFrame(1, seqNo++, chunk);
                    frames.add(frame);
                }

                int lastChunkSize = fileDetail.getFileSize() - iter*fileDetail.getChunkSize();
                if (lastChunkSize != 0) {
                    byte[] lastChunk = new byte[lastChunkSize];
                    fis.read(lastChunk);

                    String lastFrame = stuff.makeFrame(1, seqNo, lastChunk);
                    frames.add(lastFrame);
                }

                goBackN();

                objectOutputStream.writeObject("All Chunks Sent");
                System.out.println("done!");

                String sizeMatching = (String) objectInputStream.readObject();
                if (sizeMatching.equals("Size matched")) {
                    System.out.println("File upload to server succeed");

                    inquireReceiver();
                }

                else if (sizeMatching.equals("Size dismatched")) System.out.println("File upload to server failed");

            } catch (Exception e) {

                e.printStackTrace();
            }

        }

        private void inquireReceiver(){
            System.out.println("Enter Receiver ID");
            String receiverID;
            Scanner scanner = new Scanner(System.in);
            receiverID = scanner.nextLine();
            try {
                objectOutputStream.writeObject(receiverID);

                String receiverStatus = (String) objectInputStream.readObject();

                if (receiverStatus.equals("Offline")) System.out.println("Receiver is Offline.Transfer Failed");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        public void run() {
            inquireFile();
        }
    }
}



