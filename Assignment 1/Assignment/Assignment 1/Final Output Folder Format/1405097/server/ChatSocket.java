package chatsocket;

//import static com.oracle.jrockit.jfr.ContentType.StackTrace;
import java.io.*;
import java.net.*;
import java.util.*;

public class ChatSocket implements Runnable{
    
    Socket csocket;
    static HashMap<Integer,Socket> hm = new HashMap<Integer,Socket>();
    static HashMap<String,String> filemap = new HashMap<String,String>();
    static int k = 1;
    static int totalfilesize = 50000;
    static int currenttotalfilesize = 0;
    int sender;
    int error;
    ChatSocket(Socket csocket, int sender){
        this.csocket = csocket;
        this.sender = sender;
    }

    public static void main(String[] args) throws Exception{
        ServerSocket ssock = new ServerSocket(5000);
        System.out.println("Waiting For Client Request...");
        while(true){
            Socket sock = ssock.accept();
                try{
                BufferedReader firstin = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                String roll = firstin.readLine();
                int rollno = Integer.valueOf(roll);
                PrintWriter sendback;
                sendback = new PrintWriter(sock.getOutputStream(),true);
                if(hm.containsKey(rollno)){
                    sendback.println("LogIn Denied, User already logged in");
                    sendback.flush();
                }
                else{
                    ChatSocket chatsock = new ChatSocket(sock,rollno);
                    new Thread(chatsock).start();
                    hm.put(rollno,sock);
                    System.out.println("connected to client : "+rollno);
                    sendback.println("Logged In");
                    sendback.flush();
                }
            }catch(IOException e){    
            }
        }
    }

    @Override
    public void run() {
        try{
            PrintWriter out =  new PrintWriter(csocket.getOutputStream(),true);
            PrintWriter otherout = null;
            BufferedReader in = new BufferedReader(new InputStreamReader(csocket.getInputStream()));
            DataInputStream din=new DataInputStream(csocket.getInputStream()); 
            DataOutputStream dout1 = new DataOutputStream(csocket.getOutputStream());
            String inputLine;
            int randnum=0;
            int totalpart=0;
            String newkey=null;
            String receiverid = null;
            String filename;
            
            String sendorreceive = in.readLine();
            if(sendorreceive.equals("send")){
                receiverid = in.readLine();
                if(hm.containsKey(Integer.valueOf(receiverid))){
                    out.println("Enter File Name");
                    out.flush();
                    
                    filename = in.readLine();
                    String type = filename.substring(filename.length()-4);
                    String filesize = in.readLine();
                    currenttotalfilesize += Integer.valueOf(filesize);
                    
                    if(currenttotalfilesize > totalfilesize){
                        out.println("Overflow Occured");
                        out.flush();
                        currenttotalfilesize -= Integer.valueOf(filesize);
                    }
                    else{
                        Random rand = new Random();
                        randnum = rand.nextInt(16)+15;
                        int receivelength = 0;
                        if(randnum>=15 && randnum<20){
                                receivelength = randnum + 9;
                            }
                            else if(randnum>=20 && randnum<26){
                                receivelength = randnum + 10;
                            }
                            else{
                                receivelength = randnum + 11;
                            }
                        out.println(String.valueOf(randnum));
                        out.flush();
                        newkey = String.valueOf(sender).concat(receiverid);
                        newkey = newkey.concat(type);
                        //filename = newkey.concat(filename);
                        filemap.put(newkey, filename);
                        out.println(newkey);
                        out.flush();

                        if(Integer.valueOf(filesize)%randnum==0){
                            totalpart= Integer.valueOf(filesize)/randnum;
                        }
                        else{
                            totalpart= Integer.valueOf(filesize)/randnum+1;
                        }

                        //byte b[]=new byte [randnum*1024];
                        byte b[]=new byte [receivelength];
                        FileOutputStream fos=new FileOutputStream(new File(newkey),true);
                        long bytesRead;
                        
                        String ack;
                        int counter = 0;
                        bytesRead=receivelength;
                        do
                        {
                            String abc = in.readLine();
                            //System.out.println(abc);
                            if(abc.equals("ok")){
                                out.println("send me");
                                bytesRead = din.read(b, 0, b.length);
                                String s = "";
                                for(int m = 0;m<b.length;m++){
                                    s = convert(s,b[m]);
                                }
                                error = 0;
                                System.out.println(s);
                                s = bitDestuffing(s);
                                System.out.println(s);
                                String seq = s.substring(8, 16);
                                String acknow = s.substring(16,24);
                                String checksum= s.substring(s.length()-8,s.length());
                                byte newb[] = new byte[randnum];
                                newb = getByteArray(s);
                                
                                int checksumno = stringtoint(checksum);
                                String mainmsg = s.substring(24, s.length()-8);
                                int num = calculateChecksum(mainmsg,checksumno);
                                String sending = "0000000000000000"+seq;
                                
                                if(num==0){
                                    System.out.println("correct");
                                    fos.write(newb,0,newb.length);
                                    sending = sending + "0000000000000000";
                                    //out.println("send again");
                                    //out.flush();
                                }
                                else{
                                    System.out.println("error");
                                    sending = sending + "0000000100000001";
                                }
                                sending = bitStaffing(sending);
                                sending = "01111110"+sending+"01111110";
                                sending = AddZero(sending,8);
                                
                                byte bt[] = new byte[8];
                                bt = ConvertToByteArray(sending);
                                dout1.write(bt, 0, bt.length); 
                                dout1.flush();
                            }
                            else if(abc.equals("file ended")){
                                fos.close();
                                break;
                            }
                            else if(abc.equals("timeout")){
                                fos.close();
                                File f = new File(newkey);
                                f.delete();
                                break;
                            }
                        }while(true);
                        fos.close();
                        

                        if(hm.containsKey(Integer.valueOf(receiverid))){
                            System.out.println(receiverid);
                            Socket ser1 = hm.get(Integer.valueOf(receiverid));
                            otherout = new PrintWriter(ser1.getOutputStream(),true);
                            otherout.println(String.valueOf(randnum));
                            otherout.flush();
                            otherout.println(String.valueOf(totalpart));
                            otherout.flush();
                            String key = newkey;
                            otherout.println(key);
                            otherout.flush();
                            String sendingfile = filemap.get(newkey);
                            otherout.println(sendingfile);
                            otherout.flush();
                            otherout.println(receiverid);
                            otherout.flush();
                            otherout.println("Do you want to receive from "+sender);
                            otherout.flush();
                        }
                    }
                }
                else{
                    out.println("User Offline");
                    out.flush();
                }
            }
            else{
                String yesorno = in.readLine();
                if(yesorno.equals("yes")){
                    randnum = Integer.valueOf(in.readLine());
                    totalpart = Integer.valueOf(in.readLine());
                    String sendingfile = in.readLine();
                    String receiver = in.readLine();
                    
                    
                    File f=new File(sendingfile);
                    FileInputStream fin=new FileInputStream(f);
                    byte c[]=new byte[randnum];
                    int read;
                    Socket ser1 = hm.get(Integer.valueOf(receiver));
                    DataOutputStream dout = new DataOutputStream(ser1.getOutputStream());
                    while((read = fin.read(c)) != -1){
                        dout.write(c, 0, read); 
                        dout.flush(); 
                    }
                    fin.close();
                    f.delete();
                }
            }
            
        }catch(IOException e){
            System.out.println(e);
        }
    }
    
