/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javanetworking;

import static com.sun.corba.se.impl.util.Utility.printStackTrace;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 *
 * @author Riad Ashraf
 */
public class MultiThreadServer implements Runnable {

    Socket csocket;
    static HashMap<Integer, Socket> hm = new HashMap<Integer, Socket>();
    static ArrayList<MultiThreadServer> serverList = new ArrayList<MultiThreadServer>();
    static ArrayList<String> UIList = new ArrayList<String>();
    static int k = 1;
    static int serverSize = 1024000;
    static int fileId = 1;
    static String userid;
    PrintStream toClient;
    BufferedReader inFromClient;
    DataInputStream din;
    DataOutputStream dout;
//           static PrintStream os = null;
    
    public static int unsignedToBytes(byte b) {
    return b & 0xFF;
    }

     public static byte[] DeStuff(byte [] temp )
     {
         
            String Dstuff = new String();
        for (int d = 0; d < temp.length; d++) {

//            System.out.print(temp[d] + "-->");
//            System.out.println(Integer.toBinaryString(temp[d]));
//            String t = Integer.toBinaryString(temp[d]);
            String x = String.format("%8s", Integer.toBinaryString(unsignedToBytes(temp[d]))).replace(' ', '0');
//            System.out.println("x" + x);
            Dstuff = Dstuff + x;

        }
        System.out.println("Before DeStuffing Primarily: " +Dstuff );
        
//        System.out.println(BitStuff);
//        System.out.println(Dstuff);
        int len=Dstuff.length();
//            int mod = len % 8;
//                if (mod > 0) {
//       Dstuff = Dstuff.substring(0, Dstuff.length() - 8) + Dstuff.substring(Dstuff.length() - mod, Dstuff.length());
//        }      
//        System.out.println(BitStuff);
//        System.out.println(Dstuff);
           System.out.println("Before DeStuffing finally  : " +Dstuff );
        //here I am Dstuffing
        int i = 0;
        int len_Dstuff = Dstuff.length();
        boolean foundIt = false;
        String matching_bits = "011111010";
        int len_match = matching_bits.length();
        //System.out.println(BitStuff.regionMatches(11, matching_bits, 0, len_match));
        //System.out.println(Dstuff.substring(11, 20));
        int D_array[]= new int[len_Dstuff/8];
        int p=0;
        while (i <= (len_Dstuff)) {
            
            if (Dstuff.regionMatches(i, matching_bits, 0, len_match)) {
                D_array[p]=i+6;
                p++;
                foundIt = true;
   //             System.out.println("here range is "+i+"to"+(i+len_match));
                
                                i = i + len_match;
            }
            else
            {
                i++;
            }
            
        }
        int mod = p % 8;
//        System.out.println(" value of k : "+mod);
//        System.out.println();
        if (mod > 0) {
//              System.out.println(Dstuff.substring(0, Dstuff.length() - 8)+"--"+Dstuff.substring(Dstuff.length() - mod, Dstuff.length()));
            Dstuff = Dstuff.substring(0, Dstuff.length() - 8) + Dstuff.substring(Dstuff.length() - mod, Dstuff.length());
        }
        int q=0;
        while(q<p)
        {
            i=D_array[q];
            for(int r=q+1;r<p;r++)
            {
                D_array[r]=D_array[r]-1;
            }
//            System.out.println("i-->"+i);
//                System.out.println(Dstuff.substring(0, i)+"----"+Dstuff.substring(i +1, Dstuff.length()));
                Dstuff = Dstuff.substring(0, i) +  Dstuff.substring(i +1, Dstuff.length());
//                System.out.println(Dstuff);
                q++;

        }
//            System.out.println("This is DStuff: "+Dstuff);
//            System.out.println("changedbit\n"+changedBitStuff);

            System.out.println("After  DeStuffing finally  : " +Dstuff );
            //System.out.println("The String was             : " +changedBitStuff );
          //destuffing shesh, abar array te vortesi   
            int ln=Dstuff.length();
            int u=0,v=8;
           
          
          int temp_len;
        if(ln%8==0)
        {
            if(Dstuff.regionMatches(Dstuff.length()-8, "00000000", 0, 8))
            {
                
                temp_len=ln/8-1;
            }
            else{
                temp_len=ln/8;
                
            }
            
        }
        else
        {
            temp_len=ln/8+1;
        }
        byte temp1[] = new byte[temp_len];
          
          
          
          int l=0;
          //l=0;
            while (v <= ln) {
            String s = new String();
//            System.out.println(Dstuff.substring(u, v));
            s = Dstuff.substring(u, v);
//                System.out.println("s :"+s);
            int y = Integer.parseInt(s, 2);
            temp1[l] = (byte) y;
            l++;
//            System.out.println("l = " + l);
            v = v + 8;
            u = u + 8;
        }
//        System.out.println("u" + u+"-->"+ln);
//        System.out.println(BitStuff.substring(u, ln));
        if(ln%8!=0){
            
        
        temp1[l] = (byte) Integer.parseInt(Dstuff.substring(u, ln), 2);
        }
        
//        for(int e=0;e<temp1.length;e++)
//        {
////            System.out.println(temp1[e] + "-->");
//        }
        return temp1;
    
     }
    
    
    
    
    
