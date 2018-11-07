/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatclient;

import java.io.*;
import java.util.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
public class ChatClient implements Runnable{

    private static Socket clientsocket = null;
    private static PrintStream os = null;
    private static DataInputStream is = null;
    private static DataOutputStream dout = null;
    private static BufferedReader inputLine = null;
    private static boolean closed = false;
    
    private static int hrdiff=0,mndiff=0,secdiff=0;
    
    private int wanttochange = 0;
    
    
    public static void main(String[] args) {
        int portNumber = 5000;
        String host = "localhost";
        
        try{
            clientsocket = new Socket(host,portNumber);
            inputLine = new BufferedReader(new InputStreamReader(System.in));
            os = new PrintStream(clientsocket.getOutputStream());
            is = new DataInputStream(clientsocket.getInputStream());
            dout = new DataOutputStream(clientsocket.getOutputStream());
        }catch(UnknownHostException e){
            System.err.println("Dont know about host : "+host);
        }catch(IOException e){
            System.err.println("IOException occurs");
        }
        
        if(clientsocket != null && is != null && os != null){
                new Thread(new ChatClient()).start();
        }
    }
    
    @Override
    public void run() {
        try{
            os.println(inputLine.readLine());
            os.flush();
            String getmsg = is.readLine();
            System.out.println(getmsg);
            
            if(getmsg.equals("Logged In")){
                System.out.println("send or receive?");
                String sendorreceive = inputLine.readLine();
                os.println(sendorreceive);
                os.flush();
                
                if(sendorreceive.equals("send")){
                    System.out.println("Enter receiver id");
                    String receiverid;
                    receiverid = inputLine.readLine();
                    os.println(receiverid);
                    os.flush();
                    
                    String available = is.readLine();
                    System.out.println(available);
                    if(available.equals("Enter File Name")){
                        String filename = inputLine.readLine();
                        os.println(filename);
                        os.flush();
                        System.out.println("Enter file size");
                        String filesize = inputLine.readLine();
                        os.println(filesize);
                        os.flush();
                        String filesizeresponse = is.readLine();
                        System.out.println(filesizeresponse);
                        if(filesizeresponse != "Overflow Occured"){
                            int  maxchunk = Integer.valueOf(filesizeresponse);
                            String fileid = is.readLine();
                            File f=new File(filename);
                            FileInputStream fin=new FileInputStream(f);
                            byte prevb[]=new byte[maxchunk];
                            int read;
                            DateFormat dateformat = new SimpleDateFormat("HH:mm:ss");
                            String prevtime = null;
                            String nexttime = null;
                            int sendlength = 0;
                            if(maxchunk>=15 && maxchunk<20){
                                sendlength = maxchunk + 9;
                            }
                            else if(maxchunk>=20 && maxchunk<26){
                                sendlength = maxchunk + 10;
                            }
                            else{
                                sendlength = maxchunk + 11;
                            }
                            
                            int counter = 0;
                            byte newb[] = new byte[sendlength];
                            byte store[] = new byte[sendlength];
                            String ack = "00000000";
                            while(true){
                                
                                
                                if(ack.equals("00000000")){
                                    System.out.println("new");
                                    
                                    read = fin.read(prevb);
                                    int len=0;
                                    String st="";
                                    String newstring = "";
                                    
                                    if(read!=-1){
                                        
                                        //System.out.println(read);
                                        
                                        for(int i=0;i<prevb.length;i++){
                                            byte bt = prevb[i];
                                            Object ob = convert(st,len,bt);
                                            st = ob.s;
                                            len = ob.len;
                                        }
                                        
                                        len = calculateChecksum(st);
                                        
                                        System.out.println(st);
                                        
                                        newstring = st;
                                        
                                        if(wanttochange==0){
                                            String change = inputLine.readLine();
                                            if(change.equals("no")){
                                                wanttochange = 1;
                                            }
                                            else if(!change.equals("-1")){
                                                int chn = Integer.valueOf(change);
                                                if(st.charAt(chn)=='0'){
                                                    newstring = st.substring(0, chn)+'1'+st.substring(chn+1);
                                                }
                                                else{
                                                    newstring = st.substring(0, chn)+'0'+st.substring(chn+1);
                                                }
                                            }
                                        }
                                        
                                        for(int i=7;i>=0;i--){
                                            int j=len & (1<<i);
                                            if(j==0){
                                                st = st + '0';
                                                newstring = newstring +'0';
                                            }
                                            else{
                                                st = st + '1';
                                                newstring = newstring +'1';
                                            }
                                        }
                                        
                                        st = "00000000" + st;
                                        newstring = "00000000" + newstring;
                                        
                                        String secondst = "";
                                        
                                        for(int i=7;i>=0;i--){
                                            int j=counter & (1<<i);
                                            if(j==0){
                                                secondst = secondst + '0';
                                            }
                                            else{
                                                secondst = secondst + '1';
                                            }
                                        }
                                        
                                        st = secondst + st;
                                        newstring = secondst + newstring;
                                        
                                        st = "00000001" + st;
                                        newstring = "00000001" + newstring;
                                        
                                        System.out.println(newstring);
                                        
                                        st = bitStaffing(st);
                                        newstring = bitStaffing(newstring);
                                        
                                        
                                        st = "01111110" + st;
                                        st = st + "01111110";
                                        
                                        newstring = "01111110" + newstring;
                                        newstring = newstring + "01111110";
                                        
                                        st = AddZero(st,sendlength);
                                        newstring = AddZero(newstring,sendlength);
                                        
                                        System.out.println(newstring);
                                        
                                        store = ConvertToByteArray(st);
                                        newb = ConvertToByteArray(newstring);
                                        
                                        os.println("ok");
                                        os.flush();
                                        String sendme = is.readLine();
                                        if(sendme.equals("send me")){
                                            dout.write(newb, 0, newb.length); 
                                            dout.flush();
                                            Date date = new Date();
                                            prevtime = dateformat.format(date);
                                            counter++;
                                        }
                                        
                                    }
                                    else{
                                        os.println("file ended");
                                        os.flush();
                                        break;
                                    }
                                }
                                else{
                                    System.out.println("again");
                                    os.println("ok");
                                    os.flush();
                                    String sendme = is.readLine();
                                    if(sendme.equals("send me")){
                                        dout.write(store, 0, store.length); 
                                        dout.flush();
                                    }
                                }
                                //ack = is.readLine();
                                byte bt[] = new byte[8];
                                long readnum = is.read(bt,0,bt.length);
                                
                                ack = "";
                                for(int i=0;i<8;i++){
                                    Object obj = convert(ack,0,bt[i]);
                                    ack = obj.s;
                                }
                                
                                ack = bitDestuffing(ack);
                                ack = ack.substring(24, 32);
                                
                                Date date = new Date();
                                nexttime = dateformat.format(date);
                                timedifference(prevtime,nexttime);
                                if(hrdiff>0|mndiff>0|secdiff>30){
                                    os.println("timeout");
                                    os.flush();
                                    break;
                                }
                            }
                            fin.close();
                        }
                    }
                }
                else{
                    int maxchunk = Integer.valueOf(is.readLine());
                    int totalpart = Integer.valueOf(is.readLine());
                    String key = is.readLine();
                    String filename = is.readLine();
                    filename = "Client"+filename;
                    String receiver = is.readLine();
                    String permission = is.readLine();
                    System.out.println(permission);
                    String yesorno = inputLine.readLine();
                    os.println(yesorno);
                    os.flush();
                    if(yesorno.equals("yes")){
                        os.println(maxchunk);
                        os.flush();
                        os.println(totalpart);
                        os.flush();
                        os.println(key);
                        os.flush();
                        os.println(receiver);
                        os.flush();

                        byte b[]=new byte [maxchunk];
                        FileOutputStream fos=new FileOutputStream(new File(filename),true);
                        long bytesRead;
                        DataInputStream din=new DataInputStream(clientsocket.getInputStream());
                        for(int count = 1 ; count <= totalpart ; count++){
                            bytesRead = din.read(b, 0, b.length);
                            fos.write(b,0,b.length);
                        }
                        fos.close();

                    }
                }
            }              
            os.close();
            is.close();
            clientsocket.close();
            closed = true;
        }catch(IOException e){
            System.out.println("IOException : "+e);
        }
    }
    
