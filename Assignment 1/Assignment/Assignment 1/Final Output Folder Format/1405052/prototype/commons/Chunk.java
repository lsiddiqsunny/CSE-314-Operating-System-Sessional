package prototype.commons;

import java.io.Serializable;

public class Chunk implements Serializable {
    public byte[] buff;
    public String chunkName;

    public Chunk(){}

    public Chunk(String chunkName, byte[] buff){
        this.chunkName = chunkName;
        this.buff = buff;
    }
}