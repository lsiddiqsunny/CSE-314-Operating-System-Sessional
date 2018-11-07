package clientSide;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import util.Information;


import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * @author ANTU on 01-Nov-17.
 * @project File Transfer Protocol
 */
public class ClientPageController implements Initializable {
    @FXML
    TextField studentIDField;
    @FXML
    TextField filePathField;
    @FXML
    Button sendButton;
    @FXML
    TextArea incomingField;
    @FXML
    Button yesButton;
    @FXML
    Button noButton;
    @FXML
    Label fillInfoLabel;
    @FXML
    Label noSuchStudentLabel;
    @FXML
    Label noSuchFileLabel;
    @FXML
    Label notEnoughSpaceLabel;
    @FXML
    Label sendingStatusLabel;
    @FXML
    Label receivingStatusLabel;
    @FXML
    Label askReceiverLabel;
    @FXML
    Label studentIDLabel;
    @FXML
    Button senderSideRefreshButton;
    @FXML
    Button receiverSideRefreshButton;
    @FXML
    Button logoutButton;
    @FXML
    RadioButton yesRadioButton;
    @FXML
    RadioButton noRadioButton;

    Sender sender;
    Receiver receiver;
    Information clientInformation;
    ToggleGroup radioButtonGroup;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sendingStatusLabel.setVisible(false);
        disableSenderSideVisibility();
        clearReceivingSide();
        receivingStatusLabel.setVisible(false);
        radioButtonManager();
    }

    public void radioButtonManager(){
        radioButtonGroup = new ToggleGroup();
        yesRadioButton.setToggleGroup(radioButtonGroup);
        noRadioButton.setToggleGroup(radioButtonGroup);
        noRadioButton.setSelected(true);
    }

    public void setSender(Sender sender){
        this.sender = sender;
        studentIDLabel.setText("Student ID : "+sender.connection.studentID);
    }

    public void setReceiver(Receiver receiver){
        this.receiver = receiver;
        receiver.incomingField = incomingField;
        receiver.clientPageController = this;
    }

    private void disableSenderSideVisibility(){
        fillInfoLabel.setVisible(false);
        noSuchFileLabel.setVisible(false);
        noSuchStudentLabel.setVisible(false);
        notEnoughSpaceLabel.setVisible(false);
//        sendingStatusLabel.setVisible(false);
    }

    @FXML
    public void onLogoutButtonClick(ActionEvent event){
        System.exit(0);
    }

    @FXML
    public void onSendButtonClick(ActionEvent event){
        String studentID, filePath;
        boolean enableRandomLostFrame;
        studentID = studentIDField.getText();
        filePath = filePathField.getText();
        disableSenderSideVisibility();
        if(yesRadioButton.isSelected()){
            enableRandomLostFrame = true;
        }
        else{
            enableRandomLostFrame = false;
        }
        if(studentID.equals("") || filePath.equals("")){
            fillInfoLabel.setVisible(true);
            return;
        }
        File toBeSentFile = new File(filePath);
        if(!toBeSentFile.isFile()){
            noSuchFileLabel.setVisible(true);
            return;
        }
        if(!sender.idDoesNotExist(studentID)){
            noSuchStudentLabel.setVisible(true);
            return;
        }
        if(!sender.isSpaceAvailableInServer(toBeSentFile)){
            notEnoughSpaceLabel.setVisible(true);
            return;
        }
        if(!sender.isSendingSuccessful(toBeSentFile,enableRandomLostFrame)){
            sendingStatusLabel.setVisible(true);
            sendingStatusLabel.setTextFill(Color.RED);
            sendingStatusLabel.setText("Chunks did not match. Sending Failed!");
            return;
        }
        sendingStatusLabel.setVisible(true);
        sendingStatusLabel.setTextFill(Color.BLACK);
        sendingStatusLabel.setText("Sent Successfully!");
        disableSenderSideVisibility();
        clearSenderSide();
    }

    public void clearSenderSide(){
        studentIDField.setText("");
        filePathField.setText("");
    }

    //Yes button e click korle receive and merge function call
    //no te deny call
    //dui jaygay e screen clear kora after doing stuff
    @FXML
    public void onYesButtonClick(ActionEvent event){
        clearReceivingSide();
        boolean res = receiver.isReceivingAndMergingOk();
        if(res){
            receivingStatusLabel.setText("Receiving Successful");
        }
        else{
            receivingStatusLabel.setText("Files could not be received!");
        }
        receivingStatusLabel.setVisible(true);

        new Thread(receiver).start();
    }

    @FXML
    public void onNoButtonClick(ActionEvent event){
        clearReceivingSide();
        receiver.denyRequest();
        receivingStatusLabel.setText("Receiving denied !");
        receivingStatusLabel.setVisible(true);

        new Thread(receiver).start();
    }

    @FXML
    public void onSenderSideRefreshButtonClick(ActionEvent event){
        disableSenderSideVisibility();
        sendingStatusLabel.setVisible(false);
    }

    @FXML
    public void onReceiverSideRefreshButtonClick(ActionEvent event){
        receivingStatusLabel.setVisible(false);
        if(incomingField.getText().equals("")){
            clearReceivingSide();
        }
    }

    public void clearReceivingSide() {
        incomingField.setText("");
        askReceiverLabel.setVisible(false);
        yesButton.setVisible(false);
        noButton.setVisible(false);
    }

    public void showReceivingSide(){
        askReceiverLabel.setVisible(true);
        yesButton.setVisible(true);
        noButton.setVisible(true);
    }
}
