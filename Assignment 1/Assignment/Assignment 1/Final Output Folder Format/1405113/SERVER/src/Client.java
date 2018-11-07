import java.net.Socket;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Created by Md.SaifulAlam on 10/1/2017.
 */
public class Client {
    int id;
    Socket socket;
    String filename;
    int message;
    int fileSize;
    int start_index;
    int end_index;
    int student_id_who_send;
    Vector<Obj> v;
    int total_file;
    int file_id;


    public Client(int id,Socket socket){
        this.id = id;
        this.socket = socket;
        this.message=0;
        this.start_index=0;
        this.end_index=0;
        this.fileSize=0;
        this.filename=null;
        v=new Vector<>();
        total_file=0;
        file_id=0;

        //file_arr=new int[100][];
    }
}
