package Server;
import java.io.*;
import java.net.*;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileSystemView;
public class Server {

    static final int ServerCapacity = 999999999;
    static int ServerStotage = 0;
    // static Long BufferSize=1000L;
    static Hashtable<Integer, Integer> ClientInfo = new Hashtable<>();
    static Hashtable<String, byte[]> fileTransfer = new Hashtable<>();
    static Hashtable<Integer, Socket> SocketList = new Hashtable<>();
    static Hashtable<Integer, DataOutputStream> outStream = new Hashtable<>();
    static Hashtable<String, byte[]> OfflineSend = new Hashtable<>();
    static Hashtable<Integer,Integer> Size = new Hashtable<>();
    static Hashtable<Integer,Integer> totalOnecount = new Hashtable<>();
    static Hashtable<Integer,Integer> Error = new Hashtable<>();
       // static DataOutputStream m1;
    
    
    
    // static int Onecount;
    // static int bitStuffSize;
    // static int count;
    // static byte[] a;
     
     
     
     
    // static int fOnecount;
    //static int fcount;
    //static int fbyteSize;
     synchronized  static int repeatLoop(int i)
     {
         int a =3;
         while(i>a)
         {
             a=a+3;
         }
       return a-i;  
     }

    synchronized  static void ErrorMessage(int id,int frame,String filename)
    {
               String[] options1 = {"Okay"};
               JPanel panel1 = new JPanel();
              JLabel lbl = new JLabel("An ERROR HAS OCCURED ON FRAME "+frame+"  Filename:  "+filename+"  FROM ID "+id);             
              panel1.add(lbl);         
              int selectedOption1 = JOptionPane.showOptionDialog(null, panel1, "File Transmission", JOptionPane.NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options1 , options1[0]);
            
        
    }
 
                  synchronized static void  setError(int id,int result)
      {
         Server.Error.put(id, result);
      }
      
       synchronized static int getError(int id)
      {
          int result = 0,a;
     Set set = Server.Error.keySet(); // get set view of keys
    Iterator<Integer> itr = set.iterator();     // get iterator
    while(itr.hasNext()) {
      a = itr.next();
      if(a==id)
      {
          result=Server.Error.get(a);
          break;
      }
   
    }
    
    return result;
      } 
    
    
    
    
    
    


 
    
    
               synchronized static void  settotalOnecount(int id,int fbyteSize)
      {
         Server.totalOnecount.put(id, fbyteSize);
      }
      
       synchronized static int gettotalOnecount(int id)
      {
          int size = 0,a;
     Set set = Server.totalOnecount.keySet(); // get set view of keys
    Iterator<Integer> itr = set.iterator();     // get iterator
    while(itr.hasNext()) {
      a = itr.next();
      if(a==id)
      { 
          size=Server.totalOnecount.get(a);
          break;
      }
   
    }
    
    return size;
      }
    
  
    
        synchronized  static String removeZero(String s2)
    {
        String Data="";
       int fcount=0;
      //  System.out.println(s2);
        for(int s=0;s<s2.length();s++)
             {
               
                if(fcount==5 && s2.charAt(s)=='0')
                   {
                             
                             fcount=0;
                   }
                else if(fcount==5 && s2.charAt(s)=='1')
                {
                    fcount=0;
                    Data+="1";
                }
                else if(s2.charAt(s)=='1')
                   {
                              fcount++;
                               Data+="1";

                    }
                else if(s2.charAt(s)=='0')
                  {
                       Data+="0";
                       fcount=0;
                  }
        }
        
        
         //System.out.println("Finish destuffing");
        
       return Data; 
    }
        
      synchronized static void  setSize(int id,int fbyteSize)
      {
         Server.Size.put(id, fbyteSize);
      }
      
       synchronized static int getSize(int id)
      {
          int size = 0,a;
     Set set = Server.Size.keySet(); // get set view of keys
    Iterator<Integer> itr = set.iterator();     // get iterator
    while(itr.hasNext()) {
      a = itr.next();
      if(a==id)
      {
          size=Server.Size.get(a);
          break;
      }
   
    }
    
    return size;
      }
    
