
package sample;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public class FileTransmission {
    static String current_id;
    Socket current_socket;
    static DataInputStream inputstream;//datastream for current client
    static DataOutputStream outputstream;//datastream
    static boolean socket_isbusy;//either it busy or not
    static  File currentfile;

    static boolean logoutbutton;






    void start(String s)
    {



        try {
            current_id=s;
            logoutbutton=false;
            currentfile=null;
            socket_isbusy=false;
            current_socket=new Socket("localhost", 1025);
            inputstream=new DataInputStream(current_socket.getInputStream());
            outputstream=new DataOutputStream(current_socket.getOutputStream());
            outputstream.writeUTF(current_id);
            int val=inputstream.readInt();
            Stage pri=new Stage();

            if(val==0)
            {
                Label label=new Label("You are already connected");
                Scene scn=new Scene(label);
                pri.setScene(scn);
                pri.show();
                new login(new Stage());
            }
            else if(val==1)
            {

                Label label=new Label("Wrong IP");
                Scene scn=new Scene(label);
                pri.setScene(scn);pri.show();
                new login(new Stage());
            }
            else
            {

                Label label=new Label("You are connected to server.!!");
                Scene scn=new Scene(label);
                pri.setScene(scn);pri.show();
                gui();
                //FileTransmission();



            }


        } catch (IOException ex) {
            //Logger.getLogger(FileTransmission.class.getName()).log(Level.SEVERE, null, ex);
        }


    }

    void gui() throws IOException
    {

        Button online_client=new Button("Online Clients");
        online_client.setMaxSize(200, 40);
        online_client.setLayoutX(300);
        online_client.setLayoutY(20);

        final TextField receiver_id=new TextField();
        receiver_id.setMaxSize(250, 80);
        receiver_id.setLayoutX(50);
        receiver_id.setLayoutY(60);


        final Button fileselect=new Button("....");
        fileselect.setMaxSize(150, 40);
        fileselect.setLayoutX(50);
        fileselect.setLayoutY(120);

        Button send=new Button("Send");
        //send.setMaxSize(150, 40);
        send.setLayoutX(50);
        send.setLayoutY(170);

        Button disconnect=new Button("Disconnect");
        disconnect.setMaxSize(150, 40);
        disconnect.setLayoutX(340);
        disconnect.setLayoutY(220);


        RadioButton error=new RadioButton("Error");
        RadioButton frame=new RadioButton("Frame-lost");

        error.setMaxSize(100,30);
        frame.setMaxSize(100,30);
        error.setLayoutX(320);
        error.setLayoutY(180);
        frame.setLayoutX(320);
        frame.setLayoutY(140);


        Pane pane=new Pane();
        //pane.getChildren().add(0, online_client);
        pane.getChildren().add(0, receiver_id);
        pane.getChildren().add(1, fileselect);
        pane.getChildren().add(2, send);
        pane.getChildren().add(3, disconnect);
        pane.getChildren().add(4,online_client);
        pane.getChildren().add(5,error);
        pane.getChildren().add(6,frame);


        Stage stage=new Stage();
        Scene scn=new Scene(pane,500,300);
        stage.setScene(scn);
        stage.show();

        receiver_id.setOnKeyReleased(new EventHandler<KeyEvent>() {//this event handles id type.. Id must be a number

            @Override
            public void handle(KeyEvent t) {
                if(!receiver_id.getText().matches("[0-9]*"))
                {
                    receiver_id.setText(receiver_id.getText().replaceAll("[^\\d]", ""));
                    receiver_id.selectPositionCaret(receiver_id.getLength());receiver_id.deselect();
                }
            }
        });


        online_client.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                while(FileTransmission.socket_isbusy){}
                FileTransmission.socket_isbusy=true;
                try {
                    FileTransmission.outputstream.writeInt(3);
                    Queue<String> queue=new LinkedList<>();

                    while(true)
                    {
                        String s=FileTransmission.inputstream.readUTF();
                        if("end".equals(s)) break;
                        queue.add(s);
                        FileTransmission.outputstream.writeInt(0);

                    }
                    FileTransmission.socket_isbusy=false;
                    System.out.println("Online Client List----");
                    while(!queue.isEmpty())
                    {
                        System.out.println(queue.poll());
                    }

                } catch (IOException ex) {
                    Logger.getLogger(FileTransmission.class.getName()).log(Level.SEVERE, null, ex);
                }


            }
        });


        fileselect.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Open Resource File");
                currentfile=fileChooser.showOpenDialog(stage);
                if(currentfile!=null)fileselect.setText(currentfile.getName());
            }
        });


        send.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                if(receiver_id.getText().isEmpty()||currentfile==null) return;
                while(socket_isbusy){}
                socket_isbusy=true;

                //System.out.println(error.isSelected());

                client_threading clt=new client_threading(receiver_id.getText(),currentfile,error.isSelected(),frame.isSelected());

                Thread thread=new Thread(clt);
                thread.start();

            }
        });

        receivefile r=new receivefile();
        Thread t=new Thread(r);
        t.start();
        disconnect.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {

                try {
                    logoutbutton=true;
                    if(socket_isbusy) ;

                    else {outputstream.writeInt(2);FileTransmission.logoutbutton=false;}



                    Stage pri=new Stage();
                    Label label=new Label("You are disconnected to server.!!");
                    Scene scn=new Scene(label);
                    pri.setScene(scn);

                    new login(stage);

                } catch (IOException ex) {
                    Logger.getLogger(FileTransmission.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });




    }
}


