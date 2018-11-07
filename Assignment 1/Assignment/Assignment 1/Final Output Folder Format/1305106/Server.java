import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;


import java.net.*;


public class Server extends Component implements ActionListener {
    public static int workerThreadCount = 0;
    public static int workerThread1Count = 0;

    ServerSocket serverSocket = null;
    Socket socket = null;
    int rr;
    ArrayList<JCheckBox> checkboxes;
    ArrayList<String> a=new ArrayList<>();
    ObjectInputStream inputStream = null;

    ObjectOutputStream outputStream;
    //private ObjectInputStream objectInputStream;
    File dstFile = null;
    FileOutputStream fileOutputStream = null;
    File yourFolder;
    File file;
    int pol=10;
    Memo mem=new Memo();
    NetworkUtil nc;
    String dest;
    int portNumber;
    public Server() {
        /*JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Choose Your Root Directory");
        fc.setCurrentDirectory(new java.io.File(".")); // start at application current directory
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = fc.showSaveDialog(this);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            yourFolder = fc.getSelectedFile();
        }
        dest=yourFolder.getAbsolutePath();
        System.out.println(yourFolder.getAbsolutePath());*/


        portNumber = 50;


        int id=1;
        int id1=1;

        try {
            serverSocket = new ServerSocket(portNumber);
            while (true) {
                socket = serverSocket.accept();
                nc = new NetworkUtil(socket);
                inputStream = new ObjectInputStream(socket.getInputStream());
                outputStream = new ObjectOutputStream(socket.getOutputStream());

                //this.objectOutputStream=new ObjectOutputStream(socket.getOutputStream());

                //System.out.println("Client [" + id + "] is now connected. No. of worker threads = " + workerThreadCount);

                //rr=receiveRoll();
                int tt= (int) nc.read();
                System.out.println("already read "+tt);


                if(tt==1)
                {WkerThread wt = new WkerThread(this);
                Thread t = new Thread(wt);
                t.start();
                workerThreadCount++;
                System.out.println("Client [" + id + "] is now connected. No. of worker threads = " + workerThreadCount);

                id++;}
                else
                {
                    WkerThread1 wt = new WkerThread1(this);
                    Thread t = new Thread(wt);
                    t.start();
                    workerThread1Count++;
                    System.out.println("Client [" + id1 + "] is now connected. No. of worker threads = " + workerThreadCount);

                    id1++;
                }
                //downloadFiles(socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public int receiveRoll(){
        //int type=(int) nc.read();
        System.out.println("reached1");



        int roll= (int) nc.read();

        System.out.println("reached2");
        return roll;
    }

    public void downloadFiles(Socket socket) {
        while (socket.isConnected()) {
            try {

                int roll= (int) nc.read();

                //System.out.println("pool : " +pol);
                String ss=(String) nc.read();
                //System.out.println("name reacieved " +ss);

                long r= (long) nc.read();

                int bytesRead = 0;
                int total = 0;
                byte[] contents = new byte[1027];
                int randomNum = ThreadLocalRandom.current().nextInt(1, 15);

                nc.write(randomNum);
                //System.out.println("rand generated " +randomNum);


                int id=mem.Getfile(roll,(int)r,ss);
                int z=1;
                 int [] complete=new int[5000];
                 for(int i=0;i<5000;i++)
                     complete[i]=0;
                 int seq=1;

                while (total !=r )//receive filesize
                {
                    //System.out.println("total : " +total+" "+r);

                    //contents=(byte[])inputStream.readObject();
                    try{
                        contents=(byte[])inputStream.readObject();


                    }catch(IOException e){
                        //e.printStackTrace();
                        //System.out.print(obj.getOrigin() + " From server's user class");
                        System.out.println("This is from user run function");
                    } catch (ClassNotFoundException ex){
                        System.err.println("YOUR PROBLEM WAS HERE...");
                    }
                    //objectOutputStream.writeObject("received");
                    //objectOutputStream.flush();
                    //total+=contents.length-3;
                    int len,rseq,tp,cksum;

                    byte[] cont;
                    int ff=3;

                         len = (int)contents.length - 4;
                        rseq = (int)contents[1];
                    System.out.println("Sequence no "+rseq+ " recieved");

                    tp = (int)contents[0];
                         cksum = (int)contents[contents.length - 1];
                       cont = new byte[len];
                        System.arraycopy(contents, 3, cont, 0, len);
                        byte yo = 0;
                        byte ck = 0;
                        for (byte b : cont) {
                            yo ^= b;
                        }
                        int cked=(int) yo;
                        if(complete[rseq]==1) {
                            ff = 1;
                            System.out.println("duplicate pack recieved with seq no "+rseq);


                        }
                        else if(cked!=cksum)
                        {
                            ff=2;
                            System.out.println("checksum error in frame with seq no "+rseq);


                        }
                        else
                        {
                            System.out.println("Correct frame recieved with seq no "+rseq);

                        }



                    if(ff==1)
                    {
                        byte[] ult=new byte[5];
                        byte[] type={2};
                        byte[] jj={(byte) seq};
                        byte[] ac={(byte) rseq};
                        System.arraycopy(type,0,ult,0,1);
                        System.arraycopy(jj,0,ult,1,1);
                        System.arraycopy(ac,0,ult,2,1);
                        outputStream.writeObject(ult);
                        outputStream.flush();
                        continue;
                    }
                    //else if(ff==2)
                    if(ff==2)
                    {
                        byte[] ult=new byte[5];
                        byte[] type={2};
                        byte[] jj={(byte) seq};
                        byte[] ac={-1};
                        System.arraycopy(type,0,ult,0,1);
                        System.arraycopy(jj,0,ult,1,1);
                        System.arraycopy(ac,0,ult,2,1);
                        outputStream.writeObject(ult);
                        outputStream.flush();
                        continue;

                    }
                    complete[rseq]=1;




                    String fn="";
                for(int i=0;i<len;i++)
                {
                    byte sum=cont[i];
                    String fromByteToString = String.format("%8s", Integer.toBinaryString(sum & 0xFF)).replace(' ', '0');
                    fn+=fromByteToString;
                }
                    System.out.println("frame before destuffing "+fn);

                    int counter=0;
                int bitfills=0;
                String fst="";
                char cc='0';
                for(int i=0;i<fn.length();i++)
                {
                    char rr=fn.charAt(i);
                    if(rr=='0')
                        counter=0;
                    else
                        counter++;
                    fst=fst+rr;
                    if(counter==5)
                    {
                        counter=0;
                        bitfills++;
                        i++;
                    }
                    int trav=i+1-bitfills;
                    int rem=fn.length()-i-1;
                    trav=trav%8;
                    if(trav==0 && rem<8)
                        break;

                }

                    System.out.println("frame after destuffing "+fst);

                    total+=fst.length()/8;



                byte[] bval = new BigInteger(fst, 2).toByteArray();
                int acsize=bval.length;




                    String s=new String(bval);

                    z=mem.Getchunk(id,roll,s,acsize);
                    //System.out.println("pack "+seq+" recieved with size "+acsize+"and received and cal checksums are "+cksum+" "+yo);
                    //System.out.println("pack "+rseq+"len "+s.length()+" recieved string "+s);

                    //System.out.println("total : " +total+" "+r);

                    if(z==0)
                    {
                        mem.del(id,roll);
                        break;
                    }
                    byte[] uu=new byte[5];
                    byte[] tt={2};
                    byte[] jjj={(byte) seq};
                    byte[] acc={(byte) rseq};
                    System.out.println("Sending ack for frame with seq no "+rseq);
                    System.arraycopy(tt,0,uu,0,1);
                    System.arraycopy(jjj,0,uu,1,1);
                    System.arraycopy(acc,0,uu,2,1);
                    outputStream.writeObject(uu);
                    outputStream.flush();




                }

                if(z==0)
                    System.out.println("not enough chunks ");

                else {
                    System.out.println("file " + id + " from " + roll + " successfully saved ");


                    System.out.println("hello roll completed "+roll);

                    mem.comp(id,roll);
                }

                //mem.ck(id);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public void sendFiles(Socket socket) throws IOException {
        while (socket.isConnected()) {
            try {

                int roll= (int) nc.read();



                System.out.println("inside sendfile");

                byte[] contents = new byte[1024];


                System.out.println("roll "+roll);

                while (true)//receive filesize
                {
                    if((mem.totfile[roll])==0)
                        continue;
                    System.out.println("true that");


                    int id=mem.getfile(roll);
                    if(id==-1)
                        continue;
                    nc.write((mem.filename[id]));
                    int tot=mem.getlen(id);
                    nc.write(tot);

                    for(int i=0;i<tot;i++) {

                        String s = mem.getchunkat(id,i);
                        contents=s.getBytes();
                        outputStream.writeObject(contents);
                        outputStream.flush();
                        try {
                            String t= (String) inputStream.readObject();
                            System.out.println(t);
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }


                    }

                    mem.del(id,roll);

                }

        }catch (Exception e) {
                e.printStackTrace();
            }
    }
    }
    public static void main(String[] args) {
        Server server = new Server();

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        for (JCheckBox checkBox : checkboxes) {
            if (checkBox.isSelected()) {
                a.add(checkBox.getLabel());
            }
        }
    }
}