    MultiThreadServer(Socket csocket) {
        this.csocket = csocket;
    }

    public static void main(String args[])
            throws Exception {
        ServerSocket ssock = new ServerSocket(5000);
        System.out.println("Listening");
        while (true) {
            Socket sock = ssock.accept();
            BufferedReader infromClient = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            PrintWriter toClient = new PrintWriter(sock.getOutputStream(), true);
            userid = infromClient.readLine();
            if (hm.containsKey(Integer.valueOf(userid))) {
                toClient.println("User already logged in");
                toClient.flush();
            } else {
                MultiThreadServer ser = new MultiThreadServer(sock);
                new Thread(ser).start();
                serverList.add(ser);
                UIList.add(userid);
                hm.put(Integer.valueOf(userid), sock);
                System.out.println("connected to client" + userid);
                toClient.println("Logged in");
                toClient.flush();
            }

        }
    }

    public synchronized void FromServerToClient(String senderID, String filename, String FileSize, int chunk) throws FileNotFoundException, IOException {

        File f = new File(filename);
        toClient.println(senderID);
        toClient.flush(); 
        toClient.println("User Id "+senderID + " is trying to send you a file named "+filename+ " of size "+FileSize +" bytes..Do you want to receive ?");  //file name input dicce
        toClient.flush();
//        toClient.println("Do you want to receive from " + senderID + "? type yes or no");  //file name input dicce
//        toClient.flush();
        String temp = inFromClient.readLine();

//        String temp = inFromClient.readLine();
//        System.out.println(temp);
        toClient.println(filename);  //file name pathacche
        toClient.flush();

        toClient.println(f.length());  //file size pathacche
        toClient.flush();
        toClient.println(chunk);  //chunk size pathacche
        toClient.flush();

        
        if (temp.contains("yes")) {

            try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                String line;
                while ((line = br.readLine()) != null) {
                    toClient.println(line);
//                    System.out.println(line);

                }
                toClient.println("completed");
            }
        

//            FileInputStream fin = new FileInputStream(f);
////
////
////            /*int read;
////                    while((read = fin.read(b)) != -1){
////                        String str= new String(b);
////                        System.out.println(str);
////                        dout.write(b, 0, read); 
////                        dout.flush(); 
////                    }
////                    fin.close();

////                    os.flush();
////                    
////             */
//            long numSent = 0;
//
//            while (numSent < f.length()) {
//                long numThisTime = f.length() - numSent;
//
//                //numThisTime = numThisTime < bytes.length ? numThisTime : bytes.length;
//                if (numThisTime > chunk) {
//                    numThisTime = chunk;
//                }
//                byte b[] = new byte[(int) numThisTime];
//
//                int numRead = fin.read(b, 0, (int) numThisTime);
//                String str = new String(b);
//                System.out.println(str);
//         
//                if (numRead == -1) {
//                    break;
//                }
//
//                dout.write(b, 0, numRead);
//                dout.flush();
//                System.out.println("Size of dout is : " + dout.size());
//                System.out.println("janina2");
//                numSent += numRead;
//
//            }
//            
//            fin.close();
        } else if (temp.contains("no")) {
            f.delete();
        }
    }

    public void run() {
        try {
            toClient = new PrintStream(csocket.getOutputStream());
            inFromClient = new BufferedReader(new InputStreamReader(csocket.getInputStream()));
            din = new DataInputStream(csocket.getInputStream());
            dout = new DataOutputStream(csocket.getOutputStream());

            String receiverid = null;
            String FileName = null;
            String FileSize = null;
            int size;
            int chunk;
            String sendorreceive = inFromClient.readLine();
//            System.out.println(sendorreceive);
            if (sendorreceive.contains("send")) {
                toClient.println("Enter receiver id");
                toClient.flush();
                receiverid = inFromClient.readLine();
//                System.out.println(receiverid);
                if (hm.containsKey(Integer.valueOf(receiverid))) {
                    toClient.println("Enter file Path: ");
                    toClient.flush();
                    FileName = inFromClient.readLine();
                    //toClient.println("Enter filesize: ");
                    //toClient.flush();
                    FileSize = inFromClient.readLine();
                    size = Integer.valueOf(FileSize);

                    if (size < MultiThreadServer.serverSize) {
                        Random random = new Random();
                        chunk = random.nextInt(size)/3 + 1;

                        toClient.println(chunk);
                        toClient.flush();
                        fileId++;
                        toClient.println(fileId);
                        toClient.flush();
//                        System.out.println("chunk:" + chunk);
                        File f = new File(FileName);
                        // System.out.println(f.createNewFile()+"");
                        FileOutputStream fout = new FileOutputStream(f);

                        int read = 0;
                        //while((read = din.read(b)) != -1){
                        System.out.println(inFromClient.readLine());
                        int run=0;
                        while (true) {
                            run++;
                            
                            if (read < size) {
//                                System.out.println("loop runs "+run+" times");
//                                System.out.println("total size "+size);
//                                System.out.println("current read"+ read);
                                long available = size - read;
                                if (available > chunk) {
                                    available = chunk;
                                }
//                                System.out.println("value available "+(available+4));
                                //String s= inFromClient.readLine();
                                //System.out.println("from client "+inFromClient.readLine());
                                byte b[] = new byte[(int) available+4+(int) available/8+2];
                                while(true)
                                {
                                int readen = din.read(b);
//                                System.out.println("total array size "+((int) available+4+(int) available/8+1)+" total read "+readen );
                                int j=0;
                                for(int i=1;i<b.length;i++)
                                {
//                                    System.out.println(b[b.length-1]);
//                                    System.out.println("---"+b[i]+"---");
                                    if(b[i]==126 )
                                    {
                                        j=i+1;
//                                        System.out.println("new temp array size "+ (i+1));
                                        break;
                                    }
                                    
                                    
                                }
                                
                                
                                
                                
                                
                                
                                
                                byte temp[] = new byte[j-4];
                                
                                for(int i=0;i<j-4;i++)
                                {
                                    
                                    temp[i]=b[i+2];
//                                    System.out.println(temp[i]+"-->");
                                    
                                }
                                int count= b[readen-2];
//                                    System.out.println("count; "+count);
                                System.out.println("chunk received from "+userid);
                                byte temp1[] = new byte[(int) available];
                                temp1= DeStuff(temp);
                                int countGet=0;
                                for (int i = 0; i < temp1.length; i++) {
                               
//                                    System.out.println("in main");
//                                System.out.print(temp1[i] + "-->");
//                                System.out.print(Integer.toBinaryString(b[i]));
                                String t = Integer.toBinaryString(temp1[i]);
                                                           
                                for (int w = 0; w < t.length(); w++) {
                                    if (t.charAt(w) == '1') {
                                        countGet++;
                                    }

                                }
//                                    System.out.println("Number of 1 found: "+ countGet);
                                
                                }
                                if(count==countGet)
                                {
                                    System.out.println("Checksum matched.");
                                    toClient.println("noerror");
                                    toClient.flush();
                                    fout.write(temp1);
                                    read = read + temp1.length;
                                    break;
                                }
                                else
                                {
                                    toClient.println("error");
                                    toClient.flush();
                                }
                            }
                                
                                toClient.println("From server: chunk received...");
                                toClient.flush();
                                
//                                System.out.println("before fout");
                                
                                //fout.flush();
                                
//                                System.out.println("read "+ read);
//                                System.out.println("size "+ size);
                                //System.out.println(b.toString());
                            } else {
                                break;
                            }
                        }
                        System.out.println("From user Id "+userid +": "+inFromClient.readLine());
                        fout.close();
                        int index = UIList.indexOf(receiverid);

                        serverList.get(index).FromServerToClient(userid, f.getName(), FileSize, chunk);

                    }

                } else {
                    System.out.println("User Offline");
                }
            } else if (sendorreceive.contains("receive")) {
                return;

            }

          
           
        } catch (Exception ex) {
//            printStackTrace();
            System.out.println("exception !!!!!" + ex);
        }
    }

}
