package prototype.commons;


public class ReceiverInfo{
    public String fileName; public int senderid, receiverid;
    public int fileID; public long fileSize;

    public ReceiverInfo(){}
    public ReceiverInfo(int sid, int rid,String s, int fileID, long fileSize)
    {
        this.fileID = fileID; fileName = s;
        this.senderid = sid; this.receiverid = rid; this.fileSize = fileSize;
    }
}