
import javafx.util.Pair;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Vector;

public class server
{
    static long totalBufferSize=1000000;
    static long currentBufferSize=0;
    static int fid=0;
  static   Vector fileinfo= new Vector(60,5);

    public static void main(String argv[]) throws Exception
    {
        int workerThreadCount = 0;
        int id = 1;
        Vector v= new Vector(60,5);
        Myfile f=new Myfile();
        String receiverip="";
        Socket receiverSocket=null;
        ServerSocket welcomeSocket = new ServerSocket(6732);
        while(true)
        {
            Socket connectionSocket = welcomeSocket.accept();
            DataInputStream dis=new DataInputStream(connectionSocket.getInputStream());
            DataOutputStream dos =new DataOutputStream(connectionSocket.getOutputStream());
           int stdid= dis.read();
           // dis.close();
            System.out.println("std id: "+stdid);
int k=1;
            for(int j=0;j<v.size();j++)
            {
                Pair<Integer, Socket> pair= (Pair<Integer, Socket>) v.get(j);
                if(pair.getKey()==stdid || pair.getValue()==connectionSocket)
                {
                    System.out.println("multiple pc login prohibited....");

                    k=0;
                }
            }
            if(k==0)
            {
             dos.write(k);


                break;
            }
            dos.write(k);
            dos.flush();

            v.add(new Pair<Integer,Socket>(stdid,connectionSocket));




            int confirm=1;
               /* BufferedReader inFromServer = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                clientSentence = inFromServer.readLine();
                capitalizedSentence = clientSentence.toUpperCase();
                outToServer.writeBytes(capitalizedSentence + '\n');*/

            String filename= dis.readUTF();
            int receiverStdid= dis.read();


            for(int j=0;j<v.size();j++)
            {
                Pair<Integer, Socket> pair= (Pair<Integer,Socket>) v.get(j);
                if(pair.getKey()==stdid )
                {
                    receiverSocket=pair.getValue();
               //  receiverip=pair.getValue().toString();
                    break;
                }
            }

            long size=  dis.readLong();
            System.out.println("size "+size);
          /*  if((currentBufferSize+size)>totalBufferSize)
            {
                System.out.println("server overloaded.....");
              //  confirm=0;
                f= new Myfile(fid++,filename,size,receiverSocket);
                fileinfo.add(f);
            }
            else
            {*/
                long chunkCount= (totalBufferSize-currentBufferSize)/100+1;
                System.out.println("cc :"+chunkCount);

                f= new Myfile(fid++,filename,size,receiverSocket);
                fileinfo.add(f);
           // }// BufferedOutputStream bos = new BufferedOutputStream(fos);
            //  InputStream is = connectionSocket.getInputStream();
            // DataInputStream wrapper= new DataInputStream(is);
            //long filesize= wrapper.readLong();
            //No of bytes read in one read() call
            //01111110000000010011000100110000011111010011111100000000



            WorkerThread wt = new WorkerThread(connectionSocket,id,filename,size,f);
            Thread t = new Thread(wt);
            t.start();
            workerThreadCount++;
            System.out.println("Client [" + id + "] is now connected. No. of worker threads = " + workerThreadCount);

        }

    }
}
class WorkerThread implements Runnable
{
    private Socket connectionSocket;
    private int id;
    private  String filename;
    private  Myfile f;

private long size;

