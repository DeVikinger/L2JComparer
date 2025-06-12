package com.l2jtools.comparer;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;

public class MainController {

    // @FXML conecta estas variables con los elementos del archivo FXML con el mismo fx:id
    @FXML
    private TextField pathAField;

    @FXML
    private TextField pathBField;

    @FXML
    private Button compareButton;

    private File sourceADirectory;
    private File sourceBDirectory;

    @FXML
    void handleOpenSourceA(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Seleccionar Carpeta de Source A");
        // Obtiene la ventana actual para mostrar el diálogo sobre ella
        Stage stage = (Stage) pathAField.getScene().getWindow();
        sourceADirectory = directoryChooser.showDialog(stage);

        if (sourceADirectory != null) {
            pathAField.setText(sourceADirectory.getAbsolutePath());
        }
        checkComparisonReady();
    }

    @FXML
    void handleOpenSourceB(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Seleccionar Carpeta de Source B");
        Stage stage = (Stage) pathBField.getScene().getWindow();
        sourceBDirectory = directoryChooser.showDialog(stage);

        if (sourceBDirectory != null) {
            pathBField.setText(sourceBDirectory.getAbsolutePath());
        }
        checkComparisonReady();
    }

    @FXML
    void handleCompare(ActionEvent event) {
        System.out.println("Iniciando comparación entre:");
        System.out.println("A: " + sourceADirectory.getAbsolutePath());
        System.out.println("B: " + sourceBDirectory.getAbsolutePath());
        // Aquí llamaremos a la lógica de la Fase 2
    }

    private void checkComparisonReady() {
        // El botón de comparar solo se activa si ambos directorios han sido seleccionados
        boolean ready = sourceADirectory != null && sourceBDirectory != null;
        compareButton.setDisable(!ready);
    }
}