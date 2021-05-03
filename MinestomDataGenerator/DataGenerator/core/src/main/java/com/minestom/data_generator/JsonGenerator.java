package com.minestom.data_generator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public final class JsonGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonGenerator.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private final String version;
    private final File outputFolder;

    JsonGenerator(@NotNull String version, @NotNull File outputFolder) {
        this.version = version.replaceAll("\\.", "_");
        // Create output folder
        if (!outputFolder.exists() && !outputFolder.mkdirs()) {
            throw new ExceptionInInitializerError("Failed to create work folder.");
        }
        this.outputFolder = outputFolder;
    }

    public void output(JsonArray output, String fileName) {
        String filename = version + "_" + fileName + ".json";
        try {
            Writer writer = new FileWriter(new File(outputFolder, filename), false);
            GSON.toJson(output, writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            LOGGER.error("Something went wrong while writing data to " + filename + ".", e);
        }
    }

    public void output(JsonElement output, File outputFile) {
        if (!outputFile.getParentFile().exists() && !outputFile.getParentFile().mkdirs()) {
            LOGGER.error("Failed to create file: " + outputFile + ".");
            return;
        }
        try {
            Writer writer = new FileWriter(outputFile, false);
            GSON.toJson(output, writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            LOGGER.error("Something went wrong while writing data to " + outputFile + ".", e);
        }
    }
}
