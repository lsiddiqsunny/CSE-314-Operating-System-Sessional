/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package messenger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;


/**
 *
 * @author TIS
 */
public class Client {
    
    public String receiver=null;
    public String location=null;
   
    
    
    public static void main(String[] args) {
        ConnectionUtillities connection=new ConnectionUtillities("127.0.0.1",22000);
        System.out.println("Client coneected to server"); 
        
        System.out.println("Enter your Student ID : ");
        
        Scanner in = new Scanner(System.in);
        String username=in.nextLine();


        try {
            connection.write(username);
        } catch (IOException e) {
            e.printStackTrace();
        }

        new Thread(new Read(connection,username)).start();
        new Thread(new Write(connection,username)).start();
        
        while(true);
    }
    
    
    
    
    
    
}