 synchronized static byte[] destuffing(int Size,byte[] Array,int id) throws FileNotFoundException, IOException
    {
        boolean b=true;
        byte b2;
        String Data="";
        int fbyteSize;
      
        
        for(int i=0;i<Size;i++)
          {
            
            b2=(byte)Array[i];
            String s2 = String.format("%8s", Integer.toBinaryString(b2 & 0xFF)).replace(' ', '0');
            Data+=s2;

        }
      // System.out.println("string length is  "+Data.length());
       
        int index = 0;
        for(int i=Data.length()-1;i>=0;i--)
        {
            if(Data.charAt(i)=='1')
            {
                index=i+1;
                break;
            }
           // System.out.println("i is "+i);
        }
        
        Data=Data.substring(0, index+1);
       // System.out.println("Before remove zero");
        
        Data=Server.removeZero(Data);
       // System.out.println("After remove zero string length is "+Data.length());
        
       
        if(Data.length()%8!=0)
        {
            if((Data.length()+1)%8==0)Data+="0";
            else if((Data.length()-1)%8==0)Data=Data.substring(0, Data.length()-1);
        }
        
        
            String check=Data.substring(27, Data.length()-16);
            int Onecount=cntOne(check,id); //Getting number Of one
      
        
      // System.out.println("Data is  "+Data+"   data size is "+Data.length());
       // System.out.println("destuffed String is "+Data);
        
      // Server.fbyteSize=Data.length()/8;
       fbyteSize=Data.length()/8;
       // System.out.println("Data is "+Data+" Data length is "+Data.length());
        
     byte[] fstuffedByte=new byte[fbyteSize];
        fstuffedByte=Server.getByteByString(Data);
        
        
        byte[] fdesiredByte=new byte[fbyteSize-6];
        
        for(int i=4;i<fbyteSize-2;i++) fdesiredByte[i-4]=fstuffedByte[i];
        
         
        
           int checkSome=(int)fstuffedByte[fbyteSize-2];
          // b2=fstuffedByte[fbyteSize-2];
          // String s2 = String.format("%8s", Integer.toBinaryString(b2 & 0xFF)).replace(' ', '0');
          // System.out.println(s2);
           if(checkSome==Onecount)setError(id,0);
           else
                setError(id,1);
           System.out.println("********checksome "+checkSome+"  *****count "+Onecount+"  *****");

      //  String s = new String( fdesiredByte);
        
   
        
        setSize(id,fbyteSize-6);
        System.out.println("Finish destuffing");
   
       return  fdesiredByte;
    }
     

    synchronized static byte[] getByteByString(String binaryString) {
        int splitSize = 8;

        if (binaryString.length() % splitSize == 0) {
            int index = 0;
            int position = 0;

            byte[] resultByteArray = new byte[binaryString.length() / splitSize];
            StringBuilder text = new StringBuilder(binaryString);

            while (index < text.length()) {
                String binaryStringChunk = text.substring(index, Math.min(index + splitSize, text.length()));
                Integer byteAsInt = Integer.parseInt(binaryStringChunk, 2);
                resultByteArray[position] = byteAsInt.byteValue();
                index += splitSize;
                position++;
            }
            return resultByteArray;
        } else {
           // System.out.println("Cannot convert binary string to byte[], because of the input length. '" + binaryString + "' % 8 != 0");
            return null;
        }
    }


    synchronized static int cntOne(String S,int id)
    {
        int a=0;
        for(int i=0;i<S.length();i++)
        {
            if(a>127)a=0;
            if(S.charAt(i)=='1')a++;
        }
        settotalOnecount(id,a);
        return a;
    }
    

  
    
    
    
    
    
    

