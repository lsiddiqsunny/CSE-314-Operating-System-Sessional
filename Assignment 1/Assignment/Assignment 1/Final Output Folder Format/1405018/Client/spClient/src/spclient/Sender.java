/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spclient;

import NetUtil.ConnectionUtillities;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import static java.lang.Integer.min;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author Farhan
 */
public class Sender implements Runnable
{
    
    private final ConnectionUtillities connection;
    private final String recipientID;
    private final String fileLocation;
    private int chunkSize;
    public Thread thread;
    public static boolean die;
    
    
    public Sender(ConnectionUtillities connection,String recipientID,String fileLocation) throws IOException
    {
        this.connection=connection;
        this.recipientID=recipientID;
        this.fileLocation=fileLocation;
        die=false;
        SpClient.sendLock=true;
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
        
        System.out.println("Total chunk count is: "+totalChunk+" and total Goback N round will be: "+totalRound);

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
                
                System.out.println("packet no from "+head+" to "+tail+" has been transmitted");
                
                while(head<=tail)    {
                    
                    Object oo=connection.read();
                    
                    if(oo==null)    {
                        break;
                    }
                    else    {
                        System.out.println("Server received packet no "+head+" out of "+(totalChunk-1));
                        head++;
                    }
                }
                if(die)    {
                    System.out.println("Client terminates!");
                    return 0;
                }
                
                if(head>tail)    break;
                else    {
                    System.out.println("Server timedout while receiving packet no "+head);
                    
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
    
    private boolean Initialize() throws IOException
    {
        
        SendAck("Sendbegin",0);
        Object oo;
        oo=connection.read();
        if(oo==null)    {
            System.out.println("Server is not responding. Try later");
            return false;
        }
        SendAck(recipientID,1);
        oo=connection.read();
        if(oo==null)    {
            System.out.println("Server is not responding. Try later");
            return false;
        }
        else if(oo.toString().equals("offline"))    {
            System.out.println("Reciever offline");
            return false;
        }
        else if(oo.toString().equals("busy"))   {
            System.out.println("Reciever is busy with some other client, try later!");
            return false;
        }
        File file=new File(fileLocation);
        String fileName=file.getName();
        SendAck(fileName,2);
        oo=connection.read();
        if(oo==null)    {
            System.out.println("Server is not responding. Try later");
            return false;
        }
        else if(oo.toString().equals("ignored"))    {
            System.out.println("Reciever ignored request");
            return false;
        }

        long size=file.length();
        SendAck(Long.toString(size),3);
        oo=connection.read();
        if(oo==null)    {
            System.out.println("Server is not responding. Try later");
            return false;
        }
        else if(oo.toString().equals("large"))    {
            System.out.println("File size too large!");
            return false;
        }
        
        chunkSize=Integer.parseInt(oo.toString());
        System.out.println("Server decided the chunksize to be: "+chunkSize);
        System.out.println("send initiated of file "+fileName+" of size "+size+" bytes");
        
        return true;
    }
    
    
    
    
    @Override
    public void run()
    {
        
        
        try {
            
            if(!Initialize())    {
                SpClient.sendLock=false;
                return;
            } 
            
            
            
            try {
                GobackN();
                if(!die)    System.out.println("File has been successfully copied into Server.");
                Object fn=connection.read();
                if(fn!=null)    {
                    if(fn.toString().equals("sendfailed"))    {
                        System.out.println("Receiver "+recipientID+" has not successfully received the file.");
                    }
                    else    {
                        System.out.println("Receiver "+recipientID+" has successfully received the file.");
                    }
                }
            
            } catch (IOException ex) {
            Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            
            SpClient.sendLock=false;
        } catch (IOException ex) {
            Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    
}
