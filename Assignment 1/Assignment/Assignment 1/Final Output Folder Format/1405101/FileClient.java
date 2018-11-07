

package pkg1405101client;

import com.sun.org.apache.xalan.internal.xsltc.dom.BitArray;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import static java.rmi.server.ObjID.read;
import java.util.Scanner;
import java.util.Timer;

public class FileClient {
   
    private Socket studentSocket;
    DataOutputStream from_ip_to_server;
    DataInputStream  from_server_to_ip;
    BufferedReader binFile;
    InputStream is;
    OutputStream os;
    BufferedReader bin;
    
    Scanner input=new Scanner(System.in);
    int clientId,receiverID,fileSize;
    String fileName,login="L",yes_no;
    
    
    public FileClient(String host, int port)  {
         
            
            try {
                    studentSocket = new Socket(host, port);
                    from_ip_to_server = new DataOutputStream(studentSocket.getOutputStream());
                    binFile = new BufferedReader(new InputStreamReader(studentSocket.getInputStream()));
                    from_server_to_ip = new DataInputStream(studentSocket.getInputStream());
                    bin = new BufferedReader(new InputStreamReader(System.in));
            } catch (Exception e) {
                    System.err.println("Problem in connecting with the server. Exiting.");
		    System.exit(1);
            }
            
            try {
                while(!login.equals("B")){
                    
                    clientId=input.nextInt();
                    
                    if(checkID(clientId)){
                        
                        System.out.println("Do you wanna send file?");
                        yes_no=bin.readLine();
                        
                        if(yes_no.equals("y")){
                            from_ip_to_server.writeBytes("y"+ '\n');
                            System.out.println("Enter Receiver ID");
                            receiverID=input.nextInt();
                            
                            if(!checkID(receiverID)){
                                                       
                                System.out.println("Enter file name and size");
                                fileName= bin.readLine();
                                fileSize=input.nextInt();
                                int n=checkServerCapacity(fileName,fileSize);
                                
                                if(n>0){
                                    sendFile(fileName, n);
                                    String name;
                                    while((name=binFile.readLine())!=null){
                                        String a=binFile.readLine();
                                        int t_size=Integer.parseInt(a);

                                        receiveFile(name,  t_size);
                                        break;
                                    }
                                }
                                else{
                                    System.out.println("Maximum capacity in server reached.");
                                    break;
                                }
                            }
                            else System.out.println("Receiver is not logged in.");
                                
                        }

                        else if(yes_no.equals("n")){
                            
                            String name;
                            while((name=binFile.readLine())!=null){
                                String a=binFile.readLine();
                                int t_size=Integer.parseInt(a);

                                receiveFile(name,  t_size);
                                break;
                            }
                        }
                        break;
                    }
                    login=input.next();
                }
            } catch (Exception e) {
                System.err.println("Problem in connecting with the server. Exiting.");
		System.exit(1);
                   
            }            
    }
    
    public boolean checkID(int id) throws IOException{
        
        from_ip_to_server.writeBytes(String.valueOf(id)+ '\n');
        from_ip_to_server.flush();
        
        if((binFile.readLine()).equals("Y")){
            
            return true;
        }
        else return false;
        
    }
    
    
    public int checkServerCapacity(String file,int size) throws IOException{
        from_ip_to_server.writeBytes(file+ '\n');
        from_ip_to_server.flush();
        
        from_ip_to_server.writeBytes(String.valueOf(size)+ '\n');
        from_ip_to_server.flush();
        String a=binFile.readLine();
        return Integer.valueOf(a);
        
        
    }

    

    public void sendFile(String file,int chunkSize) throws IOException, InterruptedException {
            
            FileInputStream finFile = new FileInputStream(file);
            
            byte[] buffer = new byte[chunkSize];
            int count=0;
            int read;
            int sequence=0;
            int ack=0;
            
            while(true) {
                if((read=finFile.read(buffer)) > 0){
                    
                    String content = "";
                    
                    long start=System.nanoTime();
                    
                    System.out.println("hey");
                    for(int i=0;i<read;i++){
                        byte b=buffer[i];
                        content += convertByteToString(b);
                    }
                    content += checksumCalculation(buffer, read);
                    if(sequence==0 && ack==0)
                        content="0000000000000000"+content;
                    else content="0000000100000001"+content;
                    
                    content="10101010"+content;
                    
                    System.out.println(content);
                    String afterStuffedcontent = "01111110"+bitStuffing(content)+"01111110";
                    System.out.println("AfterBitstuffing");
                    System.out.println(afterStuffedcontent);
                    
                    from_ip_to_server.writeBytes(afterStuffedcontent+ '\n'); 
                    
                    String a=binFile.readLine();
                    if(a.equals("1")){
                        ack=1;
                        sequence=1;
                    }
                    else{ ack=0; sequence=0;}
                    Thread.sleep(1000);
                    from_ip_to_server.flush();
                    System.out.println(count);
                    count++;
                    
                }
                else break;
            }
            
            System.out.println("All chunks sent.");
            from_ip_to_server.writeBytes("F"+'\n');
            
            finFile.close();
            from_ip_to_server.close();
            
    }
    
    
    public void receiveFile(String file,int size) throws IOException {
        try (FileOutputStream foutFile = new FileOutputStream("Out"+file)) {
            byte[] buffer = new byte[size];

            int read;
            int totalRead = 0;
            int remaining = size;
            
            while((read = from_server_to_ip.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
                    
                totalRead += read;
                remaining -= read;
                System.out.println("read "+totalRead+" bytes");    
                foutFile.write(buffer, 0, read);
            }
        }
            from_server_to_ip.reset();
                   
            
    }
    
    
    public String convertByteToString(byte b)
    {
        String binary = "";
        for(int i = 7; i >=0; i--){
            binary+=((1<<i) & b)==0?"0":"1";
        }
        return binary;
    }
    
    public String convertBitTOstring(String input){
        StringBuilder sb = new StringBuilder(); 
        //Arrays.stream(input.split("(?<=\\G.{8})")).forEach(s -> sb.append((char) Integer.parseInt(s, 2)));
        //return sb.toString();
        String s1,s2="";
        
        int i=0;
        while(i<input.length()){
            s1=input.substring(i,i+8);
            sb.append((char) Integer.parseInt(s1, 2));
            i=i+8;
        }
        return sb.toString();
    }
    
    public String bitStuffing(String input){
        String result="";
        int counter=0;
           
        for(int i=0;i<input.length();i++){
            
            if(input.charAt(i) == '1'){
                counter++;
                result = result + input.charAt(i);
            }
            
            else{
                result = result + input.charAt(i);
                counter = 0;
            }
            
            if(counter == 5){
                result = result + '0';
                counter = 0;
            }
        }
        
        return result;
    }
    
    public String checksumCalculation(byte[] buffer,int read){
        byte checksum = buffer[0];
        for(int i=1;i<read;i++){
            checksum = (byte) (checksum ^ buffer[i]);
        }
        
        return convertByteToString(checksum);
    
    }

    public static void main(String[] args) {
        
        FileClient fc = new FileClient("localhost", 1988);
    }
    
}




