package util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import messenger.FileInfo;
import messenger.Server;

public class Writer implements Runnable{
    public ConnectionUtillities connection;
    int maxsize = 999999;
    byte[] b;
    public Writer(ConnectionUtillities con){
        this.b = new byte[maxsize];
        connection=con;
    }

    @Override
    public void run() {
        
        while(true){
            int bread;
            try {
                String fid= connection.read();
                //System.out.println("path: "+path);
                File test = new File("G:\\"+fid+".txt");
                test.createNewFile();
                FileOutputStream fos = new FileOutputStream(test);
                BufferedOutputStream out = new BufferedOutputStream(fos);
                //System.out.println("vaisob ami writer");
                while ((bread = connection.read(b,0,b.length)) != -1) 
                {
                 //   bread = connection.read(b,0,b.length);
                  //  System.out.println(new String(b));
                    out.write(b,0,bread);
                    out.flush();
                    out.close();
                  //  break;
                    //if((int)test.length() !=0 ){break;}
                    //System.out.println((int)test.length());
                  //  out.flush();
                }
                //connection.write("done");
                break;
                //out.flush();
               //FileInfo finfo=Server.fileList.get(fid); 
               //connection.sc.close();
               //fos.close();
               //connection.in.close();
                
                       
            } catch (IOException ex) {
                Logger.getLogger(Writer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }   
    }    
}