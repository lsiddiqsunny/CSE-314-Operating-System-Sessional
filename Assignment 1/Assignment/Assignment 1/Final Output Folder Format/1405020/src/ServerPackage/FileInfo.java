package ServerPackage;

/**
 * Created by Asus on 9/26/2017.
 */
public class FileInfo {
    long fileId;
    String fileName;
    long fileSize;
    int senderId;
    int recieverId;
    int status;
    long maxchunksize;
    long storedsize;
    String fileInServer;
    int currSeqNo;

    public FileInfo(long fileId, String fileName, long fileSize, int senderId, int recieverId, long maxchunksize) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.senderId = senderId;
        this.recieverId = recieverId;
        this.status = 0;
        this.maxchunksize = maxchunksize;
        this.storedsize = 0;
        fileInServer=fileId+"_"+fileName;
        this.currSeqNo=0;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getStoredsize() {
        return storedsize;
    }

    public String getFileName() {
        return fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public int getSenderId() {
        return senderId;
    }

    public int getRecieverId() {
        return recieverId;
    }

    public long getMaxchunksize() {
        return maxchunksize;
    }

    public void setStoredsize(long storedsize) {
        this.storedsize = storedsize;
    }


}
