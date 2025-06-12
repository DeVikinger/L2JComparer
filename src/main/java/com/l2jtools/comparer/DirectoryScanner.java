package com.l2jtools.comparer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DirectoryScanner {

    public Map<Path, FileNode> scan(Path rootPath) throws IOException {
        System.out.println("Escaneando: " + rootPath + "...");
        try (Stream<Path> stream = Files.walk(rootPath)) {
            Map<Path, FileNode> nodeMap = stream
                    .map(absolutePath -> {
                        try {
                            Path relativePath = rootPath.relativize(absolutePath);
                            boolean isDirectory = Files.isDirectory(absolutePath);
                            String hash = isDirectory ? null : calculateSha256(absolutePath);
                            return new FileNode(absolutePath, relativePath, isDirectory, hash);
                        } catch (IOException | NoSuchAlgorithmException e) {
                            System.err.println("Error procesando el archivo: " + absolutePath);
                            e.printStackTrace();
                            return null;
                        }
                    })
                    .filter(node -> node != null && !node.relativePath().toString().isEmpty())
                    .collect(Collectors.toMap(FileNode::relativePath, node -> node));
            System.out.println("Escaneo completado. Se encontraron " + nodeMap.size() + " elementos.");
            return nodeMap;
        }
    }

    private String calculateSha256(Path path) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        try (InputStream is = Files.newInputStream(path);
             DigestInputStream dis = new DigestInputStream(is, md)) {
            // Leer el archivo para que el MessageDigest procese el contenido
            //noinspection StatementWithEmptyBody
            while (dis.read() != -1);
        }
        byte[] digest = md.digest();
        return HexFormat.of().formatHex(digest);
    }
}