class client_threading implements Runnable
{
    String receiver;
    File file;
    boolean error_ocr,frame_lost;
    public client_threading(String s,File f,boolean e,boolean ff) {
        receiver=s;
        file=f;
        error_ocr=e;
        frame_lost=ff;
    }


    @Override
    public void run() {
        try {




            FileTransmission.outputstream.writeInt(1);
            FileTransmission.outputstream.writeUTF(receiver);



            int val=FileTransmission.inputstream.readInt();
            if(val==0)
            {
                System.out.println("Sorry Receiver is not present !!");
                FileTransmission.socket_isbusy=false;
                return;
            }


            //System.out.println("Okk2");


            FileTransmission.outputstream.writeInt((int) file.length());

            val=FileTransmission.inputstream.readInt();

            //System.out.println("Okk3");

            if(val==0)
            {
                System.out.println("Sorry File is too large !!");
                FileTransmission.socket_isbusy=false;
                return;
            }
            FileTransmission.outputstream.writeUTF(file.getName());

            //System.out.println("Okk4");





            //Some code need to be implemented here if file > 64 kb is allowed 



            int chunk=FileTransmission.inputstream.readInt();




            byte [] bytearray=new byte[chunk];



            FileInputStream fis = new FileInputStream(file);
            int i=0;



            int seq_no=0;


            int count_err=0;


            byte [][] save_byte=new byte[40][chunk+57];

            while(true)
            {

                if(file.length()-i<chunk)bytearray=new byte[(int)file.length()-i];//if remaining filesize <chunk size
                fis.read(bytearray,0,  bytearray.length);
                i+=chunk;


                if(FileTransmission.logoutbutton)
                {
                    //FileTransmission.socket_isbusy=false;
                    FileTransmission.logoutbutton=false;

                    byte temp[]=new byte[chunk+57];
                    temp[0]=0b01111110;
                    temp[1]=1;
                    temp[3]=1;
                    temp[chunk+56]=0b01111110;

                    FileTransmission.outputstream.write(temp);

                    //FileTransmission.socket_isbusy=false;
                    return;
                }






                save_byte[seq_no]=framing(bytearray,(byte)seq_no,(byte)0,chunk);

//                System.arraycopy(array,0,0,array.length);





                if(frame_lost&&seq_no+2>save_byte[seq_no][bytearray.length-2])
                {

                    System.out.println("Frame Lost- sequence No-- "+seq_no);
                }
                else
                {
                    if(error_ocr)
                    {
                        byte new_byte[]=new byte[chunk+57];
                        System.arraycopy(save_byte[seq_no],0,new_byte,0,new_byte.length);
                        new_byte[bytearray.length%20+2]= (byte) (new_byte[bytearray.length%20+2]|5);
                        if(new_byte[bytearray.length%20+2]!=save_byte[seq_no][bytearray.length%20+2])
                        {
                            System.out.println("Error at sequence No "+seq_no);
                            count_err++;
                            if(count_err==10)error_ocr=false;
                        }

                        FileTransmission.outputstream.write(new_byte);
                    }
                    else
                        FileTransmission.outputstream.write(save_byte[seq_no]);///---

                }

                seq_no++;
                //x+=byte_temp.length;




                while(seq_no==40||i>=file.length())
                {


                    int value=new timer().getInput(500);

                    byte num=-1;
                    boolean ack_lost=false;
                    if(value==1)
                    {
                        //num=FileTransmission.inputstream.readByte();
                        System.out.println("Ack. not found.");
                        ack_lost=true;
                    }
                    //System.out.println(num+"    " + seq_no);

                    while(!ack_lost&&new timer().getInput(200)!=1)
                    {
                        //System.out.println(FileTransmission.inputstream.available());
                        while(FileTransmission.inputstream.available()<chunk+4);
                        byte[]arr=new byte[chunk+4];

                        FileTransmission.inputstream.read(arr);
                        if(ack_lost)continue;
                        if(arr[3]==num+1)
                        {
                            num=arr[3];
                            System.out.println("Ack. found "+num);
                        }
                        else
                        {
                            System.out.println("Ack. found..Wrong ack no. or ack. lost "+num);
                            ack_lost=true;
                        }




                    }

                    num++;
                    if(num<seq_no)
                    {
                        FileTransmission.outputstream.flush();
                        for(int j=num;j<seq_no;j++)
                        {
                            System.out.println("Re-sending data sequence No-"+j);
                            FileTransmission.outputstream.write(save_byte[j]);
                        }
                    }
                    else break;

                }


                seq_no%=40;



                //System.out.println(i+" "+file.length());
                if(i>=file.length())
                {
                    save_byte[0][1]=1;
                    save_byte[0][3]=2;
                    FileTransmission.outputstream.write(save_byte[0]);
                    break;


                }


            }

            //System.out.println(x);




            System.out.println("Successful!!");
            FileTransmission.socket_isbusy=false;
            return;

        } catch (IOException ex) {
            Logger.getLogger(client_threading.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(client_threading.class.getName()).log(Level.SEVERE, null, ex);
        }

    }




    byte addbyte(byte seq1,byte seq2)
    {
        int temp1=seq1,temp2=seq2;
        if(temp1<0)temp1+=256;
        if(temp2<0)temp2+=256;
        return (byte) (((temp1+temp2)&255)+((temp1+temp2)>>8));

    }

    byte check_sum(byte arr[])
    {
        byte ans=0;
        for(int i=0;i<arr.length;i++)
        {
            ans=addbyte(ans, arr[i]);
        }
        //System.out.println((byte)(ans^0b11111111));
        return (byte) (ans^0b11111111);
    }




    byte[] framing(byte[]array1,byte seq_no,byte type,int chunk)
    {

        byte checksum=check_sum(array1);

        //    System.out.println(seq_no+"   "+checksum );

        byte array[]=new byte[array1.length+4];
        array[0]=type;
        array[1]=seq_no;
        array[2]=0;//ack
        array[array.length-1]=checksum;
        System.arraycopy(array1, 0, array, 3, array1.length);

        byte[]temp=new byte[chunk+57];//2 byte for flag


        int i=0,size=0;

        //System.out.println(array.length);
        Vector<Boolean> vector=new Vector<>();

        String before="",after="";


        //System.out.println(array[1]+array[2]);
        for (int j=0;j<array.length;j++)
        {

            byte check=array[j];

            for(int k=7;k>=0;k--)
            {

                if((check & 1<<k)!=0) i++;
                else i=0;
                if(i==5){
                    i=0;vector.add(Boolean.TRUE);vector.add(Boolean.FALSE);before+="1";after+="10";}
                else {vector.add((check & 1<<k)==0?Boolean.FALSE:Boolean.TRUE);String a=vector.get(vector.size()-1)==Boolean.FALSE?"0":"1";before+=a;after+=a;}
            }
            before+=" ";
            after+=" ";

        }




        //System.out.println(seq_no+"   "+vector.size());



        vector.add(Boolean.FALSE);
        for(int j=0;j<6;j++)vector.add(Boolean.TRUE);

        while(vector.size()%8!=0)vector.add(Boolean.FALSE);


        //System.out.println(vector.size());

        i=0;
        temp[size]=0b01111110;
        size++;
        temp[size]=0;
        for(int j=0;j<vector.size();j++)
        {
            byte add=(byte) (vector.get(j)==Boolean.FALSE?0:1);

            temp[size]=(byte)((temp[size]<<1)|add);
            i++;

            if(i==8){i=0;size++;if(size<array.length)temp[size]=0;}
        }




        //for(int k=size;k<temp.length;k++)temp[k]=0;

        array=new byte[size];
        System.arraycopy(temp,0,array,0,array.length);

        //String before= String.valueOf(array1),after= String.valueOf(array);

        //System.out.println("Byte array before stuffing "+array1+"  After stuffing "+array);

        System.out.println("Bit pattern before stuffing "+before+"\nBit pattern  After stuffing "+after);
        return temp;

    }

}


class receivefile implements Runnable
{
    File file;
    String filename,sender;
    int size;
    @Override
    public void run() {
        while(true)
        {
            try {

                if(FileTransmission.logoutbutton)return;
                if(new timer().get()==1) continue;




                if(FileTransmission.logoutbutton)return;
                FileTransmission.socket_isbusy=true;
                String s=FileTransmission.inputstream.readUTF();


                String []f=s.split(" ");
                sender=f[0];
                System.out.println(f[0]);
                size=Integer.parseInt(f[1]);
                filename=f[2];

                System.out.println(sender+" sent you a file.");
                System.out.println("to accept it press 'Y' or any 'A-Z' key  to cancel it");
                Scanner sc=new Scanner(System.in);
                String str=sc.nextLine();
                if(str.charAt(0)=='y'||str.charAt(0)=='Y')
                {
                    FileTransmission.outputstream.writeUTF("1"+s);

                    byte [] bytefile=new byte[size];


                    int offset=0;

                    ;
                    while(true)
                    {
                        byte []bytes=new byte[1201];
                        int get=FileTransmission.inputstream.read(bytes);
                        //System.out.println(get+" "+size+" "+offset);
                        if(get==1201)
                        {
                            //System.out.println("Yeeeessss");
                            break;
                        }
                        else
                        {
                            System.out.println(get+" "+size+" "+offset);
                            System.arraycopy(bytes, 0, bytefile, offset,get);
                            offset+=get;

                            if(FileTransmission.logoutbutton)
                            {
                                FileTransmission.outputstream.writeInt(1);
                                return;
                            }
                            else
                            {
                                FileTransmission.outputstream.writeInt(0);
                            }
                        }
                    }
                    //System.out.println("Yeeeessss111");



                    if(offset==size)
                    {
                        FileTransmission.outputstream.writeInt(1);
                        FileTransmission.socket_isbusy=false;

                        File file = new File(filename);
                        if(file.exists()&& !file.isDirectory())
                        {
                            filename=(System.nanoTime()%1000)+filename;
                        }

                        FileOutputStream fos = new FileOutputStream(filename);
                        fos.write(bytefile);
                        fos.close();
                        System.out.println("Successful");


                    }
                    else
                    {
                        FileTransmission.outputstream.writeInt(0);
                        FileTransmission.socket_isbusy=false;
                        System.out.println("Sorry error occurred!");
                    }


                }
                else
                {
                    FileTransmission.outputstream.writeUTF("0"+s);
                    FileTransmission.socket_isbusy=false;
                    //System.out.println("Yeeeesssss");
                }



            } catch (IOException ex) {
                Logger.getLogger(receivefile.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        //while() 

    }




    byte addbyte(byte seq1,byte seq2)
    {
        int temp1=seq1,temp2=seq2;
        if(temp1<0)temp1+=256;
        if(temp2<0)temp2+=256;
        return (byte) (((temp1+temp2)&255)+((temp1+temp2)>>8));

    }

    byte checksum(byte temp)
    {
        return (byte) (255^temp);
    }



}


class timer
{
    int value=0;
    TimerTask task = new TimerTask()
    {
        public void run()
        {
            value=1;
        }
    };

    public int getInput(int t) throws Exception
    {
        Timer timer = new Timer();
        timer.schedule( task, 30*t );



        while(value==0&&FileTransmission.inputstream.available()==0){}

        timer.cancel();
        return value;
    }

    public int get() throws IOException
    {
        Timer timer = new Timer();
        timer.schedule( task, 1 );

        while((FileTransmission.inputstream.available()==0||FileTransmission.socket_isbusy)&&value==0);
        timer.cancel();
        return value;
    }



}