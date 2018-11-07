
import jdk.nashorn.internal.runtime.ECMAException;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;


public class MyClient {

    public static Socket clientSocket ;
    public static int TOTAL_ALLOWED_SIZE = 500000 ;


    MyClient(Socket clientSocket){
        this.clientSocket = clientSocket ;
    }


    public static void clear_buffer(String file_name) {

        File file = new File(file_name) ;
        if(file.delete())
        {
            System.out.println(file_name + " is deleted");
        }
    }

    public static String byte_to_binarystring( byte payload[] ){

        byte[] before_stuff = new byte[payload.length];
        String byte_bit = "";

        for (int i = 0; i < payload.length; i++) {
            before_stuff[i] = payload[i];                       //copy array
        }

        for (byte b : before_stuff) {
            String str1 = Integer.toBinaryString(b & 255 | 256).substring(1);           //byte to bit of total payload
            byte_bit += str1;
        }
        System.out.println("Actual payload: " + byte_bit);

        return byte_bit ;
    }

    public static String bit_stuffing( String byte_bit ) {

        String data = byte_bit;
        String after_stuff = "";
        int count = 0, it = 0;

        while (it != byte_bit.length()) {                       //stuffing bit by bit
            char xx = byte_bit.charAt(it);

            if (xx == '1') {
                count++;
                after_stuff += xx;
            }
            else if(xx == '0') {
                count = 0;
                after_stuff += xx;
            }
            if (count == 5) {
                after_stuff += '0';
                count = 0;
            }
            else {
                //continue;
            }
            it++;
        }

        System.out.println("Stuffed bit: " + after_stuff);
        return after_stuff;
    }

    public static String add_flag(String stuffed_bit){
        String added_flag = "01111110" + stuffed_bit + "01111110";
        return added_flag ;
    }

    public static String converting_int_to8bit( int s ){

        String binaryString = Integer.toBinaryString(s);
        String new_bin_string = "" ;

        for(int i=0 ; i<8-binaryString.length(); i++){
            new_bin_string += "0" ;
        }

        new_bin_string += binaryString ;
        return new_bin_string ;
    }


    public static String sender_check_sum( String payload ){
        int sum = 0 ;

        for(int i=0; i < payload.length() ; i++){
            char ch = payload.charAt(i) ;
            int ii ;

            if( ch == '1' ){
                ii = ch - '0';
                sum += ii ;
            }
        }

        if(sum > 127){
            if(sum % 2 == 0){
                sum = 0;
            }
            else{
                sum = 1;
            }
        }
        String c_sum = converting_int_to8bit(sum) ;
        return c_sum ;
    }

