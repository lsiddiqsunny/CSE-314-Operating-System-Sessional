import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.TimeLimiter;
import com.google.common.util.concurrent.UncheckedTimeoutException;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Scanner;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.zip.CRC32;
import java.util.zip.Checksum;


/**
 * Created by samia hossain on 10/1/2017.
 */
public class SendClass extends Thread {

  public   DataInputStream dis;
   public  DataOutputStream dos;
   public  Socket socket;
   public  int receiverStdid;
   public String filepath="";
  public static   Vector v= new Vector(60,5);

   SendClass(DataInputStream dis,DataOutputStream dos,Socket socket,int rcvid,String filepath)
   {
       this.dis=dis;
       this.dos=dos;
       this.socket=socket;
       this.receiverStdid=rcvid;
       this.filepath=filepath;
       System.out.println("okkkk"+socket.isClosed());
   }
    public synchronized void run()
    {
        try {
            File file = new File(filepath);
            dos.writeUTF(file.getName());
            dos.flush();

            System.out.println("give receiver stdid...");
           // Scanner scanner = new Scanner(System.in);
          //  int receiverStdid = scanner.nextInt();
            dos.write(receiverStdid);
            dos.flush();
            send(file,socket);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public static void send(File files, Socket socket) {


        try {
            int d = 0;
            DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

            dos.writeLong(files.length());
            dos.flush();
            //   System.out.println(files.length());
            //write the number of files to the serve//
           // System.out.println(files.length());


            //write file names

            //  dos.writeUTF(files.getName());
          //  dos.flush();


            //buffer for file writing, to declare inside or outside loop?
            int n = 0;
            byte[] buf = new byte[20];
            //outer loop, executes one for each file


            System.out.println(files.getName());

            //create new fileinputstream for each file
            FileInputStream fis = new FileInputStream(files);
            //write file to dos
            //BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            TimeLimiter timeLimiter = new SimpleTimeLimiter();
            Callable<Boolean> callable;
            int x=1;
            int seq=1;
            int chunkcount=1;
            int xx=0;
            byte a[];
            a= new byte[1];
            a[0]=126;
            while ((n = fis.read(buf)) != -1) {
                int c = 0;






                ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
                ByteArrayOutputStream outputStream2 = new ByteArrayOutputStream( );
              //  outputStream.write( a );
                outputStream.write((byte)seq);
                outputStream.write( buf );
              //  outputStream2.write(seq);
              //  outputStream2.write( buf );

                //byte forcs[] = outputStream2.toByteArray( );
               // int cs=(int) calculateChecksum(forcs);
              //  outputStream.write( cs );
              //  outputStream.write( a );
                byte fullchunk[] = outputStream.toByteArray( );

                String bs="";
                int cnt=0;
                for (byte b : fullchunk) {
                    bs+=Integer.toBinaryString(b & 255 | 256).substring(1);
                    if(cnt==n)
                    {
                        break;
                    }
                    cnt++;

                }
                System.out.println(bs);
                int checksum=0;
                for(int i=0;i<bs.length();i++)
                {
                    if(bs.charAt(i)=='1')
                    {
                        checksum++;
                    }
                }
                System.out.println("csss  "+ checksum);
                String checksumstring = String.format("%8s", Integer.toBinaryString(checksum & 0xFF)).replace(' ', '0');
                System.out.println(checksumstring);
                bs=bs+checksumstring;
                for(int i=0;i<bs.length()-5;i++)
                {
                    String temp=bs.substring(i,i+5);
                    if(temp.equals("11111"))
                    {
                        bs=bs.substring(0,i+5)+"0"+bs.substring(i+5,bs.length());
                        i=i+4;
                    }

                }
                System.out.println(bs);

                bs = bs.substring(0, 0) + "01111110" + bs.substring(0, bs.length());
                bs = bs + "01111110" ;
                int l=bs.length()%8;
                if(l!=0)
                {
                    l=8-l;
                    while(l!=0)
                    {
                        bs+='0';
                        l--;
                    }
                }
                System.out.println(bs);
                ArrayList<Integer> arrayList = new ArrayList<>();

                for(String str : bs.split("(?<=\\G.{8})"))
                    arrayList.add(Integer.parseInt(str, 2));

                System.out.println(arrayList);
                byte[] result = new byte[arrayList.size()];
                for(int i = 0; i < arrayList.size(); i++) {
                    result[i] = arrayList.get(i).byteValue();
                }
                System.out.println("size "+arrayList.size());
                String fs="";
                int cnt1=0;
                System.out.println("n "+n);
                for (byte b : result) {
                    fs+=Integer.toBinaryString(b & 255 | 256).substring(1);
                    if(cnt1==arrayList.size())
                    {
                        break;
                    }
                    cnt1++;

                }
                v.add(result);
                System.out.println("final "+fs);
              /* dos.write(arrayList.size());
                dos.flush();

                dos.write(result, 0, arrayList.size());
                dos.flush();*/
                seq++;
                if(seq==254) seq=0;
                chunkcount++;

              /* try {
                    callable = new Callable<Boolean>() {
                        @Override
                        public Boolean call() throws Exception {


                            return dis.readBoolean();

                        }
                    };
                    boolean line = timeLimiter.callWithTimeout(callable, 4, TimeUnit.SECONDS, true);
                    System.out.println("line :"+ line);


                } catch (TimeoutException | UncheckedTimeoutException e) {
                    // timed out
                    System.out.println("time out");
                   // socket.close();
                    break;
                }
             catch (Exception e) {

                }*/


            }
            int pac=0;
            chunkcount--;
            int sent=0;
            int ack=8;
            int iserrorocurred=0;
            System.out.println("chunk : "+ chunkcount);
            System.out.println("size : "+ v.size());
            while(pac<chunkcount)
            {
                System.out.println(pac+" theke suru hocche");
                for(int i=0;i<8;i++)
                {

                    if((pac)==chunkcount) break;
                    System.out.println("packet "+pac);
                    //byte[] result=new byte[];
                   byte[] result=(byte[]) v.get(pac);
                    dos.write(result.length);
                    dos.flush();

                    dos.write(result, 0, result.length);
                    dos.flush();





                    pac++;
                    sent++;
                   // System.out.println("ackkkk : "+ac);

                }
                try {
                    socket.setSoTimeout(5000);
                    for (int i = 0; i < 8; i++) {
                        int ac = dis.read();
                        ack--;
                        System.out.println(ac + "  ac asche");
                   /* if (ac == 255 && iserrorocurred == 0) {
                        System.out.println("nak asche");

                        iserrorocurred = 1;
                    }*/
                    }

                }
                catch (SocketTimeoutException e)
                {
                    pac = pac - ack;
                    ack = 8;
                }

            }
            System.out.println("file sent successfully");
            //dos.close();


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
