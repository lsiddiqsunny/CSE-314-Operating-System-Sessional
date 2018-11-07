

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {

    public final static int SOCKET_PORT = 13267;  // you may change this
    public final static String FILE_TO_SEND = null;//"D:\\Study\\Versity_study\\Level3_term2\\Course_Outline_CSE317.pdf";  // you may change this
    public final static int FILE_SIZE = 704326; // file size temporary hard coded

    //String a = "D:\\Study\\Versity_study\\Level3_term2\\notice";

    public static int orginal = 2000000000;
    public static Vector<Hello_File> tracker = new Vector<Hello_File>();
    byte[] hek = new byte[788469665];



    public static LinkedList<Client> clients = new LinkedList<Client>();









    public static void main (String [] args ) throws IOException {
        FileInputStream fis = null;
        BufferedInputStream bis = null;

        OutputStream os = null;
        InputStream is = null;
        BufferedReader in = null;
        PrintWriter out = null;

        ServerSocket servsock = null;
        Socket sock = null;

        int flagg = 0;
        int id =0;
        String input = null;

        Iterator<Client> itr;






        try {
            servsock = new ServerSocket(SOCKET_PORT);
            while (true) {
                ClientWorker w;
                System.out.println("Waiting...");

                    sock = servsock.accept();
                    System.out.println("Accepted connection : " + sock);

                    try {
                        os = sock.getOutputStream();
                        is = sock.getInputStream();
                        out = new PrintWriter(sock.getOutputStream(),true);
                        in = new BufferedReader(new InputStreamReader(is));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    //to retrive student id from client
                    if((input=in.readLine())!=null){
                        id = Integer.parseInt(input);
                    }

                    itr = clients.iterator();
                    flagg = 0;
                    //to check if he is already logged in
                    while(itr.hasNext()){
                        Client client = itr.next();
                        System.out.println("id :"+client.id);
                        if(client.id==id){
                            flagg = 1;
                            break;
                        }
                    }


                    if(flagg==0){
                        //client do not logged in so he can log in now
                        System.out.println("New clientds added with id :"+id);
                        clients.add(new Client(id,sock));
                        //to send confirmation that he is now connected
                        out.println(1);
                        w = new ClientWorker(id,sock);
                        Thread t = new Thread(w);
                        t.start();
                    }
                    else{
                        //to send confirmation that he can not connect now
                        out.println(0);
                    }






            }
        }
        finally {
            if (servsock != null) servsock.close();
        }
    }
}


class ClientWorker  implements Runnable {

    public int id;
    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private int start;
    private int end;





    //Constructor
    ClientWorker(int id,Socket socket) {
        this.id=id;
        this.socket = socket;




    }

    /*public static void createAndShowGUI() {
        //Create and set up the window.
        frame = new JFrame("FileChooserDemo");
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        //Add content to the window.

        frame.add(new Gui());

        //Display the window.
        frame.pack();
        frame.setVisible(true);
        System.out.println("visible");
    }*/

    public void run() {
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;

        OutputStream os = null;
        InputStream is = null;
        PrintWriter out = null;
        BufferedReader in = null;

        int bytesRead;
        int current = 0;
        String input=null;
        int fileSize=0;
        String fileName=null;
        int total = 0;
        int chunkSize=0;
        int student_id_to_send=0;
        Iterator<Client> itr=null;
        int present =0;
        int file_start_index=0;
        int file_end_index=0;


        int present_tranmit_file_id=0;



        byte [] mybytearray  = null;




        //createAndShowGUI();


        try {
            os = socket.getOutputStream();
            is = socket.getInputStream();
            out = new PrintWriter(socket.getOutputStream(),true);
            in = new BufferedReader(new InputStreamReader(is));
        } catch (IOException e) {
            System.out.println("Line disconnected........");
            itr = Server.clients.iterator();
            while(itr.hasNext()){
                Client client15 = itr.next();
                if(client15.id==this.id){
                    Server.clients.remove(client15);
                    break;
                }

            }
            return;
        }

        out.println(100);//asking client what he wants-------------- 1
        //System.out.println("asking client for what he wants?");
        int num;
        int flag ;
        while(true){
            flag = 0;



            itr = Server.clients.iterator();
            while(itr.hasNext()){
                Client client15 = itr.next();
                System.out.println(client15.id+"  :  Total File : "+client15.total_file);

            }







            itr = Server.clients.iterator();
            int message=0;
            Client client;
            while(itr.hasNext()){
                client = itr.next();
                //System.out.println("id :"+client.id+", message :"+client.message);
                if(client.id==this.id){
                    message = client.total_file;
                    if(message==0){
                        out.println(message);//----------------------------2
                        System.out.println("sending messgae "+message);
                    }
                    else{
                        out.println(1);//--------------------------------- 2
                        System.out.println("sending messgae "+message);

                        StringBuilder info=new StringBuilder();
                        Enumeration e = client.v.elements();
                        while (e.hasMoreElements()){
                            Obj obj= (Obj)e.nextElement();
                            info.append("File Id:").append("&").append(obj.file_id).append("%").append("Student Id:").append(obj.std_id_from_get).append("%").append("File Name :")
                                    .append(obj.file_name).append("%").append("File size:").append(obj.file_size).append("%");
                            System.out.println(info);



                        }
                        String msg=info.toString();

                        try {
                            if((input = in.readLine())!=null) { //---------------2
                                System.out.println("getting "+input);
                            }
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        //System.out.println("Sending message :"+message);
                        //String info = "From Id :"+client.student_id_who_send+"File Name : "+client.filename+" , Size:"+client.fileSize;
                        out.println(msg);//---------------------------------- 3
                        System.out.println("sending pending file info "+msg);
                    }
                    break;
                }
            }


            try {
                if((input=in.readLine())!=null){//-----------------------------4
                    System.out.println("getting choice :"+input);

                    num = Integer.parseInt(input);
                    //System.out.println(num);
                    switch(num){
                        case 1:{///clients want to sends
                            try{
                                System.out.println("clients want to send file to server");
                                if((input = in.readLine())!=null){//------------------------------------5
                                    System.out.println("getting student id:"+input);

                                    student_id_to_send = Integer.parseInt(input);
                                    itr = Server.clients.iterator();
                                    present = 0;
                                    while(itr.hasNext()){
                                        Client client2 = itr.next();
                                        if(client2.id==student_id_to_send){
                                            //client.message = 1;
                                            present = 1;
                                            System.out.println("Client with id "+student_id_to_send+" is present.............");
                                            break;
                                        }
                                    }

                                }
                                if(present==1){
                                    System.out.println("Client with id "+student_id_to_send+" is present.............");

                                    out.println(50);//---------------------------------------6
                                    System.out.println("sending student id is present :"+50);




                                    //for retriving file name
                                    if((input = in.readLine())!=null){//-----------------------------------------7
                                        System.out.println("getting file name:"+input);
                                        fileName = input;
                                    }


                                    //for retriving file size
                                    if((input = in.readLine())!=null){//--------------------------------------8
                                        System.out.println("getting file size:"+input);
                                        fileSize = Integer.parseInt(input);
                                        mybytearray  = new byte [fileSize];
                                    }



                                    System.out.println("File  size = " + fileSize);


                                        out.println(1);//-------------------------------------9
                                        System.out.println("sending buffer size:"+1);
                                        Random randomGenerator = new Random();
                                       //int randomInt = randomGenerator.nextInt(fileSize/1000);
                                        int randomInt=0;
                                        int num_var = 0;
                                         //(fileSize/255+1)+(int)Math.ceil(Math.floor(((fileSize/255+1)*8)/5)/8)+4
                                        //int randomInt = (fileSize/255)+1;
                                        randomInt = fileSize/800;
                                        num_var = (randomGenerator.nextInt(randomInt));
                                        if(num_var<randomInt) num_var = randomInt/2;
                                        int chunk_size = fileSize/num_var+1;
                                        //int chunk_size = 1000;
                                        out.println(chunk_size);//-------------------------------------10
                                        System.out.println("sending chunk size:"+chunk_size);



                                        //starting file sending...
                                        total = 0;
                                        chunkSize = chunk_size;
                                        int remain ;
                                        int s = 1;
    ////////////////////////////////////////////////////////////////////////////////////////////////////
                                        int frame_size=2;
                                        int sequence_number =0;
                                        int confirmed_sequence_number = 0;

                                        int y=1;
                                        int x = 1;

                                    while(total != fileSize){
                                       // System.out.println("Hello4");
                                            //out.println("");//0--->
                                            String str=new String();
                                            while(true){
                                                System.out.println("getting :");

                                                str=in.readLine();
                                                System.out.println("getting :");



                                                 if(!str.equals("") ){
                                                    System.out.println("getting :"+y);
                                                     in.readLine();
                                                     out.println("confirm");//-------->



                                                     int received_chunk = 0;
                                                    //if(fileSize-total>=chunkSize) received_chunk=chunkSize;
                                                   // else received_chunk = fileSize-total;
                                                    received_chunk=chunkSize;
                                                    if(received_chunk == (str.length()/8)){
                                                        //frame recognizing starting





                                                        String find_frame = "01111110";
                                                        int lastIndex = 0;
                                                        int count_frame_structure = 0;

                                                        out.println("");//-------->

                                                        while (lastIndex != -1) {

                                                            lastIndex = str.indexOf(find_frame, lastIndex);

                                                            if (lastIndex != -1) {
                                                                count_frame_structure++;
                                                                lastIndex += find_frame.length();
                                                            }
                                                        }

                                                        out.println("");//--------->
                                                        if(count_frame_structure == 2){
                                                            String frame_with_stuffed_bit = new String();
                                                            int first_index_of_frame = str.indexOf("01111110",0);
                                                            int last_index_of_frame = str.indexOf("01111110",first_index_of_frame+8);
                                                            frame_with_stuffed_bit = str.substring(first_index_of_frame+8,last_index_of_frame);
                                                            //frame recognizing ended

                                                            out.println("");//0--->




                                                            System.out.println("stuffed bits removing starting....");
                                                            //stuffed bit removing...........
                                                            int counter=0;
                                                            String frame_without_stuffed_bit = new String();
                                                            for(int i=0;i<frame_with_stuffed_bit.length();i++){
                                                                if(frame_with_stuffed_bit.charAt(i)=='0'){
                                                                    frame_without_stuffed_bit += frame_with_stuffed_bit.charAt(i);
                                                                    counter=0;
                                                                }
                                                                else {
                                                                    frame_without_stuffed_bit += frame_with_stuffed_bit.charAt(i);
                                                                    counter++;
                                                                }

                                                                if(counter==5){
                                                                    i++;
                                                                    counter=0;
                                                                }

                                                                out.println("");//--->

                                                            }
                                                            //  System.out.println("frame_without stuffed bit : "+frame_without_stuffed_bit);
                                                            //stuffed bit removed
                                                            System.out.println("stuffed bits removing ending....");





                                                            int frame_length = frame_without_stuffed_bit.length();


                                                            String receivedCheckSum = frame_without_stuffed_bit.substring(frame_length-8,frame_length);
                                                            String frame_without_checksum = frame_without_stuffed_bit.substring(0,frame_length-8);


                                                            out.println("");//0--->

                                                            System.out.println("checksum calculation starting....");
                                                            // checksum calculation start
                                                            String newCheckSum = new String();
                                                            int numbers_of_1 ;
                                                            for(int i=0;i<8;i++){
                                                                numbers_of_1 = 0;
                                                                for(int j=i;j<frame_without_checksum.length();j+=8){
                                                                    if(frame_without_checksum.charAt(j)=='1') numbers_of_1 += 1;
                                                                }
                                                                if((numbers_of_1 % 2 ) == 1) newCheckSum += "1";
                                                                else newCheckSum += "0";

                                                                out.println("");//0--->

                                                            }

                                                            System.out.println("Calculated Checksum : "+newCheckSum);
                                                            System.out.println("Received Checksum : "+receivedCheckSum);
                                                            //checksum calculation ended
                                                            System.out.println("checksum calculation ending....");






                                                            if(receivedCheckSum.equals(newCheckSum)){

                                                                if(frame_without_stuffed_bit.substring(0,8).equals("00000001")) {
                                                                    System.out.println("Data found.");



                                                                    //getting sequence number (start)
                                                                    String sequence_number_string = (frame_without_checksum.substring(8,16));
                                                                    sequence_number  = 0;
                                                                    for(int k=0;k<8;k++){
                                                                        if(sequence_number_string.charAt(k)=='1') sequence_number += Math.pow(2,(7-k));
                                                                    }
                                                                    System.out.println("sequence number : "+sequence_number);
                                                                    //getting sequence number (end)

                                                                    out.println("");//0--->

                                                                    if(sequence_number == confirmed_sequence_number) break;






                                                                    //getting acknowledge number (start)
                                                        /*No need for data frame */
                                                                    //getting acknowledge number (end)



                                                                    //getting payload (start)
                                                                    String payload = frame_without_checksum.substring(24,frame_without_checksum.length());
                                                                    //getting payload (end)



                                                                    System.out.println("Data conveting starting....");
                                                                    //converting received string into byte array staring
                                                                    byte[] byte_arr = new byte[payload.length() / 8];
                                                                    int j = 0;
                                                                    int k = 0;


                                                                    while (j < payload.length()) {
                                                                        String ss = payload.substring(j, j + 8);
                                                                        byte_arr[k] = (byte) Integer.parseInt(ss, 2);
                                                                        k++;
                                                                        j += 8;

                                                                        out.println("");//0--->

                                                                    }
                                                                    System.out.println("Data conveting ending....");
                                                                    ////converting received string into byte array ended


                                                                    k = 0;
                                                                    int t = total+byte_arr.length;
                                                                    System.out.println("total : "+total+";  byte_arr :"+byte_arr.length+";  total+byte_arr: "+t);

                                                                    System.out.println("file size :"+fileSize);

                                                                    for (j = total; j < total + byte_arr.length; j++) {
                                                                        mybytearray[j] = byte_arr[k];
                                                                        k++;

                                                                        out.println("");//0--->

                                                                    }
                                                                    total = total + byte_arr.length;
                                                                    confirmed_sequence_number = sequence_number;
                                                                    //acknowledge sending (start)

                                                                    //frame without checksum forming starting
                                                                    String payload_ack = "00000000";
                                                                    String frame_without_checksum_ack = new String();
                                                                    frame_without_checksum_ack += "00000000";//for kind of frame (Data --> 00000001 /ack --> 00000000)
                                                                    frame_without_checksum_ack += "00000000";//for seqNo ---> for acknowledge frame it is arbitrary
                                                                    frame_without_checksum_ack += String.format("%8s", Integer.toBinaryString(sequence_number & 0xFF)).replace(' ', '0');
                                                                    frame_without_checksum_ack += payload_ack; //for payload (in ack, it imeans nothing)
                                                                    //frame without checksum forming ended

                                                                    out.println("");//0--->



                                                                    // checksum calculation start
                                                                    String checksum_ack = new String();
                                                                    int numbers_of_1_ack ;
                                                                    for(int i=0;i<8;i++){
                                                                        numbers_of_1_ack = 0;
                                                                        for(int m =i;m<frame_without_checksum_ack.length();m+=8){
                                                                            if(frame_without_checksum_ack.charAt(m)=='1') numbers_of_1_ack += 1;
                                                                        }
                                                                        if((numbers_of_1_ack % 2 ) == 1) checksum_ack += "1";
                                                                        else checksum_ack += "0";

                                                                        out.println("");//0--->

                                                                    }
                                                                    //checksum calculation ended


                                                                    String frame_with_checksum_ack  = frame_without_checksum_ack+checksum_ack;


                                                                    //Bit stuffing into frame
                                                                    int count =0;
                                                                    String stuffed_frame_ack = new String();
                                                                    stuffed_frame_ack = "01111110";
                                                                    for(int i=0;i<frame_with_checksum_ack.length();i++){
                                                                        if(frame_with_checksum_ack.charAt(i)=='0'){
                                                                            stuffed_frame_ack = stuffed_frame_ack+frame_with_checksum_ack.charAt(i);
                                                                            count=0;
                                                                        }
                                                                        else {
                                                                            stuffed_frame_ack = stuffed_frame_ack + frame_with_checksum_ack.charAt(i);
                                                                            count++;
                                                                        }

                                                                        if(count==5){
                                                                            stuffed_frame_ack = stuffed_frame_ack + '0';
                                                                            count=0;
                                                                        }


                                                                        out.println("");//0--->

                                                                    }
                                                                    stuffed_frame_ack += "01111110";
                                                                    //Bit stuffing ended


                                                                    out.println(stuffed_frame_ack);

                                                                    //out.println("complete");



                                                                    System.out.println("Hello3");
                                                                    break;
                                                                }
                                                                else{
                                                                    //acknowledge sending (start)------->error acknowledgement

                                                                    //frame without checksum forming starting
                                                                    String payload_ack = "00000000";
                                                                    String frame_without_checksum_ack = new String();
                                                                    frame_without_checksum_ack += "00000000";//for kind of frame (Data --> 00000001 /ack --> 00000000)
                                                                    frame_without_checksum_ack += "00000000";//for seqNo ---> for acknowledge frame it is arbitrary
                                                                    frame_without_checksum_ack += String.format("%8s", Integer.toBinaryString((sequence_number-1) & 0xFF)).replace(' ', '0');
                                                                    frame_without_checksum_ack += payload_ack; //for payload (in ack, it imeans nothing)
                                                                    //frame without checksum forming ended

                                                                    out.println("");//0--->



                                                                    // checksum calculation start
                                                                    String checksum_ack = new String();
                                                                    int numbers_of_1_ack ;
                                                                    for(int i=0;i<8;i++){
                                                                        numbers_of_1_ack = 0;
                                                                        for(int m =i;m<frame_without_checksum_ack.length();m+=8){
                                                                            if(frame_without_checksum_ack.charAt(m)=='1') numbers_of_1_ack += 1;
                                                                        }
                                                                        if((numbers_of_1_ack % 2 ) == 1) checksum_ack += "1";
                                                                        else checksum_ack += "0";


                                                                        out.println("");//0--->

                                                                    }
                                                                    //checksum calculation ended


                                                                    String frame_with_checksum_ack  = frame_without_checksum_ack+checksum_ack;


                                                                    //Bit stuffing into frame
                                                                    int count =0;
                                                                    String stuffed_frame_ack = new String();
                                                                    stuffed_frame_ack = "01111110";
                                                                    for(int i=0;i<frame_with_checksum_ack.length();i++){
                                                                        if(frame_with_checksum_ack.charAt(i)=='0'){
                                                                            stuffed_frame_ack = stuffed_frame_ack+frame_with_checksum_ack.charAt(i);
                                                                            count=0;
                                                                        }
                                                                        else {
                                                                            stuffed_frame_ack = stuffed_frame_ack + frame_with_checksum_ack.charAt(i);
                                                                            count++;
                                                                        }

                                                                        if(count==5){
                                                                            stuffed_frame_ack = stuffed_frame_ack + '0';
                                                                            count=0;
                                                                        }


                                                                        out.println("");//0--->

                                                                    }
                                                                    stuffed_frame_ack += "01111110";
                                                                    //Bit stuffing ended


                                                                    out.println(stuffed_frame_ack);



                                                                    System.out.println("Hello9");
                                                                    break;
                                                                }
                                                            }
                                                            else{
                                                                //System.out.println("Error.........................");
                                                                //System.exit(1);
                                                                //acknowledge sending (start)

                                                                //frame without checksum forming starting
                                                                String payload_ack = "00000000";
                                                                String frame_without_checksum_ack = new String();
                                                                frame_without_checksum_ack += "00000000";//for kind of frame (Data --> 00000001 /ack --> 00000000)
                                                                frame_without_checksum_ack += "00000000";//for seqNo ---> for acknowledge frame it is arbitrary
                                                                frame_without_checksum_ack += String.format("%8s", Integer.toBinaryString((sequence_number-1) & 0xFF)).replace(' ', '0');
                                                                frame_without_checksum_ack += payload_ack; //for payload (in ack, it imeans nothing)
                                                                //frame without checksum forming ended

                                                                out.println("");//0--->

                                                                // checksum calculation start
                                                                String checksum_ack = new String();
                                                                int numbers_of_1_ack ;
                                                                for(int i=0;i<8;i++){
                                                                    numbers_of_1_ack = 0;
                                                                    for(int m =i;m<frame_without_checksum_ack.length();m+=8){
                                                                        if(frame_without_checksum_ack.charAt(m)=='1') numbers_of_1_ack += 1;
                                                                    }
                                                                    if((numbers_of_1_ack % 2 ) == 1) checksum_ack += "1";
                                                                    else checksum_ack += "0";

                                                                    out.println("");//0--->

                                                                }
                                                                //checksum calculation ended


                                                                String frame_with_checksum_ack  = frame_without_checksum_ack+checksum_ack;


                                                                //Bit stuffing into frame
                                                                int count =0;
                                                                String stuffed_frame_ack = new String();
                                                                stuffed_frame_ack = "01111110";
                                                                for(int i=0;i<frame_with_checksum_ack.length();i++){
                                                                    if(frame_with_checksum_ack.charAt(i)=='0'){
                                                                        stuffed_frame_ack = stuffed_frame_ack+frame_with_checksum_ack.charAt(i);
                                                                        count=0;
                                                                    }
                                                                    else {
                                                                        stuffed_frame_ack = stuffed_frame_ack + frame_with_checksum_ack.charAt(i);
                                                                        count++;
                                                                    }

                                                                    if(count==5){
                                                                        stuffed_frame_ack = stuffed_frame_ack + '0';
                                                                        count=0;
                                                                    }


                                                                    out.println("");//0--->

                                                                }
                                                                stuffed_frame_ack += "01111110";
                                                                //Bit stuffing ended


                                                                out.println(stuffed_frame_ack);



                                                                System.out.println("Hello11");
                                                                break;
                                                            }
                                                        }
                                                        else{
                                                            //System.out.println("Error.........................");
                                                            //System.exit(1);
                                                            //acknowledge sending (start)

                                                            //frame without checksum forming starting
                                                            String payload_ack = "00000000";
                                                            String frame_without_checksum_ack = new String();
                                                            frame_without_checksum_ack += "00000000";//for kind of frame (Data --> 00000001 /ack --> 00000000)
                                                            frame_without_checksum_ack += "00000000";//for seqNo ---> for acknowledge frame it is arbitrary
                                                            frame_without_checksum_ack += String.format("%8s", Integer.toBinaryString((sequence_number-1) & 0xFF)).replace(' ', '0');
                                                            frame_without_checksum_ack += payload_ack; //for payload (in ack, it imeans nothing)
                                                            //frame without checksum forming ended

                                                            out.println("");//0--->


                                                            // checksum calculation start
                                                            String checksum_ack = new String();
                                                            int numbers_of_1_ack ;
                                                            for(int i=0;i<8;i++){
                                                                numbers_of_1_ack = 0;
                                                                for(int m =i;m<frame_without_checksum_ack.length();m+=8){
                                                                    if(frame_without_checksum_ack.charAt(m)=='1') numbers_of_1_ack += 1;
                                                                }
                                                                if((numbers_of_1_ack % 2 ) == 1) checksum_ack += "1";
                                                                else checksum_ack += "0";

                                                                out.println("");//0--->

                                                            }
                                                            //checksum calculation ended


                                                            String frame_with_checksum_ack  = frame_without_checksum_ack+checksum_ack;


                                                            //Bit stuffing into frame
                                                            int count =0;
                                                            String stuffed_frame_ack = new String();
                                                            stuffed_frame_ack = "01111110";
                                                            for(int i=0;i<frame_with_checksum_ack.length();i++){
                                                                if(frame_with_checksum_ack.charAt(i)=='0'){
                                                                    stuffed_frame_ack = stuffed_frame_ack+frame_with_checksum_ack.charAt(i);
                                                                    count=0;
                                                                }
                                                                else {
                                                                    stuffed_frame_ack = stuffed_frame_ack + frame_with_checksum_ack.charAt(i);
                                                                    count++;
                                                                }

                                                                if(count==5){
                                                                    stuffed_frame_ack = stuffed_frame_ack + '0';
                                                                    count=0;
                                                                }


                                                                out.println("");//0--->

                                                            }
                                                            stuffed_frame_ack += "01111110";
                                                            //Bit stuffing ended


                                                            out.println(stuffed_frame_ack);



                                                            System.out.println("Hello13");

                                                            break;
                                                        }
                                                    }

                                                    else{
                                                        //System.out.println("Error.........................");
                                                        //System.exit(1);
                                                        //acknowledge sending (start)

                                                        //frame without checksum forming starting
                                                        String payload_ack = "00000000";
                                                        String frame_without_checksum_ack = new String();
                                                        frame_without_checksum_ack += "00000000";//for kind of frame (Data --> 00000001 /ack --> 00000000)
                                                        frame_without_checksum_ack += "00000000";//for seqNo ---> for acknowledge frame it is arbitrary
                                                        frame_without_checksum_ack += String.format("%8s", Integer.toBinaryString((sequence_number-1) & 0xFF)).replace(' ', '0');
                                                        frame_without_checksum_ack += payload_ack; //for payload (in ack, it imeans nothing)
                                                        //frame without checksum forming ended

                                                        out.println("");//0--->

                                                        // checksum calculation start
                                                        String checksum_ack = new String();
                                                        int numbers_of_1_ack ;
                                                        for(int i=0;i<8;i++){
                                                            numbers_of_1_ack = 0;
                                                            for(int m =i;m<frame_without_checksum_ack.length();m+=8){
                                                                if(frame_without_checksum_ack.charAt(m)=='1') numbers_of_1_ack += 1;
                                                            }
                                                            if((numbers_of_1_ack % 2 ) == 1) checksum_ack += "1";
                                                            else checksum_ack += "0";

                                                            out.println("");//0--->

                                                        }
                                                        //checksum calculation ended


                                                        String frame_with_checksum_ack  = frame_without_checksum_ack+checksum_ack;


                                                        //Bit stuffing into frame
                                                        int count =0;
                                                        String stuffed_frame_ack = new String();
                                                        stuffed_frame_ack = "01111110";
                                                        for(int i=0;i<frame_with_checksum_ack.length();i++){
                                                            if(frame_with_checksum_ack.charAt(i)=='0'){
                                                                stuffed_frame_ack = stuffed_frame_ack+frame_with_checksum_ack.charAt(i);
                                                                count=0;
                                                            }
                                                            else {
                                                                stuffed_frame_ack = stuffed_frame_ack + frame_with_checksum_ack.charAt(i);
                                                                count++;
                                                            }

                                                            if(count==5){
                                                                stuffed_frame_ack = stuffed_frame_ack + '0';
                                                                count=0;
                                                            }


                                                            out.println("");//0--->

                                                        }
                                                        stuffed_frame_ack += "01111110";
                                                        //Bit stuffing ended


                                                        out.println(stuffed_frame_ack);



                                                        System.out.println("Hello5");
                                                        break;
                                                    }
                                                }
                                                else  {
                                                  //System.out.println(x);
                                                 //x++;
                                                    out.println("");//-------->
                                                    break;
                                                }
                                            }

                                        }

                                        String str;
                                        while(true){
                                            if(((str = in.readLine()).equals("1"))){

                                                System.out.println("File  downloaded (" + current + " bytes read)");
                                                out.println("1");//---------------------------------------------------------13
                                                System.out.println("sending 1");
                                                break;
                                            }
                                        }


                                        //bos.write(mybytearray, 0 , current);
                                        //bos.flush();





                                        int m = file_start_index;










                                        //to update client profile
                                        itr = Server.clients.iterator();
                                        int dFlag=0;
                                        int file_id=0;
                                        while(itr.hasNext()){
                                            Client client5 = itr.next();
                                            if(client5.id==student_id_to_send){
                                                System.out.println("Saving student id:"+student_id_to_send);
                                                dFlag=1;
                                                client5.total_file += 1;
                                                client5.file_id += 1;
                                                file_id=client5.file_id;
                                                client5.v.addElement(new Obj(client5.file_id,this.id,fileSize,fileName));

                                                client5.filename = fileName;
                                                break;
                                            }
                                        }


                                        Server.tracker.addElement(new Hello_File(student_id_to_send,file_id,fileName,mybytearray));



                                }
                                else{
                                    out.println(0);//--------------------------------6
                                    System.out.println("sending student id is present :"+0);
                                }
                            } catch (IOException e) {
                                System.out.println("Line disconnected........");
                                itr = Server.clients.iterator();
                                while(itr.hasNext()){
                                    Client client15 = itr.next();
                                    if(client15.id==this.id){
                                        Server.clients.remove(client15);
                                        break;
                                    }

                                }
                                return;
                            }
                            break;
                        }
                        case 2:{//clients want to receive
                            try {

                                // send file

                                //to retrive file id which clients want to receive
                                if((input = in.readLine())!=null){//------------------------------------14
                                    System.out.println("getting file Id:"+input);
                                    present_tranmit_file_id=Integer.parseInt(input);


                                }

                                //System.out.println("getting transmission file id : "+present_tranmit_file_id);




                                //int startSendingIndex=0;
                                //int endSendingIndex=0;
                                String file_name=null;
                                /*itr = Server.clients.iterator();
                                while(itr.hasNext()){
                                    Client client10 = itr.next();
                                    if(client10.id==this.id){

                                        file_name=client10.filename;
                                        System.out.println("Retrive file name from server :"+file_name);
                                        break;
                                    }

                                }*/




                                byte[] myByteArray=null;
                                Enumeration e = Server.tracker.elements();
                                while (e.hasMoreElements()){
                                    Hello_File hello_file= (Hello_File) e.nextElement();
                                    System.out.println(hello_file.std_id+"   "+hello_file.file_id);
                                    if(hello_file.std_id==this.id && hello_file.file_id==present_tranmit_file_id){
                                        fileSize= hello_file.file_arr.length;
                                        file_name=hello_file.file_name;
                                        System.out.println("retrive file size from server :"+fileSize);
                                        myByteArray=new byte[fileSize];
                                        myByteArray=hello_file.file_arr;
                                        break;
                                    }


                                }

                                input=null;
                                if((input = in.readLine())!=null){//----------------------------------15
                                    System.out.println("getting 100");
                                    System.out.println("Sending starting "+input);
                                }

                                //sending file name to receiver
                                out.flush();
                                out.println(file_name);//---------------------------------------16
                                System.out.println("sending file name to receiver :"+file_name);



                                input=null;
                                if((input = in.readLine())!=null){//----------------------------------15
                                    System.out.println("getting 150");
                                    System.out.println("Sending starting "+input);
                                }


                                //sending file size to client
                                out.flush();
                                out.println(fileSize);//-----------------------------------------17
                                System.out.println("sending file size to receiver :"+fileSize);


                                input=null;
                                if((input = in.readLine())!=null){//----------------------------------15
                                    System.out.println("getting 200");
                                    System.out.println("Sending starting "+input);
                                }


                                Random randomGenerator = new Random();
                                chunkSize = randomGenerator.nextInt(fileSize);


                                //sending chunk size to client
                                out.flush();
                                out.println(chunkSize);//-----------------------------------------17
                                System.out.println("sending chunk size to receiver :"+fileSize);


                                input=null;
                                if((input = in.readLine())!=null){//----------------------------------15
                                    System.out.println("getting 250");
                                    System.out.println("Sending starting "+input);
                                }








                                total =0;

                                //chunkSize=fileSize/255+1;
                                int reading_length;
                                int remain=fileSize;


                                while(total != fileSize){
                                    os.write(myByteArray,total,chunkSize);//-----------------------------------18
                                    os.flush();
                                    System.out.println("Sending "+chunkSize+"bytes");
                                    if((input = in.readLine())!=null){//-----------------------------------------19
                                        System.out.println("server getting confermation"+input);
                                    }


                                    total = total + chunkSize;
                                    remain = fileSize-total;
                                    System.out.println("remain "+remain+"bytes");

                                    if(chunkSize>remain){
                                        chunkSize = remain;
                                    }


                                }
                                os.flush();
                                System.out.println("Done.");
                                while((input = in.readLine())!=null){//---------------------------------------------20
                                    if(Integer.parseInt(input)==-1) break;
                                    System.out.println("getting "+input);
                                }





                                itr = Server.clients.iterator();
                                //int message=0;
                                Client client20;
                                while(itr.hasNext()){
                                    client = itr.next();
                                    //System.out.println("id :"+client.id+", message :"+client.message);
                                    if(client.id==this.id){
                                            Enumeration f = client.v.elements();
                                            while (f.hasMoreElements()){
                                                Obj obj= (Obj)f.nextElement();
                                                if(obj.file_id==present_tranmit_file_id) {
                                                    client.v.removeElement(obj);
                                                    client.total_file--;
                                                    break;
                                                }



                                        }


                                        break;
                                    }
                                }





                            } catch (FileNotFoundException e) {
                                System.out.println("Line disconnected........");
                                itr = Server.clients.iterator();
                                while(itr.hasNext()){
                                    Client client15 = itr.next();
                                    if(client15.id==this.id){
                                        Server.clients.remove(client15);
                                        break;
                                    }

                                }
                                return;
                            } catch (IOException e) {
                                System.out.println("Line disconnected........");
                                itr = Server.clients.iterator();
                                while(itr.hasNext()){
                                    Client client15 = itr.next();
                                    if(client15.id==this.id){
                                        Server.clients.remove(client15);
                                        break;
                                    }

                                }
                                return;
                            }
                            break;
                        }//case 2 ends here

                        case 3:{
                            //flag = 1;
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                //e.printStackTrace();
                System.out.println("Line disconnected........");
                itr = Server.clients.iterator();
                while(itr.hasNext()){
                    Client client15 = itr.next();
                    if(client15.id==this.id){
                        Server.clients.remove(client15);
                        break;
                    }

                }
               return;
            }


        }









    }


}





/*

int received_chunk = 0;
                                                                if(fileSize-total>=chunkSize) received_chunk=chunkSize;
                                                                else received_chunk = fileSize-total;
                                                                if(received_chunk == byte_arr.length){
                                                                    k = 0;
                                                                    int t = total+byte_arr.length;
                                                                    System.out.println("total : "+total+";  byte_arr :"+byte_arr.length+";  total+byte_arr: "+t);

                                                                    System.out.println("file size :"+fileSize);

                                                                    for (j = total; j < total + byte_arr.length; j++) {
                                                                        mybytearray[j] = byte_arr[k];
                                                                        k++;

                                                                        out.println("");//0--->

                                                                    }
                                                                    total = total + byte_arr.length;
                                                                    //acknowledge sending (start)

                                                                    //frame without checksum forming starting
                                                                    String payload_ack = "00000000";
                                                                    String frame_without_checksum_ack = new String();
                                                                    frame_without_checksum_ack += "00000000";//for kind of frame (Data --> 00000001 /ack --> 00000000)
                                                                    frame_without_checksum_ack += "00000000";//for seqNo ---> for acknowledge frame it is arbitrary
                                                                    frame_without_checksum_ack += String.format("%8s", Integer.toBinaryString(sequence_number & 0xFF)).replace(' ', '0');
                                                                    frame_without_checksum_ack += payload_ack; //for payload (in ack, it imeans nothing)
                                                                    //frame without checksum forming ended

                                                                    out.println("");//0--->



                                                                    // checksum calculation start
                                                                    String checksum_ack = new String();
                                                                    int numbers_of_1_ack ;
                                                                    for(int i=0;i<8;i++){
                                                                        numbers_of_1_ack = 0;
                                                                        for(int m =i;m<frame_without_checksum_ack.length();m+=8){
                                                                            if(frame_without_checksum_ack.charAt(m)=='1') numbers_of_1_ack += 1;
                                                                        }
                                                                        if((numbers_of_1_ack % 2 ) == 1) checksum_ack += "1";
                                                                        else checksum_ack += "0";

                                                                        out.println("");//0--->

                                                                    }
                                                                    //checksum calculation ended


                                                                    String frame_with_checksum_ack  = frame_without_checksum_ack+checksum_ack;


                                                                    //Bit stuffing into frame
                                                                    int count =0;
                                                                    String stuffed_frame_ack = new String();
                                                                    stuffed_frame_ack = "01111110";
                                                                    for(int i=0;i<frame_with_checksum_ack.length();i++){
                                                                        if(frame_with_checksum_ack.charAt(i)=='0'){
                                                                            stuffed_frame_ack = stuffed_frame_ack+frame_with_checksum_ack.charAt(i);
                                                                            count=0;
                                                                        }
                                                                        else {
                                                                            stuffed_frame_ack = stuffed_frame_ack + frame_with_checksum_ack.charAt(i);
                                                                            count++;
                                                                        }

                                                                        if(count==5){
                                                                            stuffed_frame_ack = stuffed_frame_ack + '0';
                                                                            count=0;
                                                                        }


                                                                        out.println("");//0--->

                                                                    }
                                                                    stuffed_frame_ack += "01111110";
                                                                    //Bit stuffing ended


                                                                    out.println(stuffed_frame_ack);



                                                                    System.out.println("Hello3");
                                                                    break;
                                                                }
                                                                else{
                                                                    //System.out.println("Error.........................");
                                                                    //System.exit(1);
                                                                    //acknowledge sending (start)

                                                                    //frame without checksum forming starting
                                                                    String payload_ack = "00000000";
                                                                    String frame_without_checksum_ack = new String();
                                                                    frame_without_checksum_ack += "00000000";//for kind of frame (Data --> 00000001 /ack --> 00000000)
                                                                    frame_without_checksum_ack += "00000000";//for seqNo ---> for acknowledge frame it is arbitrary
                                                                    frame_without_checksum_ack += String.format("%8s", Integer.toBinaryString((sequence_number-1) & 0xFF)).replace(' ', '0');
                                                                    frame_without_checksum_ack += payload_ack; //for payload (in ack, it imeans nothing)
                                                                    //frame without checksum forming ended

                                                                    out.println("");//0--->

                                                                    // checksum calculation start
                                                                    String checksum_ack = new String();
                                                                    int numbers_of_1_ack ;
                                                                    for(int i=0;i<8;i++){
                                                                        numbers_of_1_ack = 0;
                                                                        for(int m =i;m<frame_without_checksum_ack.length();m+=8){
                                                                            if(frame_without_checksum_ack.charAt(m)=='1') numbers_of_1_ack += 1;
                                                                        }
                                                                        if((numbers_of_1_ack % 2 ) == 1) checksum_ack += "1";
                                                                        else checksum_ack += "0";

                                                                        out.println("");//0--->

                                                                    }
                                                                    //checksum calculation ended


                                                                    String frame_with_checksum_ack  = frame_without_checksum_ack+checksum_ack;


                                                                    //Bit stuffing into frame
                                                                    int count =0;
                                                                    String stuffed_frame_ack = new String();
                                                                    stuffed_frame_ack = "01111110";
                                                                    for(int i=0;i<frame_with_checksum_ack.length();i++){
                                                                        if(frame_with_checksum_ack.charAt(i)=='0'){
                                                                            stuffed_frame_ack = stuffed_frame_ack+frame_with_checksum_ack.charAt(i);
                                                                            count=0;
                                                                        }
                                                                        else {
                                                                            stuffed_frame_ack = stuffed_frame_ack + frame_with_checksum_ack.charAt(i);
                                                                            count++;
                                                                        }

                                                                        if(count==5){
                                                                            stuffed_frame_ack = stuffed_frame_ack + '0';
                                                                            count=0;
                                                                        }


                                                                        out.println("");//0--->

                                                                    }
                                                                    stuffed_frame_ack += "01111110";
                                                                    //Bit stuffing ended


                                                                    out.println(stuffed_frame_ack);



                                                                    System.out.println("Hello5");
                                                                    break;
                                                                }
 */

