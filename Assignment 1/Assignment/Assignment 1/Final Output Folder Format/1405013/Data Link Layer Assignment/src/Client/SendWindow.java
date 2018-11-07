package Client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by rafid on 20/10/2017.
 */
public class SendWindow implements Initializable {
    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @FXML
    private TextField receiverIDText;

    @FXML
    public void submitReceiver(ActionEvent event) {
        Node source = (Node)event.getSource();
        Stage stage = (Stage)source.getScene().getWindow();
        stage.close();
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(Main.getWindow());
        Main.getClient().sendFile(receiverIDText.getText(), file);
    }
}
