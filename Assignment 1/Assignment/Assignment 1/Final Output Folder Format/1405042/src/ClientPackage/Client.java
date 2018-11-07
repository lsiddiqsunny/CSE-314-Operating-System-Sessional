/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ClientPackage;

import util.ConnectionUtillities;
import util.FileReader;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 *
 * @author uesr
 */
public class Client {
    
    public static void main(String[] args) throws UnknownHostException
    {

        String host = Inet4Address.getLocalHost().getHostAddress();
        ConnectionUtillities connection = new ConnectionUtillities(host,22228);

        System.out.println("Enter your username : ");
        
        Scanner in = new Scanner(System.in);
        String username=in.nextLine();                
        connection.write(username);

        String msgFromServer = connection.read().toString();
        System.out.println(msgFromServer);

        if(msgFromServer.equals("Welcome to File Transfer!"))
        {
            new Thread(new FileReader(connection)).start();

            while(true);

        }

    }
}
