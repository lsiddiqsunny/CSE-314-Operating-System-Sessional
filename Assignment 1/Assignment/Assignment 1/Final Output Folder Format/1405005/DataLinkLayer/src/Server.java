import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by Nayeem Hasan on 19-Sep-17.
 */
public class Server {

    private ArrayList<String> loggedInList;
    private ArrayList<HashMap<String, SocketDetail>> outStreamList;
    Thread serverThread;


    int bufferSize, curSize, curId;

    public Server() {
        loggedInList = new ArrayList<>();
        outStreamList = new ArrayList<>();

        bufferSize = 100000;
        curSize = 0;
        curId = 1;
    }

    private void setUpNetworking() {
        try {
            ServerSocket sock = new ServerSocket(10100);
            System.out.println("Server has started");

            while (true) {
                Socket s = sock.accept();

                serverThread = new Thread(new ClientRunnable(s));
                serverThread.start();
//                try {
//                    serverThread.join();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
            }
        }catch (SocketException e){
            System.out.println("A client goes offline");
        }catch (IOException e){
            e.printStackTrace();
        }
    }


    public void go() {
        setUpNetworking();
    }

    public static void main(String[] args) {
        new Server().go();
    }


    public class ClientRunnable implements Runnable {

        Socket socket;
        Socket sendByteSocket;

        ObjectInputStream objectInputStream;
        ObjectOutputStream objectOutputStream;

        private FileDetail fileDetail;
        private String senderId;

        ArrayList<byte[]> buffer;

        String senderOrReceiver;

        Stuff stuff;
        int seqNo;

