package filetransmission;


public class frame {
    public int type;
    public int seq_no;
    public int ack_no;
    public byte[] contents;
    public int bytesread;
    public String checksum;
    
    frame(){ }
}
