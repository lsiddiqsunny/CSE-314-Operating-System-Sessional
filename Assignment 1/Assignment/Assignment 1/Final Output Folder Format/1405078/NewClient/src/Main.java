import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import java.io.IOException;
import java.io.InputStreamReader;

import java.io.Serializable;
import java.net.Socket;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;







public class Main {
    private  static int chkSum0;
    private  static int frmsz;
    private  static int window=7;

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





    public static byte[] stuff(byte[] buffer,byte[] frame)
    {   int oneCntr=0,extra=0;
        int seq=frame[2] & 0xff;
        int j=31;
        byte[] tmp_buf=new byte[buffer.length+1];
        System.arraycopy(buffer,0,tmp_buf,0,buffer.length);
        tmp_buf[buffer.length]= (byte) calcChkSum(buffer);

        System.out.println("Before Stuffing Frame of sequence number:  "+seq);
        for (byte b : tmp_buf ) {
            for ( int mask = 0x80; mask != 0x00; mask >>= 1 ) {
                boolean value = ( b & mask ) != 0;
                if(value){

                    System.out.print(1);
                    j++;
                    frame[j/8] |=(byte) 1<<(7-(j%8));

                    oneCntr++;
                    if(oneCntr==5){
                        j++;
                        extra++;
                        oneCntr=0;
                    }
                }
                else {
                    oneCntr=0;
                    j++;
                    System.out.print(0);

                }
            }
            System.out.print(" ");
        }
        System.out.println();
        int bitCntr=(int)Math.ceil(extra/8)+tmp_buf.length+5;

        byte[] output=new byte[bitCntr+1];
        System.arraycopy(frame,0,output,0,bitCntr);
        output[bitCntr]=(byte)0b01111110;
        seq=output[2] & 0xff;
        System.out.println("After Stuffing Frame of sequence number: "+seq);

        for(byte b : output){
            for ( int mask = 0x80; mask != 0x00; mask >>= 1 ) {
                boolean value = (b & mask) != 0;
                if(value) System.out.print(1);
                else System.out.print(0);
            }
            System.out.print(" ");

        }
        return output;

    }



    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        Socket client = new Socket("localhost", 7878);
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));

        DataOutputStream outToServer = new DataOutputStream(client.getOutputStream());
        DataInputStream inFromServer1 = new DataInputStream(client.getInputStream());
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(client.getInputStream()));
        File file,file1;

        FileInputStream fis=null;
        int lastSent=0,lastSeq,waitingforAck=0;
        int seq = -1;



        file = new File("C:\\Users\\Farzana Ahmad\\Desktop\\chk.txt");
        fis=new FileInputStream(file);
        outToServer.writeBytes(file.getName()+'\n');
        outToServer.writeLong(file.length());
        int chnkSize = 0;


        chnkSize=inFromServer1.readInt();
        System.out.println("chunkSize: "+chnkSize);
        // outToServer.writeInt(chnkSize);
        byte[] frame;

        byte[] buffer=new byte[chnkSize];
        long remaining=file.length();
        Vector<byte[]> sentFrame=new Vector<byte[]>();
        Vector<Integer>pktSz=new Vector<Integer>();
        int totalRead=0,read;
        outToServer.writeLong(file.length());
        System.out.println(file.length());
        while(totalRead!=file.length())
        {
            if(remaining<chnkSize){
                buffer=new byte[(int)remaining];
            }
            read=fis.read(buffer);

            seq++;
            int frmLen=buffer.length*3+6;
            int pcktLen=buffer.length;
            pktSz.add(pcktLen);
            // outToServer.writeInt(pcktLen);
            frame=new byte[frmLen];
            frame[0]=(byte)0b01111110;
            frame[frmLen-1]=(byte)0b01111110;
            frame[1]=(byte)(1);
            frame[2]=(byte)seq;
            frame[3]=(byte)(0);
            byte[] pathamu=stuff(buffer,frame);
            sentFrame.add(pathamu);
            totalRead+=read;
            remaining-=read;
            System.out.println();
            System.out.println("Total Read from file "+totalRead);
            System.out.println("################################------------------------------################################");




        }
        System.out.println("Out of the loop.");
        // outToServer.writeInt(sentFrame.size());



        /////////Sending Frame//////////



        lastSeq=sentFrame.size();
        window=lastSeq;
        int lastAccepted=-1;



        while(lastAccepted<lastSeq){

            lastSent=sendFile(client,lastAccepted+1,lastSeq,sentFrame,pktSz);
            if(lastSent==window){
                int getval=new timer().getInput(inFromServer1);
                if(getval==0)
                {
                    int getseq = 0;
                    while(new timer().getInput(inFromServer1)==0)
                    {
                        byte[]frm=new byte[5];// size is very very important
                        inFromServer1.read(frm);
                        getseq=frm[3] & 0xFF;//get ack. from server
                    }
                    if(getseq!=(lastSent-1) )
                    {
                        //resend data from seq No (getseq+1 to seq)
                        System.out.println("Ack no "+getseq);
                        lastAccepted=getseq;
                        if(lastAccepted==(lastSeq-1)){break;}
                        // lastSent=sendFile(client,getseq+1,lastSeq,sentFrame,pktSz);
                        lastSent=getseq+1;
                    }
                    else {
                        System.out.println("Ack no "+getseq);
                        lastAccepted=getseq;
                        if(lastAccepted==(lastSeq-1)){break;}


                    }



                }
                else
                {

                    System.out.println("Time Out");
                    //code to be implemented
                    //lastSent=sendFile(client,lastAccepted+1,lastSeq,sentFrame,pktSz);

                    //resend data
                }
            }
            System.out.println("################################------------------------------################################");

        }
        byte[] ending=new byte[5];
        ending[0]=(byte)0b01111110;
        ending[4]=(byte)0b01111110;
        ending[1]=(byte)0b00000010;
        ending[2]=(byte)0b00000000;
        ending[3]=(byte)0b00000000;

        while(true){

            System.out.println("Sending terminating dummy frame.");
            outToServer.writeInt(5);
            outToServer.write(ending);
            int getval=new timer().getInput(inFromServer1);
            if(getval==0)
            {
                int getseq = 0;
                while(new timer().getInput(inFromServer1)==0)
                {
                    byte[]frm=new byte[5];// size is very very important
                    inFromServer1.read(frm);
                    getseq=frm[1];//get ack. from server
                }
                if(getseq==2 )
                {
                    //resend data from seq No (getseq+1 to seq)
                    System.out.println("Finishing Acknowledgement from server.");
                    break;
                    //lastAccepted=getseq;
                    //lastSent=sendFile(client,getseq+1,lastSeq,sentFrame,pktSz);
                }



            }


        }


        //client.close();
        return;

    }



    public static int sendFile(Socket client, int lastSent,int lastSeq,Vector<byte[]>sentFrame,Vector<Integer> packet)
            throws IOException {
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));

        DataOutputStream outToServer = new DataOutputStream(client.getOutputStream());
        lastSeq=sentFrame.size();
        window=lastSeq;
        int flag;
     /*   if(lastSeq%3==0){
            rand=(lastSeq/3)+1;
        }
        else rand=lastSeq%3;*/
        Random rand = new Random();

        int r = rand.nextInt((lastSeq-1)-lastSent) + lastSent;

        System.out.println("Random seq: "+r);


        System.out.println("To introduce Frame Lost error press 'A' , to introduce bit corruption press 'S',Press 'Z' to send file without error");

        String str=inFromUser.readLine();


        while( lastSent<lastSeq ){
            byte[] buf=sentFrame.get(lastSent);
            int pkt=packet.get(lastSent);

            ////error Introduction/////
            if(lastSent==r){
                if(str.toUpperCase().equals("A")){

                    lastSent++;
                    continue;

                }
                else if(str.toUpperCase().equals("S"))
                {
                    byte[] tmp_buf=new byte[buf.length];
                    System.arraycopy(buf,0,tmp_buf,0,buf.length);

                    //  int err= (int) Math.ceil(pkt/2);
                    tmp_buf[5]=(byte)0b11111111;
                    outToServer.writeInt(tmp_buf.length);
                    outToServer.write(tmp_buf);
                    lastSent++;
                }
                else {
                    outToServer.writeInt(buf.length);
                    outToServer.write(buf);
                    lastSent++;
                }
            }

            else {
                outToServer.writeInt(buf.length);
                outToServer.write(buf);
                lastSent++;
            }



        }
        return lastSent;


    }
}




class timer
{
    int dum=0;
    TimerTask timertask = new TimerTask()
    {
        public void run()
        {
            dum=1;
        }
    };

    public int getInput(DataInputStream input) throws Exception
    {
        Timer timer = new Timer();
        timer.schedule( timertask, 4000 );
        while(dum==0&&input.available()==0){}
        timer.cancel();
        return dum;
    }


}
