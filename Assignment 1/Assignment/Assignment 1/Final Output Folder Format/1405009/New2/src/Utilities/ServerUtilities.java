package Utilities;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Created by Rupak on 9/21/2017.
 */
public class ServerUtilities {
    public Socket socket;
    public DataOutputStream outFromServer;
    public BufferedReader inToServer;


    public ServerUtilities(Socket sc) throws IOException {
        socket = sc;
        outFromServer = new DataOutputStream(socket.getOutputStream()); //to sendServer from receiveServer
        inToServer = new BufferedReader(new InputStreamReader(socket.getInputStream())); //to receiveServer from sendServer
    }
}
