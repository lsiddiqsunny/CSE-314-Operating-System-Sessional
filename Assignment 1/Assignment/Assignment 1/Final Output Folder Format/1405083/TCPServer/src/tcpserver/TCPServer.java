package tcpserver;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.rmi.server.ExportException;
import java.util.Vector;

class file{
    int FileID;int size;String Receiver,Sender,FileName;
    public file(int FileID,int size,String Receiver,String Sender,String FileName){
        this.FileID = FileID;this.size = size; this.Receiver = Receiver;
        this.Sender = Sender;this.FileName = FileName;
    }


}

class connection{
    public WorkerThread wt ;
    public String threadID;
    public boolean request;
    connection(WorkerThread wt,String threadID){
        this.wt = wt;this.threadID = threadID;
    }
}
class Transmission{
    String sender,receiver,FileName;int FileSize,FileID;
}
public class TCPServer

{

    public java.lang.String[] student = {"80","81","82","83","84","85"};
    public static String[] Online={null,null,null,null,null,null};
    public static connection[] threads = new connection[6];
    public static Transmission[] T = new Transmission[20];
    public static byte[] byteArray= new byte[10000000];
    public static int maxSize = 10000000;
    public static int FileCount =0;
    public static int id=1;

    public static class OnlineList extends TCPServer implements Runnable {


        public void UpdateList(){

            while(true){
                for(int i=0;i<6;i++){
                    if(Online[i]!=null){
                        try{
                            Socket skt1 = threads[i].wt.skt;
                            int x = skt1.getInputStream().read();
                        }
                        catch(Exception e){
                            id--;
                            System.out.println("User " + threads[i].wt.threadID + " disconnected.\n");
                            String sp = Online[i];
                            Online[i] = null;
                            for(int j = 0;j<20;j++){
                                if(T[j]!=null && (T[j].sender.compareTo(sp)==0 ||
                                        T[j].receiver.compareTo(sp)==0)){
                                    T[j]=null;
                                }
                            }
                            try{
                                threads[i].wt.skt.close();
                            }
                            catch(Exception p){
                            }
                            threads[i].request=false;
                            threads[i].threadID=null;
                            threads[i].wt=null;
                            threads[i]= null;
                        }
                    }
                }

            }
        }
        @Override
        public void run(){
            UpdateList();

        }
    }


    public static void main(String[] args) throws Exception
    {

        int count=0;
        OnlineList ol  = new OnlineList();
        Thread th = new Thread(ol);
        th.start();

        ServerSocket welcomeSocket = new ServerSocket(5678);
        ServerSocket welcomeSocket2 = new ServerSocket(5679);
        ServerSocket welcomeSocket3 = new ServerSocket(5680);
        ServerSocket welcomeSocket4 = new ServerSocket(5681);
        for(int i = 0;i<6;i++){
            threads[i] =new connection(null,null);
        }

        while(true)
        {
            Socket skt = welcomeSocket.accept();
            Socket skt2=welcomeSocket2.accept();
            Socket skt3 =welcomeSocket3.accept();
            Socket skt4 = welcomeSocket4.accept();
            WorkerThread wt = new WorkerThread(skt,skt2,skt3,skt4,id);
            wt.Copy(wt);
            Thread t = new Thread(wt);
            t.start();
            count++;
            System.out.println("Client : "+id+"got connected.Total threads : "+count);
            id++;
        }
    }
}

class WorkerThread extends TCPServer implements Runnable
{
    public WorkerThread wt;
    private int id;
    public Socket skt;
    public Socket skt2;
    public Socket skt3;
    public Socket skt4;
    public String threadID;
    public DataOutputStream toClient;
    public DataOutputStream toClient2;
    public DataOutputStream toClient3;
    public DataOutputStream toClient4;
    public BufferedReader inFromClient;
    public BufferedReader inFromClient2;
    public BufferedReader inFromClient3;
    public BufferedReader inFromClient4;
    int thisID=0;

    public WorkerThread(Socket skt,Socket skt2, Socket skt3,Socket skt4,int id)
    {
        this.id=id;
        this.skt=skt;
        this.skt2=skt2;
        this.skt3 = skt3;
        this.skt4 =skt4;
    }
    public void Copy(WorkerThread wt){
        this.wt = wt;
    }

