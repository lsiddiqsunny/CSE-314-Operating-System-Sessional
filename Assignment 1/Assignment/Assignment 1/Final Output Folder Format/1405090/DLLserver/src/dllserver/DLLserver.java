/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dllserver;

/**
 *
 * @author Sadman Yasar Ridit
 */
import java.util.*;
import java.net.*;
import java.io.*;

public class DLLserver {

    /**
     * @param args the command line arguments
     */
    // Vector to store active clients
    static Vector<ClientHandler> online = new Vector<>();
    static Vector<Byte> chunks=new Vector<>(); 
    // counter for clients
    static int clientno = 1;
    
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        ServerSocket ss = new ServerSocket(1234);
         
        Socket s;
        
        while(true)
        {
            System.out.println("Waiting for connection...");
            // Accept the incoming request
            s = ss.accept();
 
            System.out.println("New client request received : " + s);
             
            // obtain input and output streams
            DataInputStream dis = new DataInputStream(s.getInputStream());
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());
            String clientname=dis.readUTF();
            boolean offline=true;
            for(int k=0;k<online.size();k++){
                if(online.get(k).getName()==clientname){
                    System.out.println("Already Logged In");
                    dos.writeUTF("Already Logged In");
                    offline=false;
                    break;
                }

            }
            
            if(offline==true){
            System.out.println("Creating a new handler for this client...");
 
            // Create a new handler object for handling this request.
            ClientHandler mtch = new ClientHandler(s,clientname, dis, dos,clientno);
            
 
            // Create a new Thread with this object.
            Thread t = new Thread(mtch);
             
            System.out.println("Adding this client to active client list");
 
            // add this client to active clients list
            online.add(mtch);
            /*for(int vp=0;vp<online.size();vp++){
                System.out.println(online.get(i).getName());
            }*/
            dos.writeUTF("Connected");
            System.out.println("Connected");
 
            // start the thread.
            t.start();
            
            clientno++;
            }
        }
    }
    
}

class ClientHandler implements Runnable
{
    //Scanner scn = new Scanner(System.in);
    private String name;
    DataInputStream dins;
    DataOutputStream dots;
    Socket s;
    boolean isloggedin;
    int clientno;
    int fcount=0;
    boolean chcksmerror=true;
    //FileController fc;
    ClientHandler(Socket sock, String clientname, DataInputStream in, DataOutputStream out, int clno)
    {
        this.s=sock;
        this.name=clientname;
        this.dins=in;
        this.dots=out;
        this.clientno=clno;
        this.isloggedin=true;
    }

    public String getName(){
        return this.name;
    }
    public Socket getSocket(){
        return this.s;
    }
    
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
    
    public boolean hasChecksumError(String input, int val)
    {
        int res=0;
        for(int k=0;k<input.length();k++){
            if(input.charAt(k)=='1')
            {
                res++;
            }
        }
        if(((byte)res)==((byte)val)){
            return false;
            
        }
        else{return true;}
    }
    
    public byte[] bitDestuffing(byte[] b,int size)
    {
        int p,n, a[]=new int[size];
        System.out.print("arraysize: ");
        System.out.println(size);
        n=size;
        String recframe="";
        for(p=0;p<n;p++)
        {
            a[p]=(int)b[p];
            recframe+=itos(a[p]);
            System.out.println(recframe);
        }
        System.out.println(recframe);
        int ap=recframe.length()-1;
        int count=0;
        while(count<6){
            if(recframe.charAt(ap)=='1'){
                count++;
            }
        else{count=0;}
            ap--;
        }
        String actual=recframe.substring(8, ap);
        System.out.println(actual);
        int scount=0;
        String fval="";
        for(int k=0;k<actual.length();k++){
                if(actual.charAt(k)=='1'){
                    scount++;
                }
                else{
                    scount=0;
                }
                fval+=actual.charAt(k);
                if(scount==5){
                    scount=0;
                    k++;
                }
            }
        actual=fval;
        System.out.println(actual);
        int chunkno=Integer.parseInt(actual.substring(0, 8), 2);
        ap=actual.length();
        String actual1=actual.substring(8, ap);
        ap=actual1.length();
        int realchecksum=Integer.parseInt(actual1.substring(ap-8, ap),2);
        System.out.println(realchecksum);
        String payload=actual1.substring(0, ap-8);
        System.out.print("Payload :");
        System.out.println(payload);
        boolean error=hasChecksumError(payload,realchecksum);
        //error=false;
        int finalsize=payload.length()/8;
        byte[] tobewrite= new byte[finalsize];
        int f=0,g=8,h=0;
        if(error==false)
        {
            this.chcksmerror=false;
            while(g<payload.length()){
                String pq=payload.substring(f, g);
                tobewrite[h]=(byte)Integer.parseInt(pq, 2);
                f=g;
                g=g+8;
                h++;
            }
        }
        else{this.chcksmerror=true;}
        return tobewrite;
    }
    
