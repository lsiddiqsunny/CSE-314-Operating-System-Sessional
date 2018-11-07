/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dllclient;

/**
 *
 * @author Sadman Yasar Ridit
 */
import java.net.*;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public class DLLclient {

    /**
     * @param args the command line arguments
     */
    final static int ServerPort=1234;
    public static void main(String[] args) throws UnknownHostException, IOException {
        // TODO code application logic here
        Scanner scn = new Scanner(System.in);
         
        // getting localhost ip
        InetAddress ip = InetAddress.getByName("localhost");
         
        // establish the connection
        Socket s = new Socket(ip, ServerPort);
         
        // obtaining input and out streams
        DataInputStream dis = new DataInputStream(s.getInputStream());
        DataOutputStream dos = new DataOutputStream(s.getOutputStream());
        System.out.println("Enter ID:");
        dos.writeUTF(scn.nextLine());         //login
        System.out.println(dis.readUTF());   //login notification
        
        Thread sendFile = new Thread(new Runnable() 
        {
            public String itos(int a){
                String res=new String();
                StringBuilder buf1=new StringBuilder();
                StringBuilder buf2=new StringBuilder();
                int l=1,p=1;
                while(l<9){
                    int x=a & p;
                    if(x==0)
                    {
                        buf1.append("0");
                    }
                    else{
                        buf1.append("1");
                    }
                    p=p<<1;
                    l++;
                }
                String binary=buf1.reverse().toString();
                int length=binary.length();
              
                if(length<8){
                    while(8-length>0){
                        buf2.append("0");
                        length++;
                    }
                }
                res=buf2.toString()+binary;
                return res;
            }
            
            public int doChecksum(String str)
            {
                int result=0;
                for(int j=0;j<str.length();j++)
                {
                    if(str.charAt(j)=='1')
                    {
                        result++;
                    }
                }
                return result;
            }
            
            public String makeFrame(byte[] b,int chunkno, int arraysize)
            {
                String frame="";
                Scanner in=new Scanner(System.in);
                int i,a[]=new int[arraysize];
                for(i=0;i<arraysize;i++)
                {
                    a[i]=(int)b[i];
                    frame+=itos(a[i]);
                    System.out.println(frame);
                }
                System.out.println(frame);
                String newframe="";
                newframe=frame;
                boolean abc=true;
                do{
                    System.out.println("Do you want to change any bit? Y/N");
                    String command=in.nextLine();
                    if(command.equals("Y")){
                        System.out.println("Enter following 'bit:index' format.");
                        String change=in.nextLine();
                        StringTokenizer st=new StringTokenizer(change,":");
                        String bit=st.nextToken();
                        char x;
                        if(bit.equals("0")){x='0';}
                        else{x='1';}
                        String index=st.nextToken();
                        char c[]=new char[newframe.length()];
                        c=newframe.toCharArray();
                        c[Integer.parseInt(index)]=x;
                        newframe=c.toString();
                        System.out.println(newframe);
                    }
                    else{
                        abc=false;
                    }
                }while(abc);
                int checksum=doChecksum(frame);
                System.out.println(checksum);
                newframe=itos(chunkno)+newframe+itos(checksum);
                System.out.println(newframe);
                return newframe;
            }
            public byte[] bitStuffing(String frame)
            {
                String stuffed=frame.replaceAll("11111", "111110");
                System.out.println(stuffed);
                String finalframed="01111110"+stuffed+"01111110";
                System.out.println(finalframed);
                int bytearraysize,flag;
                int rem=finalframed.length()%8;
                if(rem==0){
                    bytearraysize=finalframed.length()/8;
                    flag=0;
                }
                else{
                    bytearraysize=(finalframed.length()/8)+1;
                    flag=1;
                }
                
                byte[] result=new byte[bytearraysize];
                int p=0,q=8,r=0;
                for(r=0;r<bytearraysize;r++)
                {
                    if(flag==0){
                    String bts=finalframed.substring(p, q);
                    if(!bts.equals("")){
                    result[r]=(byte)Integer.parseInt(bts, 2);
                    //System.out.println(result[r]);
                    p=q;
                    q=q+8;
                    }
                    }
                    else{
                        if(r<bytearraysize-1){
                            String bts=finalframed.substring(p, q);
                            if(!bts.equals("")){
                                result[r]=(byte)Integer.parseInt(bts, 2);
                                //System.out.println(result[r]);
                                p=q;
                                q=q+8;
                            }
                        }
                        else{
                            q=finalframed.length();
                            String bts=finalframed.substring(p, q);
                            //System.out.println(bts);
                            int len=bts.length();
                            while(len<8)
                            {
                                bts+="0";
                                len++;
                            }
                            //System.out.println(bts);
                            result[r]=(byte)Integer.parseInt(bts, 2);
                        }
                    }
                }
                /*if((q-finalframed.length())>0)
                {
                    q=finalframed.length();
                    String bts=finalframed.substring(p, q);
                    int len=bts.length();
                    
                    
                    while(len<8)
                    {
                        bts+="0";
                        len++;
                    }
                    
                    result[r]=(byte)Integer.parseInt(bts, 2);
                    System.out.println(result[r]);
                }*/
                
                return result;
            }
            
            public void waitNsend(byte[] b,OutputStream out) throws IOException
            {
                String feedback=dis.readUTF();
                if (feedback.equals("Okay")){
                    return;
                }
                else{
                    out.write(b);
                    waitNsend(b,out);
                }
            }
             @Override
             public synchronized void run()
             {
                 while(true){
                    try {
                        dos.flush();
                        System.out.println("Enter operation#recepient:\n");
                        dos.writeUTF(scn.nextLine());
                        if(dis.readUTF().equals("Offline")){
                            continue;
                        }
                        else{
                            System.out.println("Enter Filepath:");
                            String path = scn.nextLine();
                            File files = new File(path);
                            dos.flush();
                            int flen=(int)files.length();
                            String sflen=Integer.toString(flen);
                            dos.writeUTF(sflen);            //send filesize
                            System.out.println(sflen);
                            StringTokenizer st=new StringTokenizer(files.getPath(),".");
                            String wext=st.nextToken();
                            String ext=st.nextToken();
                            dos.flush();
                            dos.writeUTF(ext);                //send extension
                            FileInputStream fin = new FileInputStream(files);
                            BufferedInputStream bin = new BufferedInputStream(fin);
                            OutputStream output = s.getOutputStream();
                            output.flush();
                            
                            int csize=0,length,cumulativesize=0,cno=1;
                            length=flen;
                            while(cumulativesize<length){
                                byte[] carrier;
                                if(length-cumulativesize>=csize){
                                    csize=512;
                                    cumulativesize+=csize;
                                }else{
                                    csize=length-cumulativesize;
                                    cumulativesize+=csize;
                                }
                                carrier= new byte[csize];
                                //output.flush();
                                int a=bin.read(carrier, 0, csize);
                                System.out.println(carrier);
                                String finalframe=makeFrame(carrier,cno,a);
                                byte[] tobesent=bitStuffing(finalframe);
                                //dos.writeUTF(Integer.toString(tobesent.length));
                                //dos.wait(100);
                                //output.flush();
                                System.out.println(tobesent);
                                output.write(tobesent);
                                waitNsend(tobesent,output);
                                System.out.println("Chunk no. "+cno+" sent to server.\n");
                                cno++;
                                
                            }
                        } 
                    } catch (IOException ex) {
                         Logger.getLogger(DLLclient.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
             }
            
             
        });
        sendFile.start();
    }
    
}
