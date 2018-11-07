/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server1;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author User
 */
public class Server1
{

    public final static int SOCKET_PORT = 44444;
    //public final static String FILE_TO_SEND = "D:/blood.bmp";
    public static HashMap<String, String> file_list = new HashMap();
    public static HashMap<String, Socket> client_list = new HashMap();


    public static void main(String[] args) throws IOException
    {
        /* FileInputStream fis = null;
        BufferedInputStream bis = null;
        BufferedOutputStream os = null;*/
        ServerSocket servsock = null;
        Socket sock = null;
        try {
            servsock = new ServerSocket(SOCKET_PORT);
            while (true)
            {
                System.out.println("Waiting... in main");
                try
                {
                    sock = servsock.accept();
                    System.out.println("Accepted connection in main : " + sock);
                    // identifier.put(FILE_TO_SEND, sock);
                    Thread t = new Thread(new separate_thread(sock));
                    t.start();
                    // send file
                    //if (bis != null) bis.close();
                    //if (os != null) os.close();
                    //if (sock!=null) sock.close();

                }
                catch (IOException ex)
                {
                    System.out.println(ex.getMessage() + ": An Inbound Connection Was Not Resolved");
                }
            }
        }
        catch (Exception e)
        {
        }
    }

}

class separate_thread extends Thread
{

    public Socket sock = null;
    int bytesRead;
    int current = 0;
    FileOutputStream fos = null;
    BufferedOutputStream bos = null;
    ObjectInputStream ty = null;
    ObjectOutputStream os = null;
    BufferedInputStream is = null;
    String reciever_id = new String();
    String s = new String();
    int size_of_file;
    public String global_file_name;
    public static String global_user_id = "";
    public static  String  user_id="";

//////////////////

    FileInputStream fis = null;
    BufferedInputStream bis = null;
    BufferedOutputStream osr = null;
    /////////////////////////////

    public static int destuff_size ;
    public static String reverse = "";
    public static String head = "";
    public static String tail = "";

    public static int [] check_sum_count = {0,0,0,0,0,0,0,0};

    ///////////////////////////////////////////////////////

    public static int[] chunk_size_array = new int[10000000];
    ///////////////////////////////////////////////////////


    public static String toBinary( byte[] bytes )
    {
        StringBuilder sb = new StringBuilder(bytes.length * Byte.SIZE);
        for( int i = 0; i < Byte.SIZE * bytes.length; i++ )
            sb.append((bytes[i / Byte.SIZE] << i % Byte.SIZE & 0x80) == 0 ? '0' : '1');
        return sb.toString();

    }


    public static byte[] fromBinary( String s )
    {
        int sLen = s.length();
        byte[] toReturn = new byte[100000];
        char c;
        for( int i = 0; i < sLen; i++ )
            if( (c = s.charAt(i)) == '1' )
                toReturn[i / Byte.SIZE] = (byte) (toReturn[i / Byte.SIZE] | (0x80 >>> (i % Byte.SIZE)));
            else if ( c != '0' )
                throw new IllegalArgumentException();
        return toReturn;
    }

    public static String bit_stuff(String data)
    {
        String res = new String();
        String out=new String();
        int counter = 0;
        for(int i=0; i<data.length(); i++)
        {

            if (data.charAt(i)!='1' && data.charAt(i)!='0')
            {
                System.out.println("Enter only Binary values!!!");
                return null;
            }
            if(data.charAt(i) == '1')
            {
                counter++;
                res = res + data.charAt(i);
            }
            else
            {
                res = res + data.charAt(i);
                counter = 0;
            }
            if(counter == 5)
            {
                res = res + '0';
                counter = 0;
            }

        }
        //String inc="01111110"+res+"01111110";
        String inc = res;
        return inc;





    }

