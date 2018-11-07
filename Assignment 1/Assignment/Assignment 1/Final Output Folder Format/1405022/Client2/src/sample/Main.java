package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private Stage stage;
    private Controller controller;
    private SecondPage secondPage;

    private String studentId;
    private String ipAddress;

    public int permit;
    public int sharedVar;
    public int fileNo;

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        stage = primaryStage;
        showLoginPage();

    }


    public void showLoginPage() throws Exception{
        FXMLLoader loader= new FXMLLoader();
        loader.setLocation(getClass().getResource("sample.fxml"));
        Parent root = loader.load();
        controller = loader.getController();
        controller.setMain(this);

        stage.setTitle("Client");
        stage.setScene(new Scene(root, 600, 400));
        stage.show();
    }


    public void showSecondPage() throws Exception{
        FXMLLoader loader= new FXMLLoader();
        loader.setLocation(getClass().getResource("secondpage.fxml"));
        Parent root = loader.load();

        stage.setTitle("Client");
        stage.setScene(new Scene(root, 600, 400));
        stage.show();

        System.out.println("Showing");

        secondPage = loader.getController();
        secondPage.setMain(this);

        /*stage.setTitle("Client");
        stage.setScene(new Scene(root, 600, 400));
        stage.show();*/
    }




    public static void main(String[] args) {
        launch(args);
    }
}
