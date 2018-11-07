package ClientSide;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class ClientReceiver implements Runnable {
    Socket socket;
    ObjectInputStream inputStream;
    ObjectOutputStream outputStream;
    String message;
    String studentID;

   // String framedArray;
    FileOutputStream fos;

    String [] eightFrames;
    ClientReceiver(Socket s,ObjectOutputStream o,ObjectInputStream i,String st){
        socket = s;
        outputStream = o;
        inputStream = i;
        studentID = st;

        eightFrames = new String[8];
        new Thread(this).start();
    }
    @Override
    public void run() {
        try {
            Scanner in = new Scanner(System.in);

            Object o;
            o = inputStream.readObject();
            message=  o.toString();
            System.out.println(message);//successfully logged in


            if(message.compareTo("Successfully Logged In")==0) {

                while (true) {
                    System.out.println("Do you want to send file?(y/n) || Enter 'l' to log out");
                    String reply = in.nextLine();
                    outputStream.writeObject(reply);
                    if (reply.compareTo("y") == 0) {

                        System.out.println("Enter Receiver ID:");
                        String reID = in.nextLine();
                        outputStream.writeObject(reID); //send receiverID

                        o = inputStream.readObject();
                        String status = o.toString();
                        if (status.compareTo("online") == 0) {
                            System.out.println(reID + " Receiver is Online.\nEnter File Location :");
                            String fileName = in.nextLine();
                            File file = new File(fileName);
                            FileInputStream fin = new FileInputStream(file);
                            int size = (int) file.length();
                            System.out.println(size);

                            outputStream.writeObject(file.getName());
                            outputStream.writeObject(size);

                            //if the receiver wants to receive the file
                            String dec = inputStream.readObject().toString();
                            outputStream.writeObject(dec);
                            if(dec.compareTo("y")==0) {
                                int chunkSize = Integer.parseInt(inputStream.readObject().toString());
                                System.out.println("Chunk size: " + chunkSize);
                                int noChunks;
                                if (size % chunkSize == 0) {
                                    noChunks = size / chunkSize;
                                } else {
                                    noChunks = size / chunkSize + 1;
                                }
                                if (noChunks < 256) {
                                    int loop;

                                    if (noChunks % 8 == 0) {
                                        loop = noChunks / 8;
                                    } else {
                                        loop = noChunks / 8 + 1;
                                    }

                                    byte[] buff = new byte[chunkSize];


                                    int readByte = 0, totalSent = 0, tobeSent = 8;
                                    for (int k = 0; k < loop; k++) {
                                        String framedArray = "";
                                        if (noChunks - totalSent < 8) {
                                            tobeSent = noChunks - totalSent;
                                        } else {
                                            tobeSent = 8;
                                        }
                                        outputStream.writeObject(tobeSent);

                                        for (int i = 0; i < tobeSent; i++) {
                                            readByte = fin.read(buff, 0, chunkSize);
                                            framedArray = buildFrame(buff, readByte, 8 * k + i + 1);
                                            eightFrames[i] = framedArray;
                                            //store 8 frames in array
                                        }
                                        for (int i = 0; i < tobeSent; i++) {
                                            //send 8 frames
                                            //outputStream.writeObject(readByte);
                                            //outputStream.writeObject(framedArray.length());  //length of the bit array
                                            outputStream.writeObject(eightFrames[i]);
                                        }

                                        int p, count = 0;
                                        boolean flag = false;
                                        for (int i = 0; i < tobeSent; i++) {
                                            //get acknowledgement
                                            String ack = "";

                                            try {
                                                socket.setSoTimeout(2000);
                                                ack = inputStream.readObject().toString();

                                            } catch (SocketException e) {
                                                System.out.println("Socket exception");
                                                flag = true;
                                            } catch (SocketTimeoutException e) {
                                                System.out.println("TimeOut! Acknowledgement no. " + (8 * k + i + 1) + " missed");
                                                flag = true;
                                            } catch (IOException e) {
                                                System.out.println("Socket Time out :(");
                                                flag = true;
                                            }
                                            if (flag) {
                                                break;
                                            }

                                            int n = getAcknowledgementNo(ack);
                                            System.out.println("Acknowledgement No. " + n + " received");
                                            count++;
                                            socket.setSoTimeout(0);
                                        }
                                        socket.setSoTimeout(0);
                                        //totalSent+=count;
                                        //send the number of acknowledgement received
                                        outputStream.writeObject(count);

                                        while (count < tobeSent) {

                                            int newAck = getAck(tobeSent - count);
                                            socket.setSoTimeout(0);
                                            count += newAck;

                                            outputStream.writeObject(newAck);

                                            //System.out.println("Are u sleeping?");
                                        }
                                        totalSent += count;

                                    }
                                    fin.close();
                                    System.out.println("File successfully sent to "+reID);


                                } else {
                                    System.out.println("ServerSide.Server Says : No. of chunks exceeded 1 byte");
                                }
                            }
                            else {//receiver doesnt want to receive file
                                System.out.println("Transmission denied by "+reID);
                            }
                        }
                        else {
                            System.out.println(reID + " Receiver is Offline.");

                        }
                    }
                    else if(reply.compareTo("l")==0){
                        //log out
                        System.out.println("You are logged out");
                        break;
                    }
                    else {//receiver doesnt want to send file
                        int senderID= Integer.parseInt(inputStream.readObject().toString());
                        String fileName= inputStream.readObject().toString();
                        int fileSize= Integer.parseInt(inputStream.readObject().toString());

                        //send sender server the senderID
                        outputStream.writeObject(senderID);

                        System.out.println(senderID +" wants to send you a file.");
                        System.out.println("File name: "+fileName+ "\nFile size: "+fileSize);
                        System.out.println("Do you want to receive it?");

                        reply= in.nextLine();
                        outputStream.writeObject(reply);
                        if (reply.compareTo("y") == 0) {

                            System.out.println("Receiving file..");
                            receiveFile(fileName,fileSize);
                            System.out.println(fileName + " Successfully Received");
                        }

                    }
                }
            }

        } catch (IOException e) {
            System.out.println("IO Exception Occured");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println("Class Not Found");
        }

    }
    int getAck(int n){
        boolean flag = false;
        int count=0, no=0;
        String ack="";
        for (int i = 0; i < n; i++) {
            //get acknowledgement


            try{
                socket.setSoTimeout(1000);
                ack = inputStream.readObject().toString();

            }catch (SocketException e){
                System.out.println("Socket exception");
                flag=true;
            } catch (SocketTimeoutException e){
                System.out.println("TimeOut! Acknowledgement no. "+(no+1)+" missed");
                flag=true;
            } catch (IOException e){
                System.out.println("Socket Time out :(");
                flag=true;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            if(flag){
                break;
            }

            no =getAcknowledgementNo(ack);
            System.out.println("Acknowledgement No. "+no+ " received");
            count++;
            try {
                socket.setSoTimeout(0);
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }
        return count;
    }

    int getAcknowledgementNo(String ack) {
        String str = deStuff(ack);
        String ackno = str.substring(16,24);
        int n = Integer.parseInt(ackno,2);
        return n;
    }

    void discardFile(){
        String fileName = null;
        try {
            fileName = inputStream.readObject().toString();
            int fileSize = Integer.parseInt(inputStream.readObject().toString());
            int chunkSize = Integer.parseInt(inputStream.readObject().toString());
            int noChunks;
            if(fileSize%chunkSize==0){
                noChunks=fileSize/chunkSize;
            }
            else {
                noChunks=fileSize/chunkSize+1;
            }
            for (int k = 0; k <  noChunks; k++) {
                inputStream.readObject(); //just receiving and discarding
            }


            } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
    String makeAck(int i) {
        String bitArray="011111100000000000000000";
        byte seqByte=(byte)i;
        String ackNo = String.format("%8s", Integer.toBinaryString(seqByte & 0xFF)).replace(' ', '0');
        int countOne=0;
        for (int j=0; j<ackNo.length();j++){
            if(ackNo.charAt(j)=='0'){
                bitArray+="0";
                countOne=0;
            }
            else {
                bitArray+="1";
                countOne++;
                if(countOne==5){
                    bitArray+="0";
                    countOne=0;
                }
            }
        }
        bitArray+="01111110";
        return bitArray;
    }

    int getSeqNo(String chunk) {
        String s = deStuff(chunk);
        String seq = s.substring(8,16);
        int seqNo = Integer.parseInt(seq,2);
        return seqNo;
    }
    void discardChunks(int k, int m, int tobeReceived){
        boolean flag =false;
        try {
            for (int i = m+1; i<tobeReceived ; i++) {
                inputStream.readObject(); //just receiving and discarding
                flag=true;
            }
            if(flag) {
                System.out.println("Discarded "+ (8*k+m+2) +" to "+ (8*k+tobeReceived));
            }
            //retransmit(k,tobeReceived);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
    int retransmit(int k, int m, int tobeReceived) {
        int count=0,error;
        Random random = new Random();
        System.out.println("Retransmitting frame no. "+(8*k+m+1) +" to "+(8*k+tobeReceived));
        try {
            error=random.nextInt(7)+1+m;    //random error frame no

            for (int i=m ; i < tobeReceived; i++) {
                //receive 8 frames
                String chunk= inputStream.readObject().toString();

                //create checksum error to loss frame
                if(i==error) {
                    chunk = createChecksumError(chunk);
                }
                //destuffing
                String deStuffed = deStuff(chunk);
                System.out.println("Destuffed: "+deStuffed);

                //extract payload
                System.out.println("len: "+deStuffed.length());
                String payload = deStuffed.substring(24, deStuffed.length() - 8);
                System.out.println("Payload: " + payload);
                //extract checksum
                String checksum = deStuffed.substring(deStuffed.length() - 8);

                int seqNo = getSeqNo(chunk);

                if(!hasChecksumError(payload, checksum)){
                    writeFile(fos,payload);
                    count++;
                }
                else {
                    System.out.println("Frame No. "+(8*k+m+1)+ " has been lost.");
                    discardChunks(k,i,tobeReceived);
                    break;
                }

            }

            for (int p = m; p < count+m; p++) {
                //send acknowledgement
                String  ack =makeAck(8*k+p+1);
                outputStream.writeObject(ack);
                System.out.println("Acknowledgement No. "+(8*k+p+1)+ " sent");

            }
        }catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return count;
    }


    String receiveFile(String fileName,int fileSize){
        byte[] buff = new byte[1024];
        try {
            File newFile = new File(fileName);
            fos = new FileOutputStream(newFile, false);
            int readByte;
            int noChunks, loop;
            noChunks = Integer.parseInt(inputStream.readObject().toString());
            if (noChunks % 8 == 0) {
                loop = noChunks / 8;
            } else {
                loop = noChunks / 8 + 1;
            }
            //send receiverserver number of loops
            outputStream.writeObject(loop);
            int tobeSent=8,error,totalSent=0;
            Random random = new Random();

            for (int k = 0; k < loop; k++) {
                tobeSent= Integer.parseInt(inputStream.readObject().toString());
                outputStream.writeObject(tobeSent);     //sendd the number of tobesend to receiverServer
                error = random.nextInt(100)%tobeSent +1;
                System.out.println("Frame Lost: "+(error+1));

                int count=0;
                //receiving 8 frames
                for (int m = 0; m < tobeSent; m++) {
                    //receive 8 frames
                    String chunk= inputStream.readObject().toString();

                    //create artificial error
                    if(m==error){
                        chunk= createChecksumError(chunk);
                    }
                    //destuffing
                    String deStuffed = deStuff(chunk);
                    System.out.println("Destuffed: "+deStuffed);

                    //extract payload
                    System.out.println("len: "+deStuffed.length());
                    String payload = deStuffed.substring(24, deStuffed.length() - 8);
                    System.out.println("Payload: " + payload);
                    //extract checksum
                    String checksum = deStuffed.substring(deStuffed.length() - 8);

                    int seqNo = getSeqNo(chunk);
                    /*if(seqNo==8*k+m+1 && m!=error){
                        count++;
                    }*/
                    if(!hasChecksumError(payload, checksum)){
                        System.out.println("Checksum matched");
                        writeFile(fos,payload);
                        count++;
                    }
                    else {
                        System.out.println("Checksum unmatched");
                        System.out.println("Frame No. "+(8*k+m+1)+ " has been lost.");
                        discardChunks(k,m,tobeSent);
                        break;
                    }

                }


                int p;
                for (int m = 0; m < count; m++) {
                    //send acknowledgement
                    String  ack =makeAck(8*k+m+1);
                    outputStream.writeObject(ack);
                    System.out.println("Acknowledgement No. "+(8*k+m+1)+ " sent");
                }

                //retransmission part
                while (count < tobeSent) {
                    int t = retransmit(k,count,tobeSent);
                    count+=t;
                }

                totalSent+=count;
            }

            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return fileName;

    }
    void writeFile(FileOutputStream fos, String payload){
        int buffSize = payload.length() / 8, bufferIndex = 0;
        byte[] array = new byte[buffSize];
        for (int b = 0; b < payload.length(); b += 8) {
            String eights = payload.substring(b, b + 8);
            int a = Integer.parseInt(eights, 2);
            array[bufferIndex] = (byte) a;
            bufferIndex++;
        }


        //write to file
        try {
            fos.write(array);
        } catch (IOException e) {
            System.out.println("File write disturbed");
            e.printStackTrace();

        }
    }

    boolean hasChecksumError(String payload, String checksum) {
        int countOne= calculateChecksum(payload);
        int checkSumInt = Integer.parseInt(checksum,2);

        int parity;
        if(countOne%2==1){
            parity=1;
        }
        else {
            parity=0;
        }
        System.out.println("count one: "+countOne+ " checksum: "+checkSumInt);
        if(checkSumInt==parity){
            return false;
        }
        return true;

    }

    int calculateChecksum(String payload){
        int countOne=0;
        for(int i=0; i< payload.length(); i++){
            if(payload.charAt(i)=='1'){
                countOne++;
            }
        }
        return countOne;
    }


    void deleteFile(String fileID) throws IOException {
        //delete the file
        File file = new File(fileID);
        Path path = FileSystems.getDefault().getPath(fileID);
        boolean b = Files.deleteIfExists(path);
        if(b)
            System.out.println("deleted successfully");
        else
            System.out.println("couldnot delete");
    }

    String  buildFrame(byte [] buff,int readByte, int n){
        //ServerSide.CreateServer.frames.set(fileID, new ArrayList<byte[]>(maxSize));//move it to calling place
        //this function will take a byte array as input and convert this to a frame
        String payload= new String("");
        for (int i=0; i<readByte; i++) {
            String s1 = String.format("%8s", Integer.toBinaryString(buff[i] & 0xFF)).replace(' ', '0');
            //System.out.println(s1);
            payload+=s1;

        }
        System.out.println(payload);
        String bitArray="";

        bitArray+= "00000001";                              //sending data,0x01 for data, 0x00 for ack
        byte b = (byte)n;
        String s2 = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
        bitArray+=s2;
        bitArray+=s2;                              //sending data, ack no. will be same as seqno

        //adding payload
        bitArray+=payload;

        //adding checksum
        int checksum;
        checksum = calculateChecksum(payload);
        int parity;
        if(checksum%2==1){
            parity=1;
        }
        else {
            parity=0;
        }
        byte checkByte = (byte)parity;
        String checkStr = String.format("%8s", Integer.toBinaryString(checkByte & 0xFF)).replace(' ', '0');
        bitArray+=checkStr;

        //bitstuffing
        String framedArray =bitStuff(bitArray);
        System.out.println("Framed BitsArray: "+framedArray);
        return framedArray;


    }

    String bitStuff(String bitArray){
        int countOne=0;
        String framedArray="01111110";
        for (int i=0; i<bitArray.length();i++){
            if(bitArray.charAt(i)=='0'){
                framedArray+="0";
                countOne=0;
            }
            else {
                framedArray+="1";
                countOne++;
                if(countOne==5){
                    framedArray+="0";
                    countOne=0;
                }
            }
        }
        //adding tail
        framedArray+="01111110";
        return framedArray;
    }
    String deStuff(String bitString) {
        String deStuffed="";
        int countOne=0;
        for(int i=8; i<bitString.length()-8; i++){
            if(bitString.charAt(i)=='0'){

                deStuffed+="0";
                countOne=0;
            }
            else {
                deStuffed+="1";
                countOne++;
                if(countOne==5){
                    i++ ;   //ignores the next bit
                    countOne=0;
                }
            }

        }
        //System.out.println("Destuffed: "+deStuffed);
        return deStuffed;
    }

    String createChecksumError(String str){
        String deStuffed = deStuff(str);
        String checkstr = deStuffed.substring(deStuffed.length() - 8);
        int checksum = Integer.parseInt(checkstr,2);
        if(checksum==1){
            checksum=0;
        }
        else {
            checksum=1;
        }
        byte b= (byte)checksum;
        checkstr = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
        String bitsArray = deStuffed.substring(0,deStuffed.length()-8);
        bitsArray+=checkstr;

        return bitStuff(bitsArray);
    }

}
//1405019