/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serverside;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import static java.lang.System.exit;
import java.math.BigInteger;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class ServerThread implements Runnable{
    private Socket ClientSock;
    private Thread thr;
    private HashMap <String, Socket> students;
    private HashMap <String, FileInfo> files;
    private String student_id;
    private int id;
    long max_size;
    
    ServerThread(Socket client, int id, HashMap <String,Socket> students, HashMap <String,FileInfo> files, long max_size) 
    {
	this.ClientSock = client;
        this.id = id;
        this.students = students;
        this.files = files;
        this.max_size = max_size;
	this.thr = new Thread(this);
	thr.start();
    }
    @Override
    public void run() {
        
        try
        {
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(ClientSock.getInputStream()));    
            DataOutputStream outToClient = new DataOutputStream(ClientSock.getOutputStream());
            
            while(true)
            {
                outToClient.writeBytes("Enter Student ID" + '\n');
                student_id = inFromClient.readLine();

                if(students.containsKey(student_id))
                {
                   System.out.println("Student with ID "+student_id+" already logged in\n");
                   outToClient.writeBytes("login denied" + '\n');
                   continue;
                }
                else
                {
                   students.put(student_id, ClientSock);
                   outToClient.writeBytes("You have been logged in" + '\n');
                   String text="Client " + Integer.toString(id) + " has logged in with ID: "+student_id+"\n";
                   System.out.println(text);
                   break;
                }
            }
            System.out.println("Current student list:\n");

            for (String key : students.keySet()) {
                System.out.println("Client "+ key +": Port " + students.get(key).getPort());
            }
            
            while (true)
            {
                String message = inFromClient.readLine();
                
                if(message.equalsIgnoreCase("l"))
                {
                    System.out.println("Client "+Integer.toString(id)+" with student ID "+student_id+" has logged out.\n");
                    outToClient.writeBytes("You have been logged out" + '\n');
                    students.remove(student_id);

                    for (String key : files.keySet()) 
                    {  
                        FileInfo rcvr = files.get(key);
                        if(rcvr.get_rcvr_id() == Integer.parseInt(student_id))
                        {
                            int lastDot = key.lastIndexOf('.'); //add 1 to server file
                            String s = key.substring(0,lastDot) + "1" + key.substring(lastDot);
                            //System.out.println(s);
                            
                            File f = new File (s);
                            f.delete();
                            
                        }
                    }
                    
                    for(Iterator<Map.Entry<String, FileInfo>> it = files.entrySet().iterator(); it.hasNext(); )
                    {
                        Map.Entry<String, FileInfo> entry = it.next();
                        if(entry.getValue().get_rcvr_id() == Integer.parseInt(student_id))
                        {
                            it.remove();
                        }    
                    }
                    
                    for (String key : files.keySet()) 
                    {
                        System.out.println("list of temp files:" + key + '\n');
                    }
                    ClientSock.close();
                }
                else if(message.equalsIgnoreCase("f"))
                {
                    System.out.println("Client "+student_id+" has requested file transfer.\n");

                    outToClient.writeBytes("Specify receiver id" + '\n');
                    String rcvr_id = inFromClient.readLine();
                    
                    if (students.containsKey(rcvr_id))
                    {
                        outToClient.writeBytes("Specify file_name" + '\n');
                        String file_name = inFromClient.readLine();

                        //outToClient.writeBytes("Specify file size" + '\n');
                        String file_size = inFromClient.readLine();

                        System.out.println("From Client "+student_id+" file " + file_name + " of size " + file_size + " to Client " + rcvr_id + "\n");

                        //check overflow
                        long total_size = 0;
                        for (String key : files.keySet()) {
                            
                            total_size = total_size + files.get(key).get_file_size();
                        }
                        System.out.println("current server total size: " + total_size + '\n');
                        long new_size = total_size + Long.parseLong(file_size);
                        System.out.println("total size with new file: " + new_size + '\n');
                        
                        if (new_size <= max_size)
                        {
                            outToClient.writeBytes("Send" + '\n');
                            //Random rand = new Random();
                            //long q = Long.parseLong(file_size);
                            //int chunk_size = rand.nextInt(1024-1) + 1;
                            
                            //System.out.println("chunk size to client : " + chunk_size + '\n');
                            //outToClient.writeBytes(Integer.toString(chunk_size) + '\n');
                            
                            String m = inFromClient.readLine();
                            int payload_size = Integer.parseInt(m);

                            int lastDot = file_name.lastIndexOf('.'); //add 1 to server file
                            String s = file_name.substring(0,lastDot) + "1" + file_name.substring(lastDot);
                            System.out.println(s);

                            File file = new File (s);
                            file.createNewFile();
                            //System.out.println("File name: "+file.getName() + " File size: " + file.length() + '\n');

                            OutputStream out = new FileOutputStream(file);
                            InputStream in = ClientSock.getInputStream();
                            OutputStream new_out = ClientSock.getOutputStream();

                            int len = 0;
                            int chunk_count = 1;
                            int rcvd = 0;
                            int res_flag = 0;
                            
                            int expected_seqNo = 0;
                            
                            try{
                                while(rcvd != Long.parseLong(file_size))
                                {
                                    
                                    String s_left = inFromClient.readLine();
                                    int stuffToRemove = Integer.parseInt(s_left);  
                                    outToClient.writeBytes("gotstufftoremove" + '\n');
                                    
                                    String f_size = inFromClient.readLine();
                                    int frame_size = Integer.parseInt(f_size);  
                                    outToClient.writeBytes("gotframesize" + '\n');
                                    
                                    byte [] buffer = new byte [frame_size];
                                    
                                    String len_size = inFromClient.readLine();
                                    int p_size = Integer.parseInt(len_size);
                                    outToClient.writeBytes("gotlen_size" + '\n');
                                    
                                    String lost_msg = inFromClient.readLine();
                                    
                                    if(lost_msg.equalsIgnoreCase("lost"))
                                    {
                                        //outToClient.writeBytes("ok" + '\n');
                                        
                                        String t = inFromClient.readLine();
                                        //System.out.println("waiting " + t + '\n');
                                        
                                        if(t.equalsIgnoreCase("timeout"))
                                        {
                                            outToClient.writeBytes("send" + '\n');
                                        }
                                        
                                        len = in.read(buffer);
                                        
                                        System.out.println("Receiving retransmitted msg\n");
                                        
                                        String wholeframe1 = ByteArrayToString(buffer);
                                        System.out.println("frame received: " + wholeframe1);

                                        String extra_removed1 = RemoveExtraBits(wholeframe1,stuffToRemove);
                                        System.out.println("extra bits removed: " + extra_removed1);

                                        String stuffed1 = RemoveFrameFlags(extra_removed1);
                                        System.out.println("flag removed: " + stuffed1);

                                        String framebits1 = DeStuff(stuffed1);
                                        System.out.println("destuffed bits: " + framebits1); 

                                        byte [] new_b = StringToByteArray(framebits1);
                                        byte [] new_payload = new byte [payload_size]; //may have garbage
                                        
                                        int k=0;
                                        for (int i=3;i<new_b.length-1;i++)
                                        {
                                            new_payload[k] = new_b[i];
                                            k++;
                                        }
                                        
                                        rcvd = rcvd + p_size;
                                        System.out.println("rcvd retransmitted msg\n");
                                        out.write(new_payload,0,p_size); // writes only the p_size read by client
                                        
                                        expected_seqNo = 1 - expected_seqNo;
                                        
                                        outToClient.writeBytes("regot" + '\n');
                                        continue;
                                        
                                    }
                                    else
                                    {
                                        outToClient.writeBytes("well" + '\n');
                                    }
                                     
                                    len = in.read(buffer);
                                    
                                    String wholeframe = ByteArrayToString(buffer);
                                    System.out.println("frame received: " + wholeframe);
                                    
                                    String extra_removed = RemoveExtraBits(wholeframe,stuffToRemove);
                                    System.out.println("extra bits removed: " + extra_removed);
                                    
                                    String stuffed = RemoveFrameFlags(extra_removed);
                                    System.out.println("flag removed: " + stuffed);
                                    
                                    String framebits = DeStuff(stuffed);
                                    System.out.println("destuffed bits: " + framebits); 
                                    
                                    byte [] b = StringToByteArray(framebits);
                                    int kind = b[0];
                                    int seqNo = b[1];
                                    int ackNo = b[2];
                                    byte checksum = b[b.length-1];

                                    System.out.println("kind: " + kind + " seqNo: " + seqNo + " ackNo: " + ackNo + " checksum: " + ToBinaryString(checksum) + '\n');
                                    
                                    byte [] for_check = new byte [b.length-1];
                                    for(int i=0;i<for_check.length;i++)
                                    {
                                        for_check[i] = b[i];
                                    }
                                    
                                    boolean checksum_result = VerifyCheckSum (for_check,checksum);

                                    byte [] payload = new byte [payload_size]; //may have garbage
                                    int j=0;
                                    for (int i=3;i<b.length-1;i++)
                                    {
                                        payload[j]=b[i];
                                        j++;
                                    }
                                    
                                    String pload = ByteArrayToString(payload);
                                    System.out.println("payload: " + pload);
                                    
                                    if(checksum_result == true)
                                    {
                                        rcvd = rcvd + p_size;
                                        //System.out.println("rcvd: " + rcvd + '\n');
                                        out.write(payload,0,p_size); // writes onlye the p_size read by client
                                        System.out.println("Receiving seqNo " + expected_seqNo + '\n');                                                                               
                                        
                                        chunk_count++;                             
                                        
                                        String ack = MakeFrame(1,2,expected_seqNo,0);
                                        byte [] ack_byte = StringToByteArray(ack);
                                        //outToClient.writeBytes("next?" + '\n');
                                        
                                        expected_seqNo = 1 - expected_seqNo;
                                        
                                        new_out.write(ack_byte,0,ack_byte.length);
                                        System.out.println("Sending positive ack: " + ack + '\n');
                                        
                                        String get = inFromClient.readLine();
                                        System.out.println("timeout or ok msg: " + get + '\n');
                                        
                                        if (get.equalsIgnoreCase("ok"))
                                        {
                                           outToClient.writeBytes("hmm" + '\n');
                                        }
                                        else if(get.equalsIgnoreCase("timeout"))
                                        {
                                            outToClient.writeBytes("hmm" + '\n');
                                            //System.out.println("hmm sent\n");
                                            int new_len = in.read(buffer);
                                            outToClient.writeBytes("already got" + '\n');
                                        }
                                        
                                    } 
                                    else
                                    {
                                        String ack = MakeFrame(1,2,expected_seqNo,1);
                                        byte [] ack_byte = StringToByteArray(ack);
                                        //outToClient.writeBytes("next?" + '\n');
                                        
                                        new_out.write(ack_byte,0,ack_byte.length);
                                        System.out.println("Sending negative ack: " + ack + '\n');
                                        
                                        String msg = inFromClient.readLine();
                                        //System.out.println(msg);

                                        if (msg.equalsIgnoreCase("timeout"))
                                        {
                                            res_flag = 1;
                                            //System.out.println("Client sent timeout\n");
                                            outToClient.writeBytes("sendagain" + '\n');
                                            
                                        }
                                        else if (msg.equalsIgnoreCase("ok"))
                                        {
                                            outToClient.writeBytes("got" + '\n');
                                        }
                                            
                                        int new_len = in.read(buffer);
                                        System.out.println("Receiving retransmitted msg\n");
                                        
                                        String wholeframe1 = ByteArrayToString(buffer);
                                        System.out.println("frame received: " + wholeframe1);

                                        String extra_removed1 = RemoveExtraBits(wholeframe1,stuffToRemove);
                                        System.out.println("extra bits removed: " + extra_removed1);

                                        String stuffed1 = RemoveFrameFlags(extra_removed1);
                                        System.out.println("flag removed: " + stuffed1);

                                        String framebits1 = DeStuff(stuffed1);
                                        System.out.println("destuffed bits: " + framebits1); 

                                        byte [] new_b = StringToByteArray(framebits1);
                                        byte [] new_payload = new byte [payload_size]; //may have garbage
                                        
                                        int k=0;
                                        for (int i=3;i<new_b.length-1;i++)
                                        {
                                            new_payload[k] = new_b[i];
                                            k++;
                                        }
                                        
                                        rcvd = rcvd + p_size;
                                        System.out.println("rcvd retransmitted msg\n");
                                        out.write(new_payload,0,p_size); // writes onlye the p_size read by client
                                        
                                        expected_seqNo = 1 - expected_seqNo;
                                        
                                        outToClient.writeBytes("regot" + '\n');
                                        
                                    }  

                                }
                                //System.out.println("out from while loop\n");
                                out.close();
                                
                            }
                            catch(Exception e)
                            {
                                System.out.println("Sender disconnected, transmission failed\n" + e);
                                
                                out.close();
                                
                                int lastDot2 = file_name.lastIndexOf('.'); //add 1 to server file
                                String h = file_name.substring(0,lastDot2) + "1" + file_name.substring(lastDot2);
                                //System.out.println(h);

                                File f = new File(h);
                                f.delete();
                            }
                            
                            
                            String completion_msg = inFromClient.readLine();
                            System.out.println(completion_msg + '\n');
                            if (completion_msg.equalsIgnoreCase("done"))
                            {
                                if(rcvd == Long.parseLong(file_size))
                                {
                                    outToClient.writeBytes("success" + '\n');
                                    System.out.println("Received all chunks of file name: " + file.getName() + "File size: " + file.length() +" successfully" + '\n');
                                    FileInfo fileinfo = new FileInfo (Long.parseLong(file_size),Integer.parseInt(student_id),Integer.parseInt(rcvr_id));

                                    if(!files.containsKey(file_name))
                                    {
                                        //System.out.println("yo\n");
                                        files.put(file_name,fileinfo);
                                    }

                                    System.out.println("Storing: " + file_name + "," + files.get(file_name).get_file_size() + "," + files.get(file_name).get_sender_id() + "," + files.get(file_name).get_rcvr_id() + '\n');

                                    for (String key : files.keySet()) {

                                        System.out.println("File "+ key +": rcvr id " + files.get(key).get_rcvr_id());
                                    }
                                }
                                else
                                {
                                    outToClient.writeBytes("error" + '\n');
                                    System.out.println("Error in receiving chunks\n");
                                    file.delete();
                                }
                            }
                            else
                            {
                                System.out.println("All chunks not received\n");
                                file.delete();
                            }
                        }
                        else
                        {
                            System.out.println("Server maximum chunk size reached, cannot transfer file.\n");
                            outToClient.writeBytes("limit reached" + '\n');
                        }    
                    }
                    else
                    {
                        outToClient.writeBytes("Receiver not online" + '\n');
                    }
                }
                
                else if(message.equalsIgnoreCase("r"))
                {   
                    System.out.println("Current student list:\n");

                    for (String key : students.keySet()) {
                        System.out.println("Client "+ key +": Port " + students.get(key).getPort());
                    }
                    int file_flag = 0;
                    for (String key : files.keySet()) 
                    {  
                        FileInfo rcvr = files.get(key);
                        if(rcvr.get_rcvr_id() == Integer.parseInt(student_id))
                        {
                            file_flag = 1; 
                            outToClient.writeBytes("Receive file?" + '\n');
                            String m = inFromClient.readLine();
                            if(m.equalsIgnoreCase("From?"))
                            {
                                outToClient.writeBytes(Integer.toString(rcvr.get_sender_id()) + '\n');
                            }
                            String n = inFromClient.readLine();
                            if(n.equalsIgnoreCase("Size?"))
                            {
                                outToClient.writeBytes(Long.toString(rcvr.get_file_size()) + '\n');
                            }
                            
                            String p = inFromClient.readLine();
                            if(p.equalsIgnoreCase("Name?"))
                            {
                                outToClient.writeBytes(key + '\n');
                            }

                            String answer = inFromClient.readLine();
                            if(answer.equalsIgnoreCase("yes"))
                            {
                                int lastDot = key.lastIndexOf('.'); //add 1 to server file
                                String s = key.substring(0,lastDot) + "1" + key.substring(lastDot);
                                System.out.println(s);
                                
                                InputStream datain = new FileInputStream(s);
                                OutputStream dataout = ClientSock.getOutputStream();
                                byte [] buf = new byte [1024];
                                int len1 = 0;
                                int chunk_count1 = 1;
                                try{
                                    while((len1 = datain.read(buf)) != -1)
                                    {
                                        dataout.write(buf,0,len1);
                                        System.out.println("Sending chunk " + chunk_count1 + '\n');
                                        chunk_count1++;
                                    }
                                    System.out.println("File sent to student " + rcvr.get_rcvr_id() + '\n');
                                    files.remove(key);
                                    datain.close();
                                    File f = new File (s);
                                    f.delete();
                                }
                                catch(Exception e)
                                {
                                    System.out.println("Receiver disconnected, transmission failed\n"); 
                                    datain.close();
                                    
                                    int lastDot2 = key.lastIndexOf('.'); //add 1 to server file
                                    String h = key.substring(0,lastDot2) + "1" + key.substring(lastDot2);
                                    //System.out.println(h);
                                    
                                    File f = new File(h);
                                    f.delete();
                                    
                                    int lastDot1 = key.lastIndexOf('.'); //add 2 to server file
                                    String g = key.substring(0,lastDot1) + "2" + key.substring(lastDot1);
                                    //System.out.println(g);

                                    File f1 = new File(g);
                                    f1.delete();
                                    
                                    for(Iterator<Map.Entry<String, FileInfo>> it = files.entrySet().iterator(); it.hasNext(); )
                                    {
                                        Map.Entry<String, FileInfo> entry = it.next();
                                        if(entry.getValue().get_rcvr_id() == Integer.parseInt(student_id))
                                        {
                                            it.remove();
                                        }    
                                    }  
                                    
                                    for (String key1 : files.keySet()) 
                                    {
                                        System.out.println("list of temp files:" + key1 + '\n');
                                    }
                                    break;
                                }
                            }
                            else
                            {
                                System.out.println("Client refused to receive file\n");
                                continue;
                            }
                        }
                    }
                    if (file_flag == 0)
                    {
                        outToClient.writeBytes("You have no files to receive" + '\n');
                    }
                }
                else
                {
                    continue;
                }

            }
        }
        catch(Exception e)
        {
            students.remove(student_id);
            System.out.println("Student with ID " + student_id + " disconnected\n");
            System.out.println("Current student list:\n");
            for (String key : students.keySet()) {
                System.out.println("Client "+ key +": Port " + students.get(key).getPort());
            }
            
            for (String key : files.keySet()) 
            {  
                FileInfo rcvr = files.get(key);
                if(rcvr.get_rcvr_id() == Integer.parseInt(student_id))
                {
                    int lastDot = key.lastIndexOf('.'); //add 1 to server file
                    String s = key.substring(0,lastDot) + "1" + key.substring(lastDot);
                    //System.out.println(s);

                    File f = new File (s);
                    f.delete();

                }
            }
            
            for(Iterator<Map.Entry<String, FileInfo>> it = files.entrySet().iterator(); it.hasNext(); )
            {
                Map.Entry<String, FileInfo> entry = it.next();
                if(entry.getValue().get_rcvr_id() == Integer.parseInt(student_id))
                {
                    it.remove();
                }    
            }
            
            for (String key : files.keySet()) 
            {
                System.out.println("list of temp files:" + key + '\n');
            }
        }  
    }
    
    private String ToBinaryString(byte buffer) {
   
        String s  = String.format("%8s", Integer.toBinaryString(buffer & 0xFF)).replace(' ', '0');
        return s;
    }
    
    private String ByteArrayToString(byte [] buffer){
        
        String payload = "";
        for(int i=0;i<buffer.length;i++)
        {
            String s = ToBinaryString(buffer[i]);
            payload = payload + s;
        }
        return payload;
    }
    
    private String MakeFrame(int kind, int seqNo, int ackNo, int payload) {
        
        byte b;
        String s;
        String frame = "";
                
        b = (byte) kind;
        s  = ToBinaryString(b);
        System.out.println("kind: " + s + "\n");
        frame += s;
        
        b = (byte) seqNo;
        s = ToBinaryString(b);
        System.out.println("seqNo: " + s + "\n");
        frame += s;
        
        b = (byte) ackNo;
        s = ToBinaryString(b);
        System.out.println("ackNo: " + s + "\n");
        frame += s;
        
        b = (byte) payload;
        s = ToBinaryString(b);
        System.out.println("payload for ack: " + s + "\n");
        frame += s;
        
        //System.out.println(frame + "\n");
        return frame;
    }

    private String RemoveFrameFlags(String wholeframe) {
        
        String stuffed = wholeframe.substring(8,wholeframe.length()-8);
        return stuffed;
    }

    private String DeStuff(String bits) {
        
        StringBuilder str = new StringBuilder (bits);
        int count = 0;
        int length = str.length();
        
        for (int i =0;i<length;i++)
        {
            if(str.charAt(i) == '0')
            {
                count = 0;
            }
            else 
            {
                count++;
                if(count == 5)
                {
                    str.deleteCharAt(i+1);
                    length--;
                    count = 0;
                }
            }
        }
        return str.toString();
    }

    private byte[] StringToByteArray(String bits) {
        
        //System.out.println("enter func\n");
        //byte[] result = new byte[size];
        ArrayList <Integer> list = new ArrayList <Integer> ();
        String str;
        int b;
        
        int i = 0;
        
        while(i+8 <= bits.length())
        {            
            str = bits.substring(i,i+8);
            b = Integer.parseInt(str, 2);
            list.add(b);
            i+=8;
            //System.out.println("in loop list\n");           
        }
        //System.out.println("out from loop list\n");
        if(i != bits.length())
        {
            str = bits.substring(i,bits.length());
            b = Integer.parseInt(str, 2);
            list.add(b);
        }
        
        byte[] result = new byte[list.size()];
        for (int j=0;j<list.size();j++)
        {
            result[j] = list.get(j).byteValue();
            //System.out.println("in store to byte array\n");
        }
        //System.out.println("func done\n");
        //System.out.println("after destuff byte len: " + result.length + '\n');
        return result;
    }

    private String RemoveExtraBits(String wholeframe, int stuffToRemove) {
        String s = wholeframe.substring(stuffToRemove,wholeframe.length());
        return s;
    }

    private boolean VerifyCheckSum(byte[] payload, byte checksum) {
        
        byte c = 0;
        for(int i=0;i<payload.length;i++)
        {
            //System.out.println("byte1: " +ToBinaryString(payload[i]) + '\n');
            
            if( (c & 0xFF) > 0xFF - (payload[i] & 0xFF))
            {
                c = (byte) (c + payload[i]);
                
                //System.out.println("calculating checksum " + ToBinaryString(c) + '\n');
                c += (byte) 1;
                
                //System.out.println("After adding 1: " + ToBinaryString(c) + '\n');
            }
            else
            {
                c += payload[i];
                //System.out.println("calculating checksum " + ToBinaryString(c) + '\n');
            }
            
        }
        c += checksum;
        int temp = ~c;
        byte t = (byte) temp;
        System.out.println("checksum result to verify: " + ToBinaryString(t) + '\n');
        
        if(t == 0)
        {
            return true;
        }
        return false;
    }
}