    public int calculateChecksum(String st){
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
        num=~num;
        num = (num&((1<<8)-1));
        return num;
    }
    
    public Object convert(String st,int num,byte b){
        for(int i=7;i>=0;i--){
            int zer = b & (1<<i);
            if(zer==0){
                st = st + '0';
            }
            else{
                st = st + '1';
                num++;
            }
        }
        Object ob = new Object(st,num);
        return ob;
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
    
    public void timedifference(String time1,String time2)
    {
        String ara1[] = time1.split(":");
        String ara2[] = time2.split(":");
        int hr1 = Integer.valueOf(ara1[0]);
        int mn1 = Integer.valueOf(ara1[1]);
        int sec1 = Integer.valueOf(ara1[2]);
        
        int hr2 = Integer.valueOf(ara2[0]);
        int mn2 = Integer.valueOf(ara2[1]);
        int sec2 = Integer.valueOf(ara2[2]);
        
        if(sec2<sec1){
            secdiff += (sec2+60)-sec1;
            mndiff-=1;
        }
        else{
            secdiff += sec2-sec1;
        }
        
        if(mn2<mn1){
            mndiff += (mn2+60)-mn1;
            hrdiff -= 1;
        }
        else{
            mndiff += mn2-mn1;
        }
        
        if(hr2<hr1){
            hrdiff += (hr2+24)-hr1;
        }
        else{
            hrdiff += hr2-hr1;
        }
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
    
}