    static public String FileSend(String FID) throws IOException, InterruptedException {
        System.out.println("Inside Filesend");
        int fileSendCount = 1;
        String fileid = FID, msg = null;
        int flag = 0;
        FileOutputStream OuttoDestination;

        byte[] out = Server.fileTransfer.get(FID);  // HERE GETTING FILEDATA BY FILEID

        Socket os = null;
        int i = fileid.indexOf("\\");
        String filename = fileid.substring(0, i);

        fileid = fileid.trim().substring(i + 1);

        i = fileid.indexOf("\\");

        String fileSize = fileid.substring(0, i);

        fileid = fileid.trim().substring(i + 1);

        i = fileid.indexOf("\\");
        String senderId = fileid.substring(0, i);

        fileid = fileid.trim().substring(i + 1);

        i = fileid.indexOf("\\");
        String receiverId = fileid.substring(0, i);

        fileid = fileid.trim().substring(i + 1);
        fileSendCount = Integer.parseInt(fileid);
        // String SendSentence="Do you want to receive this file\n Filename: "+filename+" Size: "+fileSize+" Sender ID: "+senderId+" Type Yes for accept or type anything except Yes for refuse the file";
       
        Set set = ClientInfo.keySet();
        Iterator<Integer> itr = set.iterator();
        Integer id;
        while (itr.hasNext()) {
            id = itr.next();
            if (id == Integer.parseInt(receiverId)) {
                flag = 1;
                String Clientresponse = confirmMsg(filename, Integer.parseInt(fileSize), Integer.parseInt(senderId), Integer.parseInt(receiverId));
                if (Clientresponse == "No button clicked") {
                    msg = "ID " + Integer.parseInt(receiverId) + " has rejected the file: " + filename;
                    // String SendSentence="file rejected";
                } else if (Clientresponse == "Yes button clicked") {
                    
                    
                   /* OuttoDestination = new FileOutputStream("C:\\" + receiverId + "\\Copy" + fileSendCount + filename);
                    OuttoDestination.write(out, 0, out.length);
                    OuttoDestination.close();
 */
                  
                
                     
                    DataOutputStream outToServer = new DataOutputStream(SocketList.get(Integer.parseInt(receiverId)).getOutputStream());
                    
                    String SendSentence = "found";
                    outToServer.writeBytes(SendSentence + '\n');
                    SendSentence =fileSize;
                    outToServer.writeBytes(SendSentence + '\n');
                    SendSentence =FID;
                    outToServer.writeBytes(SendSentence + '\n');
                    SendSentence =receiverId;
                    outToServer.writeBytes(SendSentence + '\n');
                    SendSentence =filename;
                    outToServer.writeBytes(SendSentence + '\n');
              
                  //  outToServer.writeInt(Integer.parseInt(fileSize));
                    
                 byte[] go;
                String st="";
                go=Server.fileTransfer.get(FID);
                for(int j=0;j<go.length;j++)
                {
                  
                    st+=(char)go[j];
                }
                
              //  System.out.println(st);
                
                    
                   
                   // outToServer.write(Server.fileTransfer.get(FID));
                  outToServer.writeBytes(st + '\n');
                   
                
                  
                    SendSentence="END";
                    outToServer.writeBytes(SendSentence+ '\n');
                   // outToServer.flush();
                    // Thread.sleep(2000);
                    
                    
                  
                    
                    SendSentence ="finish";
                    outToServer.writeBytes(SendSentence + '\n');
                    
                    
                    System.out.println(filename.substring(filename.length()-4, filename.length()));
                    
                if(filename.substring(filename.length()-4, filename.length()).equalsIgnoreCase(".jpg") ||
          filename.substring(filename.length()-4, filename.length()).equalsIgnoreCase(".png") ||
          filename.substring(filename.length()-4, filename.length()).equalsIgnoreCase(".gif") ||
      filename.substring(filename.length()-4, filename.length()).equalsIgnoreCase(".jpeg"))
      
  {
      System.out.println("picture ");
                File f=new File("C:\\" + receiverId + "\\Copy1234mam" + fileSendCount + filename);
                FileOutputStream fop=new FileOutputStream(f);
                if(!f.exists())f.createNewFile();
                fop.write(go);
                fop.flush();
                fop.close();
                    
   }    
                    
                   
                    Server.fileTransfer.remove(FID);
                    msg = "ID " + Integer.parseInt(receiverId) + " has accepted the file: " + filename;
                } else if (Clientresponse == "JOptionPane closed") {
                    msg = "ID " + Integer.parseInt(receiverId) + " is Unable to accept the file: " + filename + " now";
                }

                break;
            }

        }

   //confirmMsg(String filename,Integer fileSize,Integer SenderId,Integer ReceiverId)
        if (flag == 0) {
            msg = "Receiver Id is offline now , So process is Pending server is waiting for Receiver id " + Integer.parseInt(receiverId) + " to come online";
            Server.OfflineSend.put(FID, out);
        }
        out = null;

        return msg;

    }
  
