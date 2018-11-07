/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package multithreadchatclient;

/**
 *
 * @author Riad Ashraf
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;

/**
 *
 * @author Riad Ashraf
 */
public class MultiThreadChatClient implements Runnable {

    private static Socket clientSocket = null;
    private static PrintStream ToServer = null;
    private static BufferedReader FromServer = null;
    private static DataOutputStream dout = null;
    private static BufferedReader inputLine = null;
    private static boolean closed = false;
    DataInputStream din;
    String userID;

    public static int length(byte[] b) {

        String BitStuff = new String();

        for (int i = 0; i < b.length; i++) {

            String t = Integer.toBinaryString(b[i]);
            String x = String.format("%8s", Integer.toBinaryString(b[i])).replace(' ', '0');

            BitStuff = BitStuff + x;
        }
        //System.out.println(BitStuff);

        // BitStuff = "11111111";
        String bit = "01111110";
        int search = BitStuff.length();
        int find = bit.length();
        int i = -1;
        int k = 0;
        while (i <= (search)) {
            i++;
            if (BitStuff.regionMatches(i, bit, 0, find)) {

                k++;
//                System.out.println(BitStuff.substring(i, i + find));
//                System.out.println("found from" + i + "to" + (i + find));
                BitStuff = BitStuff.substring(0, i + find - 2) + "0" + BitStuff.substring(i + find - 2, BitStuff.length());

                i = i + find;
            }

        }

        return k;
    }

    public static byte[] StuffBit(byte[] b) {

        String BitStuff = new String();

        for (int i = 0; i < b.length; i++) {

            System.out.print(b[i] + "-->");
            System.out.println(Integer.toBinaryString(b[i]));
            String t = Integer.toBinaryString(b[i]);
            String x = String.format("%8s", Integer.toBinaryString(b[i])).replace(' ', '0');

            BitStuff = BitStuff + x;
        }
        System.out.println(BitStuff);

        // BitStuff = "11111111";
        String changedBitStuff = BitStuff;
        String bit = "01111110";
        int search = BitStuff.length();
        int find = bit.length();
        boolean foundIt = false;
        int i = -1;
        while (i <= (search)) {
            i++;
            if (BitStuff.regionMatches(i, bit, 0, find)) {
                foundIt = true;
//                System.out.println(BitStuff.substring(i, i + find));
//                System.out.println("found from" + i + "to" + (i + find));
                BitStuff = BitStuff.substring(0, i + find - 2) + "0" + BitStuff.substring(i + find - 2, BitStuff.length());

                i = i + find;
            }

        }
        if (!foundIt) {
            System.out.println("No BitStuffing Needed");
        }
        System.out.println("Before BitStuffing : " + changedBitStuff);
        System.out.println("After BitStuffing  : " + BitStuff);
//
//        System.out.println("length" + BitStuff.length());
        int len = BitStuff.length();
        int k = 8, j = 0;
//        System.out.println("value of search: " + search);
//        System.out.println(BitStuff.substring(1, len));
        int temp_len;
        //System.out.println("len "+len);
        if (len % 8 == 0) {
            temp_len = len / 8;
        } else {
            temp_len = len / 8 + 1;
        }
        byte temp[] = new byte[temp_len];

//        System.out.println("len/8+1" + (len / 8 + 1));
        int l = 0;
        while (k <= len) {
            String s = new String();

            s = BitStuff.substring(j, k);
            //System.out.println(s);
            int y = Integer.parseInt(s, 2);
            //System.out.println("y :"+y);
            temp[l] = (byte) y;

            // System.out.println("temp value at "+l+" "+Integer.toBinaryString(temp[l]));
            l++;
            //System.out.println("l = " + l);
            j = j + 8;
            k = k + 8;
        }
//        System.out.println("j" + j+"-->"+len);
//        System.out.println(BitStuff.substring(j, len));
        if (len % 8 != 0) {
            temp[l] = (byte) Integer.parseInt(BitStuff.substring(j, len), 2);
        }
//        System.out.println("length of array" + temp.length);
        return temp;
    }

    public static void main(String[] args) throws IOException {
        int portNumber = 5000;

        String host = "localhost";
        /*  if(args.length<2)
            {
                System.out.println("Usage:Java multi thread"+"Now using host:"+host+"port number: "+ portNumber);
            }
            else1
            {
                host= args[0];
                portNumber=Integer.valueOf(args[1]).intValue();
                        
            }
         */
        try {
            MultiThreadChatClient.clientSocket = new Socket(host, portNumber);

        } catch (UnknownHostException e) {
            System.err.println("Dont Know about host: " + host);
        } catch (IOException e) {
            System.err.println("IO exception:1 " + host);
        }

        if (clientSocket != null) {
            //try{
            new Thread(new MultiThreadChatClient()).start();
            /*while(!closed){
                        os.println(inputLine.readLine());
                    }*/
            //os.close();
            //is.close();
            //clientSocket.close();
            //}
            //catch(IOException e)
            //    {
            //        System.err.println("IO exception2: "+ e);
            //    }
        }
    }

