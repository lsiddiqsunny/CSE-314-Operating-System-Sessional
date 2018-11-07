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

    private int uniqueID;
    private int sizeOfChunks;
    private int presentBufferSize;

    public int getPresentBufferSize() {
        return presentBufferSize;
    }

    public void setPresentBufferSize(int presentBufferSize) {
        this.presentBufferSize = presentBufferSize;
    }

    public int getSizeOfChunks() {
        return sizeOfChunks;
    }

    public void setSizeOfChunks(int sizeOfChunks) {
        this.sizeOfChunks = sizeOfChunks*1024;
    }

    public int getUniqueID() {
        return uniqueID;
    }

    public void setUniqueID(int uniqueID) {
        this.uniqueID = uniqueID;
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        uniqueID = 1;

        stage = primaryStage;
        showConfigurePage();
    }

    public void showConfigurePage() throws Exception{
        FXMLLoader loader= new FXMLLoader();
        loader.setLocation(getClass().getResource("sample.fxml"));
        Parent root = loader.load();
        controller = loader.getController();
        controller.setMain(this);

        stage.setTitle("Server");
        stage.setScene(new Scene(root, 600, 400));
        stage.show();
    }

    public void showSecondPage() throws Exception{
        FXMLLoader loader= new FXMLLoader();
        loader.setLocation(getClass().getResource("secondpage.fxml"));
        Parent root = loader.load();
        secondPage = loader.getController();
        secondPage.setMain(this);

        stage.setTitle("Server");
        stage.setScene(new Scene(root, 600, 400));
        stage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
