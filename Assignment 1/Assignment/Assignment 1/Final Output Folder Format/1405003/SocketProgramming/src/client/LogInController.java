package client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import javafx.stage.Stage;
import util.NetworkUtil;

import java.io.IOException;


public class LogInController {

    @FXML
    private TextField studentID;

    @FXML
    private Label warning;

    public void logIn(ActionEvent Event){

        int clientID = Integer.parseInt(studentID.getText().toString());
        studentID.clear();
        //System.out.println(clientID);
        //boolean exitsUser = Main.validate(clientID);
        //if(exitsUser) {
        try {

            NetworkUtil nc = new NetworkUtil("127.0.0.1",44444,clientID);
            //if(clientID==11)System.out.println(connections.get(11));

            nc.write(clientID);
            String msg = (String)nc.read();
            if(msg.equals("u")){
                nc.closeConnection();
                warning.setText("User already exists");
            }
            else {
                NetworkUtil ncAgain = new NetworkUtil("127.0.0.1",44444,clientID);
                warning.setText("");

                //creating home page for client

                Stage primaryStage = new Stage();
                primaryStage.setTitle(Integer.toString(clientID));

                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource(
                                "clientHome.fxml"
                        )
                );
                primaryStage.setScene(
                        new Scene(
                                loader.load()
                        )
                );

                HomeController controller =
                        loader.<HomeController>getController();
                controller.initData(nc);

                primaryStage.setOnCloseRequest(event -> {
                    nc.write("logout");
                    nc.closeConnection();
                    //ClientSockets.remove(Integer.parseInt(primaryStage.getTitle()));
                    // Save file
                });
                primaryStage.show();
                new readThreadClient(ncAgain);
            }
            //new readThreadClient(nc);
        } catch (IOException e) {

        }


    }



}
