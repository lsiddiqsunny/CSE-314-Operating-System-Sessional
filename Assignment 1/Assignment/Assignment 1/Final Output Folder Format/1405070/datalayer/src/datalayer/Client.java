/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package datalayer;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.BitSet;
import java.util.Vector;
/**
 *
 * @author DELL
 */

public class Client {

    public static void main(String[] args) {
        try {
            String sid;

            int flg=1;
            Scanner sc=new Scanner(System.in);

            //s=sc.nextLine();
            System.out.println("Input your id:");
            sid =sc.nextLine();

            Socket socket=new Socket("localhost",5010);
            ObjectOutputStream output=new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream input=new ObjectInputStream(socket.getInputStream());
            output.writeObject(sid);
            System.out.println("Done");

            String in = (String) input.readObject();
            System.out.println(in);
            if(in.equalsIgnoreCase("Y"))
            {
                System.out.println("Connected");
                //thread create kore korte hbe
                new Thread(new clientThread(input,output)).start();
                System.out.println("");
            }
            else System.out.println("Already Connected");
        } catch (Exception ex) {
            System.err.println("Sorry,Server not found");
        }

    }
}
class clientThread implements  Runnable
{
    ObjectInputStream input;
    ObjectOutputStream output;

    public clientThread(ObjectInputStream input,ObjectOutputStream output)
    {
        this.input=input;
        this.output=output;
    }


    @Override
    public void run() {
        Scanner scn=new Scanner(System.in);
        System.out.println("Input your file path:");
        String pth=scn.nextLine();
        try {
            output.writeObject(pth);
        } catch (IOException ex) {
            Logger.getLogger(clientThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        //System.out.println(pth);
        File f=new File("ak.txt");
        try
        {
            File mfile = new File(pth);
            FileInputStream fi = new FileInputStream(mfile);
            BufferedInputStream bis = new BufferedInputStream(fi);

            int size = fi.available();
            String s = f.getName() + " " + size;
            System.out.println("Size: "+size);
            output.writeObject(s);
            //System.out.println("Chunk is "+0);
            int chnk= (int) input.readObject();
            int cap=0;

            //System.out.println("Chunk is "+chnk);

            while(size>0){
                int cntw=1;
                while(cntw<=8) {
                    chnk = Math.min(size, chnk);
                    System.out.println("Rem: " + chnk);
                    byte[] buffer = new byte[chnk];
                    bis.read(buffer, 0, chnk);
                    byte[] frame = create_frame(buffer, (byte) 0, (byte) 0, (byte) 0);
                    System.out.println("Before Stuffing");
                    for (int k = 0; k < frame.length; k++) {
                        printbits(frame[k]);
                    }
                    System.out.println("");
                    frame = stuff(frame);
                    System.out.println("After Stuffing");
                    for (int k = 0; k < frame.length; k++) {
                        printbits(frame[k]);
                    }
                    output.write(frame);
                    output.flush();
                    //System.out.println(buffer);
                    size -= chnk;
                    System.out.println("size rem: " + size);


                    if(size<=0) break;
                    cntw++;
                }

                int cntr=0;

                while(cntr<cntw){
                    byte[] receive = new byte[12];
                    int val = input.read(receive, 0, 10);
                    System.out.println("Received: "+cntr);
                    cntr++;

                }
                System.out.println("Acked");
            }

            bis.close();
            fi.close();


        }catch(Exception e)
        {
            e.printStackTrace();
        }

    }
    public byte[] create_frame(byte[] f,byte type,byte seq_no, byte ack_no)
    {

        int frame_size=f.length+4;
        byte[] temp=new byte[frame_size];
        temp[0]=type;
        temp[1]=seq_no;
        temp[2]=ack_no;
        for(int i=3;i<frame_size-1;i++){
            temp[i]=f[i-3];
        }
        temp[frame_size-1]=calculateCheckSum(f);
        //BitSet b=BitSet.valueOf(temp);
        return temp;
    }
    public byte[] stuff(byte[] array)
    {
        int n=array.length;
        BitSet bits=BitSet.valueOf(reverse(array));
        Vector<Boolean> stuffed=new Vector<>();
        stuffed.add(Boolean.FALSE);
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


    }
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
    public void printbits(byte one){
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