    public static void main(String argv[]) throws Exception
    {

        int StudentCount = 0;
        int id ;
        int logged = 0;

        
        ServerSocket welcomeSocket = new ServerSocket(6789);
        
        
            offLineThread sendTo = new offLineThread();
            Thread K = new Thread(sendTo);
            K.start();
        
        
        while(true)
        {
            Socket connectionSocket = welcomeSocket.accept();
             logged =0;  
            
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream())); 
         DataOutputStream outToServer = new DataOutputStream(connectionSocket.getOutputStream());
                 
        String clientSentence = inFromUser.readLine();
        id=Integer.parseInt(clientSentence);
            
    
            String IP = Inet4Address.getLocalHost().getHostAddress();
            Integer StdId;
            Set set = ClientInfo.keySet();
            
            Iterator<Integer> itr = set.iterator(); 
            
            while(itr.hasNext()) 
            {
                 StdId = itr.next();
                 if(StdId==id)
                 {
                     System.out.println("This ID "+id+ " has already been logged in, Please Enter another ID"); 
                     clientSentence="0";
                     outToServer.writeBytes(clientSentence + '\n'); 
                     
                     connectionSocket.close();
                     inFromUser.close();
                
                     logged=1;
                     
                     break;
                     
                 }
            }
           
            
            if(logged==1)continue;
            
              clientSentence="1";
             outToServer.writeBytes(clientSentence + '\n'); 
            
            ClientInfo.put(id, connectionSocket.getPort());
            
            
                    File theDir = new File("C:\\"+id);


                      if (!theDir.exists()) //CREATING NEWDIRECTORY
                      {
                                    try
                                   {
                                       theDir.mkdir();
                                       
                                    }                          
                 
                      catch(SecurityException se){ } 
                                   
                                   
                      }
      
          


            
            
            
            
            

            WorkerThread wt = new WorkerThread(connectionSocket,id);
            Thread t = new Thread(wt);
            t.start();
            StudentCount++;
            System.out.println("Client [" + id + "] is now connected. Local port  Number is = " +welcomeSocket.getLocalPort());
           
            System.out.println("socket number is "+connectionSocket.getPort());

            
            System.out.println("IP number is "+Inet4Address.getLocalHost().getHostAddress()+id);
        }
		
    } 
    
    
  static String confirmMsg(String filename,Integer fileSize,Integer SenderId,Integer ReceiverId) 
  {
      String msg = null;
      String s1="Do you want to receive the file? File name: "+filename+" Filesize: "+fileSize+" File Sender Id : "+SenderId;
      String s2="Confirmation for Receiving file of ID: "+ReceiverId;
             JDialog.setDefaultLookAndFeelDecorated(true);
   int response = JOptionPane.showConfirmDialog(null, s1,s2,
       // int response = JOptionPane.showConfirmDialog(null, "Do you want to receive the file? File name: Server.text Filesize: 2246 File Sender Id : 3?", "Confirmation of ID: 5",

         JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
    if (response == JOptionPane.NO_OPTION) {
        System.out.println("no button click");
      msg="No button clicked";
    } else if (response == JOptionPane.YES_OPTION) {
      msg="Yes button clicked";
    } else if (response == JOptionPane.CLOSED_OPTION) {
     msg="JOptionPane closed";
    }
    return msg;
  }
    
}
class WorkerThread implements Runnable
{
    private Socket connectionSocket;
    private int id;
    private int fileSendCount=0;
    private int receiverId;

