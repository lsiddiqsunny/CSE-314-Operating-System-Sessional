package Offline_1.Server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by Shahriar Sazid on 01-Oct-17.
 */
public class ServerWriter implements Runnable {
    public StreamConnection rec_con;
    public File f;
    public String name;

    public ServerWriter(StreamConnection con, File f, String nm) {
        rec_con = con;
        this.f = f;
        name = nm;
    }

    @Override
    public void run() {
        while (true) {
            try {
                String msg = (String) rec_con.read();
                if (msg.startsWith("N") || msg.startsWith("n")) {
                    rec_con.write("File not transmitted");
                    continue;
                } else {
                    rec_con.write(name);
                    FileInputStream fis = new FileInputStream(f);
                    byte[] chunk = new byte[1024];
                    int chunkLen;
                    chunkLen = fis.read(chunk);
                    if (chunkLen == -1) {
                        rec_con.write("complete");
                    } else {
                        if (chunkLen != 1024) {
                            byte[] temp = chunk.clone();
                            chunk = new byte[chunkLen];
                            for (int i = 0; i < chunkLen; i++) {
                                chunk[i] = temp[i];
                            }
                        }
                        rec_con.write(chunk);
                        while (true) {
                            chunkLen = fis.read(chunk);
                            if (chunkLen == -1) {
                                msg = (String) rec_con.read();
                                if (msg.equals("next!")) {
                                    rec_con.write("complete");
                                }
                                break;
                            }
                            if (chunkLen != 1024) {
                                byte[] temp = chunk.clone();
                                chunk = new byte[chunkLen];
                                for (int i = 0; i < chunkLen; i++) {
                                    chunk[i] = temp[i];
                                }
                            }
                            msg = (String) rec_con.read();
                            if (msg.equals("next!")) {
                                rec_con.write(chunk);
                            }
                        }
                        fis.close();
                        f.delete();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
