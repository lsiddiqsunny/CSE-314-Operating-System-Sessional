
package pkg1405101sever;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.print.DocFlavor;

public class FileServer {
    public static Vector<FileData> dataset=new Vector<FileData>(1,1);
    public static Vector<IpData> ipSet=new Vector<IpData>(1,1);
    
    public int totalcapacity=10000000;
    public int file_id=0;
    
	    
    public static void main(String argv[]) throws Exception
    {
        
        int workerThreadCount = 0;
        int id = 1;
        
        ServerSocket mainSocket = new ServerSocket(1988);
        while(true)
        {
            
            Socket connectionSocket = mainSocket.accept();
            WorkerThread wt = new WorkerThread(connectionSocket,id);
            Thread t = new Thread(wt);
            t.start();
            workerThreadCount++;
            System.out.println("Client [" + id + "] is now connected. No. of worker threads = " + workerThreadCount);
            id++;
        }
		
    }

}


class WorkerThread extends FileServer implements Runnable 
{
    private Socket connectionSocket;
    private int ip_address;
    private int student_id;
    
    private DataOutputStream server_to_client ;
    private DataInputStream client_to_server;
    private BufferedReader inFile;
    
   
    public WorkerThread(Socket ConnectionSocket, int id) throws IOException 
    {
        this.connectionSocket=ConnectionSocket;
        this.ip_address=id;
        IpData data=new IpData();
        data.IP=id;
        ipSet.add(data);
        server_to_client = new DataOutputStream(this.connectionSocket.getOutputStream());
        client_to_server=new DataInputStream(this.connectionSocket.getInputStream());
        inFile = new BufferedReader(new InputStreamReader(this.connectionSocket.getInputStream()));
        
    }
    
    @Override
    public void run() {
        
        
        while (true) {
            try {
                
                                
                String a=inFile.readLine();
                student_id=Integer.parseInt(a);
                
                
                if(checkStudentID(student_id)){
                                        
                    server_to_client.writeBytes("Y"+ '\n');
                    enterID(ip_address,student_id);
                    
                    
                    String choice=inFile.readLine();
                    
                    if(choice.equals("y")){
                        
                        String Cho=inFile.readLine();
                        int receiver_id=Integer.parseInt(Cho);
                        
                        if(!checkStudentID(receiver_id)){

                            server_to_client.writeBytes("N"+ '\n');
                            
                            String name=inFile.readLine();
                            String c_size=inFile.readLine();
                            
                            int new_size=Integer.parseInt(c_size);
                            
                            int d=generateChunkSize(new_size);
                            server_to_client.writeBytes(String.valueOf(d)+"\n");
                            
                            createFileID(file_id++,name,new_size,student_id,receiver_id,d);
                            

                            try (FileOutputStream fos = new FileOutputStream(name)
                            ) {
                                int read, totalRead = 0;
                                while(true) {
                                    
                                    System.out.println("--1--");
                                    String stuffSize=inFile.readLine();
                                    System.out.println(stuffSize);
                                    
                                    if(!stuffSize.equals("F")){
                                        
                                        String content = bitDeStuffing(stuffSize);
                                        System.out.println("AfterBitDestuffing");
                                        System.out.println(content);
                                        int len=content.length();
                                        String checksum=content.substring(len-8);
                                        String sequence=content.substring(8, 16);
                                        String ack=content.substring(16,24);
                                        String mainPart=content.substring(24,len-8);
                                        content=convertBitTOstring(mainPart);
                                        
                                        byte[] workingBuffer=content.getBytes();
                                        
                                        System.out.println(workingBuffer.length);
                                        
                                        if(hasChecksumError(workingBuffer, workingBuffer.length, checksum))
                                            fos.write(workingBuffer);
                                        else System.out.println("checksum error" +workingBuffer.length);
                                        
                                        if(ack.equals("00000000"))
                                            server_to_client.writeBytes("0"+'\n');
                                        else if(ack.equals("00000001"))
                                            server_to_client.writeBytes("1"+'\n');
                                        
                                        
                                        
                                        try {
                                            Thread.sleep(1000);
                                        } catch (InterruptedException ex) {
                                            Logger.getLogger(WorkerThread.class.getName()).log(Level.SEVERE, null, ex);
                                        }
                                        server_to_client.flush();
                                        
                                    }
                                    else break;
                                }   if(inFile.readLine().equals("F"))
                                    System.out.println("File reads finished.");
                            }
                            
                            

                        }
                        else server_to_client.writeBytes("Y"+ '\n');
                    }
                    else if(choice.equals("n")){
                        while(true)
                            sendFile(student_id);
                                
                    }
                }
            } catch (IOException e) {
                   System.err.println("Problem in connecting with the server. Exiting main.");
		   System.exit(1);
            }
        }
    }
    
    
    