    private Long size;
    String filename;
    String fileId;
    public WorkerThread(Socket ConnectionSocket, int id) 
    {
        this.connectionSocket=ConnectionSocket;
        this.id=id;
    }
    public void run()
    {
            String clientSentence;
        
            DataOutputStream outToServer = null;
            BufferedReader inFromUser = null; 
        try {
            inFromUser = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
             outToServer = new DataOutputStream(connectionSocket.getOutputStream());
             
           // Server.inStream.put(this.id, inFromUser);  //STORING INPUT MESSAGE PORT
              Server.outStream.put(id, outToServer);  //STORING INPUT MESSAGE PORT
             Server.SocketList.put(id, connectionSocket); 
            
            System.out.println("ID is "+this.id);
        } catch (IOException ex) {
        }
            DataInputStream inFromServer;
            FileOutputStream OuttoDestination;
            
            
        
            
            String SendSentence="Choose your file from filechooser";
        while(true)
        {
            try
            {
               // SendSentence="Enter your file name";
               
                inFromServer = new DataInputStream(connectionSocket.getInputStream());
              
                
               // outToServer = new DataOutputStream(connectionSocket.getOutputStream());
                outToServer.writeBytes(SendSentence + '\n');               
                   
                clientSentence = inFromUser.readLine(); //GETTING FILENAME
                filename=clientSentence;
                
                clientSentence = inFromUser.readLine(); //GETTING FILESIZE
                 size=Long.parseLong(clientSentence);
                 clientSentence = inFromUser.readLine(); 
                 receiverId=Integer.parseInt(clientSentence); //GETTING RECEIVER'S ID
                 System.out.println("receiver id is "+receiverId);
                 
                 if(size+Server.ServerStotage>Server.ServerCapacity)
                 {
                     SendSentence="overflows";
                     outToServer.writeBytes(SendSentence + '\n');  
                 }
                 
                 else
                 {
                     Server.ServerStotage+=size;  // Decreasing SERVER SPACE
                     int randomNum = 1 + (int)(Math.random() * size); 
                     SendSentence="okay";
                      outToServer.writeBytes(SendSentence + '\n');
                      SendSentence=""+randomNum;                  //SENDING MAXIMMUM CHUNK SIZE
                      outToServer.writeBytes(SendSentence + '\n');
                      fileSendCount++;
                      SendSentence=filename+"\\"+size+"\\"+id+"\\"+receiverId+"\\"+fileSendCount;    //SENDING FILE ID
                      fileId=SendSentence;
                       outToServer.writeBytes(SendSentence + '\n');                       
                       System.out.println("File id is "+fileId);
                       
                       
                       
               // inFromServer = new DataInputStream(connectionSocket.getInputStream());
               
		// OuttoDestination = new FileOutputStream("C:\\Users\\Mamun\\Documents\\NetBeansProjects\\Test\\src\\test\\Copy"+fileSendCount+filename);
		
                
                 ByteArrayOutputStream Buffer = new ByteArrayOutputStream();
                 int filesize = size.intValue(); // CONVERTING FILESIZE TO INTEGER
                 byte[]array=new byte[2500]; // WILL STORE CLIENT DATA ACCORDING TO CHUNK SIZE
                 

		int read = 0,i;
		int totalRead = 0;
		int remaining = filesize;
                int index=0;
               // int rem=2406;
                  int rem=126;
                
               // int chunksize=(int)Math.ceil(size*1.0/2000);
                 int chunksize=(int)Math.ceil(size*1.0/100);
                
      /*    read=inFromServer.read(array);
          Buffer.write(array, 0, read);
          System.out.println("Size is "+read);
                */
                
                   //  System.out.println("File size is "+size);
                
                byte[] data;
                int control=0;
                
                
                
		//while((read = inFromServer.read(array, 0, Math.min( rem,126))) > 0) {
                   // index++;
                
                     System.out.println("Sender will send "+chunksize+" times size is "+ size);
                for( i=1;i<=chunksize;i++)
                {
                      
			//totalRead += read;
			//remaining -= read;
                   // read = inFromServer.read(array, 0, Math.min( rem,2406));
                    read = inFromServer.read(array, 0, Math.min( rem,126));
                    
                    if(control>0)
                    {
                        i--;
                        control--;
                        continue;
                    }
                   // i=currnum;
                  //  currnum=-1;
			System.out.println("read " + read + " bytes.");
		
                      // Buffer.write(array, 0, read);
                        data=new byte[Server.getSize(id)];
                       data=Server.destuffing(read,array,id);
                       int Error=Server.getError(id);
                       if(Error==1)
                       {
                           System.out.println("\n\n###############Error has occured in frame "+i+" for ID "+id+"  ####################");
                          // Server.ErrorMessage(id,(int)array[2],filename);
                           control=Server.repeatLoop(i);
                               SendSentence="0";
                              outToServer.writeBytes(SendSentence + '\n');
                              i--;
                       }
                       else
                       {
                            Buffer.write(data, 0,Server.getSize(id) ); 
                            System.out.println("\n\n############### No Error in frame "+ i+"  ####################");
                             SendSentence="1";
                              outToServer.writeBytes(SendSentence + '\n');
                       }
                  
                       
                     // Buffer.write(data, 0,Server.getSize(id) );                       
                     //  SendSentence=""+i;
                     // outToServer.writeBytes(SendSentence + '\n');

		          }
                     System.out.println("Finish receiving "+(i-1)+" times");
                
                totalRead=filesize;
                
                if(totalRead==filesize)
                {
                 byte[] out = Buffer.toByteArray();
                 
                Server.fileTransfer.put(fileId,out);        // STORING IN SERVER With RESPECTIVE FILEID 
                

 
                
                 SendSentence=Server.FileSend(fileId );    // File is sending with the help of FileId by FileSend Function
                 
                 outToServer.writeBytes(SendSentence + '\n');      
                 
                   
                        
              //  OuttoDestination.write(out, 0, totalRead);
                 out=null;
                 Buffer=null;

                }
                
                else
                {
                 SendSentence=" Fail! all chunks size does not match the initial file size";
                 outToServer.writeBytes(SendSentence + '\n');
                    Buffer=null;
                }


                System.out.println("Total read "+totalRead+" bytes  File size "+filesize+" bytes");
                
              // OuttoDestination.close();
               array=null;
              
                Server.ServerStotage-=size;   // SERVER STORAGE IS DECREASING   
                     
     
                 }
              
                
                       
                
               
              /*   inFromServer = new DataInputStream(connectionSocket.getInputStream());
                
		 OuttoDestination = new FileOutputStream("C:\\Users\\Mamun\\Documents\\NetBeansProjects\\Test\\src\\test\\Copy"+fileSendCount+filename);
		
                
                 ByteArrayOutputStream Buffer = new ByteArrayOutputStream();
                 byte[]array=new byte[1000];
                 
                 int filesize = size.intValue(); // Send file size in separate msg
		int read = 0;
		int totalRead = 0;
		int remaining = filesize;
		while((read = inFromServer.read(array, 0, Math.min(1000, remaining))) > 0) {
			totalRead += read;
			remaining -= read;
			System.out.println("read " + totalRead + " bytes.");
			//OuttoDestination.write(buffer, 0, read);
                        Buffer.write(array, 0, read);
                       
		}
                byte[] out = Buffer.toByteArray();
                OuttoDestination.write(out, 0, totalRead);
                array=null;
                Server.ServerStotage-=size;
               
                
              */

   
                 
                 
                 
                
                
                
                 SendSentence="Type Yes to send another file otherwise type Anything";
                outToServer.writeBytes(SendSentence + '\n');
                
                clientSentence = inFromUser.readLine(); 
                
              

                
                
                  /*
                 if(clientSentence.toLowerCase().equalsIgnoreCase("YES"))   //TIMEOUT CASE
                {
                
                break;
                
                } 
                */
                if(clientSentence.toLowerCase().equalsIgnoreCase("YES"))
                {
                    SendSentence="";
                    outToServer.writeBytes(SendSentence + '\n');
                }
                
		else
                {
                SendSentence="You have successfully Logged Out";
                outToServer.writeBytes(SendSentence + '\n');
                 System.out.println(id+" has logged out");
                 Server.ClientInfo.remove(id); // REMOVING CLIENT FROM ONLINE
		
		 inFromServer.close();
                connectionSocket.close();

                break;
                }
                
                
            }
            catch(Exception e)
            {
                
            }
           // System.out.println("Mamun2");
            SendSentence="Choose your file from filechooser";
        }
    }
    
    

}