        public ClientRunnable(Socket socket) {
            stuff = new Stuff();
            seqNo = 1;
            this.socket = socket;
            try {
                objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                objectInputStream = new ObjectInputStream(socket.getInputStream());

                senderOrReceiver = "N";

                buffer = new ArrayList<>();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            checkLogin();
            if (senderOrReceiver.equals("Y") || senderOrReceiver.equals("y")) {
                checkBuffer();
            }
        }

        private void checkLogin() {
            try {
                String id = null;
                id = (String) objectInputStream.readObject();

                System.out.println("ID No "+ id + " Logged In");
                senderId = id;

                for (int i = 0; i < loggedInList.size(); i++) {
                    if (loggedInList.get(i).equals(id)) {
                        objectOutputStream.writeObject("Already logged in");
                        socket.close();
                        break;
                    }
                }

                if (socket.isClosed() == false) {
                    objectOutputStream.writeObject("Not Already logged in");
                    loggedInList.add(id);

                    HashMap<String, SocketDetail> p = new HashMap<>();
                    SocketDetail socketDetail = new SocketDetail(socket, objectOutputStream, objectInputStream);
                    p.put(id, socketDetail);
                    outStreamList.add(p);
                }

                senderOrReceiver = (String) objectInputStream.readObject();

            } catch (Exception e){

            }
        }

        private void checkBuffer() {
            try {
                String fileName;
                int fileSize;
                fileDetail = null;
                try {
                    while (fileDetail == null) fileDetail = (FileDetail) objectInputStream.readObject();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                fileName = fileDetail.getFileName();
                fileSize = fileDetail.getFileSize();

                if (curSize + fileSize > bufferSize) {
                    objectOutputStream.writeObject("File transmission cannot be done");
                } else {
                    objectOutputStream.writeObject("File transmission can be done");
                    setUpFile();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        private void setUpFile() {
            fileDetail.setFileId(curId++);
            fileDetail.setSenderID(senderId);

            Random random = new Random();
            int x = random.nextInt(50) + 50;
            fileDetail.setChunkSize(x);

            try {
                objectOutputStream.writeObject(fileDetail);
            } catch (IOException e) {
                e.printStackTrace();
            }

            receiveChunks();
            //goBackNReceive();
        }



        private void receiveChunks() {

            try {

                int iter = fileDetail.getFileSize() / fileDetail.getChunkSize();
                int tempIter = iter;

                int lastChunkSize = fileDetail.getFileSize() - iter * fileDetail.getChunkSize();

                if (lastChunkSize != 0) iter++;
                String frame, ackFrame, tempFrame, payload;

                byte[] chunks;
                int k = 0;

                for (int i = 0; i < iter; i++) {
                    k++;

                    if (lastChunkSize != 0 && i == iter - 1) chunks = new byte[lastChunkSize];
                    else chunks = new byte[fileDetail.getChunkSize()];

                    tempFrame = (String) objectInputStream.readObject();
                    frame = stuff.destuffBits(tempFrame);
                    payload = frame.substring(16, frame.length() - 8);

                    if (Integer.parseInt(frame.substring(0, 8), 2) == 1 &&
                            seqNo == Integer.parseInt((frame.substring(8, 16)), 2) &&
                            stuff.hasNoChecksumError(payload, frame.substring(frame.length() - 8, frame.length()))
                            && k!=2)
                    {

                        System.out.println("Received frame no " + seqNo + " : " + tempFrame);
                        System.out.println("Desdtuffed frame no :"+seqNo+" : "+frame.substring(0,8)+"|"+
                                frame.substring(8,16)+"|"+payload+"|"+
                                frame.substring(frame.length() - 8, frame.length()));
                        System.out.println();

                        int j = 0;
                        for (String str : payload.split("(?<=\\G.{8})")) {
                            chunks[j++] = Byte.parseByte(str, 2);
                        }
                        buffer.add(chunks);

                        Thread.sleep(2000);

                        ackFrame = stuff.makeAckFrame(2, seqNo);
                        seqNo++;
                        objectOutputStream.writeObject(ackFrame);
                    }
                    else i--;
                }

                objectOutputStream.reset();

                String allChunks = (String) objectInputStream.readObject();
                if (allChunks.equals("All Chunks Sent")) System.out.println("All Chunks Uploaded");

                if ((tempIter * fileDetail.getChunkSize() + lastChunkSize ) == fileDetail.getFileSize()) {
                    curSize = curSize + fileDetail.getFileSize();
                    objectOutputStream.writeObject("Size matched");
                    getReceiver();
                    askReceiver();
                }

                else {
                    buffer.clear();
                    objectOutputStream.writeObject("Size dismatched");
                }



            } catch (Exception e) {
                //e.printStackTrace();
            }
        }

        private void getReceiver() {

            String receiverID;
            try {
                receiverID = (String) objectInputStream.readObject();
                fileDetail.setReceiverID(receiverID);

            } catch (Exception e) {

            }
        }

        private void askReceiver() {

            try {
                int i;
                for (i = 0; i < loggedInList.size(); i++) {
                    if (loggedInList.get(i).equals(fileDetail.getReceiverID())) break;
                }

                if (i == loggedInList.size()) {
                    buffer.clear();
                    curSize = curSize - fileDetail.getFileSize();
                    objectOutputStream.writeObject("Offline");
                }

                else {
                    objectOutputStream.writeObject("Online");

                    sendByteSocket = outStreamList.get(i).get(loggedInList.get(i)).getSocket();


                    String prompt = "Do you want to receive " + fileDetail.getFileName() +
                            " having size of " + fileDetail.getFileSize() + " bytes from ID No:" +
                            fileDetail.getSenderID() + "? (Y/N)";

                    ObjectOutputStream oos = outStreamList.get(i).get(loggedInList.get(i)).getObjectOutputStream();
                    ObjectInputStream ois = outStreamList.get(i).get(loggedInList.get(i)).getObjectInputStream();

                    oos.writeObject(prompt);
                    String promptAns = (String) ois.readObject();

                    oos.writeObject(fileDetail);

                    if (promptAns.equals("Y") || promptAns.equals("y")) {

                        for (byte[] b : buffer) {
                            sendByteSocket.getOutputStream().write(b);
                        }
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}

class SocketDetail{

    Socket socket;
    ObjectOutputStream objectOutputStream;
    ObjectInputStream objectInputStream;

    public SocketDetail(Socket socket, ObjectOutputStream objectOutputStream, ObjectInputStream objectInputStream) {
        this.socket = socket;
        this.objectOutputStream = objectOutputStream;
        this.objectInputStream = objectInputStream;
    }

    public Socket getSocket() {
        return socket;
    }

    public ObjectOutputStream getObjectOutputStream() {
        return objectOutputStream;
    }

    public ObjectInputStream getObjectInputStream() {
        return objectInputStream;
    }
}




