package client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.stage.Stage;
import sun.nio.ch.Net;
import util.NetworkUtil;

import java.awt.*;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by user on 10/15/2017.
 */
public class receiveFileController {

    private long fileSize;
    NetworkUtil nc;
    private Helper rc;
    private int sender;
    private String fileName;
    @FXML private javafx.scene.control.Label msg;
    @FXML private javafx.scene.control.TextField path;
    @FXML private javafx.scene.control.Button accept;
    @FXML private javafx.scene.control.Button deny;

    public void yes(ActionEvent Event) {
        Stage stage = (Stage) accept.getScene().getWindow();
        nc.write("a");

        byte[] data = new byte[(int)fileSize];
        nc.readByte(data);
        //System.out.println(path.getText().toString());
        try {
            FileOutputStream fos = new FileOutputStream(path.getText().toString());

            fos.write(data);
            fos.close();
        }
        catch (FileNotFoundException e)
        {

        }
        catch (IOException e) {

        }
        rc.helper = false;

        // do what you have to do
        stage.close();
    }

    public void no(ActionEvent Event) {
        Stage stage = (Stage) deny.getScene().getWindow();
        nc.write("z");
        // do what you have to do
        rc.helper = false;
        //while(Helper.helper!=false)Helper.helper = false;
        //System.out.println(Helper.helper);
        stage.close();

    }

    public void initdata(Helper rc , NetworkUtil nc,int sender,long fileSize,String fileName){
        this.fileName = fileName;
        this.sender = sender;
        this.rc = rc;
        this.nc = nc;
        this.fileSize = fileSize;
        msg.setText("Hi, "+sender+" wants to send you a file named "+fileName+" and size \n"+fileSize+" bytes. Do you want to accpet?");
    }
}
