package Student;

import Tools.NetworkUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.Optional;

/**
 * Created by Toufik on 9/22/2017.
 */
public class StudentLogInController {
    @FXML
    public Button fileChooser;

    @FXML
    public AnchorPane Anchor1;

    @FXML
    public AnchorPane Anchor2;

    @FXML
    public Button Disconnect;

    @FXML
    public TextField port;

    @FXML
    public TextArea log;

    @FXML
    public TextField ip;

    @FXML
    public TextField filePath;

    @FXML
    public TextField Receiver;

    @FXML
    public TextField ID;

    @FXML
    public Button connect;

    @FXML
    public Text iptext;

    @FXML
    public Text portText;

    @FXML
    public Button send;

    NetworkUtil nu;

    @FXML
    void ConnectClicked(ActionEvent e) throws IOException {
        NetworkUtil netUtil = new NetworkUtil(ip.getText(),Integer.parseInt(port.getText()));
        nu=netUtil;
        netUtil.write(ID.getText());
        String read;
        read = (String) netUtil.read();
        if(read.equals("Successfully Connected"))
        {
            ID.setEditable(false);
            Anchor2.setVisible(true);
            ip.setVisible(false);
            iptext.setVisible(false);
            port.setVisible(false);
            portText.setVisible(false);
            connect.setVisible(false);
            log.appendText(read+"\n");
            new WorkingThread(netUtil,this);
        }
        else
        {
            log.appendText(read+"\n");
            netUtil.closeConnection();
        }
    }

    @FXML
    public void ChooseFile(ActionEvent e)
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File to be sent");
        File file = fileChooser.showOpenDialog(StudentMain.window);
        if(file!=null)
        {
            String path = file.getPath();
            filePath.setText(path);
        }
    }

    @FXML
    public void sendClicked(ActionEvent e)
    {
        File file = new File(filePath.getText());
        if(!file.exists()) log.appendText("Select a valid file\n");
        if(Receiver.getText().equals("")) log.appendText("Input receiver ID\n");
        else
        {
            String msz = "receive";
            nu.write(msz);
        }
    }

    @FXML
    public void DisconnectClicked(ActionEvent e)
    {
        String str= "cDisconnect";
        nu.write(str);
        ID.setEditable(true);
        Anchor2.setVisible(false);
        ip.setVisible(true);
        iptext.setVisible(true);
        port.setVisible(true);
        portText.setVisible(true);
        connect.setVisible(true);
        log.appendText("You are logged out\n");
    }

}
