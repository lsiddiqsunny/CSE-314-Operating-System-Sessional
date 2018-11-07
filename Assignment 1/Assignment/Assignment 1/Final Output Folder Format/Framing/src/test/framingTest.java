package test;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Objects;

public class framingTest {

    public static void main(String agrs[]) throws IOException {

        File client = new File("clientLog.txt");
        File server = new File("serverLog.txt");
        BufferedReader brClient= null, brServer = null;
        try {
            brClient = new BufferedReader(new FileReader(client));
            brServer = new BufferedReader(new FileReader(server));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        int clientCurr = 0;
        int serverCurr = 0;
        int clientIdx = 3;
        int serverIdx = 3;

        int i = 1;
        int serverIncr = 4;
        int clientIncr = 3;

        String clientLine = null;
        String serverLine = null;

        while (true){
            while(clientCurr < clientIdx){
                clientLine = brClient.readLine();
                if(clientLine != null){
                    clientCurr++;
                }
                else break;
            }

            while(serverCurr < serverIdx){
                serverLine = brServer.readLine();
                if(serverLine != null){
                    serverCurr++;
                }
            }

            if(clientLine == null || serverLine == null) break;
            if(!Objects.equals(clientLine, serverLine)){
                System.out.println("Mismatch at frame " + i++);
                System.out.println(clientLine);
                System.out.println(serverLine);
            }
            else{
                System.out.println("Frame " + i++ + " ok");
            }
            clientIdx += clientIncr;
            serverIdx += serverIncr;
        }

        System.out.println("Done");


    }
/*
    public static void main(String args[]){
        File file = new File("A.txt");
        FileInputStream fis;
        BufferedInputStream bis = null;
        byte[] fileData = new byte[(int)file.length()*3/2];

        try {
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);

        } catch (FileNotFoundException e) {
            System.out.println(e);
        }

        try {
            bis.read(fileData, 0 , (int)file.length());
        } catch (IOException e) {
            System.out.println(e);
        }

        byte frameType = 1;
        byte seqNo = 1;
        int payLoadSize = 10;

        byte [] output= utils.frameUtils.stuff(fileData, 0 , payLoadSize, frameType, seqNo);

        System.out.println("Before bit stuffing");
        for(int i = 0; i< payLoadSize; i++){
            System.out.print(Integer.toBinaryString(Byte.toUnsignedInt(fileData[i]))+ " ");
        }
        System.out.println("");


        System.out.println("After bit stuffing");
        //System.out.println(output.length);
        for(int i = 0; i<output.length ; i++){
            System.out.print(Integer.toBinaryString(Byte.toUnsignedInt(output[i]))+ " ");
        }
        System.out.println("");

        int seqNo1 = utils.frameUtils.getSeqNo(output);
        int frameType1 = utils.frameUtils.getFrameType(output);

        byte []destuffed = utils.frameUtils.destuff(output);

        System.out.println("Frame Type: " + frameType1);
        System.out.println("Seq No " + seqNo1);

        System.out.println("After destuffing");
        for(int i = 0; i< destuffed.length; i++){
            System.out.print(Integer.toBinaryString(Byte.toUnsignedInt(destuffed[i]))+ " ");
        }
        System.out.println("");

        /*
        byte[] bytes = {0x0F, (byte)0xF1};

        //bytes[0] = (byte)0xFE;
        //bytes[1] = (byte)0x1D;

        System.out.println(bytes[1]);
        System.out.println(bytes[0]);

        ByteBuffer wrapped = ByteBuffer.wrap(bytes);
        short num = wrapped.getShort();

        ByteBuffer dbuf = ByteBuffer.allocate(2);
        dbuf.putShort(num);

        short mask = 0x001F;

        System.out.println(Integer.toBinaryString(num));

        byte[] out = dbuf.array();
        System.out.println(out[1]);
        System.out.println(out[0]);

        */

        /*for(int i = 0; i < file.length(); i++){
            if(fileData[i] < 0) System.out.println(i + "\t"+ fileData[i]);
        }*/

        /*
        byte mask = -128;
        byte current, checksum;

        //byte [] buffer = new byte[2];
        //buffer[0] = fileData[0];

        StringBuilder output = new StringBuilder("01111110");
        int count = 0;

        checksum = 0x00;

        for(int i = 0; i<20; i++){
            current = fileData[i];
            checksum = (byte)(checksum ^ fileData[i]);

            for(int j = 0; j<8; j++){
                if((current & mask) != 0){
                    count++;
                    if(count == 5){
                        output.append("10");
                        count = 0;
                    }
                    else output.append('1');
                }
                else{
                    count = 0;
                    output.append("0");
                }
                current =(byte)(current << 1);
            }
        }


        for(int i = 0; i< 20; i++){
            System.out.print(Integer.toBinaryString(Byte.toUnsignedInt(fileData[i]))+ " ");
        }
        System.out.println("");
        output.append("01111110");
        //System.out.println(output.toString());

        System.out.println(Integer.toBinaryString(Byte.toUnsignedInt(checksum)));

        */
        //System.out.println(Integer.toBinaryString(Short.toUnsignedInt(mask)));
    //}
}
