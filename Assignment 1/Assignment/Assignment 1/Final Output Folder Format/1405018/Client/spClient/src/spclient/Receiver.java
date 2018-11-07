/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spclient;

import NetUtil.ConnectionUtillities;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import static java.lang.Math.sqrt;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Farhan
 */
public class Receiver implements Runnable
{
    private final ConnectionUtillities connection;
    public Thread thread;
    public static boolean die;
    private String senderID;
    private String fileName;
    private String saveLocation;
    private int fileSize;
    private int receivedSize;
    private int chunkSize;
    private byte[] stream;
    public static int receiveCmd;
    
    public Receiver(ConnectionUtillities con)
    {
        connection=con;
        die=false;
        receiveCmd=0;
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
            errorChance=LostGen((int)sqrt(totalChunk)+35);
        }
        else    errorChance=1;
        if(errorChance==0)    {
            in[4]^=1;
        }
        /////////////////////////
        
        
        return in;
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    @Override
    public void run()
    {
        int gobacknlastseq=0;
        while(true)    {
            if(die)    break;
            try {
                byte[] in=read();
                
                if(in==null)    {
                    //System.out.println("Server is dead :(");
                }
                
                
                
                else    {
                    
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
                    
                    if(!destuffer.isValid)    System.out.println("Data corrupted at packet no "+gobacknlastseq+" of type "+type);
                
                    else if(type==0)    {
                        String request=new String(destuffed);

                        switch (seqNo) {
                            case 0:
                                senderID=new String(destuffed);   
                                break;
                            case 1:
                                fileName=new String(destuffed);
                                Scanner scanner=new Scanner(System.in);
                                System.out.println("Do you want to receive "+fileName+" from "+senderID+"?\n"+"Press R to receive: Press I to ignore");
                                
                                
                                while(receiveCmd==0)    {
                                    try {
                                        Thread.sleep(500);
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(Receiver.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                                
                                
                                if(receiveCmd==1)    {
                                    connection.write("accepted");
                                    saveLocation="src/receivedFiles/"+senderID+"_"+fileName;
                                    System.out.println("Sender is uploading the file in the server....");
                                }
                                else    {
                                    connection.write("ignored");
                                }
                                break;
                            case 2:
                                String uploadCom=new String(destuffed);
                                if(uploadCom.equals("upload complete"))    {
                                    System.out.println("Sender completed uploading the file in the server.");
                                }
                                else    {
                                    System.out.println("Sender couldn't complete uploading the file in the server: Receive failed.");
                                }
                                break;
                            case 3:
                                fileSize=Integer.parseInt(new String(destuffed));
                                break;
                            case 4:
                                chunkSize=Integer.parseInt(new String(destuffed));
                                stream=new byte[fileSize];
                                receivedSize=0;
                                gobacknlastseq=0;
                                break;
                            default:
                                break;
                        }
                    
                    }
                    
                    else if(type==1)    {
                        
                        if(seqNo!=gobacknlastseq)    {
                            System.out.println("Packet no "+gobacknlastseq+" lost");
                        }
                        else    {
                            System.arraycopy(destuffed,0,stream,seqNo*chunkSize,destuffed.length);
                            System.out.println("Packet no "+seqNo+" received");

                            //////////Pseudo delay Introduced
                            long pauseTime=(long)LostGen(10000);
                            if(pauseTime<9900)    pauseTime=0;
                            Thread.sleep(pauseTime);
                            ////////////////////////////////

                            connection.write("received"+seqNo);
                            receivedSize+=destuffed.length;
                            gobacknlastseq++;
                            if(receivedSize==fileSize)    { 
                                try (FileOutputStream fos=new FileOutputStream(saveLocation)) {
                                    fos.write(stream);
                                }
                                System.out.println("File from "+senderID+" received and copied successfully!");
                                receivedSize=0;
                                fileSize=0;
                                gobacknlastseq=0;
                            }
                        
                        }
                    }         
                }
                
                
            } catch (IOException | ClassNotFoundException | InterruptedException ex) {
                Logger.getLogger(Receiver.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
        
        
    }
}
