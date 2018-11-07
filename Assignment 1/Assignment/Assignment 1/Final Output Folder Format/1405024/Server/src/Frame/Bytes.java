package Frame;

import java.io.Serializable;

/**
 * Created by ashiq on 10/16/17.
 */
public class Bytes implements Serializable {
    public boolean bytes[];

    public Bytes(boolean[] bytes) {
        this.bytes = bytes.clone();
    }

    public Bytes(int size) {
        bytes=new boolean[size];
    }

    public Bytes() {
        bytes=null;
    }
}
