package offlinenetworking;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import static java.lang.String.format;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client implements Runnable{
    static Socket socketc;
    int [] gbn_arr;
    Client(Socket socketc)
    {
        this.socketc=socketc;
    }
    static String roll;

    
   public static String stuff(String st)
   {
       int [] au = new int[100];
       int ind = 0, r1 = 0;
       String fg = "01111110";
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
            pre1 = st.substring(0,au[i]+r1+1+5);
            post1 = st.substring(au[i]+1+r1+5);
            st = pre1 + "0" + post1;
            r1++;
            //System.out.println(aa);
        }
        return st;
   }
    
   public static byte [] bitStuff(byte [] bitar, int t )
   {
       String bs = "", bs1 = "",retSt = "",convertee = "", stpre = "", stpost = "",zeroPadd = "";
       int bsz = 0;
       for(int z=1; z<=(t+2); z++)
       {
           bs = String.format("%8s", Integer.toBinaryString(abcdef(bitar[z]))).replace(' ', '0');
           bs1 += bs;
       }
       retSt = stuff(bs1);
       bsz = retSt.length()/8;
       if((retSt.length()%8) != 0)
       {
           stpre = retSt.substring(0,(retSt.length()-(retSt.length()%8)));
           for(int h=1; h<=(8-(retSt.length()%8)); h++)
           {
               zeroPadd += "0";
           }
           stpost = retSt.substring(retSt.length()-(retSt.length()%8));
           bsz++;
           retSt = stpre + zeroPadd + stpost;
       }
       byte [] abc = new byte[bsz+2];
       int x=0,v=8; 
       abc[0] = bitar[0];
       abc[bsz+1] = bitar[bitar.length-1];
       for(int i=1; i<=bsz; i++)
       {
           convertee = retSt.substring(x,v);
           int cnv = Integer.parseInt(convertee,2);
           abc[i] = (byte) cnv;
           x+=8;
           v+=8;
       }
       return abc;
   }
   
   public static int sizeExtend(byte [] bitar, int t)
   {
       String bs = "", bs1 = "",retSt = "",convertee = "", stpre = "", stpost = "",zeroPadd = "";
       int bsz = 0;
       for(int z=1; z<=(t+2); z++)
       {
           bs = String.format("%8s", Integer.toBinaryString(bitar[z])).replace(' ', '0');
           bs1 += bs;
       }
       retSt = stuff(bs1);
       bsz = retSt.length()/8;
       if((retSt.length()%8) != 0)
       {
           bsz++;
       }
       return bsz;
   }
   public static int abcdef(byte ab)
    {
        return (ab & 0xFF);
    }
    
    public static void main(String args[])throws Exception{  
    int portno = 5000;
    String host = "localhost";
    
    
    BufferedReader infromUser = new BufferedReader(new InputStreamReader(System.in));//user console input
    
    System.out.println("Enter Roll No: ");
    roll = infromUser.readLine();
    
    /*System.out.println("Enter IP Adress: ");
    host = brcc.readLine();*/
    /*System.out.println(host);*/

    try{
       
    Socket so= new Socket(host,portno);
     Client obj = new Client(so);
     DataOutputStream sendToServer = new DataOutputStream(so.getOutputStream());//sending inofrmation to server 
     sendToServer.writeBytes(roll + '\n');
     
     
    if(socketc!=null){
    new Thread(obj).start();
    }
    
    }
    catch(UnknownHostException e)
     {
        System.err.println("Problem with host: "+ host);
     }
    
    /*PrintStream psc = new PrintStream(socketc.getOutputStream());
    DataInputStream doutc = new DataInputStream(socketc.getInputStream());*/
    
    
    }

    
    
    
    @Override
    public void run() {
       String fileName;
       String select;
       String receiverClientRoll;
       String clntroll;
        try {
            BufferedReader filens = new BufferedReader(new InputStreamReader(System.in));
            DataOutputStream sendFileToServer = new DataOutputStream(socketc.getOutputStream());
            BufferedReader br = new BufferedReader(new InputStreamReader(socketc.getInputStream()));
            DataInputStream dinst = new DataInputStream(socketc.getInputStream());
            
            sendFileToServer.writeBytes("go" + '\n');
            
            clntroll = br.readLine();
            if(clntroll.equals("new logger"))
            {
            System.out.println("Do you want to receive file or send file?");
            select = filens.readLine();//selection
            if(select.equals("send")){
            sendFileToServer.writeBytes("SendIt" + '\n');
            System.out.println("Enter reveiver client roll: ");
            receiverClientRoll = filens.readLine();
            sendFileToServer.writeBytes(receiverClientRoll + '\n');
            if(br.readLine().equals("Online")){
            
            System.out.println("Enter file path: ");
            fileName = filens.readLine();
            File fil = new File(fileName);
            /*System.out.println("Enter File Size: ");
            fileSize = filens.readLine();*/
            /*long l= Long.parseLong("15454677546757768");
            System.out.println(l);*/
            //DataInputStream dinc = new DataInputStream(socketc.getInputStream());  
             
            sendFileToServer.writeBytes(fil.getName() + '\n');
            //String flnm = "Client to Server from Roll " + roll + " " + fil.getName();
            sendFileToServer.writeBytes("Client to Server from Roll " + roll + " " + fil.getName() + '\n');
            sendFileToServer.writeBytes(Long.toString(fil.length()) + '\n');
            
            
            long fileSize = fil.length();
            System.out.println("File size: " + fileSize);
            long MaxChunkSize = 0;
            int TotalChunks = 0, maxchunksize = 0;
            if(br.readLine().equals("Yes"))
            {
                MaxChunkSize = Long.parseLong(br.readLine());
                System.out.println("Maximum chunk size from server class: " + MaxChunkSize);
                TotalChunks = Integer.parseInt(br.readLine());
                System.out.println("Total chunks for sending the file: "+ TotalChunks);
                System.out.println("Sending file...");
            }
            else
            {
                System.out.println("Server overloaded, transmission denied");
            }
            maxchunksize = (int) MaxChunkSize;
            //System.out.println(maxchunksize);
            
            int chunktrack = Integer.parseInt(br.readLine());
            //System.out.println(chunktrack);
            
            
            
            FileInputStream fins = new FileInputStream(fil);
            int readfile = 0;
            int sentChunk = 0;
            int seqChnk = 0;
            int completedFlag = 0;
            
            long fileSent = 0;
            long fileToSent = fileSize;
            int go_back_n = 3;
            
            String bArrString = "", payLoadString = "", flagString = "", seqNoString = "", cSumString = "";
            
            int flagv = 126, seqNo = 0;
            
            while(fileSent < fileToSent)
            {
                if(TotalChunks < go_back_n)
                {
                    go_back_n = TotalChunks;
                }
                gbn_arr = new int[go_back_n+1];
                sendFileToServer.writeBytes(Integer.toString(go_back_n) + '\n');
                for(int itrr=1; itrr<=go_back_n; itrr++){
                long fileThisTime = fileToSent - fileSent;
                fileThisTime = fileThisTime < maxchunksize ? fileThisTime : maxchunksize;
                byte arra[] = new byte[(int)fileThisTime+1+1+2];
                readfile = fins.read(arra, 2, (int) fileThisTime);
                gbn_arr[itrr] = readfile;
                /*new*/
                int checkSum = 0;
                arra[0] = (byte) flagv;
                flagString = String.format("%8s", Integer.toBinaryString(arra[0])).replace(' ', '0');
                seqNo = seqChnk + 1;
                arra[1] = (byte) (seqNo);
                seqNoString = String.format("%8s", Integer.toBinaryString(arra[1])).replace(' ', '0');
                for(int u=2; u<(int) (fileThisTime+2); u++)
                {
                    bArrString = String.format("%8s", Integer.toBinaryString(arra[u])).replace(' ', '0');
                    payLoadString += bArrString;
                    //System.out.print(bArrString + " ");
                    for(int g=0; g<bArrString.length(); g++)
                    {
                        if(bArrString.charAt(g) == '1')
                        {
                            checkSum++;
                        }
                    }
                }
                
                arra[arra.length - 2] = (byte) checkSum;
                cSumString = String.format("%8s", Integer.toBinaryString(arra[arra.length -2])).replace(' ', '0');
                arra[arra.length - 1] = (byte) flagv;
                bArrString = seqNoString + payLoadString + cSumString;
                //checkSum = 0;
                System.out.println("Frame " + (seqChnk+1) + ": ");
                System.out.println("The frame without bit stuffing: " + flagString+bArrString+flagString);               
                System.out.println("Checksum: " + checkSum);
                bArrString = "";
                payLoadString = "";    
                
                int sizebytearr = 0;               
                sizebytearr = sizeExtend(arra, (int) fileThisTime);
                sendFileToServer.writeBytes(Integer.toString(sizebytearr) + '\n');
                br.readLine();
                byte [] arratest = new byte[sizebytearr+2];
                arratest = bitStuff(arra, (int) fileThisTime);
                String printStringB = "", qwe = ""; 
                for(int it=0; it<(sizebytearr+2); it++)
                {
                    qwe = String.format("%8s", Integer.toBinaryString(abcdef(arratest[it]))).replace(' ', '0');
                    printStringB += qwe;
                    
                }
                System.out.println("The frame with bit stuffing:    " + printStringB);
                System.out.println("\n");
                /*new*/
                if(readfile == -1)
                {
                    break;
                }
                sendFileToServer.write(arratest, 0, arratest.length);
                sendFileToServer.writeBytes(Integer.toString(checkSum) + '\n');
                fileSent = fileSent + readfile;
                seqChnk++;}
                TotalChunks -= go_back_n;
                for(int itt=1; itt<=go_back_n; itt++){
                if(br.readLine().equals("got chunk"))
                {
                    sentChunk++;
                    System.out.println("Chunk no " + sentChunk + " sent to server, size: " + gbn_arr[itt]);
                }
                else
                {
                    completedFlag = 1;
                    break;               
                }}
               
            }
            if(completedFlag == 0)
            {
                System.out.println("File sending to server completed");
            }
            else
            {
                System.out.println("Problem occured, file sending not completed");
            }
            fins.close();
            } // online
            else
            {
                System.out.println("Receiver Client is offline");
            }//offline
            }
           
            
            
            
            
            
            else if(select.equals("receive"))
            {
                sendFileToServer.writeBytes("ReceiveIt" + '\n');
                long fileSizeFromServer;
                int recClient, senClient;
                String fileRname;
                String fileCname;
                long chunksz = 0;
                int readf = 0;
                
                
                fileRname = br.readLine();
                fileCname = br.readLine();
                fileSizeFromServer = Long.parseLong(br.readLine());
                recClient = Integer.parseInt(br.readLine());
                senClient = Integer.parseInt(br.readLine());
                chunksz = Long.parseLong(br.readLine());
                System.out.println("Client " + senClient + " wants to send you a file");
                System.out.println("File Name: " + fileRname);
                System.out.println("File Size: " + fileSizeFromServer + " bytes");
                System.out.println("Do You want to receive the file? yes or no ?");
                String sel = filens.readLine();
                sendFileToServer.writeBytes(sel + '\n');
                
                String snd = br.readLine();
                if(snd.equals("Ok")){
                System.out.println("Receiving File...");
                File fileR = new File("G:\\cse\\Level-3, Term-2\\Networking Sessional\\Client\\" + "Client " + senClient + " to " + recClient + " " + fileRname);
                FileOutputStream fous2 = new FileOutputStream(fileR);
                long fileReceivedC = 0;
                long fileToReceivedC = fileSizeFromServer;
                //long chunksz = 13;
                int chunk = 0;
                while(fileReceivedC < fileToReceivedC){
                long fileThisTimesc = fileToReceivedC - fileReceivedC;
                fileThisTimesc = fileThisTimesc < chunksz ? fileThisTimesc : chunksz;
                byte byarr[] = new byte[(int)fileThisTimesc];

                readf = dinst.read(byarr, 0, (int) fileThisTimesc);
                 
                fous2.write(byarr, 0, readf);
                
                
                
                /*int lncount = 0, cnt1 = 0;
                for(int bitt=0; bitt<(int)fileThisTimesc; bitt++)
                {
                    //String teststr = Integer.toBinaryString(byarr[bitt]);
                    


                    String teststr = String.format("%8s", Integer.toBinaryString(byarr[bitt])).replace(' ', '0');
                    //String teststr = Byte.toString(byarr[bitt]);
                    System.out.print(teststr + " ");
                    //System.out.print(byarr[bitt] + " ");
                    lncount++;
                    if(lncount == 100)
                    {
                        System.out.print("\n");
                        lncount = 0;
                    }
                    for(int p=0; p<teststr.length(); p++)
                    {           
                        if(teststr.charAt(p) == '1')
                        {
                            cnt1++;
                        }
                    }
                }
                System.out.print("\n");
                System.out.println(cnt1);*/
                
                
                
                fileReceivedC = fileReceivedC + readf;
                chunk++;
                System.out.println("Chunk " + chunk + " received");
                }
                System.out.println("File received from Client " + senClient);
                fous2.close();
                }
                else if(snd.equals("Not ok"))
                {
                    System.out.println("I do not want to receive the file");
                    System.out.println("File deleted from server");
                }
            }
            
            
            
            
            
            
            else
            {
                sendFileToServer.writeBytes("WrongIt" + '\n');
                System.out.println(br.readLine());
            }
            sendFileToServer.writeBytes("Disconnected" + '\n');
            socketc.close();
            
        }//new logger
        else if(clntroll.equals("already logged in"))           
        {
            System.out.println("Client " +  roll + " is already connected");
            socketc.close();
        }   
            
            
        } catch (IOException ex) {
            System.err.println("IOException 1"+ex);
        }
    }
}