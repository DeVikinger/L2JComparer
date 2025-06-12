package com.l2jtools.comparer;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert; // <-- NUEVO IMPORT PARA LAS ALERTAS
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

public class MainController {

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
        // =======================================================
        //        NUEVA VALIDACIÓN "ANTI-TONTOS"
        // =======================================================
        if (sourceADirectory.equals(sourceBDirectory)) {
            System.err.println("[Error: No puedes escanear el mismo folder]");

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Advertencia");
            alert.setHeaderText("Selección de carpetas inválida");
            alert.setContentText("No puedes comparar la misma carpeta contigo misma. Por favor, selecciona dos carpetas diferentes.");

            alert.showAndWait(); // Muestra la alerta y espera a que el usuario la cierre
            return; // Detiene la ejecución del método aquí mismo
        }
        // =======================================================
        //              FIN DE LA VALIDACIÓN
        // =======================================================

        System.out.println("=========================================");
        System.out.println("======= INICIANDO COMPARACIÓN ===========");
        System.out.println("=========================================");

        Path pathA = sourceADirectory.toPath().resolve("game");
        Path pathB = sourceBDirectory.toPath().resolve("game");

        if (!Files.exists(pathA) || !Files.exists(pathB)) {
            System.err.println("Error: La subcarpeta 'game' no se encontró en una de las rutas seleccionadas.");
            // También podríamos mostrar una alerta aquí
            return;
        }

        DirectoryScanner scanner = new DirectoryScanner();
        try {
            Map<Path, FileNode> nodesA = scanner.scan(pathA);
            Map<Path, FileNode> nodesB = scanner.scan(pathB);

            System.out.println("\n--- ANÁLISIS DE DIFERENCIAS ---");
            for (FileNode nodeA : nodesA.values()) {
                FileNode nodeB = nodesB.get(nodeA.relativePath());
                if (nodeB != null) {
                    if (Objects.equals(nodeA.hash(), nodeA.isDirectory() ? nodeB.isDirectory() : nodeB.hash())) {
                        System.out.println("[IDÉNTICO] " + nodeA.relativePath());
                    } else {
                        System.out.println("[DIFERENTE] " + nodeA.relativePath());
                    }
                }
            }
            for (FileNode nodeA : nodesA.values()) {
                if (!nodesB.containsKey(nodeA.relativePath())) {
                    System.out.println("[SOLO EN A] " + nodeA.relativePath());
                }
            }
            for (FileNode nodeB : nodesB.values()) {
                if (!nodesA.containsKey(nodeB.relativePath())) {
                    System.out.println("[SOLO EN B] " + nodeB.relativePath());
                }
            }
            System.out.println("\n--- COMPARACIÓN FINALIZADA ---");
        } catch (IOException e) {
            System.err.println("Ocurrió un error durante el escaneo de directorios.");
            e.printStackTrace();
        }
    }

    private void checkComparisonReady() {
        boolean ready = sourceADirectory != null && sourceBDirectory != null;
        compareButton.setDisable(!ready);
    }
}