import com.sun.javafx.binding.StringFormatter;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Created by maksudul_mimon on 9/25/2017.
 */
public class serverThread extends Thread{
    Socket socket = null;
    int std_id=0, receiver_id=0;
    //int start_of_available_buffer;
    String file_to_pass = "";
    int file_to_pass_length = 0;


    HashMap<Integer, Socket> hashmap;
    HashMap<Integer, Integer> hash_fId_fSize;
    HashMap<Integer, String> hash_not_available;
    HashMap<Integer, Integer> hash_fId_fstart;
    HashMap<Integer,String> hash_fId_fName;
    static byte [] ser_buffer;

    public serverThread(Socket socket, HashMap<Integer, Socket> hashmap,HashMap<Integer, Integer> hash_fId_fSize, HashMap<Integer, String> hash_not_available,
            HashMap<Integer, Integer> hash_fId_fstart,  HashMap<Integer, String> hash_fId_fName,byte [] ser_buffer){
        this.socket = socket;
        this.hashmap = hashmap;
        this. hash_fId_fSize = hash_fId_fSize;
        this.hash_not_available = hash_not_available;
        this.hash_fId_fstart = hash_fId_fstart;
        this.ser_buffer = ser_buffer;
        this.hash_fId_fName = hash_fId_fName;

    }




    String bitDestuff( String s){
        char[] char_array = {};
        int len;
        len = s.length();

        for (int i = 0 ;; i++) {
            if(i>=7 && s.substring(i,i+6).equals("111111")){
                break;
            }
            else if(i>=7 && s.substring(i,i+5).equals("11111")){

                char_array = s.toCharArray();

                for(int j=i+5;j<len-1;j++){
                    char_array[j]=char_array[j+1];
                }
                s = String.valueOf(char_array);
                s = s.substring(0,s.length()-1);
                len = s.length();

                i+=5;
            }

        }
        return s;

    }

    int get_checksum_frame(String s){
        int checksum_frame = 0;
        checksum_frame = Integer.parseInt(s.substring((s.length()/8-2)*8,(s.length()/8-1)*8),2);
        return checksum_frame;
    }

    int get_checksum_server( String s){
        int checksum_server = 0;

        int count_payload_byte = (s.length()/8)-6;
        for(int i=4;i<count_payload_byte+4;i++){
            checksum_server += Integer.parseInt(s.substring(i*8,(i+1)*8),2);
        }
        checksum_server=checksum_server%(120);
        System.out.println("checksum_re: "+ checksum_server);
        return checksum_server;


    }



    byte [] stringToByte(String s , int chunk_size){
        int count_payload_byte=0;
        String q ="";
        char p;
        int chunk_array_size = chunk_size;
        count_payload_byte = (s.length()/8)-6;
        System.out.println("count payload: "+count_payload_byte);


        for(int i=4;i<count_payload_byte+4;i++){
            p= (char)Integer.parseInt(s.substring(i*8,(i+1)*8),2);
            q += String.valueOf(p);
        }
        System.out.println("raw representation:");
        System.out.println(q);
        System.out.println("");



        byte [] chunk_array = q.getBytes();
        return chunk_array;
    }

    int get_seq(String s){
        int seq=0;
        seq = Integer.parseInt(s.substring(2*8,3*8),2);
        return seq;
    }

    public static String make_acknowlegdement(int ack){
        int ack_number = ack;///////////////////////////////need change ////////////////////////
        String s = "01111110";
        s += String.format("%8s", Integer.toBinaryString(((int) 'A') & 0xFF)).replace(' ', '0');
        s += String.format("%8s", Integer.toBinaryString(ack_number & 0xFF)).replace(' ', '0');
        s += "01111110";
        return s;

    }




