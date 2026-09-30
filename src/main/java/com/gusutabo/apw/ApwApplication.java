package com.gusutabo.apw;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ApwApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ApwApplication.class.getResource("task-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1050, 650);
        scene.getStylesheets().add(ApwApplication.class.getResource("style.css").toExternalForm());
        stage.setTitle("APW");
        stage.setScene(scene);
        stage.show();
    }
}
