package com.l2jtools.comparer;

import java.nio.file.Path;

public record FileNode(
        Path absolutePath,  // Ruta completa al archivo
        Path relativePath,  // Ruta relativa desde la carpeta 'game'
        boolean isDirectory, // Es un directorio?
        String hash          // Huella digital (hash SHA-256) del contenido
) {}