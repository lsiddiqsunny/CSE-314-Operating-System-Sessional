import java.io.*;
import java.net.*;
import java.util.Random;


public class Server {

    public static ServerSocket serverSocket=null;
    public static Socket socket=null;
    public  static final int socketPort=9998;

    public static void ConnectionFunction()
    {
        try
        {
            serverSocket=new ServerSocket(socketPort);
        }catch (IOException e)
        {
            System.out.println(e);
        }
    }

    public static byte[] DestuffString(String stuffedString, int extraBitInt)
    {
        System.out.println(stuffedString);
        int stringSize=(int) stuffedString.length();

        String payLoad=stuffedString.substring(8, stringSize-extraBitInt-16);
        System.out.println("payload:"+payLoad);
        System.out.println("paySize:"+(int) payLoad.length());

        String checkSumString = stuffedString.substring(stringSize-16, stringSize-8);
        System.out.println("checkSumString:"+checkSumString);

        payLoad=payLoad.replaceAll("01111101", "0111111");


        byte bytes[]=StringToByte(payLoad);
        return bytes;
    }

    public static byte[] StringToByte(String string)
    {
        int stringSize=(int)string.length();
        System.out.println(stringSize);
        int byteNum=stringSize/8;
        if(stringSize>byteNum*8)
        {
            byteNum++;
        }
        byte [] bytes=new byte[byteNum];
        int byteCounter=0;
        for(int i=0; i<stringSize; i=i+8)
        {
            int factor=1;
            int sumInt=0;
            for(int j=i+7; j>=i; j--)
            {
                int charInt=string.charAt(j)-48;
                charInt=charInt*factor;
                sumInt+=charInt;
                factor*=2;
                //System.out.println(charInt);
            }
            bytes[byteCounter]=(byte) sumInt;
            //System.out.println("bytes:"+byteCounter+":"+bytes[byteCounter]);
            byteCounter++;
        }

        return bytes;
    }

    public static String CheckSumFunc(String stuffedString, int extraBitInt)
    {
        int size=stuffedString.length();
        String payLoad=stuffedString.substring(8, size-extraBitInt-16);
        payLoad=payLoad.replaceAll("01111101", "0111111");

        byte bytes[]=StringToByte(payLoad);
        int byteNum=payLoad.length()/8;
        String checkSumString="";
        byte checkBit=(byte) 0b00000000 ;

        int j=0;
        while(j<byteNum)
        {
            checkBit = (byte) (checkBit^bytes[j]);
            j++;
        }

        checkSumString=String.format("%8s", Integer.toBinaryString(checkBit & 0xff));
        checkSumString=checkSumString.replaceAll(" ", "0");
        return checkSumString;
    }


    public static void main(String[] args) throws IOException {

        DataInputStream dataInputStream=null;
        DataOutputStream dataOutputStream=null;

        ConnectionFunction();
        System.out.println("Server running");

        while(true)
        {
            socket=serverSocket.accept();
            FileOutputStream fileOutputStream = null;
            BufferedOutputStream bufferedOutputStream = null;

            try
            {
                dataOutputStream=new DataOutputStream(socket.getOutputStream());
                System.out.println("Server Receiving");
                dataInputStream=new DataInputStream(socket.getInputStream());
                int fileSize=dataInputStream.readInt();//file size
                System.out.println("filesize"+fileSize);
                int minNum=fileSize;
                minNum=(minNum/255)+1; //chunck
                Random rand=new Random();

                int randomNum = rand.nextInt((1000) + 1) + minNum;

                dataOutputStream.writeInt(randomNum);
                System.out.println(randomNum);//randpmNUM==chuncksize
                int noChunck=fileSize/randomNum;
                if(fileSize> noChunck* randomNum)
                {
                    noChunck++;
                }

                dataOutputStream.writeInt(noChunck);
                System.out.println("chunck:"+noChunck);

                byte[] mybytearray = new byte[randomNum];

                InputStream inputStream = socket.getInputStream();
                fileOutputStream=new FileOutputStream("C:/Users/sakib/IdeaProjects/Sakib/src/test_copy.txt");
                bufferedOutputStream=new BufferedOutputStream(fileOutputStream);
                int count=0;
                for(int i=0; i<noChunck; i++)
                {
                    try {
                        int  extraBitInt=dataInputStream.readInt();
                        System.out.println("size:"+extraBitInt);
                        String stuffedString = dataInputStream.readUTF();
                        int size=stuffedString.length();
                        System.out.println("stuffed:"+stuffedString);

                        String checkSumServer= CheckSumFunc(stuffedString, extraBitInt);
                        String checkSumReceiver= stuffedString.substring(size-extraBitInt-16,size-extraBitInt-8);

                        if(checkSumReceiver.equals(checkSumReceiver))
                        {
                            byte[] bytes=DestuffString(stuffedString,extraBitInt);
                            fileOutputStream.write(bytes);
                            dataOutputStream.writeInt(1);
                            System.out.println("chunck received");
                        }
                        else
                        {
                            dataOutputStream.writeInt(0);
                            System.out.println("chunck error! retransmission");
                        }


                    }
                    catch(Exception e) {
                        System.out.println(e);
                    }
                }

                System.out.println("file received");


               // fileOutputStream.write(mybytearray);
                bufferedOutputStream.flush();
                fileOutputStream.flush();

            }catch (IOException e)
            {
                System.out.println(e);
            }
        }


    }
}


 /*while ((count=inputStream.read(mybytearray))>=0) {
                    try {
                        //System.out.println("bye");
                       // fileOutputStream.write(mybytearray,0,count);
                        String stuffedString = dataInputStream.readUTF();
                        System.out.println("stuffed:"+stuffedString);

                    }
                    catch(Exception e) {
                        System.out.println(e);
                    }
                }*/