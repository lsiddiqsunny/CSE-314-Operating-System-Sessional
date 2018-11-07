/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package FileTransmitter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import static java.lang.Integer.min;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author uesr
 */
public class clientReaderWriter implements Runnable{
    public ConnectionUtillities connection;
    public String userName ;
    public clientReaderWriter(String username, ConnectionUtillities con){
        connection=con;
        userName = username ;
    }
    
    @Override
    public void run() {
        
        Scanner in=new Scanner(System.in);
        Object o ;
        String data ;
        //System.out.println("clientreader starts..........");
        o = connection.read() ;
        data = o.toString();
        if(data.matches("login Failed"))
        {
            System.err.println("login is denied by server! BYE!BYE!!");
            return ;
        }
        while(true){
            
            System.out.println("For Sending file , write 'send'" );
            System.out.println("For Logging out, write 'logout'" );
            System.out.println("For Recieving file, write 'receive'" );
            String text ;
            text = "nothning" ;
            
            text = in.nextLine(); 
            if(text.matches("logout"))
            {
                System.err.println("student "+userName+" goes to offline now!!!!");
                connection.write(text);
                o = connection.read() ;
                data = o.toString() ;
                if(data.matches("logout")){break ;}
                else { System.err.println("jhamela :/ :/");}
                return ;
                //System.out.println("login "+ Client.login );
                //break ;
            }
            if( text.matches("send"))
            {
                System.out.println("write the student id of the receiver");
                text = in.nextLine() ;
                connection.write("studentID:"+text);
                o = connection.read() ;
                data = o.toString() ;
                //System.err.println("Server response : "+ data );
                if(data.matches("receiver not Found"))
                {
                    System.err.println("receiver not Found");
                    System.out.println("please type 'OK' to continue");
                }
                else if(data.matches("receiver Found! plz send filename and filesize"))
                {
                    System.out.println("receiver Found");
                    System.out.println("please write the filename.");
                }
                text = in.nextLine() ;
                if(text.matches("OK")){continue ;}
                String fileName = text ;
                File file = new File(fileName);
                while(!file.exists())
                {
                    System.err.println("File does not exists.please write the filename again.");
                    fileName = in.nextLine() ;
                    file = new File(fileName);                   
                }
                long fileSize =  file.length() ;
                //System.out.println("filesize is "+ fileSize );
                connection.write("fileName&Size:"+fileName+":"+fileSize);
                //System.out.println("filenamesize pathylam");
                o = connection.read() ;
                data = o.toString() ;
                //System.out.println("server response : "+ data);
                if(data.matches("Sorry. due to overflow, file transmission can not be allowed"))
                {
                    System.err.println("Sorry. due to overflow, file transmission can not be allowed");
                    continue ;
                }
                else
                {
                   int maxChunkSIze = Integer.parseInt(data) ;
                   //System.out.println("maxChunkSIze is "+ maxChunkSIze);
                   o = connection.read() ;
                   data = o.toString() ;
                   //System.out.println("file ID is "+ data);
                   int fileID = Integer.parseInt(data) ;
                   byte[] bytesArray = new byte[(int) file.length()];
                   FileInputStream fis;
                    try {
                        fis = new FileInputStream(file);
                        fis.read(bytesArray); //read file into bytes[]
                        fis.close();
                        Chunk chunk ;
                        int last = 0 ;
                        byte seqNo = 0x00 ;
                        while(true)
                        {
                            if(last==fileSize){
                                byte [] empty = {0x00};
                                Frame EOTframe = new Frame((byte)0x5,(byte)0xe,empty);
                                byte [] frameByteArray = EOTframe.synthesis() ;
                                System.err.println("EOTframe is sending");
                                connection.write(frameByteArray);
                                break ;
                            }
                            byte [] newByteArray;
                            int chunkSize = (min(maxChunkSIze, (int) (fileSize-last))) ;
                            newByteArray = new byte[chunkSize];
                            for(int i = 0 ; i < chunkSize ; i ++ )
                            {
                                newByteArray[i] = bytesArray[last+i] ;
                            }
                            last += chunkSize ;
                            Frame frame = new Frame((byte)0x5,seqNo,newByteArray) ; 
                            //frame.print();
                            //long startTime = System.nanoTime();
                            byte [] frameByteArray = frame.synthesis() ;
//                            System.err.println("this byte array sending");
//                            frame.printB(frameByteArray);
//                            System.err.println(".................");
                            byte [] msg = null ;
                            int Counterid=0;
                            while(true)
                            {        
                                frame.print();
                                frame.printB(frameByteArray);
                                boolean errorFrame = false ;
                                System.out.println("Do you want to bit manipulate ? ");
                                text = in.nextLine();
                                if(text.equalsIgnoreCase("yes"))
                                {
                                     errorFrame = true ;
              
                                }
                                System.out.println("Do you want to drop the frame ? ");
                                text = in.nextLine();
                                if(text.equalsIgnoreCase("no"))
                                {
                                    System.err.println("frame "+seqNo+ " is sending ");
                                    if(!errorFrame)connection.write(frameByteArray);
                                    else
                                    {
                                        byte [] errorFrameByteArray = Arrays.copyOfRange(frameByteArray, 0, frameByteArray.length) ;
                                        
                                        Random random = new Random();
                                        random.nextBytes(errorFrameByteArray);
                                        System.err.println("original frameByteArray");
                                        frame.printB(frameByteArray);
                                        System.err.println("error frameByteArray");
                                        frame.printB(errorFrameByteArray);
                                        connection.write(errorFrameByteArray);
                                    }
                                }
                                else
                                {
                                    System.err.println("frame "+seqNo+ " is dropped");
                                }
                                
                                try
                                {
                                    //System.err.println("sigh timer");
                                    //msg = (byte[]) timer.get(5,TimeUnit.SECONDS);
                                    o = connection.read();
                                    msg = (byte[]) o;
                                    //System.err.println("sigh msg pelam");
                                    //frame.printB(msg);
                                    // System.err.println("sigh msg..........");
                                    frame = new Frame(msg);
                                    if(frame.isPositive())
                                    {
                                        System.err.println("frame has been successfully sent, new frame will be sending again");
                                        seqNo = (byte) (1 - seqNo) ;
                                        break ;                                                                          
                                    }
                                    else
                                    {
                                        System.err.println("frame got corrupted, same frame will be sending again");
                                    }
                                    
                                }catch( Exception e)
                                {
                                     System.err.println("timeout, same frame will be sending again");
                                }
                            }
                            
                        }
                        //System.err.println("file pathaono ses");
                        data = (String) connection.read() ;
                        System.out.println(data);
                        
                        //while(true);
                        //Chunk chunk = new Chunk(fileID,bytesArray) ;
                        //System.out.println("chunk is :  ");
                        //chunk.print();
                        //connection.write(chunk) ;
                    } catch (FileNotFoundException ex) {
                        //Logger.getLogger(clientReaderWriter.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                       // Logger.getLogger(clientReaderWriter.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                   
                }
            }
            else if(text.contains("receive"))
            {
                System.out.println("You are in now receiver mode!");
                while(true)
                {
                    try
                    {
                         data = (String) connection.read() ;
                         
                         if(data != null )break ;
                    }catch(Exception e)
                    {
                         //System.err.println("null ponter");
                    }
                           
                }
               
                System.out.println("new file is coming "+data) ;
                String msg[]=data.split(":",4);
                
                System.out.println("Student "+msg[0]+ " send a file named "+msg[1] + " of size "+msg[2]+" bytes");
                System.out.println("Do you want to download it ?");
                text = in.nextLine() ;
                //System.out.println("client response "+ text);
                
                String fileName = msg[1] ;
                if(text.matches("yes"))
                {
                    connection.write(text+",I want to receive file") ;
                    connection.write(data);
                    //System.out.println("file rcv korte chai..........s");
                    byte bytesArray[] = (byte[]) connection.read();
                    FileOutputStream fos;
                    try {
                        fos = new FileOutputStream(userName+fileName);
                        fos.write(bytesArray);
                        fos.close();
                        System.err.println(fileName+" file download is complete!");
                    } catch (FileNotFoundException ex) {
                        //Logger.getLogger(ServerReaderWriter.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                       //Logger.getLogger(ServerReaderWriter.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                }
                else 
                {
                    connection.write(text+",I don't want to receive file") ;
                    connection.write(data);
                }
            }
            else
            {
                System.err.println("Invalid keyword!");
            }
            
        }
        //System.out.println("clientreader stops..........");
        
    }
    
//    public byte [] checkTimeOut(int id )
//    {
//        byte msg[] = null ;
//        try {
//            ExecutorService executorService = Executors.newCachedThreadPool();
//            Callable<Object> task;
//            
//            task = new Callable<Object>(){
//                public Object call()
//                {
//                    byte [] ack = null ;
//                    try
//                    {
//                        ack = (byte[]) connection.read();
//                        
//                       
//                        if(ack==null){System.err.println("null ack :( :( ");}
//                        printB(ack);
//                        System.err.println("ack pylam");
//                        
//                    }
//                    catch(Exception e)
//                    {
//                        System.err.println("exception in timer :/ :/ ");
//                    }
//                    return ack;
//                }
//            };
//            try{
//                Future<Object> timer = executorService.submit(task);
//            
//                System.err.println("id "+ id );
//                msg = (byte[]) timer.get(5,TimeUnit.SECONDS);
//                System.err.println("ack pylam id "+ id );
//                
//                
//            } catch (ExecutionException ex) {
//                //Logger.getLogger(clientReaderWriter.class.getName()).log(Level.SEVERE, null, ex);
//            }
//            } catch (InterruptedException ex) {
//                //Logger.getLogger(clientReaderWriter.class.getName()).log(Level.SEVERE, null, ex);
//            }catch (TimeoutException ex) {
//                System.err.println("time out again in func");
//                    ///Logger.getLogger(clientReaderWriter.class.getName()).log(Level.SEVERE, null, ex);
//            }
//            
//        
//        return msg ;
//    }
    public void printB( byte [] byteArray)
    {
        for(int i=0;i < byteArray.length ; i++ )print(byteArray[i]);
        System.err.println("");
    }
    public void print( byte byt)
    {
        for(int i = 7 ; i > -1 ; i--)
        {
            if((byt&(1<<i))==0) { System.err.print("0");
            } else {
                System.err.print("1");
             }
        }
        //System.err.println("");
    }
    
}
