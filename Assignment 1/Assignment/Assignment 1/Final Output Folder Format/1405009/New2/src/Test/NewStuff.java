package Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class NewStuff {
    public static void main(String[] args) throws IOException {
        NewStuff newStuff = new NewStuff();
        File file = new File("./test/5.jpg");
        FileInputStream fileInputStream = new FileInputStream(file);
        int fileLength = (int) file.length();
        System.out.println(fileLength);

        byte[] array = new byte[200];
        byte[] frame = new byte[204];
        byte[] stuff = new byte[230];
        byte[] destuff = new byte[204];

        /*int eachRun=100;
        int len=fileLength/eachRun;
        int offset=0;
        for(int i=0;i<eachRun;i++){
            fileInputStream.read(array,offset,len);
            offset+=len;
        }
        fileInputStream.read(array,offset,fileLength%eachRun);

        FileOutputStream fileOutputStream = new FileOutputStream(new File("./test/7.jpg"));
        offset=0;
        for(int i=0;i<eachRun;i++){
            fileOutputStream.write(array,offset,len);
            offset+=len;
        }
        fileOutputStream.write(array,offset,fileLength%eachRun);
        fileOutputStream.close();

        int x = newStuff.stuff(array,stuff,fileLength);
        int y = newStuff.deStuff(stuff,destuff);

        FileOutputStream fileOutputStream1 = new FileOutputStream(new File("./test/8.jpg"));
        offset=0;
        for(int i=0;i<eachRun;i++){
            fileOutputStream1.write(destuff,offset,len);
            offset+=len;
        }
        fileOutputStream1.write(destuff,offset,fileLength%eachRun);
        fileOutputStream1.close();



        for(int i=0;i<fileLength;i++)
        {
            newStuff.printBit(array[i]);
        }
        System.out.println(x);
        for(int i=0;i<x;i++)
        {
            //newStuff.printBit(stuff[i]);
        }
        System.out.println();
        System.out.println(y);
        for(int i=0;i<y;i++)
        {
            newStuff.printBit(destuff[i]);
        }


*/
        fileInputStream.read(array,0,200);

        int z = newStuff.makeFrame(array,frame,(byte) 1,(byte) 5,(byte) 0,200);
        //newStuff.makeCheckSumError(frame,200);
        int x = newStuff.stuff(frame,stuff,z);
        int y = newStuff.deStuff(stuff,destuff);
        for(int i=0;i<200;i++)
        {
            newStuff.printBit(array[i]);
        }
        System.out.println();
        for(int i=0;i<z;i++)
        {
            newStuff.printBit(frame[i]);
        }
        System.out.println();
        for(int i=0;i<x;i++)
        {
            newStuff.printBit(stuff[i]);
        }
        System.out.println();
        for(int i=0;i<y;i++)
        {
            newStuff.printBit(destuff[i]);
        }
        System.out.println();
        System.out.println(newStuff.hasCheckSumError(destuff,y));
    }

    int getBit(byte b,int pos)
    {
        return b>>pos & 1;
    }

    void printBit(byte x)
    {
        for(int i=7;i>=0;i--)
        {
            int bit = this.getBit(x,i);
            System.out.print(bit);
        }
        System.out.print(" ");
    }
    int countBit(byte b)
    {
        int count=0;
        for(int i=7;i>=0;i--)
        {
            int bit = this.getBit(b,i);
            count+=bit;
        }
        return count%2;
    }

    int stuff(byte[] source, byte[] dest,int len)
    {
        byte currByte = 126;//framing
        int bitCount = 0;
        int prevCount = 0;
        int currBit;
        int byteCount=0;
        boolean stuff = false;

        dest[0] = currByte;
        byteCount++;
        currByte=0;
        int stuffCount=0;

        for(int p=0;p<len;p++)
        {
            byte y = source[p];
            for(int i=7;i>=0;i--)
            {
                currBit = this.getBit(y,i);
                currByte = (byte) (currByte <<1);
                currByte = (byte) (currByte | currBit);
                bitCount++;


                if(currBit==0) {prevCount=0;}

                    else
                    {
                        if(prevCount<=3)
                            prevCount++;
                        else
                        {
                            prevCount=0;
                            stuffCount++;
                            stuff=true;
                        }
                    }

                    if(bitCount==8)
                    {
                        //System.out.println(byteCount/dCol+" "+byteCount%dCol);
                        dest[byteCount] = currByte;
                        byteCount++;
                        currByte=0;
                        if(stuff)
                            bitCount=1;
                        else bitCount=0;
                    }
                    else
                    {
                        if(stuff) {
                            currByte = (byte) (currByte << 1);
                            bitCount++;
                            if(bitCount==8)
                            {
                                dest[byteCount] = currByte;
                                byteCount++;
                                currByte=0;
                                bitCount=0;
                            }
                        }
                    }
                    stuff=false;
                }
            }

        if(bitCount!=0){
            byte delimByte = 126;
            currByte = (byte) ( (currByte<<(8-bitCount))|delimByte>>bitCount);
            dest[byteCount] = currByte;
            byteCount++;
            currByte = (byte) (delimByte<<(8-bitCount));
            dest[byteCount] = currByte;
            byteCount++;

        }
        else
        {
            currByte = 126;
            dest[byteCount] = currByte;
            byteCount++;
        }
        System.out.println(stuffCount);
        return byteCount;
    }

    int deStuff(byte[] source, byte[] dest)
    {
        int bitCount = 0;
        int prevCount = 0;
        int currBit;
        int byteCount=0;
        boolean stuffed = false;
        int isEnd = 0;
        boolean isStart = true;
        byte  currByte=0;

        for(byte y : source)
        {
                for(int i=7;i>=0;i--)
                {
                    currBit = this.getBit(y,i);
                    /*System.out.print("Got : "+currBit+" current byte :");
                    this.printBit(currByte);
                    System.out.println(" Stuffed: "+stuffed+" pc :"+prevCount);*/

                    if(stuffed){
                        stuffed=false;
                        if(currBit==0)
                            continue;
                        isEnd++;
                        if(isEnd==2)
                        {
                            break;
                        }
                    }
                    currByte = (byte) (currByte <<1);
                    currByte = (byte) (currByte | currBit);
                    bitCount++;

                    if(currBit==0) {prevCount=0;}

                    else
                    {
                        if(prevCount<=3) prevCount++;
                        else
                        {
                            prevCount=0;
                            stuffed=true;
                        }
                    }
                    if(isEnd==2)
                    {
                        break;
                    }

                    if(bitCount==8)
                    {
                        //System.out.println(byteCount/dCol+" "+byteCount%dCol);
                        if(currByte!=126 || !isStart) {
                            isStart=false;
                            dest[byteCount] = currByte;
                            byteCount++;
                        }
                        bitCount=0;
                        currByte=0;
                    }

                }
            }


        return byteCount;
    }

    boolean hasCheckSumError(byte[] source,int  len)
    {

        boolean hasError;
        int count=0;
        for(int i=0;i<len;i++)
        {
            count +=countBit(source[i]);
        }
        if(count%2==0)hasError=false;
        else hasError=true;
        return hasError;
    }

    int makeFrame(byte[] source,byte[] dest,byte kind,byte seq,byte ack,int len)
    {
        dest[0]=kind;
        dest[1]=seq;
        dest[2]=ack;

        for(int i=0;i<len;i++)
        {
            dest[3+i]=source[i];
        }

        int count=0;
        for(int i=0;i<len+3;i++)
        {
            count +=countBit(dest[i]);
            System.out.print(count+"n ");
        }
        count = count%2;
        if(count==0)
            dest[len+3]=0;
        else
            dest[len+3]=1;
        return len+4;
    }
    void makeCheckSumError(byte[] source,int  len)
    {

        byte b = source[5];
        int count = countBit(b);
        if(count ==0)
            source[5]=(byte)4;
        else
            source[5]=(byte)5;
    }



}
