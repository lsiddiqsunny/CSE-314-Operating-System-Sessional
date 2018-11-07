package Client;


import Server.Server;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import java.io.*;
import java.net.*;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
class Client
{
   
    


    synchronized static void FileTransfer(BufferedReader inFromServer,File file,int from ,int to,byte[] Filebytearray,DataOutputStream outToServer,int Size,String FileName,String  ID) throws FileNotFoundException, IOException, InterruptedException
    {
       // Thread.sleep(2000);
                FileInputStream FileInput = new FileInputStream(file);
               BufferedInputStream fileBuffer = new BufferedInputStream(FileInput);
               int read,i;
               String ServerMsg;
               int totalRead=0;
               int remaining =Size;
               
               
               
                                    for( i=1;i<=to;i++)
                  {
                         
                     // read = FileInput.read(Filebytearray, 0,Math.min( remaining,2000));                 
                     read = FileInput.read(Filebytearray, 0,Math.min( remaining,100));
                        if(i>=from)
                        {
                        totalRead += read;
			remaining -= read;
                        //outToServer.write(Filebytearray, 0, read);
                        
                        System.out.println("\n\n##################################read "+read+" bytes");
                  
                
                          
                             Filebytearray=bitstuffing(read,Filebytearray,i,Integer.parseInt(ID),FileName);

                         
                         
                        //outToServer.write(Filebytearray, 0,2406 );
                         outToServer.write(Filebytearray, 0,126 );
                        outToServer.flush();
                        
                       
                            
                         // ServerMsg = inFromServer.readLine(); 
                        
                      
                        }

                  }
               
               
   // Thread.sleep(2000);
        
        
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
            System.out.println("Cannot convert binary string to byte[], because of the input length. '" + binaryString + "' % 8 != 0");
            return null;
        }
    }

    synchronized static String insertZero(String s2,int id) {
        String Data = "";
        
        int count=0;
        for (int s = 0; s < s2.length(); s++) {
      
            if (count == 5) {
                Data += "0";
                count = 0;
            }
            if (s2.charAt(s) == '1') {
                count++;
                Data += "1";
                
                // checkSome+="0";
            } else {
                Data += "0";
                // checkSome+="1";
                count = 0;
            }
        }
        
        

        return Data;
    }
    synchronized static int cntOne(String S,int id)
    {
        int a=0;
        for(int i=0;i<S.length();i++)
        {
            if(a>127)a=0;
            if(S.charAt(i)=='1')a++;
        }
      
        return a;
    }
    

    synchronized static byte[] bitstuffing(int bufferSize, byte[] Array, int seqNum,int id,String filename) throws InterruptedException {
      
   
       
        
        
        
        byte b2, W;

        int count = 0;
        int Onecount=0;

        W = (byte) seqNum;
        String sequence = String.format("%8s", Integer.toBinaryString(W & 0xFF)).replace(' ', '0');

      //  sequence = Trial.insertZero(sequence);   // Inserting ZERO

        String s3 = "00100000" + sequence + "00000000";

        //  String checkSome="";
        count = 0;
        Onecount = 0;  //we need not to count the number of one in sequence Number
        String mainData="";
        for (int i = 0; i < bufferSize; i++) {

            b2 = (byte) Array[i];
            String s2 = String.format("%8s", Integer.toBinaryString(b2 & 0xFF)).replace(' ', '0');
            
           // s3 += Trial.insertZero(s2);
            s3+=s2;
            mainData+=s2;
           // System.out.println(s3);
           

        }
        Onecount=Client.cntOne(mainData,id);
        b2 = (byte)Onecount;
        String s2 = String.format("%8s", Integer.toBinaryString(b2 & 0xFF)).replace(' ', '0');
       System.out.println("********checksome "+s2+"  *****count "+Onecount+"  *****");

        //s3 += Trial.insertZero(s2);
        s3+=s2;
    // System.out.println("01111110"+s3+"01111110");
      //  System.out.println("S3 length before inserting zero is "+s3.length());
        s3=Client.insertZero(s3,id);
        s3="01111110"+s3;
        s3 += "01111110";
      //  System.out.println(s3);


          //  int zeroCount = 19248-s3.length();
             int zeroCount = 1008-s3.length();
          
            for (int i = 1; i <= zeroCount; i++) {
                s3 += "0";
            }

        //  System.out.println("S3 length is "+s3.length());

        //}
          
         if(seqNum%20==0) {s3=confirmMsg(s3,seqNum,id,filename) ;}//---------------------------
           //  Thread.sleep(1000); 
        // if(seqNum%3==0) {s3=confirmMsg(s3,seqNum,id,filename) ;} 
         
         
       // byte[] a=new byte[2406];
           byte[] a=new byte[126];
      //  setSize(id,length);

        
        a = getByteByString(s3);
        
        
        
        System.out.println("Finish Stuffing");
        
        return a;

    }
    
    
    
      static String confirmMsg(String Data,int index,int id,String filename) 
  {
      String msg = Data;
      int flag=0;
      String s1="Do you want to generate an error in frame number "+index+" on the File : "+filename;
      String s2="Error generation for ID "+id;
             JDialog.setDefaultLookAndFeelDecorated(true);
   int response = JOptionPane.showConfirmDialog(null, s1,s2,

    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
    if (response == JOptionPane.NO_OPTION){}
 
    
    else if (response == JOptionPane.YES_OPTION) 
    {
        for(int i=32;i<Data.length()-16;i++)
        {
            if(msg.charAt(i)=='1')
            {
               
                msg = msg.substring(0,i)+'0'+msg.substring(i+1);              
                flag=1;
                break;
               // flag=1;
                
            }
        }
        
        if(flag==0)
        {
               msg = msg.substring(0,32)+'0'+msg.substring(34); 
        }
        
    } 
    else if (response == JOptionPane.CLOSED_OPTION){}
   
    return msg;
  }
    
    
    
    
    
    
     static  void  ReceiveFile(String msg,Integer size,DataInputStream FromServer,Integer fileSendCount,String FID,int receiverId,String filename) throws IOException
    {
                  System.out.println("here");
                 // System.out.println(msg);
                    ByteArrayOutputStream Buffer = new ByteArrayOutputStream();
                     FileOutputStream OuttoDestination;
                    int filesize = size; // CONVERTING FILESIZE TO INTEGER
                    byte[] array = new byte[size]; // WILL STORE CLIENT DATA ACCORDING TO CHUNK SIZE
                    

                    

      if(filename.substring(filename.length()-4, filename.length()).equalsIgnoreCase(".jpg") ||
          filename.substring(filename.length()-4, filename.length()).equalsIgnoreCase(".png") ||
          filename.substring(filename.length()-4, filename.length()).equalsIgnoreCase(".gif") ||
      filename.substring(filename.length()-4, filename.length()).equalsIgnoreCase(".jpeg")) { }else
      {
                  

                    

                    Buffer.write(msg.getBytes());
              // array=msg.getBytes();
                   byte[] out = Buffer.toByteArray();
                  
                    OuttoDestination = new FileOutputStream("C:\\" + receiverId + "\\Copy" + fileSendCount + filename);
                
                   OuttoDestination.write(out );
                   OuttoDestination.close();
                   Buffer=null;
                   array=null;
                  
                   
                   System.out.println("File has successfully received");
                    
      }    
                    
    }  
    
    
    
    
    public static void main(String argv[]) throws Exception
    {
        String ServerMsg = null,SendMsg,FileId;
        Long MaxChunkSize=0L;
        int receiverId;
        int Filecount=0;
        int fileSize;
        int myId;
        String ID;
        String FID,fileName;
       
       
       // File file=new File("C:\\Users\\Mamun\\Documents\\NetBeansProjects\\Test\\src\\test\\Server.txt");
       // String FileName=file.getName();
       // Long Size=file.length();
         Integer login;
         Socket clientSocket;
         BufferedReader inFromUser;
          DataInputStream FromServer;
          DataOutputStream outToServer;
           BufferedReader inFromServer;
        do
        {
        clientSocket = new Socket("localhost", 6789); //GETTING CONNECTED WITH SERVER
        
       
      // Socket clientSocket = new Socket("192.168.43.20", 6789);
            
            
        
         inFromUser = new BufferedReader(new InputStreamReader(System.in));
          FromServer = new DataInputStream(clientSocket.getInputStream());      
        outToServer = new DataOutputStream(clientSocket.getOutputStream());
        inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        
       

                 
                 String[] options1 = {"Okay"};
               JPanel panel1 = new JPanel();
              JLabel lbl = new JLabel("Enter Your Student ID: ");
              JTextField txt = new JTextField(10);
              panel1.add(lbl);
              panel1.add(txt);
              int selectedOption1 = JOptionPane.showOptionDialog(null, panel1, "File Transmission", JOptionPane.NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options1 , options1[0]);
               ID=txt.getText(); // GETTING USERID
              
               outToServer.writeBytes(ID + '\n'); 
               
               ServerMsg = inFromServer.readLine();
               login=Integer.parseInt(ServerMsg);
               if(login==0)System.out.println("This ID is already logged in");
               else
                   System.out.println("You (ID "+ID+" ) have successfully logged in");
              
        
                   }while(login == 0);
                
                
        while(true)
        {

             ServerMsg = inFromServer.readLine();
             
             if(ServerMsg.equalsIgnoreCase("found"))
            {
               // System.out.println(ServerMsg);
          //  SendMsg="go\\start"; 
           // outToServer.writeBytes(SendMsg + '\n'); 
           // continue;
               Filecount++; 
               ServerMsg = inFromServer.readLine();
               fileSize=Integer.parseInt(ServerMsg);   
                ServerMsg = inFromServer.readLine();
                 FID=ServerMsg;
                 ServerMsg = inFromServer.readLine();
                 myId=Integer.parseInt(ServerMsg);
                  ServerMsg = inFromServer.readLine();
                 fileName=ServerMsg;
                 
                 System.out.println(" Filesize= "+fileSize+" FID= "+FID+" myID= "+myId+" Filename= "+fileName);
                 
String newLine;
ServerMsg="";
while ( true )
{
newLine = inFromServer.readLine(); 

          if (newLine.equalsIgnoreCase("END"))
{
    break;
}
  ServerMsg+=newLine;
  ServerMsg+="\r\n";
}
 // System.out.println(ServerMsg);
                 ReceiveFile(ServerMsg,ServerMsg.length(),FromServer,Filecount,FID,myId,fileName);
                 
    
 
                 
               
            }

             System.out.println(ServerMsg);
           //  SendMsg=inFromUser.readLine(); //GETTING FILE PATH
                 JFileChooser chooser = new JFileChooser();
    File f = new File(new File("filename.txt").getCanonicalPath());
    chooser.setSelectedFile(f);
    chooser.showOpenDialog(null);
    File file = chooser.getSelectedFile();
             
             
             
            // File file=new File(SendMsg);
             String FileName=file.getName();  //GETTING FILENAME
             Long Size=file.length();          //GETTING FILESIZE
             String FileSize=""+Size;
           
            outToServer.writeBytes(FileName + '\n'); //SENDING FILE NAME
            outToServer.writeBytes(FileSize+ '\n');  //SENDING FILE SIZE
            System.out.println("Enter your receiver Id");
            Scanner sc=new Scanner(System.in);
            receiverId=sc.nextInt();
            SendMsg=""+receiverId; 
            outToServer.writeBytes(SendMsg + '\n');  //SENDING RECEIVER'S ID
            
            ServerMsg = inFromServer.readLine();
            
            if(ServerMsg.equalsIgnoreCase("found"))
            {
               // System.out.println(ServerMsg);
          //  SendMsg="go\\start"; 
           // outToServer.writeBytes(SendMsg + '\n'); 
           // continue;
               Filecount++; 
               ServerMsg = inFromServer.readLine();
               fileSize=Integer.parseInt(ServerMsg);   
                ServerMsg = inFromServer.readLine();
                 FID=ServerMsg;
                 ServerMsg = inFromServer.readLine();
                 myId=Integer.parseInt(ServerMsg);
                  ServerMsg = inFromServer.readLine();
                 fileName=ServerMsg;
                 
                 System.out.println(" Filesize= "+fileSize+" FID= "+FID+" myID= "+myId+" Filename= "+fileName);
String newLine;
ServerMsg="";
while ( true )
{
newLine = inFromServer.readLine(); 

          if (newLine.equalsIgnoreCase("END"))
{
    break;
}
  ServerMsg+=newLine;
  ServerMsg+="\r\n";
}
 // System.out.println(ServerMsg);
                 ReceiveFile(ServerMsg,ServerMsg.length(),FromServer,Filecount,FID,myId,fileName);
                 
                 
                 
  
              
               
            }
            
            
            
            if(ServerMsg.equalsIgnoreCase("overflows"))
            {
                System.out.println("Transmission Is not allowed cause Server does not have enough Space to load this file");
                
            }
            else if(ServerMsg.equalsIgnoreCase("okay"))
            {
                System.out.println("You can start sending the file now");
                 ServerMsg = inFromServer.readLine(); //GETTING MAXIMUM CHUNK SIZE FROM SERVER
                 
                if(ServerMsg.equalsIgnoreCase("found"))
            {
             //   System.out.println(ServerMsg);
          //  SendMsg="go\\start"; 
           // outToServer.writeBytes(SendMsg + '\n'); 
           // continue;
               Filecount++; 
               ServerMsg = inFromServer.readLine();
               fileSize=Integer.parseInt(ServerMsg);   
                ServerMsg = inFromServer.readLine();
                 FID=ServerMsg;
                 ServerMsg = inFromServer.readLine();
                 myId=Integer.parseInt(ServerMsg);
                  ServerMsg = inFromServer.readLine();
                 fileName=ServerMsg;
                 
                 System.out.println(" Filesize= "+fileSize+" FID= "+FID+" myID= "+myId+" Filename= "+fileName);
String newLine;
ServerMsg="";
while ( true )
{
newLine = inFromServer.readLine(); 

          if (newLine.equalsIgnoreCase("END"))
{
    break;
}
 ServerMsg+=newLine;
 ServerMsg+="\r\n";
}
  //System.out.println(ServerMsg);
        ReceiveFile(ServerMsg,ServerMsg.length(),FromServer,Filecount,FID,myId,fileName);
        
                
    
                 
               
            }
                 
                 
                 
                 System.out.println("Your Maximum chunk Size is "+ServerMsg);
                 MaxChunkSize=Long.parseLong(ServerMsg); 
                  ServerMsg = inFromServer.readLine(); //GETTING FILE ID FROM SERVER
                  
                  
                    if(ServerMsg.equalsIgnoreCase("found"))
            {
                //System.out.println(ServerMsg);
          //  SendMsg="go\\start"; 
           // outToServer.writeBytes(SendMsg + '\n'); 
           // continue;
               Filecount++; 
               ServerMsg = inFromServer.readLine();
               fileSize=Integer.parseInt(ServerMsg);   
                ServerMsg = inFromServer.readLine();
                 FID=ServerMsg;
                 ServerMsg = inFromServer.readLine();
                 myId=Integer.parseInt(ServerMsg);
                  ServerMsg = inFromServer.readLine();
                 fileName=ServerMsg;
                 
                 System.out.println(" Filesize= "+fileSize+" FID= "+FID+" myID= "+myId+" Filename= "+fileName);
String newLine;
ServerMsg="";
while ( true )
{
newLine = inFromServer.readLine(); 

          if (newLine.equalsIgnoreCase("END"))
{
    break;
}
          ServerMsg+=newLine;
          ServerMsg+="\r\n";
}
  //System.out.println(ServerMsg);
                 ReceiveFile(ServerMsg,ServerMsg.length(),FromServer,Filecount,FID,myId,fileName);
                 
   
            }
                  
                 
                //----------------------------------------------------------------------
                  FileId=ServerMsg; 
                  System.out.println("Your file id is "+FileId);
                  
                //  byte[] Filebytearray = new byte[MaxChunkSize.intValue()]; //READING CHUNKSIZE BYTE FROM FILE 
                  
                int read = 0;
                int i=1;
		int totalRead = 0;
		int remaining = Size.intValue();
                  FileInputStream FileInput = new FileInputStream(file);
                  BufferedInputStream fileBuffer = new BufferedInputStream(FileInput);
                
                
                 // fileBuffer.read(Filebytearray, 0, MaxChunkSize.intValue());
                  // int chunksize=(int)Math.ceil(Size.intValue()*1.0/2000);
                  
                  int chunksize=(int)Math.ceil(Size.intValue()*1.0/100);
                  
                       
                  
                  
                  // byte[] Filebytearray = new byte[2406];
                   byte[] Filebytearray = new byte[126];
                  
                   
                  
                  System.out.println("File size is "+file.length());
                                  
                  
                  
                 // while((read = FileInput.read(Filebytearray, 0,Math.min( remaining,2000))) > 0)
                
           Hashtable<Integer,byte[]> Array = new Hashtable<>();
           Hashtable<Integer,Integer> length = new Hashtable<>();
           int index=1,start=1,end=start+2;
           
             
                 
                     for( i=1;i<=chunksize;i++)
                  {/*
                         
                     // read = FileInput.read(Filebytearray, 0,Math.min( remaining,2000));                 
                    // read = FileInput.read(Filebytearray, 0,Math.min( remaining,100));
                        
                        totalRead += read;
			remaining -= read;
                        //outToServer.write(Filebytearray, 0, read);
                        
                        System.out.println("\n\n##################################read "+read+" bytes");
                         if(index>i)
                         {
                            Array.get(i);
                            read=length.get(i);
                            Filebytearray=bitstuffing(read,Filebytearray,i,Integer.parseInt(ID),FileName);
                         }
                         else
                         {
                             read = FileInput.read(Filebytearray, 0,Math.min( remaining,100));
                             Filebytearray=bitstuffing(read,Filebytearray,i,Integer.parseInt(ID),FileName);
                             Array.put(i, Filebytearray);
                             length.put(i, read);
                         }
                         
                        //outToServer.write(Filebytearray, 0,2406 );
                         outToServer.write(Filebytearray, 0,126 );
                        outToServer.flush();
                        
                            Test obj = new Test();
                            
                          ServerMsg = inFromServer.readLine(); 
                        */
                      
                      //FileTransfer(inFromServer,file,i,i,Filebytearray,outToServer,remaining,FileName,ID);
                       FileTransfer(inFromServer,file,i,end,Filebytearray,outToServer,remaining,FileName,ID);
                      
                         Timeset timeInstance=new Timeset(inFromServer,clientSocket);
                     
                       int K=timeInstance.Invoke(i,end-i+1);
                       System.out.println("#############Acknowledged  upto "+(K-1)+"  frame  ##########################");
                       i=K-1;
                       if(K>end)end=Math.min(K+2,chunksize);
                             
    

                  }
                  
                  System.out.println("Finish Sending");
              
                  
                  
               
                  
                 
                  
                  
                  
                  
                  
         
            ServerMsg = inFromServer.readLine(); //GETTING MESSAGE WHETHER FILE TRANSMISSION IS SUCCESSFUL OR NOT
            
            
            
            
             if(ServerMsg.equalsIgnoreCase("found"))
            {
              //  System.out.println(ServerMsg);
          //  SendMsg="go\\start"; 
           // outToServer.writeBytes(SendMsg + '\n'); 
           // continue;
               Filecount++; 
               ServerMsg = inFromServer.readLine();
               fileSize=Integer.parseInt(ServerMsg);   
                ServerMsg = inFromServer.readLine();
                 FID=ServerMsg;
                 ServerMsg = inFromServer.readLine();
                 myId=Integer.parseInt(ServerMsg);
                  ServerMsg = inFromServer.readLine();
                 fileName=ServerMsg;
                 
                 
                 System.out.println(" Filesize= "+fileSize+" FID= "+FID+" myID= "+myId+" Filename= "+fileName);
String newLine;
ServerMsg="";
while ( true )
{
newLine = inFromServer.readLine(); 

          if (newLine.equalsIgnoreCase("END"))
{
    break;
}
          ServerMsg+=newLine;
          ServerMsg+="\r\n";
}
  //System.out.println(ServerMsg);
                 ReceiveFile(ServerMsg,ServerMsg.length(),FromServer,Filecount,FID,myId,fileName);
                 
                 
                
                 
            }
            
            System.out.println(ServerMsg);
            
     
             
            }
            
            
            /*
                FileInputStream FileInput = new FileInputStream(file);
		byte[] buffer = new byte[Size.intValue()];
		
		while (FileInput.read(buffer) > 0) {
			outToServer.write(buffer);
		}
                    */
            

                
                
                
                
                
             ServerMsg = inFromServer.readLine();
             
             
             
                                                            if(ServerMsg.equalsIgnoreCase("found"))
            {
               // System.out.println(ServerMsg);
          //  SendMsg="go\\start"; 
           // outToServer.writeBytes(SendMsg + '\n'); 
           // continue;
               Filecount++; 
               ServerMsg = inFromServer.readLine();
               fileSize=Integer.parseInt(ServerMsg);   
                ServerMsg = inFromServer.readLine();
                 FID=ServerMsg;
                 ServerMsg = inFromServer.readLine();
                 myId=Integer.parseInt(ServerMsg);
                  ServerMsg = inFromServer.readLine();
                 fileName=ServerMsg;
                 
                 System.out.println(" Filesize= "+fileSize+" FID= "+FID+" myID= "+myId+" Filename= "+fileName);
String newLine;
ServerMsg="";
while ( true )
{
newLine = inFromServer.readLine(); 

          if (newLine.equalsIgnoreCase("END"))
{
    break;
}
          ServerMsg+=newLine;
          ServerMsg+="\r\n";
}
  //System.out.println(ServerMsg);
  

                 ReceiveFile(ServerMsg,ServerMsg.length(),FromServer,Filecount,FID,myId,fileName);
                 
               
                 
                

               
            }
             
             

            
             System.out.println(ServerMsg);
             SendMsg=inFromUser.readLine();
              outToServer.writeBytes(SendMsg + '\n');
               ServerMsg = inFromServer.readLine();
               
               
               
                                                              if(ServerMsg.equalsIgnoreCase("found"))
            {
               // System.out.println(ServerMsg);
          //  SendMsg="go\\start"; 
           // outToServer.writeBytes(SendMsg + '\n'); 
           // continue;
               Filecount++; 
               ServerMsg = inFromServer.readLine();
               fileSize=Integer.parseInt(ServerMsg);   
                ServerMsg = inFromServer.readLine();
                 FID=ServerMsg;
                 ServerMsg = inFromServer.readLine();
                 myId=Integer.parseInt(ServerMsg);
                  ServerMsg = inFromServer.readLine();
                 fileName=ServerMsg;
             
                 
                 System.out.println(" Filesize= "+fileSize+" FID= "+FID+" myID= "+myId+" Filename= "+fileName);
String newLine;
ServerMsg="";
while ( true )
{
newLine = inFromServer.readLine(); 

          if (newLine.equalsIgnoreCase("END"))
{
    break;
}
          ServerMsg+=newLine;
          ServerMsg+="\r\n";
}
 // System.out.println(ServerMsg);
                 ReceiveFile(ServerMsg,ServerMsg.length(),FromServer,Filecount,FID,myId,fileName);
                 
                 

            }
               
               
               System.out.println(ServerMsg);
               if(SendMsg.toLowerCase().equalsIgnoreCase("YES"))
               {
                   
               }
               else
               {
                   clientSocket.close();
                   break;
                   
               }
             
           
            
        }
               
    }
}


 class Timeset
{
     BufferedReader inFromServer;
     Socket clientSocket;
     int start,end,i,val,flag,count;
     long currtime;
     String msg;
    
     
     Timeset(BufferedReader buffer,Socket S)
     {
         this.inFromServer=buffer;
         this.i=0;
         this.clientSocket=S;
         this.flag=1;
         this.val=1;
         currtime=System.currentTimeMillis();
          
     }
     
     int Invoke(int a,int b) throws IOException, InterruptedException
     {
         i=a;
         count=b;
         
           // timeThread t = new timeThread();
           // Thread K = new Thread(t);
           // K.start();
         
         
        // while(System.currentTimeMillis()-currtime<2000)
        // {
         
         //  while(val!=0 &&count!=0)
          //  {
         
                   Timer timer = new Timer();
          timer.schedule(new TimerTask() {

            @Override
            public void run() {
          
                   
                try {
                    msg = inFromServer.readLine();
                } catch (IOException ex) {
                    Logger.getLogger(Timeset.class.getName()).log(Level.SEVERE, null, ex);
                }
            
             val=Integer.parseInt(msg);
             if(val==0)flag=0;
              if(flag==1)i++;
             count--;
  
        
            }
          
           
        }, 0, 1000);
         
          
         long currtime=System.currentTimeMillis();
         while(System.currentTimeMillis()-currtime<4000&&val!=0 &&count!=0 );
         timer.cancel();
      
         //   }
         
        // }
       
                     
                         
                     
           //  System.out.println("-----------------------value of I is -----------------------------"+i);
           
         
         
      return i;   
     }
    
}