    public class Sendfile extends TCPServer implements Runnable{
        public Sendfile(){
            try{
                inFromClient3 = new BufferedReader(new InputStreamReader(skt3.getInputStream()));
                toClient3 = new DataOutputStream(skt3.getOutputStream());
            }
            catch (Exception e){

            }
        }
        @Override
        public void run(){
            while(true){
                try{
                    System.out.println("waiting for receiver to receive.\n");
                    String in = inFromClient3.readLine();
                    if(in!=null && in.compareTo("")!=0){
                        String[]Array = in.split(" ");
                        int Fileid= Integer.parseInt(Array[0]);
                        String FileName=Array[2];int FileSize=0;
                        if(Array[1].compareTo("Y")==0){
                            if(Fileid==-1){
                                toClient3.writeBytes("Z"+'\n');
                                System.out.println("file not found\n");break;
                            }else{
                                toClient3.writeBytes("incoming!\n");
                                String name=Array[0]+"_"+FileName;
                                File file  = new File(name);FileSize=(int)file.length();
                                FileInputStream FI = new FileInputStream(file.getName());
                                byte[] toSend = new byte[FileSize];FI.read(toSend);FI.close();
                                int n=0;
                                while(n<FileSize){
                                    int x = gobackN(n,toSend,toClient3);
                                    String s = inFromClient3.readLine();
                                    int  p = Integer.parseInt(s);
                                    n=p;
                                    System.out.println("Frame : "+n+'\n');
                                }
                                for(int i=0;i<20;i++){
                                    if(T[i]!=null && T[i].FileID==Integer.parseInt(Array[0])){
                                        T[i]=null;break;
                                    }
                                }
                                file.delete();

                            }
                        }else if(Array[1].compareTo("NO")==0){
                            String name = Array[0]+"_"+Array[2];
                            File file = new File(name);
                            file.getAbsoluteFile().delete();
                            toClient3.writeBytes("T\n");
                        }
                        else{
                            toClient3.writeBytes("F\n");
                        }
                    }
                }
                catch (Exception e){break;}
            }

        }
    }
    public class SendInbox extends TCPServer implements Runnable{
        SendInbox(){
            try{
                toClient4 = new DataOutputStream(skt4.getOutputStream());
            }
            catch (Exception e){

            }
        }
        @Override
        public void run(){
            while(true){
                String msg="";
                for(int i = 0; i <20;i++){
                    if(T[i]!=null && T[i].receiver.compareTo(threadID)==0){
                        msg=msg+" "+T[i].FileName+" "+T[i].FileSize+" "+T[i].sender+" "+T[i].FileID;
                    }
                }
                if(msg!=null && msg!=""){
                    try{
                        toClient4.writeBytes(msg+'\n');
                    }
                    catch (Exception e){ }
                }
            }
        }
    }
    public class SendOnlineList extends TCPServer implements Runnable {
        public SendOnlineList(){ }
        @Override
        public void run(){
            try{
                toClient2 = new DataOutputStream(skt2.getOutputStream());
            }
            catch (Exception e){
                e.printStackTrace();
            }
            while(true){
                int x=0;String S="";
                for(int i = 0;i<6;i++){
                    if(Online[i]!=null && Online[i]!=threadID){
                        S = S+" "+Online[i];
                    }
                }
                if(S!=null && S.compareTo("")!=0){
                    try{
                        toClient2.writeBytes(S+'\n');
                    }
                    catch (Exception e){

                    }
                }else{
                    try{
                        toClient2.writeBytes("Z"+'\n');
                    }
                    catch (Exception e){
                        break;
                    }
                }
                try{
                    Thread.sleep(5000);
                }
                catch (Exception e){
                    break;
                }
            }
        }
    }
    public class ReceiveFile {
        String fileName,Sender,Receiver;int fileID,fileSize;
        ReceiveFile(String fileName,String Sender,String Receiver,int fileID,int fileSize){
            this.fileName=fileName;this.Sender=Sender;
            this.Receiver = Receiver;this.fileID=fileID;
            this.fileSize = fileSize;
        }
        public void run(){
            try{
                if(Online[Integer.parseInt(Receiver)-80]==null){
                    toClient.flush();
                    toClient.writeBytes("Error : 101. Receiver not Online.\n");
                }
                else if(fileSize>maxSize){
                    toClient.flush();
                    toClient.writeBytes("Error : 102. Maximum filesize "+Integer.toString(maxSize)+"B.\n");
                }
                else{

                    if(Online[Integer.parseInt(Receiver)-80]!=null){
                        int totalChunk=0;int maxChunkSize=0;
                        try{
                            toClient.flush();
                            toClient.writeBytes(Integer.toString(maxChunkSize)+" "+Integer.toString(fileID)+'\n');
                            int n=0;
                            while(n<fileSize){

                                n = writeN(n,byteArray,inFromClient);
                                toClient.writeBytes(Integer.toString(n)+'\n');
                            }
                            DataOutputStream DO = new DataOutputStream(new FileOutputStream(fileID+"_"+fileName));
                            DO.write(byteArray,0,fileSize);
                            DO.close();FileCount++;
                            Transmission t = new Transmission();
                            t.sender = threadID;
                            t.receiver = Receiver;
                            t.FileSize = fileSize;
                            t.FileName = fileName;
                            t.FileID=fileID;
                            try{
                                toClient4 = new DataOutputStream(threads[Integer.parseInt(Receiver)-80].wt.skt4.getOutputStream());
                                toClient4.writeBytes(fileName+" "+fileSize+" "+threadID+" "+fileID+'\n');
                            }
                            catch (Exception e){
                                //e.printStackTrace();
                            }

                        }
                        catch (Exception e){
                            System.out.println("Error chunk.\n");
                        }

                    }
                    else{
                        toClient.writeBytes("Error : 101.1. Receiver gone Offline.\n");
                    }

                }
            }
            catch (Exception e){
                System.out.println("Exception Receiving File.\n");
            }
        }
    }
    public void logIN(){
        String s1;
        String s2;
        try{
            inFromClient = new BufferedReader(
                    new InputStreamReader(skt.getInputStream()));
            toClient = new DataOutputStream(skt.getOutputStream());
            toClient.writeBytes("you are connected. Enter ID to login"+'\n');

            while(true)
            {
                int loop = 1;
                s1=inFromClient.readLine();
                for(int i = 0;i<6;i++){
                    if(student[i].compareTo(s1)==0 && Online[i]==null){
                        try{
                            toClient.writeBytes("Connected\n");
                        }
                        catch (Exception e){}
                        threadID = student[i];
                        Online[i] = threadID;
                        threads[i] = new connection(wt,threadID);
                        threads[i].request=false;
                        thisID = i;
                        loop=0;
                        break;
                    }
                    else if(i==5){
                        toClient.writeBytes("Error 104 : Wrong ID. Please try again."+'\n');
                        break;

                    }
                    else if (student[i].compareTo(s1)==0 && Online[i]!=null){
                        toClient.writeBytes("Error 105 : You are already loggedin from a different ip address.\n");
                        break;
                    }

                }
                if(loop == 0){
                    break;
                }

            }
        }
        catch (Exception e){System.out.println("Caught Exception while logging in.\n");}
    }
    public static int toBytes(String s){
    //System.out.println("toBytes received : "+s+'\n');
    int res=0;
    for(int i=0;i<s.length();i++){
        if(s.charAt(i)=='1'){
            res=res+(int)Math.pow(2,s.length()-i-1);
        }
    }
    //System.out.println("toBytes creates : "+res+'\n');
    return res;
}
    public static String bitDestuff(String s) {
        int total1 = 0;int con1 = 0;String res = "";
        for (int i = 0; i < s.length(); i++) {
            if (con1 == 5 && s.charAt(i) == '0') {
                con1 = 0;
            } else if (s.charAt(i) == '1') {
                con1++;
                total1++;
                res = res + "1";
            } else {
                con1 = 0;
                res = res + "0";
            }


        }
        //System.out.println("bitDestuff creates : "+res+'\n');
        return res;
    }
    public static String getPayload(String s,int ft,int sq,int ak){

        //s has 0th char as frametype
        //then 4 chars as frame seqNo
        //then 4 chars as frame ackNo
        //s has last 4 chars as checkSum
        int frameType=-3,seqNo=-3,ackNo=-3,checkSum=-3;
        String res="";int no1=0;String tmp="";
        frameType = Integer.parseInt("0"+s.charAt(0));
        if(frameType != ft){
            System.out.println("Frametype mismatched for the Frame with ftype "+ft+" sqno "+sq+
                    " ackno "+ak+'\n'); return"E";
        }
        for(int i=1;i<5;i++){
            tmp = tmp+s.charAt(i);

        }
        seqNo = Integer.parseInt(tmp);
        if(sq != seqNo){
            System.out.println("SeqNo mismatched for the Frame with ftype "+ft+" sqno "+sq+
                    " ackno "+ak+'\n');return "E";
        }
        tmp = "";
        for(int i=5;i<9;i++){
            tmp = tmp+s.charAt(i);
        }

        ackNo = Integer.parseInt(tmp);
        if(ackNo != ak){
            System.out.println("AckNo mismatched for the Frame with ftype "+ft+" sqno "+sq+
                    " ackno "+ak+'\n'); return "E";
        }
        tmp="";
        for(int i=9;i<s.length()-4;i++){

            res = res+s.charAt(i);
            if(s.charAt(i)=='1'){
                no1++;
            }
        }
        for(int i=s.length()-4;i<s.length();i++){
            tmp = tmp+s.charAt(i);
        }
        checkSum = Integer.parseInt(tmp);
        if(no1!=checkSum){
            System.out.println("Checksum mismatched for the Frame with ftype "+ft+" sqno "+sq+
                    " ackno "+ak+'\n');return "E";
        }
        System.out.println("getPayload creates : "+res+'\n');
        return res;
    }
    public static int unFrame(String frame,int ft,int sq,int ak){

        String res = "";
        for(int i =8;i< frame.length()-8;i++){
            res = res+frame.charAt(i);

        }
        System.out.println("unFrame creates : "+res+'\n');
        res = getPayload(res,ft,sq,ak);
        if(res.compareTo("E")==0){
            return -1;
        }
        res=bitDestuff(res);
        int x = toBytes(res);
        return x;
    }
    public static int writeN(int n, byte[]Array, BufferedReader BR){
        try{
            String in = BR.readLine();String res="";
            int last1=0;int g=0;int bytesrec=0;

            for(int i=0;i<in.length();i++){
                res=res+in.charAt(i);
                if(in.charAt(i)=='1'){last1++;}
                else {last1=0;}
                if(last1==6){last1=0;g++;i++;res=res+'0';}
                if(g==2){
                    g=0;
                    System.out.println("Frame  : "+res+'\n');
                    int x = unFrame(res,1,n,n);
                    if(x==-1) break;
                    else {
                        Array[n]=(byte)x;
                        n++;//bytesrec++;
                        //System.out.println("bytes  rec : "+bytesrec+'\n');
                    }
                    res="";
                }
            }
            return n;
        }
        catch (Exception e){return n;}
    }

