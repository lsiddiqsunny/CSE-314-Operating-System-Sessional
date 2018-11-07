/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sserver;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;




public class Sserver implements Runnable{

    int maxFileSize =80000;
    int fileSize;
    String fileName=null;
    
    static class ob{
        int roll;
        Socket sock;
    }
    
    static ArrayList<ob> list=new ArrayList<ob>();
    
    
    int r;
    Socket soc;
    Sserver(int roll,Socket s){
        r=roll;
        soc=s;
    }
    
    
    //private static BufferedReader bf;
    //private static PrintWriter pw;
    static HashMap<String,String> filemap = new HashMap<String,String>();
    
    public static void main(String[] args) throws Exception {
        
        int Port = 2000;
        ServerSocket server_sock;
        
        char ch1 = 'a';         //for check...
        server_sock =new ServerSocket(Port);
        
        String this_out = null;
        char ch;
        while(true){
            
            int jh,kh;
            Socket s ;
            s = server_sock.accept();
            String buffering = null;
            BufferedReader buff=new BufferedReader(new InputStreamReader(s.getInputStream()));
            
            buffering = "is buffered reader okayy!!";
            PrintWriter pwr = new PrintWriter(s.getOutputStream(),true);
            
            
            pwr.println("Wanna Log in? Enter Student id: ");
            pwr.flush();
            
            
            int client_roll = Integer.valueOf(buff.readLine());   //taking roll from client...
            ob a = new ob();
            a.roll = client_roll;
            a.sock = s;
            int f = 0;
            for(int i=0;i<list.size();i++)
            {
                if(list.get(i).roll == client_roll)
                {
                    f=1;
                    break;
                }
            }
            switch(f){
                case 1:
                    pwr.println("NO log in access...User already logged in. ");
                    String sttt = null;
                    sttt = "doneeee";
                    pwr.flush();
                    break;
                default:
                    Sserver ss;
                    int in_ittt = 0;
                    ss = new Sserver(client_roll,s);
                    new Thread(ss).start();
                    pwr.println("Okay...You are now Logged in");
                    String feel;
                    feel = "this is a test";
                    System.out.println("Connection established with client :"+client_roll);   
                    list.add(a);
                    //pwr.println("Logged in");
                    pwr.flush();
                    break;
            }
            
            
        }       
    }
    
