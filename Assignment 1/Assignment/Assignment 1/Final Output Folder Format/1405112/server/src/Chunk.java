import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.BitSet;

public class Chunk implements Serializable
{
    int size;
    byte[] data;
    String FileId;
    BitSet bitSet;
    int sn=0;
    byte cks;
    byte ackn;
    BitSet frame;
    //String finalString;
    byte[] finalData;

    public Chunk(byte acknowledge,int sequenceNo,int size,String FileId)
    {
        this.ackn=acknowledge;
        this.size=size;
        this.sn=sequenceNo;
        data=new byte[size];
        this.FileId=FileId;
    }
    String toBitArray()
    {
        String bits=""+ackn;
        bits=bits+sn;
        //System.out.println("initially");
        bitSet=BitSet.valueOf(data);
        //System.out.println(bitSet.toString());
        for (int i = 0; i < bitSet.length() ; i++)
        {
            if(bitSet.get(i)==true)
                bits=bits+'1';
            else
                bits=bits+'0';

        }
        bits=bits+tobits(cks);
        return bits;
    }
    String stuffBit(String s)
    {
        char ch[]=s.toCharArray();
        int count=0;
        String stuffed="01111110";
        /*
        for (char c:ch)
        {
            //System.out.println(c);

            if(c=='1' && count<5)
            {
                stuffed=stuffed+c;
                count++;
                System.out.println("no stuffing");
            }
            else if(c=='0')
            {
                count=0;
                stuffed=stuffed+c;
                System.out.println("no stuffing");
            }
            else
            {
                stuffed=stuffed+"01";
                //System.out.println(count);
                System.out.println("Yeaah Stuffed");
                count=0;
            }


        }
        */
        s=s.replace("11111","111110");
        stuffed=stuffed+s+"01111110";
        return stuffed;

    }
    String tobits(byte b)
    {
        String s="";
        int mask= 0b10000000;
        for (int i = 0; i <8 ; i++)
        {
            int result= b & mask;
            if(result==0)
            {
                s=s+'0';
            }
            else
            {
                s=s+"1";
            }
            mask=mask >>> 1;
        }
        return s;
    }
    String tobits(int b)
    {
        String s="";
        int mask= 0b10000000000000000000000000000000;
        for (int i = 0; i <32 ; i++)
        {
            int result= b & mask;
            if(result==0)
            {
                s=s+'0';
            }
            else
            {
                s=s+"1";
            }
            mask=mask >>> 1;
        }
        return s;
    }
    String deStuff(String s)
    {
        String original="";
        String temp=s.replace("01111110","");
        original=temp.replace("111110","11111");
        //System.out.println(s);
        //System.out.println(s.getBytes());
        //char ch[]=s.toCharArray();
        return  original;
    }
    byte checksum()
    {
        int x=0;
        for (int i = 0; i < data.length ; i++)
        {
            short s=0;
            int y=(byte)(data[i]);
            x+=(y&255);
            x=(x&255)+(x>>8);

            //System.out.println(Integer.toBinaryString(x));
            //short s1= (short) data[i];
            //short s2= (short) data[i+1];
            //System.out.println("s1: "+s1+" s2: "+s2);
            //s= (short) ((s1<<8)|s2);

            //System.out.println("s: "+s);

            //System.out.println(Integer.toBinaryString(Short.toUnsignedInt(s)));

            //x=x+Short.toUnsignedInt(s);
            //System.out.println(Integer.toBinaryString(x));
        }
        //System.out.println("test");
        //System.out.println(Integer.toBinaryString(x));
        //int mask=0b1111111100000000;
        byte sum=(byte)(x);

        //System.out.println(x&mask);
        //int f=x%65536;

        //short y=(short)x;
        //System.out.println(y);
        //System.out.println(Integer.toBinaryString(sum));
        return sum;
    }

    BitSet toBitset(String s)
    {
        BitSet bitSet=new BitSet();
        for (int i = 0; i < s.length() ; i++)
        {
            if(s.charAt(i)=='1')
            {
                bitSet.set(i,true);
            }
            else
            {
                bitSet.set(i, false);
            }
        }
        return bitSet;
    }
    String toStringFromBit(BitSet b)
    {
        String s="";
        for (int i = 0; i <=b.length() ; i++)
        {
            if(b.get(i))
            {
                s=s+"1";
            }
            else
                s=s+"0";
        }
        return s;

    }
    boolean hasChecksumError (byte b)
    {
        if(b==cks)
            return false;
        else
            return true;
    }
}
