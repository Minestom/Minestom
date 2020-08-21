package net.minestom.server.registry;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Responsible for making sure Minestom has the necessary files to run (notably registry files)
 */
public class ResourceGatherer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceGatherer.class);

    public static final File DATA_FOLDER = new File("./minecraft_data/");
    private static final File TMP_FOLDER = new File("./.minestom_tmp/");

    /**
     * Checks if registry/ folder is present
     * If it is not, download the minecraft server jar, run the data generator and extract the wanted files
     * If it is already present, directly return
     */
    public static void ensureResourcesArePresent(String version, File minecraftFolderOverride) throws IOException {
        if (DATA_FOLDER.exists()) {
            return;
        }
        LOGGER.info(DATA_FOLDER + " folder does not exist. Minestom will now generate the necessary files.");

        if (!TMP_FOLDER.exists() && !TMP_FOLDER.mkdirs()) {
            throw new IOException("Failed to create tmp folder.");
        }

        LOGGER.info("Starting download of Minecraft server jar for version " + version + " from Mojang servers...");
        File minecraftFolder = getMinecraftFolder(minecraftFolderOverride);
        if (!minecraftFolder.exists()) {
            throw new IOException("Could not find Minecraft installation folder, attempted location " + minecraftFolder + ". If this location is not the correct one, please supply the correct one as argument of ResourceGatherer#ensureResourcesArePresent");
        }
        File serverJar = downloadServerJar(minecraftFolder, version);
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
        LOGGER.info("Removal successful, now moving data to " + DATA_FOLDER);
        Files.walkFileTree(tmpFolderPath, new SimpleFileVisitor<>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path relativePath = generatedFolder.relativize(dir);
                if (dir.startsWith(generatedFolder)) { // don't copy logs
                    Path resolvedPath = dataFolderPath.resolve(relativePath);
                    LOGGER.info("> Creating sub-folder " + relativePath);
                    Files.createDirectories(resolvedPath);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                LOGGER.info("> Deleting folder " + dir);
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path relativePath = generatedFolder.relativize(file);
                if (file.startsWith(generatedFolder)) { // don't copy logs
                    Path resolvedPath = dataFolderPath.resolve(relativePath);
                    LOGGER.info("> Moving " + relativePath);
                    Files.move(file, resolvedPath);
                } else {
                    LOGGER.info("> Deleting " + relativePath);
                    Files.delete(file);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static void runDataGenerator(File serverJar) throws IOException {
        ProcessBuilder dataGenerator = new ProcessBuilder("java", "-cp", serverJar.getName(), "net.minecraft.data.Main", "--all", "--server", "--dev");
        dataGenerator.directory(TMP_FOLDER);
        LOGGER.info("Now running data generator with options '--dev', '--server', '--all'");
        LOGGER.info("Executing: " + String.join(" ", dataGenerator.command()));
        LOGGER.info("Minestom will now wait for it to finish, here's its output:");
        LOGGER.info("");
        Process dataGeneratorProcess = dataGenerator.start();
        new BufferedReader(
                new InputStreamReader(dataGeneratorProcess.getInputStream())
        ).lines().forEach(LOGGER::info);
        LOGGER.info("");

        try {
            int resultCode = dataGeneratorProcess.waitFor();
            if (resultCode != 0) {
                throw new IOException("Data generator finished with non-zero return code " + resultCode);
            }
        } catch (InterruptedException e) {
            throw new IOException("Data generator was interrupted.", e);
        }
    }

    /**
     * Finds the URL for the server jar inside the versions/ folder of the game installation and download the .jar file from there
     *
     * @param minecraftFolder
     * @param version
     * @return
     */
    private static File downloadServerJar(File minecraftFolder, String version) throws IOException {
        File versionInfoFile = new File(minecraftFolder, "versions/" + version + "/" + version + ".json");
        if (!versionInfoFile.exists()) {
            throw new IOException("Could not find " + version + ".json in your Minecraft installation. Make sure to launch this version at least once before running Minestom");
        }

        try (FileReader fileReader = new FileReader(versionInfoFile)) {
            Gson gson = new Gson();
            VersionInfo versionInfo = gson.fromJson(fileReader, VersionInfo.class);
            VersionInfo.DownloadObject serverJarInfo = versionInfo.getDownloadableFiles().get("server");
            String downloadURL = serverJarInfo.getUrl();

            LOGGER.info("Found URL, starting download from " + downloadURL + "...");
            return download(version, downloadURL);
        }
    }

    private static File download(String version, String url) throws IOException {
        File target = new File(TMP_FOLDER, "server_" + version + ".jar");
        try (BufferedInputStream in = new BufferedInputStream(new URL(url).openStream())) {
            Files.copy(in, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IOException("Failed to download Minecraft server jar.", e);
        }
        return target;
    }

    private static File getMinecraftFolder(File minecraftFolderOverride) {
        if (minecraftFolderOverride != null) {
            return minecraftFolderOverride;
        }

        // https://help.minecraft.net/hc/en-us/articles/360035131551-Where-are-Minecraft-files-stored-
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            String user = System.getProperty("user.home");
            return new File(user + "/AppData/Roaming/.minecraft/");
        }
        if (os.contains("mac")) {
            String user = System.getProperty("user.home");
            return new File(user + "/Library/Application Support/minecraft");
        }

        return new File(System.getProperty("user.home") + "/.minecraft");
    }
}
