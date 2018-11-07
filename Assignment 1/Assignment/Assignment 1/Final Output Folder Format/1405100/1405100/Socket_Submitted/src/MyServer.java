
import javax.xml.crypto.Data;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

class MyServer {

    public static Send_receive sr_array[] = new Send_receive[123] ;
    public static int x =0 ;
    public static int max_buffer_size = 25000000 ;
    public static int total_chunk_size = 0 ;
    public static int file_id = 1;

    public static void clear_buffer(String file_name) {

        File file = new File(file_name) ;
        if(file.delete())
        {
            System.out.println(file_name + " is deleted");
        }
    }

    public static void main(String argv[]) throws Exception
    {
        int Thread_RunCount = 0;
        int id = 1;
        int sender_roll_array[] = new int[123] ;

        for(int i=0; i<=122; i++){
            sender_roll_array[i] = 0;
        }

        ServerSocket welcomeSocket = new ServerSocket(6789);

        int chunk_size ;
        Random rand = new Random();
        chunk_size = rand.nextInt(1000) + 1;


        while(true)
        {
            Socket connectionSocket = welcomeSocket.accept();
            Thread_Run wt = new Thread_Run(connectionSocket,id,sender_roll_array,chunk_size);
            Thread t = new Thread(wt);
            t.start();
            Thread_RunCount++;
            System.out.println("Client [" + id + "] is now connected. No. of worker threads = " + Thread_RunCount);
            id++;
        }

    }

}

class Send_receive {
    String sender;
    String receiver;
    String file_name;
    String file_size;
    int file_id;
    Socket connect_socket ;

    Send_receive(String sender, String receiver, String file_name, String file_size, int file_id, Socket connect_socket) {
        this.sender = sender ;
        this.receiver = receiver ;
        this.file_name = file_name ;
        this.file_size = file_size ;
        this.file_id = file_id ;
        this.connect_socket =  connect_socket ;
    }
}

class Thread_Run implements Runnable
{
    private Socket connectionSocket;
    private int id;
    int sender_roll ;
    int receiver_roll ;
    int selected_chunk_size ;
    int sender_roll_array[] = new int[123] ;
    public static String sending_file_name ;
    int track ;

    public Thread_Run(Socket ConnectionSocket, int id, int sender_roll_array[], int chunk_size)
    {
        this.connectionSocket=ConnectionSocket;
        this.id=id;
        this.sender_roll_array = sender_roll_array ;
        this.selected_chunk_size = chunk_size ;
    }

    public static String string_split(String file_name, int file_id) {
        String str = file_name;
        String find_dot = ".";
        String before_dot = str.substring(0, str.indexOf(find_dot));
        String after_dot = str.substring(str.indexOf(find_dot) + find_dot.length());
        before_dot = before_dot + "_"+ Integer.toString(file_id) + "." ;
        str = before_dot + after_dot;
        return str;
    }

    public static String String_split(String file_name) {
        String str = file_name;
        String find_dot = ".";
        int num;
        String before_dot = str.substring(0, str.indexOf(find_dot));
        String after_dot = str.substring(str.indexOf(find_dot) + find_dot.length());

        String find_us = "_";

        String before_us = before_dot.substring(0, before_dot.indexOf(find_us));
        String after_us = before_dot.substring(before_dot.indexOf(find_us) + find_us.length());
        str = before_dot + "_" + after_us + "." + after_dot;
        return str;
    }

