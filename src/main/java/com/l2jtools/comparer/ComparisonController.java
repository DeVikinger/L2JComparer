package com.l2jtools.comparer;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener; // <-- ¡EL IMPORT QUE FALTABA!
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.paint.Color;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class ComparisonController {

    @FXML
    private TreeView<ComparisonResult> treeViewA;
    @FXML
    private TreeView<ComparisonResult> treeViewB;

    private final Map<Path, TreeItem<ComparisonResult>> nodeMapA = new HashMap<>();
    private final Map<Path, TreeItem<ComparisonResult>> nodeMapB = new HashMap<>();

    private record ComparisonResult(Path path, ComparisonState state, boolean isDirectory) {}

    public void initializeData(Map<Path, FileNode> nodesA, Map<Path, FileNode> nodesB) {
        Map<Path, ComparisonResult> results = computeDifferences(nodesA, nodesB);

        buildTree(treeViewA, results, nodeMapA, true);
        buildTree(treeViewB, results, nodeMapB, false);

        setCellFactory(treeViewA);
        setCellFactory(treeViewB);

        syncScrolling();
        syncExpansion(nodeMapA, nodeMapB);
        syncExpansion(nodeMapB, nodeMapA);
    }

    private void syncScrolling() {
        Platform.runLater(() -> {
            ScrollBar scrollBarA = findScrollBar(treeViewA);
            ScrollBar scrollBarB = findScrollBar(treeViewB);

            if (scrollBarA != null && scrollBarB != null) {
                final AtomicBoolean adjusting = new AtomicBoolean(false);

                scrollBarA.valueProperty().addListener((obs, old, newValue) -> {
                    if (!adjusting.get()) {
                        adjusting.set(true);
                        scrollBarB.setValue(scrollBarA.getValue());
                        adjusting.set(false);
                    }
                });

                scrollBarB.valueProperty().addListener((obs, old, newValue) -> {
                    if (!adjusting.get()) {
                        adjusting.set(true);
                        scrollBarA.setValue(scrollBarB.getValue());
                        adjusting.set(false);
                    }
                });
            }
        });
    }

    private void syncExpansion(Map<Path, TreeItem<ComparisonResult>> sourceMap, Map<Path, TreeItem<ComparisonResult>> targetMap) {
        final AtomicBoolean adjusting = new AtomicBoolean(false);

        sourceMap.values().forEach(item -> {
            item.expandedProperty().addListener((obs, wasExpanded, isNowExpanded) -> {
                if (!adjusting.get()) {
                    adjusting.set(true);
                    Path path = item.getValue().path();
                    TreeItem<ComparisonResult> targetItem = targetMap.get(path);
                    if (targetItem != null && targetItem.isExpanded() != isNowExpanded) {
                        targetItem.setExpanded(isNowExpanded);
                    }
                    adjusting.set(false);
                }
            });
        });
    }

    private ScrollBar findScrollBar(TreeView<?> treeView) {
        for (Node node : treeView.lookupAll(".scroll-bar")) {
            if (node instanceof ScrollBar scrollBar && scrollBar.getOrientation() == javafx.geometry.Orientation.VERTICAL) {
                return scrollBar;
            }
        }
        return null;
    }

    private Map<Path, ComparisonResult> computeDifferences(Map<Path, FileNode> nodesA, Map<Path, FileNode> nodesB) {
        Map<Path, ComparisonResult> results = new HashMap<>();
        nodesA.forEach((path, nodeA) -> {
            FileNode nodeB = nodesB.get(path);
            if (nodeB != null) {
                boolean identical = Objects.equals(nodeA.hash(), nodeB.hash());
                results.put(path, new ComparisonResult(path, identical ? ComparisonState.IDENTICAL : ComparisonState.DIFFERENT, nodeA.isDirectory()));
            } else {
                results.put(path, new ComparisonResult(path, ComparisonState.UNIQUE_A, nodeA.isDirectory()));
            }
        });
        nodesB.forEach((path, nodeB) -> {
            if (!nodesA.containsKey(path)) {
                results.put(path, new ComparisonResult(path, ComparisonState.UNIQUE_B, nodeB.isDirectory()));
            }
        });
        return results;
    }

    private void buildTree(TreeView<ComparisonResult> treeView, Map<Path, ComparisonResult> results, Map<Path, TreeItem<ComparisonResult>> directoryItems, boolean isSourceA) {
        TreeItem<ComparisonResult> root = new TreeItem<>(new ComparisonResult(Path.of("game"), ComparisonState.IDENTICAL, true));
        root.setExpanded(true);
        treeView.setRoot(root);
        directoryItems.put(Path.of(""), root);

        results.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.comparing(Path::toString)))
                .forEach(entry -> {
                    Path path = entry.getKey();
                    ComparisonResult result = entry.getValue();

                    if ((isSourceA && result.state == ComparisonState.UNIQUE_B) || (!isSourceA && result.state == ComparisonState.UNIQUE_A)) return;

                    Path parentPath = path.getParent() == null ? Path.of("") : path.getParent();
                    TreeItem<ComparisonResult> parentItem = findOrCreateParent(parentPath, directoryItems, results);

                    TreeItem<ComparisonResult> newItem = new TreeItem<>(result);
                    parentItem.getChildren().add(newItem);
                    if (result.isDirectory()) {
                        directoryItems.put(path, newItem);
                    }
                });
    }

    private TreeItem<ComparisonResult> findOrCreateParent(Path path, Map<Path, TreeItem<ComparisonResult>> directoryItems, Map<Path, ComparisonResult> results) {
        if (path == null || path.toString().isEmpty()) return directoryItems.get(Path.of(""));
        if (directoryItems.containsKey(path)) return directoryItems.get(path);

        Path parentPath = path.getParent() == null ? Path.of("") : path.getParent();
        TreeItem<ComparisonResult> grandParentItem = findOrCreateParent(parentPath, directoryItems, results);
        ComparisonResult result = results.get(path);
        TreeItem<ComparisonResult> parentItem = new TreeItem<>(result);
        grandParentItem.getChildren().add(parentItem);
        directoryItems.put(path, parentItem);
        return parentItem;
    }

    private void setCellFactory(TreeView<ComparisonResult> treeView) {
        treeView.setCellFactory(tv -> new TreeCell<>() {
            @Override
            protected void updateItem(ComparisonResult item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    String fileName = item.path.getFileName() == null ? item.path.toString() : item.path.getFileName().toString();
                    setText(fileName);
                    switch (item.state) {
                        case IDENTICAL -> setTextFill(Color.web("#85e885"));
                        case DIFFERENT -> setTextFill(Color.web("#8ab5ff"));
                        case UNIQUE_A, UNIQUE_B -> setTextFill(Color.web("#ff7373"));
                    }
                }
            }
        });
    }
}