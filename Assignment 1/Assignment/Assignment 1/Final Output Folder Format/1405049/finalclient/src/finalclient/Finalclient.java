/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package finalclient;

import java.io.*;
import java.net.*;

/**
 *
 * @author Ayesha
 */
public class Finalclient {

    
        public static String sendfile(Socket s, File f) throws FileNotFoundException, IOException {
        FileInputStream fis = new FileInputStream(f);
        BufferedInputStream bis = new BufferedInputStream(fis);
        //OutputStream os = s.getOutputStream();
        int sz=1000;
        long fileLength = f.length();
        long cur = 0;
        
        byte[] array;
        
        String ans= new String();
        

        for(;cur < fileLength;) {
            
            if (fileLength - cur < sz) {
                sz = (int) (fileLength - cur);
            } 
            cur+=sz;
            array = new byte[sz];
            
            bis.read(array, 0, sz);
            for(int i=0;i<sz;i++)
            {
                byte b = array[i];
                String bitt = Integer.toBinaryString(b & 0xFF);
                ans+=bitt;
                //System.out.println(bitt);
            }
            
            //os.write(array);
            System.out.println("Sending file ... " + (cur * 100) / fileLength + "% complete!");

        }
        //s.close();
        return ans;
    }

    public static void main(String[] args) throws IOException {
        String client1, client2,send;
        String modifiedSentence;
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        Socket clientSocket = new Socket("localhost", 6789);
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        

        while (true)
        {
            
            client1 = inFromUser.readLine();
            outToServer.writeBytes(client1 + "\n");
            String filename = null;
            filename = inFromUser.readLine();
            File file3 = new File(filename);
            String aaa = new String();
            //aaa = "11111110111111111";
            aaa= sendfile(clientSocket,file3);
            
            int cnt=0;
            
            String str = new String();
            
            for(int i=0;i<aaa.length();i++)
            {
                if(aaa.charAt(i)=='1'){
                    cnt++;
                }
                else cnt=0;
                str+=(aaa.charAt(i));
                if(cnt==5)
                {
                    cnt=0;
                    str+='0';
                }
            }
           
            
            outToServer.writeBytes("01111110"+str+"01111110"+"\n");
            
            //System.out.println(str);

        }
    }
}
