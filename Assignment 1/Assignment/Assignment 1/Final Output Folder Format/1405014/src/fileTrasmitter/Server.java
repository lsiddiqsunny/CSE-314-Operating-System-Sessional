/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fileTrasmitter;

import transmissionUtilities.ClientInformation;
import transmissionUtilities.ConnectionUtilities;
import transmissionUtilities.CreateClientConnection;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Server
{

    private Server (int port)
    {

        HashMap<String, ClientInformation> clientList = new HashMap<> ();

        try
        {
            ServerSocket serverSocket = new ServerSocket (port);
            System.out.println("File transmission server started.");
            
            while(true)
            {
                Socket clientSocket= serverSocket.accept();
                ConnectionUtilities connection = new ConnectionUtilities(clientSocket);
                new Thread(new CreateClientConnection(clientList,connection)).start();
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        
    }

    public static void main(String[] args) {
        new Server(22222);
    }

}
