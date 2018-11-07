package sample;

import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class login {

    public login(Stage s) {
        open(s);
    }

    void open(Stage stage)
    {
        Button Login=new Button("Login");
        Login.setMaxSize(150, 40);
        Login.setLayoutX(220);
        Login.setLayoutY(150);

        final TextField input_id=new TextField();
        input_id.setMaxSize(250, 100);
        input_id.setLayoutX(100);
        input_id.setLayoutY(80);

        Pane rootpane=new Pane();
        rootpane.getChildren().add(0, input_id);
        rootpane.getChildren().add(1, Login);
        Scene scene=new Scene(rootpane,500,350);
        stage.setTitle("File Transmission");
        stage.setScene(scene);
        stage.show();


        input_id.setOnKeyReleased(new EventHandler<KeyEvent>() {//this event handles id type.. Id must be a number

            @Override
            public void handle(KeyEvent t) {
                if(!input_id.getText().matches("[0-9]*"))
                {
                    input_id.setText(input_id.getText().replaceAll("[^\\d]", ""));
                    input_id.selectPositionCaret(input_id.getLength());input_id.deselect();
                }
            }
        });

        Login.setOnMouseClicked(new EventHandler<MouseEvent>() {//this event handles log in.

            @Override
            public void handle(MouseEvent t) {
                if(!input_id.getText().isEmpty())
                {
                    stage.close();
                    new FileTransmission().start(input_id.getText());
                }
            }
        });
    }


}