package net.minestom.codegen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.javapoet.JavaFile;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public abstract class MinestomCodeGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(MinestomCodeGenerator.class);
    protected static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

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

    protected static String toConstant(String namespace) {
        return namespace.replace("minecraft:", "").toUpperCase(Locale.ROOT);
    }
}