    public WorkerThread(Socket ConnectionSocket, int id,String s,long size,Myfile f)
    {
        this.connectionSocket=ConnectionSocket;
        this.id=0;
        this.size=size;
        this.filename=s;
        this.f=f;
    }
    public synchronized void  run()
    {
        String clientSentence;
        String capitalizedSentence;

        int n=0;

            try
            {
                FileOutputStream fos = new FileOutputStream("C:\\Users\\samia hossain\\desktop\\filefolder\\"+filename);




                DataOutputStream dos = new DataOutputStream(connectionSocket.getOutputStream());
                DataInputStream dis= new DataInputStream(new BufferedInputStream(connectionSocket.getInputStream()));
                int check=0;
                int expectedseq=1;
                while ( size > 0 ) {
                    check++;
                    //dos.writeBoolean(false);
                  //  bytesRead = is.read(contents);
                    int length=dis.read();
                    System.out.println("len "+length);
                    byte[] buf = new byte[length];
                //   n = dis.read(buf, 0, (int)Math.min(length, size));
                    n = dis.read(buf, 0, length);
                    if(n==-1) break;
                    String fs="";
                    int cnt=1;
                  //  System.out.println("n "+n);
                    for (byte b : buf) {
                        //System.out.println(cnt);
                        fs+=Integer.toBinaryString(b & 255 | 256).substring(1);
                        if(cnt==n)
                        {
                            break;
                        }
                        cnt++;

                    }
                //    System.out.println(fs);
                    ArrayList<Integer> arrayList2 = new ArrayList<>();

                    for(String str : fs.split("(?<=\\G.{8})"))
                        arrayList2.add(Integer.parseInt(str, 2));

                 //   System.out.println(arrayList2);
                    //01111110 00000001 00110001 00110000 01111101 00111111 000 00 000 0000 0000
                   String temp=fs.substring(0,8);
                    //System.out.println("temp :"+ temp);
                    if(temp.equals("01111110"))
                    {
                        // System.out.println("asche");
                        fs=fs.substring(0+8,fs.length());
                    }
                   int  l=fs.length();
                   // System.out.println("vul " +fs);
                    while(true)
                    {
                        String temp1= fs.substring(l-8,l);
                        if(temp1.equals("01111110")) {
                            fs=fs.substring(0,l-8);
                            break;
                        }
                        l--;
                    }

//destuff
                    for(int i=0;i<fs.length()-5;i++)
                    {
                        String t=fs.substring(i,i+5);
                        if(t.equals("11111"))
                        {
                            fs=fs.substring(0,i+5)+fs.substring(i+6,fs.length());
                            i+=4;
                        }

                    }
                    int seq=Integer.parseInt(fs.substring(0,8),2);
                //    System.out.println(fs.substring(fs.length()-8,fs.length()));
                    String checksumsender=fs.substring(fs.length()-8,fs.length());
                   // System.out.println("integer "+Integer.parseInt(checksumsender,2));
                   // System.out.println("check "+ check);


                   fs= fs.substring(0,fs.length()-8);
                   // System.out.println("cs badd "+ fs);
                    int checksum=0;
                    for(int i=0;i<fs.length();i++)
                    {
                        if(fs.charAt(i)=='1')
                        {
                            checksum++;
                        }
                    }
                  //  System.out.println("cssender "+checksumsender);

                    if(checksum!=(Integer.parseInt(checksumsender,2))) continue;

                   // System.out.println("csss "+checksum);
                    System.out.println("seq "+ seq);
                    System.out.println("expectedseq "+ expectedseq);
                    if(expectedseq==254) expectedseq=0;
                    if(check==5) continue;
                    if(seq!=expectedseq || checksum!=Integer.parseInt(checksumsender,2)){
                        //fos.write(result, 0, arrayList.size());
                        //System.out.println("nak pataise");
                        //dos.write(-1);
                        continue;
                    }

                    //seq number baad disi
                    fs=fs.substring(8,fs.length());

                   // System.out.println(fs);
                    ArrayList<Integer> arrayList = new ArrayList<>();

                    for(String str : fs.split("(?<=\\G.{8})"))
                        arrayList.add(Integer.parseInt(str, 2));

                    //System.out.println(arrayList);
          byte[] result = new byte[arrayList.size()];
        for(int i = 0; i < arrayList.size(); i++) {
            result[i] = arrayList.get(i).byteValue();
        }
                    expectedseq++;
                    size-=arrayList.size();
                    fos.write(result, 0, arrayList.size());
                    dos.write(seq);

                    System.out.print("receiving file ... " + id + " complete!\n");

                 /*   if(id==4){
                       // Thread.sleep(5000);
                    }*/

                    System.out.println("seq "+ seq);

                    id++;


                    // dos.flush();
                    //dos.flush();

                       /*  if(connectionSocket.isClosed())
                         {
                             System.out.println("saaaaa");
                             break;
                         }*/


                }

                  fos.close();

                //connectionSocket.close();

                System.out.println("Myfile saved successfully!");
            //  Myfile file = new Myfile("C:\\Users\\samia hossain\\desktop\\idm.txt");


               // System.out.println(f.reveiverip);
               // connectionSocket.getInetAddress();
            /*    for(int j=0;j<v.size();j++)
                {
                    Pair<Integer, Socket> pair= (Pair<Integer, Socket>) v.get(j);
                    if( pair.getValue()==connectionSocket)
                    {
                        System.out.println("multiple pc login prohibited....");


                    }
                }*/
             /*   dos.writeUTF("do you want to receive the file");
              //  dos.wait();
                Socket socket=f.reveiversocket;
                String fname=f.filename;

                dos.flush();
                System.out.println("fname: "+fname);
                int n1=0;
                byte[] buf1 = new byte[1000];
                File file= new File("C:\\Users\\samia hossain\\desktop\\filefolder\\"+fname);
                FileInputStream fis = new FileInputStream(file);
                while ((n1 = fis.read(buf1)) != -1) {
                    int c = 0;
                    dos.write(buf1, 0, n1);
                    dos.flush();
                   // System.out.print("Sending file ... " + (current * 100) / fileLength + "% complete!");
                }
                fis.close();
                Files.deleteIfExists(Paths.get("C:\\Users\\samia hossain\\desktop\\filefolder\\"+fname));


                //Read Myfile Contents into contents array







                //Myfile transfer done. Close the socket connection!
                socket.close();
                //  welcomeSocket.close();
                System.out.println("Myfile sent succesfully!");*/

            }
            catch(Exception e)
            {
e.printStackTrace();
            }

    }
    }