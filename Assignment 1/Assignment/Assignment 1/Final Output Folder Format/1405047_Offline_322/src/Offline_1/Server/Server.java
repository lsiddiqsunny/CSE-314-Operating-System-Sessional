package Offline_1.Server;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;

/**
 * Created by Shahriar Sazid on 22-Sep-17.
 */
public class Server {
    public static Hashtable<Integer, StreamConnection> studentList = new Hashtable<>();
    public static Hashtable<Integer, StreamConnection> recList = new Hashtable<>();
    public static long max_size = 104857600;
    public static long free_space = 104857600;
    public static StreamConnection str_con;
        public static void main(String[] args) throws IOException, ClassNotFoundException {
            ServerSocket ss = new ServerSocket(5047);
            while(true) {
                Socket sc = ss.accept();
                System.out.println("AlhamduLILLah! Connected");
                new Thread(new MakeConnection(sc, studentList)).start();
            }
        }
    }
