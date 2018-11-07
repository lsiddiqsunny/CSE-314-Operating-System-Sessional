package sample;

import javafx.application.Platform;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Created by DELL on 21-Sep-17.
 */
public class ClientThread implements Runnable {

    private Main main;
    private SecondPage secondPage;

    public int unique;

    public String StudentId;

    public Socket socket;
    public ObjectInputStream ois;
    public ObjectOutputStream oos;

    public ClientThread(Main m, Socket s, SecondPage sp, int un) throws IOException {
        main = m;
        secondPage = sp;
        unique = un;

        socket = s;
        oos = new ObjectOutputStream(socket.getOutputStream());
        ois = new ObjectInputStream(socket.getInputStream());

        Thread t = new Thread(this);
        t.start();
    }


    @Override
    public void run() {
        try {
            String rec = (String) ois.readObject();
            //System.out.println("rec is "+rec);

            if(!secondPage.StudentIds.contains(rec)){
                oos.writeObject("Successful Login");
                StudentId = rec;
                secondPage.StudentIds.add(rec);


                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        secondPage.OnlineList.getItems().add(StudentId);
                    }
                });



                new ReceiveFromClientThread(main,this, secondPage);


            }
            else{
                oos.writeObject("Failure");
                CloseConnection();
            }






        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void CloseConnection() throws IOException {

        ois.close();
        oos.close();

    }
}
