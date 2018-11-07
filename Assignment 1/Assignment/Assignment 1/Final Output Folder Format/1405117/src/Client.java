
import java.io.*;
import java.net.*;
import java.util.*;


public class Client  {

    private static Socket socket = null;
    public static int portNumber = 9998;
    public static String host = "localhost";
    public static int  extraBitInt;

    public static String CheckSum(byte[] bytes, int count)
    {
        String checkSumString="";
        byte checkBit=(byte) 0b00000000;

        int j=0;
        while(j<count)
        {
            checkBit = (byte) (checkBit^bytes[j]);
            j++;
        }

        checkSumString=String.format("%8s", Integer.toBinaryString(checkBit & 0xff));
        checkSumString=checkSumString.replaceAll(" ", "0");
        return checkSumString;
    }

    public static String ByteToString(byte[] bytes, int count)
    {
        String bitString="";
        for(int i=0; i<count; i++)
        {
            String temp=String.format("%8s", Integer.toBinaryString(bytes[i] & 0xff));
            temp=temp.replaceAll(" ","0");
            bitString += temp;
        }

        return bitString;
    }

    public static String StuffedString(byte [] bytes, int count)
    {
        String stuffString=ByteToString(bytes, count);

        String dataString=stuffString.replaceAll("0111111", "01111101");

        String checkSumString = CheckSum(bytes, count);
        System.out.println("checkSumString:"+checkSumString);
        dataString="01111110"+dataString+checkSumString+"01111110";
        int stringLenght = dataString.length()/8;
        int diff =dataString.length() - (stringLenght * 8);
        System.out.println("diff:"+diff);
        if(diff>0)
        {
            diff=8-diff;
            System.out.println("diff:"+diff);
            extraBitInt=diff;
            String extraBit="";
            for (int i=0; i<diff; i++)
            {
                extraBit+="0";
            }
            dataString=dataString+extraBit;
        }
        return dataString;
    }

    public static void main(String[] argv) throws Exception {

        socket=new Socket(host,portNumber);
        System.out.println("connection established");

        //File Sending part
        FileInputStream fileInputStream = null;
        BufferedInputStream bufferedInputStream = null;
        OutputStream outputStream= null;
        OutputStream outputStream1=null;
        DataOutputStream dataOutputStream=null;
        DataInputStream dataInputStream=null;

        try{
            dataInputStream=new DataInputStream(socket.getInputStream());
            String FILE_TO_SEND="C:/Users/sakib/IdeaProjects/Sakib/src/test.txt";
            File myFile = new File(FILE_TO_SEND);

            System.out.println(myFile.length());
            dataOutputStream=new DataOutputStream(socket.getOutputStream());
            dataOutputStream.writeInt((int)myFile.length());
            int chuncksize=dataInputStream.readInt();

            int noChunck = dataInputStream.readInt();
            System.out.println(noChunck);

            byte[] mybytearray = new byte[chuncksize];
            fileInputStream=new FileInputStream(myFile);
            bufferedInputStream=new BufferedInputStream(fileInputStream);
            outputStream = socket.getOutputStream();
            int count=0;

            while(true)
            {
                count = bufferedInputStream.read(mybytearray);
                if(count<=0) break;
                String stuffedString = StuffedString(mybytearray, count);
                System.out.println("size:"+extraBitInt);
                dataOutputStream.writeInt(extraBitInt);

                System.out.println("stuffedString:"+stuffedString);
                dataOutputStream.writeUTF(stuffedString);

                int ack=dataInputStream.readInt();

               while(ack!=1)
               {
                   dataOutputStream.writeInt(extraBitInt);
                   dataOutputStream.writeUTF(stuffedString);
                   System.out.println("Chunck retransmitted");
               }

            }
            while ((count = bufferedInputStream.read(mybytearray)) > 0) {

                String stuffedString = StuffedString(mybytearray, count);
                int chunckStringSize=(int) stuffedString.length();
                System.out.println("size:"+extraBitInt);
                dataOutputStream.writeInt(extraBitInt);
                System.out.println("stuffedString:"+stuffedString);
                dataOutputStream.writeUTF(stuffedString);
            }

            System.out.println("check");
        }catch (IOException e)
        {
            System.out.println(e);
        }
        socket.close();
    }
}