    public void run() {
        String loggedin;
        try {
            inputLine = new BufferedReader(new InputStreamReader(System.in));

            ToServer = new PrintStream(clientSocket.getOutputStream());
            FromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            dout = new DataOutputStream(clientSocket.getOutputStream());
            din = new DataInputStream(clientSocket.getInputStream());
            System.out.println("Input your UserID: ");
            userID = inputLine.readLine();
            ToServer.println(userID);
            ToServer.flush();
            loggedin = FromServer.readLine();
            System.out.println(loggedin);
            if (loggedin.equals("Logged in")) {
                while (true) {
                    /*while((responseLine=is.readLine())!=null)
                {
                    System.out.println(responseLine);
                }*/

                    System.out.println("Do you want to send file or receive? Print 'send' or 'receive'");
                    String SendorReceive = inputLine.readLine();
                    ToServer.println(SendorReceive);   //send
                    ToServer.flush();

                    if (SendorReceive.equals("send")) {
                        String temp1 = FromServer.readLine(); //enter receiver ID
                        System.out.println(temp1);
                        ToServer.println(inputLine.readLine()); //send receiver ID
                        ToServer.flush();

                        System.out.println(FromServer.readLine()); //enter file name

                        String filename = inputLine.readLine();
                        File f = new File(filename);

                        ToServer.println(f.getName());  //file name input dicce
                        ToServer.flush();

                        //System.out.println(is.readLine());
                        // os.println(inputLine.readLine());
                        ToServer.println(f.length());
                        ToServer.flush();

                        String temp = FromServer.readLine();
                        int chunk = Integer.valueOf(temp);
//                        System.out.println(chunk);
                        temp = FromServer.readLine();
                        System.out.println("File ID no: " + temp);
                        int fileId = Integer.valueOf(temp);

                        FileInputStream fin = new FileInputStream(f);


                        /*int read;
                    while((read = fin.read(b)) != -1){
                        String str= new String(b);
                        System.out.println(str);
                        dout.write(b, 0, read); 
                        dout.flush(); 
                    }
                    fin.close();

                    os.flush();
                    
                         */
                        int flag = 126;
                        int sqno = 0;
                        long numSent = 0;

                        ToServer.println("here is now");
                        clientSocket.setSoTimeout(30000);
                        int run = 0;
                        while (numSent < f.length()) {
                            run++;
//                            System.out.println("loop runs " + run + " time");
                            long numThisTime = f.length() - numSent;

                            //numThisTime = numThisTime < bytes.length ? numThisTime : bytes.length;
                            if (numThisTime > chunk) {
                                numThisTime = chunk;
                            }
                            /*
                            byte b[] = new byte[(int) numThisTime];
                             */

                            byte b[] = new byte[(int) numThisTime + 4];

                            int numRead = fin.read(b, 2, (int) numThisTime);

                            byte temp2[] = new byte[(int) numThisTime];
                            int count = 0;
                            for (int i = 2; i < (int) numThisTime + 2; i++) {
                                temp2[i - 2] = b[i];

//                                System.out.print(b[i] + "-->");
//                                System.out.print(Integer.toBinaryString(b[i]));
                                String t = Integer.toBinaryString(b[i]);
                                String x = String.format("%8s", Integer.toBinaryString(b[i])).replace(' ', '0');

                                for (int j = 0; j < t.length(); j++) {
                                    if (t.charAt(j) == '1') {
                                        count++;
                                    }

                                }

                            }

                            sqno++;
                            b[0] = (byte) flag;
                            b[(int) numThisTime + 3] = (byte) flag;
                            b[1] = (byte) sqno;
                            b[(int) numThisTime + 2] = (byte) count;

                            int k = length(temp2);
//                            System.out.println("value of k " + k);
//                            System.out.println("value of numthistime " + (4 + numThisTime));
                            byte temp3[] = new byte[(int) numThisTime + k];
                            byte temp4[] = new byte[(int) numThisTime + k + 4];

//                            System.out.println("length of old array" + temp4.length);
                            temp3 = StuffBit(temp2);

                            //ToServer.println(temp4.length);
                            for (int i = 0; i < temp3.length; i++) {
//                                    System.out.println("before-->"+b[i]+"now-->"+temp3[i-2]);
                                temp4[i + 2] = temp3[i];
                            }
                            String error = "noerror";
                            do {
                                int newcount=count;
                                error = "noerror";
                            System.out.println("Checksum is : " + newcount + ". Do you want to change it?");
                            String yesno = inputLine.readLine();
//                            System.out.println("yesno: " + yesno);
                            String checksum= new String();
                            if (yesno.contains("yes")) {
                                System.out.println("Input Checksum here: ");
                                checksum = inputLine.readLine();
                                newcount= Integer.parseInt(checksum);
                            }
                            
                            
                            
                                temp4[0] = (byte) flag;
                                temp4[temp4.length - 1] = (byte) flag;
                                temp4[1] = (byte) sqno;
                                temp4[temp4.length - 2] = (byte) newcount;

//                            int numRead = fin.read(b, 0, (int) numThisTime);
                                if (numRead == -1) {
                                    break;
                                }

                                dout.write(temp4, 0, temp4.length);
                                String server =FromServer.readLine();
//                                System.out.println(server);
                                if(server.equals("error"))
                                {
                                    System.out.println("inside");
                                    error="error";
                                
                                }
//                                System.out.println("after dout");
//                                System.out.println(error);
                            } while (error.equals("error"));
                            
                            System.out.println(FromServer.readLine());

                            numSent += numRead;
//                            System.out.println("read " + numSent);
//                            System.out.println("size " + f.length());
                        }
                        clientSocket.setSoTimeout(Integer.MAX_VALUE);
                        String t = "File has been sent to server...";
//                        System.out.println(t);
                        ToServer.println(t);
                        fin.close();

                        System.out.println("File has been sent to server...");

                    } else if (SendorReceive.equals("receive")) {

                        System.out.println("Waiting for any user to send 'sending request'..." + '\n');
                        String senderId = FromServer.readLine();
                        String temp1 = FromServer.readLine();
                        System.out.println(temp1);
//                        String temp2 = FromServer.readLine();
//                        System.out.println(temp2);
                        temp1 = inputLine.readLine();

                        //ToServer.println(temp1.getBytes("UTF-8"));
                        // ToServer.flush();
//                        System.out.println(FromServer.readLine());
//                        temp1=FromServer.readLine();
                        ToServer.println(temp1);
                        ToServer.flush();

                        String FileName = FromServer.readLine();

                        String FileSize = FromServer.readLine();
//                        System.out.println(FileSize);
                        String ChunkSize = FromServer.readLine();
                        int chunk = Integer.valueOf(ChunkSize);
                        int size = Integer.valueOf(FileSize);

//                        System.out.println("chunk:" + chunk);
                        File f = new File(senderId + userID + FileName);

                        //  new
                        FileWriter fileWriter = new FileWriter(f);
//                        PrintWriter pw = new PrintWriter(fileWriter);
//                        FileOutputStream fout = new FileOutputStream(f);
                        BufferedWriter out = new BufferedWriter(new FileWriter(f));
                        while (true) {
                            String str = FromServer.readLine();

                            //you are trying to write
                            if ("completed".equals(str)) {
                                break;
                            }

//                            pw.println(str);
//                            fileWriter.flush();
                            out.write(str);  //Replace with the string
                            out.newLine();
//                            System.out.println(str);

                        }
                        fileWriter.close();

                        out.close();

                        System.out.println("File has been received successfuly");

//
//                            FileOutputStream fout = new FileOutputStream(f);
//
//                            int read = 0;
//                            //while((read = din.read(b)) != -1){
//                            System.out.println("Hello");
//                            DataInputStream din2 = new DataInputStream(clientSocket.getInputStream());
//                            while (true) {
//                                if (read < size) {
//                                    long available = size - read;
//                                    if (available > chunk) {
//                                        available = chunk;
//                                    }
//                                     
//                                    System.out.println("Hello1");
//                                    byte b[] = new byte[(int) available];
//                                    
//                                    
//                                    
//                                    int readen = din2.read(b, 0, (int) available);
//                                    
////                                    String str = new String(b);
////                                    System.out.println(str);
//                                    //System.out.println(readen);
//                                    System.out.println("Hello1");
//                                    fout.write(b);
//                                    System.out.println("Hello2");
//                                    fout.flush();
//                                    read = read + readen;
//
//                                    System.out.println(b.toString());
//                                } else {
//                                    break;
//                                }
//                                   
//                            }
//                            fout.close();
//                        ToServer.close();
//                        FromServer.close();
                    }
//                    System.out.println("Finished...");
                }
            }
        } catch (IOException ex) {
            System.err.println("IO exception3: " + ex);
        }

    }

}
