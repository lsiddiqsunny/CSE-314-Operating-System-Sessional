/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client1;

import com.oracle.jrockit.jfr.DataType;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;

/**
 *
 * @author User
 */
public class Client1
{

    public final static int SOCKET_PORT = 44444;
    public final static String SERVER = "localhost";
    // public final static String
    //      FILE_TO_RECEIVED = "file-rec.jpg";

    public final static int FILE_SIZE = Integer.MAX_VALUE;

    public static String FILE_TO_SEND; //= "E:/a.jpg";
    public static String user_id;
    public static String reciever_id;
    public static String file_path;
    public static String file_name_first;

    ////////////////////
    public static int new_byte_array_size = 0;

    ///////////////////////////////////////////////////////
    public static int[] check_sum_count = {0, 0, 0, 0, 0, 0, 0, 0};
    ///////////////////////

    public static String full_str = "";
    public static int[] chunk_size_array = new int[100000];
    public static int chunk_index = 0;

    ///////////////////////////////////////////////////////
    public static String toBinary(byte[] bytes, int len)
    {
        StringBuilder sb = new StringBuilder(len * 8);
        for (int i = 0; i < len * 8; i++)
        {
            sb.append((bytes[i / Byte.SIZE] << i % Byte.SIZE & 0x80) == 0 ? '0' : '1');
        }
        return sb.toString();

    }

    public static byte[] fromBinary(String s)
    {
        int sLen = s.length();
        byte[] toReturn = new byte[(sLen + Byte.SIZE - 1) / Byte.SIZE];
        new_byte_array_size = (sLen + Byte.SIZE - 1) / Byte.SIZE;
        char c;
        for (int i = 0; i < sLen; i++)
        {
            if ((c = s.charAt(i)) == '1')
            {
                toReturn[i / Byte.SIZE] = (byte) (toReturn[i / Byte.SIZE] | (0x80 >>> (i % Byte.SIZE)));
            }
            else if (c != '0')
            {
                throw new IllegalArgumentException();
            }
        }
        return toReturn;
    }

    public static String bit_stuff(String data)
    {
        String res = new String();
        String out = new String();
        int counter = 0;
        for (int i = 0; i < data.length(); i++)
        {

            if (data.charAt(i) != '1' && data.charAt(i) != '0')
            {
                System.out.println("Enter only Binary values!!!");
                return null;
            }
            if (data.charAt(i) == '1')
            {
                counter++;
                res = res + "1";
            }
            else
            {
                res = res + "0";
                counter = 0;
            }
            if (counter == 5)
            {
                res = res + "0";
                counter = 0;
            }

        }
        //String inc="01111110"+res+"01111110";
        String inc = res;
        return inc;

    }

    public static String bit_destuff(String res)
    {
        int counter = 0;
        String out = "";
        for (int i = 0; i < res.length(); i++)
        {

            if (counter == 5)
            {

                counter = 0;
                continue;
            }
            if (res.charAt(i) == '1')
            {

                counter++;
                out = out + "1";

            }
            else
            {
                out = out + "0";
                counter = 0;
            }

        }

        return out;

    }

    ///////////////////////////////////////////////////////////
    public static void checksum_1(String bit_str)
    {
        check_sum_count[0] = 0;
        check_sum_count[1] = 0;
        check_sum_count[2] = 0;
        check_sum_count[3] = 0;
        check_sum_count[4] = 0;
        check_sum_count[5] = 0;
        check_sum_count[6] = 0;
        check_sum_count[7] = 0;

        for (int i = 0; i < bit_str.length(); i++)
        {

            if (i % 8 == 0 && bit_str.charAt(i) == '1')
            {
                check_sum_count[0] = check_sum_count[0] + 1;

            }
            else if (i % 8 == 1 && bit_str.charAt(i) == '1')
            {
                check_sum_count[1] = check_sum_count[1] + 1;

            }
            else if (i % 8 == 2 && bit_str.charAt(i) == '1')
            {
                check_sum_count[2] = check_sum_count[2] + 1;

            }
            else if (i % 8 == 3 && bit_str.charAt(i) == '1')
            {
                check_sum_count[3] = check_sum_count[3] + 1;

            }
            else if (i % 8 == 4 && bit_str.charAt(i) == '1')
            {
                check_sum_count[4] = check_sum_count[4] + 1;

            }
            else if (i % 8 == 5 && bit_str.charAt(i) == '1')
            {
                check_sum_count[5] = check_sum_count[5] + 1;

            }
            else if (i % 8 == 6 && bit_str.charAt(i) == '1')
            {
                check_sum_count[6] = check_sum_count[6] + 1;

            }
            else if (i % 8 == 7 && bit_str.charAt(i) == '1')
            {
                check_sum_count[7] = check_sum_count[7] + 1;

            }

        }

    }