    public static String bit_destuffing(String stuffed_bit){

        int count = 0, it = 0, slength = stuffed_bit.length() ;
        String destuffed = "" ;

        while (it != slength) {
            char xx = stuffed_bit.charAt(it) ;

            if(xx == '1') {
                count++;
                destuffed += xx;
            }
            else if(xx == '0') {
                count = 0;
                destuffed += xx;
            }
            if(count == 5) {
                int now = it+2;
                if( now != slength ) {
                    destuffed += stuffed_bit.charAt(now);
                }
                else {
                    destuffed += '1';
                }

                count = 1;
                it += 2;
            }
            else{
                //continue;
            }
            it++;
        }

        //System.out.println("Destuffed bit: " + destuffed );
        return destuffed ;
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

    public static void file_Send(String file_name, int file_size, int selected_chunk_size, Socket clientSocket) throws IOException
    {

        FileInputStream fileInputStream = new FileInputStream(file_name);
        BufferedInputStream bufferInputStream = new BufferedInputStream(fileInputStream);
        OutputStream outputStream = clientSocket.getOutputStream();
        PrintWriter printWriter = new PrintWriter(clientSocket.getOutputStream());

        Thread_Run.sending_file_name = file_name ;

        long already_read = 0;
        byte[] byte_written;
        long fileLength = fileInputStream.available();
        int chunk_div = selected_chunk_size ;
        printWriter.println(String.valueOf(fileLength));
        printWriter.flush();

        while(already_read != fileLength){

            if(fileLength - already_read >= chunk_div) {
                already_read += chunk_div;
                byte_written = new byte[chunk_div];
                bufferInputStream.read(byte_written, 0, chunk_div);
                outputStream.write(byte_written);
            }
            else{
                int chunk_div_1 = (int)(fileLength - already_read);
                already_read = fileLength;
                byte_written = new byte[chunk_div_1];
                bufferInputStream.read(byte_written, 0, chunk_div_1);
                outputStream.write(byte_written);
            }
            //System.out.println(byte_written);
        }
        outputStream.flush();
        System.out.println("file is sent to particular client");
        MyServer.total_chunk_size -= file_size ;

    }

    public String file_Receive(Socket connect_socket, String file_name, int file_size, int selected_chunk_size) throws IOException{

        DataOutputStream outToServer = new DataOutputStream(connect_socket.getOutputStream());
        BufferedReader br = new BufferedReader(new InputStreamReader(connect_socket.getInputStream())) ;
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(connect_socket.getInputStream()));
        PrintWriter printWriter = new PrintWriter(connect_socket.getOutputStream());
        String new_name = null;

        try
        {
            //if( MyServer.total_chunk_size + file_size <= MyServer.max_buffer_size ) {

            MyServer.total_chunk_size += file_size ;
            String receive_string = br.readLine();
            int file_er_size = Integer.parseInt(receive_string);
            byte[] byte_written = new byte[selected_chunk_size];

            new_name = string_split(file_name, MyServer.file_id);

            FileOutputStream fileOutputStream = new FileOutputStream(new_name);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
            InputStream inputStream = connect_socket.getInputStream();
            System.out.println("available " + inputStream.available() + "file size" + file_er_size);

            System.out.println("xx");
            int bytesRead = 0;
            int already_read = 0;

            while (already_read != file_er_size)
            {

                bytesRead = inputStream.read(byte_written);
                already_read += bytesRead;

                //System.out.println("hehe" + byte_written);
                //long end_time = System.nanoTime();
                //System.out.println(end_time);

                String sender_frame = inFromServer.readLine();
                System.out.println("sender frame: " + sender_frame) ;                           //read full frame

                String des = sender_frame.substring( 16, sender_frame.length()-8 );
                System.out.println("des: " + des) ;

                String destuffed = bit_destuffing(des);                                // destuffed frame without flag
                System.out.println("destuf: " + destuffed) ;
                //String flag = destuffed.substring(0,8);

                String kind = sender_frame.substring(8,16);
                System.out.println("kind: " + kind) ;

                String seq_no = destuffed.substring(0,8);
                System.out.println("seq no: " + seq_no) ;


                String payload = destuffed.substring(8, destuffed.length()-8);
                System.out.println("payload: " + payload) ;

                String checksum = destuffed.substring(destuffed.length()-8, destuffed.length());
                System.out.println("checksum: " + checksum);

                String check_sum = sender_check_sum( payload );

                byte[] array = new byte[payload.length()/8];

                if( check_sum.equalsIgnoreCase(checksum) ){

                    String b = payload;

                    for(int i=0, j=0; i<b.length(); i+=8, j++) {
                        int in_bit = Integer.parseInt(b.substring(i,i+8), 2);
                        array[j] = (byte)in_bit;
                    }

                    String ack_no = seq_no;
                    String ack = "01111110" + ack_no + "01111110";
                    outToServer.writeBytes(ack + '\n');
                    bufferedOutputStream.write(array, 0, array.length);             // file e content likhe
                    outToServer.writeBytes("Server has received the chunk...." + '\n');

                }



  /*              System.out.println();

                System.out.println("bw");

                for(int i =0 ; i< byte_written.length; i++){
                    System.out.print(byte_written[i]);
                }

                System.out.println();

                System.out.println("arr");

                for(int i =0 ; i< array.length; i++){
                    System.out.print(array[i]);
                }

*/
                //outToServer.writeBytes("ack" + '\n');



                //outToServer.writeBytes("Server has received the chunk...." + '\n');
            }
            bufferedOutputStream.flush();

            String timeout = inFromServer.readLine();                 //time out or not

            if(timeout.equalsIgnoreCase("0")){ //ok
                System.out.println(inFromServer.readLine());
            }
            else{                                                       //timeout
                System.out.println(inFromServer.readLine());
                MyServer.clear_buffer(file_name);
            }

            //all chunks sent

            if(already_read == file_er_size){
                System.out.println("file received successfully from sen client");
            }
            else{
                MyServer.total_chunk_size -= file_size ;
            }
            MyServer.file_id++;


        }
        catch(Exception e)
        {
            System.err.println("sorry can not transfer file.");
        }

        return new_name ;

    }

