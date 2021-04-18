package net.minestom.server.registry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Responsible for making sure Minestom has the necessary files to run (notably registry files)
 */
public class ResourceGatherer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceGatherer.class);
    public static File DATA_FOLDER;


    /**
     * Checks if the data exists and copies it to the correct directory.
     */
    public static void ensureResourcesArePresent() throws IOException {
        URL jarURL = ResourceGatherer.class.getResource("/minecraft_data");
        if (jarURL == null) {
            LOGGER.error("An error occured while getting the resources.");
            return;
        }
        URLConnection urlConnection = jarURL.openConnection();

        DATA_FOLDER = new File("./minecraft_data/");
        if (DATA_FOLDER.exists()) {
            return;
        } else {
            LOGGER.info("The folder '{}' does not exist. Minestom will now copy over the necessary files.", DATA_FOLDER);
            if (urlConnection instanceof JarURLConnection) {
                JarURLConnection jarURLConnection = (JarURLConnection) urlConnection;
                // JarURLConnection
                JarFile jarFile = jarURLConnection.getJarFile();
                for (Enumeration<JarEntry> e = jarFile.entries(); e.hasMoreElements();) {

                    JarEntry jarEntry = e.nextElement();
                    String jarEntryName = jarEntry.getName();
                    String jarConnectionEntryName = jarURLConnection.getEntryName();


                    if (jarEntryName.startsWith(jarConnectionEntryName)) {

                        String filename;
                        if (jarEntryName.startsWith(jarConnectionEntryName)) {
                            filename = jarEntryName.substring(jarConnectionEntryName.length());
                        } else {
                            filename = jarEntryName;
                        }
                        File currentFile = new File(DATA_FOLDER, filename);

                        if (jarEntry.isDirectory()) {
                            currentFile.mkdirs();
                        } else {
                            try (InputStream is = jarFile.getInputStream(jarEntry)) {
                                try (OutputStream out = FileUtils.openOutputStream(currentFile)) {
                                    IOUtils.copy(is, out);
                                }
                            }
                        }
                    }
                }
            } else {
                // FileURLConnection
                try {
                    FileUtils.copyDirectory(new File(jarURL.toURI()), DATA_FOLDER);
                } catch (IOException | URISyntaxException e) {
                    LOGGER.error("An error occured while copying the necessary files. Minestom will attempt to load anyway, but things may not work, and crashes can happen.", e);
                    return;
                }
            }
            LOGGER.info("Finished copying over the necessary files.");
        }

        LOGGER.info("Resource gathering done!");
    }
}
