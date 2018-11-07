import java.io.*;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.Scanner;


public class Main {

    public static void main(String[] args) {

        Chunk frame;
        BitSet bitSet;
        File file=new File("/home/jawad/Desktop/download.png");
        frame=new Chunk((byte)1,1,(int)file.length(),"Testing File");
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            fileInputStream.read(frame.data);
            frame.cks=frame.checksum();
            //frame.finalString=frame.toBitArray();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        System.out.println("initial data");
        bitSet=BitSet.valueOf(frame.data);
        System.out.println(bitSet.toString());

        //System.out.println(frame.data);
        String StuffIT=frame.toBitArray();
        System.out.println(StuffIT);

        //frame.checksum();

        String stuffed=frame.stuffBit(StuffIT);

        System.out.println("Stuffed");
        System.out.println(stuffed);

        String origin=frame.deStuff(stuffed);
        System.out.println("DeStuffed");
        System.out.println(origin);


        String acnbit=origin.substring(0,8);
        System.out.println(acnbit);

        origin=origin.substring(8);
        System.out.println("Without acknowledge bit");
        System.out.println(origin);

        String seqBit=origin.substring(0,32);
        System.out.println(seqBit);

        origin=origin.substring(32);
        System.out.println("Without sequence no");
        System.out.println(origin);

        String chk=origin.substring(origin.length()-8,origin.length());
        System.out.println(chk);

        origin=origin.substring(0,origin.length()-8);
        System.out.println("final data");
        System.out.println(origin);

        bitSet=new BitSet(origin.length());
        for (int i = 0; i < origin.length() ; i++)
        {
            if (origin.charAt(i)=='1')
            {
                bitSet.set(i,true);
            }
            else
                bitSet.set(i,false);

        }

        System.out.println("bitset");
        System.out.println(bitSet.toString());

        byte[] bytes=bitSet.toByteArray();
        /*
        System.out.println(bytes);
        for (int i = 0; i <bytes.length ; i++) {
            System.out.println(bytes[i]);
        }*/
        //short a= Short.parseShort(origin,2);
        //ByteBuffer byteBuffer=ByteBuffer.allocate(2).putShort(a);
        //byte[] ara=new BigInteger(origin,2).toByteArray();

        //System.out.println(ara);
        /*
        String s;
        System.out.println("Enter name");
        Scanner scanner=new Scanner(System.in);
        s=scanner.nextLine();
        frame=new Chunk(s.length(),s);
        frame.data=s.getBytes();
        String bits=frame.toBitArray();
        System.out.println(bits);
        String stuffed=frame.stuffBit(bits);
        System.out.println(stuffed);
        */
        try {
            FileOutputStream fileOutputStream=new FileOutputStream("/home/jawad/Desktop/d.png");
            fileOutputStream.write(bytes);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