    public void run()
    {
        String file_sender;
        String file_receiver = "0";
        String f_size ;
        String f_name ;
        String capitalizedSentence;

        try {
            while (true) {

                try {

                    BufferedReader inFromServer = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                    DataOutputStream outToServer;

                    file_sender = inFromServer.readLine(); //roll

                    sender_roll = Integer.parseInt(file_sender);

                    if (sender_roll_array[sender_roll] == 0) {
                        sender_roll_array[sender_roll] = 1;
                        outToServer = new DataOutputStream(connectionSocket.getOutputStream());
                        outToServer.writeBytes("noproblem" + '\n');
                    } else {
                        System.out.println("Already logged in");
                        outToServer = new DataOutputStream(connectionSocket.getOutputStream());
                        outToServer.writeBytes("alreadylogedin" + '\n');
                    }

                    file_receiver = inFromServer.readLine(); //roll
                    receiver_roll = Integer.parseInt(file_receiver);

                    if (receiver_roll == -1) {
                        //kisuna
                    } else if (sender_roll_array[receiver_roll] == 0) {
                        System.out.println("Receiver is not connected to server");
                        outToServer = new DataOutputStream(connectionSocket.getOutputStream());
                        outToServer.writeBytes("notlogedin" + '\n');
                    } else {
                        outToServer = new DataOutputStream(connectionSocket.getOutputStream());
                        outToServer.writeBytes("logedin" + '\n');
                    }
                    f_name = inFromServer.readLine();
                    f_size = inFromServer.readLine();


                    MyServer.sr_array[MyServer.x] = new Send_receive(file_sender, file_receiver, f_name, f_size, MyServer.file_id, connectionSocket);


                    track = MyServer.x;


                    outToServer = new DataOutputStream(connectionSocket.getOutputStream());

                    String xx = Integer.toString(selected_chunk_size);
                    outToServer.writeBytes(xx + '\n');
                    System.out.println("max chunk size : " + selected_chunk_size);


                    if (sender_roll_array[receiver_roll] == 1) {
                        //file transfer

                        for (int i = 0; i < MyServer.sr_array.length; i++) {


                            if (file_receiver.equalsIgnoreCase(MyServer.sr_array[i].sender)) {

                                //capitalizedSentence = file_sender.toUpperCase();
                                //outToServer = new DataOutputStream(MyServer.sr_array[i].connect_socket.getOutputStream());
                                //outToServer.writeBytes("The client can start sending the file now" + '\n');

                                //outToServer.writeBytes("oh yeeeeeeeeeeeeeeeeeeeeeeeee "+capitalizedSentence + '\n');
                                //System.out.println(inFromServer.readLine()) ;
                                System.out.println(" 1 " + connectionSocket + " 2 " + MyServer.sr_array[i].connect_socket);

                                if (MyServer.total_chunk_size + Integer.parseInt(f_size) <= MyServer.max_buffer_size) {

                                    outToServer = new DataOutputStream(connectionSocket.getOutputStream());
                                    outToServer.writeBytes("You can start sending the file now......" + '\n');

                                    String file_received_name = file_Receive(connectionSocket, f_name, Integer.parseInt(f_size), selected_chunk_size);

                                    String file_send_name = String_split(file_received_name);

                                    outToServer = new DataOutputStream(MyServer.sr_array[i].connect_socket.getOutputStream());
                                    //System.out.println(file_send_name);
                                    outToServer.writeBytes(file_send_name + '\n');
                                    outToServer.writeBytes("do you want to receive file from " + file_sender + " file name " + f_name + " file size " + f_size + "?" + '\n');
                                    file_Send(file_received_name, Integer.parseInt(f_size), selected_chunk_size, MyServer.sr_array[i].connect_socket);

                                } else {

                                    System.out.println(f_name + " has exceeded maximum buffer size of server");
                                    outToServer = new DataOutputStream(connectionSocket.getOutputStream());
                                    outToServer.writeBytes("file has exceeded limit" + '\n');

                                    outToServer = new DataOutputStream(MyServer.sr_array[i].connect_socket.getOutputStream());
                                    //System.out.println(file_send_name);
                                    outToServer.writeBytes("null" + '\n');
                                    outToServer.writeBytes("null" + '\n');
                                }

                            }
                        }


                        //capitalizedSentence = file_sender.toUpperCase();
                        //outToServer.writeBytes(capitalizedSentence + '\n');
                    }
                /*else {
                    System.out.println("Not connected to server");

                    outToServer = new DataOutputStream(connectionSocket.getOutputStream());
                    outToServer.writeBytes( "notlogedin" + '\n');
                }*/

                    //MyServer.x++ ;

                } catch (Exception e) {
                    //System.out.println("going offline.....");
                    //break;
                }
                //System.out.println(MyServer.x);
                MyServer.x++;
            }
        }
        catch (Exception e){
            System.out.println("going offline.....");
        }
    }
}


