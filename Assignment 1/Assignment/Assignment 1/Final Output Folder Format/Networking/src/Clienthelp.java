
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import util.ConnectionUtillities;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Fuji
 */
public class Clienthelp implements Runnable{
    public ConnectionUtillities connection;
   // byte[] bytes=new byte[100];
    
    public Clienthelp(ConnectionUtillities conn){
    
            connection=conn;
    
    }

    @Override
    public void run() {
       // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        
        
        
        System.out.println("Enter rcieverID : ");
        Scanner in = new Scanner(System.in);
        String recieverID=in.nextLine();
        connection.write(recieverID); 
//        System.out.println("Enter your filename : ");
//        String filename=in.nextLine();
        
        

//        Object m=connection.read();
//         String msg=m.toString();
//         
//         
//         if(msg.equals("Enter Filename")){
//             System.out.println("Enter Filename:");
//             
//             String filename=in.nextLine();
//             
//             
//             
//               try {
//            FileInputStream fis = new FileInputStream(filename);
//            fis.read(bytes);
//            connection.write(new String(bytes));
//        } catch (FileNotFoundException ex) {
//            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IOException ex) {
//            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
//        }
//             
//               
////         Object p=connection.read();
////         String ms=p.toString();
////         System.out.println(ms);
//             
//             
//          }
//         else{
//             System.out.println("not found");
//             
//         }
         
          


     
        
        while(true);
        
    }
    
    
    
}
