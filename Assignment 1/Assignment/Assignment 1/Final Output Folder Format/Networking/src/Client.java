import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import util.ConnectionUtillities;
import util.Reader;
import util.Writer;

/**
 *
 * @author uesr
 */
public class Client {
    
    public static void main(String[] args) {
        
       
        ConnectionUtillities connection=new ConnectionUtillities("127.0.0.1",22222);
        
        byte[] bytes=new byte[Server.maxchnk];
        long filesize;
        
        
        System.out.println("Enter your username : ");
        Scanner in = new Scanner(System.in);
        String username=in.nextLine();                
        connection.write(username);
        
        Object k=connection.read();
        String  warng=k.toString();
        if(warng.equals("username already in use")){
                System.out.println("username already in use");
        }
        else{
            
            new Thread(new Clienthelp(connection)).start();
            
            Object m=connection.read();
         String msg=m.toString();
         
         
         if(msg.equals("Enter Filename")){
             
             System.out.println("Enter Filename:");
             String filename=in.nextLine();
             
             
             File file = new File(filename);
             filesize=file.length();
             if(filesize<Server.buffersize){
                 
                 try {
                   
            FileInputStream fis = new FileInputStream(filename);
            fis.read(bytes);
            connection.write(new String(bytes));
            
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
             
             }
             
             
             
               
             
               
//         Object p=connection.read();
//         String ms=p.toString();
//         System.out.println(ms);
             
             
          }
         else{
             System.out.println("not found");
             
         }
            
            
            
         //   new Thread(new Reader(connection)).start();
            new Thread(new Writer(connection)).start();
        
        }
        
    }
}
