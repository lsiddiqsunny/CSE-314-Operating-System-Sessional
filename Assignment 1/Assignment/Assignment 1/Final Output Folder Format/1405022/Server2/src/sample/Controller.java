package sample;

import javafx.scene.control.TextField;

public class Controller {

    private Main main;

    public TextField SizeOfChunks;


    public void setMain(Main m){
        main = m;
    }


    public void ConfigureBtnPressed(){
        String chunk = SizeOfChunks.getText().toString();
        int s = Integer.parseInt(chunk);
        System.out.println("Size of chunk "+s);

        main.setSizeOfChunks(s);
        main.setPresentBufferSize(0);

        try {
            main.showSecondPage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




}
