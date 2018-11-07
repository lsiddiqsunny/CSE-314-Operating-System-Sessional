package server;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import utility.ConnectionSetup;


public class LoginClient implements Runnable{
    
    public ConnectionSetup connection;
    
    public LoginClient(ConnectionSetup conn)
    {
        connection = conn;
    }

    public void run() {
        try {
            String username = (String)connection.read();
            if(Server.clientList.containsKey(username))
            {
                String msg = "false" ;
                connection.write((Object)msg);
            }
            else
            {
                Server.clientList.put(username, connection);
                String msg = "true" ;
                connection.write((Object)msg);
                new Thread(new ServerReadWrite(connection, username)).start();
            }
        } catch (Exception ex) {
            System.out.println("Error in login client.");
        }
    }
}
