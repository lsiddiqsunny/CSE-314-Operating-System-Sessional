package Client;

import javafx.util.Pair;
import java.io.File;
import java.util.Scanner;

public class InputThread implements Runnable{
    private Thread t;
    private Client CLIENT;

    public InputThread(Client CLIENT) {
        t=new Thread(this);
        this.CLIENT=CLIENT;
        t.start();
    }

    @Override
    public void run() {
        while (true) {
            //Taking Reciever User Name as Input
            System.out.println("Please Enter Recipient Name");
            String Recipient = new Scanner(System.in).nextLine();

            //Taking File Path as Input
            System.out.println("Please Enter File Path");
            String FilePath = new Scanner(System.in).nextLine();
            File FILE = new File(FilePath);
            if(!FILE.exists()) System.out.println("Error : Invalid File Path");
            else CLIENT.TobeSent.add(new Pair<>(Recipient,FilePath));
        }
    }

}