    /*public static String toBits(int b){
        String s = "";
        while(b!=0){
            int i = (b&1);
            if(i==1)s="1"+s;
            else s="0"+s;
            b=b>>1;
        }
        while(s.length()<8){
            s="0"+s;
        }
        //System.out.println("t0Bits creates : "+s+'\n');
        return s;
    }
    public static int getcheckSum(int data){
        String s = toBits(data);
        int no1=0;
        for(int i=0;i<s.length();i++){
            if(s.charAt(i)=='1'){
                no1++;
            }
        }
        return no1;
    }
    public static String makePayload(int data){
        String res =toBits(data);
        res = bitSuff(res);
        //System.out.println("makePayload creates : "+res+'\n');
        return res;
    }
    public static String bitSuff(String s){
        int total1 =0; int con1 = 0; String res="";
        for(int i =0;i<s.length();i++) {
            if (s.charAt(i) == '1') {
                con1++;
                total1++;
                res = res + "1";
            } else {
                con1 = 0;
                res = res + "0";
            }
            if (con1 == 5) {
                res = res + "0";
                con1 = 0;
            }

        }
        //System.out.println("bitStuff creates : "+s+'\n');
        return res;
    }
    public static String makeFrame(int frameType,int seqNo,int ackNo,String payLoad,int checkSum){
        String res="";
        String seq = Integer.toString(seqNo);
        while(seq.length()<4){ seq="0"+seq; }

        String ack = Integer.toString(ackNo);
        while(ack.length()<4){ ack="0"+ack; }

        String ckS = Integer.toString(checkSum);
        while(ckS.length()<4){ ckS="0"+ckS; }

        String fT = Integer.toString(frameType);
        res="01111110"+fT+seq+ack+payLoad+ckS+"01111110";
        //System.out.println("makeFrmae creats : "+res+'\n');
        return res;
    }
    public static int gobackN(int n, byte[]Array, DataOutputStream DOS){
        int length=Array.length; String data="";int i=0;
        for(i=0;i<5;i++){
            if(n+i<Array.length){
                int sq=i+n; int ack=i+n;
                data=data+makeFrame(1,sq,ack,makePayload((int)Array[n+i]),getcheckSum((int)Array[n+i]));
            }
            else break;
        }
        try{
            DOS.writeBytes(data+'\n');return i+n-1;
        }
        catch (Exception p){return n;}
    }*/