class offLineThread implements Runnable
{
  byte[]out=null;
  String str=null;
  Integer ReceiverId=null;
    public offLineThread() 
    {

    }
    public void run()
    {

        while(true)
        {
       try
         {     
    Set set = Server.OfflineSend.keySet(); // get set view of keys
    Iterator<String> itr = set.iterator();     // get iterator
    while(itr.hasNext()) {
      str = itr.next();
      ReceiverId=getReceiverId(str);
      if(isOnline(ReceiverId))
      {
          // for SENDING FILE WRITE CODE HERE
          sendFile(str);
          
      }
      
    }
                
         }
            catch(Exception e)
            {
                
            }
        }
    }
    
    
    Integer getReceiverId(String FID)
    {
                   
         String fileid=FID;

        int i=fileid.indexOf("\\");
        //String filename=fileid.substring(0, i);
   
        fileid=fileid.trim().substring(i+1);

        i=fileid.indexOf("\\");
        
       // String fileSize=fileid.substring(0, i);
     
         fileid=fileid.trim().substring(i+1);
 
         i=fileid.indexOf("\\");
       // String senderId=fileid.substring(0, i);
       
         fileid=fileid.trim().substring(i+1);
           
           
           
           i=fileid.indexOf("\\");
        String receiverId=fileid.substring(0, i);
       
         fileid=fileid.trim().substring(i+1);
        // fileSendCount=Integer.parseInt(fileid);
         return Integer.parseInt(receiverId);
    }
    
    
    
