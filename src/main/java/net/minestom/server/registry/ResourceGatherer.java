package net.minestom.server.registry;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Responsible for making sure Minestom has the necessary files to run (notably registry files)
 */
public class ResourceGatherer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceGatherer.class);
    public static File DATA_FOLDER;


    /**
     * Checks if the data exists and makes sure it is in the form that is configured.
     *
     * @param externalMode True if the data should be loaded from the filesystem folder ./minecraft_data instead of Minestom's own JAR location /minecraft_data/ .
     */
    public static void ensureResourcesArePresent(boolean externalMode) {
        File jarDataFolder;
        try {
            jarDataFolder = new File(ResourceGatherer.class.getResource("/minecraft_data").toURI());
        } catch (URISyntaxException e) {
            LOGGER.error("An error occured while loading the data directory. Minestom will attempt to load anyway, but things may not work, and crashes can happen.", e);
            return;
        }
        if (externalMode) {
            LOGGER.info("Minestom has been started in external Mode.");
            LOGGER.info("The folder ./minestom_data can be used to customize certain attributes.");
            File dataFolder = new File("./minecraft_data/");
            if (dataFolder.exists()) {
                return;
            }
            LOGGER.info("{} folder does not exist. Minestom will now copy over the necessary files.", dataFolder);
            try {
                FileUtils.copyDirectory(jarDataFolder, dataFolder);
            } catch (IOException e) {
                LOGGER.error("An error occured while copying the necessary files. Minestom will attempt to load anyway, but things may not work, and crashes can happen.", e);
                return;
            }
            LOGGER.info("Finished copying over the necessary files.");

            DATA_FOLDER = dataFolder;
        } else {
            DATA_FOLDER = jarDataFolder;
        }
        LOGGER.info("Resource gathering done!");
    }
}
