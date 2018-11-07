package utils;

import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class frameUtils {
    public static void sendFile(File file, DataOutputStream outputStream, DataInputStream inputStream, PrintWriter clientLog){

        System.out.println("Sending file");
        //clientLog.println("Sending File");
        byte[] filedata = new byte[(int)file.length()*3/2];
        FileInputStream fileInputStream ;
        BufferedInputStream bufferedInputStream = null;

        //setup File input
        while(bufferedInputStream == null){
            try {
                fileInputStream = new FileInputStream(file);
                bufferedInputStream = new BufferedInputStream(fileInputStream);
                bufferedInputStream.read(filedata, 0, (int)file.length());
            } catch (Exception e) {
                System.out.println(e);
            }
        }

        //send file
        int payLoadSize = 0;
        int frames, start, seqNo, i;
        byte[] stuffed;


        System.out.println("waiting to receive payload size");

        while(payLoadSize == 0) {
            try {
                payLoadSize = inputStream.readInt();
            } catch (Exception e) {
                System.out.println(e);
            }
        }

        System.out.println("payload size received = " +payLoadSize );
        System.out.println("Payload size = " + payLoadSize);
        frames = (int)(file.length()/payLoadSize) + (file.length()%payLoadSize >0 ? 1: 0);

        clientLog.println("Number of frames = " + frames);
        System.out.println("running loop for "+ frames + " frames");
        start = 0;
        seqNo = 0;


        i = 0;
        while (true) {
            System.out.println("Sending frame no " + (i+1));
            clientLog.println("Sending frame no " + (i+1));
            //clientLog.println("start index " + start);
            stuffed = stuff(filedata, start, payLoadSize, 0, seqNo , clientLog);
            sendFrame(stuffed, seqNo, outputStream, inputStream , clientLog);

            if(i == frames-1){
                while(true) {
                    try {
                        outputStream.writeUTF("done");
                        break;
                    } catch (IOException e) {
                        clientLog.println(e);
                    }
                }
                System.out.println("done");
                break;
            }

            seqNo = 1- seqNo;
            start += payLoadSize;
            i++;
        }



    }

    public static byte[] receiveFile(long fileSize, DataInputStream inputStream, DataOutputStream outputStream, PrintWriter serverLog){
        byte[] fileData = new byte[(int)fileSize*3/2];
        int index = 0;


        int payLoadSize = ThreadLocalRandom.current().nextInt(2, 5+1)*10;
        int frames = (int)(fileSize/payLoadSize) + (fileSize%payLoadSize >0 ? 1: 0);

        byte [] received = new byte[payLoadSize+6];
        byte [] destuffed = null;
        byte checkSum;

        //String fileString = "";

        try {
            outputStream.writeInt(payLoadSize);
        } catch (IOException e) {
            System.out.println(e);
        }

        int currentSeq = 1;
        int status;
        String ending = "";

        int i =0;
        while (true) {
            status = 0;
            System.out.println("Expecting frame no " + (i+1) + " with seqNo " + (1-currentSeq));
            serverLog.println("Expecting frame no " + (i+1) + " with seqNo " + (1-currentSeq));

            while(status == 0) {
                try {
                    inputStream.read(received, 0, payLoadSize + 6);
                    destuffed = destuff(received, payLoadSize, serverLog);
                    //destuffedString = destuffString(received, serverLog);
                    checkSum = getCheckSum(received);
                } catch (IOException e) {
                    status = 0;
                    serverLog.println("Frame read failed");
                    continue;
                }

                if(getFrameType(received) != 0){
                    status = 0;
                    serverLog.println("Faulty frame received");
                }

                else if(getSeqNo(received) != 1-currentSeq){
                    status = 2;
                    serverLog.println("Previous frame received again");
                }

                /*else if(checkParity(destuffed, checkSum) == 0){
                    status = 0;
                    System.out.println("Checksum found errors");
                }*/

                else{
                    status = 1;
                    serverLog.println("Received frame is correct");
                }

            }

            //out of while loop means successfully received frame
            //send acknowledgement
            try {
                sendACK(currentSeq, outputStream, serverLog);
            } catch (IOException e) {
                System.out.println(e);
            }

            if(status == 1){
                if(destuffed[0] != 0) {
                    for (byte aDestuffed : destuffed) {
                        fileData[index++] = aDestuffed;
                    }
                }
                else{
                    for(int j = 1; j<destuffed.length; j++){
                        fileData[index++] = destuffed[j];
                    }
                }
                //fileString += destuffedString;
            }
            currentSeq = 1-currentSeq;
            i++;
            if(i >= frames){
                try {
                    ending = inputStream.readUTF();
                    break;
                } catch (IOException e) {
                    serverLog.println(e);
                }
            }

            if(Objects.equals(ending, "done")){
                System.out.println("done");
                break;
            }

        }

        return fileData;
    }

    private static void sendACK(int ackNo, DataOutputStream outputStream, PrintWriter serverLog) throws IOException {
        String frame = "01111110";
        frame += "1";
        frame += Integer.toString(ackNo);
        frame += "01111110";

        byte [] temp = new BigInteger(frame, 2).toByteArray();

        /*serverLog.print("Ack Frame : ");
        for(int i = 0; i<temp.length ; i++){
            serverLog.print(String.format("%8s", Integer.toBinaryString(temp[i] & 0xFF)).replace(' ', '0'));
        }
        serverLog.println("");

        */

        outputStream.write(temp, 0, temp.length);
    }

    private static void sendFrame(byte[] frame,int seq, DataOutputStream outputStream, DataInputStream inputStream, PrintWriter clientLog) {


        byte [] ack = new byte[3];

        while(true){
            try {
                outputStream.write(frame, 0, frame.length);
            } catch (IOException e) {
                //System.out.println("Frame sending failed");
                clientLog.println("Frame sending failed");
                continue;
            }

            //clientLog.println("Expecting ack with ackNo " + (1-seq));
            //System.out.println("Expecting ack with ackNo " + (1-seq));
            try {
                inputStream.read(ack, 0, 3);
            } catch (IOException e) {
                clientLog.println("Ack frame not received");
                continue;
            }

            if(getFrameType(ack) != 1){
                //System.out.println("Faulty ack frame");
                clientLog.println("Faulty ack frame");
            }
            else if(getSeqNo(ack) != 1-seq){
                clientLog.println("AckNo mismatch");
                //clientLog.println("Current seq" + seq + " received ack "  + getSeqNo(ack));
            }
            else break;
        }

        //clientLog.println("Frame with seq " + seq + " sent successfully");

    }

    /*
    public static byte[] receiveFrame(int seq, int frameSize, DataInputStream inputStream, DataOutputStream outputStream){
        byte[] received = new byte[frameSize];
        byte[] destuffed = null;
        byte checkSum;
        int status = 0;
        while(status == 0) {
            //read the block
            try {
                inputStream.read(received, 0, frameSize);
                destuffed = destuff(received);
                checkSum = getCheckSum(received);

            } catch (IOException e) {
                System.out.println("Frame read failed");
                continue;
            }

            if(getSeqNo(received) != 1-seq || getFrameType(received) != 0){
                status = 0;
                System.out.println("Faulty frame received");
            }
            else if(checkParity(destuffed, checkSum) == 0){
                status = 0;
                System.out.println("Checksum found errors");
            }
            else{
                status = 1;
                System.out.println("Received frame is correct");
            }
        }
        return destuffed;
    }
    */

    public static byte[] stuff(byte[] filedata, int start, int payLoadSize, int type, int seqNo, PrintWriter clientLog){
        byte [] temp;
        clientLog.println("Frame before stuffing");
        for(int i = 0; i<payLoadSize ; i++){
            //System.out.print(Integer.toBinaryString(Byte.toUnsignedInt(filedata[start+i]))+ " ");
            clientLog.print(String.format("%8s", Integer.toBinaryString(filedata[start+i] & 0xFF)).replace(' ', '0'));
            clientLog.print(' ');
        }
        clientLog.println("");

        byte mask = -128;
        byte current, checksum;

        StringBuilder output = new StringBuilder("01111110");
        int count = 0;
        int extra = 0;
        output.append(type);
        output.append(seqNo);

        if(type == 1 && seqNo == 1){count = 2;}
        checksum = 0x00;

        for(int i = 0; i<payLoadSize; i++){
            current = filedata[start+i];
            checksum = (byte)(checksum ^ filedata[start+i]);

            for(int j = 0; j<8; j++){
                if((current & mask) != 0){
                    count++;
                    if(count == 5){
                        output.append("10");
                        count = 0;
                        extra = (extra+1)%8;
                    }
                    else output.append('1');
                }
                else{
                    count = 0;
                    output.append("0");
                }
                current =(byte)(current << 1);
            }
        }

        output.append(String.format("%8s", Integer.toBinaryString(checksum & 0xFF)).replace(' ', '0'));
        output.append("01111110");

        //temp = output.toString().getBytes();
        //System.out.println(output);
        temp = new BigInteger(output.toString(), 2).toByteArray();

        /*clientLog.println("Frame after stuffing");
        for (byte aTemp : temp) {
            //System.out.print(Integer.toBinaryString(Byte.toUnsignedInt(temp[i]))+ " ");
            clientLog.print(String.format("%8s", Integer.toBinaryString(aTemp & 0xFF)).replace(' ', '0'));
            clientLog.print(' ');
        }

        clientLog.println("");
        */
        return temp;
    }

    private static byte getCheckSum(byte[] input){
        int index = input.length - 1;
        while(input[index]==0) index--;
        return input[index-1];
    }

    public static int checkParity(byte[] destuffed, byte checkSum){

        byte cS = 0;
        for(int i = 0; i<destuffed.length; i++ ){
            cS = (byte)(cS ^ destuffed[i]);
        }

        if(cS != checkSum) return 0;
        else return 1;
    }

    public static int getSeqNo(byte[] input){

        String frame = String.format("%8s", Integer.toBinaryString(input[0] & 0xFF)).replace(' ', '0')
                + String.format("%8s", Integer.toBinaryString(input[1] & 0xFF)).replace(' ', '0')
                + String.format("%8s", Integer.toBinaryString(input[2] & 0xFF)).replace(' ', '0');

        int index = 0;
        while (frame.charAt(index) == '0') index++;
        while (frame.charAt(index) == '1') index++;
        if(frame.charAt(index+2) == '1') return 1;
        else return 0;

    }

    public static int getFrameType(byte[] input){
        String frame = String.format("%8s", Integer.toBinaryString(input[0] & 0xFF)).replace(' ', '0')
                + String.format("%8s", Integer.toBinaryString(input[1] & 0xFF)).replace(' ', '0')
                + String.format("%8s", Integer.toBinaryString(input[2] & 0xFF)).replace(' ', '0');

        System.out.println("Getting frame type from " + frame);
        int index = 0;
        while (frame.charAt(index) == '0') index++;
        while (frame.charAt(index) == '1') index++;
        if(frame.charAt(index+1) == '1') return 1;
        else return 0;
    }

    public static byte[] destuff(byte[] input, int frameSize ,PrintWriter serverLog){

        byte [] temp = new byte[frameSize];

        //String frame = Integer.toBinaryString(Byte.toUnsignedInt(input[0]))
         //       + Integer.toBinaryString(Byte.toUnsignedInt(input[1]));

        String frame = "";

        for (byte anInput : input) {
            frame += String.format("%8s", Integer.toBinaryString(anInput & 0xFF)).replace(' ', '0');
        }

        //System.out.println("Received frame");
        //System.out.println(frame);

        //serverLog.println("Received frame");
        //serverLog.println(frame);


        int index = 0;
        int count = 0;
        while(frame.charAt(index) == '0') index++;
        while(frame.charAt(index) == '1') index++;
        index++;//skip the next 0 of delimeter

        if(frame.charAt(index) == '1' && frame.charAt(index+1) == '1') count = 2;
        index += 2;//skip the next 0, typeBit, seqNo

        String output = "";

        while(index<frame.length()){
            if(frame.charAt(index) == '0'){
                output += "0";
                count = 0;
                index++;
            }
            else if(frame.charAt(index) == '1'){
                output += "1";
                count++;
                if(count == 5){
                    if(frame.charAt(index+1) == '1')
                    {
                        output += "10";
                        break;
                    }
                    index +=2;
                    count = 0;
                }
                else index++;
            }
        }


        //System.out.println("input frame in destuffing");
        //System.out.println(frame);
        //System.out.println("output from destuffing");
        //System.out.println(output);

        String out = output.substring(0, output.length()-16);
        //temp = new BigInteger(output, 2).toByteArray();

        ArrayList<Integer> arrayList = new ArrayList<>();
        for(String str: output.split("(?<=\\G.{8})")){
            arrayList.add(Integer.parseInt(str, 2));
        }

        //temp = new byte[arrayList.size()];
        for(int i = 0; i< frameSize; i++){
            temp[i] = arrayList.get(i).byteValue();
        }
/*
        byte [] destuffed = null;

        if(temp[0] != 0){
            destuffed = new byte[temp.length - 2];
            System.arraycopy(temp, 0, destuffed, 0, temp.length - 2);
        }
        else {
            destuffed = new byte[temp.length - 3];
            System.arraycopy(temp, 1, destuffed, 0, temp.length-3);
        }
*/
        serverLog.println("Destuffed frame");
        for (byte aTemp : temp) {
            serverLog.print(String.format("%8s", Integer.toBinaryString(aTemp & 0xFF)).replace(' ', '0'));
            serverLog.print(" ");
        }
        serverLog.println("");

        return temp;
    }


    public static String destuffString(byte[] input, PrintWriter serverLog){


        String frame = "";

        for (byte anInput : input) {
            frame += String.format("%8s", Integer.toBinaryString(anInput & 0xFF)).replace(' ', '0');
        }

        //serverLog.println("Received frame");
        //serverLog.println(frame);


        int index = 0;
        int count = 0;
        while(frame.charAt(index) == '0') index++;
        while(frame.charAt(index) == '1') index++;
        index++;//skip the next 0 of delimeter

        if(frame.charAt(index) == '1' && frame.charAt(index+1) == '1') count = 2;
        index += 2;//skip the next 0, typeBit, seqNo

        String output = "";

        while(index< frame.length()){
            if(frame.charAt(index) == '0'){
                count = 0;
                output += "0";
                index++;
            }
            else if(frame.charAt(index) == '1'){
                count++;
                output += "1";
                if(count == 5){
                    if(frame.charAt(index+1) == '1')
                    {
                        output += "10";
                        break;
                    }
                    index +=2;
                    count = 0;
                }
                else index++;
            }
        }


        String out = output.substring(0, output.length()-16);
        //serverLog.println("destuffed");
        //serverLog.println(out);
        //temp = new BigInteger(output, 2).toByteArray();

        /*
        byte [] destuffed = null;

        if(temp[0] != 0){
            destuffed = new byte[temp.length - 2];
            System.arraycopy(temp, 0, destuffed, 0, temp.length - 2);
        }
        else {
            destuffed = new byte[temp.length - 3];
            System.arraycopy(temp, 1, destuffed, 0, temp.length-3);
        }

        serverLog.println("Destuffed frame");
        for (byte adestuffed : destuffed) {
            serverLog.print(String.format("%8s", Integer.toBinaryString(adestuffed & 0xFF)).replace(' ', '0'));
            serverLog.print(" ");
        }
        serverLog.println("");
        */

        return out;
    }

}
