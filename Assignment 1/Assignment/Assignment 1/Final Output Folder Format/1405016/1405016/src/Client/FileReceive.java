package Client;

import java.io.*;
import java.net.Socket;
import Stuffing.*;

class FileReceive implements Runnable{
    Socket socket;
    BufferedReader br;
    PrintWriter pw;
    public FileReceive(Socket s,BufferedReader b,PrintWriter p){
        socket = s;
        br = b;
        pw = p;
        new Thread(this).start();
    }
    @Override
    public void run() {
        int sent = -20;
        try {
            sent = br.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (sent == 20){
            int rec;
            pw.println("1");
            pw.flush();
            try {
                System.out.println("You have a file to receive");
                int filesize = Integer.parseInt(br.readLine());
                String filename = br.readLine();
                String sender = br.readLine();
                System.out.println("File name : "+filename+"\nFile size(bytes) : "+filesize+"\nSender : "+sender);
                System.out.println("Press R to receive");
                //int chunk = socket.getInputStream().read();
                int chunk = 150;
                rec = socket.getInputStream().read();
                if (rec == 5){
                    pw.println("1");
                    pw.flush();

                    File file = new File("."+"/Narcos/"+filename);
                    DataInputStream dis = new DataInputStream(socket.getInputStream());
                    FileOutputStream fos = new FileOutputStream(file);
                    byte[] mybyte = new byte[chunk];
                    while (filesize > chunk){
                        dis.read(mybyte);
                        filesize -= chunk;
                        fos.write(mybyte);
                    }
                    dis.read(mybyte,0,filesize);
                    fos.write(mybyte,0,filesize);
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }
}