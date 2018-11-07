import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.sql.BatchUpdateException;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Created by maksudul_mimon on 9/25/2017.
 */
public class client {

    public static String byteToString(byte [] chunk_array, int seq, int ack){
        int seq_number = seq;///////////////////////////////need change ////////////////////////
        int ack_number = ack;///////////////////////////////need change ////////////////////////
        String s = "01111110";
        //String s = "";
        s += String.format("%8s", Integer.toBinaryString(((int) 'D') & 0xFF)).replace(' ', '0');
        s += String.format("%8s", Integer.toBinaryString(seq_number & 0xFF)).replace(' ', '0');
        s += String.format("%8s", Integer.toBinaryString(ack_number & 0xFF)).replace(' ', '0');
        int checksum=0;
        int chunk_array_size = chunk_array.length;
        for (int i = 0; i < chunk_array_size; i++) {
            s += String.format("%8s", Integer.toBinaryString(chunk_array[i] & 0xFF)).replace(' ', '0');
            checksum+=chunk_array[i];
        }
        checksum = checksum%(120);

        System.out.println("checksum: "+checksum);
        s += String.format("%8s", Integer.toBinaryString(checksum & 0xFF)).replace(' ', '0');
        s += "01111110";
        return s;

    }

    public  static int get_acknowledgement(String s){
        int ack=0;
        ack = Integer.parseInt(s.substring(2*8,3*8),2);
        return ack;
    }

