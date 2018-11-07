
package msgserver2;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
class people
{
 //   int port;
    String sen;
    Socket soc;
    String rec;
//    InetAddress ip;
    people(String sen,String rec, Socket t)
    {
        this.sen=sen;
        this.soc=t;
        this.rec=rec;
        
    }
    String getrec()
    {
        return rec;
    }
    Socket getsoc()
    {
        return soc;
    }
    String getsen()
    {
        return sen;
    }
}
public class Msgserver2
{
  
    public static int i=0;
    public static Map vehicles = new HashMap();
    public static people p[]=new people [150];
        public static void sendFile(File file,Socket socket) {

        FileInputStream fis;
        InputStream bis;
        //OutputStream out;
        BufferedOutputStream out;
    //    Random r=new Random();
     //       int chunk_size = r.nextInt(1000)+1;
        byte[] buffer = new byte[1000];
        try {
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);
            out = new BufferedOutputStream(socket.getOutputStream());
            //out = socket.getOutputStream();
            int count=0;
            while ((count = bis.read(buffer)) > 0) {
                out.write(buffer, 0, count);

            }
            out.close();
            fis.close();
            bis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    


            
    
    
        public static void saveFile(String filename,Socket socket) throws Exception {
            File f=new File(filename);
        try {
            InputStream is = socket.getInputStream();
            int bufferSize = socket.getReceiveBufferSize();
            System.out.println("Size of the file " + bufferSize);
            
            FileOutputStream fos = new FileOutputStream(f);
            BufferedOutputStream bout = new BufferedOutputStream(fos);
            
        //    Random r=new Random();
         //   int chunk_size = r.nextInt(1000)+1;
            int seq=0;
            byte[] bytes = new byte[5];
            int bytesRead=0;
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
           String ss ;
           while((ss=inFromServer.readLine())!=null)
           {
               seq++;
               ss=ss.substring(0,ss.length()-8);
               System.out.println("ss "+ss);
               char[] ch=ss.toCharArray(); 
               
               
           int p=0,x=0;
           int count=0;
           for(x=3;x<ch.length;x++)
           {
               if(ch[x] == '1')
                  {
                      count++;
                      System.out.print(ch[x]);
                  }
                  else if(ch[x]=='0' && count==5 && ch[x+1]=='1')
                          {
                          //    System.out.print("bit++ "+ ch[x+1]);
                              count=0;
                          }
                  else {count=0;System.out.print(ch[x]);}
           }
        /*   for(char bit : ss.toCharArray())
                {
                    if(p<3){p++;System.out.print(bit);}
                    else{
                   
                  if(bit == '1')
                  {
                      count++;
                      System.out.print(bit);
                  }
                  else if(bit=='0' && count==5 && bit++=='1')
                          {
                              System.out.print("bit++ "+ bit++);
                              count=0;
                          }
                  else {count=0;System.out.print(bit);}
                  p++;
                }
                  
                }*/
               
               
           }
         /*   while ((bytesRead = is.read(bytes)) >= 0) {
                bout.write(bytes, 0, bytesRead);
                String ss=new String(bytes);
                ss=ss.trim();
            char [] array=new char[ss.length()*8];
            int p=0;
            for(char c : ss.toCharArray())
            {
                int cc=Character.getNumericValue(c);
                String binary = Integer.toBinaryString(cc);

                for(char bit : binary.toCharArray())
                {
                    array[p]=bit;
                    System.out.println(bit);
                    p++;
                }
                
    
                
            }
            }*/
            bout.close();
            is.close();
            
        } catch (IOException e) {}
        
        
    }

    
    public static void main(String argv[]) throws Exception
    {
        
     //   people p[]=new people [150];
        
    //    Map receivers = new HashMap();
        
        int workerThreadCount = 0;
        int id = 1;
        ServerSocket welcomeSocket = new ServerSocket(6789);
        while(true)
        {
            Socket connectionSocket = welcomeSocket.accept();
            WorkerThread wt = new WorkerThread(connectionSocket,id,vehicles);
            Thread t = new Thread(wt);
            t.start();
            workerThreadCount++;
            System.out.println("Client [" + id + "] is now connected. No. of worker threads = " + workerThreadCount);
            id++;
        }

    }
}
class WorkerThread implements Runnable
{

    private Socket connectionSocket;
    private int id;
    Map vehicles=new HashMap();
    
 //   int i;
  //  Map receivers=new HashMap();

    public WorkerThread(Socket ConnectionSocket, int id,Map< String,Integer> v)
    {
        this.connectionSocket=ConnectionSocket;
        this.id=id;
        vehicles=v;
  //      this.p=p;
    //    this.i=i;
        
    }

