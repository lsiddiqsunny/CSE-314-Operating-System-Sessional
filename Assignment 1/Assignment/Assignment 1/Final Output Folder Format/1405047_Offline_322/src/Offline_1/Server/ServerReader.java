package Offline_1.Server;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;

/**
 * Created by Shahriar Sazid on 25-Sep-17.
 */
public class ServerReader implements Runnable {
    public int id;
    public StreamConnection str_con;
    public Hashtable<Integer, StreamConnection> studentList;
    public Hashtable<Integer, StreamConnection> recList;
    public Destuffing d = new Destuffing();

    ServerReader(StreamConnection con, Hashtable<Integer, StreamConnection> sL, int i) {
        str_con = con;
        studentList = sL;
        id = i;
    }

    public byte calculate_checksum(byte[] bytes) {
        int cnt = 0;
        for (int i = 0; i < bytes.length; i++) {
            int m = 0b10000000;
            int int_byte = bytes[i];
            for (int j = 0; j < 8; j++) {
                if ((int_byte & m) != 0) {
                    cnt++;
                }
                m >>= 1;
            }
        }
        if (cnt % 2 == 0) return 0;
        else return 1;
    }

    @Override
    public void run() {
        int turn = 0;
        try {
            while (true) {
                int rec = (int) str_con.read();
                File f;
                long size;
                String name;
                String file_id;
                long check = 0;
                System.out.println(rec);
                if (!studentList.containsKey(Integer.valueOf(rec))) {
                    str_con.write("Recipient not available");
                    continue;
                } else {
                    str_con.write("ok");
                    name = (String) str_con.read();
                    size = (long) str_con.read();
                    file_id = (String) str_con.read();
                    f = new File(file_id + "_" + name);
                    FileOutputStream fop = new FileOutputStream(f);
                    BufferedOutputStream bop = new BufferedOutputStream(fop);
                    while (true) {
                        try {
                            turn++;
                            byte[] chunk = (byte[]) str_con.read();
                            byte bytes[] = new byte[chunk.length - 3];
                            for (int i = 0; i < bytes.length; i++) {
                                bytes[i] = chunk[i + 1];
                            }
                            byte[] pre_payload = d.destuuff_payload(bytes);
                            byte[] payload = new byte[pre_payload.length - 3];
                            for (int i = 0; i < payload.length; i++) {
                                payload[i] = pre_payload[3 + i];
                            }
                            if (chunk[chunk.length - 2] == calculate_checksum(bytes)) {
                                Server.free_space -= chunk.length;
                                check += payload.length;
                                bop.write(payload);
                                str_con.write("next!");
                            } else System.out.println("Checksum vul.....");
                        } catch (ClassCastException cce) {
                            bop.flush();
                            bop.close();
                            break;
                        }
                    }
                }
                if (false) {
                    f.delete();
                } else {
                    StreamConnection rec_con = Server.recList.get(rec);
                    rec_con.write("Student id " + id + " wants to send you a file\nFile Name: " + name
                            + " File Size: " + size + "\nDo you want to receive?[y/n]");
                    new Thread(new ServerWriter(rec_con, f, name)).start();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