    public static String checksum(String bit_str)
    {
        checksum_1(bit_str);
        String out_str = "";

        for (int i = 0; i < 8; i++)
        {
            if (check_sum_count[i] % 2 == 0)
            {
                out_str = out_str + "0";

            }
            else if (check_sum_count[i] % 2 == 1)
            {
                out_str = out_str + "1";

            }

        }

        return out_str;

    }

    public static String int_to_bit_string(int my_int)
    {
        String bit_string = Integer.toBinaryString(my_int);
        String result = "";
        if (bit_string.length() == 1)
        {
            result = "0000000" + bit_string;

        }
        else if (bit_string.length() == 2)
        {
            result = "000000" + bit_string;

        }
        else if (bit_string.length() == 3)
        {
            result = "00000" + bit_string;

        }
        else if (bit_string.length() == 4)
        {

            result = "0000" + bit_string;

        }
        else if (bit_string.length() == 5)
        {
            result = "000" + bit_string;

        }
        else if (bit_string.length() == 6)
        {
            result = "00" + bit_string;

        }
        else if (bit_string.length() == 7)
        {
            result = "0" + bit_string;

        }
        else
        {
            result = bit_string;

        }

        return result;

    }

    ////////////////////////////////////////////////////
    public static void main(String[] args) throws IOException, ClassNotFoundException
    {
        /*   int bytesRead;
        int current = 0;
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;*/
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        BufferedOutputStream os = null;
        Socket sock = new Socket(SERVER, SOCKET_PORT);
        ObjectOutputStream oo = new ObjectOutputStream(sock.getOutputStream());
        ObjectInputStream oi = new ObjectInputStream(sock.getInputStream());

        /////
        int bytesRead;
        int current = 0;
        FileOutputStream fos = null;
        //BufferedOutputStream bos = null;

        ////////////////////////////////////////
        while (true)
        {

            //public final static int FILE_SIZE = Integer.MAX_VALUE;
            //public static StringFILE_TO_SEND; //= "E:/a.jpg";
            //public static String user_id;
            //public static String reciever_id;
            //public static String file_path;
            //public static String file_name_first;
            ////////////////////
            new_byte_array_size = 0;

            ///////////////////////////////////////////////////////
            //public static int[] check_sum_count = {0, 0, 0, 0, 0, 0, 0, 0};
            ///////////////////////
            for (int l = 0; l < 8; l++)
            {
                check_sum_count[l] = 0;

            }

            full_str = "";
            chunk_size_array = new int[100000];
            chunk_index = 0;

            System.out.println("Enter your username : ");

            Scanner in = new Scanner(System.in);
            user_id = in.nextLine();
            oo.writeObject(user_id);
            oo.flush();
            ////////////////////////////////////////////
            int user_id_log_alrdy = (int) oi.readObject();
            if (user_id_log_alrdy == 0)
            {
                System.out.println("this id alrdy logged in.");
                return;
            }
            ///////////////////////////////////////////

            System.out.println("send/recieve?:");
            int choose = in.nextInt();
            //E:/a.jpg

            if (choose == 1)
            {
                ///////////////////////////////////////////////
                ///////////////////////////////////////////////
                System.out.println("Enter file location: ");
                Scanner into3 = new Scanner(System.in);
                file_path = into3.nextLine();

                System.out.println("Enter file name with extension: ");
                Scanner into4 = new Scanner(System.in);
                file_name_first = into4.nextLine();

                FILE_TO_SEND = file_path + file_name_first;

                System.out.println("Enter your reciever name : ");

                //Scanner in = new Scanner(System.in);
                Scanner into = new Scanner(System.in);

                reciever_id = into.nextLine();

                System.out.println("Do you want timeout? [0/1] : ");

                Scanner into1 = new Scanner(System.in);

                int time_out_choice = into1.nextInt();
                int time_out_chunk;

                if (time_out_choice == 1)
                {
                    System.out.println("Insert the chunk no. where you want time out?: ");
                    Scanner in1 = new Scanner(System.in);
                    time_out_chunk = in1.nextInt() - 1;

                }
                else
                {
                    time_out_chunk = -1;
                }

                oo.writeObject((int) 1);
                oo.flush();
                sock.setSoTimeout(1000);

                oo.writeObject(reciever_id);
                oo.flush();
                ///////////////////////////////////
                int rec_online = (int) oi.readObject();
                if (rec_online == 0)
                {
                    System.out.println("file can't be sent. your reciever not currently online");
                    return;
                }

                oo.writeObject(file_name_first);

                oo.flush();

                File myFile = new File(FILE_TO_SEND);

                oo.writeObject((int) myFile.length());
                oo.flush();
                int chunk = (int) oi.readObject();
                byte[] mybytearray = new byte[chunk];
                fis = new FileInputStream(myFile);
                bis = new BufferedInputStream(fis);
                os = new BufferedOutputStream(sock.getOutputStream());
                int size = 0;
                int y = 0;
                int count = 0;

                while ((y = bis.read(mybytearray)) > 0)
                {

                    full_str = full_str + toBinary(mybytearray, y);
                    chunk_size_array[chunk_index++] = y * 8;
                    oo.writeObject((int) y);
                    oo.flush();

                }
                ///////////////
                //////////////
                int koybar_ghurbe = chunk_index;
                int koybar_ghurse = 0;
                int start = 0;
                int end = chunk_size_array[0];
                int start_index = 0;
                int end_index = 1;
                int time_out = 0;

                while (true)
                {
                    //////////start 1////////////////////////////////////
                    sock.setSoTimeout(10000); //  10000 = 10 sec

                    try
                    {
                        //int time_out_test = (int) oi.readObject();
                        if (koybar_ghurbe == koybar_ghurse)
                        {
                            break;
                        }

                        System.out.println("COUNTTTttttttttttt:" + (count + 1));
                        oo.writeObject((int) count + 1);
                        oo.flush();

                        //System.out.println("Bit-Stuffed form:    " + bit_stuff(full_str.substring(start, end)));
                        //System.out.println("Normal BIT FORM:     " + full_str.substring(start, end));
                        //System.out.println("Normal BIT FORM (check to_byte):     " + toBinary(fromBinary(toBinary(mybytearray))));
                        //System.out.println("again Destuffed form:" + bit_destuff(bit_stuff(full_str.substring(start, end))));
                        //////////////end 1///////////////////////////////////
                        //head tail = 01111110//
                        ///////////start 2///////////////
                        String kind = "00000000";
                        String seq = int_to_bit_string(count + 1);
                        String ack = "00000000";

                        System.out.println("Bit String of chunk count at client: " + int_to_bit_string(count + 1));

                        int test = Integer.parseInt(int_to_bit_string(count + 1), 2);
                        System.out.println("Int of chunk count at client: " + test);

                        //int foo = Integer.parseInt("1001", 2);
                        String to_be_sent = "01111110" + kind + seq + ack + bit_stuff(full_str.substring(start, end)) + checksum(full_str.substring(start, end)) + "01111110";
                        //System.out.println("checksum bits :" + checksum(full_str.substring(start, end))); //okay
                        oo.writeObject(to_be_sent);
                        //oo.writeObject(toBinary(mybytearray,y));
                        oo.flush();

                        ///////////end 2////////////////
                        //System.out.println("chunk size: " + chunk);
                        //System.out.println("y: " + y);
                        //System.out.println("new aarray size: "+ fromBinary(bit_stuff(toBinary(mybytearray))).length );
                        if (count == time_out_chunk)
                        {
                            time_out = 1;
                            time_out_chunk = -1;

                        }
                        else
                        {
                            time_out = 0;

                        }

                        oo.writeObject((int) time_out);
                        //oo.writeObject(toBinary(mybytearray,y));
                        oo.flush();

                        ///////////////////////
                        String acknowledgement = oi.readObject().toString();
                        System.out.println(acknowledgement);

                        ///////////////////
                        //oo.writeObject((int) y);
                        //oo.flush();
                        ///os.write(fromBinary(bit_stuff(toBinary(mybytearray))), 0, fromBinary(bit_stuff(toBinary(mybytearray))).length);
                        //os.write(mybytearray,0,y);
                        //os.flush();
                        //System.out.println((int)oi.readObject());
                        String time_out_ack = oi.readObject().toString();
                        System.out.println("Time out ack got in sender side");

                        count++;

                        size += y;
                        //System.out.println("chunk no. in client: " + count);
                        start = start + chunk_size_array[start_index++];
                        end = end + chunk_size_array[end_index++];

                        koybar_ghurse++;

                        if (koybar_ghurbe == koybar_ghurse)
                        {
                            break;
                        }

                    }
                    catch (InterruptedIOException iioe)
                    {
                        //koybar_ghurse--;
                        //count--;

                        System.err.println("Remote host timed out during read operation");
                    } // Exception thrown when general network I/O error occurs
                    catch (IOException ioe)
                    {
                        System.err.println("Network I/O error - " + ioe);
                    }

                }
                System.out.println("Sending " + FILE_TO_SEND + "(" + size + " bytes)");
                //  while()

                //}
                System.out.println("Done.");

                oo.writeObject(user_id);
                oo.flush();
            }
            else
            {

                /////////////////////////////////////////
                //try kori
                oo.writeObject((int) 2);
                oo.flush();

                try
                {
                    //sock = new Socket(SERVER, SOCKET_PORT);
                    System.out.println("Connecting...");
                    System.out.println("hereeeee");
                    int file_ase = (int) oi.readObject();
                    if (file_ase == 0)
                    {
                        System.out.println("you have no file to recieve.");
                        return;
                    }
                    // receive file
                    int file_size = (int) oi.readObject();
                    //file_size = 3654;
                    System.out.println("file size in client rec:  " + file_size);
                    byte[] mybytearray_rec = new byte[file_size]; ////filesize
                    System.out.println("here  22222222222");
                    String file_name = oi.readObject().toString();
                    BufferedInputStream is = new BufferedInputStream(sock.getInputStream());
                    fos = new FileOutputStream(file_name); // file name here
                    //os = new BufferedOutputStream(fos);
                    bytesRead = is.read(mybytearray_rec, 0, mybytearray_rec.length);//
                    current = bytesRead;
                    System.out.println("heree 33333333333333");
                    do
                    {
                        bytesRead
                            = is.read(mybytearray_rec, current, (mybytearray_rec.length - current));
                        if (bytesRead >= 0)
                        {
                            current += bytesRead;
                        }
                        System.out.println(bytesRead);
                    }
                    while (bytesRead > 0);

                    fos.write(mybytearray_rec, 0, current);
                    fos.flush();

                    System.out.println("File " + "rec.jpg" //file name here
                                       + " downloaded (" + current + " bytes read)");
                    fos.close();

                    oo.writeObject(user_id);
                    oo.flush();

                    //is.close();
                }
                catch (Exception e)
                {
                }

            }
        }
        //////////////////////////////////////////
        //while (true);

    }
}
