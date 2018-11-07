package offlinenetworking;

import com.sun.org.apache.xpath.internal.operations.Quo;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Server implements Runnable{
     Socket consocket;
     static HashMap<Integer,Socket>hm = new HashMap<Integer,Socket>();
     static int z = 1;
     static int p = 0;
     static long serverSize = 500*1024*1024;
     long maxChunkSize = 0;
     int totalChunks = 0;
     int ReceiverClientNo = 0;
     long filesizeInBytes =  0;
     String filenam = "";
     String fileRealName = "";
     int rollno = 0;
     int rf = 0;
     int N = 0;
     public static Vector<RecieveClient>vec = new Vector<RecieveClient>();
     Server(Socket consocket)
     {
         this.consocket = consocket;
     }
     
     public static String DeStuff(String st)
   {
       int [] au = new int[100];
       int ind = 0;
       String fg = "011111010";
       for (int i = -1; (i = st.indexOf(fg, i + 1)) != -1; i++) {
        //System.out.println(i);
        au[ind++] = i;
        }
//       for(int i=0;i<ind;i++)
//        {
//            System.out.println(au[i]);
//        }
        String pre1 = "", post1 = "";
        for(int i=0; i<ind; i++)
        {
            pre1 = st.substring(0,au[i]+1+5);
            post1 = st.substring(au[i]+7);
            st = pre1 + post1;
            for(int it2=i; it2<ind; it2++)
            {
                au[it2]--;
            }
            //System.out.println(aa);
        }
        if((st.length()%8) != 0)
        {
            int zeroPos = 0;
            zeroPos = (((st.length()/8))*8);
            pre1 = st.substring(0, zeroPos);
            post1 = st.substring(zeroPos + (st.length()%8));
            st = pre1 + post1;
        }
        return st;
   }
     
     
     public static byte [] bitDestuff(byte [] bitarr)
     {
       String bs = "", bs1 = "",retSt = "",convertee = "";
       for(int z=1; z<=(bitarr.length-3); z++)
       {
           bs = String.format("%8s", Integer.toBinaryString(abcdefg(bitarr[z]))).replace(' ', '0');
           bs1 += bs;
       }
       retSt = DeStuff(bs1);
       retSt = retSt.substring(8);
       byte [] newBitarr = new byte[retSt.length()/8];
       int x=0,v=8;
       for(int i=0; i<(retSt.length()/8); i++)
       {
           convertee = retSt.substring(x,v);
           int cnv = Integer.parseInt(convertee,2);
           newBitarr[i] = (byte) cnv;
           x+=8;
           v+=8;
       }
       
       return newBitarr;
     }
     
     
     public static int MinusSize(byte [] bitarr)
   {
       String bs = "", bs1 = "",retSt = "",convertee = "";
       for(int z=1; z<=(bitarr.length-3); z++)
       {
           bs = String.format("%8s", Integer.toBinaryString(abcdefg(bitarr[z]))).replace(' ', '0');
           bs1 += bs;
       }
       retSt = DeStuff(bs1);
       retSt = retSt.substring(8);
       return (retSt.length()/8);
   }
     
     
     
     
     public static int abcdefg(byte bte)
    {
        return (bte & 0xFF);
    }
     public static void main(String args[])throws Exception{ 
         ServerSocket serversock = new ServerSocket(5000);
         System.out.println("Server Started");
         
                                    
         while(true){
         Socket sockets = serversock.accept();
         Server ser = new Server(sockets);
         BufferedReader recieveFromClient= new BufferedReader(new InputStreamReader(sockets.getInputStream()));//receiving information from client
         ser.rollno = Integer.parseInt(recieveFromClient.readLine());
         if(hm.containsKey(ser.rollno))
         {
             ser.rf = 1;
         }
         else{
         hm.put(ser.rollno,sockets);
         System.out.println("Connected to active Client no "+z+", Roll: "+ser.rollno);
         z++;}
         new Thread(ser).start();
         }
     }
     
     
     
     
     
     
     
     
     
    @Override
    public void run() {
         try {
             BufferedReader receiveFileFromClient = new BufferedReader(new InputStreamReader(consocket.getInputStream()));
             DataOutputStream servertoclient = new DataOutputStream(consocket.getOutputStream());
             DataInputStream din = new DataInputStream(consocket.getInputStream());
             
             receiveFileFromClient.readLine();
             if(rf == 1)
             {
                 servertoclient.writeBytes("already logged in" + '\n');
             }
             else
             {
                 servertoclient.writeBytes("new logger" + '\n');
             
             
             
             String str = receiveFileFromClient.readLine();
             if(str.equals("SendIt")){
             ReceiverClientNo = Integer.parseInt(receiveFileFromClient.readLine());
             if(hm.containsKey(ReceiverClientNo)){
             servertoclient.writeBytes("Online" + '\n');
             fileRealName = receiveFileFromClient.readLine();
             filenam = receiveFileFromClient.readLine();
             String fileNamString = "";
             fileNamString = "With bit stuffing " + filenam;
             File file = new File(filenam);
             File file2 = new File(fileNamString);
             //Path p = Paths.get(receiveFileFromClient.readLine());
             filesizeInBytes = Long.parseLong(receiveFileFromClient.readLine());
             //servertoclient.writeBytes(Long.toString(filesizeInBytes/1024) + '\n');
             if(filesizeInBytes < serverSize)
             {
                 int flag = 0;
                //System.out.println((filesizeInBytes/1024) + " KB");
                servertoclient.writeBytes("Yes" + '\n');
                
                Random random =  new Random();
                int maxchunks = (int) (filesizeInBytes/25);
                int minchunks = (int) (filesizeInBytes/4);
                int temp = random.nextInt(minchunks);
                temp = temp >=  0 ? temp : (-1)*temp; 
                maxChunkSize = temp + maxchunks;
                //maxChunkSize = 100*1024;
                
                //maxChunkSize = random.nextInt((int) (filesizeInBytes/25)) + 1;
                //System.out.println(maxChunkSize);
                servertoclient.writeBytes(Long.toString(maxChunkSize) + '\n');
                if((filesizeInBytes%maxChunkSize) == 0)
                {
                    totalChunks = (int) (filesizeInBytes/maxChunkSize);
                }
                else
                {
                    totalChunks = (int) ((filesizeInBytes/maxChunkSize) + 1);
                    flag = 1;
                }
                //System.out.println(totalChunks);
                servertoclient.writeBytes(Integer.toString(totalChunks) + '\n');
                servertoclient.writeBytes(Integer.toString(flag) + '\n');
                
                
                
                 FileOutputStream fous = new FileOutputStream(file);
                 FileOutputStream fous2 = new FileOutputStream(file2);
                 
                 
                 int mcs = (int) maxChunkSize;
                 //System.out.println(maxChunkSize);
                 int rd = 0, rd1 = 0;
                 
                long fileReceived = 0, fileToReceived = filesizeInBytes;
                while(fileReceived < fileToReceived)
                {
                    N = Integer.parseInt(receiveFileFromClient.readLine());
                    for(int itrtr=1; itrtr<=N; itrtr++){
                    long fileThisTimeR = fileToReceived - fileReceived;
                    fileThisTimeR = fileThisTimeR < mcs ? fileThisTimeR : mcs;
                    
                    
                    
                    /*new*/
                    int stfsize = Integer.parseInt(receiveFileFromClient.readLine())+2;
                    servertoclient.writeBytes("got size" + '\n');
                    byte arra[] = new byte[stfsize];
                    
                    
                    rd = din.read(arra, 0, stfsize);
                    int chSum = Integer.parseInt(receiveFileFromClient.readLine());
                    int newSize = MinusSize(arra);
                    byte [] newArra = new byte[newSize];
                    newArra = bitDestuff(arra);
                    //rd1 = din.read(newArra, 0, newSize);
                    
                    int chkSumcount = 0;
                    for(int itr=0; itr<newSize; itr++)
                    {
                        String forCheckSumString = String.format("%8s", Integer.toBinaryString(newArra[itr])).replace(' ', '0');
                        for(int itrt = 0; itrt<forCheckSumString.length(); itrt++)
                        {
                            if(forCheckSumString.charAt(itrt) == '1')
                            {
                                chkSumcount++;
                            }
                        }
                    }
                    
                    
                    /*new*/
                    if(chkSumcount == chSum){
                    fous.write(newArra, 0, newSize);
                    fous2.write(arra, 0, rd);
                    
                    
                    fileReceived = fileReceived + fileThisTimeR;}}
                    for(int itrtr2=1; itrtr2<=N; itrtr2++){
                    servertoclient.writeBytes("got chunk" + '\n');}
                }
                RecieveClient rcl = new RecieveClient(fileRealName, filenam, file.length(), ReceiverClientNo, rollno,maxChunkSize);
                vec.add(rcl);
                fous.close();
                fous2.close();
             }
                 
                 
                 
                 
                 
                 
                 
                 
             
             else
             {
                 servertoclient.writeBytes("No" + '\n');
             }
             }
             else
             {
                 servertoclient.writeBytes("Offline" + '\n');
             }
             }//SendIt: received from client
             
             
             
             else if(str.equals("ReceiveIt"))
             {
                 /*int a = 9;
                 servertoclient.writeBytes("hmm" + '\n');*/
                 int Rclient = 0;
                 int Sclient = 0;
                 long flSize = 0;
                 String fCname = "";
                 String fRname = "";
                 long chnksz = 0;
                 
                 for(int i=0; i<vec.size(); i++)
                 {
                     if(vec.elementAt(i).rclient == rollno)
                     {
                         //p++;
                         //System.out.println("Client roll "+ vec.elementAt(i).sclient + "send file");
                         
                         fRname = vec.elementAt(i).filenamereal;
                         fCname = vec.elementAt(i).filenamechanged;
                         flSize = vec.elementAt(i).filesizeF;
                         Rclient = vec.elementAt(i).rclient;
                         Sclient = vec.elementAt(i).sclient;
                         chnksz = vec.elementAt(i).mchnkszE;
                         servertoclient.writeBytes(fRname + '\n');
                         servertoclient.writeBytes(fCname + '\n');
                         servertoclient.writeBytes(Long.toString(flSize) + '\n');
                         servertoclient.writeBytes(Integer.toString(Rclient) + '\n');
                         servertoclient.writeBytes(Integer.toString(Sclient) + '\n');
                         servertoclient.writeBytes(Long.toString(chnksz) + '\n');
                         break;
                     }
                 }
                 //System.out.println(p);
                
                //int completedFlag = 0;
            
            
                /*while(fileSent < fileToSent)
                {
                    long fileThisTime = fileToSent - fileSent;
                    fileThisTime = fileThisTime < maxchunksize ? fileThisTime : maxchunksize;*/
                if(receiveFileFromClient.readLine().equals("yes")){
                    servertoclient.writeBytes("Ok" + '\n');
                    File fileS = new File(fCname);
                    String fCnameString = "";
                    fCnameString = "With bit stuffing " + fCname;
                    File fileS3 = new File(fCnameString);
                    FileInputStream fins2 = new FileInputStream(fileS);
                    FileInputStream fins3 = new FileInputStream(fileS3);
                    int rdfile = 0;
                    long fileToSend = flSize;
                    long fileSend = 0;            
                    //System.out.println(maxChunkSize);
                    //System.out.println(chnksz);
                    
                    while(fileSend < fileToSend){
                    long fileThisTimeS = fileToSend - fileSend;
                    fileThisTimeS = fileThisTimeS < chnksz ? fileThisTimeS : chnksz;
                    byte byarr[] = new byte[(int)fileThisTimeS];
                    rdfile = fins2.read(byarr, 0, (int) fileThisTimeS);
                    if(rdfile == -1)
                    {
                        break;
                    }
                    servertoclient.write(byarr, 0, rdfile);
                    fileSend  = fileSend + rdfile;
                    }
                    fins3.close();
                    fileS3.delete();
                    fins2.close();
                    fileS.delete();
                    //System.out.println("aaaaa");
                    
                    //System.out.println("bbbbb");
                    int vecsize = vec.size();
                    for(int i=0; i<vecsize ; i++)
                    {
                        for(int j=0; j<vec.size(); j++)
                        {
                            if((vec.elementAt(j).rclient == Rclient) && (vec.elementAt(j).sclient == Sclient)) 
                            {
                                vec.remove(j);
                                break;
                            }
                        }
                    }
                    }
                else
                {
                    File fileS = new File(fCname);
                    String fCnameString = "";
                    fCnameString = "With bit stuffing " + fCname;
                    File fileS3 = new File(fCnameString);
                    FileInputStream fins2 = new FileInputStream(fileS);
                    FileInputStream fins3 = new FileInputStream(fileS3);
                    fins2.close();
                    fileS.delete();
                    fins3.close();
                    fileS3.delete();
                    int vecsize = vec.size();
                    for(int i=0; i<vecsize ; i++)
                    {
                        for(int j=0; j<vec.size(); j++)
                        {
                            if((vec.elementAt(j).rclient == Rclient) && (vec.elementAt(j).sclient == Sclient)) 
                            {
                                vec.remove(j);
                                break;
                            }
                        }
                    }
                    servertoclient.writeBytes("Not ok" + '\n');       
                }
                    
                    //fileSent = fileSent + readfile;
                    /*if(br.readLine().equals("got chunk"))
                    {
                        sentChunk++;
                        System.out.println("Chunk no " + sentChunk + " sent to server, size: " + readfile);
                    }
                    else
                    {
                        completedFlag = 1;
                        break;               
                    }
                    
                }*/
             }//ReceiveIt: server to client
             else if(str.equals("WrongIt"))
             {
                 servertoclient.writeBytes("Wrong format of input" + '\n');
             }
             
             if(receiveFileFromClient.readLine().equals("Disconnected"))
             {
                 z--;
                 for(int i=0; i<hm.size() ; i++)
                 {
                     if(hm.containsKey(rollno))
                     {
                         hm.remove(rollno);
                         System.out.println("Client with roll " + rollno + " disconnected");
                     }
                 }
             }
         }
         } catch (IOException ex) {
             Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
         }
    }
}
