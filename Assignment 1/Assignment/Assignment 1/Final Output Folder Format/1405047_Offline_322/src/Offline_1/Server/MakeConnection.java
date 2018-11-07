package Offline_1.Server;

import java.io.IOException;
import java.net.Socket;
import java.util.Hashtable;

/**
 * Created by Shahriar Sazid on 23-Sep-17.
 */
public class MakeConnection implements Runnable {
    StreamConnection str_con;
    public Hashtable<Integer, StreamConnection> studentList;
    public MakeConnection(Socket con, Hashtable<Integer, StreamConnection> sL) throws IOException {
        studentList = sL;
        str_con = new StreamConnection(con);
    }

    @Override
    public void run() {
        try {
            String type = (String)str_con.read();
            if(type.equals("sender")){
                int id = (int)str_con.read();
                if(studentList.containsKey(Integer.valueOf(id))){
                    str_con.write("You have already logged in!");
                }else {
                    studentList.put(Integer.valueOf(id), str_con);
                    str_con.write("Login Successful");
                }
                new Thread(new ServerReader(str_con, studentList, id)).start();
            }
            else{
                int id = Integer.valueOf(type.replace("receiver", ""));
                Server.recList.put(id, str_con);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
