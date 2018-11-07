package Server;

/**
 * Created by Toufik on 9/21/2017.
 */

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import java.io.IOException;

public class ServerGUIController {

    @FXML
    private TextField BufferSize;

    @FXML
    private Button StartButton;

    @FXML
    private TextArea log;

    @FXML
    public void StartServer(ActionEvent actionEvent) {
        if(BufferSize.getText().equals(""))
        {
            ServerGUIController1.bufferSize = 10000;
            try {
                ServerMain.root = FXMLLoader.load(getClass().getResource("ServerGUI1.fxml"));
                ServerMain.window.setScene(new Scene(ServerMain.root));
            } catch (IOException ioe) {

            }
        }
        else
        {
            try {
                int buffer= Integer.parseInt(BufferSize.getText());
                ServerGUIController1.bufferSize = buffer;
                try {
                    ServerMain.root = FXMLLoader.load(getClass().getResource("ServerGUI1.fxml"));
                    ServerMain.window.setScene(new Scene(ServerMain.root));
                } catch (IOException ioe) {

                }
            } catch (Exception exp) {
                log.appendText("Please provide valid buffer size\n");
            }

        }
    }
}