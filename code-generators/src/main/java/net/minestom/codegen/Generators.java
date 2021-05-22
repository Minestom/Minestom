package net.minestom.codegen;

import net.minestom.codegen.blocks.BlockGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class Generators {
    private static final Logger LOGGER = LoggerFactory.getLogger(Generators.class);

    public static void main(String[] args) {
        if (args.length < 3) {
            LOGGER.error("Usage: <MC version> <source folder> <target folder>");
            return;
        }
        String targetVersion = args[0];
        File inputFolder = new File(args[1]);
        File outputFolder = new File(args[2]);
        // Generate blocks
        new BlockGenerator(
                new File(inputFolder, targetVersion.replaceAll("\\.", "_") + "_blocks.json"),
                new File(inputFolder, targetVersion.replaceAll("\\.", "_") + "_block_properties.json"),
                outputFolder
        ).generate();
        LOGGER.info("Finished generating code");
    }
}