    public void DoOperation(String task, ClientHandler client) throws IOException
    {
        try {
            if (task.contentEquals("S")) {
                
                this.dots.flush();
                DataInputStream din = new DataInputStream(client.s.getInputStream());
                
                int filelength = Integer.parseInt(din.readUTF());
                System.out.println(filelength);
                String ext = din.readUTF();
                File bar = new File("C:\\Users\\Sadman Yasar Ridit\\Desktop\\TCPServer\\Shouse\\file" + this.clientno + "-" + this.fcount + "." + ext);
                bar.createNewFile();
                this.fcount++;
                FileOutputStream fout = new FileOutputStream(bar);
                this.dots= new DataOutputStream(fout);
                DataOutputStream dout=new DataOutputStream(s.getOutputStream());
                //bout.flush();
                int byteread=0,cno=1;
                int arraysize=600;
                System.out.println(arraysize);
                byte[] incoming=new byte[arraysize];
                while(byteread<filelength){
                    //Do bitDeStuffing
                    
                    int current=this.dins.read(incoming, 0, arraysize);
                    //System.out.println(bitDestuffing(incoming, current));
                    byte[] towrite=bitDestuffing(incoming, current);
                    System.out.println(towrite);
                    if(this.chcksmerror==false){
                        byteread+=current;
                        
                        this.dots.write(towrite);
                        dout.writeUTF("Okay");
                        System.out.println("Chunk no. " + cno + " received from client. \n");
                        cno++;
                    }
                    else{
                        continue;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public synchronized void run(){
        while(true)
        {
            try {
                String received;
                // receive the string
                //dos.writeUTF("Enter task 'R' or 'S'");
                System.out.println("Waiting for operation...");
                received = dins.readUTF();
                 
                System.out.println(received);
                 
                if(received.equals("logout")){
                    System.out.println("Logging Out.");
                    dots.writeUTF("Logged Out");
                    for(int k=0;k<DLLserver.online.size();k++){
                        if(DLLserver.online.get(k).getName()==this.getName()){
                            DLLserver.online.remove(k);
                            break;
                        }

                    }
                    this.isloggedin=false;
                    this.s.close();
                    break;
                }
                else{              
                    StringTokenizer st = new StringTokenizer(received,"#");
                    String WorkToDo = st.nextToken();
                    String recipient = st.nextToken();
                    int flag=0;
                    ClientHandler p=null;
                    for (ClientHandler mc : DLLserver.online) 
                    {
                        if (mc.getName().equals(recipient) && mc.isloggedin==true) 
                        {
                            flag=1;
                            p=mc;
                            break;
                        }
                    
                    }
                    dots.flush();
                    if(flag==0){
                        dots.writeUTF("Offline");
                        System.out.println("Offline");
                        continue;
                    }
                    else{
                        dots.writeUTF("Online");
                        DoOperation(WorkToDo,p);
                        System.out.println("Task Done.");
                    }
            
                }
            } catch(IOException e){
                e.printStackTrace();
            }
        }
    }
    
}