    public void run() {
        ObjectOutputStream outToClient = null;
        ObjectInputStream inFromClient = null;
        Scanner scanner = new Scanner(System. in);
        try {
            outToClient = new ObjectOutputStream(socket.getOutputStream());
            inFromClient = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            // e.printStackTrace();
            return;
        }

        try {
            std_id = (int) inFromClient.readObject();
        } catch (IOException e) {
            // e.printStackTrace();
            System.out.println("user did not log in!");
            return;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


        while (hashmap.get(std_id) != null) {
            System.out.println("already logged in!");
            try {
                outToClient.writeObject("not permitted!");
            } catch (IOException e) {
                //e.printStackTrace();
                System.out.println("user did not log in!");
            }
            try {
                std_id = (int) inFromClient.readObject();
            } catch (IOException e) {
                //e.printStackTrace();

            } catch (ClassNotFoundException e) {
                //e.printStackTrace();
            }
        }
        hashmap.put(std_id, socket);
        System.out.println("a new log in!");
        try {
            outToClient.writeObject("permitted!");
        } catch (IOException e) {
            // e.printStackTrace();
            System.out.println("user logged out1!");
            hashmap.remove(std_id);
            return;

        }

        try {
            receiver_id = (int) inFromClient.readObject();
        } catch (IOException e) {
            // e.printStackTrace();
            System.out.println("user logged out!2");
            hashmap.remove(std_id);
            return;
        } catch (ClassNotFoundException e) {
            //e.printStackTrace();
        }


        System.out.println("yes!");

        /*try {
            socket.setSoTimeout(35000);
        } catch (SocketException e) {
            //e.printStackTrace();
        }*/


        if (hashmap.get(receiver_id) != null) {
            try {
                outToClient.writeObject("receiver is online!");
            } catch (IOException e) {
                // e.printStackTrace();
                System.out.println("user logged out!3");
                hashmap.remove(std_id);
                return;
            }
            System.out.println("receiver is online!");


            try {
                file_to_pass = (String) inFromClient.readObject();
                file_to_pass_length = (int) inFromClient.readObject();
            } catch (IOException e) {
                // e.printStackTrace();
                System.out.println("user logged out!4");
                hashmap.remove(std_id);
                return;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            System.out.println("file is acceptable" + '\n' + file_to_pass_length);
            String send;




            if (file_to_pass_length <= 300) {
                int slot = -1;
                for (int i = 0; i < 10; i++) {
                    if (hash_not_available.get(i) == null) {
                        slot = i;
                        System.out.println(slot);
                        hash_not_available.put(i, "blocked");
                        break;
                    }

                }
                if (slot >= 0) {
                    ////////////// file_id = receiver_id;
                    int start = slot * 300 + 1;
                    send = "send";
                    try {
                        outToClient.writeObject(send);
                    } catch (IOException e) {
                        //e.printStackTrace();
                        System.out.println("user logged out!5");
                        hashmap.remove(std_id);
                        return;
                    }
                    /////////////////////////////////////////////////////////////////// file acception
                    int current_position = start;
                    int chunk_size = 45;
                    int last_chunkSize = 0;
                    int surity = 1;
                    try {
                        outToClient.writeObject(chunk_size);
                    } catch (IOException e) {
                        // e.printStackTrace();
                        System.out.println("user logged out!6");
                        hashmap.remove(std_id);
                        return;
                    }

                    int seq = 1, ack = 1, totalNumberOfFrame = 0, rest = 0;
                    totalNumberOfFrame = file_to_pass_length / chunk_size;
                    if (file_to_pass_length % chunk_size != 0) {
                        rest = 1;
                    }


                    int count;
                    System.out.println(chunk_size);
                    while (seq+3 <= totalNumberOfFrame+1) {
                        count = 0;
                        for (int inc = 0; inc < 3; inc++) {
                            System.out.println("entering!");
                            //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> bit de-stuffing <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                            String s = ""; //// take from client
                            try {
                                s = (String) inFromClient.readObject();
                            } catch (IOException e) {
                                // e.printStackTrace();
                            } catch (ClassNotFoundException e) {
                                // e.printStackTrace();
                            }
                            s = bitDestuff(s);
                            System.out.println("after de-stuffing bits:");
                            System.out.println(s);
                            System.out.println("");
                            //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> converting from binary <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

                            int chunk_array_size;
                            byte[] chunk_array = stringToByte(s, chunk_size);
                            chunk_array_size = chunk_array.length;
                            for (int i = 0; i < chunk_array_size; i++) {
                                System.out.print(chunk_array[i] + " ");
                            }
                            System.out.println(" ");
                            //------------------------------------------------------------------------------------------------------

                            int seq_from_sender = get_seq(s);

                            System.out.println(seq_from_sender+" "+ seq);
                            for (int i = 0; i < chunk_size; i++) {
                                ser_buffer[i + start+(seq-1)*45] = chunk_array[i];
                            }
                            int frame_checksum;
                            int server_checksum;

                            frame_checksum = get_checksum_frame(s);
                            server_checksum = get_checksum_server(s);
                            String change = scanner.nextLine();
                            if(change.equals("yes")){
                                frame_checksum+=1;
                            }
                            if(frame_checksum == server_checksum && seq == seq_from_sender) {
                                seq += 1;
                                count+=1;
                            }
                            System.out.println("-----------------------------------------------------------------------------------------------");
                        }
                        System.out.println("count: "+count);

                        String acknowledgement = "";
                        for(int inc =0;inc<count;inc++) {
                            acknowledgement = make_acknowlegdement(ack);
                            try {
                                outToClient.writeObject(acknowledgement);
                                System.out.println("yes!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                            } catch (IOException e) {

                            }
                            System.out.println("------------------------------------->>>>>>>>"+ack+count);
                            ack+=1;
                        }
                        System.out.println("here!");

                        seq=ack;
                        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"+ack+" "+seq);
                    }

                    //---------------------------------------------------------for extra bytes-------------------------------------------------
                    //System.out.println(".........................."+seq+" "+ totalNumberOfFrame);
                    while(rest!=0 && seq<= totalNumberOfFrame+1) {
                        count = 0;
                        for (int inc = seq; inc <= totalNumberOfFrame + 1; inc++) {
                            String s = ""; //// take from client
                            try {
                                s = (String) inFromClient.readObject();
                            } catch (IOException e) {
                                // e.printStackTrace();
                            } catch (ClassNotFoundException e) {
                                // e.printStackTrace();
                            }

                            s = bitDestuff(s);

                            System.out.println("after de-stuffing bits:");
                            System.out.println(s);
                            System.out.println("");

                            //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> converting from binary <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                            int chunk_array_size;
                            byte[] chunk_array = stringToByte(s, chunk_size);
                            chunk_array_size = chunk_array.length;

                            for (int i = 0; i < chunk_array_size; i++) {
                                System.out.print(chunk_array[i] + " ");
                            }

                            for (int i = 0; i < chunk_array_size; i++) {
                                ser_buffer[i + start+(seq-1)*45] = chunk_array[i];
                            }

                            System.out.println(" ");

                            //------------------------------------------------------------------------------------------------------

                            current_position += chunk_size;
                            int seq_from_sender = get_seq(s);

                            System.out.println(seq_from_sender + " " + seq);

                            int frame_checksum;
                            int server_checksum;

                            frame_checksum = get_checksum_frame(s);
                            server_checksum = get_checksum_server(s);
                            String change = scanner.nextLine();
                            if(change.equals("yes")){
                                frame_checksum+=1;
                            }
                            if(frame_checksum == server_checksum) {
                                seq += 1;
                                count+=1;
                            }
                            System.out.println("-----------------------------------------------------------------------------------------------");

                        }
                        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"+count+" "+ack);
                        String acknowledgement = "";
                        for(int inc =0;inc<count;inc++) {
                            acknowledgement = make_acknowlegdement(ack);
                            try {
                                outToClient.writeObject(acknowledgement);
                                System.out.println("yes!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                            } catch (IOException e) {
                                //e.printStackTrace();
                            }
                            ack+=1;
                        }
                        seq = ack;
                        System.out.println("ack : "+ ack);
                    }

                    System.out.println("received!!!!");
                    int fid = 0;
                    for (int i = 0; i < 5; i++) {
                        if (hash_fId_fName.get(receiver_id + i * 1000 * 1000 * 1000) == null) {
                            fid = receiver_id + i * 1000 * 1000 * 1000;
                            break;
                        }
                    }
                    hash_fId_fstart.put(fid, start);
                    hash_fId_fSize.put(fid, file_to_pass_length);
                    hash_fId_fName.put(fid, file_to_pass);


                } else {
                    send = "not enough space";
                    ////////////////////////// receiving code
                    try {
                        outToClient.writeObject(send);
                    } catch (IOException e) {
                        //e.printStackTrace();
                        System.out.println("user logged out!");
                        hashmap.remove(std_id);
                        return;
                    }

                }

            }
        } else {
            try {
                outToClient.writeObject("receiver is offline!");
            } catch (IOException e) {
                // e.printStackTrace();
                System.out.println("user logged out!13");
                hashmap.remove(std_id);
                return;
            }
            System.out.println("receiver is offline!");
        }

        try {
            socket.setSoTimeout(100000);
        } catch (SocketException e) {
            //e.printStackTrace();
        }


        int file_count = 0;
        for (int i = 0; i < 5; i++) {
            if (hash_fId_fName.get(std_id + i * 1000 * 1000 * 1000) != null) {
                file_count++;
            }

        }
        System.out.println("file count for " + std_id + " is: " + file_count);

        try {
            String isSent = (String) inFromClient.readObject();
        } catch (IOException e) {
            //
        } catch (ClassNotFoundException e) {
           // e.printStackTrace();
        }
        try {
            System.out.println("okay!!!!!!");
            outToClient.writeObject(file_count);
        } catch (IOException e) {
            // e.printStackTrace();
            System.out.println("user logged out!13");
            hashmap.remove(std_id);
            return;
        }

        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        if (hash_fId_fSize.get(std_id) != null) {
            String file_to_receive;
            int file_to_receive_size;


            for(int i =0;i<file_count;i++){

                int fid;
                fid = std_id + i * 1000 * 1000 * 1000;

            file_to_receive = hash_fId_fName.get(fid);
            file_to_receive_size = hash_fId_fSize.get(fid);

            try {
                outToClient.writeObject("you have a file naming " + file_to_receive + " of size:" + file_to_receive_size + "..... receive the file?");
            } catch (IOException e) {
                //e.printStackTrace();
                System.out.println("user logged out!13");
                hashmap.remove(std_id);
                return;
            }
            String confirmation = "";

            try {
                confirmation = (String) inFromClient.readObject();
            } catch (IOException e) {
                //e.printStackTrace();
                System.out.println("user logged out!13");
                hashmap.remove(std_id);
                return;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            if (confirmation.equals("receive")) {
                /////////////////////////// write the code
                System.out.println("its time to send!!!!");
                System.out.println(hash_fId_fName.get(fid) + hash_fId_fSize.get(fid));
                try {
                    outToClient.writeObject(hash_fId_fName.get(fid));
                    outToClient.writeObject(hash_fId_fSize.get(fid));
                    outToClient.write(ser_buffer, hash_fId_fstart.get(fid), hash_fId_fSize.get(fid));
                    outToClient.flush();


                    //////////////////////////clear all
                    int index;
                    index = hash_fId_fstart.get(fid) / 3;
                    hash_fId_fName.remove(fid);
                    hash_fId_fSize.remove(fid);
                    hash_fId_fstart.remove(fid);
                    hash_not_available.remove(index);

                } catch (IOException e) {
                    System.out.println("user logged out!14");
                    hashmap.remove(std_id);
                    //e.printStackTrace();
                    return;
                }
            }

        }

    }
        System.out.println("here is okay also!!!!!");
            try {
                outToClient.writeObject("no file to send to u, check after a while, bye!");
                hashmap.remove(std_id);
                return;
            } catch (IOException e) {
                System.out.println("user logged out!15");
                hashmap.remove(std_id);
                //e.printStackTrace();
                return;
            }

        }
    }


