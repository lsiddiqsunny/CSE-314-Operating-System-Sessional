package util;

public class FileInfo
{
    public int fileId;
    public String mainFile;
    public String tempFile;
    public String sender;
    public String receiver;
    public int fileSize;
    public int supportedMaxChunk;


    public FileInfo(int fileId, String filename, String tempFileName, String sender, String receiver, int fileSize, int supportedMaxChunk)
    {
        this.fileId = fileId;
        this.mainFile = filename;
        this.tempFile = tempFileName;
        this.sender = sender;
        this.receiver = receiver;
        this.fileSize = fileSize;
        this.supportedMaxChunk = supportedMaxChunk;
    }



}
