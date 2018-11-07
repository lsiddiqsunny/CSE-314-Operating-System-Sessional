import java.io.Serializable;

public class FileDescription implements Serializable
{
    public String sender;
    public String recepient;
    public String name;
    public int FileSize;
    public FileDescription(String sender, String recepient, String name, int FileSize)
    {
        this.sender=sender;
        this.recepient=recepient;
        this.name=name;
        this.FileSize=FileSize;
    }
}
