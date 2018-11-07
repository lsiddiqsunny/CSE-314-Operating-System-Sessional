
//send file
//reader reads file name from console and sends the file

package util;

import messenger.ServerReaderWriter;

import java.io.*;
import java.math.BigInteger;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Scanner;

import static com.oracle.jrockit.jfr.ContentType.Bytes;


public class FileReader implements Runnable
{
    public ConnectionUtillities connection;

    FileInputStream fis = null;
    FileOutputStream fos = null;
    BufferedInputStream bis = null;
    boolean breakLoop = false;

    public final static String FILE_TO_SEND = "file.txt";

    public FileReader(ConnectionUtillities con) {
        connection = con;
    }

    @Override
    public void run()
    {

        Scanner in=new Scanner(System.in);

        while (true)
        {
            System.out.println("Do you want to send files? Y/N : (Press 'Q' to quit) : ");
            String command = in.nextLine();
            connection.write(command);

            if (command.equals("Y"))
            {

                System.out.println("Do you want to check what happens if a frame is lost? Y/N :");
                String error_or_not = in.nextLine();

                connection.write(error_or_not);

                System.out.println("Please provide the id you want to send a file : ");
                String text = in.nextLine();

                connection.write(text);

                String foundOrNot = connection.read().toString();
                System.out.println(foundOrNot);


                if(foundOrNot.equals(text + " is online!"))
                {
                    //provide file name
                    System.out.println(connection.read().toString());
                    String filename = in.nextLine();
                    connection.write(filename);

                    //provide file size
                    System.out.println(connection.read().toString());
                    String fileSize = in.nextLine();
                    connection.write(fileSize);

                    //confirmation
                    String confirmation = connection.read().toString();
                    System.out.println("\n" + confirmation);

                    if(confirmation.equals("Space is available!"))
                    {

                        //fileId and maxChunk
                        String data = connection.read().toString();
                        System.out.println("\n" + data + "\n");

                        String information[] = data.split("-", 5);

                        int fileId = Integer.parseInt(information[1]);
                        int maxChunkSize = Integer.parseInt(information[3]);


                        File myFile = new File(filename);


                        byte[] mybytearray = new byte[maxChunkSize];

                        try {
                            fis = new FileInputStream(myFile);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        bis = new BufferedInputStream(fis);



                        String[] info;
                        int read = 0;
                        int totalRead = 0;
                        int filesize = Integer.parseInt(fileSize);
                        int remaining = filesize;
                        boolean success = true;
                        boolean send_frame_again = false;
                        String ready_to_send = new String();


                        int seq_no = 0;
                        int ack_number = 0;
                        int it = 0;
                        int iterations = (int)Math.ceil((float)filesize/maxChunkSize) ;


                        try
                        {
                            while (true)
                            {
                                if(iterations <= 0)break;


                                if(send_frame_again == false)
                                {
                                    it++;

                                    if(it%2 == 1)seq_no = 1;
                                    else seq_no = 0;

                                    System.out.println("\n\n" + it + "-th iteration!\n");


                                    read = bis.read(mybytearray);
                                    totalRead += read;
                                    remaining -= read;

                                    System.out.println("\nTotal Read : " + totalRead + " bytes!\n");

                                    System.out.println("Payload : " + Arrays.toString(mybytearray) + "\n");
                                    ready_to_send = prepareFrame("11111111", seq_no, 0, null, mybytearray, 0, maxChunkSize);

                                }
                                else
                                {
                                    System.out.println("\nI am sending the frame again! :( ");
                                }


                                connection.write(ready_to_send);
                                send_frame_again = false;

                                //for acknowledgement
                                connection.sc.setSoTimeout(30000);

                                try
                                {
                                    String acknowledgement = connection.read().toString();
                                    String de_ack = destuffMyBits(acknowledgement);

                                    String type_ack = de_ack.substring(0,8);

                                    ack_number = Integer.parseInt(de_ack.substring(16,24), 2);

                                    System.out.println("\nack number : " + ack_number);
                                    System.out.println("seq number : " + seq_no);

                                    if(ack_number == seq_no)
                                    {
                                        connection.write("1");
                                        System.out.println("Got ack of " + it + "-th iteration!");
                                    }

                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace();
                                    connection.write("0");
                                    System.out.println("\nNo acknowledgement!");
                                    //connection.write(ready_to_send);
                                    send_frame_again = true;
                                    iterations++; //vabo je ei iteration ta chiloi na

                                }


                                mybytearray = null;
                                mybytearray = new byte[maxChunkSize];
                                iterations--;

                            }


                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                            //error
                            System.out.println(connection.read().toString());
                            success = false;
                            breakLoop = true;
                        }


                        if(success == true)
                        {
                            //send confirmation
                            connection.write("1"); //completed
                        }

                        //confirmation or error
                        //if(connection.read().toString().equals("1"))System.out.println("\nFile transfer was successful!\n" +connection.read().toString());


                        try {
                            fis.close();
                            bis.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                    }


                }


            }
            else if(command.equals("N"))
            {

                String servermsg = connection.read().toString();
                System.out.println("\nYou got " + servermsg + " files to be downloaded!");

                if(servermsg.equals("Sorry no messages to display! "))
                {
                    continue;
                }
                else
                {
                    int totalfiles = Integer.parseInt(servermsg);
                    int current = 1;

                    while(current <= totalfiles)
                    {
                        current++;

                        //taking permission
                        String info = connection.read().toString();
                        System.out.println(info);


                        String permission = in.nextLine();
                        connection.write(permission);

                        if(permission.equals("Y"))
                        {
                            String information[] = info.split("-", 5);

                            String sender = information[0];
                            String filename = information[1];
                            int filesize = Integer.parseInt(information[2]);
                            int maxChunkSize = Integer.parseInt(information[3]);


                            String path = "C:"+File.separator+"hello";
                            String fname= path+File.separator+filename;
                            File f = new File(path);
                            File f1 = new File(fname);

                            f.mkdirs() ;
                            try {
                                f1.createNewFile();
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }


                            try
                            {
                                fos = new FileOutputStream(f1);
                            }
                            catch (FileNotFoundException e)
                            {
                                e.printStackTrace();
                            }


                            byte[] buffer = new byte[maxChunkSize];


                            int read = 0;
                            int totalRead = 0;
                            int remaining = 3;
                            boolean success = true;
                            int iterations = (int)Math.ceil((float)filesize/maxChunkSize) ;
                            int it = 0;

                            System.out.println("\nBuffer in String : " + new String(buffer));


                            try
                            {
                                //System.out.println(connection.read().toString());


                                while (true)
                                {

                                    if(iterations <= 0)break;

                                    it++;
                                    System.out.println("\n\n" + it + "-th iteration!\n");

                                    totalRead += read;
                                    remaining -= read;

                                    //System.out.println("\nTotal Read : " + totalRead + " and Remaining : " + remaining + " bytes!");

                                    //System.out.println("\nBuffer : " + Arrays.toString(buffer));

                                    //System.out.println("\n Buffer in String : " + new String(buffer));

                                    String rawFrame = connection.read().toString();

                                    info get_info = getPayload(rawFrame, "N", it,0,false);


                                    try
                                    {
                                        fos.write(get_info.payload, 0, get_info.read);
                                    }
                                    catch (Exception e)
                                    {
                                        e.printStackTrace();
                                    }


                                    buffer = null;
                                    buffer = new byte[maxChunkSize];
                                    iterations--;

                                    //connection.write("\nReceived " + read + " bytes of chunk! ");

                                }


                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                                //error
                                System.out.println(connection.read().toString());
                                success = false;
                                breakLoop = true;
                                try
                                {
                                    fos.close();
                                    f1.delete();
                                    f.delete();

                                } catch (IOException e1) {
                                    e1.printStackTrace();
                                }

                            }


                            //confirmation
                            System.out.println();
                            System.out.println(connection.read().toString());


                            try {
                                fos.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }


                        }
                        //else
                        {
                            //server message
                            //System.out.println(connection.read().toString());
                        }


                    }

                }



            }
            else if(command.equals("Q"))
            {
                System.out.println(connection.read().toString());
                //System.exit(0);
                break;
            }

            else
            {
                System.out.println("Not a valid command!");
            }

            if(breakLoop == true)
            {
                System.out.println(connection.read().toString());
                break;
            }


        }

    }


    private byte[] bigIntToByteArray( final int i )
    {
        BigInteger bigInt = BigInteger.valueOf(i);
        return bigInt.toByteArray();
    }


    private String stuffMyBits(String stringTobeStuffed)
    {
        String stuffedstring = new String();
        int count = 0;

        for(int i=0; i<stringTobeStuffed.length(); i++)
        {
            if(stringTobeStuffed.charAt(i) == '1')
            {
                count++;
                stuffedstring += stringTobeStuffed.charAt(i);
            }
            else
            {
                count = 0;
                stuffedstring += stringTobeStuffed.charAt(i);
            }

            if(count == 5)
            {
                stuffedstring += '0';
                count = 0;
            }

        }


        return stuffedstring;
    }


    private String destuffMyBits(String input)
    {
        String destuffedstring = new String();
        int count = 0;

        String stringTobedeStuffed = input.replaceAll("01111110$|^01111110", "");

        for(int i=0; i<stringTobedeStuffed.length(); i++)
        {
            if(stringTobedeStuffed.charAt(i) == '1')
            {
                count++;
                destuffedstring += stringTobedeStuffed.charAt(i);
            }
            else
            {
                count = 0;
                destuffedstring += stringTobedeStuffed.charAt(i);
            }

            if(count == 5)
            {
                i++;
                count = 0;
                continue;
            }

        }


        return destuffedstring;
    }



    private class info
    {
        private byte[] payload;
        private int read;
        private boolean send_ack;
        private int seq_no;

        private info()
        {
            payload = null;
            read = 0;
            send_ack = false;
            seq_no = 0;
        }

    }


    private info getPayload(String rawFrame, String error_or_not, int it, int rand_it, boolean want_error)
    {
        info return_info = new info();

        int read = 0;

        System.out.println("This one is raw frame before destuffing : " + rawFrame);

        String de_rawFrame = destuffMyBits(rawFrame);
        System.out.println("\nThis one is raw frame after destuffing : " + de_rawFrame);

        //erpor frame theke type | seq no. | ack no. | payload | checksum  ei order e alada korbo

        String type = de_rawFrame.substring(0,8);
        System.out.println("\ntype : " + type);

        int seq_no = Integer.parseInt(de_rawFrame.substring(8,16),2);
        System.out.println("seq no. : " + (byte)seq_no);

        int ack_no = Integer.parseInt(de_rawFrame.substring(16,24),2); //eta shobshomoy 0
        System.out.println("ack no. : " + (byte)ack_no);

        String checksum = de_rawFrame.substring(de_rawFrame.length()-8, de_rawFrame.length());

        String payload_string = de_rawFrame.substring(24,de_rawFrame.length()-8);
        System.out.println("\nPayload (before in binary) : " + payload_string);


        //for random frame error
        if(error_or_not.equals("Y") && it == rand_it && want_error == true)
        {
            //add an error
            System.out.println("\nBefore adding error, payload : " + payload_string);
            payload_string += "11111111";
            System.out.println("After adding error, payload : " + payload_string);
        }


        byte [] payload = new byte[payload_string.length()/8];
        int check_payload_sum = 0;

        for(int i=0, j=0; i<payload_string.length(); i+=8,j++)
        {
            payload[j] = (byte)(Integer.parseInt(payload_string.substring(i, i+8),2));
            check_payload_sum ^= payload[j];
            //System.out.println("payload["+ j + "] = " + payload[j]);
            //if(payload[j]!=0)read++;
        }


        //this was for checking
        String pay = new String();

        for(int i=0; i<payload.length; i++)
        {
            pay += Integer.toBinaryString(payload[i] & 255 | 256).substring(1);
        }


        String checksum_after_count = Integer.toBinaryString((byte)check_payload_sum & 255 | 256).substring(1);
        System.out.println("\nchecksum in string before counting : " + checksum);
        System.out.println("checksum in string after counting : " + checksum_after_count);


        if(checksum.equals(checksum_after_count))
        {
            System.out.println("\nMessages are same!");
            return_info.send_ack = true;
        }
        else
        {
            System.out.println("\nThere is error in message!");
        }


        System.out.println("\nPayload : " + Arrays.toString(payload));
        System.out.println("Payload(in binary) : " + pay);



        if(it == rand_it && want_error == true)
        {
            return_info.read = 0;
            return_info.payload = null;
        }
        else return_info.read = payload.length;
        return_info.payload = payload;
        return_info.seq_no = seq_no;


        return  return_info;

    }



    private String prepareFrame(String type, int seq_no, int ack_number, String payload_for_ack, byte[] mybytearray , int checksum_for_ack, int maxChunkSize)
    {
        //make a frame

        String frame = new String();
        String ready_to_send = new String();
        int checksum = 0;


        if(type.equals("00000000"))
        {
            frame += type;
            frame += Integer.toBinaryString((byte)seq_no & 255 | 256).substring(1);
            frame += Integer.toBinaryString((byte)ack_number & 255 | 256).substring(1);
            frame += payload_for_ack;
            frame += Integer.toBinaryString((byte)checksum_for_ack & 255 | 256).substring(1);

            ready_to_send = "01111110" + stuffMyBits(frame) + "01111110";

        }
        else if(type.equals("11111111"))
        {
            String payload = new String();

            for(int i=0; i<mybytearray.length; i++)
            {
                payload += Integer.toBinaryString(mybytearray[i] & 255 | 256).substring(1);
                //System.out.println("payload["+ i + "] = " + mybytearray[i]);
                checksum ^= mybytearray[i];

            }

            System.out.println("\nMybyteArray : " + Arrays.toString(mybytearray));

            System.out.println("\nPreparing frame : ");
            System.out.println();

            frame += type;
            System.out.println("Type : " + type);

            frame += Integer.toBinaryString((byte)seq_no & 255 | 256).substring(1);
            System.out.println("Seq. Number : " + Integer.toBinaryString((byte)seq_no & 255 | 256).substring(1));

            frame += Integer.toBinaryString((byte)ack_number & 255 | 256).substring(1);
            System.out.println("Ack. Number : " + Integer.toBinaryString((byte)ack_number & 255 | 256).substring(1));

            frame += payload;
            System.out.println("Payload : " + payload);

            frame += Integer.toBinaryString((byte)checksum & 255 | 256).substring(1);
            System.out.println("Checksum : " + Integer.toBinaryString((byte)checksum & 255 | 256).substring(1));


            System.out.println("\nThis one is full frame before stuffing : " + frame);

            String s_frame = stuffMyBits(frame);

            System.out.println("\nThis one is full frame after stuffing : " + s_frame);

            ready_to_send = "01111110" + s_frame + "01111110";

            System.out.println("\nThis one is full frame after adding flag : " + ready_to_send);



        }


        return ready_to_send;

    }


}