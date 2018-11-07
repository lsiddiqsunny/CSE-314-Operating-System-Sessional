/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package serverSide;





import clientSide.Sender;
import util.ConnectionUtilities;
import util.Information;
import util.StringConstants;

import java.util.HashMap;

/**
 *
 * @author Antu
 */
public class CreateClientConnection implements  Runnable{
    public HashMap<String, Information> clientList;
    public ConnectionUtilities senderConnection, receiverConnection;


    public CreateClientConnection(HashMap<String,Information> list, ConnectionUtilities con,
                                  ConnectionUtilities receiverConnection){
        clientList=list;
        this.senderConnection = con;
        this.receiverConnection = receiverConnection;
    }
    
    @Override
    public void run() {
        String studentID = "";
        try {
            while (true) {
                Object o = senderConnection.read();
                studentID = o.toString();
                System.out.println(studentID);
                if (clientList.containsKey(studentID)) {
                    senderConnection.write(StringConstants.ID_EXISTS);
                } else {
                    senderConnection.write(StringConstants.ID_DOES_NOT_EXIST);
                    break;
                }
            }
            //Will have to add IP address too.
            senderConnection.studentID = receiverConnection.studentID = studentID;
            Sender clientSender = (Sender) senderConnection.read();
            Information clientInformation = new Information(senderConnection, receiverConnection, studentID, clientSender);
            clientList.put(studentID, clientInformation);
            new Thread(new ServerReaderWriter(clientInformation,clientList)).start();
        }catch (Exception e){
            System.out.println("Client Thread terminated");
        }
    }
    
    
    
}
