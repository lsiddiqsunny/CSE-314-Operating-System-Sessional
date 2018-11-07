import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by Ashiqur Rahman on 9/19/2017.
 */
public class Confirmation {

    public Confirmation() {
    }

    boolean Valid (Socket socket) {
        DataOutputStream dout;
        DataInputStream din;

        try {
            dout= new DataOutputStream(socket.getOutputStream());
            din= new DataInputStream(socket.getInputStream());

            String id=din.readUTF();
            if(Start.studentList.containsKey(id))
            {
                dout.writeUTF("idnotconfirmed");
                dout.flush();
                return  false;
            }else
            {
                dout.writeUTF("idconfirmed");
                dout.flush();
                new Transfer(dout,din,socket,id);
                Start.studentList.put(id,new Datastreams(din,dout,"0"));
                return true;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


        return false;
    }



}
