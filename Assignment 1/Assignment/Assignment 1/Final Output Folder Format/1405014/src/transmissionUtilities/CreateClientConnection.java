/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package transmissionUtilities;

import fileTrasmitter.ServerReaderWriter;

import java.util.HashMap;

public class CreateClientConnection implements Runnable
{
    public HashMap<String, ClientInformation> clientList;
    public ConnectionUtilities connection;

    public CreateClientConnection(HashMap<String, ClientInformation> list, ConnectionUtilities con)
    {
        clientList = list;
        connection = con;
    }
    
    @Override
    public void run()
    {
        String information = connection.readString();
        if (information.equals ("disconnect"))
        {
            System.out.println ("User has disconnected from server.");
            return;
        }

        if (!clientList.containsKey(information) || (clientList.containsKey(information) && !clientList.get(information).isLoggedIn))
        {
            if (!clientList.containsKey(information)) clientList.put(information, new ClientInformation(connection, information));
            connection.writeString("Doesn't exist");
            System.out.println("User named " + information + " has joined.");
            connection.writeString("Server welcomes " + information);
            new Thread(new ServerReaderWriter(information, connection, clientList)).start();
        }
        else
        {
            connection.writeString("Exists");
            System.out.println("User named " + information + " is already logged in.");
        }
    }

}
