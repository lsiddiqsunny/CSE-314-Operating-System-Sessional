/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package messenger;

import java.util.HashMap;
import util.ConnectionUtillities;
import util.FileInfo;

/**
 *
 * @author uesr
 */
public class CreateClientConnection implements Runnable
{

    public ConnectionUtillities connection;


    public CreateClientConnection(ConnectionUtillities con)
    {
        connection=con;
    }
    
    @Override
    public void run() {
        Object o=connection.read();
        String username=o.toString();

        //checking multiple login
        if(Server.clientList.containsKey(username))
        {
           System.out.println("Sorry! You are already logged in!");
           connection.write("Sorry! You are already logged in!");

        }
        else
        {
            connection.write("Welcome to File Transfer!");
            Server.clientList.put(username, new Information(connection, username));
            new Thread(new ServerReaderWriter(username,connection)).start();
        }

        
    }
    
    
    
}