    public static void file_Send(String file_name, int filesize, int selected_chunk_size) throws IOException, TimeoutException{


            FileInputStream fileInputStream = new FileInputStream(file_name);
            BufferedInputStream bufferInputStream = new BufferedInputStream(fileInputStream);
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            OutputStream outputStream = clientSocket.getOutputStream();
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            PrintWriter printWriter = new PrintWriter(clientSocket.getOutputStream());


            int flag = 0;

            long already_read = 0;

            int sequence_no = 1;
            String seq_no;

            //int ack_no = 1;
            //String ac_no = converting_int_to8bit(ack_no);

            int kind_data = 0;
            String d_data = converting_int_to8bit(kind_data);

            int kind_ack = 1;
            String d_ack = converting_int_to8bit(kind_ack);

            String sender_frame = "";

            byte[] byte_written;
            long file_er_length = fileInputStream.available();
            int chunk_div = selected_chunk_size;
            printWriter.println(String.valueOf(file_er_length));
            printWriter.flush();

            while (already_read != file_er_length) {

                long start_time = System.currentTimeMillis();
                long end_time;
                System.out.println("shuru" + start_time);

                if (file_er_length - already_read >= chunk_div) {
                    already_read += chunk_div;
                    byte_written = new byte[chunk_div];
                    bufferInputStream.read(byte_written, 0, chunk_div);

                    String binary_string = byte_to_binarystring(byte_written);
                    //System.out.println(binary_string);

                    //String stuffed_bit = bit_stuffing(binary_string);
                    //System.out.println(stuffed_bit);

                    String with_checksum = sender_check_sum(binary_string);
                    System.out.println("checksum: " + with_checksum);

                    //with_checksum = bit_stuffing(with_checksum);
                    //System.out.println("checksum: " + with_checksum);

                    seq_no = converting_int_to8bit(sequence_no);
                    System.out.println("seq_no: " + seq_no);

                    //seq_no = bit_stuffing(seq_no);
                    //System.out.println("seq_no: " + seq_no);

                    sender_frame = seq_no + binary_string + with_checksum;
                    sender_frame = bit_stuffing(sender_frame);
                    sender_frame = d_data + sender_frame;
                    sender_frame = add_flag(sender_frame);                                 //whole frame

                    outputStream.write(byte_written);

                    outToServer.writeBytes(sender_frame + '\n');


                } else {
                    long chunk_div_long = file_er_length - already_read;
                    int chunk_div_1 = (int) chunk_div_long;
                    already_read = file_er_length;
                    byte_written = new byte[chunk_div_1];
                    bufferInputStream.read(byte_written, 0, chunk_div_1);

                    String binary_string = byte_to_binarystring(byte_written);
                    //System.out.println(binary_string);

                    //String stuffed_bit = bit_stuffing(binary_string);
                    //System.out.println(stuffed_bit);

                    String with_checksum = sender_check_sum(binary_string);
                    System.out.println("checksum: " + with_checksum);

                    //with_checksum = bit_stuffing(with_checksum);
                    //System.out.println("checksum: " + with_checksum);

                    seq_no = converting_int_to8bit(sequence_no);
                    System.out.println("seq_no: " + seq_no);

                    //seq_no = bit_stuffing(seq_no);
                    //System.out.println("seq_no: " + seq_no);

                    sender_frame = seq_no + binary_string + with_checksum;
                    sender_frame = bit_stuffing(sender_frame);
                    sender_frame = d_data + sender_frame;
                    sender_frame = add_flag(sender_frame);                                 //whole frame

                    System.out.println("sender frame: " + sender_frame);
                    outputStream.write(byte_written);

                    outToServer.writeBytes(sender_frame + '\n');

                }

                //System.out.println("hoho "+byte_written);


                clientSocket.setSoTimeout(30000);
                try {
                    String ack = inFromServer.readLine();
                    System.out.println("sent ack: " + ack);                                            //ack
                    System.out.println(inFromServer.readLine());                                //server has received

                    String ack_no = ack.substring(8,16);
                    if(ack_no.equalsIgnoreCase(seq_no)){

                    }
                    else{
                        System.out.println("ack_no != seq_no...........resending........");
                        String resend = sender_frame;
                        outToServer.writeBytes(sender_frame + '\n');
                        System.out.println("sent");

                        String ack1 = inFromServer.readLine();
                        System.out.println("sent ack: " + ack1);                                            //ack
                        System.out.println(inFromServer.readLine());
                    }


                } catch (Exception e) {
                    System.out.println("lost frame detected...........resending........");
                    String resend = sender_frame;
                    outToServer.writeBytes(sender_frame + '\n');
                    System.out.println("sent");

                    String ack1 = inFromServer.readLine();
                    System.out.println("sent ack: " + ack1);                                            //ack
                    System.out.println(inFromServer.readLine());
                }

                end_time = System.currentTimeMillis();
                System.out.println(end_time);

            /*if(end_time - start_time > 30000) {
                System.out.println("Time out for chunk");
                flag = 1 ;
                clear_buffer(file_name);
            }*/

                sequence_no += 1;
            }
            //outputStream.flush();

            if (flag == 0) {
                outToServer.writeBytes(Integer.toString(flag) + '\n');
                outToServer.writeBytes("all chunks have been sent" + '\n');
                System.out.println("file is sent to server");
            } else {
                outToServer.writeBytes(Integer.toString(flag) + '\n');
                outToServer.writeBytes("time out for the chunk" + '\n');
            }

    }

