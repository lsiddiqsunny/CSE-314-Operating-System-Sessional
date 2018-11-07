package client;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.omg.PortableServer.THREAD_POLICY_ID;
import util.NetworkUtil;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by user on 9/21/2017.
 */
public class readThreadClient implements Runnable{

    private Thread thr;
    private NetworkUtil nc;
    private Helper help = new Helper();
    //private LogInController lc;
    public readThreadClient(NetworkUtil nc){
        this.nc = nc;
        //this.lc = lc;
        thr = new Thread(this);
        thr.start();
    }

    public void run(){
        while(true){
            //System.out.println("hoho");
            while(help.helper == true){}

            //System.out.println("age");
            Object oo = nc.read();
            //System.out.println("pore");
            if(oo==null){
                continue;
            }
            //System.out.println(oo);
            int sender = (int)oo;
            //System.out.println(sender);
            String fileName = (String)nc.read();
            long fileSize = (long)nc.read();
            help.helper = true;
            //System.out.println(sender + fileName + fileSize);

            //System.out.println("haha");
            //nc.write("a");
            /*try{
                wait();

            }
            catch (Exception e){

            }*/
            Platform.runLater(new Runnable() {
                @Override public void run() {
                    Stage primaryStage = new Stage();
                    //primaryStage.setTitle(Integer.toString(clientID));

                    FXMLLoader loader = new FXMLLoader(
                            getClass().getResource(
                                    "receiveFile.fxml"
                            )
                    );
                    try {
                        primaryStage.setScene(
                                new Scene(
                                        loader.load()
                                )
                        );
                        receiveFileController controller =
                                loader.<receiveFileController>getController();
                        controller.initdata(help,nc,sender,fileSize,fileName);
                    }
                    catch (Exception e){

                    }

                    primaryStage.show();
                }
            });

            for(int i=000;i<1000;i++){
                for(int j=000;j<1000;j++){
                    for(int k=000;k<1000;k++);
                }
            }

        }
    }
}
