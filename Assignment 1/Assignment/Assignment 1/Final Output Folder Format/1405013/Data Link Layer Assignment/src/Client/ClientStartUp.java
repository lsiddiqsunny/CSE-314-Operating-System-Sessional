package Client;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;


import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by rafid on 19/10/2017.
 */
public class ClientStartUp implements Initializable {
    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
    @FXML
    TextField textBoxID;
    @FXML
    Label label;
    @FXML
    public void connect() throws Exception{
        boolean connected = Main.getClient().connect(textBoxID.getText());
        if(connected) {
            Parent root = FXMLLoader.load(getClass().getResource("ClientLogWindow.fxml"));
            Main.getWindow().setOnCloseRequest(event -> Main.getClient().closeConnection());
            Main.getWindow().setScene(new Scene(root, 640, 480));
            Main.getWindow().show();
        }
        else label.setVisible(true);
    }
}
