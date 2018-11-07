
package client;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import static java.lang.Thread.sleep;
import java.math.BigInteger;
import java.net.Socket;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javax.imageio.ImageIO;

/**
 *
 * @author main
 */
public class clientController implements Initializable {
    
   @FXML
    private TextField studentcon;

    @FXML
    private Button logout;

    @FXML
    private TextArea clientcon;

    @FXML
    private TextField sendcon;

    @FXML
    private Button login;

    @FXML
    private Button send;
    
    @FXML
    private Button sndb;
    
    @FXML
    private Button sss;
    
    ///
    
     int secound= 0;
     
    String username, address = "localhost";
    ArrayList<String> users = new ArrayList();
    int port = 2222;
    int port1 = 2225;
    Boolean isConnected = false;
    
    Socket sock;
    Socket extra;
    BufferedReader reader;
    PrintWriter writer;
    
    DataOutputStream dout;
    DataInputStream din;
   
    FileInputStream fis = null;
    BufferedInputStream bis = null;
    OutputStream os = null;
    
    RandomAccessFile ramin;
    RandomAccessFile ramout;
    String dir=null;
    String extention;
    String fname;
    int cnksize=2048;
    File selectedFile;
    byte[] mybytearray = new byte[cnksize];
    boolean sentComplete = false;
    String extentions = null;
    
    List<File> selectedFiles;
    boolean doListen = true;
    int i = 0;
    int Size=0,y=0;
    
    //////
    
    
    public String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
    public void ListenThread() {
         Thread IncomingReader = new Thread(new IncomingReader());
         IncomingReader.start();
    }
    public void userAdd(String data) {
         users.add(data);
    }
    public void userRemove(String data){
         clientcon.setText(clientcon.getText()+data + " is now offline.\n");
    }
    public void writeUsers(){
         String[] tempList = new String[(users.size())];
         users.toArray(tempList);
         for (String token:tempList) 
         {
             
         }
    }
    