    void sendFile(String FID) throws IOException
    {
          int fileSendCount=1;
         String fileid=FID,msg = null;
          int flag=0;
          FileOutputStream OuttoDestination;
          
       byte[] out = Server.OfflineSend.get(FID);
          
          Socket os = null;
        int i=fileid.indexOf("\\");
        String filename=fileid.substring(0, i);
   
        fileid=fileid.trim().substring(i+1);

        i=fileid.indexOf("\\");
        
        String fileSize=fileid.substring(0, i);
     
         fileid=fileid.trim().substring(i+1);
 
         i=fileid.indexOf("\\");
        String senderId=fileid.substring(0, i);
       
         fileid=fileid.trim().substring(i+1);
           
           
           
           i=fileid.indexOf("\\");
        String receiverId=fileid.substring(0, i);
       
         fileid=fileid.trim().substring(i+1);
         fileSendCount=Integer.parseInt(fileid);
         
         
      try {
          OuttoDestination = new FileOutputStream("C:\\"+receiverId+"\\Copy"+fileSendCount+filename);
          OuttoDestination.write(out, 0, out.length);
          OuttoDestination.close();
      } catch (FileNotFoundException ex) {
          Logger.getLogger(offLineThread.class.getName()).log(Level.SEVERE, null, ex);
      }

        Server.OfflineSend.remove(FID);     
             
                        // STORING IN SERVER With RESPECTIVE FILEID 

         
    }
    
    boolean isOnline(Integer ID)
    {
        Integer idCheck;

         Set set = Server.ClientInfo.keySet(); // get set view of keys
    Iterator<Integer> itr = set.iterator();     // get iterator
    while(itr.hasNext()) {
      idCheck = itr.next();
      if(idCheck==ID) return true;
    }
    return false;
}
}