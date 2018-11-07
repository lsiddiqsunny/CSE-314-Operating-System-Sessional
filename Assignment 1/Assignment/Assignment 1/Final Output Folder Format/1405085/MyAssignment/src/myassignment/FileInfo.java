
package myassignment;

import java.io.File;
import java.io.Serializable;

public class FileInfo implements Serializable {

    String from;
    String to;
    String file;
    String fileId;
    long length;

    public FileInfo(String from, String to, String fileName) {
        this.from = from;
        this.to = to;
        file = fileName;
        File f = new File(fileName);
        length = f.length();

    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getFile() {
        return file;
    }

    public long getLength() {
        return length;
    }

}