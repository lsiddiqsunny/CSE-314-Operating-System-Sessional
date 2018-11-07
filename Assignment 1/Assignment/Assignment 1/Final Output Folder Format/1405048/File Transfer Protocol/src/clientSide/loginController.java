package clientSide;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import util.ConnectionUtilities;
import util.StringConstants;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class loginController implements Initializable {
    @FXML
    TextField studentIDField;
    @FXML
    Label alertLabel;
    @FXML
    Button signInButton;

    String studentId = "", emptyBoxText = "Please input Student ID", mulLogText = "Please log out from other devices";
    ConnectionUtilities senderConnection,receiverConnection;

    public loginController() {
        senderConnection=new ConnectionUtilities("127.0.0.1",22222);
        receiverConnection = new ConnectionUtilities("127.0.0.1",33333);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        alertLabel.setVisible(false);
    }

    public void closeStage(){
        Stage loginStage = (Stage) signInButton.getScene().getWindow();
        String path = "D:\\Google Drive\\Programming\\Java\\File Transfer Protocol\\src\\clientSide\\clientPage.fxml";
        File file = new File("D:\\Google Drive\\Programming\\Java\\File Transfer Protocol\\src\\clientSide\\clientPage.fxml");
        if(file.isFile()){
//            System.out.println("File is File");
        }
        else{
            System.out.println("ERROR");
        }
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("clientPage.fxml"));

        if(getClass().getResource("clientPage.fxml") == null){
//            System.out.println("NUUULLL");
        }
        if(getClass().getResource(path) == null){
//            System.out.println("REsoucrce e nullllllllll");
        }
        Sender sender = new Sender(senderConnection,receiverConnection);
        Receiver receiver = new Receiver(receiverConnection);
        try {
            senderConnection.write(sender);
            Parent root = fxmlLoader.load();
            ClientPageController clientPageController = fxmlLoader.getController();

            clientPageController.setSender(sender);
            clientPageController.setReceiver(receiver);
            new Thread(receiver).start();

            Scene scene = new Scene(root);
            Stage clientPageStage = new Stage();
            clientPageStage.setTitle("File Transfer");
            clientPageStage.setScene(scene);
            loginStage.close();
            clientPageStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        while (true); //How about commenting out this ?
    }

    @FXML
    public void signInButtonClick(ActionEvent event){
        String doExist = null;
        studentId = studentIDField.getText();
        if(studentId.equals("")){
            alertLabel.setVisible(true);
            alertLabel.setText(emptyBoxText);
            return;
        }
        try {
            senderConnection.write(studentId);
            doExist = (String) senderConnection.read();
            if(doExist.equals(StringConstants.ID_EXISTS)){
                alertLabel.setVisible(true);
                alertLabel.setText(mulLogText);
                return;
            }
            receiverConnection.studentID = senderConnection.studentID = studentId;
            closeStage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
