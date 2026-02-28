package net.minestom.codegen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public final class Generators {

    static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: <target folder>");
            return;
        }
        Path outputFolder = ensureDirectory(Path.of(args[0]));

        CodegenRegistry registry = CodegenRegistries.registry();
        for (CodegenValue value : registry) {
            value.generator().get().generate(outputFolder, registry, value);
        }

        System.out.println("Finished generating code");
    }

    @SuppressWarnings("JvmTaintAnalysis") // We trust the generator. (Dont run as root!)
    private static Path ensureDirectory(Path directory) throws IllegalStateException {
        Objects.requireNonNull(directory, "Directory is null");
        if (Files.isDirectory(directory)) return directory;
        try {
            return Files.createDirectories(directory);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create folder for %s".formatted(directory), e);
        }
    }
}
