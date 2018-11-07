package Server;

import UI.LogWindow;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by rafid on 20/10/2017.
 */
public class ServerLogWindow implements Initializable, LogWindow {
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        new Server(this);
    }

    @FXML
    private ListView<String> serverLog;

    @FXML
    private ListView<String> frameLog;

    @Override
    public void appendToLog(String text) {
        Platform.runLater(()->{
            ObservableList<String> temp = serverLog.getItems();
            temp.add(text);
            serverLog.setItems(temp);
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
}
