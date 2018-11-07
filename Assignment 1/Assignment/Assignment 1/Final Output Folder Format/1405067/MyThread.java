
package file.transmission;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;


public class MyThread extends Thread{
    private FileTransmissionServer server=null;
    int threadId=0;
    Client c = null;
    Socket socket=null;
    ObjectOutputStream os=null;
    ObjectInputStream is=null;
    
    MyThread(FileTransmissionServer server,Socket socket)
    {
        this.server = server;
        this.socket = socket;
    }
    
    @Override
    public void run()
    {
        try {
            
            os = new ObjectOutputStream(socket.getOutputStream());
            is = new ObjectInputStream(socket.getInputStream());
            
            threadId = (int) is.readObject();
            checkID(threadId);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while(true)
                    {
                        if( ! c.incomingFile.isEmpty() )
                        {
                            try {
                                    os.writeObject(new String("incoming file request interrupt"));   
                                    while(! c.incomingFile.isEmpty())
                                    {
                                    
                                    }
                            } catch (IOException ex) {
                                Logger.getLogger(MyThread.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                }
            }).start();
            
            while(socket.isConnected())
            {
                 String code = (String)checkLogOut();
                 if(code.equalsIgnoreCase("download request"))
                {
                    sendFileRequest();
                }
                else if(code.equalsIgnoreCase("send request"))
                {
                    Vector<Object> vec=(Vector<Object>)checkLogOut();
                    String fileName = (String)vec.get(0);
                    System.out.println("file :"+fileName);
                    int fileSize = (int) vec.get(1);
                    System.out.println("size :"+fileSize);
                    int receiverId = (int) vec.get(2);
                    System.out.println("receiver :"+receiverId);

                    Client c = (Client) server.clients.get(receiverId);
                    if(!server.checkOverFlow(fileSize))
                    {
                        os.writeObject(new Boolean(false));
                        os.writeObject(new String("server capacity Exceeds"));
                    }
                    else if( c==null || !c.isOnline)
                    {
                        os.writeObject(new Boolean(false));
                        os.writeObject(new String("Receiver is offline"));
                    }
                    else
                    {
                        os.writeObject(new Boolean(true));
                        int fileId=server.fileCounter++;
                        int chunkSize = random_number(fileSize);
                        os.writeObject(chunkSize);
                        os.writeObject(fileId);
                        int numOfChunks=(int) Math.ceil((double)fileSize/(double)chunkSize);
                        System.out.println("Num of chunks : "+numOfChunks);
                        readChunks(numOfChunks,fileName,fileSize,fileId,receiverId);
                    }
                }

            }
                
        } catch (IOException ex) {
            Logger.getLogger(MyThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(MyThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(MyThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    int random_number(int size)
    {
        return (int) (Math.random()*500+size/120);
    }

    private void readChunks(int numOfChunks,String fileName,int fileSize,int fileId,int to) throws IOException, ClassNotFoundException, InterruptedException {
        
        MyChunkedFile file=new MyChunkedFile(fileId,fileName,fileSize,threadId,to );
        DLL d = new DLL();
        int received_byte = 0;
        int acknowledgeSeq=0;
        int errorSeq=0;
        int seq=0;
        while( seq < numOfChunks)
        {
            String s = (String)checkLogOut();
            if(d.checkError(s))
            {
                seq = d.getSeqNo();
                if((errorSeq == 0 || errorSeq == seq) && acknowledgeSeq < seq )
                {
                    byte[] data = d.getPayload();
                    received_byte += data.length;
                    String acknowledge = d.makeFrame(d.acknowledge,(byte)seq,null);
                    acknowledgeSeq=seq;
                    os.writeObject(acknowledge);
                    System.out.println("Chunk received "+seq+" out of "+numOfChunks);
                    Thread.sleep(1000);
                    file.addChunks(data);
                    if(errorSeq == seq)errorSeq = 0;
                }

            }
            else if(errorSeq == 0)
            {
                seq = d.getSeqNo();
                System.out.println("      *****      Error occured in Chunk : "+seq);
                errorSeq = seq;
                seq = 0;
            }

        }
        Client c=(Client) server.clients.get(to);
        if(c==null)
        {
            c= new Client(to,false);
            server.clients.put(to,c);
        }
        if(received_byte == fileSize)
        {
            os.writeObject(new String("File : "+fileName+" transmitted Completely"));
            System.out.println("File : "+fileName+" transmitted Completely");
            c.addToIncoming(file);
            server.availableBuffer -= fileSize;
        }
        else
        {
            System.out.println(received_byte);
            os.writeObject(new String("File : "+fileName+" transmission Discarded"));
        }
    }

    private void checkID(int clientId) throws IOException, ClassNotFoundException {
        c =(Client) server.clients.get(clientId);
        if(c != null)
        {
            if(c.isOnline)
            {
                os.writeObject(new Integer(-1));
                System.out.println("online");
                socket.close();
                Thread.currentThread().stop();
            }
            else
            {
                c.isOnline = true;
                os.writeObject(new Integer(1));
                System.out.println("offline");
                sendFileRequest();

            }
        }
        else
        {
            c= new Client(clientId,true);
            server.clients.put(clientId,c);
            os.writeObject(new Integer(0));
        }
    }
    
    private Object checkLogOut() throws IOException, ClassNotFoundException
    {
        Object obj = is.readObject();
        if(obj instanceof String && ((String)obj).equalsIgnoreCase("log out performed"))
        {
            logOut();
        }
        return obj;
    }
    
    public void logOut() throws IOException
    {
        Client c=(Client) server.clients.get(threadId);
        if(c.incomingFile.isEmpty())
        {
            server.clients.remove(threadId);
        }
        else
        {
            c.isOnline = false;
        }
        System.out.println("Id :"+threadId+" logged out");
        os.writeObject(new Boolean(true));
        socket.close();
        Thread.currentThread().stop();
        
    }
    
    public void sendFileRequest() throws IOException, ClassNotFoundException
    {
        int s=c.incomingFile.size();
        os.writeObject(s);
        for(int i=0 ; i<s ; i++)
        {
            MyChunkedFile f = c.incomingFile.get(i);
            os.writeObject(f.getFileName());
            os.writeObject(c.studentId);
            os.writeObject(f.filesize);
            boolean b = (boolean) is.readObject();
            if(b) 
            {
                os.writeObject(f);
                server.availableBuffer += f.filesize;
            }
        }
        c.incomingFile.removeAllElements();
    }
    
    
}
