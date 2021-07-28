package net.minestom.server.registry;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.io.FastBufferedInputStream;
import net.minestom.server.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Responsible for making sure Minestom has the necessary files to run (notably registry files)
 */
public class ResourceGatherer {
    public static final File DATA_FOLDER = new File("./minecraft_data/");
    private static final Gson GSON = new Gson();
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceGatherer.class);
    private static final File TMP_FOLDER = new File("./.minestom_tmp/");

    /**
     * Checks if registry/ folder is present
     * If it is not, download the minecraft server jar, run the data generator and extract the wanted files
     * If it is already present, directly return
     */
    public static void ensureResourcesArePresent(String version) throws IOException {
        if (DATA_FOLDER.exists()) {
            return;
        }
        LOGGER.info("{} folder does not exist. Minestom will now generate the necessary files.", DATA_FOLDER);

        if (!TMP_FOLDER.exists() && !TMP_FOLDER.mkdirs()) {
            throw new IOException("Failed to create tmp folder.");
        }

        LOGGER.info("Starting download of Minecraft server jar for version {} from Mojang servers...", version);
        File serverJar = downloadServerJar(version);
        LOGGER.info("Download complete.");

        runDataGenerator(serverJar);

        moveAndCleanup(version);
        LOGGER.info("Resource gathering done!");
    }

    private static void moveAndCleanup(String version) throws IOException {
        Path dataFolderPath = DATA_FOLDER.toPath();
        Path tmpFolderPath = TMP_FOLDER.toPath();
        Path generatedFolder = tmpFolderPath.resolve("generated");
        LOGGER.info("Data generator successful, removing server jar");
        Files.delete(tmpFolderPath.resolve("server_" + version + ".jar"));
        LOGGER.info("Removal successful, now moving data to {}", DATA_FOLDER);
        Files.walkFileTree(tmpFolderPath, new SimpleFileVisitor<>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path relativePath = generatedFolder.relativize(dir);
                if (dir.startsWith(generatedFolder)) { // don't copy logs
                    Path resolvedPath = dataFolderPath.resolve(relativePath);
                    LOGGER.info("> Creating sub-folder {}", resolvedPath);
                    Files.createDirectories(resolvedPath);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                LOGGER.info("> Deleting folder {}", dir);
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path relativePath = generatedFolder.relativize(file);
                if (file.startsWith(generatedFolder)) { // don't copy logs
                    Path resolvedPath = dataFolderPath.resolve(relativePath);
                    LOGGER.info("> Moving {}", relativePath);
                    Files.move(file, resolvedPath);
                } else {
                    LOGGER.info("> Deleting {}", relativePath);
                    Files.delete(file);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static void runDataGenerator(File serverJar) throws IOException {
        final String javaExecutable = System.getProperty("java.home") + "/bin/java";
        ProcessBuilder dataGenerator = new ProcessBuilder(javaExecutable, "-cp", serverJar.getName(), "net.minecraft.data.Main", "--all", "--server", "--dev");
        dataGenerator.directory(TMP_FOLDER);
        LOGGER.info("Now running data generator with options '--dev', '--server', '--all'");
        LOGGER.info("Executing: {}", String.join(StringUtils.SPACE, dataGenerator.command()));
        LOGGER.info("Minestom will now wait for it to finish, here's its output:");
        LOGGER.info("");
        Process dataGeneratorProcess = dataGenerator.start();
        new BufferedReader(
                new InputStreamReader(dataGeneratorProcess.getInputStream())
        ).lines().forEach(LOGGER::info);
        new BufferedReader(
                new InputStreamReader(dataGeneratorProcess.getErrorStream())
        ).lines().forEach(LOGGER::error);
        LOGGER.info("");

        try {
            int resultCode = dataGeneratorProcess.waitFor();
            if (resultCode != 0) {
                throw new IOException("Data generator finished with non-zero return code " + resultCode + " verify that you have 'java' cli");
            }
        } catch (InterruptedException e) {
            throw new IOException("Data generator was interrupted.", e);
        }
    }

    private static File downloadServerJar(String version) throws IOException {
        // Mojang's version manifest is located at https://launchermeta.mojang.com/mc/game/version_manifest.json
        // If we query this (it's a json object), we can then search for the id we want.
        InputStream versionManifestStream = new URL("https://launchermeta.mojang.com/mc/game/version_manifest.json").openStream();
        LOGGER.debug("Successfully queried Mojang's version_manifest.json.");

        JsonObject versionManifestJson = GSON.fromJson(new InputStreamReader(versionManifestStream), JsonObject.class);
        LOGGER.debug("Successfully read Mojang's version_manifest.json into a json object.");

        JsonArray versionArray = versionManifestJson.getAsJsonArray("versions");
        LOGGER.debug("Iterating over the version manifest to find a version with the id {}.", version);

        JsonObject versionEntry = null;
        for (JsonElement element : versionArray) {
            if (element.isJsonObject()) {
                JsonObject entry = element.getAsJsonObject();
                if (entry.get("id").getAsString().equals(version)) {
                    LOGGER.debug("Successfully found a version with the id {}.", version);
                    versionEntry = entry;
                    break;
                }
            }
        }
        if (versionEntry == null) {
            throw new IOException("Could not find " + version + " in Mojang's official list of minecraft versions.");
        }
        // We now have the entry we want and it gives us access to the json file containing the downloads.
        String versionUrl = versionEntry.get("url").getAsString();
        InputStream versionStream = new URL(versionUrl).openStream();
        LOGGER.debug("Successfully queried {}.json.", version);

        JsonObject versionJson = GSON.fromJson(new InputStreamReader(versionStream), JsonObject.class);
        LOGGER.debug("Successfully read {}.json into a json object.", version);

        // Now we need to navigate to "downloads.client.url" and "downloads.server.url" }
        JsonObject downloadsJson = versionJson.getAsJsonObject("downloads");

        // Designated spot if we ever need the client.

        // Server
        {
            JsonObject serverJson = downloadsJson.getAsJsonObject("server");
            final String jarURL = serverJson.get("url").getAsString();
            final String sha1 = serverJson.get("sha1").getAsString();

            LOGGER.debug("Found all information required to download the server JAR file.");
            LOGGER.debug("Attempting download.");
            return download(version, jarURL, sha1);
        }
    }

    private static File download(@NotNull String version, @NotNull String url, @NotNull String sha1Source) throws IOException {
        File target = new File(TMP_FOLDER, "server_" + version + ".jar");
        // Download
        try (FastBufferedInputStream in = new FastBufferedInputStream(new URL(url).openStream())) {
            Files.copy(in, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IOException("Failed to download Minecraft server jar.", e);
        }
        // Verify checksum
        try (FileInputStream fis = new FileInputStream(target)) {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            messageDigest.reset();
            messageDigest.update(fis.readAllBytes());
            byte[] digest = messageDigest.digest();
            StringBuffer sb = new StringBuffer();
            for (byte b : digest) {
                sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }
            // This just converts the sha1 back into a readable string.
            String sha1Target = sb.toString();
            if (!sha1Target.equals(sha1Source)) {
                LOGGER.error("The checksum test failed after downloading the Minecraft server jar.");
                LOGGER.error("The expected checksum was: {}.", sha1Source);
                LOGGER.error("The calculated checksum was: {}.", sha1Target);
                throw new IOException("Failed to download Minecraft server jar.");
            }
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("Failed to find SHA-1 hashing algorithm in Java Environment.");
            throw new IOException("Failed to download Minecraft server jar.");
        }
        return target;
    }
}
