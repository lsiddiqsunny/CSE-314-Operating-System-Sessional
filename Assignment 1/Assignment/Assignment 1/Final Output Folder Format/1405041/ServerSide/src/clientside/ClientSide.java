/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientside;

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
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Timer;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ClientSide {
    
    public String sentence;
    public DataOutputStream outToServer;
    public BufferedReader inFromServer;
    public Socket clientSocket;
    public int stuff_num;
    public int res_flag;
    
    ClientSide()
    {
        try {
            clientSocket = new Socket("localhost", 33333);
        } catch(Exception e) {
            System.out.println("Exception in client start "+e);
        }
        try {
            outToServer = new DataOutputStream(clientSocket.getOutputStream());
        } catch (Exception e) {
            System.out.println("Exception in client outstream "+e);
        }
        try {
            inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (Exception e) {
            System.out.println("Exception in client instream "+e);
        }
        try {
            Scanner scan = new Scanner(System.in);
            while(true)
            {
                sentence = inFromServer.readLine();
                System.out.println("From Server : "+sentence+"\n");
                String student_id = scan.nextLine();
                outToServer.writeBytes(student_id + '\n');
                sentence = inFromServer.readLine();
                System.out.println("From Server : "+sentence+"\n");
                if(sentence.equalsIgnoreCase("login denied"))
                {
                    System.out.println("Already logged in\n");
                    continue;
                }
                else
                {
                    break;
                }
            }
            while(true)
            {
                System.out.println("Enter message (f to send file, r to receive, l to logout) :\n");
                String message = scan.nextLine();
                if(!message.equalsIgnoreCase("l") && !message.equalsIgnoreCase("r") && !message.equalsIgnoreCase("f"))
                {
                    continue;
                }
                outToServer.writeBytes(message + '\n');
                sentence = inFromServer.readLine();
                System.out.println("From Server : "+sentence+"\n");
                if(sentence.equalsIgnoreCase("You have been logged out"))
                {
                    exit(0);
                }
                else if(sentence.equalsIgnoreCase("Specify receiver id"))
                {
                    String rcvr_id = scan.nextLine();
                    outToServer.writeBytes(rcvr_id + '\n');
                    
                    sentence = inFromServer.readLine();
                    if (sentence.equalsIgnoreCase("Specify file_name"))
                    {
                        System.out.println("From Server : "+sentence+"\n");
                        File file;
                        while(true)
                        {
                            String str = scan.nextLine();
                            file = new File(str);

                            if(file.exists())
                            {
                                break;
                            }
                            else
                            {
                                System.out.println("Enter valid name\n");
                            }
                        }
                        
                        String file_name = file.getName();
                        outToServer.writeBytes(file_name + '\n');

                        long file_size = file.length();
                        outToServer.writeBytes(String.valueOf(file_size) + '\n');

                        sentence = inFromServer.readLine();
                        System.out.println("From Server : "+sentence+"\n");

                        if(sentence.equalsIgnoreCase("Send"))
                        {
                            //sentence = inFromServer.readLine();
                            //int chunk_size = Integer.parseInt(sentence);
                            //System.out.println("chunk size from server : "+ chunk_size + '\n');
                            
                            int payload_size = 20; // function of file size
                            outToServer.writeBytes(String.valueOf(payload_size) + '\n');
                           
                            FileInputStream in = new FileInputStream(file);
                            OutputStream out = clientSocket.getOutputStream();
                            InputStream new_in = clientSocket.getInputStream();

                            byte [] buffer = new byte [payload_size];
                            int len = 0 ;  
                            int next_seqNo = 0;
                            
                            try{
                                
                                int chunk_count = 1;
                                while((len = in.read(buffer)) != -1)
                                { 
                                    
                                    String payload = ByteArrayToString(buffer);
                                    System.out.println("string buffer: " + payload + "\n");

                                    String framebits = MakeFrame(0,next_seqNo,2,payload);
                                    System.out.println("framebits without checksum and flag: " + framebits + "\n");
                                    
                                    byte [] calc_check = StringToByteArray(framebits);
                                    int checksum = CalculateChecksum(calc_check);
                                    checksum = checksum^0xFF;
                                    
                                    byte check_sum = (byte) checksum;
                                    System.out.println("final checksum: " + ToBinaryString(check_sum) + '\n');
                                    
                                    String checksum_added = AddChecksum(framebits,ToBinaryString(check_sum));
                                    System.out.println("Checksum added: " + checksum_added + '\n');
                                    
                                    String stuffed = BitStuff(checksum_added);
                                    System.out.println("stuffed frame: " + stuffed + "\n");
                                    
                                    String wholeframe = AddFrameFlags(stuffed);
                                    System.out.println("stuffed frame with flag: " + wholeframe + "\n");
                                    
                                    System.out.println("Do you want to change a bit in the frame?\n");
                                    String response = scan.nextLine();
                                    
                                    String after_error_asked;
                                    if(response.equalsIgnoreCase("y"))
                                    {
                                        after_error_asked = IntroduceError(wholeframe);
                                    }
                                    else
                                    {
                                        after_error_asked = wholeframe;
                                    }
                                    
                                    int stuff_left = 8 - (stuff_num % 8);
                                    String extra_added = AddExtraBits(after_error_asked,stuff_left); //replace with after_error_asked
                                    System.out.println("extra added: " + extra_added + '\n');
                                    
                                    String extra_added_stored = AddExtraBits(wholeframe,stuff_left);
                                    
                                    outToServer.writeBytes(String.valueOf(stuff_left) + '\n');
                                    sentence = inFromServer.readLine();
                                    //System.out.println("stuffleft given: " + sentence + '\n');
                                    
                                    byte [] b = StringToByteArray(extra_added);
                                    byte [] stored = StringToByteArray(extra_added_stored);
                                    //System.out.println("converted to byte\n");
                                    
                                    outToServer.writeBytes(String.valueOf(b.length) + '\n');
                                    sentence = inFromServer.readLine();
                                    //System.out.println("framesizegiven: " + sentence + '\n');
                                    
                                    outToServer.writeBytes(String.valueOf(len) + '\n');
                                    sentence = inFromServer.readLine();
                                    //System.out.println("len given: " + sentence + '\n');
                                    
                                    
                                    System.out.println("Do you want to drop the frame?\n");
                                    String response1 = scan.nextLine();
                                    
                                    if(response1.equalsIgnoreCase("y"))
                                    { 
                                        res_flag = 0;
                                        outToServer.writeBytes("lost" + '\n');
                                        
                                        ExecutorService executor = Executors.newCachedThreadPool();
                                        Callable <Object> task = new Callable<Object>() {
                                            public Object call() {
                                                try{
                                                    
                                                    sentence = inFromServer.readLine();
                                                    //System.out.println("Receiving ack\n");

                                                }catch(Exception e)
                                                {
                                                    System.out.println("exception " + e);
                                                }
                                                if (Thread.currentThread().isInterrupted()) {
                                                    System.out.println("Exiting gracefully current thrd");
                                                    res_flag = 1;
                                                    
                                                    return 0;
                                                }
                                                return 1;
                                           }
                                           
                                        };
                                        //System.out.println("hi2\n");
                                        Future<Object> future = executor.submit(task);
                                        try {

                                           Object result = future.get(100, TimeUnit.MILLISECONDS);                  
                                        }
                                        catch (InterruptedException ex){
                                            System.out.println("Exiting gracefully int excptn!");

                                        }
                                        catch (CancellationException e)
                                        {
                                            System.out.println("cancelling gracefully!");
                                        }
                                        catch (TimeoutException ex) {

                                            future.cancel(true);
                                            

                                            System.out.println("Timeout reached!!! " + ex + '\n');
                                            res_flag = 1;                                                                            
                                        }
                                        
                                        if (res_flag == 1)
                                        {
                                            outToServer.writeBytes("timeout" + '\n');
                                            
                                            while(future.isDone() != true)
                                            {
                                                System.out.println("in while loop\n");
                                            }
                                            
                                            //inFromServer.readLine();
                                            //System.out.println("timeout sent\n");

                                            out.write(stored,0,stored.length);
                                            System.out.println("Retransmitting seq no due to timeout: " + next_seqNo + '\n');

                                            String regot = inFromServer.readLine();
                                            //System.out.println(regot + '\n');

                                            next_seqNo = 1 - next_seqNo;
                                            continue;
                                        }
                                        
                                    }
                                    else
                                    {
                                        outToServer.writeBytes("no_lost" + '\n');
                                        sentence = inFromServer.readLine();
                                        
                                        out.write(b,0,b.length);
                                        System.out.println("Sending seqNo: " + next_seqNo + '\n'); 
                                        chunk_count++;
                                    }
 
                                    //System.out.println("byte array length " + b.length);
 
                                    res_flag = 0;
                                    byte [] get_ack = new byte [4];
                                    ExecutorService executor = Executors.newCachedThreadPool();
                                    Callable <Object> task = new Callable<Object>() {
                                        public Object call() {
                                            try{
                                                
                                                new_in.read(get_ack);
                                                System.out.println("Receiving ack\n");
     
                                            }catch(Exception e)
                                            {
                                                System.out.println("exception " + e);
                                            }
                                            if (Thread.currentThread().isInterrupted()) {
                                                System.out.println("Exiting gracefully current thrd");
                                                res_flag = 1;
                                                return null;
                                            }
                                            return 1;
                                       }
                                    };
                                    Future<Object> future = executor.submit(task);
                                    try {
                                        
                                       Object result = future.get(10, TimeUnit.MILLISECONDS);
                                       
                                    }
                                    catch (InterruptedException ex){
                                        System.out.println("Exiting gracefully int excptn!");
                                        
                                    }
                                    catch (CancellationException e)
                                    {
                                        System.out.println("cancelling gracefully!");
                                    }
                                    catch (TimeoutException ex) {
                                        // handle the timeout
                                        while(future.isDone() != true)
                                        {
                                            
                                        }
                                        
                                        System.out.println("Timeout reached!!! " + ex + '\n');
                                        res_flag = 1;                                                                            
                                    }
                                    
                                    if(res_flag != 1)
                                    {                                       
                                        if(get_ack[3] == 1)
                                        {
                                            System.out.println("negative ack received\n");
                                            
                                            outToServer.writeBytes("ok" + '\n');
                                            sentence = inFromServer.readLine();

                                            out.write(stored,0,stored.length);
                                            System.out.println("Retransmitting seq no due to neg ack: " + next_seqNo + '\n');

                                            String regot = inFromServer.readLine();
                                            //System.out.println(regot + '\n');
                                        }
                                        else
                                        {
                                            System.out.println("positive ack received\n");
                                            
                                            outToServer.writeBytes("ok" + '\n');
                                            String get = inFromServer.readLine();
                                        }
                                        next_seqNo = 1 - next_seqNo;
                                    }
                                    else if (res_flag == 1)
                                    {
                                        outToServer.writeBytes("timeout" + '\n');
                                        sentence = inFromServer.readLine();
                                        
                                        out.write(stored,0,stored.length);
                                        System.out.println("Retransmitting seq no due to timeout: " + next_seqNo + '\n');
                                        
                                        String regot = inFromServer.readLine();
                                        //System.out.println(regot + '\n');
                                        
                                        next_seqNo = 1 - next_seqNo;
                                    }
                                                                      
                                }
                                in.close();                              
                                /*if (res_flag == 1)
                                {
                                    System.out.println("server response too late, transfer stopped\n");
                                    continue;
                                }*/
                                
                                outToServer.writeBytes("done" + '\n');
                                
                                String success_msg = inFromServer.readLine();
                                if (success_msg.equalsIgnoreCase("success"))
                                {
                                    System.out.println("All chunks sent to server successfully " + '\n');
                                }
                                else if (success_msg.equalsIgnoreCase("error"))
                                {
                                    System.out.println("Error in receiving chunks\n");
                                }
                        
                            }catch(Exception e){
                                System.out.println(e);
                                continue;
                            }
                        }
                        else if (sentence.equalsIgnoreCase("limit reached"))
                        {
                            System.out.println("Cannot transfer file, server limit reached\n");
                        }
                    }
                    if (sentence.equalsIgnoreCase("Receiver not online"))
                    {
                        System.out.println("From Server : " + sentence + "\n");
                    }
                }
                
                
                else if(sentence.equalsIgnoreCase("Receive file?"))
                {

                    outToServer.writeBytes("From?" + '\n');
                    String from = inFromServer.readLine();
                    System.out.println("From " + from);

                    outToServer.writeBytes("Size?" + '\n');
                    String size = inFromServer.readLine();
                    System.out.println("of size " + size);

                    outToServer.writeBytes("Name?" + '\n');
                    String name = inFromServer.readLine();
                    System.out.println("having name " + name + "?\n");
                    System.out.println("yes/no\n");
                    
                    String answer;
                    while(true)
                    {
                        answer = scan.nextLine();
                        if(answer.equalsIgnoreCase("yes") || answer.equalsIgnoreCase("no"))
                        {
                            break;
                        }
                        else
                        {
                            System.out.println("Enter yes/no\n");
                        }
                    }
                    
                    outToServer.writeBytes(answer + '\n');
                    
                    if(answer.equalsIgnoreCase("no"))
                    {
                        continue;
                    }

                    int lastDot = name.lastIndexOf('.'); //add 2 to server file
                    String s = name.substring(0,lastDot) + "2" + name.substring(lastDot);
                    //System.out.println(s);

                    File file = new File (s);
                    file.createNewFile();
                    OutputStream dataout = new FileOutputStream(file);
                    InputStream datain = clientSocket.getInputStream();
                    byte [] buf = new byte [1024];
                    int len1 = 0;
                    int rcvd = 0;
                    int chunk_count1 = 1;
                    try{
                        while(rcvd != Long.parseLong(size))
                        {
                            len1 = datain.read(buf);
                            rcvd = rcvd + len1;
                            dataout.write(buf,0,len1);
                            System.out.println("Receiving chunk " + chunk_count1 + '\n');
                            chunk_count1++;
                        }
                        dataout.close();
                        System.out.println("all rcvd\n"); 
                    }
                    catch(Exception e )
                    {
                        continue;
                    }
                }

            }
            
        } catch (Exception e) {
            System.out.println("Exception in message transfer "+e);
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

    private String MakeFrame(int kind, int seqNo, int ackNo, String payload) {
        
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
        
        frame += payload;
        
        //System.out.println(frame + "\n");
        return frame;
    }

    private byte CalculateChecksum(byte [] bits) {
        
        byte checksum = 0;
        for(int i=0;i<bits.length;i++)
        {
            //System.out.println("byte1: " +ToBinaryString(bits[i]) + '\n');
            
            if( (checksum & 0xFF) > 0xFF - (bits[i] & 0xFF))
            {
                checksum = (byte) (checksum + bits[i]);
                
                //System.out.println("calculating checksum " + ToBinaryString(checksum) + '\n');
                checksum += (byte) 1;
                
                //System.out.println("After adding 1: " + ToBinaryString(checksum) + '\n');
            }
            else
            {
                checksum += bits[i];
                //System.out.println("calculating checksum " + ToBinaryString(checksum) + '\n');
            }
            
        }
        return checksum;
    }

    private String AddFrameFlags(String framebits) {
        String flag = "01111110";
        String wholeframe = "";
        wholeframe += flag;
        wholeframe += framebits;
        wholeframe += flag;
        return wholeframe;
    }

    private String BitStuff(String bits) {
        StringBuilder str = new StringBuilder (bits);
        int count = 0;
        int c = 0;
        for (int i =0;i< str.length();i++)
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
                    str.insert(i+1,'0');
                    c++;
                }
            }
        }
        stuff_num = c;
        return str.toString();
    }
    
    
    private byte[] StringToByteArray(String bits) {
        //System.out.println("enter func\n");
        //byte[] result = new byte[size];
        ArrayList <Integer> list = new ArrayList <Integer> ();
        String str;
        int b;
        
        int i = 0;
        //int j = 0;
        while(i+8 <= bits.length()){
            
            str = bits.substring(i,i+8);
            b = Integer.parseInt(str, 2);
            list.add(b);
            i+=8;
            //System.out.println("in loop list\n");
            //j++;
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
        //System.out.println("frame to send byte len: " + result.length + '\n');
        return result;
    }
    
    private String AddExtraBits(String wholeframe, int stuff_left) {
        String s = "";
        for(int i=0;i<stuff_left;i++)
        {
            s += '0';
        }
        s += wholeframe;
        return s;
    }
    
    private String AddChecksum(String framebits, String checksum) {
        String s = "";
        s += framebits;
        s += checksum;
        return s;
    }
    
    private String IntroduceError(String s) {
        
        StringBuilder err_msg = new StringBuilder(s);
        if (err_msg.charAt(32) == '0')
        {
            err_msg.setCharAt(32, '1');
        }
        else
        {
            err_msg.setCharAt(32, '0');
        }
        
        System.out.println("Error introduced frame: " + err_msg.toString() + '\n');
        return err_msg.toString();
    }
   
    public static void main(String[] args){
        ClientSide objclient = new ClientSide();
    }
    
}
