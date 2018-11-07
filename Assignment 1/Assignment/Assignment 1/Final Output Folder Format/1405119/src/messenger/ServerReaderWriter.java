package messenger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import util.ConnectionUtillities;
import util.Writer;


public class ServerReaderWriter implements Runnable{

    public HashMap<String,Information> clientList;
    public ConnectionUtillities connection;
    public String user;
    long maxsize = Server.rest;
    byte[] b;
    String fid=Server.fileID();
    File test;
    byte type1 = (byte) 0xFF;
    byte type2 = (byte) 0x00;
    
    public ServerReaderWriter(String username,ConnectionUtillities con, HashMap<String,Information> list){
        connection=con;
        clientList=list;
        user=username;
        b=new byte[(int)maxsize];
    }
    public void waitt(long t)
    {
        while(t>0)
        {
            t--;
        }
        
    }
    public String calculateChecksum(byte[] ara,int size)
    {
        byte a=0x00;
        for(int i=0;i<size;i++)
        {
            a = (byte) (a^ara[i]);
        }
        return tosb(a);
    }
    
    public String tosb(byte a)
    {
        return String.format("%8s", Integer.toBinaryString((byte)a & 0xFF)).replace(' ', '0');
    }
    
    public String destuff(String res)
    {
        int counter=0;
        String out=new String();
        for(int i=8;i<res.length()-8;i++)
        {                   
            if(res.charAt(i) == '1')
            {
                counter++;
                out = out + res.charAt(i);
            }
            else
            {
                out = out + res.charAt(i);
                counter = 0;
            }
           if(counter == 5)
            {
                if((i+2)!=res.length())
                {
                    out = out + res.charAt(i+2);
                }
                else
                {
                    out=out + '1';
                }
                i=i+2;
                counter = 1;
            }
        }
        return out;
    }
    
    @Override
    public void run() {
        while(true){
            int ack=0;
            String data=connection.read();
            String msg[]=data.split("%");
            String username=msg[0];
            String fname=msg[1];
            if(clientList.containsKey(username))
            {
                connection.write("start");
                long fsize=connection.readd();
                if(fsize>maxsize)
                {
                   connection.write("N");
                }
                
                else
                {   
                    try {
                        connection.write("Y");
                        connection.writee(Server.chunksize);
                        int chunkno=(int)(fsize/Server.chunksize);
                        Server.rest=(Server.rest-fsize);
                        test = new File("H:\\"+fid+".txt");
                        test.createNewFile();
                        FileOutputStream fos = new FileOutputStream(test);
                        BufferedOutputStream out = new BufferedOutputStream(fos);
                        int i=0;
                        while(true)
                        {
                            int p=0;
                            String read=connection.read();
                            String ds=destuff(read);
                            String type=ds.substring(0,8);
                            String seq_no=ds.substring(8,16);
                            String ack_no=ds.substring(16,24);
                            String payload=ds.substring(24,ds.length()-8);
                            String checksum=ds.substring(ds.length()-8,ds.length());
                            
                            for(int k=0;k+8<=(payload.length());k+=8)
                            {
                                String now=payload.substring(k,k+8);
                                b[p]=(byte)Integer.parseInt(now,2);
                                p++;
                            }
                            
                            if(p==Server.chunksize)
                            {
                                if(calculateChecksum(b,p).equalsIgnoreCase(checksum))
                                {
                                    ack=Integer.parseInt(seq_no,2);
                                    System.out.println("Checksum: at server: " + calculateChecksum(b,p) 
                                            + ", at cleint: " + checksum);
                                    String all=tosb(type2)+tosb(type2)+
                                            tosb((byte)ack)+tosb(type1)+tosb(type2);
                                    connection.write(all);
                                }
                                else
                                {
                                    continue;
                                }   
                                i+=p;
                                out.write(b,0,p);
                                out.flush();
                            }
                            else
                            {
                                i+=p;
                                break;
                            }
                        }
                        if(i==fsize)
                        {
                            Server.fileList.put(fid,new FileInfo(user,username,fsize,test));
                        }
                        else
                        {
                            System.out.println("error in receiving in server");
                            test.delete();
                            Server.rest+=fsize;
                            break;
                        }
                        fos.close();
                        out.close();
                    } catch (IOException ex) {
                        Logger.getLogger(ServerReaderWriter.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    Information info=clientList.get(username);
                    info.connection.write(fid);
                    FileInfo finfo=Server.fileList.get(fid);
                    FileInputStream fis;
                    byte[]ara=new byte[(int)fsize];
                    try {
                        //System.out.println(finfo.getFile().getAbsoluteFile());
                        fis = new FileInputStream(finfo.getFile());
                        BufferedInputStream bis=new BufferedInputStream(fis);
                        bis.read(ara,0,(int)fsize);
                       // System.out.println(new String(ara));
                        info.connection.write(ara,0,(int)fsize);
                        fis.close();
                        test.delete();
                        
                        
                        Server.rest+=fsize;
                        
                       // String read=info.connection.read();
                       // System.out.println("read: "+read);
                      //  if(read.equalsIgnoreCase("done"))
                     //   {
                          //finfo.getFile().delete();
                     //   }
                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(ServerReaderWriter.class.getName()).log(Level.SEVERE, null, ex);
                    }catch(IOException ex){
                        
                    }
                }
            }
            else
            {
                connection.write("stop");
            }
        }
    }
}