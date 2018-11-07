/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package messenger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Random;

import util.ConnectionUtillities;
import util.FileInfo;

/**
 *
 * @author uesr
 */
public class ServerReaderWriter implements Runnable
{

    public ConnectionUtillities connection;
    public String user;

    public final static String FILE_TO_SEND = "file.txt";

    FileInputStream fis;
    BufferedInputStream bis;
    BufferedOutputStream bos;
    FileOutputStream fos;
    boolean breakLoop = false;


    public ServerReaderWriter(String username, ConnectionUtillities con)
    {
        connection = con;
        user = username;

    }

    @Override
    public void run() {
        while (true)
        {
            String command = connection.read().toString();
            System.out.println("\n" + user + " command is " + command);

            if (command.equals("Y"))
            {
                //receiving file

                String error_or_not = connection.read().toString();

                if(error_or_not.equals("Y"))System.out.println("User wants to introduce a lost frame!");

                Object o = connection.read();
                String username = o.toString();

                System.out.println(user + " is willing to send a file to " + username);

                if (Server.clientList.containsKey(username))
                {
                    System.out.println(username + " found!");

                    //user found!
                    connection.write(username + " is online!");

                    Information info = Server.clientList.get(username);

                    //request file name
                    connection.write("Provide the file name : ");
                    String filename = connection.read().toString();


                    //request file size
                    connection.write("Provide the file size : ");
                    String fileSize = connection.read().toString();
                    int filesize = Integer.parseInt(fileSize);


                    if(filename != null && fileSize != null)
                    {
                        System.out.println(user + " provided file name and size!");
                    }


                    if (filesize < Server.remainingStorage)
                    {

                        connection.write("Space is available!");

                        Server.remainingStorage -= filesize;

                        System.out.println("Enough space! Available space is now " + Server.remainingStorage + " bytes.");

                        Random rand = new Random();
                        int maxChunkSize = Math.min(1024 , rand.nextInt((int) (filesize / 4)) + (int) (filesize / 4)); //bytes

                        Server.fileID++;

                        connection.write("fileId-" + Server.fileID + "-and you can send-" + maxChunkSize + "-bytes at a time.");



                        byte[] buffer = new byte[maxChunkSize+6];

                        //System.out.println("\nfilesize " + filesize + " bytes.");// Send file size in separate msg
                        int read = 0;
                        int totalRead = 0;
                        int remaining = filesize;
                        boolean success = true;
                        boolean shouldBeStored = true;
                        String completedOrNot = null;

                        String name[] = filename.split("\\.",2);

                        System.out.println(name[0] + ", " + name[1]);


                        String tempFileName = new String(Integer.toString(Server.fileID )+ "." + name[1]) ;

                        System.out.println("Temp file : " + tempFileName + " created!");


                        //File file = new File(tempFileName);
                        Path filePath = Paths.get(tempFileName);

                        try
                        {
                            fos = new FileOutputStream(tempFileName);
                        }
                        catch (FileNotFoundException e)
                        {
                            e.printStackTrace();
                        }


                        String rawFrame = new String();

                        int iterations = (int)Math.ceil((float)filesize/maxChunkSize) ;
                        int it = 0;


                        System.out.println("\n" +iterations + "-total iterations!\n");

                        int rand_it = 0;
                        boolean receive_again = false;
                        info get_info = new info();


                        if(error_or_not.equals("Y"))
                        {
                            rand_it = rand.nextInt((int)iterations/2) + 1;
                            System.out.println("Error will occur at " +rand_it + "th iteration!\n");
                        }

                        try
                        {
                            //while ((read = connection.read(buffer, 0, Math.min(buffer.length, remaining))) > 0)
                            while(true)
                            {
                                if(iterations <= 0)break;

                                if(receive_again == false)
                                {
                                    it++;

                                    System.out.println("\n\n" + it + "-th iteration!\n");

                                }
                                else
                                {
                                    System.out.println("\nReceiving the frame again! :( \n");
                                }


                                rawFrame = connection.read().toString();
                                get_info = null;


                                if(receive_again == true)
                                {
                                    get_info = getPayload(rawFrame,error_or_not,it,rand_it, false);
                                    receive_again = false;
                                }
                                else
                                {
                                    get_info = getPayload(rawFrame,error_or_not,it,rand_it, true);
                                }


                                totalRead += get_info.read;
                                remaining -= get_info.read;

                                System.out.println("\nTotal read : " + totalRead + " bytes!");



                                if(get_info.send_ack == true)
                                {
                                    //send ack frame
                                    try
                                    {
                                        fos.write(get_info.payload, 0, get_info.read);
                                    }
                                    catch (IOException e)
                                    {
                                        e.printStackTrace();
                                    }

                                    String ack_frame = prepareFrame("00000000", 0, get_info.seq_no, "1", null,0,0);

                                    connection.write(ack_frame);

                                }
                                else
                                {
                                    System.out.println("\nERROR IN FRAME!\n");
                                }



                                if(connection.read().toString().equals("0"))
                                {

                                    System.out.println("User sent a timeout message!");
                                    System.out.println("Again going to read your frame!");
                                    iterations++;
                                    receive_again = true;

                                }



                                rawFrame = null;
                                rawFrame = new String();

                                iterations--;

                                //System.out.println("Total read : " + tot_read);

                            }

                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                            connection.close();
                            System.out.println("User is willing to log out!");
                            Server.clientList.remove(user);
                            shouldBeStored = false;
                            success = false;
                            breakLoop = true;
                        }


                        if(success == true)
                        {
                            //Receiving confirmation
                            completedOrNot = connection.read().toString();
                            if(completedOrNot.equals("1"))System.out.println("\nSender says it's completed!");
                            if(completedOrNot.equals("1"))success = true;
                            else success = false;
                        }


                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        if(success == true && shouldBeStored == true)
                        {
                            //connection.write("1"); //File transfer was successful!
                            Server.fileMap.put(Server.fileID, new FileInfo(Server.fileID, filename, tempFileName, user, username, totalRead, maxChunkSize));
                            Server.receivers.add(username);
                            //Server.receiverAndFileID.put(username,Server.fileID);
                            System.out.println("\nFile saved successfully!");
                        }
                        else
                        {
                            //connection.write("An error occurred during transmission. You got logged out or something else happened! Please try again.");
                            System.out.println("Error occurred! User logged out or sent a timeout message!");
                            try {
                                Files.delete(filePath);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }



                    }
                    else
                    {
                        connection.write("Not enough space!");
                    }

                }
                else
                {
                    System.out.println("User not found!");
                    connection.write("User not found!");
                }

            }
            else if(command.equals("N"))
            {

                //send all the files to the corresponding users

                if(!Server.receivers.contains(user))
                {
                    connection.write("Sorry no messages to display! ");
                    System.out.println("No messages to deliver!");
                }
                else
                {
                    int totalFiles = 0;


                    for(Integer key : Server.fileMap.keySet())
                    {
                        System.out.println("\nID : " + key);
                        FileInfo fileInfo = Server.fileMap.get(key);
                        if(fileInfo.receiver.equals(user))
                        {
                            totalFiles++;
                        }
                    }

                    connection.write(totalFiles);

                    for(Integer key : Server.fileMap.keySet())
                    {

                        System.out.println("Going through fileId " + key);

                        FileInfo fileInfo = Server.fileMap.get(key);

                        //check if this one is the right receiver
                        if(fileInfo.receiver.equals(user))
                        {
                            Path filePath = Paths.get(fileInfo.tempFile);

                            System.out.println("Trying to send file to " + fileInfo.receiver);

                            //Information information = Server.clientList.get(fileInfo.receiver);

                            connection.write(fileInfo.sender + "-" + fileInfo.mainFile + "-" + fileInfo.fileSize
                                    + "-" + fileInfo.supportedMaxChunk + "-Receive it? Y/N : ");

                            String permission = connection.read().toString();

                            if(permission.equals("Y"))
                            {

                                System.out.println("\nUser approved to download the file!");

                                File myFile = new File(fileInfo.tempFile);


                                byte[] buffer = new byte[fileInfo.supportedMaxChunk];

                                try
                                {
                                    fis = new FileInputStream(myFile);
                                }
                                catch (FileNotFoundException e)
                                {
                                    e.printStackTrace();
                                }

                                bis = new BufferedInputStream(fis);

                                int read = 0;
                                int totalRead = 0;
                                //int remaining = fileInfo.fileSize;
                                int remaining = fileInfo.fileSize;
                                boolean success = true;
                                int seq_no = 0;
                                int ack_no = 0;
                                String frame = new String();

                                int it = 0;
                                System.out.println("\n Buffer in String : " + new String(buffer));

                                try
                                {
                                    //connection.write("\nHi from server\n");


                                    //while ((read = fis.read(buffer, 0, Math.min(buffer.length, remaining))) > 0)
                                    while((read = bis.read(buffer, 0, Math.min(buffer.length, remaining))) > 0)
                                    {
                                        it++;
                                        //System.out.println("\n Buffer in String : " + new String(buffer));
                                        if(it%2 == 0)seq_no = 0;
                                        else seq_no = 0;


                                        totalRead += read;
                                        remaining -= read;

                                        System.out.println("\nTotal Read : " + totalRead + " and Remaining : " + remaining + " bytes!");

                                        //System.out.println("\nBuffer in String : " + new String(buffer));


                                        frame = prepareFrame("11111111", seq_no, ack_no, null, buffer, 0, fileInfo.supportedMaxChunk);

                                        connection.write(frame);

                                        //System.out.println("\nBuffer : " + Arrays.toString(buffer));

                                        //System.out.println("\n Buffer in String : " + new String(buffer));


                                        //System.out.println("\nSuccessfully sent " + read + " bytes");

                                        buffer = null;
                                        buffer = new byte[fileInfo.supportedMaxChunk];
                                        frame = null;
                                        frame = new String();

                                        //System.out.println("User says : " + connection.read().toString());


                                    }



                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace();
                                    System.out.println("\nUser logged out!");
                                    connection.close();
                                    Server.clientList.remove(user);
                                    success = false;
                                    breakLoop = true;
                                    break;
                                }

                                if(success == true)
                                {
                                    System.out.println("\nFile transferd successfully!");
                                    connection.write("You received the file successfully!");
                                }
                                else
                                {
                                    System.out.println("\nUser logged out!");
                                    connection.write("You cancelled the transmission!");
                                }


                            }
                            else
                            {
                                System.out.println("\nUser denied to receive file!");
                                connection.write("Your file is deleted!");

                            }


                            //delete after transmission
                            //also remove from receivers and filemap and receiversAndFile
                            try
                            {
                                //bis.close();
                                fis.close();
                                Files.delete(filePath);
                                Server.fileMap.remove(fileInfo.fileId);
                                Server.receivers.remove(user);
                                //Server.receiverAndFileID.remove(user);

                            }
                            catch (IOException e)
                            {
                                e.printStackTrace();
                            }

                        }

                    }

                    //connection.write("No more messages!");


                }


            }
            else if(command.equals("Q"))
            {
                Server.clientList.remove(user);
                connection.write("Logging out...");
            }


            if(breakLoop == true)
            {
                connection.write("Sorry you cannot send or receive more files! Log in again and then send file. Thank you!");
                System.out.println("Done with user!");
                break;
            }

        }


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

            System.out.println("Preparing frame : ");
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





