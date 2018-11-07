
public class MultiThread implements Runnable
{
    Thread t;
    String name;
    MultiThread(String ThreadName)
    {
        name=ThreadName;
        t=new Thread(this,name);
        System.out.println("New Thread: "+ t );
        t.start();
    }
    @Override
    public void run() {
        for (int i = 0; i <10 ; i++) {
            System.out.println(name+" printling "+ i);
            try {
                t.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}