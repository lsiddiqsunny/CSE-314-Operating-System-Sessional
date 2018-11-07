import java.io.Serializable;

public class extra implements Serializable
{
    int chunkCount;
    int complete;
    extra(int chunkCount,int complete)
    {
        this.chunkCount=chunkCount;
        this.complete=complete;
    }
}