    public void run()
    {

        
        String clientSentence,clientSentence1,sentence2,filename;
        String rec_id,sen_id;
        String capitalizedSentence;
        while(true)
        {
            try
            {
                
                BufferedReader inFromServer = new BufferedReader(new InputStreamReader(this.connectionSocket.getInputStream()));
                DataOutputStream outToServer ;//= new DataOutputStream(connectionSocket.getOutputStream());
           //     System.out.println(outToServer);
                
                clientSentence = inFromServer.readLine();
                
                System.out.println("port "+ this.connectionSocket.getPort());
                sen_id=clientSentence;
                //System.out.println(clientSentence);
                //outToServer = new DataOutputStream(connectionSocket.getOutputStream());
                //outToServer.writeBytes("pom pom pom" + '\n'); 
                String searchKey =clientSentence;
                if(Msgserver2.vehicles.containsKey(searchKey))
                {
                    outToServer = new DataOutputStream(this.connectionSocket.getOutputStream());
                    outToServer.writeBytes("Invalid connection!" + '\n');
                    capitalizedSentence = "Invalid connection!";
                   // outToServer = new DataOutputStream(connectionSocket.getOutputStream());
             //       outToServer.writeBytes(capitalizedSentence + '\n');
                    System.out.println("invalid!");
                  //  i--;
                    continue;
                  //  System.exit(0);
                }
                else
                {
                    outToServer = new DataOutputStream(this.connectionSocket.getOutputStream());
                    outToServer.writeBytes("Connected" + '\n');
                    //MsgServer.[Msgserver.i] = new people(clientSentence,this.connectionSocket);
                  
                    //System.out.println("Sender " + p[Msgserver.i].id);
//  System.out.println("dhukse");
                  //  p[ii] = new people(clientSentence,connectionSocket);
                  //  System.out.println(connectionSocket.getPort());
                  //  p[i].s=connectionSocket;
                    Msgserver2.vehicles.put(clientSentence,1);
                    capitalizedSentence = "connected";
            //        outToServer.writeBytes(capitalizedSentence + '\n');
                    System.out.println("valid!");
                }
               // Msgserver.i++ ;
                outToServer.writeBytes("Send File or Receive File?" + '\n');
                clientSentence1 = inFromServer.readLine();
                if(clientSentence1.equalsIgnoreCase("Send"))
                {
                    rec_id= inFromServer.readLine();
                    searchKey=rec_id;
                    Msgserver2.p[Msgserver2.i] = new people(clientSentence,rec_id,this.connectionSocket);
                    
                if(vehicles.containsKey(searchKey))
                {
                    outToServer = new DataOutputStream(this.connectionSocket.getOutputStream());
                    outToServer.writeBytes("receiver is online" + '\n');
                 //   receivers.put(clientSentence,1);
                 //   System.out.println("mee"+ Msgserver.i);
                 //   outToServer.writeBytes("Congrazz receiver is online" + '\n');
                    Socket tt=new Socket();
                    for(int j=0;j<=Msgserver2.i ;j++)
                    {
                     //   System.out.println("paisi1");
                     //   System.out.println("bull " + rec_id) ;
                        if(Msgserver2.p[j].getsen().equalsIgnoreCase(rec_id)){ 
                          tt=Msgserver2.p[j].getsoc();
                    //    System.out.println("paisi2");
                        //tt=new Socket(Msgserver.p[j].getip(),Msgserver.p[j].getport());
                          
                          System.out.println(j);
                          sentence2 = inFromServer.readLine();
                      //    System.out.println("sentence2 "+sentence2);
                     //     for(int k=0;k<=Msgserver.i ;k++)
                          outToServer = new DataOutputStream(tt.getOutputStream());
                          
                   outToServer.writeBytes(sentence2 + '\n');
                   filename = inFromServer.readLine();
  //                 byte[] size = new byte[buffered.available()];
                    File f= new File(filename);
                    System.out.println("size " + f.length()/1024);
                  Msgserver2.saveFile(filename,this.connectionSocket);
                  
                   outToServer.writeBytes(filename);
                //   Msgserver2.sendFile(f, tt);
                 //  System.out.println("sdsd "+j);
                          break;
                        }
                        else {System.out.println("mimi"); continue ;}
                    }
                    
                 //  outToServer = new DataOutputStream(t.getOutputStream());
                 //  outToServer.writeBytes("pom pom pom" + '\n');
                    //sentence2 = inFromServer.readLine();
                    //DataOutputStream outToServer = new DataOutputStream(connectionSocket.getOutputStream());
                }
                else
                {
                 //   outToServer = new DataOutputStream(.getOutputStream())
                    outToServer = new DataOutputStream(this.connectionSocket.getOutputStream());
                    outToServer.writeBytes("receiver is offline, file sending is error" + '\n');
                    System.out.println("receiver is offline");
                 //   System.exit(0);
                    
            //        outToServer.writeBytes("Sorry receiver is offine" + '\n');
                }
                
                    
                    
                    
                }
                else
                {
                    outToServer = new DataOutputStream(connectionSocket.getOutputStream());
                    Msgserver2.p[Msgserver2.i] = new people(clientSentence,"-99",this.connectionSocket);
                    //rec_id = "-11"; 
                    outToServer.writeBytes("waot for file" + '\n');
                }
                
                Msgserver2.i++ ;
                
           
            }
            catch(Exception e)
            {

            }
        }
    }
}