    public static String bit_destuff(String res)
    {
        int counter=0;
        String out = "";
        for(int i=0; i<res.length(); i++)
        {

            if(counter == 5)
            {

                counter = 0;
                continue;
            }
            if(res.charAt(i) == '1')
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

    public static byte[] getByteByString(String binaryString)
    {
        int splitSize = 8;

        if(binaryString.length() % splitSize == 0)
        {
            int index = 0;
            int position = 0;

            byte[] resultByteArray = new byte[100000];
            StringBuilder text = new StringBuilder(binaryString);

            while (index < text.length())
            {
                String binaryStringChunk = text.substring(index, Math.min(index + splitSize, text.length()));
                Integer byteAsInt = Integer.parseInt(binaryStringChunk, 2);
                resultByteArray[position] = byteAsInt.byteValue();
                index += splitSize;
                position ++;
            }
            return resultByteArray;
        }
        else
        {
            System.out.println("Cannot convert binary string to byte[], because of the input length. '" +binaryString+"' % 8 != 0");
            return null;
        }
    }



    public static int check_head_tail(String bits_with_head_tail )
    {
        //returns 1 if head tail okay, else 0
        head =  bits_with_head_tail.substring(0, 8);
        reverse = new StringBuffer(bits_with_head_tail).reverse().toString();
        tail = reverse.substring(0, 8);

        //System.out.println("head is : " + head);
        //System.out.println("tail is : "  + tail);

        if(head.equals(tail))
        {
            return 1;

        }
        else
        {
            return 0;
        }



    }

    ////////////////////
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

            if( i % 8 == 0 && bit_str.charAt(i) == '1')
            {
                check_sum_count[0] = check_sum_count[0] + 1;

            }
            else if( i % 8 == 1 && bit_str.charAt(i) == '1')
            {
                check_sum_count[1] = check_sum_count[1] + 1;

            }
            else if( i % 8 == 2 && bit_str.charAt(i) == '1')
            {
                check_sum_count[2] = check_sum_count[2] + 1;

            }
            else if( i % 8 == 3 && bit_str.charAt(i) == '1')
            {
                check_sum_count[3] = check_sum_count[3] + 1;

            }
            else if( i % 8 == 4 && bit_str.charAt(i) == '1')
            {
                check_sum_count[4] = check_sum_count[4] + 1;

            }
            else if( i % 8 == 5 && bit_str.charAt(i) == '1')
            {
                check_sum_count[5] = check_sum_count[5] + 1;

            }
            else if( i % 8 == 6 && bit_str.charAt(i) == '1')
            {
                check_sum_count[6] = check_sum_count[6] + 1;

            }
            else if( i % 8 == 7 && bit_str.charAt(i) == '1')
            {
                check_sum_count[7] = check_sum_count[7] + 1;

            }


        }


    }

    public static int checksum(String bit_str, String checksum_str)
    {
        //returns 1 if there is no error,  and 0 else
        checksum_1(bit_str);
        //System.out.println("checksum array count:  ");
        //for (int i = 0; i < 8; i++) {
        //System.out.print(check_sum_count[i]%2 + " ");

        //}
        // System.out.println("");
        //System.out.println("checksum got from sender: ");
        // for (int i = 0; i < 8; i++) {
        //System.out.print(Character.getNumericValue(checksum_str.charAt(i)) + " ");

        //}
        //System.out.println("");
        //Character.getNumericValue(char c)
        //if((check_sum_count[0]%2)==Character.getNumericValue(checksum_str.charAt(0)) && (check_sum_count[1]%2)==Character.getNumericValue(checksum_str.charAt(1))&&(check_sum_count[2]%2)==checksum_str.charAt(2)&&(check_sum_count[3]%2)==Character.getNumericValue(checksum_str.charAt(3)) && (check_sum_count[4]%2)==checksum_str.charAt(4) &&(check_sum_count[5]%2)==Character.getNumericValue(checksum_str.charAt(5)) &&(check_sum_count[6]%2)==Character.getNumericValue(checksum_str.charAt(6)) &&(check_sum_count[7]%2)==Character.getNumericValue(checksum_str.charAt(7)) )
        if(check_sum_count[0]%2 ==Character.getNumericValue(checksum_str.charAt(0))&& check_sum_count[1]%2 ==Character.getNumericValue(checksum_str.charAt(1))&& check_sum_count[2]%2 ==Character.getNumericValue(checksum_str.charAt(2))&& check_sum_count[3]%2 ==Character.getNumericValue(checksum_str.charAt(3))&&check_sum_count[4]%2 ==Character.getNumericValue(checksum_str.charAt(4)) &&check_sum_count[5]%2 ==Character.getNumericValue(checksum_str.charAt(5)) &&check_sum_count[6]%2 ==Character.getNumericValue(checksum_str.charAt(6)) &&check_sum_count[7]%2 ==Character.getNumericValue(checksum_str.charAt(7)) )
        {
            //System.out.println("checksum okay");
            return 1; // no error

        }
        System.out.println("checksum error");
        return 0; //error



    }









