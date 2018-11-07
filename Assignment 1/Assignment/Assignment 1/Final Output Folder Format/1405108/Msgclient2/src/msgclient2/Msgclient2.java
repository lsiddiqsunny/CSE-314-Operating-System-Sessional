
package msgclient2;

import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

class Msgclient2
{
   
    

    
    
   public static void receiveFile(String fileName, Socket socket) {
        try {
            InputStream is = socket.getInputStream();
        //    int bufferSize = socket.getReceiveBufferSize();
        //    System.out.println("Size of the file " + bufferSize);
            FileOutputStream fos = new FileOutputStream(fileName);
            BufferedOutputStream bout = new BufferedOutputStream(fos);
         //   Random r=new Random();
          //  int chunk_size = r.nextInt(1000)+1;
            byte[] bytes = new byte[1000];
            int bytesRead=0;
            while ((bytesRead = is.read(bytes)) >= 0) {
                bout.write(bytes, 0, bytesRead);
            }
            bout.close();
            is.close();
            fos.close();
        } catch (IOException e) {}
    } 
    

    public static void main(String argv[]) throws Exception
      
    {
        
        String sentence,sentence2,rec_id,sentence3,tt;
        String modifiedSentence;
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        Socket clientSocket = new Socket("localhost", 6789);
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream()); // amar id server er kache pathacchi
        sentence = inFromUser.readLine();
        outToServer.writeBytes(sentence + '\n');
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        modifiedSentence = inFromServer.readLine();
        if(modifiedSentence.equalsIgnoreCase("Invalid connection!")) {
        System.out.println("Invalid!");
        System.exit(0);

            }
        modifiedSentence = inFromServer.readLine();
        sentence = inFromUser.readLine();
        outToServer.writeBytes(sentence + '\n');
        
        if(sentence.equalsIgnoreCase("Receive"))
        {
            modifiedSentence = inFromServer.readLine();
            String filename=modifiedSentence;
            System.out.println(modifiedSentence);
            modifiedSentence = inFromServer.readLine();
            System.out.println(modifiedSentence);
            modifiedSentence = inFromServer.readLine();
         //   System.out.println(modifiedSentence);
         //   File f = new File("me.txt");
                        //  Msgclient2.receiveFile( "amar.txt",clientSocket);
       //  Msgclient2.receiveFile( "1"+modifiedSentence,clientSocket);
       //    Msgclient2.saveFile(clientSocket);
            
        }
        else if (sentence.equalsIgnoreCase("Send"))
        {
            System.out.println("choose ur partner");
            rec_id= inFromUser.readLine();
            outToServer.writeBytes(rec_id + '\n');
            modifiedSentence = inFromServer.readLine();
            System.out.println(modifiedSentence);
            if(modifiedSentence.equalsIgnoreCase("receiver is offline, file sending is error" + '\n') )
            {
                System.out.println("pong pong");
                System.exit(0);
            }
            else
                
            {
                modifiedSentence = inFromUser.readLine();
                outToServer.writeBytes(modifiedSentence+ '\n');
                        String fileName = null;
 
 
        System.out.println("Enter the name of the file :");
        Scanner scanner = new Scanner(System.in);
        String file_name = scanner.nextLine();
         
        
        outToServer.writeBytes(file_name + '\n');
        
        File file = new File(file_name);
        double fileLength = file.length();
     //   long fileLength =  file.length();
        System.out.println(fileLength);
            
                    InputStream ois = clientSocket.getInputStream();
        BufferedOutputStream oos = new BufferedOutputStream(clientSocket.getOutputStream());
 
   //     oos.write(file.getName());
 //  Random r=new Random();
 //           int chunk_size = r.nextInt(1000)+1;
        FileInputStream fis = new FileInputStream(file);
        byte [] buffer = new byte[5];
        Integer bytesRead = 0;
        int seq=0;
        while ((bytesRead = fis.read(buffer)) > 0) {
            //oos.write(buffer,0,bytesRead);
            String ss=new String(buffer);
         //   ss=ss.trim();
            //System.out.println("Before stuffing "+ss);
           String binary=new BigInteger(ss.getBytes()).toString(2);
           System.out.println("Before stuffing "+binary);
           char [] chararr=new char[1000];
           int p=0;
           seq++;
           int count=0;
           int checksum=0;
           for(char bit : binary.toCharArray())
                {
                    if(bit=='1') checksum++;
                   if(bit=='1')
                    {
                        count++;
                        if(count==6) {chararr[p]='0';p++;count=0;}
                    } 
                   else {count=0;}
                    chararr[p]=bit;
                   // System.out.println(bit);
                    p++;
                }
           String str = String.valueOf(chararr);
           System.out.println("&&&"+str + " "+seq);
          // str="1"+seq+'0'+str+checksum;                 ///////////// pore
           str=str+"01111110";
        //   System.out.println("After Stuffing "+str);   ////////////////// pore
        //   str=str.substring(0,str.length()-8);
         //  System.out.println("After Stuffing "+str);
          /*  char [] array=new char[ss.length()*8];
            int p=0;
            for(char c : ss.toCharArray())
            {
                System.out.println("c "+c);
                int cc=Character.getNumericValue(c);
                String binary = Integer.toBinaryString(cc);

                for(char bit : binary.toCharArray())
                {
                    array[p]=bit;
                    System.out.println(bit);
                    p++;
                }
                
    
                
            }
            String b=new String(array); */
          //str=str+"01111110";
            outToServer.writeBytes(str+'\n');
          //  System.out.println(str);
            /*byte[] toRet = new byte[array.length];
            for(int i = 0; i < toRet.length; i++) {
                    toRet[i] = (byte) array[i];
            }
            oos.write(toRet,0,bytesRead);*/
            
           // oos.write(Arrays.copyOf(buffer, buffer.length));
        }
 
        oos.close();
        ois.close();
            
        }
        
       
    }
    
}
}