    private void sendFile(int id) throws IOException {
        String name="";
        int size=0;
        for(int i=0;i<dataset.size();i++){
            if(dataset.elementAt(i).receiverID==id){
                int read;
                name= dataset.elementAt(i).fileName;
                size= dataset.elementAt(i).fileSize;
                System.out.println(name +" "+ size);
                dataset.remove(i);
                
                server_to_client.writeBytes(name+'\n');
                server_to_client.flush();
                server_to_client.writeBytes(String.valueOf(size)+'\n');
                server_to_client.flush();
                
                FileInputStream fis = new FileInputStream(name);
                
                byte[]  buffer = new byte[size];

                while ((read=fis.read(buffer)) > 0) {
                        server_to_client.write(buffer,0,read);

                }
                fis.close();
                server_to_client.flush();
                break;
            }                
        }
        
        
    }
    
    
    
    public boolean checkStudentID(int ID){
        for(int i=0;i<ipSet.size();i++){
            if(ID==ipSet.elementAt(i).clientID)
                return false;
        }
        return true;
    }
    
    public void enterID(int ip, int ID){
        System.out.println(ipSet.size());
 
        for(int i=0;i<ipSet.size();i++){                     
            if(ipSet.elementAt(i).IP==ip){
               ipSet.elementAt(i).clientID=ID;
               
               break;
            }
        }
    }
    
    public int generateChunkSize(int new_size){
        int capacity=0;
        
        Random dice=new Random();
        
        for(int i=0;i<100;i++){
            capacity=+dice.nextInt(100);
        }        
                
        if(capacity+new_size<totalcapacity){
            return dice.nextInt(new_size/10)+1;
        }
        return -1;
            
    }
    
    public void createFileID(int file_id,String name, int f_size, int c_ID, int r_ID, int chunk_size){
        FileData data=new FileData();
        data.fileId=file_id;
        data.fileName=name;
        data.fileSize=f_size;
        data.clientID=c_ID;
        data.receiverID=r_ID;
        data.chunkSize=chunk_size;
        dataset.addElement(data);
    }
    
    
    public String convertBitTOstring(String input){
        StringBuilder sb = new StringBuilder(); 
        
        String s1,s2="";
        
        int i=0;
        while(i<input.length()){
            s1=input.substring(i,i+8);
            sb.append((char) Integer.parseInt(s1, 2));
            i=i+8;
        }
        return sb.toString();
    }
    
    public String convertByteToString(byte b)
    {
        String binary = "";
        for(int i = 7; i >=0; i--){
            binary+=((1<<i) & b)==0?"0":"1";
        }
        return binary;
    }
    
    public String checksumCalculation(byte[] buffer,int read){
        byte checksum = buffer[0];
        for(int i=1;i<read;i++){
            checksum = (byte) (checksum ^ buffer[i]);
        }
        
        return convertByteToString(checksum);
    
    }
    
    public boolean hasChecksumError(byte[] buffer,int read, String check){
        byte checksum = buffer[0];
        for(int i=1;i<read;i++){
            checksum = (byte) (checksum ^ buffer[i]);
        }
        
        return check.compareTo(convertByteToString(checksum))==0;
    
    }
    
    public String bitDeStuffing(String input1){
        String result="";
        int counter=0;
        String input=input1.substring(8, input1.length()-8);
        
        for(int i=0;i<input.length();i++)
        {
            if(input.charAt(i) == '1')
            {

                counter++;
                result = result + input.charAt(i);

            }
            else
            {
                result = result + input.charAt(i);
                counter = 0;
            }
            if(counter == 5)
            {
                if((i+2)!=input.length())
                    result = result + input.charAt(i+2);
                else
                    result=result + '1';
                i=i+2;
                counter = 1;
            }
        }
        
        return result;
    }
    
}



class FileData{
    public int fileId;
    public String fileName;
    public int fileSize;
    public int clientID;
    public int receiverID;
    public int chunkSize;
}

class IpData{
    public int IP;
    public int clientID;
}



