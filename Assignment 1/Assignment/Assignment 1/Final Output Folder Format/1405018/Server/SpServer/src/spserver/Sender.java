/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spserver;

import NetUtil.ConnectionUtillities;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import static java.lang.Integer.min;
import static java.lang.Math.sqrt;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;



/**
 *
 * @author Farhan
 */
public class Sender implements Runnable
{
    private final ConnectionUtillities connection;
    public String ID;
    public Thread thread;
    private int chunkSize;
    private byte[] stream;
    private String receiverID;
    private int fileSize;
    private int receivedSize;
    private String fileName;
    
    public Sender(ConnectionUtillities connection,String ID)
    {
        this.connection=connection;
        this.ID=ID;
        thread=new Thread(this);
        thread.start();
    }
    
    public Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException
    {
        try(ByteArrayInputStream b = new ByteArrayInputStream(bytes)){
            try(ObjectInputStream o = new ObjectInputStream(b)){
                return o.readObject();
            }
        }
    }
    
    
    private int LostGen(int x)
    {
        Random rand=new Random();
        int r=rand.nextInt(x);
        return r;
    }
    
    private byte[] read() throws IOException, ClassNotFoundException
    {
        Object oo=connection.read();
        if(oo==null)    return null;
        
        byte[] in=(byte[])deserialize((byte[])oo);
        
        //HOLLOW ERROR INTRODUCING
        int errorChance;
        int lostFrame;
        
        if(chunkSize!=0)    {
            int totalChunk;
            if(fileSize%chunkSize==0)    totalChunk=fileSize/chunkSize;
            else    totalChunk=fileSize/chunkSize+1;
            errorChance=LostGen((int)sqrt(totalChunk)+50);
        }
        else    errorChance=1;
        if(errorChance==0)    {
            in[4]^=1;
        }
        /////////////////////////
        
        
        return in;
    }
    
    
    
    
    private int ChunksizeGen(int size)
    {
        int ret;
        if(size<4)    ret=size;
        else ret=min(15,size/4);
        Random rand = new Random();
        int rret= rand.nextInt(ret)+1;
        if(ret==15 && rret<10)    rret=10;
        chunkSize=rret;
        return rret;
    }
    
    
    @Override
    public void run()
    {
        int gobacknlastseq=0;
        while(true)    {
            try {
                byte[] in=read();
                if(in==null)    {
                    System.out.println("Client "+ID+" has left!");
                    SpServer.setOccupiedCapacity(SpServer.getOccupiedCapacity()-fileSize);
                    Initialize.IPMap.remove(ID);
                    break;
                }
                
                DeStuffer destuffer=new DeStuffer(in);
                byte[] destuffed=destuffer.deStuff();
                int type=destuffer.getType();
                int seqNo=destuffer.getSeqNo();
                
                
                //HOLLOW ERROR INTRODUCING
                int lostFrame;
                
                if(chunkSize!=0)    {
                    int totalChunk;
                    if(fileSize%chunkSize==0)    totalChunk=fileSize/chunkSize;
                    else    totalChunk=fileSize/chunkSize+1;
                    lostFrame=LostGen((int)sqrt(totalChunk)+75);
                }
                else   lostFrame=1;
                if(lostFrame==0 && type==1)    {
                    seqNo=-1;
                }
                /////////////////////////
                
                
                if(!destuffer.isValid)    System.out.println("Data corrupted from ID "+ID+" at packet no "+gobacknlastseq+" of type "+type);
                
                else if(type==0)    {
                    String request=new String(destuffed);
                    
                    switch (seqNo) {
                        case 0:
                            connection.write("ok");
                            break;
                        case 1:
                            receiverID=new String(destuffed);
                            if(!Initialize.IPMap.containsKey(receiverID))    {
                                System.out.println("Receiver "+receiverID+" is offline");
                                connection.write("offline");
                            }
                            else if(Initialize.receiverBusy.get(receiverID))    {
                                System.out.println("Receiver "+receiverID+" is busy");
                                connection.write("busy");
                            }
                            else    connection.write("ok");
                            //connection.write("ok");
                            break;
                        case 2:
                            fileName=new String(destuffed);
                            ArrayList<String>details=new ArrayList<>();
                            details.add(ID);
                            details.add(fileName);
                            Initialize.receiverFile.put(receiverID,details);
                            
                            
                            
                            while(!Initialize.receiverBusy.get(receiverID))    {
                                if(!Initialize.receiverFile.containsKey(receiverID))    break;
                            }
                            
                            if(Initialize.receiverBusy.get(receiverID))    {
                                connection.write("ok");
                            }
                            else    {
                                connection.write("ignored");
                            }
                            
                            //connection.write("ok");
                            break;
                        case 3:
                            fileSize=Integer.parseInt(request);
                            if(SpServer.getOccupiedCapacity()+fileSize<=SpServer.getMaxChunkCapacity())    {
                                connection.write(ChunksizeGen(fileSize));
                                SpServer.setOccupiedCapacity(SpServer.getOccupiedCapacity()+fileSize);
                                gobacknlastseq=0;
                                stream=new byte[fileSize];
                                receivedSize=0;
                                System.out.println("File transfer start from ID "+ID);
                            }
                            else    connection.write("large");
                            break;
                        default:
                            break;
                    }
                    
                }
                
                else if(type==1)    {
                          
                    if(seqNo!=gobacknlastseq)    {
                        System.out.println("Packet no "+gobacknlastseq+" lost from ID "+ID);
                    }
                    else    {
                        System.arraycopy(destuffed,0,stream,seqNo*chunkSize,destuffed.length);
                        System.out.println("Packet no "+seqNo+" received from ID "+ID);
                        
                        //////////Pseudo delay Introduced
                        long pauseTime=(long)LostGen(10000);
                        if(pauseTime<9900)    pauseTime=0;
                        Thread.sleep(pauseTime);
                        ////////////////////////////////
                        
                        connection.write("received"+seqNo);
                        receivedSize+=destuffed.length;
                        gobacknlastseq++;
                        if(receivedSize==fileSize)    { 
                            try (FileOutputStream fos=new FileOutputStream("src/storedFiles/"+ID+"_to_"+receiverID+"_"+fileName)) {
                                fos.write(stream);
                            }
                            System.out.println("File from "+ID+" received and copied in server");
                            File tmpFile=new File("src/storedFiles/"+ID+"_to_"+receiverID+"_"+fileName);
                            
                            while(tmpFile.exists())    {
                                if(!Initialize.receiverBusy.get(receiverID))    break;    
                            }
                            
                            if(tmpFile.exists())    {
                                System.out.println("Client "+receiverID+" didn't successfully receive file form "+ID);
                                tmpFile.delete();
                                SpServer.setOccupiedCapacity(SpServer.getOccupiedCapacity()-(int)tmpFile.length());
                                if(!connection.sc.isClosed())    connection.write("sendfailed");
                            }
                            
                            else    {
                                System.out.println("Client "+receiverID+" successfully received file form "+ID);
                                if(!connection.sc.isClosed())    connection.write("sendsuccess");
                            }
                            
                            
                            receivedSize=0;
                            fileSize=0;
                            gobacknlastseq=0;
                        }
                        
                    }
                }
                
                else    {
                    System.out.println("Wrong data from ID: "+ID);
                }
                
            } catch (IOException | ClassNotFoundException | InterruptedException ex) {
                Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            
            
        }
        
        
    }
}