    static public int calculateChecksum(String st,int checksum){
        int num=0;
        for(int j=0;j<st.length();j+=8){
            String s = st.substring(j,j+8);
            int b=0;
            for(int i=0;i<=7;i++){
                if(s.charAt(i)=='1'){
                    b=b<<1;
                    b+=1;
                }
                else{
                    b=b<<1;
                }
            }
            num = num + b;
            if(num>255){
                num-=256;
                num+=1;
            }
        }
        
        num = num + checksum;
        num=~num;
        num = (num&((1<<8)-1));
        return num;
    }
    
    public String convert(String st,byte b){
        for(int i=7;i>=0;i--){
            int zer = b & (1<<i);
            if(zer==0){
                st = st + '0';
            }
            else{
                st = st + '1';
            }
        }
        return st;
    }
    
    public String bitDestuffing(String st){
        String s = "";
        int count = 0;
        
        if(st.substring(0, 8).equals("01111110")){
            
            int i=8;
            while(!st.substring(i, i+8).equals("01111110")){
                //System.out.println("yoo bro");
                if(count==5){
                    if(st.charAt(i)=='0'){
                        count=0;
                    }
                    else{
                        error = 1;
                    }
                }
                else{
                    if(st.charAt(i)=='0'){
                        count=0;
                        s = s + '0';
                    }
                    else{
                        count++;
                        s = s + '1';
                    }
                }
                i++;
            }
        }
        return s;
    }
    
    public byte[] getByteArray(String st){
        String temp="";
        for(int i=24;i<st.length()-8;i++){
            temp = temp + st.charAt(i);
        }
        //System.out.println(temp.length());
        
        int i = 0;
        byte bt[] = new byte[temp.length()/8];
        int j=0;
        while(i<temp.length()){
            int b=0;
            int count = 8;
            while(count!=0){
                if(temp.charAt(i)=='1'){
                    b=b<<1;
                    b+=1;
                }
                else{
                    b=b<<1;
                }
                count--;
                i++;
                
            }
            bt[j]=(byte)b;
            //System.out.println(bt[j]);
            j++;
        }
        //System.out.println();
        return bt;
        
    }
    
    public int stringtoint(String st){
        int num = 0;
        int i=0;
        while(i<st.length()){
            if(st.charAt(i)=='1'){
                num=num<<1;
                num+=1;
            }
            else{
                num=num<<1;
            }
            i++;
        }
        return num;
    }
    
    public int countCheckSum(String st){
        int count=0;
        for(int i=24;i<st.length()-8;i++){
            if(st.charAt(i)=='1'){
                count++;
            }
        }
        return count;
    }
    
    public String bitStaffing(String st){
        int count=0;
        String temp = "";
        for(int i=0;i<st.length();i++){
            if(st.charAt(i)=='0'){
                temp = temp + '0';
                count = 0;
            }
            else{
                temp = temp + '1';
                count++;
                if(count==5){
                    temp = temp + '0';
                    count = 0;
                }
            }
        }
        return temp;
    }
    
    public String AddZero(String st,int sendlength){
        while(st.length()!=(sendlength*8)){
            st = st + '0';
        }
        return st;
    }
    
    public byte[] ConvertToByteArray(String s){
        int i = 0;
        byte bt[] = new byte[s.length()/8];
        int j=0;
        while(i<s.length()){
            int b=0;
            int count = 8;
            while(count!=0){
                if(s.charAt(i)=='1'){
                    b=b<<1;
                    b+=1;
                }
                else{
                    b=b<<1;
                }
                count--;
                i++;
                
            }
            bt[j]=(byte)b;
            
            j++;
        }
        
        return bt;
    }
    
}
