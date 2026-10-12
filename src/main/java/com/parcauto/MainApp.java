package com.parcauto;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class MainApp extends Application {

    @Override
    public void start(Stage stagePrincipal) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        Parent racine = loader.load();

        Scene scene = new Scene(racine, 1280, 800);

        stagePrincipal.setTitle("Gestion d'un Parc Automobile");
        stagePrincipal.setScene(scene);
        stagePrincipal.setMinWidth(1024);
        stagePrincipal.setMinHeight(650);
        stagePrincipal.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
