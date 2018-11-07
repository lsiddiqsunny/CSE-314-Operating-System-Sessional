package Utility;

import java.io.Serializable;

/**
 * Created by Asus on 9/27/2017.
 */
public class recievedFileInfo implements Serializable {
  long fileId;
  String filename;
  long filesize;
  int senderId;

  public recievedFileInfo(long fileId, String filename, long filesize, int senderId) {
    this.fileId = fileId;
    this.filename = filename;
    this.filesize = filesize;
    this.senderId = senderId;
  }

  public long getFileId() {
    return fileId;
  }

  public String getFilename() {
    return filename;
  }

  public long getFilesize() {
    return filesize;
  }

  public int getSenderId() {
    return senderId;
  }

}
