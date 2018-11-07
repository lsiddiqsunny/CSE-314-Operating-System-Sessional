package Offline_1.Client;

import Offline_1.Server.StreamConnection;

import java.io.IOException;
import java.util.Scanner;

/**
 * Created by Shahriar Sazid on 22-Sep-17.
 */
public class Client {
    public static StreamConnection str_con;
    public static StreamConnection rec_con;
    public static int id;
    public static int file_id = 0;
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Give your id:");
        Scanner in = new Scanner(System.in);
        id = in.nextInt();
        str_con = new StreamConnection("localhost", 5047);
        str_con.setTimer(1000);
        str_con.write("sender");
        str_con.write(id);
        try {
            String msg = (String) str_con.read();
            if (msg.equals("Login Successful")) {
                System.out.println(msg);
                new Thread(new Reader(str_con)).start();
                rec_con = new StreamConnection("localhost", 5047);
                rec_con.write("receiver"+String.valueOf(id));
                new Thread(new Writer(rec_con)).start();
            } else {
                System.out.println(msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Thread.currentThread().join(0);
    }
}
