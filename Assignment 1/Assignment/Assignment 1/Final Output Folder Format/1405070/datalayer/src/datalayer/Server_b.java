/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package datalayer;


import java.io.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Scanner;
import java.util.Random;

/**
 *
 * @
 */
public class Server_b {
    static HashMap<String,String> client=new HashMap<>();


    public static void main(String[] args) {
        try {
            Scanner scan = new Scanner(System.in);
            /*System.out.println("Input Max Size:");
            int mxsz =scan.nextInt();*/
            ServerSocket server=new ServerSocket(5010);
            while(true)
            {
                Socket socket=server.accept();
                ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                String id= (String) input.readObject();
                System.out.println(id);
                String j= client.get(id);
                //System.out.println(j);
                if(j==null){

                    client.put(id, socket.toString());
                    //        String s=client.get(id);
                    //        System.out.println(s+"done");
                    output.writeObject("Y");
                    serverThread svr=new serverThread(id,socket.getLocalAddress().toString(),input,output);
                    Thread thr=new Thread(svr);
                    thr.start();
                }


                else output.writeUTF("n");

            }
        } catch (Exception ex) {
            Logger.getLogger(Server_b.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}


class serverThread implements Runnable
{
    String id,ip;
    ObjectInputStream input;
    ObjectOutputStream output;
    int mxcap=1000000000;
    int ccap=0;

    Vector<Byte> buffer=new Vector<>();



    public serverThread(String i,String d,ObjectInputStream s,ObjectOutputStream o) {
        id=i;
        ip=d.substring(1);//d=/127.0.0.1 that's why d.substr(1)
        input=s;
        output=o;
    }



    @Override
    public void run() {

        try {
            //output.writeUTF("Y");
            String pth= (String) input.readObject();


            Random rand= new Random();
            int chsz=rand.nextInt(10)+5;


            String s = (String) input.readObject();
            String msg[]=s.split(" ",2);
            System.out.println("File Name: "+msg[0]);
            System.out.println("File Size: "+msg[1]);
            int size=Integer.parseInt(msg[1]);
            ccap=ccap+size;

            File mfile = new File("E:\\L 3 T 2\\Networking lab\\datalayer\\src\\datalayer\\a7.txt");

            FileOutputStream fos = new FileOutputStream(mfile);
            BufferedOutputStream bos = new BufferedOutputStream(fos);


            if(ccap<mxcap) {

                //client_data data=new client_data();
                //System.out.println("Chsz: "+chsz);
                output.writeObject(chsz);
                byte[] server = new byte[1000];


                int sum = 0;
                while (sum != size) {

                    int cntr = 1;
                    while (cntr <= 8) {
                        int value = input.read(server, 0, 100);
                        //de_stuff
                        byte[] frame = destuff(server);
                        System.out.println("After DeStuffing");
                        for (int k = 0; k < frame.length; k++) {
                            printbyte(frame[k]);
                        }
                        System.out.println(" ");

                        //protocol

                        System.out.println("value is " + value);
                        frame = get_payload(frame);
                        //System.out.println("After get payload");
                        value = frame.length;

                        bos.write(frame);

                        bos.flush();

                        //System.out.println("After flush");
                        sum += value;
                        System.out.println("sum is " + sum);

                        if (sum == size) break;
                        cntr++;

                    }


                    int cntw=0;

                    while(cntw<cntr){
                        byte[] ackn=create_ackframe((byte)1);
                        output.write(ackn);
                        output.flush();
                        System.out.println("Sent: "+cntw);
                        cntw++;
                    }

                    System.out.println("Acknowledged");

                }

                bos.flush();
                bos.close();
                fos.close();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
    public byte[] get_payload(byte[] array)
    {
        byte temp[]=new byte[array.length-4];
        for(int i=3;i<array.length-1;i++)
        {
            temp[i-3]=array[i];
        }
        return temp;
    }

    public byte[] create_ackframe(byte type)
    {

        int frame_size=7;
        byte[] temp=new byte[frame_size];
        temp[0]=0b01111110; //header
        temp[1]=0b00000010; //ack type frame indicator
        temp[2]=0b0000000; //seq no(not important)
        temp[3]=type; //ack no
        temp[4]=0b00000000; //payload(not important)
        temp[5]=0b00000000; //checksum(not important)
        temp[6]=0b01111110; //trailer



        return temp;
    }
    public byte[] destuff(byte[] array)
    {
        int n=array.length;
        BitSet bits=BitSet.valueOf(reverse(array));
        Vector<Boolean> stuffed=new Vector<>();
        int count=0;
        for(int i=8;i<bits.size();i++)
        {
            if(bits.get(i)){

                count++;
            }
            else
            {

                for(int j=0;j<count;j++)
                {
                    stuffed.add(Boolean.TRUE);
                }
                if(count<5){
                    stuffed.add(Boolean.FALSE);
                    count=0;
                }
                else if(count==6) {
                    stuffed.remove(stuffed.lastIndexOf(Boolean.FALSE));
                    break;
                }
                else count=0;
            }

        }
        byte[] toReturn = new byte[stuffed.size() / 8];
        for (int entry = 0; entry < toReturn.length; entry++) {

            for (int bit = 0; bit < 8; bit++) {

                if (stuffed.elementAt(entry * 8 + bit)) {
                    toReturn[entry] |= (128 >> bit);
                }
            }
        }
        return toReturn;
    }

        /*stuffed.add(Boolean.FALSE);
        for(int i=0;i<6;i++)
        {
            stuffed.add(Boolean.TRUE);
        }
        stuffed.add(Boolean.FALSE);
        int count=0;
        for(int i=0;i< n*8;i++)
        {
            if(bits.get(i))
            {
                count++;
                stuffed.add(Boolean.TRUE);
                if(count==5)
                {
                    stuffed.add(Boolean.FALSE);
                    count=0;
                }
            }
            else
            {
                stuffed.add(Boolean.FALSE);
                count=0;
            }
        }
        stuffed.add(Boolean.FALSE);
        for(int i=0;i<6;i++)
        {
            stuffed.add(Boolean.TRUE);
        }
        stuffed.add(Boolean.FALSE);

        byte[] toReturn = new byte[stuffed.size() / 8];
        for (int entry = 0; entry < toReturn.length; entry++) {

            for (int bit = 0; bit < 8; bit++) {

                if (stuffed.elementAt(entry * 8 + bit)) {
                toReturn[entry] |= (128 >> bit);
            }
        }
    }
    return toReturn;

    */

    public byte[] reverse(byte[] data) {
        byte[] bytes = data.clone();

        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) (Integer.reverse(bytes[i]) >>> 24);
        }

        return bytes;
    }
    public byte calculateCheckSum(byte[] array)
    {
        byte count=0;
        for(int j=0;j<array.length;j++){
            int mask=128;
            for(int i=0;i<8;i++){
                if((mask & array[j])!=0){
                    count++;
                }

                mask = mask >> 1;
            }
        }
        return count;
    }
    public void printbyte(byte one){
        int mask=128;
        for(int i=0;i<8;i++){
            if((mask & one)!=0){
                System.out.print("1");
            }
            else System.out.print("0");
            mask = mask >> 1;
        }
        System.out.print(" ");
    }

}


class client_data
{
    String id;
    String rid;
    Vector<byte[]>flch=new Vector<>();

}

