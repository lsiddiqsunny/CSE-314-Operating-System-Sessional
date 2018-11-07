package Server;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by rafid on 20/10/2017.
 */
public class ServerStartUp implements Initializable {
    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @FXML
    public void startServer() throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("ServerLogWindow.fxml"));
        Main.getWindow().setScene(new Scene(root, 640, 480));
        Main.getWindow().show();
    }

}
