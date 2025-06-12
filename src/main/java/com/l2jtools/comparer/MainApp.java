package com.l2jtools.comparer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        // --- NUEVO CÓDIGO PARA CARGAR LA FUENTE ---
        // La ruta empieza con '/' para indicar que es desde la raíz de los recursos.
        Font.loadFont(getClass().getResourceAsStream("/fonts/JetBrainsMono-Regular.ttf"), 14);
        // -------------------------------------------
        // Carga el archivo FXML que define la interfaz
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MainView.fxml"));
        Parent root = loader.load();

        // Configura la ventana principal (el "Stage")
        primaryStage.setTitle("L2J Source Comparer");
        Scene scene = new Scene(root, 800, 400);

        // Carga nuestra hoja de estilos CSS para el tema oscuro
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}