    ////////////////////////////////////////////////////

    public separate_thread(Socket sock)
    {
        this.sock = sock;
        try
        {
            ty = new ObjectInputStream(sock.getInputStream());
            // receive file
            os = new ObjectOutputStream(sock.getOutputStream());
            is = new BufferedInputStream(sock.getInputStream());
            osr = new BufferedOutputStream(sock.getOutputStream());

        }
        catch (Exception e)
        {
            Server1.client_list.remove(global_user_id);
            System.out.println("found");
        }
    }

    @Override
    public void run()
    {
        while (true)
        {
            try
            {
                // sock = new Socket(SERVER, SOCKET_PORT);
                System.out.println("Connecting...");
                //rollinput
                //if(check hashmap ei roll
                //jodu=i thake return
                //na thakle hashmap.put(roll,sock

                String user_id = ty.readObject().toString();
                System.out.println("user id : " + user_id);
                global_user_id = user_id;

                if (Server1.client_list.containsKey(user_id) == true)
                {
                    os.writeObject(0);  // ekhane 0 chilo, ekhon 1 kore dilam, cz alrdy logged in e prb
                    os.flush();
                    System.out.println("Error: this id is already logged in.");
                    //return;
                }
                else
                {
                    os.writeObject(1);
                    os.flush();

                    Server1.client_list.put(user_id, sock);
                }
                int choose = (int)ty.readObject();
                System.out.println("choose: "+choose);
                if(choose == 1)
                {
                    reciever_id = ty.readObject().toString();
                    System.out.println(reciever_id);
                    ////////////////////

                    if (Server1.client_list.containsKey(reciever_id) == false)
                    {
                        os.writeObject(0);
                        os.flush();
                        System.out.println("Error: reciever not currently online.");
                        //return;
                    } //else
                    //{
                    //  Server1.client_list.put(user_id,sock);
                    //}
                    else
                    {
                        os.writeObject(1);
                        os.flush();
                        s = ty.readObject().toString();
                        int a = (int) ty.readObject();
                        Server1.file_list.put(reciever_id, s);
                        Random rand = new Random();
                        //int chunk = rand.nextInt(a) % 5000;
                        int chunk = 700;
                        destuff_size = chunk;
                        size_of_file = a;
                        os.writeObject(chunk);
                        os.flush();
                        byte[] mybytearray = new byte[100000];
                        byte[] mybytearray2 = new byte[100000];
                        fos = new FileOutputStream(new File(s));
                        //bos = new BufferedOutputStream(fos);
                        int z = 0;
                        int size = 0;
                        int p = 0;
                        int koybar_cholbe = a / chunk + ((a % chunk == 0) ? 0 : 1);
                        int check_z = -2;
                        int q = 0;
                        int index = 0;
                        int desired_count = 0;
                        int coming_count;
                        while(true)
                        {

                            chunk_size_array[index++] = (int)ty.readObject();

                            q++;

                            if (q == koybar_cholbe)
                            {
                                break;

                            }


                        }
                        int chunk_index = 0;


                        while (true)
                        {

                            ///////start 1//////
                            System.out.println("COUNTTTttttttttttt:"  + (desired_count+1) );
                            coming_count = (int) ty.readObject();

                            String bits_with_head_tail;
                            //String bits;

                            bits_with_head_tail = ty.readObject().toString();

                            int check_head_tail = check_head_tail(bits_with_head_tail);  // 1 hoile head tail thik ase

                            // System.out.println("check head tail : " + check_head_tail);

                            ////removing head and tail
                            String bits_without_head_tail_1 = bits_with_head_tail.substring(8);
                            String bits_without_head_tail_2 = new StringBuffer(bits_without_head_tail_1).reverse().toString();
                            String bits_without_head_tail_3 = bits_without_head_tail_2.substring(8);
                            String bits_without_head_tail_final = new StringBuffer(bits_without_head_tail_3).reverse().toString();
                            //////////head tail removed

                            String kind = bits_without_head_tail_final.substring(0, 8);
                            String seq = bits_without_head_tail_final.substring(8, 16);
                            String ack = bits_without_head_tail_final.substring(16, 24);

                            //System.out.println("kind in server : " + kind) ;
                            System.out.println("seq in server : "+seq);
                            // System.out.println("ack in server : "+ ack);
                            int coming_count_seq = Integer.parseInt(seq, 2);

                            String desired_kind = "00000000";
                            //String desired_seq = "10101010";
                            String desird_ack = "00000000";
                            /////removing checksum bit
                            String bits_without_checksum_1 = new StringBuffer(bits_without_head_tail_final).reverse().toString();
                            String checksum = new StringBuffer(bits_without_checksum_1.substring(0, 8)).reverse().toString();
                            String temp = bits_without_checksum_1.substring(8);
                            String fresh_stuffed_bits = new StringBuffer(temp).reverse().toString();
                            fresh_stuffed_bits = fresh_stuffed_bits.substring(24);
                            String fresh_destuffed_bits = bit_destuff(fresh_stuffed_bits);


                            /////checksum bit removed


                            int checksum_error = 1 - checksum(fresh_destuffed_bits, checksum);  //error = 1 mane error ase .,.... and error = 0 mane no error
                            //System.out.println("error: " + checksum_error);



                            //System.out.println("at server bit:   " +bit_destuff(fresh_stuffed_bits));

                            int time_out = (int) ty.readObject();



                            ///////end 1/////

                            //  z = is.read(mybytearray);
                            //  if(check_z < z )
                            // {
                            //     check_z = z;
                            //  }
                            if(check_head_tail == 1 && checksum_error == 0 && kind.equals(desired_kind) && ack.equals(desird_ack) )
                                //if(check_head_tail == 1 && checksum_error == 0)
                            {
                                p++;
                                String acknowledgement = "chunk no. "+ p +" Received by the server";
                                os.writeObject(acknowledgement);
                                os.flush();


                            }

                            //mybytearray = fromBinary(bit_destuff(bits));
                            ///System.out.println("lol           : "  + bit_destuff(toBinary(mybytearray)));
                            //System.out.println("destuffed size at server :" + fromBinary(bit_destuff(bits)).length  );
                            //fos.write(fromBinary(bit_destuff(bits)), 0, z);
                            //if(check_z == z)
                            //{
                            //fos.write(fromBinary(bit_destuff(bits)), 0, z);
                            //}
                            //else
                            //{
                            //z = (int)ty.readObject();
                            //System.out.println("z value: "+ z);

                            if(desired_count+1 == coming_count_seq)
                            {
                                fos.write(fromBinary(bit_destuff(fresh_stuffed_bits)), 0, chunk_size_array[chunk_index++]);
                                fos.flush();
                                desired_count++;
                            }

                            //System.out.println("check bits here: "+ toBinary(fromBinary(bit_destuff(bits_without_head_tail_final))));


                            //}
                            size += chunk_size_array[chunk_index];

                            //os.writeObject(p);
                            //os.flush();
                            //System.out.println("chunk no." + p);

                            if(time_out == 0)
                            {
                                String time_out_ack = "OKAY, no time-out";
                                os.writeObject(time_out_ack);
                                os.flush();


                            }
                            else
                            {
                                p--;
                            }
                            if (p == koybar_cholbe)
                            {
                                break;
                            }

                            // if(size>a){break;}
                        }

                        fos.close();

                        System.out.println("File " + s
                                           + " downloaded (" + size + " bytes read)");

                        String logout_user = ty.readObject().toString();
                        Server1.client_list.remove(logout_user);
                    }
                }
                else
                {
                    Socket rec_sock = Server1.client_list.get(user_id);//(reciever_id); //
                    //   while (true) {
                    System.out.println("else er moddhe recvr: "+ user_id);
                    System.out.println("Waiting...");
                    try
                    {
                        //sock = servsock.accept();
                        //ObjectOutputStream oo_rec=new ObjectOutputStream(rec_sock.getOutputStream());
                        String file_name = "" ;
                        if(Server1.file_list.containsKey(user_id))
                        {
                            os.writeObject(1);//file ase
                            os.flush();
                            file_name = Server1.file_list.get(user_id);
                        }
                        else
                        {
                            os.writeObject(0);//file nai
                            os.flush();

                            // return;

                        }
                        System.out.println("Accepted connection : " + rec_sock);
                        System.out.println("check the sock: "+ sock);
                        // send file
                        File myFile = new File (file_name);
                        global_file_name = file_name;
                        System.out.println("file name : "+ file_name);
                        byte [] mybytearray  = new byte [(int)myFile.length()];
                        System.out.println("file last mod: "+myFile.length());
                        os.writeObject((int)myFile.length());
                        os.flush();
                        os.writeObject(file_name);
                        os.flush();
                        fis = new FileInputStream(myFile);
                        bis = new BufferedInputStream(fis);
                        bis.read(mybytearray);
                        //osr = new BufferedOutputStream(rec_sock.getOutputStream());
                        System.out.println("Sending " + file_name + "(" + mybytearray.length + " bytes)");
                        osr.write(mybytearray,0,mybytearray.length);
                        osr.flush();
                        System.out.println("Done okay.");
                        Server1.file_list.remove(user_id);
                        
                        String logout_user = ty.readObject().toString();
                        Server1.client_list.remove(logout_user);




                    }
                    catch (IOException ex)
                    {
                        Server1.client_list.remove(user_id);

                        System.out.println(ex.getMessage()+": An Inbound Connection Was Not Resolved");
                    }
                    finally
                    {
                         System.out.println("finally 1");   
                        /*
                         if(osr != null)
                        {
                            osr.close();

                        }
                        if(bis != null)
                        {
                             bis.close();


                        }

                        if(fis != null)
                        {

                            fis.close();

                        }
                        if(os != null)
                        {
                            os.close();

                        }
                        if(fos != null)
                        {
                            fos.close();

                        }
                        if(bos != null)
                        {
                            bos.close();

                        }
                        if(is != null)
                        {
                            is.close();
                        }
                        if(ty != null)
                        {
                            ty.close();
                        }*/
                        File file = new File(global_file_name);
                        file.delete();

                    }

                }
                // while(true);
            }
            catch (IOException ex)
            {
                Server1.client_list.remove(global_user_id);
                //Server1.client_list.remove(user_id);

                Logger.getLogger(separate_thread.class.getName()).log(Level.SEVERE, null, ex);
            }
            catch (ClassNotFoundException ex)
            {
                Server1.client_list.remove(global_user_id);
                Logger.getLogger(separate_thread.class.getName()).log(Level.SEVERE, null, ex);
            }
            finally
            {
                System.out.println("finally 2");
            }

            //reciever code starts here

            // try {

            //}

            //catch(Exception e)
            //{

            //}




        }
    }

}
