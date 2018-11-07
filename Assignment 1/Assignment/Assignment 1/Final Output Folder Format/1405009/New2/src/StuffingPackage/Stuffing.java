package StuffingPackage;

/**
 * Created by Rupak on 10/28/2017.
 */
public class Stuffing {

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

    public int stuff(byte[] source, byte[] dest,int len)
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
        //System.out.println(stuffCount);
        return byteCount;
    }

    public int deStuff(byte[] source, byte[] dest)
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



    public boolean hasCheckSumError(byte[] source,int  len)
    {

        boolean hasError;
        int[] count = new int[8];
        for(int i=0;i<len;i++)
        {
            for(int j=7;j<0;j++)
            {
                int x = getBit(source[i],j);
                count[j] += x;
            }
        }
        byte currbyte=0;
        for(int j=7;j<0;j++)
        {
            int y = count[j]%2;
            currbyte = (byte) (currbyte<<1 | y);
        }

        if(currbyte == source[len-1])
            hasError=false;
        else
            hasError=true;

        return hasError;
    }

    public void makeCheckSumError(byte[] source,int  len)
    {

        byte b = source[5];
        int count = countBit(b);
        if(count ==0)
            source[5]=(byte)4;
        else
            source[5]=(byte)5;
    }

    public byte checksum(byte[] source,int len)
    {
        int[] count = new int[8];
        for(int i=0;i<len;i++)
        {
            for(int j=7;j<0;j++)
            {
                int x = getBit(source[i],j);
                count[j] += x;
            }
        }
        byte currbyte=0;
        for(int j=7;j<0;j++)
        {
            int y = count[j]%2;
            currbyte = (byte) (currbyte<<1 | y);
        }
        return currbyte;
    }

    public int makeFrame(byte[] source,byte[] dest,byte kind,byte seq,byte ack,int len)
    {
        dest[0]=kind;
        dest[1]=seq;
        dest[2]=ack;


        for(int i=0;i<len;i++)
        {
            dest[3+i]=source[i];
        }
        byte curr = checksum(source,len);
        dest[len+3]=curr;
        return len+4;
    }

    public int printArray(byte[] b,int len)
    {
        int i=0;
        for(i=0;i<len;i++)
        {
            printBit(b[i]);
            System.out.print(" ");
        }
        System.out.println();
        return i;

    }

    public FrameInfo getFrame(byte[] source,byte[]dest,int len)
    {
        FrameInfo frameInfo= new FrameInfo();
        frameInfo.setKindOfFrame(source[0]);
        frameInfo.setSeqNo(source[1]);
        frameInfo.setAckNo(source[2]);
        for(int i=3;i<len-1;i++)
        {
            dest[i-3]=source[i];
        }
        frameInfo.setCheckSum(source[len-1]);
        frameInfo.setFrameSize(len-4);
        return frameInfo;
    }
}
