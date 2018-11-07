package fileclient;

import java.io.*;
import java.net.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

class FileClient {

    Socket clientSocket;
    String UserID;
    String recieverID;
    BufferedReader inFromUser;
    BufferedReader inFromServer;
    DataOutputStream outStream;
    DataInputStream inStream;
    byte ED = 126;

    public FileClient(String host, int port) {
        try {
            clientSocket = new Socket(host, port);

            inFromUser = new BufferedReader(new InputStreamReader(System.in));
            inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            outStream = new DataOutputStream(clientSocket.getOutputStream());
            inStream = new DataInputStream(clientSocket.getInputStream());
            System.out.println("UserID : ");
            UserID = inFromUser.readLine();
            outStream.writeBytes(UserID + '\n');

            String msg = inFromServer.readLine();
            if (msg.equals("1")) {
                System.out.println("User Connected...");
                while (true) {
                    System.out.println("Press S to Send R to receive");
                    String dec = inFromUser.readLine();

                    if (dec.equals("S") || dec.equals("s")) {
                        outStream.writeBytes(dec + '\n');
                        sendFile(clientSocket);
                    } else if (dec.equals("R") || dec.equals("r")) {
                        outStream.writeBytes(dec + '\n');
                        getFile(clientSocket);
                    } else {
                        System.out.println("Undefined command");
                    }
                }

            } else {
                System.out.println("User already logged in");
            }

        } catch (IOException ex) {
            Logger.getLogger(FileClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String makeFrame(byte seq, byte ack, byte type, String line) throws FileNotFoundException, IOException {

        byte ED = 126;
        String sequence = String.format("%8s", Integer.toBinaryString(seq & 0xFF)).replace(' ', '0');
        String acknowledge = String.format("%8s", Integer.toBinaryString(ack & 0xFF)).replace(' ', '0');
        String frameType = String.format("%8s", Integer.toBinaryString(type & 0xFF)).replace(' ', '0');
        String endDelim = String.format("%8s", Integer.toBinaryString(ED & 0xFF)).replace(' ', '0');

        // FileReader fr = new FileReader("Bitfile" + seq + ".txt");
        // BufferedReader br = new BufferedReader(fr);
        // String line = br.readLine();
        line = frameType + sequence + acknowledge + line;

        int num = 0;
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) == '1') {
                num++;
            }
        }
        int make_chk=((num % 256) + (num >> 8));
        while(make_chk>255){
            make_chk=((make_chk % 256) + (make_chk >> 8));
        }
        
        byte checksum_uncomp = (byte) make_chk;
        String checksum = String.format("%8s", Integer.toBinaryString(~checksum_uncomp & 0xFF)).replace(' ', '0');

        line += checksum;

        int count = 0;
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) == '0') {
                count = 0;
            } else {
                count++;
            }
            if (count == 5) {
                line = line.substring(0, i + 1) + "0" + line.substring(i + 1, line.length());
                count = 0;
            }
        }

        line += endDelim;

        // FileOutputStream fos = new FileOutputStream(new File("frame" + seq + ".txt"));
        // fos.write(line.getBytes());
        // fos.flush();
        // fos.close();
        return line;
    }

    public String makeBits(byte[] array, int size, int seq) {
        FileOutputStream fos = null;
        String bitfile = "";
//        try {
            //fos = new FileOutputStream(new File("Bitfile" + seq + ".txt"));
            for (int i = 0; i < size; i++) {
                String s1 = String.format("%8s", Integer.toBinaryString(array[i] & 0xFF)).replace(' ', '0');
               // fos.write(s1.getBytes());
                bitfile += s1;
            }
            //fos.flush();
//        } catch (FileNotFoundException ex) {
//            Logger.getLogger(FileClient.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IOException ex) {
//            Logger.getLogger(FileClient.class.getName()).log(Level.SEVERE, null, ex);
//        } finally {
//            try {
//                fos.close();
//            } catch (IOException ex) {
//                Logger.getLogger(FileClient.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
        return bitfile;
    }

    private void getFile(Socket clientSock) throws IOException {

        inStream = new DataInputStream(clientSock.getInputStream());
        String msg = inStream.readLine();

        if (msg.equals("1")) {

            String fileName = inStream.readLine();
            long filesize = inStream.readLong();

            byte[] getBuffer = new byte[100];

            System.out.println("FileName: " + fileName + " FileSize: " + filesize);
            System.out.println("Receive(Y/n)?");
            String rep = inFromUser.readLine();
            outStream.writeBytes(rep + '\n');
            if (rep.equals("Y") || rep.equals("y")) {
                FileOutputStream fos = new FileOutputStream(new File("Server" + fileName));
                int read = 0;
                int totalRead = 0;
                int remaining = (int) filesize;
                while ((read = inStream.read(getBuffer, 0, Math.min(getBuffer.length, remaining))) > 0) {
                    totalRead += read;
                    remaining -= read;
                    System.out.println("read " + totalRead + " bytes.");
                    fos.write(getBuffer, 0, read);
                }

                fos.flush();
                fos.close();
                System.out.println("Recieved");

                String no = inStream.readLine();
                System.out.println("server says" + no);
                String st = inStream.readLine();
                System.out.println("server says" + st);
            }

        } else {
            System.out.println("No File is sent to you.");
        }
    }

    public void sendFile(Socket s) throws IOException {

        System.out.println("Reciever ID:");
        recieverID = inFromUser.readLine();
        outStream.writeBytes(recieverID + '\n');
        String msg = inStream.readLine();

        if (msg.equals("1")) {
            System.out.println("Insert filename: ");
            String filename = inFromUser.readLine();

            File file = new File(filename);
            FileInputStream fis = new FileInputStream(file);

            outStream.writeBytes(filename + '\n');
            outStream.writeLong(file.length());
            String msg2 = inStream.readLine();
            if (msg2.equals("1")) {
                int chunkSize = inStream.readInt();
                System.out.println("Chunk size: " + chunkSize);
                byte[] buffer = new byte[chunkSize];

                int sent = 0;
                byte numOfFrames = 0;

                String biteFile[] = new String[130];

                int read = 0;
                byte seq = 0;
                

                while (true) {
                    read = fis.read(buffer);
                    if (read > 0) {
                        biteFile[numOfFrames] = makeBits(buffer, read, numOfFrames);
                        numOfFrames++;
                    } else {
                        break;
                    }
                }

                fis.close();
                System.out.println("Number of sequences : "+numOfFrames);
                byte ack = 0;
                byte type = (byte) 0b0000000001;
                byte sentFrame = 0;
                int startframe=0;
                byte expectedFrame=0;
                
                long start = System.currentTimeMillis();
                while (true) {
                    if (numOfFrames != sentFrame) {
                        long end = System.currentTimeMillis();
                        System.out.println("Time Spent: " + (end-start)/1000+" Secs");
                        if(((end-start)/1000)>4){
                            startframe = sentFrame;
                            start=end;
                        }
                        for (int i = startframe; i < Math.min(startframe + 5, numOfFrames); i++) {
                            System.out.println("Sending Frame :" + i);
                            ack=(byte) ((expectedFrame+numOfFrames)%(numOfFrames+1));
                            String frame = makeFrame(sentFrame, ack, type, biteFile[sentFrame]);
                            outStream.writeBytes(frame + '\n');
                            System.out.println("Sent Framelength :" + frame.length());
                            String acknowledged = inStream.readLine();
                            
                            expectedFrame=(byte) inStream.readInt();
                            
                            if (acknowledged.equals("frameOk")) {
                                sentFrame++;
                            }

                        }
                        
                        //String frame = makeFrame(sentFrame, ack, type, biteFile[sentFrame]);
                        //outStream.writeBytes(frame + '\n');
                       // String acknowledged = inStream.readLine();
                       // System.out.println("Server: " + acknowledged);

                       // sentFrame++;
                    } else {
                        outStream.writeBytes("end" + '\n');
                        break;
                    }
                }

                /*                String[] frame = new String[128];

                
                numOfFrames--;
                fis.close();
                int sentframe = 0;
                int startframe = 0;
                long start = System.nanoTime();
                while (true) {
                    if (sentframe != numOfFrames) {
                        startframe = sentframe;
                        for (int i = startframe; i < Math.min(startframe + 4, numOfFrames); i++) {
                            System.out.println("Sending Frame :" + i);
                            outStream.writeBytes(frame[i] + '\n');
                            System.out.println("Sent Frame :" + sentframe);
                            String acknowledged = inStream.readLine();

                            if (acknowledged.equals("frameOk")) {
                                sentframe++;
                            }
                            else{
                                System.out.println("Server's ackknowledgement : "+acknowledged);
                            }

                        }
                    } else {
                        outStream.writeBytes("end" + '\n');
                        break;
                    }
                }
                 */
                long end = System.nanoTime();
                System.out.println("Time elapsed : " + (end - start) + "nanosecs");
                outStream.writeBytes("ok" + '\n');
                outStream.writeBytes("sent" + '\n');
            } else {
                System.out.println("Server cannot take any more files");
            }

        } else {
            System.out.println(msg);
            System.out.println("Receipient is Offline");
        }
    }

    public static void main(String argv[]) throws Exception {
        FileClient tc = new FileClient("localhost", 7789);
    }
}
