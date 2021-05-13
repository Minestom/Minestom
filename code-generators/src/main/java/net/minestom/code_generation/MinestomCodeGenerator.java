package net.minestom.code_generation;

import com.squareup.javapoet.JavaFile;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

public abstract class MinestomCodeGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(MinestomCodeGenerator.class);

    public abstract void generate();

    protected void writeFiles(@NotNull List<JavaFile> fileList, File outputFolder) {
        for (JavaFile javaFile : fileList) {
            try {
                javaFile.writeTo(outputFolder);
            } catch (IOException e) {
                LOGGER.error("An error occured while writing source code to the file system.", e);
            }
        }
    }
}
