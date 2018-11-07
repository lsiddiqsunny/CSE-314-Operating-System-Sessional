import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Ashiqur Rahman on 9/23/2017.
 */
public class Checker implements  Runnable{
    Thread thread;

    public Checker() {
        thread=new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        while(true)
        {
            for(Iterator<Map.Entry<String, Datastreams>> it = Start.studentList.entrySet().iterator()
                ; it.hasNext();)
            {
                Map.Entry<String, Datastreams> i=it.next();

                try {
                    if(i.getValue().state.equals("1")) {
                        i.getValue().dout.writeUTF("111");
                        i.getValue().dout.flush();
                    }

                } catch (IOException e) {
                   // e.printStackTrace();
                    System.out.println(i.getKey()+" is offline");
                    it.remove();
                }
            }
            try {
                thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }


    }
}
