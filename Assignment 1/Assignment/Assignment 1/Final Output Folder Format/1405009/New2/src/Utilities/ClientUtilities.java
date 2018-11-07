package Utilities;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Created by Rupak on 9/21/2017.
 */
public class ClientUtilities {
    public Socket socket;
    public BufferedReader inFromUser;
    public DataOutputStream outToServer;
    public BufferedReader inFromServer;

    public ClientUtilities(Socket sc) throws IOException {
        socket = sc;
        inFromUser = new BufferedReader(new InputStreamReader(System.in)); //user input
        outToServer = new DataOutputStream(socket.getOutputStream()); //sendServer to receiveServer
        inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream())); //receiveServer to sendServer
    }
}
