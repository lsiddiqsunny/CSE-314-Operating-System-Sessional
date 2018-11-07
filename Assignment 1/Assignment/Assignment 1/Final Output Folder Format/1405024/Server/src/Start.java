/**
 * Created by Ashiqur Rahman on 9/19/2017.
 */

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Collections;
import java.util.*;
public class Start {
    public static Map<String, Datastreams> studentList;
    public static int SIZE=500000;
    public static int FILECOUNT=0;
    public static String tempdir="temp\\";
    public static synchronized boolean check(int size )
    {
        if(SIZE< size) return false;
        SIZE-=size;
        return true;
    }

    public static synchronized void inc(int size )
    {
        SIZE+=size;
    }
    public static synchronized String getfile( )
    {
        return String.valueOf(FILECOUNT++);
    }

    ServerSocket socket;

    public Start() {
        try {
            socket=new ServerSocket(9000);
            new Acceptor(socket);
            studentList = Collections.synchronizedMap(new HashMap<String,Datastreams>());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Start start=new Start();
        System.out.println(start.socket.getInetAddress());
        new Checker();



    }

}
