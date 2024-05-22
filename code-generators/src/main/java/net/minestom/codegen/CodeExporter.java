package net.minestom.codegen;

import com.squareup.javapoet.JavaFile;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * The class contains method which allows to write {@link JavaFile} to a given folder which is given as {@link java.nio.file.Path} reference.
 * This class exists to reduce the amount of code duplication in the code generators.
 * @version 1.0.0
 * @since 1.1.4
 */
public interface CodeExporter {

    Logger LOGGER = LoggerFactory.getLogger(CodeExporter.class);

    /**
     * Write a list of {@link JavaFile} objects to a given folder.
     * @param fileList the list of files to write
     * @param outputFolder the folder to write to
     */
    default void writeFiles(@NotNull List<JavaFile> fileList, File outputFolder) {
        if (fileList.isEmpty()) return;
        for (JavaFile javaFile : fileList) {
            writeFile(javaFile, outputFolder);
        }
    }

    /**
     * Write a single {@link JavaFile} to a given folder.
     * @param javaFile the file to write
     * @param outputFolder the folder to write to
     */
    default void writeFile(@NotNull JavaFile javaFile, @NotNull File outputFolder) {
        try {
            javaFile.writeTo(outputFolder);
        } catch (IOException e) {
            LOGGER.error("An error occurred while writing source code to the file system.", e);
        }
    }
}
