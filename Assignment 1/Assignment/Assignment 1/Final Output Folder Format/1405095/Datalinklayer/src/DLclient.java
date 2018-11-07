import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

/**
 * Created by Dell on 06-Nov-17.
 */
public class DLclient {
    public static void main(String argv[]) throws Exception {

        String sentence;
        String modifiedSentence;
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        try {

            Socket clientSocket = new Socket("localhost", 6789);
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            DataInputStream input = new DataInputStream((clientSocket.getInputStream()));

            System.out.println("Enter Filename : ");
            String filename = inFromUser.readLine();
            System.out.println(filename);
            outToServer.writeBytes(filename+"\n");
            outToServer.flush();


            File gradeList = new File(filename);
            if (!gradeList.exists()) {
                throw new FileNotFoundException("Failed to find file: " +
                        gradeList.getAbsolutePath());
            }

            Path path = Paths.get(filename);
            byte[] data = Files.readAllBytes(path);
            int sze = data.length;

            System.out.println("length "+sze);
            outToServer.writeInt(sze);


            int chunk = 32;
            int total_chunk;

            if (sze % chunk == 0) total_chunk = sze / chunk;
            else {
                total_chunk = sze / chunk + 1;
            }
            System.out.println("Total chunk : " + total_chunk);

            outToServer.writeInt(total_chunk);
            outToServer.flush();

            byte[] each_chunk = new byte[chunk];
            int file_index = 0;
            int temp_size = 0;


            while (temp_size != total_chunk)  //sending chunks
            {

                int temp_ind=file_index;
                if (((data.length - 1) - file_index) < chunk) {
                    each_chunk = new byte[data.length - file_index];
                    System.arraycopy(data, file_index, each_chunk, 0, data.length - file_index);
                    file_index += data.length - file_index;
                } else {
                    System.arraycopy(data, file_index, each_chunk, 0, chunk);
                    file_index += chunk;
                }
                System.out.println("File index : " + file_index);

                ABC temp = new ABC();
                int op = temp.get_sum(each_chunk, each_chunk.length);
                System.out.println("Input: ");
                temp.print(each_chunk);
                for (int j = 0; j < each_chunk.length; j++) {
                    temp.get_bit(each_chunk[j]);
                }
                System.out.println();

                System.out.println("Press 'Y' to introduce error and 'N' to send error-free frame to the server");
                String error=inFromUser.readLine();
                if(error.charAt(0)=='Y'){
                    System.out.println("Select position :");
                    int error_pos=Integer.parseInt(inFromUser.readLine());
                    System.out.println("error_pos "+error_pos);
                    int p=temp.get(each_chunk,error_pos);
                    if(p==0) {
                        System.out.println("Cuurent value at position " + error_pos + " is : 0");
                    }else System.out.println("Cuurent value at position " + error_pos + " is : 1");
                    System.out.println("Select new bit :");
                    int error_val=Integer.parseInt(inFromUser.readLine());
                    temp.set(each_chunk,error_pos,error_val);

                }

                byte[] send;
                send = temp.stuff_bits(each_chunk);
                System.out.println("Stuffed array ");

                temp.print(send);
                for (int j = 0; j < send.length; j++) {
                    temp.get_bit(send[j]);
                }

                clientSocket.setSoTimeout(10000);

                try {

                    outToServer.writeInt(send.length);
                    outToServer.flush();

                    outToServer.write(send,0,send.length);
                    outToServer.flush();

                    int size = input.readInt();
                    System.out.println("ack size " + size);

                    byte[] acknowledge = new byte[size];
                    int val = input.read(acknowledge);

                    System.out.println("Frame "+temp_size+" tranmitted");
                    temp.seq=0;

                    temp_size++;

                    System.out.println("tranmit Successful");

                }catch (Exception e){

                    System.out.println("Error timeout");
                    temp.seq=1;
                    file_index=temp_ind;

                }


            }
            clientSocket.setSoTimeout(0);
            System.out.println("All Frames tranmitted");

            Thread.sleep(40000);

        } catch (Exception e) {

            System.exit(0);
        }
    }
}
