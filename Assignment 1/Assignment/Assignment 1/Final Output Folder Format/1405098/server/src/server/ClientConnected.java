/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author USER
 */
public class ClientConnected implements Runnable {

    private Socket socket;
    private InputStream IS;
    private OutputStream OS;
    private int id;
    static int FileSize;
    FileOutputStream fos;
    FileInputStream fis;
    private static byte[] array;
    static String FileName;
    private static int[] seq_arr;
    static String res = "";
    static String checksum = "";

    public ClientConnected(Socket s, int id) {
        this.socket = s;
        this.id = id;

        try {
            this.IS = s.getInputStream();
            this.OS = s.getOutputStream();

        } catch (Exception e) {
            System.err.println("Problem connecting with client [" + id + "] .");
        }

    }

    @Override
    public void run() {
        BufferedReader br = new BufferedReader(new InputStreamReader(this.IS));
        PrintWriter pr = new PrintWriter(this.OS);
        String str;
        Scanner input = new Scanner(System.in);

        while (true) {
            try {

                if ((str = br.readLine()) != null) {
                    if (str.equalsIgnoreCase("Exit")) {
                        System.out.println("Client " + id + " wants to EXIT now. Connection will be terminated....");
                        break;
                    } else if (str.equalsIgnoreCase("send")) {

                        /* try {
                            String s = br.readLine();
                            int Rid = Integer.parseInt(s);
                            if (Server.OnlineClient[Rid % 120] == "") {
                                pr.println("Not");
                                pr.flush();
                                break;
                            } 
                         */
                        FileName = br.readLine();
                        String length = br.readLine();
                        FileSize = Integer.parseInt(length);
                        str = br.readLine();
                        int chunk_size = Integer.parseInt(str);
                        /*      if (FileSize % chunk_size == 0) {
                            seq_arr = new int[FileSize % chunk_size];
                        } else {
                            seq_arr = new int[(FileSize / chunk_size) + 1];
                        }

                        //    System.out.println("Enter buffer size: ");
                        //   int i = input.nextInt();
                        /*   if (FileSize > i) {
                            // System.err.println("Buffer Overflow!");
                            pr.println("Not");
                            pr.flush();
                            continue;
                        }
                         */
                        int cc = 0;
                        fos = new FileOutputStream("Copy1.txt");
                        byte buffer[] = new byte[chunk_size];
                        while (cc != FileSize) {

                            str = br.readLine();
                            int c_size = str.length();
                            str = br.readLine();
                            int payload_size = str.length();
                            str = br.readLine();
                            String frame = "";
                            frame += str;

                            /*   
                             */
                            System.out.println("Frame received :" + frame);
                            int i = 32, j = 0, counter = 0;
                            int limit = 32 + payload_size;
                            int c = 0, k = 0;
                            res = "";
                            checksum = "";
                            while (j != (frame.length() - (40 + c_size))) {
                                if (frame.charAt(i) == '1') {
                                    counter++;
                                    res += frame.charAt(i);
                                } else {
                                    res += frame.charAt(i);
                                    counter = 0;
                                }
                                if (counter == 5) {
                                    counter = 0;
                                    i++;
                                    j++;
                                }
                                i++;
                                j++;

                            }
                            while (k != (frame.length() - (40 + payload_size))) {
                                if (frame.charAt(limit) == '1') {
                                    c++;
                                    checksum += frame.charAt(limit);
                                } else {
                                    checksum += frame.charAt(limit);
                                    c = 0;
                                }
                                if (c == 5) {
                                    c = 0;
                                    k++;
                                    limit++;

                                }
                                k++;
                                limit++;
                            }
                            //OS.flush()

                            System.out.println("Payload: " + res);
                            System.out.println("Checksum: " + checksum);
                            while (true) {
                                int count1 = 0;
                                for (int m = 0; m < res.length(); m++) {
                                    if (res.charAt(m) == '1') {
                                        count1++;
                                    }
                                }

                                if ((Integer.parseInt(checksum, 2)) != count1) {
                                    pr.println("-1");
                                    pr.flush();
                                    str = br.readLine();
                                    int i1 = 32, j1 = 0, counter1 = 0;
                                    int limit2 = 32 + payload_size;
                                    int c1 = 0, k1 = 0;
                                    res = "";
                                    checksum = "";
                                    while (j1 != (str.length() - (40 + c_size))) {
                                        if (str.charAt(i1) == '1') {
                                            counter1++;
                                            res += str.charAt(i1);
                                        } else {
                                            res += str.charAt(i1);
                                            counter1 = 0;
                                        }
                                        if (counter1 == 5) {
                                            counter1 = 0;
                                            i1++;
                                            j1++;
                                        }
                                        i1++;
                                        j1++;

                                    }
                                    while (k1 != (str.length() - (40 + payload_size))) {
                                        if (str.charAt(limit2) == '1') {
                                            c1++;
                                            checksum += str.charAt(limit2);
                                        } else {
                                            checksum += str.charAt(limit2);
                                            c1 = 0;
                                        }
                                        if (c1 == 5) {
                                            c1 = 0;
                                            k1++;
                                            limit2++;

                                        }
                                        k1++;
                                        limit2++;
                                    }

                                    continue;

                                } else {

                                   
                                    int I = Integer.parseInt(res, 2);
                                    buffer = intToByteArray(I);
                                    fos.write(buffer);
                                    pr.println("1");
                                    pr.flush();
                                    System.out.println("Wriiten byte: "+ res );
                                    break;

                                }
                            }

                            cc += chunk_size;

                        }

                        FileReader fr = new FileReader("Copy1.txt");
                        BufferedReader bfr = new BufferedReader(fr);
                        FileWriter fw = new FileWriter("Copy.txt",true);
                        String line;

                        while ((line = bfr.readLine()) != null) {
                            line = line.trim(); // remove leading and trailing whitespace
                            line = line.replaceAll("\\W{3}(?=[a-z])|\\W{3}(?=[A-Z])|(?<=\\s)\\s+", "");
                            fw.write(line);
                            fw.write("\n");
                        }
                        fr.close();
                        fw.close();
                        fos.close();
                        File f = new File("Copy1.txt");
                        f.delete();
                        System.out.println("All frames Received");

                    } else if (str.equalsIgnoreCase("receive")) {
                        pr.println(FileName);
                        pr.flush();
                        OS = socket.getOutputStream();
                        OS.write(array, 0, FileSize);

                        OS.flush();
                        System.out.println("File sent");

                        if (IS != null) {
                            IS.close();
                        }
                        if (OS != null) {
                            OS.close();
                        }
                        if (socket != null) {
                            socket.close();
                        }

                    }
                }
            } catch (IOException ex) {
                //  System.err.println("Could not connect to the client");
            }
        }
        Server.ClientCount--;
        Server.OnlineClient[id % 120] = "";
        System.out.println(
                "Client [" + id + "] is now terminating....");
        System.out.println(
                "Number Of Connected Client :" + Server.ClientCount);
    }

    public static int hash_key(int i, int c) {
        int x;
        if (FileSize % c == 0) {
            x = FileSize / c;
        } else {
            x = (FileSize / c) + 1;
        }

        return (i % x);
    }

    public static byte[] intToByteArray(int value) {
        return new byte[]{
            (byte) (value >>> 24),
            (byte) (value >>> 16),
            (byte) (value >>> 8),
            (byte) value};
    }

}