    public void sendDisconnect() {
        String bye = (username + ": :Disconnect");
        try
        {
           dout.writeUTF(bye);
           dout.flush();
           // writer.println(bye);
            //writer.flush();
        } catch (Exception e) 
        {
            clientcon.setText(clientcon.getText()+"Could not send Disconnect message.\n");
        }
    }
    public void Disconnect() 
    {
        try 
        {
            clientcon.setText(clientcon.getText()+"Disconnected.\n");
            sock.close();
        } catch(Exception ex) {
            clientcon.setText(clientcon.getText()+"Failed to disconnect. \n");
        }
        isConnected = false;
        //tf_username.setEditable(true);

    }
    /////
    
    
     public class IncomingReader implements Runnable
    {
        String[] data;   
        String message ,file = "File",done = "Done", connect = "Connect", disconnect = "Disconnect", chat = "Chat";    
       
        
        @Override
        public void run() 
        {
            try 
            {
                
                while ((message = din.readUTF()) != null) 
                {
                     data = message.split(":");

                     if (data[2].equals(chat)) 
                     {
                        clientcon.setText(clientcon.getText()+data[0] + ": " + data[1] + "\n");
                        //System.out.println(data[1]);
                        if(data[1].equals("ok sent the file")){
                            sndb.setVisible(true);
                            System.out.println("inside ok");
                        }
                        //ta_chat.setCaretPosition(ta_chat.getDocument().getLength());
                     }
                     else if(data[2].equals("Start")){
                         clientcon.setText(clientcon.getText()+data[0] + ": " + data[1] + "\n");
                     }
                     else if(data[2].equals("No")){
                         clientcon.setText(clientcon.getText()+data[0] + ": " + data[1] + "\n");
                     }
                     else if(data[2].equals("End")){
                         doListen = false;
                         clientcon.setText(clientcon.getText()+data[0] + ": " + data[1] + "\n");
                         
                         margeFiles();
                     }
                     else if(data[2].contains(".")){
                         extentions = data[2];
                         clientcon.setText(clientcon.getText()+data[0] + ": " + data[1]+":"+extentions + "\n");
                         filereceiver();
                     }
                     else if (data[2].equals(connect))
                     {
                        
                        userAdd(data[0]);
                     } 
                     else if (data[2].equals(disconnect)) 
                     {
                         userRemove(data[0]);
                     } 
                      
                     
                     else if (data[2].equals(done)) 
                     {
                        //users.setText("");
                        writeUsers();
                        users.clear();
                     }
                }
           }catch(Exception ex) { }
        }
    }
     public File[] readllfiles(){
        

        File folder = new File("/home/main/src/");
        File[] listOfFiles = folder.listFiles();
        int counter = 0;
        for (File file : listOfFiles) {
            if (file.isFile()) {
                counter++;
                //System.out.println(file.getName());
            }
            
        }
        //System.out.println(counter+"files");


        return listOfFiles;
    }
     public void margeFiles() throws FileNotFoundException, IOException{
            File[] files = readllfiles();
            Arrays.sort(files, new Comparator<File>() {
            @Override
                public int compare(File o1, File o2) {
                    int n1 = extractNumber(o1.getName());
                    int n2 = extractNumber(o2.getName());
                    return n1 - n2;
                }

                private int extractNumber(String name) {
                    int i = 0;
                    try {
                        int s = name.indexOf('_')+1;
                        int e = name.lastIndexOf('.');
                        String number = name.substring(s, e);
                        i = Integer.parseInt(number);
                    } catch(Exception e) {
                        i = 0; // if filename does not match the format
                               // then default to 0
                    }
                    return i;
                }
            });

            try{
                boolean folder = new File("/home/main/download"+username).mkdir();
                File fi = new File("/home/main/download"+username,"some."+extentions);
		ramout=new RandomAccessFile(fi,"rw");
		double raminLength=(int)ramout.length()/(double)cnksize;
		for(int i=0;i<files.length;i++)
		{
                    File f=(File)files[i];
                    ramin=new RandomAccessFile(f,"r");
                    int sze=(int)ramin.length();
					
                    byte byt[]=new byte[sze];
				
                    ramin.read(byt);
                    ramout.seek(Size);
                    Size=Size+sze;
                    ramout.write(byt);
                    ramin.close();
		}
		ramout.close();
				
				
            }
            catch(FileNotFoundException t){}
            catch(IOException o){}
    
    
            
    }
     public  List<File> splitFile(File file) throws IOException {
        List<File> filess = new ArrayList<>();
        try{
            ramin=new RandomAccessFile(file,"r");
				
            byte bt[]=new byte[cnksize];
            int len=(int)ramin.length();
				
            int loops=(len / cnksize);
            int remainingBytes=len-(loops*cnksize);
				
            byte bt1[]=new byte[remainingBytes];
            boolean folder = new File("/home/main/src"+username).mkdir();
            for(int i=1;i<=loops+1;i++)
            {
                                    
					
                
                File files=new File("/home/main/src"+username,username+String.valueOf(i)+extentions);
		ramout=new RandomAccessFile(files,"rw");
		if(i==loops+1)
		{
                    ramin.read(bt1);
                    ramout.write(bt1);
						//clientcon.setText(clientcon.getText()+files.toString()+"     <"+(int)ramout.length()/1024.0+" MB>\n");
						//scrollPane();
                    ramout.close();
                                                
		}
		else
		{
                    ramin.read(bt);
                    ramout.write(bt);
						//clientcon.setText(clientcon.getText()+files.toString()+"     <"+(int)ramout.length()/1024.0+" MB>\n");
						
                    ramin.seek(i*cnksize);
                    ramout.close();
		}
                filess.add(files);
	}
				//clientcon.setText(clientcon.getText()+"\nFinished splitting "+file.toString()+"     <Original size> <"+(int)file.length()/1024.0+" MB>\n");
				
            ramin.close();
	}
	catch(FileNotFoundException e){}
	catch(IOException t){}
        return filess;
        
    }
    public void filereceiver(){
       receiveFile receiveFilehh = new receiveFile();
        Thread filereceiver = new Thread(receiveFilehh);
        if(doListen==true){
            doListen = false;
         filereceiver.start();
        }else {
            doListen = false;
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ex) {
               // Logger.getLogger(serverController.class.getName()).log(Level.SEVERE, null, ex);
            }
            receiveFilehh.terminate();
        }
    
    }
     public class receiveFile implements Runnable{
        
        private volatile boolean running = true;
        
        
        
        
        
        public void terminate() {
            running = false;
        }
        public void copy(InputStream in, OutputStream out) throws IOException {
            byte[] buf = new byte[cnksize];
            int len = 0;
            while ((len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            clientcon.setText(clientcon.getText()+"received\n");
            //sentComplete = true;
        }
        public void receiveFile() throws IOException {
            try{
                //extra.close();
                //
                InputStream in = extra.getInputStream();
                //extra = sock;
                OutputStream out = new FileOutputStream("/home/main/src/"+"_"+i+".mp3");
                //System.out.println(out.toString());
                i++;
                copy(in, out);
                out.flush();
                out.close();
                in.close();
                extra = new Socket(address, port1);
            }catch(IOException e){}




        }
        @Override
        public void run() {
              while(running){ 
                  try {
                      receiveFile();
                      
                  } catch (IOException ex) {
                      //Logger.getLogger(clientController.class.getName()).log(Level.SEVERE, null, ex);
                  }
               }
              clientcon.setText(clientcon.getText()+"All files succesfully received\n");
        }
    
    
    }
    
    public class senderFile implements Runnable{
        int[] total = {0,0,0,0,0,0,0,0}; 
        int[] dummy = {0,0,0,0,0,0,0,0};
     
        public int[] addBinary(int[] a,int[] b) {
               
                int firstbit=0 ;
                int[] sum = {0,0,0,0,0,0,0,0};
                int carry = 0;
                
                
                for(int i = 7; i >= 0; i--){
                    int add = a[i] + b[i] + carry;
                    sum[i] = add % 2;
                    carry = (int)add / 2;
                    //System.out.println(carry);
                }

                while(carry == 1){
                    int[] c = {0,0,0,0,0,0,0,1};
                    if(sum[7]==0){sum[7] = sum[7]+carry;break;}
                    else {
                        carry=0;
                        for(int i = 7; i >= 0; i--){
                            int add = sum[i] + c[i] + carry;
                            sum[i] = add % 2;
                            carry = (int)add / 2;
                            //System.out.println(carry);
                        }
                      
                    }
                }
                  
                return sum;

        }
        public String makechecksum(byte[] buf){
            int i=0;
            for(byte b : buf){

                    String s = Integer.toBinaryString(b & 255 | 256).substring(1);

                    for(int u = 0;u<8;u++){
                        total[u] = Character.getNumericValue(s.charAt(u));
                    }
                    
                    dummy = addBinary(total,dummy);


                    //System.out.println(Arrays.toString(dummy));


            }
            for(i=0;i<8;i++){
                dummy[i] = (dummy[i]==0)?1:0;
            }
           // System.out.println(Arrays.toString(dummy));

            String s = Arrays.toString(dummy).replaceAll("\\[|\\]|,|\\s", "");

            //System.out.println(s);
           //int val = Integer.parseInt(s, 2);
           //byte b = (byte) val;
            //System.err.println(b);
            //System.out.println(b);
            //System.out.println("hhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh");
            return s;
        
        }
        public byte[] concat(byte[]... bufs) {
            if (bufs.length == 0)
                return null;
            if (bufs.length == 1)
                return bufs[0];
            for (int i = 0; i < bufs.length - 1; i++) {
                byte[] res = Arrays.copyOf(bufs[i], bufs[i].length+bufs[i + 1].length);
                System.arraycopy(bufs[i + 1], 0, res, bufs[i].length, bufs[i + 1].length);
                bufs[i + 1] = res;
            }
            return bufs[bufs.length - 1];
        }
        public void copy(InputStream in, OutputStream out) throws IOException, InterruptedException {
            byte[] buf = new byte[cnksize];
            int len = 0;
            String s;
            while ((len = in.read(buf)) != -1) {
                String d = makechecksum(buf);
                for(int i=0;i<8;i++){
                    dummy[i] = 0;
                }
                String stuffbits = "";
                String temp="";
                int count = 0;
                for(byte b : buf){
                    
                    s = Integer.toBinaryString(b & 255 | 256).substring(1);
                    temp+=s;
                   
                }
                
                
               
                stuffbits = Integer.toString(len)+"~"+"01111110"+"~"+d+"~"+StaffBits(temp)+"~"+"01111110";
                byte[] bk = stuffbits.getBytes();
               // byte[] bigByteArray = new byte[fg.length+fs.length+bk.length];
               // ByteBuffer target = ByteBuffer.wrap(bigByteArray);
                //target.put(bk);
                //target.put(fs);
                //target.put(fg);
                //byte[] bytes = ByteStreams.toByteArray(in);
                
                //String sk = new String(bigByteArray);
                //System.out.println(sk);
                //String[] make = sk.split("~");
                
               // System.out.println(make[0]+"~"+make[1]+"~"+make[3]+"~"+temp.length());
                //System.out.println(bigByteArray.length+"+"+len);
               
                
                
                out.write(bk, 0, bk.length);
            }
            
            
         
            
            clientcon.setText(clientcon.getText()+"uploaded\n");
            sentComplete = true;
        }
        
	String StaffBits(String data){
              int counter = 0;
              String res="";
              for(int i=0;i<data.length();i++)
                {
                   
                   if (data.charAt(i)!='1' && data.charAt(i)!='0')
                        {
                            System.out.println("Enter only Binary values!!!");
                            return null;
                        }
                   if(data.charAt(i) == '1')
                        {
                            counter++;
                            res = res + data.charAt(i);
                        }
                   else
                        {
                            res = res + data.charAt(i);
                            counter = 0;
                        }
                   if(counter == 5)
                        {
                            res = res + '0';
                            counter = 0;
                        }
                }
            return res;
        
        }

        String toBinary( byte[] bytes )
        {
            StringBuilder sb = new StringBuilder(bytes.length * Byte.SIZE);
            for( int i = 0; i < Byte.SIZE * bytes.length; i++ )
                sb.append((bytes[i / Byte.SIZE] << i % Byte.SIZE & 0x80) == 0 ? '0' : '1');
            return sb.toString();
        }



        public void sendFile(File f) throws IOException, InterruptedException {


            try{
                //extra.close();
                //
                InputStream in = new FileInputStream(f);
                //extra = sock;
                OutputStream out = extra.getOutputStream();
                copy(in, out);
//                String[] sx = out.toString().split("@");
//               
//                //for(int i=0;i<sx[1].length();i++){
//                System.out.println("///////////////////////////");
//                System.out.println(sx[1]);
//                   // String s = Character.toString(sx[1].charAt(i));
//                BigInteger bigint = new BigInteger(sx[1], 16);
//                System.out.println(bigint.toString(2)+","+bigint.toString(2).length());
//                String s = bigint.toString(2);
//                    
//                System.out.println(StaffBits(bigint.toString(2)));
//                    //System.out.println(sx[1].charAt(i));
//                //}
                out.flush();
                out.close();
                in.close();
                extra = new Socket(address, port1);
            }catch(IOException e){}




        }

        @Override
        public void run() {
                if(selectedFile!=null){
                        try {
                            selectedFiles = splitFile(selectedFile);
                            for(File f : selectedFiles){
                                try {
                                    sendFile(f);
                                    Thread.sleep(500);
                                } catch (IOException ex) {
                                   clientcon.setText(clientcon.getText()+"Can'e sent file");
                                } catch (InterruptedException ex) {
                                    //Logger.getLogger(clientController.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                //System.out.println(f.getName());
                               
                            }
                            dout.writeUTF(username + ": has ended:"+"End");
                            dout.flush();
                            deleteFileas();
                            
                        } catch (IOException ex) {
                           
                        } 
                        //Logger.getLogger(clientController.class.getName()).log(Level.SEVERE, null, ex);
                    
                       
                
                    }
        }
    }
    public void deleteFileas(){
    
        File dir = new File("/home/main/src"+username);
        if (dir.isDirectory()) 
        { 
            
            File[] children = dir.listFiles(); 
            for (int i=0; i<children.length; i++)
            {
                boolean a = children[i].delete();
            }
        }  
  // The directory is now empty or this is a file so delete it 
//  return dir.delete(); 

    }
    Timer mytime = new Timer();
        TimerTask task = new TimerTask(){

            @Override
            public void run() {
                secound++;
            }
                                
    };
    public void start(){
        mytime.scheduleAtFixedRate(task, 1000, 1000);
    
    }
    
     /////
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        sss.setVisible(false);
        sndb.setVisible(false);
        sss.setOnAction(new EventHandler(){

            @Override
            public void handle(Event t) {
                    
                   Thread sender = new Thread(new senderFile());
                    sender.start();
            }
        
        
        });
       
        
        sndb.setOnAction(new EventHandler(){
            private Object FilenameUtils;

            @Override
            public void handle(Event t) {
               
                
                try {
                    FileChooser fileChooser = new FileChooser();
                    
                    
                    File file = fileChooser.showOpenDialog(null);
                   // System.out.println(file);
                    clientcon.setText(clientcon.getText()+file);
                    selectedFile = file;
                    String path = selectedFile.getAbsolutePath();
                   // String s = "/var/www.dir/file.tar.gz";
                    String name = path.substring(path.lastIndexOf("/"));
                    extentions = name.substring(name.indexOf("."));
                    dout.writeUTF(username + ":"+extentions+":Ext");
                    dout.flush();
                    sss.setVisible(true);
                    // sendFile();
                    sndb.setVisible(false);
                } catch (IOException ex) {
                    //.getLogger(clientController.class.getName()).log(Level.SEVERE, null, ex);
                }
            
            }
        });
        
        login.setOnAction(new EventHandler(){

            @Override
            public void handle(Event t) {
                 if (isConnected == false) 
                    {
                        username = studentcon.getText();
                        //tf_username.setEditable(false);

                        try 
                        {
                            sock = new Socket(address, port);
                            extra = new Socket(address, port1);
                            //InputStreamReader streamreader = new InputStreamReader(sock.getInputStream());
                            //reader = new BufferedReader(streamreader);
                            //writer = new PrintWriter(sock.getOutputStream());
                            //writer.println(username + ":has connected.:Connect");
                            //writer.flush(); 
                            din=new DataInputStream(sock.getInputStream()); 
                            dout=new DataOutputStream(sock.getOutputStream());        
                            dout.writeUTF(username + ":has connected:Connect");
                            dout.flush();
                            isConnected = true; 
                        } 
                        catch (Exception ex) 
                        {
                            clientcon.setText(clientcon.getText()+"Cannot Connect! Try Again. \n");
                            //tf_username.setEditable(true);
                        }

                        ListenThread();

                    } else if (isConnected == true) 
                    {
                        clientcon.setText(clientcon.getText()+"You are already connected. \n");
                    }
            }
        
        
        
        });
        
        logout.setOnAction(new EventHandler(){

            @Override
            public void handle(Event t) {
                    sendDisconnect();
                    Disconnect();
            }
        
        
        });
        send.setOnAction(new EventHandler(){

            @Override
            public void handle(Event t) {
                String nothing = "";
                
                    {
                        try {
                            String ss = username + ":" + sendcon.getText() + ":" + "Chat";
                            //System.out.println(ss+"Before");
                            //writer.println(ss);
                            //writer.flush();
                            
                            
                               // sentComplete = false;
                                //System.out.println(ss+"after");
                            
                           dout.writeUTF(ss);
                           dout.flush();

                        } catch (Exception ex) {
                           
                            //System.out.println(ex.toString());
                            clientcon.setText(clientcon.getText()+"Message was not sent. \n");
                        }
                        studentcon.setText("");
                        //tf_chat.requestFocus();
                    }
                   
                    studentcon.setText("");
                    
                    //tf_chat.requestFocus();
            }
        });
        
       
        
        
    }    

        
    
}
