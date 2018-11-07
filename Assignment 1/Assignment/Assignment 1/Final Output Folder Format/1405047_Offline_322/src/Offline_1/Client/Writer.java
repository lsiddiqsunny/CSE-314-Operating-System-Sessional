package Offline_1.Client;

import Offline_1.Server.StreamConnection;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

/**
 * Created by Shahriar Sazid on 23-Sep-17.
 */
public class Writer implements Runnable {
    public StreamConnection rec_con;

    public Writer(StreamConnection con) {
        rec_con = con;
    }

    @Override
    public void run() {
        Scanner in = new Scanner(System.in);
        try {
            while (true) {
                String msg = (String) rec_con.read();
                if (msg.startsWith("Student")) {
                    System.out.println(msg);
                    String rep = in.next();
                    rec_con.write(rep);
                    String name = (String)rec_con.read();
                    File f = new File("D:\\PROGRAMMING\\My_Codes\\1405047_Offline_322\\src\\Offline_1\\Receiver\\"+name);
                    FileOutputStream fop = new FileOutputStream(f);
                    BufferedOutputStream bop = new BufferedOutputStream(fop);
                    while (true) {
                        try {
                            byte[] chunk = (byte[]) rec_con.read();
                            bop.write(chunk);
                            rec_con.write("next!");
                        } catch (ClassCastException cce) {
                            bop.flush();
                            bop.close();
                            break;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
