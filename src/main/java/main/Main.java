package main;

import controller.*;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        ClassLoader classLoader = getClass().getClassLoader();
        FXMLLoader loader = new FXMLLoader(classLoader.getResource("scene/main.fxml"));
        Parent root = loader.load();

        MainController controller = loader.getController();
        controller.window = primaryStage;

        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 1000, 600));

        primaryStage.show();
}

    public static void main(String[] args) {
        launch(args);
    }
}
