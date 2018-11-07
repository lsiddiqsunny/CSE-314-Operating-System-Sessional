

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;

/**
 *
 * @author user
 */
public class Main {

    private static int chkSum;
    private  static  int seq=0;
    private static  int ack=0;
    private static  int tmpChkSum;

    public static int calcChkSum(byte[] buffer){
        int chkSum=0;
        for (byte b : buffer ) {
            for ( int mask = 0x80; mask != 0x00; mask >>= 1 ) {
                boolean value = ( b & mask ) != 0;
                if(value) chkSum++;
            }
        }
        System.out.println("chk SUm: "+chkSum);
        return chkSum;
    }
    public static byte[] destuff(byte[] frame){
        byte[] buffer=new byte[80];
        int j,bitcntr=0,isStuff=0,oneCntr=0,extra=0;
        for (int i=4;i<frame.length;i++ ) {
            for (int mask = 0x80; mask != 0x00; mask >>= 1) {
                if (isStuff == 1) {
                    isStuff = -1;
                    continue;
                }
                boolean value = (frame[i] & mask) != 0;
                if (value) {
                    oneCntr++;
                    buffer[bitcntr / 8] |= (byte) (1 << (7 - (bitcntr % 8)));
                    if (oneCntr == 5) {
                        isStuff = 1;
                        oneCntr = 0;
                        extra++;
                    }
                } else {
                    oneCntr = 0;
                }
                bitcntr++;
            }
        }
        int seq=frame[2] & 0xff;
        System.out.println("After destuffing Frame of sequence number: "+seq);
        for(int i = 0; i<((bitcntr)/8)-1; i++) {
            for (int mask = 0x80; mask != 0x00; mask >>= 1) {
                boolean value = (buffer[i] & mask) != 0;
                if (value) System.out.print(1);
                else System.out.print(0);
            }
            System.out.print(" ");


        }

        System.out.println();

        int arraySize=(bitcntr/8)-1;
        System.out.println("arraySize: "+arraySize);
        tmpChkSum=buffer[arraySize-1] & 0xFF;
        System.out.println("destuffing theke pawa checkSum "+tmpChkSum);
        byte[] output=new byte[arraySize-1];
        System.arraycopy(buffer,0,output,0,arraySize-1);
        chkSum=calcChkSum(output);

        return output;


    }
    public static boolean hasChecksumError(int calCulatedSum,int bitSum){
        if(calCulatedSum==bitSum){
            return false;
        }
        else return true;


    }


    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        ServerSocket welcomeSocket=new ServerSocket(7878);
        Socket ClientSocket=welcomeSocket.accept();
        File file1 = new File("C:\\Users\\Farzana Ahmad\\Desktop\\out1.txt");

        long totalRead=0,read,remaining;
        DataOutputStream outToServer=new DataOutputStream(ClientSocket.getOutputStream());
        BufferedReader inFromClient=new BufferedReader(new InputStreamReader(ClientSocket.getInputStream()));
        DataInputStream toServer=new DataInputStream(ClientSocket.getInputStream());
        FileOutputStream fos=null;
        fos=new FileOutputStream(file1);
        long fileSize;


        System.out.println("File Name :"+inFromClient.readLine());
        fileSize=toServer.readLong();
        System.out.println("File Size : "+fileSize);




        int frmSize,lastSeenSeq,waitingforAck=0,totalFrame;
        int chnkSize;
        int len= (int)fileSize;
        // if(len%8!=7){ chnkSize=(len%8)+1;}
        //else chnkSize=7;
        Random rand = new Random();

        chnkSize = rand.nextInt(10) + 20;
        System.out.println("chunkSize: "+chnkSize);
        outToServer.writeInt(chnkSize);


        System.out.println("Chunk Size : "+chnkSize);
        int pcktLen;
        long fSize=toServer.readLong();
        System.out.println("File Size : "+fSize);

        remaining=fSize;
        //  totalFrame=toServer.readInt();
        // System.out.println("Total frame number :"+totalFrame);

        //////// chking frame and acknowledgement/////

        int lastAccepted=-1;
        while (true){
            System.out.println("while loop theke bolsi.");
            //int pktlen=toServer.readInt();
            frmSize=toServer.readInt();
            System.out.println("frame size: "+frmSize);
            byte[] receivedFrame=new byte[frmSize];
            read=toServer.read(receivedFrame);
            int tmpSeq=receivedFrame[2] & 0xff;
            System.out.println("What is received from Sender.");
            for (int m=0;m<(receivedFrame.length);m++){
                for ( int mask = 0x80; mask != 0x00; mask >>= 1 ) {
                    boolean value = (receivedFrame[m] & mask ) != 0;
                    if(value){
                        System.out.print(1);
                        chkSum++;
                    }
                    else System.out.print(0);
                }
                System.out.print(" ");

            }

            System.out.println();


            if(receivedFrame[1]==2){
                System.out.println("Got finishing acknowledgement from client");
                outToServer.write(receivedFrame);
                break;

            }

            else if(tmpSeq>lastAccepted) {
                byte[] buffer = destuff(receivedFrame);
                boolean errChkSum=hasChecksumError(chkSum,tmpChkSum);
                System.out.println("Waiting for frame sequence number : "+waitingforAck);
                if (receivedFrame[1] == 1 && (tmpSeq == waitingforAck) && (errChkSum==false)) {
                    byte[] acknowdegement = new byte[5];
                    acknowdegement[0] = (byte) 0b01111110;
                    acknowdegement[1] = (byte) 0;
                    acknowdegement[2] = receivedFrame[2];
                    acknowdegement[3] = receivedFrame[2];
                    acknowdegement[4] = (byte) 0b01111110;

                    fos.write(buffer);
                    outToServer.write(acknowdegement);
                    lastAccepted=waitingforAck;
                    System.out.println("Last accepted: "+lastAccepted);
                    waitingforAck++;

                    //remaining -= read;


                } else {
                    if(tmpSeq!=waitingforAck){
                        System.out.println("Frame Lost error frame sequence no " + waitingforAck);
                    }
                    else if(errChkSum==true){
                        System.out.println("Bit Corruption error frame sequence no " + waitingforAck);
                    }
                    else
                    {
                        System.out.println("Both error frame sequence no " + waitingforAck);
                    }

                }


            }
            else{
                System.out.println("Purano frame abr pathaise sender whose sequence number is "+tmpSeq);

            }
            System.out.println("################################------------------------------################################");

        }
        System.out.println("The End.");

        return;








    }

}
