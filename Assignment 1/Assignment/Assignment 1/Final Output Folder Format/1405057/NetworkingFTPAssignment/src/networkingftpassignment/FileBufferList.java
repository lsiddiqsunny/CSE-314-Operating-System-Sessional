package networkingftpassignment;

/**
 *
 * @author HP
 */

public class FileBufferList {
    String senderStudentId;
    String receivertStudentId;
    String fileName;
    long fileSize;
    String fileId;

    public FileBufferList(String senderStudentId, String receivertStudentId, String fileName, long fileSize, String fileId) {
        this.senderStudentId = senderStudentId;
        this.receivertStudentId = receivertStudentId;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fileId = fileId;
    }

    public String getSenderStudentId() {
        return senderStudentId;
    }

    public void setSenderStudentId(String senderStudentId) {
        this.senderStudentId = senderStudentId;
    }

    public String getReceivertStudentId() {
        return receivertStudentId;
    }

    public void setReceivertStudentId(String receivertStudentId) {
        this.receivertStudentId = receivertStudentId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }
    
}
