import java.util.ArrayList;
import java.util.HashMap;

public class ServerStorage
{
    HashMap<String,ConnectionUtilities> ClientList;
    FileDescription fileDescription;
    int serverSize=10*1024*1024;
    HashMap<String,ArrayList<Chunk>> files;
    HashMap<String,String> recepientList;
    HashMap<String,FileDescription> fileDescriptionHashMap =new HashMap<String, FileDescription>();


    ArrayList<Integer> ChunkSizes=new ArrayList<Integer>();
    public ServerStorage(ConnectionUtilities connectionUtilities,HashMap<String,ConnectionUtilities> ClientList)
    {
        //this.connectionUtilities=connectionUtilities;

    }

}
