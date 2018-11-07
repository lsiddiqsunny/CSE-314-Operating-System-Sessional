import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

/**
 * Created by Ashiqur Rahman on 9/19/2017.
 */
public class Datastreams {
    DataInputStream din;
    DataOutputStream dout;
    String state;

    public Datastreams(DataInputStream din, DataOutputStream dout, String state) {
        this.din = din;
        this.dout = dout;
        this.state=state;
    }
    void change()
    {
        if(this.state.equals("0"))this.state="1";
        else this.state="0";
    }
}
