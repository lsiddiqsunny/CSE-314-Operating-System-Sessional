/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Scanner;

/**
 *
 * @author USER
 */
public class Client {

    private static Socket socket = null;
    private static BufferedReader br = null;
    private static PrintWriter pr = null;
    private static int ID;
    static long FileSize;
    private static String payload;
    private static String frame = "";
    static String frame1;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        // TODO code application logic here

        Scanner input = new Scanner(System.in);
        String send = null;
        String line;
        String FileName;
        FileInputStream IS;
        OutputStream OS;
        InputStream is;
        FileOutputStream fos;
        String ht = "01111110";

        byte num = 1;
        byte ack_no = 0;
        String stuffed;
        String stuffed1;
        String real_stuffed;
        try {
            socket = new Socket("localhost", 1558);
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            pr = new PrintWriter(socket.getOutputStream());
            System.out.println("Connected to server");
            socket.setSoTimeout(50);

        } catch (Exception e) {
            System.err.println("Problem connecting to the server");
            System.exit(1);
        }

        System.out.println("Enter your ID: ");
        while (true) {
            try {

                send = input.nextLine();
            } catch (Exception ex) {
                continue;
            }
            pr.println(send);
            pr.flush();

            line = br.readLine();
            if (!(line.equalsIgnoreCase("yes"))) {
                System.out.println("You are already logged in with IP address: " + line);
                if (socket != null) {
                    socket.close();
                    break;
                }
            }
            ID = Integer.parseInt(send);
            while (true) {
                System.out.println("Type : Exit/Send/Receive");
                try {
                    send = input.nextLine();
                } catch (Exception ex) {
                    continue;
                }
                pr.println(send);
                pr.flush();
                if (send.equalsIgnoreCase("Exit")) {
                    System.out.println("Client wants to exit the connection. Exiting......");
                    break;
                } else if (send.equalsIgnoreCase("send")) {

                    File f = new File("text1.txt");
                    FileName = f.getName();
                    pr.println(FileName);
                    pr.flush();
                    long FileSize = f.length();
                    pr.println(FileSize);
                    pr.flush();
                    IS = new FileInputStream(f);
                    OS = socket.getOutputStream();
                    int chunk = 1;
                    pr.println(chunk);
                    pr.flush();
                    long count = 0;
                    while (count != FileSize) {
                        byte[] buffer = new byte[chunk];
                        if (FileSize - count >= chunk) {
                            IS.read(buffer, 0, chunk);
                            count += chunk;

                            String seq = Integer.toBinaryString(num & 255 | 256).substring(1);
                            String ack = Integer.toBinaryString(ack_no & 255 | 256).substring(1);

                            payload = Integer.toBinaryString(buffer[0] & 255 | 256).substring(1);

                            System.out.println("Payload: " + buffer[0]);
                            String e = Make_Error(payload);
                            stuffed = bitStuff(e, chunk);
                            real_stuffed=bitStuff(payload,chunk);
                            int sum = 0;
                            int limit = chunk * 8;
                            for (int i = 0; i < limit; i++) {
                                if (payload.charAt(i) == '1') {
                                    sum++;
                                }
                            }
                            String c = Integer.toBinaryString((byte) sum & 255 | 256).substring(1);
                            stuffed1 = BitStuff_C(c);
                            pr.println(stuffed1);
                            pr.flush();
                            pr.println(stuffed);
                            pr.flush();
                            frame = "01111110" + "00000001" + seq + ack + stuffed + stuffed1 + "01111110";
                            frame1="01111110" + "00000001" + seq + ack + real_stuffed + stuffed1 + "01111110";
                            pr.println(frame);
                            pr.flush();
                            OS.flush();
                            System.out.println("Frame sent: " + frame1);

                            while (true) {
                                try {
                                    line = br.readLine();
                                    if (Integer.parseInt(line) == -1) {
                                        pr.println(frame1);
                                        pr.flush();
                                        continue;
                                    } else {

                                        num++;
                                        break;

                                    }
                                } catch (SocketTimeoutException ex) {
                                    System.err.println("Session Timed Out");
                                    if (socket != null) {
                                        socket.close();
                                    }
                                }

                            }

                        } else {
                            chunk = (int) (FileSize - count);
                            IS.read(buffer, 0, chunk);
                            count = FileSize;
                            String seq = Integer.toBinaryString(num & 255 | 256).substring(1);
                            String ack = Integer.toBinaryString(ack_no & 255 | 256).substring(1);

                            payload = Integer.toBinaryString(buffer[0] & 255 | 256).substring(1);
                            String E = Make_Error(payload);
                            stuffed = bitStuff(E, chunk);
                            real_stuffed=bitStuff(payload,chunk);
                            int limit = chunk * 8;
                            int sum = 0;
                            for (int i = 0; i < limit; i++) {
                                if (payload.charAt(i) == '1') {
                                    sum++;
                                }
                            }
                            String c = Integer.toBinaryString((byte) sum & 255 | 256).substring(1);
                            stuffed1 = BitStuff_C(c);
                            pr.println(stuffed1);
                            pr.flush();
                            pr.println(stuffed);
                            pr.flush();
                            
                            frame = "01111110" + "00000001" + seq + ack + stuffed + stuffed1 + "01111110";
                            frame1="01111110" + "00000001" + seq + ack + real_stuffed + stuffed1 + "01111110";
                            pr.println(frame);
                            pr.flush();

                            OS.flush();
                            System.out.println("Frame sent: " + frame1);

                            while (true) {
                                try {
                                    line = br.readLine();
                                    if (Integer.parseInt(line) == -1) {
                                        pr.println(frame1);
                                        pr.flush();
                                    } else {
                                        num++;
                                        break;
                                    }
                                } catch (SocketTimeoutException ex) {
                                    System.err.println("Session Timed Out");
                                    if (socket != null) {
                                        socket.close();
                                    }
                                }
                            }

                        }
                        //num++;
                    }

                } else if (send.equalsIgnoreCase("Receive")) {
                    line = br.readLine();

                    try {
                        is = socket.getInputStream();

                        fos = new FileOutputStream("Copy.jpg");

                        byte[] buffer = new byte[1024];
                        int count;

                        while ((count = is.read(buffer)) >= 0) {
                            fos.write(buffer, 0, count);
                        }

                        fos.flush();

                        System.out.println("Received Filename: " + line);
                        System.out.println("Saved To: " + "Copy.jpg");

                        System.out.println("File Received");

                    } catch (Exception ex) {
                        System.err.print("Error Receiving file");
                    }
                }
            }
        }
    }

    public static String bitStuff(String s, int chunk) {
        int count = 0;
        String res = "";
        int limit = chunk * 8;
        for (int i = 0; i < limit; i++) {
            if (s.charAt(i) == '1') {
                count++;
                res += s.charAt(i);
            } else {
                count = 0;
                res += s.charAt(i);

            }
            if (count == 5) {
                res += '0';
                count = 0;
            }
        }
        return res;
    }

    public static String BitStuff_C(String s) {
        int count = 0;
        String res = "";
        for (int i = 0; i < 8; i++) {
            if (s.charAt(i) == '1') {
                count++;
                res += s.charAt(i);
            } else {
                count = 0;
                res += s.charAt(i);

            }
            if (count == 5) {
                res += '0';
                count = 0;
            }
        }
        return res;
    }

    public static String Make_Error(String s) {
        String str1 = "";
        str1+=s.charAt(0);
        if(s.charAt(1)=='0')
        {
            str1+='1';
        }
        else
        {
            str1+='0';
        }
        for(int w=2;w<8;w++)
        {
            str1+=s.charAt(w);
        }
        return str1;
    }
}
