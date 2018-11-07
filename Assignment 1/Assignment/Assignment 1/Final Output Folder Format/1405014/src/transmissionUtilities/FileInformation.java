package transmissionUtilities;

import java.io.File;

/**
 * Created by Rakib on 24-Oct-17.
 */
public class FileInformation {
    public File file;
    public int fileId;
    public String sender;
    private String receiver;

    public FileInformation(File file, int fileId, String sender, String receiver)
    {
        this.file = file;
        this.fileId = fileId;
        this.sender = sender;
        this.receiver = receiver;
    }
}
