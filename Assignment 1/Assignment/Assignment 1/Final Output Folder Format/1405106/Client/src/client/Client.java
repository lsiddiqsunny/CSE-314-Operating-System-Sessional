/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.lang.*;
import java.util.Scanner;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.logging.Level;
import java.util.logging.Logger;
 
 
public class Client extends Thread{
    public static int max_size=50000000;
    public final static int FILE_SIZE = 50000000;
    public static Scanner scanner;
    public static final int PORT1 = 3332;
    public static final int PORT2 = 2000;
    
    @Override
    public void run() {
        scanner=new Scanner(System.in);
        //while(true){
        System.out.println("If you want to send file then insert s else insert r:");
            String choose=scanner.nextLine();
            if(choose.equals("s")){
                try {
                    sender();
                } catch (IOException ex) {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ClassNotFoundException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
            }
            else if(choose.equals("r")){
                try {
                    receiver();
                } catch (IOException ex) {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        //}
        
    }
    
    public static void receiver() throws IOException{
        String fileLocation,ipAddress;
        Socket smtpSocket = null;  
        DataOutputStream os = null;
        DataInputStream is = null;
        int portNo;
        String studentID=null;

        scanner=new Scanner(System.in);
        System.out.println("Enter Student ID: ");
        studentID=scanner.next();
        
        
        try {
                smtpSocket=new Socket("localhost",2000);
                os = new DataOutputStream(smtpSocket.getOutputStream());
                is = new DataInputStream(smtpSocket.getInputStream());
            } catch (UnknownHostException e) {
                System.err.println("Don't know about host: hostname");
            } catch (IOException e) {
                System.err.println("Couldn't get I/O for the connection to: hostname");
            }
            
            if (smtpSocket != null && os != null && is != null) {
                try {

                        os.writeUTF("receive:"+studentID+":"+"!!"); 
                        os.close();
                        is.close();
                        smtpSocket.close();   
                } catch (UnknownHostException e) {
                        System.err.println("Trying to connect to unknown host: " + e);
                } catch (IOException e) {
                        System.err.println("IOException:  " + e);
                }
            }
        
        
        
        
        System.out.println("Enter ipAddress of machine :");
        ipAddress=scanner.next();

        System.out.println("Enter port number of machine :");
        portNo=scanner.nextInt();
        System.out.println("Please enter file location with file name to save : ");
        fileLocation=scanner.next();
        receiveFile(ipAddress, portNo, fileLocation);
    }
    
    
    
    public static void receiveFile(String ipAddress,int portNo,String fileLocation) throws IOException
	{
  
            int bytesRead;
            int current = 0;
            FileOutputStream fos = null;
            BufferedOutputStream bos = null;
            Socket sock = null;
            try {
              sock = new Socket("localhost", portNo);
              System.out.println("Connecting...");

              
              byte [] mybytearray  = new byte [FILE_SIZE];
              InputStream is = sock.getInputStream();
              fos = new FileOutputStream(fileLocation);
              bos = new BufferedOutputStream(fos);
              bytesRead = is.read(mybytearray,0,mybytearray.length);
              current = bytesRead;

              do {
                 System.out.println(current);
                 bytesRead =
                    is.read(mybytearray, current, (mybytearray.length-current));
                 if(bytesRead >= 0) current += bytesRead;
              } while(bytesRead > -1);

              bos.write(mybytearray, 0 , current);
              bos.flush();
              
            }
            finally {
              if (fos != null) fos.close();
              if (bos != null) bos.close();
              if (sock != null) sock.close();
            }
        }
    
    
    
    public static String toBinary( byte[] bytes )
    {
        StringBuilder sb = new StringBuilder(bytes.length * Byte.SIZE);
        for( int i = 0; i < Byte.SIZE * bytes.length; i++ )
            sb.append((bytes[i / Byte.SIZE] << i % Byte.SIZE & 0x80) == 0 ? '0' : '1');
        return sb.toString();
    }
    
    
    static byte[] toBytes(int val, int bufferSize)
    {
        byte[] result = new byte[bufferSize];
        for(int i = bufferSize - 1; i >= 0; i--) {
            result[i] = (byte) (val /*>> 0*/);
            val = (val >> 8);
        }
        return result;
    }
    public static String intToBinary(int s){
        System.out.println("In function1: "+s);
      
        String sCheck=Integer.toBinaryString(s);
        System.out.println("In function2: "+sCheck);
        return sCheck;
    }
    
    public static String addHeadTail(String s){
        String ht="01111110"+s+"01111110";
        return ht;
    }
    
    
    public static String getBits(byte b)
    {

        String s1 = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
        return s1;
    }
    
    public static String checkSum(String s)
    {
        int sum=0;
        for (int i = 0; i < s.length(); i++) {
            if(s.charAt(i)=='1')
            {
                sum=sum+1;
            }
        }
        
        String sCheck=Integer.toBinaryString(sum);
        
       
        return sCheck;
    }
    public static String bitStaffing(String s)
    {
        int counter=0;
        int co=0;
        String res = new String();
        for(int i=0;i<s.length();i++)
        {
            if(s.charAt(i) == '1')
            {
                counter++;
                res = res + s.charAt(i);
            }
            else
            {
                res = res + s.charAt(i);
                counter = 0;
            }
            if(counter == 5)
            {
                res = res + '0';
                co++;
                counter = 0;
            }
        }
        System.out.println("Number of bitstaff: "+co);
        return res;
    }
    
    
    public static byte CalculateCheckSum( byte[] bytes ){
         byte CheckSum = 0, i = 0;
         for( i = 0; i < bytes.length; i++){
              CheckSum += (byte)(bytes[i] & 0xFF);
         }
         return CheckSum;
    }
    
    
    public static void sender() throws IOException, ClassNotFoundException{
            int port;
            System.out.println("Enter Sender's Student ID :");
            String senderID=scanner.next();
            System.out.println("Enter Receiver's Student ID :");
            String receiverID=scanner.next();
            System.out.println("Enter ipAddress of machine :");
            String ipAddress=scanner.next();
            System.out.println("Enter Port No:");
            port=scanner.nextInt();
            System.out.println("Enter the name of the file :");
            Socket smtpSocket = null;  
            DataOutputStream os = null;
            DataInputStream is = null;
            String file_name = scanner.next();
            FileOutputStream fos = null;
            
            
            try {
                smtpSocket=new Socket(ipAddress,2000);
                os = new DataOutputStream(smtpSocket.getOutputStream());
                is = new DataInputStream(smtpSocket.getInputStream());
            } catch (UnknownHostException e) {
                System.err.println("Don't know about host: hostname");
            } catch (IOException e) {
                System.err.println("Couldn't get I/O for the connection to: hostname");
            }
            
            if (smtpSocket != null && os != null && is != null) {
				try {
	 
					os.writeUTF("send:"+senderID+":"+receiverID); 
					os.close();
					is.close();
					smtpSocket.close();   
				} catch (UnknownHostException e) {
					System.err.println("Trying to connect to unknown host: " + e);
				} catch (IOException e) {
					System.err.println("IOException:  " + e);
				}
			}
            
            

            File file = new File(file_name);
            Socket socket = new Socket("localhost", port);
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            System.out.println(file.getName());
            oos.writeObject(file.getName());
            Scanner sc=new Scanner(System.in);
            

            FileInputStream fis = new FileInputStream(file);
            byte [] buffer = new byte[1024];
            Integer bytesRead = 0;
            int counter=0;
            int seqNo=0;
            String s=file.getName();

            while ((bytesRead = fis.read(buffer)) > 0) {
                counter=counter+bytesRead;
                if(counter>max_size){
                    break;
                }
                else{
                    //oos.writeObject(bytesRead);
                    //oos.writeObject(Arrays.copyOf(buffer, buffer.length));
                    
                    seqNo=seqNo+1;
                    
                    String bPattern="";
                    String ss="";
                    //String fromByteToString = String.format("%8s", Integer.toBinaryString(buffer & 0xFF)).replace(' ', '0');
//                    for(byte bit : buffer)
//                    {
//                        
//                        ss=getBits(bit);
//                        bPattern=bPattern+ss;
//                    }
                    for(int k=0;k<bytesRead;k++)
                    {
                        ss=getBits(buffer[k]);
                        bPattern=bPattern+ss;
                    }
                    
                    System.out.println("main String : "+bPattern);

                    
                    byte checkSum=buffer[0];
                    for(int i=1;i<buffer.length;i++){
                        checkSum=(byte)(checkSum ^ buffer[i]);
                    }
                    
                    String csum=getBits(checkSum);
                    System.out.println("Checksum : "+csum);
                    String plcs=bPattern+csum;
                    System.out.println("Checksum+payLoad : "+plcs);
                    String seq_merge=intToBinary(seqNo);
                    String splcs=seq_merge+plcs;
                    System.out.println("Add seq : "+splcs);
                    String bsplcs=bitStaffing(splcs);
                    System.out.println("Bit Staff : "+bsplcs);
                    String add_ht=addHeadTail(bsplcs);
                    System.out.println("Adding head tail : "+add_ht);
                    System.out.println("Want to change data? if yes then enter 'y' else enter 'n': ");
                    String choice=sc.next();
                    if(choice.equals("y")){
                        System.out.println("Enter position between 0 - "+add_ht.length());
                        int pos=sc.nextInt();
                        System.out.println("replace with: ");
                        char c = sc.next().charAt(0);
                        StringBuilder myName = new StringBuilder(add_ht);
                        myName.setCharAt(pos, c);
                        System.out.println("builder :"+myName);
                        System.out.println("builder ohh: "+myName.toString());
                        add_ht=myName.toString();
                    }
                    
                    
                    byte[] array=new byte[add_ht.length()];
                    for (int i=0; i<add_ht.length(); i++) {
                        array[i]= add_ht.charAt(i)=='1' ? (byte)1 : (byte)0;
                    }
                    
//                    
//                    byte[] array = add_ht.getBytes(StandardCharsets.UTF_8);
                    String pp="";
                    String ll="";
                    //System.out.println("Here we go : ");
                    for(int i=0;i<array.length;i++)
                    {
                        System.out.print(array[i]);
                    }
                    System.out.println();
                    
                    //System.out.println(ll.length());
                    System.out.println("array length : "+array.length);
                    oos.writeObject(array.length);
                    //oos.writeObject(array);
                    oos.writeObject(Arrays.copyOf(array,array.length));
                    
                    Object o = ois.readObject();
                    int i=(Integer)o;
                    System.out.println("ackn: "+i);
                    
                    if(i==1){
                        System.out.println("Chunk sent");
                    }
                    else{
                        System.out.println("Chunk corrupted!! file sending failed");
                        break;
                        
                    }
                    
                    
                }


            }

            oos.close();
            ois.close();
            //fos.close();
            socket.close();
            //System.exit(0);    
    }
    
    public static void main(String[] args) throws Exception {
        new Client().start();
       
    }
 
}
 