    public static String bitStuff(String s, int chunk_array_size){

        char[] frame_char_array = {};
        //System.out.println("inside"+chunk_array_size);
        for (int i = 0 ; i <= (chunk_array_size+4)*8-5; i++) {
            if(i>=7 && s.substring(i,i+5).equals("11111")){
                //System.out.println("found!");
                s+='0';
                int len = s.length();
                frame_char_array = s.toCharArray();

                for(int j=len-1;j>=i+6;j--){
                    frame_char_array[j]=frame_char_array[j-1];
                }
                frame_char_array[i+5]='0';
                s = String.valueOf(frame_char_array);
            }

        }
        return s;
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Socket cli_socket = null;
        ObjectInputStream inFromServer = null;
        ObjectOutputStream outToServer = null;
        String receiver_state ="";
        String file_to_send_name ="";
        File file_to_send = null;
        int file_to_send_length=0;
        int std_id = 0, receiver_id = 0;
        byte [] cli_buffer = null;
        FileInputStream fileInputStream;
        BufferedInputStream bufferedInputStream;


        try {
            cli_socket = new Socket("127.0.0.1", 12345);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            inFromServer = new ObjectInputStream(cli_socket.getInputStream());
            outToServer = new ObjectOutputStream(cli_socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        //cli_socket.setSoTimeout(30000);


        System.out.println("please enter your id!");

        Scanner sc = new Scanner(System.in);
        std_id = sc.nextInt();
        String permission ="";

        try {
            outToServer.writeObject(std_id);
            permission = (String) inFromServer.readObject();
            while (permission.equals("not permitted!")){
                System.out.println("try again!");
                std_id = sc.nextInt();
                outToServer.writeObject(std_id);
                permission = (String) inFromServer.readObject();
            }

            System.out.println("please enter the receiver id!");
            receiver_id = sc.nextInt();
            receiver_state ="";

            outToServer.writeObject(receiver_id);

            try {
                receiver_state = (String) inFromServer.readObject();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            //e.printStackTrace();
            System.out.println("u r logged out! try again!");
            return;
        }

        System.out.println(receiver_state);
        if(receiver_state.equals("receiver is online!")){
            System.out.println("which file is to send?");
            file_to_send_name =  sc.next();
            file_to_send = new File("C:\\Users\\maksudul_mimon\\Documents\\"+file_to_send_name);
            file_to_send_length = (int) file_to_send.length();
            try {
                outToServer.writeObject(file_to_send_name);
                outToServer.writeObject(file_to_send_length);
            } catch (IOException e) {
               // e.printStackTrace();
                System.out.println("u r logged out! try again!");
                return;

            }
            String send ="";
            try {
                send = (String) inFromServer.readObject();
            } catch (IOException e) {
               // e.printStackTrace();
                System.out.println("u r logged out! try again!");
                return;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            if(send.equals("send")) {

                int chunk_size = 0;
                int last_chunkSize = 0;
                int current_position = 0;
                cli_buffer = new byte[file_to_send_length];
                chunk_size = (int) inFromServer.readObject();
                String acknowledgement = "";
                int surity = 1;

                fileInputStream = new FileInputStream(file_to_send);
                bufferedInputStream = new BufferedInputStream(fileInputStream);

                bufferedInputStream.read(cli_buffer, 0, file_to_send_length);
                //-----------------------------------------------------------------------------------------------------------------------------------
                System.out.println(chunk_size);

                int seq = 1, ack = 0, totalNumberOfFrame = 0, rest = 0;
                totalNumberOfFrame = file_to_send_length / chunk_size;
                if (file_to_send_length % chunk_size != 0) {
                    rest = 1;
                }


                while (seq + 3 <= totalNumberOfFrame + 1) {
                    for (int inc = 0; inc < 3; inc++) {
                        System.out.println("entering!");
                        System.out.println("");
                        System.out.println("sending seq no: "+seq);
                        //------------------------------------------------------------framing---------------------------------------------------------------
                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>creating chunk from cli_buffer<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

                        byte[] chunk_array = null;


                        chunk_array = Arrays.copyOfRange(cli_buffer, (seq - 1) * 45, seq * 45);//should be changed!
                        int chunk_array_size = 0;

                        chunk_array_size = chunk_array.length;

                        for (int i = 0; i < chunk_array_size; i++) {
                            System.out.print(chunk_array[i] + " ");
                        }
                        System.out.println(" ");


                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> converting to binary <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                        String s;
                        s = byteToString(chunk_array, seq, ack);

                        System.out.println("binary representation:");
                        System.out.println(s);
                        System.out.println("");

                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> bit stuffing <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                        s = bitStuff(s, chunk_array_size);
                        System.out.println("after stuffing bits:");
                        System.out.println(s);
                        System.out.println("");


                        //--------------------------------------------------------------------------------------------------------------------
                        current_position += chunk_size;
                        outToServer.writeObject(s);
                        System.out.println("-----------------------------------------------------------------------------------------------");

                        seq += 1;
                    }

                    seq = ack+1;
                    int m;
                    int ack_from_server;
                    for(int inc = 0;;inc++) {
                            cli_socket.setSoTimeout(10000);
                            try {
                                acknowledgement = (String) inFromServer.readObject();
                            }catch (SocketTimeoutException e){
                                System.out.println("i am here!");
                                break;
                            }
                            ack_from_server = get_acknowledgement(acknowledgement);
                        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~"+ack_from_server);
                            if (ack_from_server == ack + 1) {
                                ack += 1;
                                seq = ack + 1;

                            }
                            //System.out.println("acknowledgegement from server: " + ack_from_server + " " + ack);


                        }

                    System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"+ack+" "+seq);
                }
                //System.out.println("seq: "+seq);

                //------------------------------------------------- for extra bytes-------------------------------------------------

                while (rest != 0 && seq <= totalNumberOfFrame+1) {

                    int ac_inc = totalNumberOfFrame-seq+1;
                    System.out.println("...................................."+totalNumberOfFrame+" "+seq);
                    for (int inc = seq; inc <= totalNumberOfFrame + 1; inc++) {
                        System.out.println("sending seq no: "+seq);
                        last_chunkSize = file_to_send_length - (seq - 1) * 45;
                        System.out.println("");

                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>creating chunk from cli_buffer<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                        byte[] chunk_array = null;
                        if(inc==totalNumberOfFrame+1) {
                            chunk_array = Arrays.copyOfRange(cli_buffer, (seq - 1) * 45, file_to_send_length);//should be changed!
                        }
                        else{
                            chunk_array = Arrays.copyOfRange(cli_buffer, (seq - 1) * 45, seq*45);
                        }
                        int chunk_array_size = 0;
                        chunk_array_size = chunk_array.length;
                        for (int i = 0; i < chunk_array_size; i++) {
                            System.out.print(chunk_array[i] + " ");
                        }
                        System.out.println(" ");

                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> converting to binary <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                        String s;
                        s = byteToString(chunk_array, seq, ack);
                        System.out.println("binary representation:");
                        System.out.println(s);
                        System.out.println("");

                        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> bit stuffing <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                        s = bitStuff(s, chunk_array_size);
                        System.out.println("after stuffing bits:");
                        System.out.println(s);
                        System.out.println("");

                        //--------------------------------------------------------------------------------------------------------------------
                        current_position += chunk_size;
                        outToServer.writeObject(s);
                        System.out.println("-----------------------------------------------------------------------------------------------");

                        seq+=1;

                    }
                    int ack_from_server;
                    seq = ack+1;
                    for(int inc = 0;inc<ac_inc+1;inc++){
                        cli_socket.setSoTimeout(10000);
                        try {
                            acknowledgement = (String) inFromServer.readObject();
                        }catch (SocketTimeoutException e){
                            System.out.println("i am here2!");
                            break;
                        }
                        ack_from_server = get_acknowledgement(acknowledgement);

                        if(ack_from_server==ack+1){
                            ack +=1;
                            seq=ack+1;

                        }
                        System.out.println("acknowledgegement from server: "+ack_from_server+" "+ack+" "+seq);
                    }


                }
            }

        }

        String sent = "okay";
        outToServer.writeObject(sent);

        String message="";
        String confirmation="";
        int file_count = 0;
        try {
            file_count = (int) inFromServer.readObject();
        } catch (IOException e) {
            // e.printStackTrace();
            System.out.println("u r logged out! try again!");
            return;
        }

        System.out.println("you have files:"+file_count);

        for(int i=0;i<file_count;i++) {

            message = (String) inFromServer.readObject();
            System.out.println(message);
            if (!message.equals("no file to send to u, check after a while, bye!")) {

                confirmation = sc.next();
                try{
                outToServer.writeObject(confirmation);
                } catch (IOException e) {
                    // e.printStackTrace();
                    System.out.println("u r logged out! try again!");
                    return;
                }
                if (confirmation.equals("receive")) {
                    /////////////////////////////////code
                    byte[] rec_buffer;
                    int file_to_receive_length = 0;
                    String file_to_receive_name = "";
                    File file_to_receive;
                    FileOutputStream fileOutputStream;
                    BufferedOutputStream bufferedOutputStream;
                    try {
                        file_to_receive_name = (String) inFromServer.readObject();
                        file_to_receive_length = (int) inFromServer.readObject();
                    }catch (IOException e){
                        System.out.println("something went wrong! try again!");
                        return;

                    }
                    rec_buffer = new byte[file_to_receive_length];


                    inFromServer.read(rec_buffer, 0, file_to_receive_length);


                    file_to_receive = new File("C:\\Users\\maksudul_mimon\\Pictures\\" + file_to_receive_name);
                    fileOutputStream = new FileOutputStream(file_to_receive);
                    bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
                    bufferedOutputStream.write(rec_buffer, 0, file_to_receive_length);
                    bufferedOutputStream.flush();


                }
               //
            }

        }
        try{
        message = (String) inFromServer.readObject();
        } catch (IOException e) {
            // e.printStackTrace();
            System.out.println("u r logged out! try again!");
            return;
        }
        System.out.println(message);



        return;

    }
}
