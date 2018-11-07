package Server;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Toufik on 9/21/2017.
 */
public class ServerGUIController1 implements Initializable
{
   public static int bufferSize;
   @FXML
   public Button StopButton;

   @FXML
   public TextArea log;

   @FXML
   public TextField IPtext;

   @FXML
   public TextField port;

   @Override
   public void initialize(URL location, ResourceBundle resources) {
      try {
         new ServerThread(this);
         log.appendText("Server Started with Buffer Size: "+bufferSize+" KB\n");
      } catch (IOException e) {
         //e.printStackTrace();
         log.appendText("Server start failed\n");
      }
   }
}
