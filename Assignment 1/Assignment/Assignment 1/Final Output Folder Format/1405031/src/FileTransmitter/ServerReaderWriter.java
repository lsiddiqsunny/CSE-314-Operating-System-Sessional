/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package FileTransmitter;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author uesr
 */
public class ServerReaderWriter implements Runnable{

    public HashMap<String,ClientInfo> clientList;
    public ConnectionUtillities connection;
    public String user;
    public boolean flag ;
    public ServerReaderWriter(String username,ConnectionUtillities con, HashMap<String,ClientInfo> list){
        connection=con;
        clientList=list;
        user=username;
    }
    
    @Override
    public synchronized void run() {
        int errorFileID = -1 ;
        try
        {
            while(true){

                Object o=connection.read();
                String data=o.toString();
                //System.out.println("user "+ user + " & "+ data ) ;
                if(data.matches("yes,I want to receive file"))
                {
                    //System.out.println("client response 1 : " + data);
                    
                    o=connection.read();
                    data=o.toString(); 
                    //System.out.println("client response 2 : " + data);
                    String msg[] = data.split(":",4);
                    int fileID = Integer.parseInt(msg[3]);
                    errorFileID = fileID ;
                    int fileSize = Integer.parseInt(msg[2]) ;
                    byte [] bytesArray = new byte[fileSize] ; 
                    int chunksSize = Server.chunks.size() ;
                    //System.out.println("chuksSize = " + chunksSize);
                    int last = 0 ;
                    for(int i = 0; i < chunksSize ; i++)
                    {
                        Chunk chunk = (Chunk) Server.chunks.get(i) ;
                        if(chunk.fileID==fileID)
                        {
                            int chunkSize = chunk.bytesArray.length ;
                            for(int j = 0 ; j < chunkSize ; j++)
                            {
                                bytesArray[last+j] = chunk.bytesArray[j] ;
                            }
                            last += chunkSize ;
                        }

                    }
                    connection.write(bytesArray);
                    Server.removeChunks(fileID) ;
                    //Server.curSize -= fileSize ;
                    continue ;
                }
                else if(data.matches("no,I don't want to receive file"))
                {
                    o=connection.read();
                    data=o.toString(); 
                    String msg[] = data.split(":",4);
                    int fileID = Integer.parseInt(msg[3]);
                    int fileSize = Integer.parseInt(msg[2]) ;
                    //Server.curSize -= fileSize ;
                    Server.removeChunks(fileID);              
                    continue ;
                }

                if(data.matches("logout"))
                {
                    connection.write(data);
                    clientList.remove(user) ;
                    System.out.println("student "+user+" goes to offline now!");
                    return ;
                }
                String msg[]=data.split(":",2);

                String key   = msg[0];
                String value = msg[1];
                String studentID = value ;
                if(key.matches("studentID"))
                {
                    String username = value ;
                    studentID = value ;
                    //System.out.println("studentid is "+ studentID);

                    if( clientList.containsKey(username))
                    {
                        connection.write("receiver Found! plz send filename and filesize");
                    }
                    else 
                    {
                        connection.write("receiver not Found");
                        continue ;
                    }
                    data = (String) connection.read() ;
                    msg = data.split(":",2);
                    key = msg[0] ;
                    value = msg[1] ;
                    if(key.matches("fileName&Size"))
                    {
                        msg = value.split(":",2);
                        String fileName = msg[0] ;
                        int fileSize =  Integer.parseInt(msg[1]) ;
                        //System.out.println("filename = "+ fileName + " & fileSize "+ fileSize ) ;
                        //byte[] bytesArray = new byte[fileSize] ;
                        int last = 0;
                        if(fileSize+Server.curSize>Server.maxSize)
                        {
                            connection.write("Sorry. due to overflow, file transmission can not be allowed");
                        }
                        else
                        {
                            //Server.curSize += fileSize ;
                            Random rand = new Random() ;
                            int maxChunkSize = rand.nextInt(5)+(fileSize)/5+1 ;
                            int fileID = Server.curFileID ;
                            errorFileID = fileID ;
                            Server.curFileID++ ;
                            //System.out.println("maxChunkSIze is "+ maxChunkSize);
                            connection.write(maxChunkSize);
                            connection.write(fileID);
                            int cnt = 0 ;
                            byte ackNo = 0 ;
                            byte [] negative = { 0x00 } ;
                            byte [] positive = { ~0   } ;
                            while(true)
                            {
                                
                                byte [] frameByteArray =  (byte [] ) connection.read() ;
                                
                                
                                System.err.println("this byte array received");
                                printB(frameByteArray);
                                System.err.println(".................");
                                
                                Frame frame = new Frame(frameByteArray);
                                frameByteArray=Arrays.copyOfRange(frameByteArray, 1, frameByteArray.length-1);
                                if(frame.hasError(frameByteArray))
                                {                    
                                        System.err.println("error found");
                                        Frame negAck = new Frame((byte)0xA, (byte) ackNo,negative ) ; 
                                        byte [] negAckArray = negAck.synthesis() ;
                                        System.err.println(ackNo+"Frame is discarded.\n send old frame ");
                                        connection.write(negAckArray);
                                        continue ;
                                }
                                if(frame.isEOTframe())
                                {
                                    System.err.println("EOTframe has been received.");
                                    break ;
                                }
                                //frame.print();
                                if(frame.seqOrAckno==ackNo)
                                {                
                                    ackNo = (byte) (1 - ackNo) ;
                                    
                                    System.err.println("send new frame "+ackNo );
                                    Frame posAck = new Frame((byte)0xA, (byte) ackNo,positive ) ;
                                    byte [] posAckArray = posAck.synthesis() ;
                                    //printB(posAckArray);                                    
                                    connection.write(posAckArray);
                                }
                                else 
                                {
                                    System.err.println("send new frame "+ackNo);
                                    Frame posAck = new Frame((byte)0xA, (byte) ackNo,positive ) ;
                                    byte [] posAckArray = posAck.synthesis() ;
                                    //printB(posAckArray);
                                    connection.write(posAckArray);
                                }
                                //frame.print();
                                Chunk chunk = new Chunk(fileID,frame.payload);
                                Server.chunks.add(chunk);
                                Server.curSize += chunk.bytesArray.length ;
                                cnt++ ;
                                int chunkSize = chunk.bytesArray.length;
                                
                                last += chunkSize ;
                                //System.err.println("last = "+ last );
                                
                            }
                            System.err.println("file paoa ses" ) ;
                            if( last == fileSize )
                            {
                                connection.write("file transmission successfully completed");
                                errorFileID = -1 ;
                            }
                            else 
                            {
                                data = "" ;
                                if(last==-1)data = " due to TIME-OUT" ;
                                
                                
                                connection.write("sorry. file transmission is failed"+data);
                                Server.removeChunks(fileID);
                                continue ;
                            }

                            //System.out.println("full file paoa ses");

                            //System.out.print("bytesarrays is "+ Arrays.toString(bytesArray));

        //                    FileOutputStream fos;
        //                    try {
        //                        fos = new FileOutputStream("server"+fileName);
        //                        fos.write(bytesArray);
        //                        fos.close();
        //                    } catch (FileNotFoundException ex) {
        //                        //Logger.getLogger(ServerReaderWriter.class.getName()).log(Level.SEVERE, null, ex);
        //                    } catch (IOException ex) {
        //                        //Logger.getLogger(ServerReaderWriter.class.getName()).log(Level.SEVERE, null, ex);
        //                    }
                            //System.out.println("studentID is "+ studentID );
                            if(clientList.containsKey(studentID))
                            {
                                ClientInfo clientInfo = clientList.get(studentID) ;
                                //System.out.println("info ->> "+ info.username);
                                clientInfo.connection.write(user+":"+fileName+":"+fileSize+":"+fileID);
                                //System.out.println("info -> "+info.username);
                                //o = info.connection.read() ;
                                //data = o.toString();
                                //System.out.println("client response " + data);

                            }
                            else 
                            {
                                Server.removeChunks(fileID) ;
                            }


                            //System.out.println("chunk is :  ");
                            //chunk.print();
                            //System.out.println("chunk fileid is "+chunk.fileID);

                        }   

                    }
                }
    //            if(clientList.containsKey(username)){
    //                ClientInfo info=clientList.get(username);
    //                info.connection.write(user+" - "+msgInfo);
    //            }
    //            else{
    //               connection.write(username+" not found ");
    //            }

            }
        }
        catch( Exception e)
        {
            System.out.println("student "+user+" goes to offline now!");
            clientList.remove(user) ;
            Server.removeChunks(errorFileID);
        }
    }
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
