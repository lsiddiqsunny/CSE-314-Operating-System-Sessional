package Server;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Random;
import Stuffing.*;

import static Server.Server.CHUNK_SIZE;
import static Server.Server.REDUCED_CHUNK;

class ConnectionThread implements Runnable{
    private Socket clientSocket;
    private HashMap<String,Socket> clientList;
    private HashMap<Integer,byte[][]> files;

    public ConnectionThread(Socket socket,HashMap<String,Socket> cL,HashMap<Integer,byte[][]> fL){
        clientList = cL;
        files = fL;
        clientSocket = socket;
        new Thread(this).start();
    }


    @Override
    public void run() {
        try{
            BufferedReader bw = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String s = bw.readLine();
            PrintWriter pw = new PrintWriter(clientSocket.getOutputStream());

            while (clientList.containsKey(s)){
                pw.println("-1");
                pw.flush();
                s = bw.readLine();
            }
            String firstID = s;
            pw.println("0");
            pw.flush();
            clientList.put(s,clientSocket);
            s = bw.readLine();
            String filename = null;
            if (s.equals("0")) {
                String receiver = bw.readLine();
                if (clientList.containsKey(receiver) == false) {
                    //System.out.println("nai");
                    pw.println("-1");
                    pw.flush();
                }
                else {
                    //System.out.println("ase");
                    pw.println("-0");
                    pw.flush();
                    //System.out.println("hi");
                    filename = bw.readLine();

                    int chunk = 150;

                    int remaining = Integer.parseInt(bw.readLine());
                    int filesize = remaining;

                    if (CHUNK_SIZE < REDUCED_CHUNK + remaining) {
                        pw.println("-1");
                        pw.flush();
                    } else {
                        pw.println("0");
                        pw.flush();
                        REDUCED_CHUNK += remaining;
                        DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
                        DataOutputStream ack = new DataOutputStream(clientSocket.getOutputStream());
                        Stuffdestuff destuffer = new Stuffdestuff();
                        int chunk_no = remaining / chunk;
                        byte[][] mybyte = new byte[chunk_no + 1][chunk];
                        byte[] frame = new byte[chunk + 4];
                        byte[] recByte = new byte[chunk + 35];
                        Stuffdestuff deStuffer = new Stuffdestuff();
                        int key = (firstID + receiver).hashCode();
                        files.put(key, mybyte);
                        pw.println(key);
                        pw.flush();
                        int i = 0;
                        byte waitingFor = 0;
                        while (true) {
                            if (remaining == 0) break;
                            System.out.println("Waiting for frame : "+ (waitingFor & 0xFF));
                            dis.read(recByte);
                            deStuffer.deStuffBit(recByte, frame);
                            Frame f = deStuffer.getFrame(frame);
                            System.out.println(f.getSec_no() & 0xFF);
                            if (deStuffer.hasChecksumError(frame, f.getChecksum())) {
                                System.out.println("Found error");
                                continue;
                            }
                            if (f.getSec_no() == waitingFor) {
                                if (remaining > chunk) remaining -= chunk;
                                else remaining = 0;
                                byte[] payload = new byte[1];
                                byte[] sendBytes = new byte[8];
                                byte[] tobeStuffed = new byte[5];
                                payload[0] = (byte) 0;
                                Frame f1 = new Frame((byte) 2, (byte) 0, waitingFor, payload);
                                waitingFor++;
                                mybyte[i] = f.getPayload();
                                i++;
                                deStuffer.byteFrame(f1, tobeStuffed);
                                deStuffer.stuffBit(tobeStuffed, sendBytes);
                                ack.write(sendBytes);
                            } else continue;
                        }
                        Socket so = clientList.get(receiver);
                        so.getOutputStream().write(20);

                        BufferedReader bz = new BufferedReader(new InputStreamReader(so.getInputStream()));
                        PrintWriter pz = new PrintWriter(so.getOutputStream());
                        pz.println(filesize);
                        pz.flush();
                        pz.println(filename);
                        pz.flush();
                        pz.println(firstID);
                        pz.flush();
                        int s7 = so.getInputStream().read();
                        //so.getOutputStream().write(chunk);
                        if (s7 == 5) so.getOutputStream().write(5);
                        if (bz.readLine().equals("1")) {

                            DataOutputStream dos = new DataOutputStream(so.getOutputStream());
                            i = 0;
                            remaining = filesize;
                            while (remaining > chunk) {
                                dos.write(mybyte[i]);
                                remaining -= chunk;
                                i++;
                            }
                            dos.write(mybyte[i], 0, remaining);
                            dos.close();
                        }
                        REDUCED_CHUNK -= filesize;
                        files.remove(key);
                    }
                }
            }
            else if(s.equals("1")){
                clientList.remove(firstID);
                return;
            }
            clientList.remove(firstID);
            // System.out.println(firstID+" out");
        }catch (Exception e){

        }
    }
}
