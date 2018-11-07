/*
 * To change this template, choose Tools | Templates1
 * and open the template in the editor.
 */
package FileTransmitter;

import java.net.SocketException;
import java.util.Scanner;
/**
 *
 * @author uesr
 */
public class Client {
    public static String username ;
    public static void main(String[] args) throws SocketException {
        ConnectionUtillities connection=new ConnectionUtillities("127.0.0.1",22222);
        connection.sc.setSoTimeout(5000);
        System.out.println("Enter your Student id : ");
        
        Scanner in = new Scanner(System.in);
        username=in.nextLine();                
        connection.write(username);        
        
        new Thread(new clientReaderWriter(username,connection)).start();

    }
}
