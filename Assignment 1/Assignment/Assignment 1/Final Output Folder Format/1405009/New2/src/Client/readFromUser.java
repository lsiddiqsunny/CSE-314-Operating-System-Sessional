package Client;

import Utilities.ClientUtilities;

import java.io.IOException;

/**
 * Created by Rupak on 10/16/2017.
 */
public class readFromUser implements Runnable{
    ClientUtilities Client;

    readFromUser(ClientUtilities c)
    {
        Client = c;
    }

    @Override
    public void run() {
        try {
            String sx = Client.inFromUser.readLine();
            int x = Integer.parseInt(sx);
            if (x == 1) {
                Client.outToServer.writeByte(1);
                System.out.print("Enter RECEIVER ID :");
                String sID = Client.inFromUser.readLine();
                Client.outToServer.writeBytes(sID + "\n");
            } else if (x == 2) {
                Client.outToServer.writeByte(2);
                //Client.outToServer.writeByte(5);
            } else
                Client.outToServer.writeByte(6);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}