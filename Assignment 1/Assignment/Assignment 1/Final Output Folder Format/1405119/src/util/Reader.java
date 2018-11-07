package util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.SocketException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Reader implements Runnable{
    public ConnectionUtillities connection;
    
    public Reader(ConnectionUtillities con){
        connection=con;
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
    
    public String stuff(String data)
    {
        String res=new String();
        int counter = 0;
        for(int i=0;i<data.length();i++)
        {
            if(data.charAt(i) == '1')
            {
                counter++;
                res = res + data.charAt(i);
            }
            else
            {
                res = res + data.charAt(i);
                counter = 0;
            }
            if(counter == 5)
            {
                 res = res + '0';
                 counter = 0;
            }
        }
        return "01111110"+res+"01111110";
    }

    @Override
    public void run() {
        String fname,roll;
        String checksum= "00000000";
        long fsize; 
        Scanner in=new Scanner(System.in);
        while(true){
            System.out.println("Enter the receiver student ID:");
            roll=in.nextLine();
            if(roll.isEmpty())
            {
                new Thread(new Writer(connection)).start();
                break;
            }
            else
            {
                System.out.println("Enter the fileName: ");
                fname=in.nextLine();
                connection.write(roll+"% "+fname);


                String s1=connection.read();
                if(s1.equalsIgnoreCase("stop"))
                {
                    System.out.println("receiver student id is not logged in");
                }
                else
                {
                    System.out.println("Start file sending!");
                    System.out.println("Enter the fileSize: ");
                    fsize=in.nextInt();
                    connection.writee(fsize);

                    String decision=connection.read();
                    if(decision.equalsIgnoreCase("Y"))
                    {
                        long csize=connection.readd();
                        int ack=0;
                        int chunkno= (int)(fsize/csize);
                        File file=new File(fname);
                        byte [] ara = new byte[(int)fsize];
                        byte type1 = (byte) 0xFF;
                        byte type2 = (byte) 0x00;
                        try 
                        {
                            FileInputStream fis = new FileInputStream(file);
                            BufferedInputStream bis=new BufferedInputStream(fis);
                            int j=0;
                            String all;
                            
                            for(int i=0;i<=chunkno;i++)
                            {
                                all="";
                                if(i==0)
                                {
                                    if(fsize<csize)
                                    {
                                        bis.read(ara,0,(int)fsize);
                                        for(int k=0;k<fsize;k++)
                                        {
                                            all+=tosb(ara[k]);
                                        }
                                        all=tosb(type1)+tosb((byte)(i+1))+
                                        tosb(type1)+all+
                                        calculateChecksum(ara,(int)fsize);
                                        String fin=stuff(all);
                                        connection.write(fin);
                                        break;
                                    }
                                    else
                                    {
                                        bis.read(ara,0,(int)csize);
                                        for(int k=0;k<csize;k++)
                                        {
                                            all+=tosb(ara[k]);
                                        }
                                        all=tosb(type1)+tosb((byte)(i+1))+
                                            tosb(type1)+all+
                                              calculateChecksum(ara,(int)csize);
                                        String fin=stuff(all);
                                        connection.write(fin);
                                    }                                    
                                }
                                else
                                {
                                    ack++;
                                    try{
                                        
                                        connection.sc.setSoTimeout(30000);
                                        String s2=connection.read();
                                        if(Integer.parseInt(s2.substring(16,24),2)==ack && i==chunkno)
                                        {
                                            bis.read(ara,0,(int)(fsize-(chunkno*csize)));
                                            for(int k=0;k<(fsize-(chunkno*csize));k++)
                                            {
                                                all+=tosb(ara[k]);
                                            }
                                            all=tosb(type1)+tosb((byte)(i+1))+
                                            tosb(type1)+all+
                                                  calculateChecksum(ara,(int)csize);
                                            String fin=stuff(all);
                                            connection.write(fin);
                                            break;
                                        }
                                        else if(Integer.parseInt(s2.substring(16,24),2)==ack)
                                        {
                                            bis.read(ara,0,(int)csize);
                                            for(int k=0;k<csize;k++)
                                            {
                                                all+=tosb(ara[k]);
                                            }
                                            all=tosb(type1)+tosb((byte)(i+1))+
                                            tosb(type1)+all+
                                                  calculateChecksum(ara,(int)csize);
                                            String fin=stuff(all);
                                            connection.write(fin);
                                        }
                                        else
                                        {
                                            i--;
                                        }   
                                    }catch(SocketException e){
                                        connection.write(stuff(all));
                                        i--;    
                                    }
                                }
                            }
                        } catch (FileNotFoundException ex) {
                            Logger.getLogger(Reader.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (IOException e){
                        }
                  }
                    else
                    {
                        System.out.println("File size is too big!");
                        //break;
                    }
                }   
            }    
        }
    }
}