    public static int string_to_int(String s)
    {
        int sum = 0;
        for(int i=0;i<s.length();i++)
        {
            if(s.charAt(i)=='1')
            {
                sum= (int) (sum+ Math.pow(2,s.length()-i-1));
            }
            
        }
        return sum;
    }
    
    
    public static int checking(String s){
        int no=0;
        for(int i=0;i<s.length();i++){
            if(s.charAt(i)=='1') no++;
        }
        return no;
    }
    
    
    @Override
    public void run() {
        DataInputStream din = null;
        int aaa,bbb=200;                              //D_C
        BufferedReader bf = null;
        PrintWriter pw = null;
        int kkk=0;                                  //D_C
        String sttt="initial";                      //D_C
        PrintWriter pp =null;
        
        int my_count;                 //ekhane prob hoy kina!!! :/ 
        
        
        try {
            
            String todo = null;
            bf = new BufferedReader(new InputStreamReader(soc.getInputStream()));
            
            String response_todo=null;
            din = new DataInputStream(soc.getInputStream());
             
            String re_id_as_string;
            pw = new PrintWriter(soc.getOutputStream(),true);
            
            //din=new DataInputStream(soc.getInputStream());
            int rev_id;
            int flag=0;
            todo="wanna Send?...then Enter S or wanna Receive?...then Enter R";
            pw.println(todo);
            pw.flush();
            
            try {
                response_todo=bf.readLine();
                
                switch(response_todo)
                {    
                    
                    case "S":    
                        pw.println("Input Receiver ID: ");
                        //pw.flush();

                        flag=0;

                        pw.flush();

                        re_id_as_string = bf.readLine();
                        rev_id=Integer.valueOf(re_id_as_string);


                        for(int j=0;j<list.size();j++)
                        {
                            if(list.get(j).roll == rev_id)
                            {
                                flag=1;
                                break;

                            }

                        }

                        if(flag != 0){

                            pw.println("Please input file name: ");
                            pw.flush();


                            fileName=bf.readLine();      //filename ta client theke server e nilam

                            String fs="Input File Size: ";      //filesize input dite bolchi
                            
                            
                            pw.println(fs);
                            pw.flush();
                            Random random;
                            random = new Random();
                            
                            fileSize=Integer.valueOf(bf.readLine());

                            int max = random.nextInt(30)+1;      //chunk size in byte.....


                            int z = (fileSize*1024) % max;
                            int x = (fileSize*1024)/max;

                            switch(z){
                                case 0:
                                    my_count = x;
                                    break;
                                default:
                                    my_count = x+1;
                                    break;
                            }

                            byte by[];                         //
                            
                            pw.println(String.valueOf(max));
                            pw.flush();
                            
                            FileOutputStream fout;             //
                            String a = String.valueOf(r);
                            String b = re_id_as_string;

                            System.out.println(a+b);

                            int my_index = fileName.indexOf(".");
                            String extension = fileName.substring(my_index , fileName.length());

                            String identification;
                            identification= a+b+extension;              // file new id at server

                            filemap.put(identification, fileName);

                            by=new byte [max];                 //

                            File fi=new File(identification);
                            fout = new FileOutputStream(fi,true);
                            String uber;
                            long b_r;

                            int frame_no_print = 0;
                            int cycle_completed = 0;
                            int yo = 0;
                            int switch_flag = 0;

                            do{
                                int TEST = 100;
                                
                                uber = bf.readLine();
                                String ttsstt = null;
                                ttsstt = "print this";
                                
                                switch(uber){
                                    case "done":

                                        pw.println("push");

                                        switch_flag = 0;

                                        //receiving the frame

                                        String received_stuffed;
                                        received_stuffed = bf.readLine();
                                        
                                        
                                        String main_part = received_stuffed.substring(8, received_stuffed.length()-8);

                                        //deStuffing......

                                        String destuffed_str = "";
                                        int ones = 0,prev = 0;

                                        for(int i=0;i<main_part.length();i++)
                                        {
                                            if(main_part.charAt(i) == '1')
                                            {
                                                ones++;
                                                if(ones ==5)
                                                {
                                                    destuffed_str = destuffed_str + main_part.substring(prev, i+1);
                                                    prev = i+2;
                                                    i++;
                                                    ones = 0;
                                                }
                                            }
                                            else ones = 0;
                                        }
                                        if(prev != main_part.length())
                                        {
                                            destuffed_str = destuffed_str + main_part.substring(prev, main_part.length());
                                        }

                                        //extracting fields

                                        String kind_of_frame = destuffed_str.substring(0, 8);
                                        String sequence_no = destuffed_str.substring(8,16);
                                        String ack_no = destuffed_str.substring(16,24);

                                        String payload = destuffed_str.substring(24, destuffed_str.length()-8);

                                        String check_sum = destuffed_str.substring(destuffed_str.length()-8, destuffed_str.length());


                                        //verifying check sum....

                                        String ackno = "00000000";         //paisi..next frame pathao er jonno use korbo
                                        //String re_ackno = "00000001";      //send same frame er jonno

                                        String ackw = "";

                                        int test = checking(payload);
                                        if(test == string_to_int(check_sum))
                                        {
                                            //System.out.println("framee: "+yo);
                                            yo++;

                                            if(string_to_int(sequence_no) == 255 )
                                            {
                                                frame_no_print = string_to_int(sequence_no)+ cycle_completed*255;
                                                System.out.println("Frame received : "+ frame_no_print);
                                                cycle_completed++;
                                            }
                                            else{

                                                if(string_to_int(sequence_no) == 0)
                                                {
                                                    frame_no_print = string_to_int(sequence_no)+ cycle_completed*255 + cycle_completed ;
                                                    System.out.println("Frame received : "+ frame_no_print);
                                                }
                                                else
                                                {
                                                    frame_no_print = string_to_int(sequence_no)+ cycle_completed*255 + cycle_completed;
                                                    System.out.println("Frame received : "+ frame_no_print);
                                                }

                                            }
                                            
                                            //printing.....
                                            System.out.println("(Before Destuffing) Received Frame "+frame_no_print+": "+received_stuffed);
                                            
                                            String deee = "01111110"+ destuffed_str + "01111110";
                                            System.out.println("(After Destuffing)  Received Frame "+frame_no_print+": "+deee);
                                            
                                            
                                            
                                            //unstuffed ackwlgmnt...

                                            ackw ="00000001" + "00000000" + sequence_no + "00000000";


                                            //now bitstuffing ackw.... 


                                            String stuffed_ackw = "";
                                            String temp;
                                            int markss=0,prevss = 0;


                                            for(int i=0;i< ackw.length();i++)           
                                            {
                                                if(ackw.charAt(i)=='1')
                                                {
                                                    markss++;
                                                    if(markss == 5)
                                                    {
                                                        temp = ackw.substring(prevss,i+1);
                                                        stuffed_ackw = stuffed_ackw + temp + "0";
                                                        prevss = i+1;
                                                        markss = 0;
                                                    }
                                                }
                                                else markss = 0;

                                            }
                                            if(prevss != ackw.length())
                                            {
                                                stuffed_ackw = stuffed_ackw +ackw.substring(prevss, ackw.length());
                                            }

                                            stuffed_ackw ="01111110" + stuffed_ackw + "01111110";

                                            //forming byte array...

                                            int k=0;
                                            for(int j=0;j<max;j++)
                                            {
                                                int len= payload.length();
                                                String oka = payload.substring(k, k+8);
                                                k = k+8;
                                                int it = string_to_int(oka);
                                                byte my_b = (byte)it;
                                                by[j]=my_b;

                                            }
                                            fout.write(by,0,by.length);
                                            pw.println(stuffed_ackw);
                                            pw.flush();

                                        }
                                        else        //no ack sent..timeout hoye jacche...
                                        {
                                            System.out.println("Checksum Error Occurred...");
                                        }
                                        break;

                                    case "File sent...":
                                        
                                        fout.close();
                                        switch_flag = 1;
                                        break;
                                }
                                if(switch_flag == 1) break;


                            }while(true);
                            fout.close();


                            for(int j=0;j<list.size();j++)
                            {
                                if(list.get(j).roll == rev_id)
                                {
                                    String maximum = null;
                                    String no_of_chnks = null;
                                    Socket rev_sock = null;
                                    int len;
                                    String fi_name = null;
                                    fi_name = fileName;                 //........
                                            
                                    rev_sock = list.get(j).sock;
                                    
                                    
                                    maximum = String.valueOf(max);           //........
                                    no_of_chnks = String.valueOf(my_count);  //........
                                    pp = new PrintWriter(rev_sock.getOutputStream(),true);

                                    String to_send = fi_name + "-" +maximum +"-"+ no_of_chnks+"-"+String.valueOf(rev_id);
                                    pp.println(to_send);
                                    pp.flush();
                                    
                                    String msg_show =null;
                                    msg_show = "Write rev to receive";
                                    String my_promt =null;
                                    
                                    pp.println(msg_show);
                                    String promt_2 = null;
                                    pp.flush();
                                    
                                    break;

                                }

                            }


                        }
                        else{
                            pw.println("Receiver not logged in");
                            pw.flush();

                        }
                        break;

                    default:
                        
                        String resp = null;
                        resp = bf.readLine();
                        
                        String info_received;
                        
                        info_received =bf.readLine();
                        
                        String[] parts = info_received.split("-");

                        String part1 = parts[0];
                        String part2 = parts[1];
                        String part3 = parts[2];
                        String part4 = parts[3];
                        
                        File my_f;
                        FileInputStream fin;
                        
                        String f_name = part1;
                        int max_c = Integer.valueOf(part2);
                        int total_c = Integer.valueOf(part3);
                        int receiver_id = Integer.valueOf(part4);
                        
                        my_f = new File(f_name);
                        DataOutputStream dd;
                        int zerooo;
                        
                        fin = new FileInputStream(my_f);
                        String okzz = null;
                        byte k[] = new byte[max_c];
                        
                        
                        switch(resp)
                        {
                            case "rev":
                                Socket s=null;
                            
                                for(int j=0;j<list.size();j++)
                                {
                                    if(list.get(j).roll == receiver_id)
                                    {
                                        s = list.get(j).sock;

                                        int rr;
                                        dd = new DataOutputStream(s.getOutputStream());
                                        rr = fin.read(k);
                                        
                                        for(int p = 0;rr!= -1;p++)
                                        {
                                            dd.write(k, 0, rr); 
                                            dd.flush();
                                            rr= fin.read(k);
                                        }
                                        
                                        fin.close();
                                        my_f.delete();
                                        
                                        break;
                                    }

                                }
                                break;
                            default:
                                break;
                        }
                        
                        break;

                }
                
                
                
            } catch (IOException ex) {
            }
        } catch (IOException ex) {
            Logger.getLogger(Sserver.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                din.close();
            } catch (IOException ex) {
                Logger.getLogger(Sserver.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
         
        
        
    }
    
}
