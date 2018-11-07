package Client;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Scanner;
import Stuffing.*;

class FileSend implements Runnable {
    Socket socket;
    PrintWriter pw;
    BufferedReader br;

    public FileSend(Socket s, PrintWriter p, BufferedReader b) {
        socket = s;
        pw = p;
        br = b;
        new Thread(this).start();
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        String s = scanner.next();
        if (s.equals("S")) {
            pw.println("0");
            pw.flush();
            System.out.print("Enter receiver ID : ");
            s = scanner.next();
            pw.println(s);
            pw.flush();
            try {
                s = br.readLine();
                if (s.equals("0")) {
                    System.out.print("Enter File Path : ");
                    File file = new File(scanner.next());
                    if (file == null) System.out.println("Specified file not found");
                    else {
                        try {
                            pw.println(file.getName());
                            pw.flush();
                            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            int chunk = 150;
                            Stuffdestuff stuffer = new Stuffdestuff();
                            PrintWriter pw = new PrintWriter(socket.getOutputStream());
                            pw.println(file.length());
                            pw.flush();
                            if (br.readLine().equals("-1")) {
                                System.out.println("File is too big for server");
                                return;
                            }
                            int id = Integer.parseInt(br.readLine());
                            System.out.println("File id is " + id);
                            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                            DataInputStream dis = new DataInputStream(socket.getInputStream());
                            FileInputStream fis = new FileInputStream(file);
                            byte[] mybyte = new byte[chunk];
                            byte[] frame = new byte[chunk+4];
                            int read;
                            byte seqNo = 0;
                            byte waitingAck = 0;
                            //System.out.println(chunk);
                            int remaining = (int) file.length();
                            ArrayList<byte[]>sentList = new ArrayList<>();
                            for (int i = 0; i < 5; i++){
                                if (remaining == 0)
                                    break;
                                byte[] sendByte = new byte[chunk+35];
                                if (remaining > chunk){
                                    read = fis.read(mybyte);
                                    Frame f = new Frame((byte) 1,seqNo,(byte) 0,mybyte);
                                    stuffer.byteFrame(f,frame);
                                    stuffer.stuffBit(frame, sendByte);
                                    remaining -= read;
                                }
                                else {
                                    byte[] mybyte1 = new byte[remaining];
                                    byte[] frame1 = new byte[remaining+4];
                                    Frame f = new Frame((byte)1, seqNo, (byte) 0, mybyte1);
                                    stuffer.byteFrame(f,frame1);
                                    stuffer.stuffBit(frame1, sendByte);
                                    remaining = 0;
                                }
                                sentList.add(seqNo & 0xFF,sendByte);
                                if (Math.random() > 0.1) {
                                    dos.write(sendByte);
                                    System.out.println("Sender sent frame with sequence no : "+ (seqNo & 0xFF));
                                }
                                else System.out.println("Sender lost frame with sequence no : "+ (seqNo & 0xFF));
                                seqNo++;
                            }
                            while (waitingAck != seqNo){
                                byte[] ackstuffedByte = new byte[8];
                                byte[] ackByte = new byte[5];
                                try {
                                    socket.setSoTimeout(100);
                                    dis.read(ackstuffedByte);
                                    stuffer.deStuffBit(ackstuffedByte, ackByte);
                                    if (ackByte[0] == 2 && ackByte[2] == waitingAck) {
                                        System.out.println("Received acknowledgement for sequence no "+waitingAck);
                                        waitingAck++;
                                    }
                                    if ((ackByte[2] & 0xFF) == seqNo)
                                        break;
                                    byte[] sendByte = new byte[chunk+35];
                                    if (remaining != 0){
                                        if (remaining > chunk){
                                            read = fis.read(mybyte);
                                            Frame f = new Frame((byte) 1,seqNo,(byte) 0,mybyte);
                                            stuffer.byteFrame(f,frame);
                                            stuffer.stuffBit(frame, sendByte);
                                            remaining -= read;
                                        }
                                        else {
                                            byte[] mybyte1 = new byte[remaining];
                                            byte[] frame1 = new byte[remaining+4];
                                            Frame f = new Frame((byte)1, seqNo, (byte) 0, mybyte1);
                                            stuffer.byteFrame(f,frame1);
                                            stuffer.stuffBit(frame1, sendByte);
                                            remaining = 0;
                                        }
                                        sentList.add(sendByte);
                                        if (Math.random() > 0.1) {
                                            dos.write(sendByte);
                                            dos.flush();
                                            System.out.println("Sender sent frame with sequence no : "+ (seqNo & 0xFF));
                                        }
                                        else System.out.println("Sender lost frame with sequence no : "+ (seqNo & 0xFF));
                                        seqNo++;
                                    }
                                } catch (SocketTimeoutException ket) {
                                    for (int a = waitingAck & 0xFF; a < Math.min( (waitingAck & 0xFF)+5, seqNo); a++) {
                                        dos.write(sentList.get(a));
                                        System.out.println("Resending frame  " + a);
                                        //stuffer.stuffBit(sentList.get(i),sendByte);
                                        System.out.println(sentList.get(0)[2]);
                                    }
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    System.out.println("Receiver is offline\nPlease try again later");
                }
                // this.send(new File(scanner.next()),s);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (s.equals("R")) {
            try {
                socket.getOutputStream().write(5);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}