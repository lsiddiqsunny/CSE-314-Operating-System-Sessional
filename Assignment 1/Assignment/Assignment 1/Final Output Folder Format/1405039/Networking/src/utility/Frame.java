package utility;
import java.io.Serializable;


public class Frame implements Serializable {
    
    public char flagbit1;
    public char kind;
    public int seqNo;
    public int ackNo;
    public byte[] payload;
    public int checksum;
    public char flagbit2;
    
    public Frame()
    {
        flagbit1 = '~';
        flagbit2 = '~';
    }
    
    public Frame(char k, int seqack, byte[] pl, int checks)
    {
        flagbit1 = '~';
        kind = k;
        if(k == 'a') {
            ackNo=seqack;
        }
        else if(k == 'd'){
            seqNo = seqack;
        }
        
        payload = new byte[pl.length];
        for(int i = 0; i < pl.length; i++)
        {
            payload[i] = pl[i];
        }
        checksum = checks;
        flagbit2 = '~';
    }
    
    public int getSize()
    {
        int frameSize = 1 + 1 + 1 + 1 + payload.length;
        return frameSize;
    }
    
    public int getCheckSum()
    {
        return checksum;
    }
    
    public byte[] getPayload()
    {
        return payload;
    }
    
    public void setPayload(byte[] pl)
    {
        payload = pl;
    }
}
