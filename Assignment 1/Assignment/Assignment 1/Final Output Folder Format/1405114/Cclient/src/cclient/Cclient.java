/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cclient;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Cclient implements Runnable{
    
    private static BufferedReader bf;
    private static DataInputStream din;
    private static DataOutputStream dout;
    private static PrintStream ps;
    private static DataInputStream dis;
    private static Socket client_socket = null;
    
    public static void main(String[] args){
        
        
        try {
            bf = new BufferedReader(new InputStreamReader(System.in));
            String jack = "print here";
            client_socket = new Socket("localhost",2000);
            int binary = 1;
            din = new DataInputStream(client_socket.getInputStream());
            int k=0;
            //System.out.println("val_of_K :"+k);
            dout=new DataOutputStream(client_socket.getOutputStream());
            String non_bin = "testing unittt.....";
            ps= new PrintStream(client_socket.getOutputStream());
            
            dis = new DataInputStream(client_socket.getInputStream());
            
        } catch (IOException ex) {
            
        }
        
        String j = "print here";
        char c = 'j';
        new Thread(new Cclient()).start();     
        //System.out.println("j: "+j);        
        String moh = "end";
        
    }
    
    //byte to string.....
    
    public static String my_str(byte b){
        int mux=1;
        String st="";
        for(int k=0;k<8;k++)
        {
            if((b & mux)!=0){
                st= "1"+st;
            }
            else st = "0"+ st;
            mux = mux<<1;
        }
        
        return st;
    }
    
    //......
    public static int check_sum(String s){
        int no=0;
        for(int i=0;i<s.length();i++){
            if(s.charAt(i)=='1') no++;
        }
        return no;
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

    
    @Override
    public void run() {
        
        
        String s=null,fileName=null,r_id=null;
        String fileSize;
        try{
            String here = "dnt touch";
            
            String welc_msg=din.readLine();
            System.out.println(welc_msg);     //welc msg print dicchi
            
            s=bf.readLine();                 //roll user input nilam
            
        } catch (IOException ex) {
            System.out.println("IOException :"+ex);
        }
        ps.println(s);                       //roll taakee server e send korlam
        ps.flush();
        String msg;
        try {
            msg=din.readLine();             //already logged in or new login msg ta server theke nilam
            System.out.println(msg);        // ekhane print korlam 
            
        } catch (IOException ex) {
            System.out.println("IOException :"+ex);
        }
        
        
        try {
            String todo=null,response_todo=null;
            
            todo=din.readLine();
            System.out.println(todo);       //send naki receive print korlam
            
            String st_msg ="to receive in client../send to server ";
            response_todo = bf.readLine();   //S or R user input
            int my_seqqq = 0;
            ps.println(response_todo);       //server ke pathailam
            ps.flush();
            
            int seqno =0;
            switch(response_todo)
            {        
                case "S":
                    
                    String ask_rev_id;
                    String response;
                    ask_rev_id=din.readLine();
                    response = null;
                    System.out.println(ask_rev_id);   //server receiver_id chacche eta show korlam
 

                    r_id=bf.readLine();               //receiver_id user input  
                    String ffn = null;
                    
                    ps.println(r_id);                 //eta server ke send korlam -for server checking :receiver logged in kina
                    ps.flush();             
                                   
                    response = din.readLine();           //
                    //nor print the response......
                    System.out.println(response);

                    String timeStamp3 = null;
                    int timeStamp3_int;
                    String timeStamp4 = null;
                    int timeStamp4_int;
                    
                    switch(response)
                    {
                        case "Please input file name: ":

                            String fn,fs;

                            fileName = bf.readLine();       //filename user input

                            int okkk;
                            ps.println(fileName);         //filename server ke diam 
                            String f_n_m = null;
                            ps.flush();

                            fs=din.readLine();
                            System.out.println(fs);              // server filesize chacche

                            fileSize = bf.readLine();  //filesize user input
                            String f_s_z = fileSize;
                            ps.println(fileSize);      //  filesize server ke dilam
                            f_n_m = fileName;
                            ps.flush();

                            //today...

                            File f;
                            int dnt_cr;
                            FileInputStream fin;
                            
                            int chunk_size;
                            String chh_ss;
                            byte b[];   //byte_array
                            int r;
                            String acknw,anw;


                            f=new File(fileName);
                            String data = "00000000";
                            String ack  = "00000001";

                            chunk_size= Integer.valueOf(din.readLine());
                            fin = new FileInputStream(f);
                            
                            String timeStamp1 = "";
                            String timeStamp2 = "";
                            String prev_time = "";
                            String later_time="";
                            
                            b = new byte[chunk_size];

                            acknw="00000000";

                            String foo_str = "";
                            String stuffed_str = "";
                            seqno = 0;
                            int ack_print = 0;

                            int cycle_completed = 0;
                            int cy_com = 0;
                            int frame_no = 0;

                            while(true){
                                
                                String payload = null;
                                //initially.........
                                String func = null;
                                
                                if(acknw.equals("00000000")){

                                    payload = "";
                                    func = "";
                                        
                                    int ind =0;
                                    ind = fin.read(b);
                                    r = ind;
                                    int ck_sum = 0;
                                    if(r != -1){
                                        
                                        String p_lc= String.valueOf(ck_sum);
                                        ps.println("done");
                                        ps.flush();

                                        //....

                                        for(int j=0;j<(chunk_size);j++){

                                            func = my_str(b[j]);
                                            payload = payload + func;
                                        }



                                        byte bt = (byte)seqno;                   //seq_No.........
                                        String seq_string = my_str(bt);

                                        //System.out.println("seqqqqno "+ seqno);

                                        seqno++;                     //incrementing frame no...... 


                                        //check_sum calculation

                                        ck_sum = check_sum(payload);    
                                        byte check_byte = (byte)ck_sum;
                                        String check = my_str(check_byte);

                                        //unstuffed frame...

                                        foo_str = data + seq_string + "00000000" + payload + check;


                                        //determining frame_no..........
                                        if(string_to_int(seq_string) == 255 )
                                        {
                                            frame_no = string_to_int(seq_string)+ cy_com*255;

                                            cy_com++;
                                        }
                                        else{
                                            if(string_to_int(seq_string) == 0){
                                                frame_no = string_to_int(seq_string)+ cy_com*255 + cy_com;

                                            }
                                            else{
                                                frame_no = string_to_int(seq_string)+ cy_com*255 + cy_com;

                                            }

                                        }


                                        String data_frame_print = "01111110"+ foo_str + "01111110";
                                        System.out.println("(Before Bitstuffing) Data frame "+frame_no+": "+data_frame_print);

                                        //Bitstuffing.....

                                        stuffed_str = "";
                                        String temp;
                                        int mark=0,prev = 0;


                                        for(int i=0;i< foo_str.length();i++)               //foo_str holo ultimate string ta
                                        {
                                            if(foo_str.charAt(i)=='1')
                                            {
                                                mark++;
                                                if(mark == 5)
                                                {
                                                    temp = foo_str.substring(prev,i+1);
                                                    stuffed_str = stuffed_str + temp + "0";
                                                    prev = i+1;
                                                    mark = 0;
                                                }
                                            }
                                            else mark = 0;

                                        }
                                        if(prev != foo_str.length())
                                        {
                                            stuffed_str = stuffed_str +foo_str.substring(prev, foo_str.length());
                                        }

                                        stuffed_str ="01111110" + stuffed_str + "01111110";

                                        //.....

                                        System.out.println("(After Bitstuffing) Data frame "+frame_no+":  "+stuffed_str);

                                        String reply;
                                        reply = din.readLine();

                                        switch(reply)
                                        {
                                            case "push":
                                                
                                                ps.println(stuffed_str);
                                                ps.flush();

                                                timeStamp1 = new SimpleDateFormat("HH.mm.ss").format(new Date());
                                                prev_time = timeStamp1.substring(6, 8);
                                                break;

                                            default:
                                                break;
                                        }
                                        
                                    }
                                    else{
                                        ps.println("File sent...");
                                        ps.flush();
                                        break;
                                    }

                                    try{
                                        client_socket.setSoTimeout(30000);      
                                        acknw = din.readLine();       //acknowledgement receive korlam..
                                        
                                        timeStamp2 = new SimpleDateFormat("HH.mm.ss").format(new Date());
                                        
                                        String received_ackw =acknw;

                                        String main_part = received_ackw.substring(8, received_ackw.length()-8);  

                                        //acknw_destuffing....

                                        String destuffed_str = "";
                                        int ones = 0,prevs = 0;

                                        for(int i=0;i<main_part.length();i++)
                                        {
                                            if(main_part.charAt(i) == '1')
                                            {
                                                ones++;
                                                if(ones ==5)
                                                {
                                                    destuffed_str = destuffed_str + main_part.substring(prevs, i+1);
                                                    prevs = i+2;
                                                    i++;
                                                    ones = 0;
                                                }
                                            }
                                            else ones = 0;
                                        }
                                        if(prevs != main_part.length())
                                        {
                                            destuffed_str = destuffed_str + main_part.substring(prevs, main_part.length());
                                        }

                                        //acknw_destuffing done.....

                                        //Printttt........
                                        System.out.println("Time_Stamp_1 : "+timeStamp1);   //timestamp....
                                        
                                        String ack_number = destuffed_str.substring(16,24);

                                        if(string_to_int(ack_number) == 255 )
                                        {
                                            ack_print = string_to_int(ack_number)+ cycle_completed*255;
                                            System.out.println("Acknowledgement Received : "+ ack_print);
                                            cycle_completed++;
                                        }
                                        else{
                                            if(string_to_int(ack_number) == 0){
                                                ack_print = string_to_int(ack_number)+ cycle_completed*255 + cycle_completed;
                                                System.out.println("Acknowledgement Received : "+ ack_print);
                                            }
                                            else{
                                                ack_print = string_to_int(ack_number)+ cycle_completed*255 + cycle_completed;
                                                System.out.println("Acknowledgement Received : "+ ack_print);
                                            }

                                        }
                                        
                                        System.out.println("Time_Stamp_2 : "+timeStamp2 +"\n");   //timestamp.....
                                        
                                        acknw = "00000000";

                                    }catch(SocketException e){         

                                        //time_out_ocurred.................Now...

                                        String rply;
                                        rply = din.readLine();

                                        switch(rply)
                                        {
                                            case "push":
                                                ps.println(stuffed_str);
                                                ps.flush();
                                                break;
                                            default:
                                                break;
                                        }


                                        acknw ="00000000";

                                    }


                                }

                                else
                                {
                                    //no need ...ack nai....
                                }

                            }
                            break;

                        default:  
                            System.out.println("Receiver not logged in....Can't send file...");
                            break;
                            
                    }     
                    break;
                default:    
                
                    String info_received =null;
                    String file_key;
                    String rcvr_id;
                    int max_ch_size;
                    String file_name;
                    int total_no_of_chunks;
                    
                    info_received = din.readLine();
                    
                    String[] parts = info_received.split("-");
                    
                    String part1 = parts[0];
                    String part2 = parts[1];
                    String part3 = parts[2];
                    
                    file_name = part1;        //......
                    int ok;
                    ok = Integer.valueOf(part2);
                    max_ch_size = ok;         //.........
                    
                    String msg_show;
                    msg_show = din.readLine();
                    
                    ok = Integer.valueOf(part3);
                    
                    System.out.println(msg_show);
                    //total_no_of_chunks = ok;
                    
                    String final_press =null;
                    
                    final_press = bf.readLine();
                    total_no_of_chunks = ok;           //.......
                    
                    ps.println(final_press);
                    ps.flush();
                    
                    byte b[];
                    long oka;
                    String s_temp;
                    FileOutputStream fos;
                    
                    
                    s_temp = file_name;
                    file_name = "Received_".concat(file_name);
                    
                    b = new byte [max_ch_size];
                    File ef = new File(file_name);
                    fos = new FileOutputStream(ef,true);
                    
                    DataInputStream di=new DataInputStream(client_socket.getInputStream());
                    
                    
                    switch(final_press)
                    {
                        case "rev":
                            ps.println(info_received);
                            ps.flush();
                            int j = 1;
                            while(j <= total_no_of_chunks)
                            {
                                oka = di.read(b, 0, b.length);
                                fos.write(b,0,b.length);
                                
                                j++;
                            }
                            fos.close();
                            break;
                    }
                  
                    break;
            }
            
            
        } catch (IOException ex) {
            Logger.getLogger(Cclient.class.getName()).log(Level.SEVERE, null, ex);
        }
         
        
        
    }
    
}
