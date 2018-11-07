package sample;

import javafx.scene.control.TextField;

import java.net.Inet4Address;
import java.net.UnknownHostException;

public class Controller {

    private Main main;

    public TextField StudentId;


    public void setMain(Main m){
        main = m;
    }

    public void LogInBtnClicked() throws UnknownHostException {

        String Id = StudentId.getText().toString();
        int i = Integer.parseInt(Id);
        //System.out.println("Student id: "+i);

        main.setStudentId(Id);
        main.setIpAddress(Inet4Address.getLocalHost().getHostAddress());

        try {
            main.showSecondPage();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }




}
