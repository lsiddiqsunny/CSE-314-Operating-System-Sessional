package sample;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OfflineServer {

    static final HashMap<String,String> clientip_info=new HashMap<>();//clientlist and their fixed ip's
    static final Vector<String> online_list=new Vector<>();//online client list
    static final HashMap<String,Socket> client_socket_list=new HashMap<>();//client & their socket

    static final HashMap<String,String> socket_isbusy=new HashMap<>();//socket status

    static final HashMap<String,Vector<byte[]>> file_info=new HashMap<>();//file
    static final Vector<Filedescription> server_fileid=new Vector<>(); //file list


    public static void main(String[] args) {
        try {
            for(int i=1;i<=50;i++)
            {
                clientip_info.put(""+i,"127.0.0.1");
                clientip_info.put(""+i+50, "127.0.0."+i);
            }

            ServerSocket server=new ServerSocket(1025);


            while(true)
            {
                Socket serversocket=server.accept();
                DataInputStream input=new DataInputStream(serversocket.getInputStream());
                DataOutputStream output=new DataOutputStream(serversocket.getOutputStream());

                /*Scanner sc=new Scanner(System.in);
                int a=sc.nextInt();
                output.writeInt(a);
                */
                String id=input.readUTF();
                if(online_list.contains(id)) output.writeInt(0);//already connected
                else if(!serversocket.getLocalAddress().toString().equals("/"+clientip_info.get(id))) {output.writeInt(1);}//wrong ip
                else
                {
                    online_list.add(id);
                    client_socket_list.put(id,serversocket);
                    socket_isbusy.put(id,"n");
                    FileThreading thread=new FileThreading(serversocket,id,input,output);
                    Thread t=new Thread(thread);
                    t.start();
                }


            }
        } catch (IOException ex) {
            Logger.getLogger(OfflineServer.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}


class FileThreading implements Runnable
{
    private String Current_id;
    private Socket socket;
    private DataInputStream input;
    private DataOutputStream output;
    //Filedescription filee;
    boolean isreceiveFile;
    String file;
    public FileThreading(Socket s,String ss,DataInputStream d,DataOutputStream o) {
        Current_id=ss;
        socket=s;
        input=d;
        output=o;
        isreceiveFile=false;
    }

    void set()
    {
        isreceiveFile=true;
    }


    byte addbyte(byte seq1,byte seq2)
    {
        int temp1=seq1,temp2=seq2;
        if(temp1<0)temp1+=256;
        if(temp2<0)temp2+=256;
        return (byte) (((temp1+temp2)&255)+((temp1+temp2)>>8));

    }

    boolean check_sum(byte a[],byte b)
    {
        byte ans=0;
        for(int i=0;i<a.length;i++) ans=addbyte(a[i], ans);
        //System.out.println((byte)(ans+b)+  "  "+b+ "   "+ans);

        if((byte)(ans+b)==-1) return true;
        return false;
    }





    @Override
    public void run() {

        try {


            if(isreceiveFile)
            {
                while(!"n".equals(OfflineServer.socket_isbusy.get(Current_id)));
                OfflineServer.socket_isbusy.put(Current_id, "f");

                output.writeUTF(file);
                return;
            }












            output.writeInt(2);
            Iterator <Filedescription>itr1=OfflineServer.server_fileid.iterator();
            while(itr1.hasNext())
            {
                Filedescription file=itr1.next();
                if(file.receiver==Current_id)
                {
                    OfflineServer.socket_isbusy.put(Current_id, "f");
                    output.writeUTF(file.fileid);

                    while(true)
                    {
                        int a=input.available();
                        if(a==0||"y".equals(OfflineServer.socket_isbusy.get(Current_id)))  {continue;}

                        if(a>0&&"f".equals(OfflineServer.socket_isbusy.get(Current_id)))//file request
                        {
                            String val=input.readUTF();
                            if(val.charAt(0)=='1')
                            {
                                String temp=val.substring(1);

                                Vector<byte[]>vec=OfflineServer.file_info.get(temp);
                                Iterator <byte[]> it=vec.iterator();

                                while(it.hasNext())
                                {
                                    byte[]bytes=it.next();
                                    output.write(bytes);
                                    //System.out.println(bytes.length);

                                    int value=input.readInt();
                                    if(value==1)//logout
                                    {
                                        OfflineServer.online_list.remove(Current_id);
                                        OfflineServer.client_socket_list.remove(Current_id);
                                        socket.close();
                                        return;
                                    }

                                }

                                output.write(new byte[1201]);


                                Iterator<Filedescription> itr=OfflineServer.server_fileid.iterator();
                                while(itr.hasNext())
                                {
                                    Filedescription f=itr.next();
                                    if(f.fileid.equals(temp))
                                    {
                                        file=f; break;
                                    }
                                }

                                int value=input.readInt();
                                if(value==1)
                                {
                                    //output.writeInt(1);
                                    OfflineServer.socket_isbusy.put(Current_id,"n");
                                    OfflineServer.server_fileid.remove(file);
                                    OfflineServer.file_info.remove(file.fileid);
                                    break;
                                }
                                else
                                {
                                    //output.writeInt(0);
                                    output.writeUTF(file.fileid);


                                }


                            }
                            else
                            {
                                OfflineServer.socket_isbusy.put(Current_id,"n");
                                String temp=val.substring(1);
                                OfflineServer.file_info.remove(temp);
                                Iterator<Filedescription> itr=OfflineServer.server_fileid.iterator();
                                while(itr.hasNext())
                                {
                                    Filedescription f=itr.next();
                                    if(f.fileid.equals(temp))
                                    {
                                        OfflineServer.server_fileid.remove(f);break;
                                    }
                                }
                            }

                        }


                    }
                }


            }

            //System.out.println("Here go"+input.available());
            while(true)
            {
                int a=input.available();
                if(a==0||"y".equals(OfflineServer.socket_isbusy.get(Current_id)))  {continue;}

                if(a>0&&"f".equals(OfflineServer.socket_isbusy.get(Current_id)))//file request
                {
                    String val=input.readUTF();
                    if(val.charAt(0)=='1')
                    {
                        String temp=val.substring(1);

                        Vector<byte[]>vec=OfflineServer.file_info.get(temp);
                        Iterator <byte[]> it=vec.iterator();

                        while(it.hasNext())
                        {
                            byte[]bytes=it.next();
                            output.write(bytes);


                            //System.out.println(bytes.length);

                            int value=input.readInt();
                            if(value==1)//logout
                            {
                                OfflineServer.online_list.remove(Current_id);
                                OfflineServer.client_socket_list.remove(Current_id);
                                socket.close();
                                return;
                            }

                        }

                        output.write(new byte[1201]);

                        Filedescription file = new Filedescription();
                        Iterator<Filedescription> itr=OfflineServer.server_fileid.iterator();
                        while(itr.hasNext())
                        {
                            Filedescription f=itr.next();
                            if(f.fileid.equals(temp))
                            {
                                file=f; break;
                            }
                        }

                        int value=input.readInt();
                        if(value==1)
                        {
                            //output.writeInt(1);
                            OfflineServer.socket_isbusy.put(Current_id,"n");
                            OfflineServer.server_fileid.remove(file);
                            OfflineServer.file_info.remove(file.fileid);
                            continue;
                        }
                        else
                        {
                            //output.writeInt(0);
                            output.writeUTF(file.fileid);
                            continue;
                        }


                    }
                    else
                    {
                        OfflineServer.socket_isbusy.put(Current_id,"n");
                        String temp=val.substring(1);
                        OfflineServer.file_info.remove(temp);
                        Iterator<Filedescription> itr=OfflineServer.server_fileid.iterator();
                        while(itr.hasNext())
                        {
                            Filedescription f=itr.next();
                            if(f.fileid==temp)
                            {
                                OfflineServer.server_fileid.remove(f);
                            }
                        }
                        continue;
                    }

                }


                OfflineServer.socket_isbusy.put(Current_id,"y");
                int  val=input.readInt();

                if(val==1)
                {
                    String name=input.readUTF();


                    //System.out.println("okk1");

                    if(!OfflineServer.online_list.contains(name))
                    {
                        output.writeInt(0);
                    }
                    //else if()
                    else
                    {
                        output.writeInt(1);
                        int size =input.readInt();

                        //System.out.println("okk2");

                        if(!check(size)) output.writeInt(0);
                        else
                        {
                            output.writeInt(1);
                            String filename=input.readUTF();
                            //System.out.println("okk3");

                            Filedescription des=new Filedescription();
                            des.fileid=Current_id+" "+size+" "+filename+" "+name+Math.random()%100;
                            des.size=size;
                            des.sender=Current_id;
                            des.receiver=name;
                            int chunk_size=(int) (System.currentTimeMillis()%20)+236;
                            output.writeInt(chunk_size);


                            //System.out.println("okk4");

                            OfflineServer.file_info.put(des.fileid, new Vector<>());

                            int get=0;
                            byte current_seq=0;

                            boolean error_found=false;
                            int seq=0;
                            while(true)
                            {



                                byte[] bytes=new byte[chunk_size+57];
                                while(input.available()<bytes.length);
                                int value=input.read(bytes);




                                byte[]bytee=framing(bytes);


                                if(bytee[0]==1&&bytee[2]==1)//logout
                                {
                                    OfflineServer.file_info.remove(des.fileid);
                                    OfflineServer.server_fileid.remove(des);
                                    OfflineServer.client_socket_list.remove(Current_id);
                                    OfflineServer.online_list.remove(Current_id);

                                    socket.close();

                                    return;
                                }


                                else if(bytee[0]==1&&bytee[2]==2)
                                {
                                    //System.out.println("OOOOOOOO");



                                    System.out.println("Successful");

                                    Socket s=OfflineServer.client_socket_list.get(des.receiver);
                                    if(s!=null)
                                    {
                                        FileThreading file=new FileThreading(s, des.receiver, new DataInputStream(s.getInputStream()),new DataOutputStream(s.getOutputStream()));
                                        file.set();file.file=des.fileid;
                                        Thread t=new Thread(file);
                                        t.start();

                                    }

                                    break;
                                }

                                else if(current_seq!=bytee[1])
                                {
                                    System.out.println("Client No- "+Current_id+"--Sequence "+current_seq+" is expected."+bytee[1]+" is  received.Discarding data");
                                    continue;
                                }




                                bytes=new byte[bytee.length-4];
                                System.arraycopy(bytee, 3, bytes, 0, bytes.length);

                                error_found=check_sum(bytes, bytee[bytee.length-1]);

                                //System.out.println(current_seq+"  "+chunk_size+"  "+bytee[bytee.length-1]);

                                if(!error_found)
                                {


                                    System.out.println("Error Detected!! Client ID- "+Current_id+"  sequence No-"+current_seq+" ..Data Discarding.");

                                    continue;




                                }

                                else
                                {
                                    byte array[]=new byte[chunk_size+4];
                                    array[1]=1;
                                    array[0]=0b01111110;
                                    array[2]=0;
                                    array[3]=current_seq;
                                    array[chunk_size+3]=0b01111110;
                                    System.out.println("Ack. sent "+array[3]);
                                    output.write(array);
                                }

                                current_seq++;
                                current_seq%=40;




                                //output.writeInt(0);
                                OfflineServer.file_info.get(des.fileid).add(bytes);
                                get+=bytes.length;



                            }

                            System.out.println("Done here");

                        }
                    }
                }
                else if(val==3)
                {
                    Iterator<String> temp=OfflineServer.online_list.iterator();
                    int i=0;
                    while(temp.hasNext())
                    {
                        String s=temp.next();

                        if(Current_id.equals(s))continue;
                        output.writeUTF(s);
                        int vall=input.readInt();

                    }
                    output.writeUTF("end");

                }
                else if(val==2)
                {
                    OfflineServer.client_socket_list.remove(Current_id);
                    OfflineServer.online_list.remove(Current_id);

                    socket.close();
                    return;
                }
                OfflineServer.socket_isbusy.put(Current_id, "n");
                //System.out.println("Okkk");
            }



        } catch (IOException ex) {
            Logger.getLogger(FileThreading.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(FileThreading.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    synchronized boolean  check(int a)
    {
        Iterator<Filedescription> it=OfflineServer.server_fileid.iterator();
        int sum=0;
        while(it.hasNext())
        {
            Filedescription temp=it.next();
            Iterator<byte[]>itr=OfflineServer.file_info.get(temp.fileid).iterator();
            while(itr.hasNext())
            {
                int aa=itr.next().length;
                sum=sum+aa;

            }

        }
        if(sum+a>100000000) return false;//max 100mb
        return true;
    }


    byte[] framing(byte[]array)
    {





        int value=array.length;
        byte[]temp=new byte[array.length];
        int i=0,size=0;

        if(array[0]!=0b01111110) {}//wrong

        Vector<Boolean> vector=new Vector<>();



        //while(array[value-1]==0)value--;



        byte test_byte=0;
        for (int j=1;j<value;j++)
        {


            byte check=array[j];

            for(int k=7;k>=0;k--)
            {




                vector.add((check & 1<<k)==0?Boolean.FALSE:Boolean.TRUE);


            }
        }

        while(vector.get(vector.size()-1)==Boolean.FALSE)vector.remove(vector.size()-1);
        for(int m=0;m<7;m++) vector.remove(vector.size()-1);


        //System.out.println(array[2]+"                   00000              "+vector.size());


        String before="",after="";
        for(int m=0;m<vector.size();m++)
        {
            if(m%8==0){
                before+=" ";
                after+=" ";
            }

            if(vector.get(m)==Boolean.TRUE) {i++;before+="1";after+="1";}
            else {
                before+="0";after+="0";
                i=0;
            }



            if(i==5){i=0;
                //System.out.println("okkka");
                before+=vector.get(m+1)==Boolean.TRUE?"1":"0";
                vector.remove(m+1);
            }

        }


        //System.out.println(vector.size()/8.0);
        i=0;

        temp[size]=0;
        for(int j=0;j<vector.size();j++)
        {
            byte add=(byte) (vector.get(j)==Boolean.FALSE?0:1);
            temp[size]=(byte)((temp[size]<<1)|add);
            i++;
            if(i==8){i=0;size++;if(size<value)temp[size]=0;}
        }



        System.out.println("Bit pattern received : "+before+"\nBit de-stuffing!\nBit pattern found--- : "+after);

        array=new byte[size];
        System.arraycopy(temp, 0, array, 0, size);
        return array;

    }


}

class Filedescription
{
    String sender,receiver,fileid,filename;

    int size;
}



class timer01
{
    int value=0;
    TimerTask task = new TimerTask()
    {
        public void run()
        {
            value=1;
        }
    };

    public int getInput(DataInputStream input,int x) throws Exception
    {
        Timer timer = new Timer();
        timer.schedule( task, x*1000 );



        while(value==0&&input.available()==0){}

        timer.cancel();
        return value;
    }


}
