package Server;

/**
 * Created by Rupak on 9/29/2017.
 */

public class FileTransmission {
    public int receiver;
    public String fileName;
    public int fileID;
    public int fileSize;
    public int chunkSize;
    public int received;
    public int chunkNumber;
    public int leftOut;
    byte[][] data;


    public FileTransmission(int receiver, String fileName, int fileID, int fileSize, int chunkSize) {
        this.receiver = receiver;
        this.fileName = fileName;
        this.fileID = fileID;
        this.fileSize = fileSize;
        this.chunkSize = chunkSize;
        received=0;
        chunkNumber = fileSize/chunkSize;
        leftOut = fileSize % chunkSize;
        data = new byte[chunkNumber+1][chunkSize];
    }

    public boolean add(byte[] array,int size)
    {
        if(received>chunkNumber)return false;
        for(int i=0;i<size;i++)
        {
            data[received][i]=array[i];
        }
        received++;
        return true;
    }
}
