package ServerSide;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class ConnectionInfo implements Serializable {
    ObjectInputStream inputStream;
    ObjectOutputStream outputStream;
    String ID;


    public ConnectionInfo(ObjectInputStream inputStream, ObjectOutputStream outputStream, String senderID) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.ID = senderID;

    }
}
