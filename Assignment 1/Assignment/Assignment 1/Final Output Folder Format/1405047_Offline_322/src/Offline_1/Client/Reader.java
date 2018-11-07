package Offline_1.Client;

import Offline_1.Server.Server;
import Offline_1.Server.StreamConnection;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Random;
import java.util.Scanner;

/**
 * Created by Shahriar Sazid on 23-Sep-17.
 */
public class Reader implements Runnable {
    public StreamConnection str_con;

    public Reader(StreamConnection con) {
        str_con = con;
    }

    Frame frame = new Frame();

    private void display(int number) {
        int mask = 1 << 7;

        for (int i = 7; i >= 0; i--) {
            if (((1 << i) & number) != 0)
                System.out.print(1);
            else
                System.out.print(0);
        }
    }

    public void run() {
        Scanner in = new Scanner(System.in);
        while (true) {
            int turn = 0;
            Random ck_er = new Random();
            int ck_turn = ck_er.nextInt(100);
            System.out.println("Do you want to send file to someone?[y/n]");
            String rep = in.next();
            if (rep.startsWith("y") || rep.startsWith("Y")) {
                System.out.println("Enter receiver's id:");
                int rec = in.nextInt();
                try {
                    str_con.write(rec);
                    String msg = (String) str_con.read();
                    if (msg.equals("Recipient not available")) {
                        System.out.println(msg);
                    } else {
                        System.out.println("Enter file path:");
                        String path = in.nextLine();
                        path = in.nextLine();
                        path = path.replace("\\", "\\\\");
                        File f = new File(path);
                        long size = f.length();
                        if (Server.free_space - size < 0) {
                            System.out.println("Space not available\nFile not transmitted");
                        } else {
                            str_con.write(f.getName());
                            str_con.write(f.length());
                            String file_id = String.valueOf(Client.id) + "_" + String.valueOf(++Client.file_id);
                            str_con.write(file_id);
                            FileInputStream is = new FileInputStream(f);
                            Random rd = new Random();
                            int chunkSize = (int)f.length()%100; ;
                            System.out.println(chunkSize);
                            byte[] chunk = new byte[chunkSize];
                            int sqnc = 1;
                            int chunkLen;
                            chunkLen = is.read(chunk);
                            if (chunkLen == -1) {
                                str_con.write("complete");
                            } else {
                                if (chunkLen != chunkSize) {
                                    byte[] temp = chunk.clone();
                                    chunk = new byte[chunkLen];
                                    for (int i = 0; i < chunkLen; i++) {
                                        chunk[i] = temp[i];
                                    }
                                }
                                byte[] bytes = frame.make_frame(chunk, 0, 0, 1);
                                str_con.write(bytes);
                                turn++;
                                byte[] pre_bytess = null;
                                while (true) {
                                    chunkLen = is.read(chunk);
                                    if (chunkLen == -1) {
                                        msg = (String) str_con.read();
                                        if (msg.equals("next!")) {
                                            str_con.write("complete");
                                        }
                                        break;
                                    }
                                    if (chunkLen != chunkSize) {
                                        byte[] temp = chunk.clone();
                                        chunk = new byte[chunkLen];
                                        for (int i = 0; i < chunkLen; i++) {
                                            chunk[i] = temp[i];
                                        }
                                    }
                                    byte[] bytess = frame.make_frame(chunk, 0, 0, 1);
                                    if (turn == ck_turn) {
                                        if (bytess[bytess.length - 2] == 0) bytess[bytess.length - 2] = 1;
                                        else bytess[bytess.length - 2] = 0;
                                    }
                                    while(true){
                                        Object o = str_con.read();
                                        if (o instanceof String) {
                                            msg = (String)o;
                                            if (msg.equals("next!")) {
                                                str_con.write(bytess);
                                                turn++;
                                                break;
                                            }
                                        } else if(o instanceof Integer){
                                            if((int)o==-1){
                                                str_con.write(pre_bytess);
                                            }
                                        }
                                    }
                                    pre_bytess = bytess;
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}
