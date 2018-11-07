package Helper;

        import java.io.Serializable;

/**
 * Created by user on 10/26/2017.
 */
public class ByteAsObject implements Serializable {
    public byte[] buf;
    public ByteAsObject(byte[] buf){
        this.buf = buf;
    }
}