    public static void file_Receive(Socket connect_socket, String file_name) throws IOException{

        DataOutputStream outToServer = new DataOutputStream(connect_socket.getOutputStream());
        BufferedReader bufferReader = new BufferedReader(new InputStreamReader(connect_socket.getInputStream())) ;
        PrintWriter printWriter = new PrintWriter(connect_socket.getOutputStream());
        FileOutputStream fileOutputStream = new FileOutputStream(file_name);
        BufferedOutputStream bufferOutputStream = new BufferedOutputStream(fileOutputStream);
        InputStream inputStream = connect_socket.getInputStream();

        try
        {
            String receive_string = bufferReader.readLine();
            int file_size=Integer.parseInt(receive_string);
            int start_read = 0;
            int already_read=0;
            byte[] byte_written = new byte[10000];
            while(already_read != file_size) {

                start_read=inputStream.read(byte_written);
                already_read+=start_read;
                bufferOutputStream.write(byte_written, 0, start_read);

            }
            bufferOutputStream.flush();
            System.out.println("file is received from server");
        }
        catch(Exception e)
        {
            System.err.println("sorry can not transfer file.");
        }

    }

    public static void main(String argv[]) throws Exception
    {
        String file_sender;
        String file_receiver ;
        String back_to_client;
        String file_name ;
        String file_size ;
        int filesize ;
        int selected_chunk_size ;

        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        Socket clientSocket = new Socket("localhost", 6789);
        MyClient mc = new MyClient(clientSocket) ;
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        //PrintWriter pr = new PrintWriter(clientSocket.getOutputStream());

        //while(true)

        System.out.println("Enter your student id:");
        file_sender = inFromUser.readLine();
        outToServer.writeBytes(file_sender + '\n');

        back_to_client = inFromServer.readLine();

        if( back_to_client.equalsIgnoreCase("alreadylogedin") ){
            System.out.println("Already logged in from this IP address");
            return;
        }

        System.out.println("transfer(1/0)?");
        String trans = inFromUser.readLine() ;

        if(trans.equalsIgnoreCase("1")){
            try {
                System.out.println("Enter receiver id:");
                file_receiver = inFromUser.readLine();
                outToServer.writeBytes(file_receiver + '\n');

                back_to_client = inFromServer.readLine();

                if( back_to_client.equalsIgnoreCase("notlogedin") ){
                    System.out.println("Receiver is not connected to server");
                    return ;
                }
                else {

                    System.out.println("Enter file name:");
                    file_name = inFromUser.readLine();
                    outToServer.writeBytes(file_name + '\n');

                    System.out.println("Enter file size:");
                    file_size = inFromUser.readLine();
                    filesize = Integer.parseInt(file_size);
                    outToServer.writeBytes(file_size + '\n');

                    selected_chunk_size = Integer.parseInt(inFromServer.readLine());
                    String exceed_not = inFromServer.readLine();                        //file exceed
                    //outToServer.writeBytes("mishaaaaa" + '\n');
                    System.out.println(exceed_not);
                    //System.out.println("gadha returns ");
                    if(exceed_not.equalsIgnoreCase("file has exceeded limit")){

                    }
                    else{
                        file_Send(file_name, filesize, selected_chunk_size);
                    }
                }

            }

            catch (Exception exception){

            }
        }
        else{
            //outToServer.writeBytes(file_sender + '\n');
            outToServer.writeBytes("-1" + '\n');    // no receiver
            outToServer.writeBytes("dummy" + '\n');  // no file name
            outToServer.writeBytes("-1" + '\n');    // no file size

            selected_chunk_size = Integer.parseInt(inFromServer.readLine());
            //System.out.println("receiver "+inFromServer.readLine());
        }


        String receice_file_name = inFromServer.readLine();
        String want_to = inFromServer.readLine();
        System.out.println(want_to);

        String yes_no = "yes";
        if(yes_no.equalsIgnoreCase("yes")){
            file_Receive(clientSocket, receice_file_name) ;
        }
        else{
            System.out.println("ok file is not given");
        }

        return;
    }


}
