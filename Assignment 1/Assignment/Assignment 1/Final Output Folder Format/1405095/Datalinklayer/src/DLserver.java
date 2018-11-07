import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Created by Dell on 06-Nov-17.
 */
public class DLserver {
    public static void main(String argv[]) throws Exception
    {
        int workerThreadCount = 0;
        int id = 1;


        ServerSocket welcomeSocket = new ServerSocket(6789);
        while(true)
        {
            Socket connectionSocket = welcomeSocket.accept();
            ABC x=new ABC();
            WorkerThread wt = new WorkerThread(connectionSocket,x);
            Thread t = new Thread(wt);
            t.start();
            workerThreadCount++;
            id++;
        }

    }
}


class WorkerThread implements Runnable {
    private Socket connectionSocket;
    private int id;
    ABC notun;


    public WorkerThread(Socket ConnectionSocket,ABC u) {
        this.connectionSocket = ConnectionSocket;
        this.notun=u;


    }

  /*  public void check(String s, int x) {
        if (s.substring(0, 2) == "Log") map.delete(x);
    }*/

    public void run() {
        String clientSentence;


        while (true) {
            try {
                DataOutputStream outToServer = new DataOutputStream(connectionSocket.getOutputStream());
                InputStreamReader x=new InputStreamReader(connectionSocket.getInputStream());
                BufferedReader inFromServer = new BufferedReader(x);
                DataInputStream input=new DataInputStream(connectionSocket.getInputStream());

//                byte[] fname=new byte[20];
                // System.out.println(new String (fname));
                String filename=inFromServer.readLine();
                System.out.println(filename);


                int f_len=input.readInt();
                System.out.println("size : "+f_len);


                int total_chunk=input.readInt();
                System.out.println("total_chunk "+total_chunk);

                int temp_size = 0;

                Path newPath = Paths.get("E:\\"+filename);
                byte[] Contents = {};
                Files.write(newPath, Contents, StandardOpenOption.CREATE);

                int file_index=0;
                while (temp_size != total_chunk){

                    int doneread=0;
                    int frame_len=input.readInt();
                    System.out.println("received frame length : "+frame_len);

                    // if(temp_size==total_chunk-1) frame_len=f_len-file_index;
                    //else frame_len=32;
                    byte[] each_frame = new byte[frame_len];

                    while (doneread!=frame_len) {

                        //System.out.println("first line");
                        byte[] temp_frame = new byte[frame_len-doneread];
                        int value = input.read(temp_frame,0,frame_len-doneread);
                        //System.out.println("bytes read : " + value);
                        System.arraycopy(temp_frame,0,each_frame,doneread,value);
                        doneread+=value;
                        //System.out.println("file read : " + new String(each_frame) );
                        //System.out.println("doneread : " + doneread);
                        Thread.sleep(70);

                        //System.out.println("last line");

                    }

                    /*notun.print(each_frame);
                    for(int j=0;j<each_frame.length;j++) {
                        notun.get_bit(each_frame[j]);
                    }*/

                    byte [] received=notun.destuff_bits(each_frame);
                    notun.ack_data=1;

                    if(notun.mode==true){
                        System.out.println("");
                        notun.mode=false;
                      /*  notun.ack=(byte) 0;
                        byte[] acknowledge={0};
                        byte[] error=notun.stuff_bits(acknowledge);

                        outToServer.writeInt(error.length);
                        outToServer.flush();

                        outToServer.write(error,0,error.length);
                        outToServer.flush();*/
                    }
                    else {
                        notun.print(received);
                        for (int j = 0; j < received.length; j++) {
                            notun.get_bit(received[j]);
                        }
                        file_index += frame_len;
                        System.out.println("Frame " + temp_size + " transmitted.");
                        Files.write(newPath, received, StandardOpenOption.APPEND);

                        notun.ack=1;
                        byte[] acknowledge={0};
                        byte[] success=notun.stuff_bits(acknowledge);

                        System.out.println("Success");

                        notun.print(success);
                        for (int j = 0; j < success.length; j++) {
                            notun.get_bit(success[j]);
                        }

                        outToServer.writeInt(success.length);
                        outToServer.flush();

                        outToServer.write(success,0,success.length);
                        outToServer.flush();
                        temp_size++;

                    }



                }

                System.out.println("Chunks transmitted.");


            } catch (Exception e) {
                try {
                    connectionSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }
}
