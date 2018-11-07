package Client;

import UI.LogWindow;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by rafid on 20/10/2017.
 */
public class ClientLogWindow implements Initializable, LogWindow {
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Main.getClient().setClientLogWindow(this);
    }
    @FXML
    private ListView<String> clientLog;

    @FXML
    private ListView<String> frameLog;

    @Override
    public void appendToLog(String text) {
        Platform.runLater(()->{
            ObservableList<String> temp = clientLog.getItems();
            temp.add(text);
            clientLog.setItems(temp);
        });
    }

    @Override
    public void appendToFrameLog(String text) {
        Platform.runLater(()->{
            ObservableList<String> temp = frameLog.getItems();
            temp.add(text);
            frameLog.setItems(temp);
        });
    }

    @FXML
    public void send() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("SendWindow.fxml"));
        Scene scene = new Scene(root, 198, 106);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.show();
    }
    @FXML
    public void receive() throws IOException {
        Main.getClient().receiveFile();
    }
    @FXML
    public void exit(ActionEvent event) {
        Main.getClient().closeConnection();
        Node source = (Node)event.getSource();
        Stage stage = (Stage)source.getScene().getWindow();
        stage.close();
    }
}
