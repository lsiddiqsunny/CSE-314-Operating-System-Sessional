/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spserver;

import NetUtil.ConnectionUtillities;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import static java.lang.Integer.min;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;




/**
 *
 * @author Farhan
 */
public class Receiver implements Runnable
{
    private final ConnectionUtillities connection;
    public String ID; 
    public String fileLocation;
    private int chunkSize;
    public Thread thread;
    
    
    public Receiver(ConnectionUtillities connection,String ID)
    {
        
        this.connection=connection;
        this.ID=ID;
        thread=new Thread(this);
        thread.start();
        
    }
    
    
    
    public byte[] serialize(Object obj) throws IOException 
    {
        try(ByteArrayOutputStream b = new ByteArrayOutputStream()){
            try(ObjectOutputStream o = new ObjectOutputStream(b)){
                o.writeObject(obj);
            }
            return b.toByteArray();
        }
    }
    
    private void send(byte[] frame) throws IOException
    {
        byte[] ser=serialize(frame);
        connection.write(ser);
    }
    
    public int GobackN() throws IOException
    {
        int N=8;
        byte[] fileData=Files.readAllBytes(Paths.get(fileLocation));
        
        int totalChunk;
        if(fileData.length%chunkSize==0)    totalChunk=fileData.length/chunkSize;
        else    totalChunk=fileData.length/chunkSize+1;
        int totalRound;
        if(totalChunk%N==0)    totalRound=totalChunk/N;
        else    totalRound=totalChunk/N+1;
        
        System.out.println("Total chunk count is: "+totalChunk+" and total Goback N round will be: "+totalRound+" for ID:"+ID);

        for(int i=0;i<totalRound;i++)    {
            int head=i*N+0;
            int tail=min(head+N-1,totalChunk-1);
            while(true)    {
                for(int k=head;k<=tail;k++)    {
                    int len=min(chunkSize,fileData.length-k*chunkSize);
                    byte[] toStuff=new byte[len];
                    System.arraycopy(fileData,k*chunkSize,toStuff,0,len);
                    BitStuffer bitStuffer=new BitStuffer(toStuff,1,k);
                    byte[] stuffed=bitStuffer.stuff();
                    send(stuffed); 
                }
                
                System.out.println("packet no from "+head+" to "+tail+" has been transmitted"+" for ID:"+ID);
                
                while(head<=tail)    {
                    
                    Object oo=connection.read();
                    
                    if(oo==null)    {
                        break;
                    }
                    else    {
                        System.out.println("Receiver "+ID+" received packet no "+head+" out of "+(totalChunk-1));
                        head++;
                    }
                }
                
                if(head>tail)    break;
                else    {
                    
                    if(Initialize.IPMap.containsKey(ID))    System.out.println("Receiver "+ID+" timedout while receiving packet no "+head);
                    else    {
                        return -1;
                    }
                    
                    
                }
            }
        }
        
        
        
        return 0;
    }
    
    private void SendAck(String ack,int seq) throws IOException
    {
        byte[] toStuff=ack.getBytes();
        BitStuffer bitStuffer=new BitStuffer(toStuff,0,seq);
        byte[] stuffed=bitStuffer.stuff();
        send(stuffed);
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
        while(true)    {
            
            if(!Initialize.IPMap.containsKey(ID))    break;
            
            try {
                Thread.sleep(1500);
            } catch (InterruptedException ex) {
                Logger.getLogger(Receiver.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            if(Initialize.receiverFile.containsKey(ID))    {
                
                try {
                    String sender=Initialize.receiverFile.get(ID).get(0);
                    String fileName=Initialize.receiverFile.get(ID).get(1);
                    
                    SendAck(sender,0);
                    SendAck(fileName,1);
                    
                    Object oo=connection.read();
                    
                    if(oo==null || oo.toString().equals("ignored"))    {
                        Initialize.receiverFile.remove(ID);
                        System.out.println("Receiver "+ID+" igonored file "+fileName);
                    }
                    else    {
                        Initialize.receiverBusy.put(ID, Boolean.TRUE);
                        fileLocation="src/storedFiles/"+sender+"_to_"+ID+"_"+fileName;
                        File file=new File(fileLocation);
                        
                        
                        while(!file.exists())    {
                            if(!Initialize.IPMap.containsKey(sender))    break;
                        }
                        
                        if(file.exists())    {
                            SendAck("upload complete",2);
                            chunkSize=ChunksizeGen((int)file.length());
                            System.out.println("Server decides the chunksize for sending to ID: "+ID+" to be "+chunkSize);
                            SendAck(Integer.toString((int)file.length()),3);
                            SendAck(Integer.toString(chunkSize),4);
                            
                            System.out.println("Receiver "+ID+" starts receiving.");
                            connection.sc.setSoTimeout(30000);
                            int ver=GobackN();
                            
                            if(ver==0)    {
                                file.delete();
                                SpServer.setOccupiedCapacity(SpServer.getOccupiedCapacity()-(int)file.length());
                            }
                            
                            Initialize.receiverBusy.put(ID, Boolean.FALSE);
                            Initialize.receiverFile.remove(ID);
                            connection.sc.setSoTimeout(0);
                        }
                        else    {
                            SendAck("Upload incomplete",2);
                            System.out.println("Receiver "+ID+" can't start receiving.");
                            Initialize.receiverBusy.put(ID, Boolean.FALSE);
                            Initialize.receiverFile.remove(ID);
                        }
                        
                    }
                    
                    
                    
                    
                    
                } catch (IOException ex) {
                    Logger.getLogger(Receiver.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                
                
                
            }
        }
        
        
        
    }
    
    
    
    
    
}