    public static String toBits(int b){
        String s = "";
        while(b!=0){
            int i = (b&1);
            if(i==1)s="1"+s;
            else s="0"+s;
            b=b>>1;
        }
        while(s.length()<8){
            s="0"+s;
        }
        //System.out.println("t0Bits creates : "+s+'\n');
        return s;
    }
    public static int getcheckSum(int data){
        String s = toBits(data);
        int no1=0;
        for(int i=0;i<s.length();i++){
            if(s.charAt(i)=='1'){
                no1++;
            }
        }
        return no1;
    }
    public static String makePayload(int data){
        String res =toBits(data);
        res = bitSuff(res);
        //System.out.println("makePayload creates : "+res+'\n');
        return res;
    }
    public static String bitSuff(String s){
        int total1 =0; int con1 = 0; String res="";
        for(int i =0;i<s.length();i++) {
            if (s.charAt(i) == '1') {
                con1++;
                total1++;
                res = res + "1";
            } else {
                con1 = 0;
                res = res + "0";
            }
            if (con1 == 5) {
                res = res + "0";
                con1 = 0;
            }

        }
        //System.out.println("bitStuff creates : "+s+'\n');
        return res;
    }
    public static String makeFrame(int frameType,int seqNo,int ackNo,String payLoad,int checkSum){
        String res="";
        String seq = Integer.toString(seqNo);
        while(seq.length()<4){ seq="0"+seq; }

        String ack = Integer.toString(ackNo);
        while(ack.length()<4){ ack="0"+ack; }

        String ckS = Integer.toString(checkSum);
        while(ckS.length()<4){ ckS="0"+ckS; }

        String fT = Integer.toString(frameType);
        res="01111110"+fT+seq+ack+payLoad+ckS+"01111110";
        //System.out.println("makeFrmae creats : "+res+'\n');
        return res;
    }
    public static int gobackN(int n, byte[]Array, DataOutputStream DOS){
        int length=Array.length; String data="";int i=0;
        for(i=0;i<5;i++){
            if(n+i<Array.length){
                int sq=i+n; int ack=i+n;
                data=data+makeFrame(1,sq,ack,makePayload((int)Array[n+i]),getcheckSum((int)Array[n+i]));
            }
            else break;
        }
        try{
            DOS.writeBytes(data+'\n');return i+n-1;
        }
        catch (Exception p){return n;}
    }
    @Override
    public void run()
    {

        logIN();
        try {
            Thread tr = new Thread(new SendOnlineList());
            tr.start();
        }
        catch (Exception e) {
            //e.printStackTrace();
        }
        try{
            Thread send  = new Thread(new Sendfile());
            send.start();
        }
        catch (Exception e){}
        while(true){

            try{
                String s1;
                s1 = inFromClient.readLine();
                //
                String[] A;
                A=s1.split(" ");
                if(A[0].compareTo("-3")==0){
                    System.out.println("Array Size : "+A.length+"\n");
                    ReceiveFile rF = new ReceiveFile(A[2],threadID,A[1],FileCount,Integer.parseInt(A[3]));
                    rF.run();
                }

                else if(A[0].compareTo("1")==0){

                }
            }
            catch(Exception e){
                break;
            }

        }
        /*String s1;
        String s2;
        String FileName=null;
        int thisID =0;
        int fSize = 0;


        try
        {
            inFromClient = new BufferedReader(
                    new InputStreamReader(skt.getInputStream()));
            DataInputStream iFC = new DataInputStream(skt.getInputStream());
            toClient = new DataOutputStream(skt.getOutputStream());
            toClient.writeBytes("you are connected. Enter ID to login"+'\n');

            while(true)
            {
                int loop = 1;
                s1=inFromClient.readLine();
                for(int i = 0;i<6;i++){
                    if(student[i].compareTo(s1)==0 && Online[i]==null){

                        toClient.writeBytes("You are logged in. Your id is "+student[i].toString()+" Press S to sender mode or R to check inbox.\n");
                        threadID = student[i];
                        Online[i] = threadID;
                        threads[i] = new connection(wt,threadID);
                        threads[i].request=false;
                        thisID = i;

                        loop=0;
                        break;
                    }
                    else if(i==5){
                        toClient.writeBytes("Wrong ID. Please try again."+'\n');
                        break;

                    }
                    else if (student[i].compareTo(s1)==0 && Online[i]!=null){
                        toClient.writeBytes("You are already loggedin from a different ip address.\n");
                        break;
                    }

                }
                if(loop == 0){
                    break;
                }

            }




            while(true){

                s1 = inFromClient.readLine().toUpperCase();

                if(s1.compareTo("S")==0)
                {   toClient.writeBytes("Enter receiver ID.\n");
                    s1 = inFromClient.readLine();
                    for(int i = 0;i<6;i++){
                        String receiver = threads[i].threadID;
                        if (threads[i]!=null&& threads[i].threadID!=null && receiver.compareTo(s1)==0)    {
                            toClient.writeBytes("Receiver online. Send Filename<space>Filesize.\n");
                            threads[i].request =true;
                            s1 = inFromClient.readLine();
                            int size;
                            String []A = new String[2];
                            A = s1.split(" ");
                            FileName = A[0];size = Integer.parseInt(A[1]);
                            int maxSize = 10000;

                            int totalChunk=0;int maxChunkSize=0;
                            if(maxSize-size>=0){


                                if(size<=50){
                                    totalChunk = 1;maxChunkSize = size;
                                }
                                else if (size>50){
                                    if(size%50==0){
                                        totalChunk = (size/50);
                                        maxChunkSize = 50;
                                    }


                                    else
                                    {
                                        totalChunk = (size/50)+1;
                                        maxChunkSize = 50;
                                    }

                                }

                                byte[]buffer = new byte[10000];
                                file f  = new file(FileCount,size,receiver,threadID,FileName);

                                toClient.writeBytes(Integer.toString(maxChunkSize)+" "+Integer.toString(FileCount)+'\n');
                                int x = 0;
                                int y = maxChunkSize;
                                FileOutputStream FO = new FileOutputStream(FileName);
                                InputStream IS = skt.getInputStream();
                                int bytesRead =0;int total = 0;

                                int flag = 1;
                                while(total<size){


                                    bytesRead=IS.read(buffer,x,y);

                                    total =total + y;
                                    x= x+y;
                                    if(size-total<=maxChunkSize){
                                        y = size-total;
                                    }
                                    else{
                                        y = maxChunkSize;
                                    }



                                    toClient.writeBytes("received chunk "+flag+" of "+totalChunk+'\n');

                                    flag++;
                                }
                                System.out.println("Total bytes read = "+total+" \n");
                                FO.write(buffer,0,size);
                                FO.close();


                                FileCount++;
                                Transmission t = new Transmission();
                                t.sender = threadID;
                                t.receiver = receiver;
                                t.FileSize = fSize;
                                t.FileName = FileName;
                                T[Integer.parseInt(t.receiver)-80]=t;



                            }

                            else toClient.writeBytes("Sorry. Filesize overflowed.\n");
                            break;
                        }
                        else if(i==5){
                            toClient.writeBytes("Receiver is not online. Try another receiver.\n");
                            break;
                        }


                    }
                }
                else if(s1.compareTo("R")==0){

                    if(threads[thisID].request ==true){



                        Transmission t = new Transmission();
                        t = T[thisID];
                        File f = new File(t.FileName);
                        fSize = (int)f.length();

                        Request(t.sender,t.FileName,Integer.toString(fSize));

                        toClient.writeBytes("Press S to sender mode or R to check inbox.\n");

                    }
                    else
                        toClient.writeBytes("No incoming file. S to send R to check inbox\n");
                }
                else toClient.writeBytes("Press S to sender mode or R to check inbox.\n");


            }
        }
        catch(Exception e)
        {

        }*/


    }

}
