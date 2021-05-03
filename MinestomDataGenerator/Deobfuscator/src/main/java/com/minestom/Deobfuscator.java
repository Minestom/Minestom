package com.minestom;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.lxgaming.reconstruct.common.Reconstruct;
import io.github.lxgaming.reconstruct.common.configuration.Config;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class Deobfuscator {
    private static final Logger LOGGER = LoggerFactory.getLogger(Deobfuscator.class);
    private static final Gson GSON = new Gson();
    private static final File WORK_FOLDER = new File("./work/");
    private static final File DEOBFUSCATED_FOLDER = new File("./deobfuscated_jars/");
    private static File serverJar;
    private static File serverMappings;

    public static void main(String[] args) {
        if (args.length == 0) {
            LOGGER.info("You must specify a version to deobfuscated.");
            return;
        }
        String version = args[0];
        // Create downloads Folder
        if (!WORK_FOLDER.exists() && !WORK_FOLDER.mkdirs()) {
            LOGGER.error("Failed to create work folder.");
            return;
        }
        // Create Final folder
        if (!DEOBFUSCATED_FOLDER.exists() && !DEOBFUSCATED_FOLDER.mkdirs()) {
            LOGGER.error("Failed to create deobfuscated JARs folder.");
            return;
        }
        File deobfuJar = new File(DEOBFUSCATED_FOLDER, "deobfu_" + version + ".jar");
        if (!deobfuJar.exists()) {
            LOGGER.info("Did not find the deobfuscated JAR, proceeding to generate the deobfuscated JAR.");
            try {
                getServerJarWithLibrariesAndMappings(version);
            } catch (IOException e) {
                LOGGER.error("Unable to get server JAR and/or mappings for version '" + version + "'.", e);
                return;
            }
            Reconstruct reconstruct = new Reconstruct(new Config() {
                private int threads = 1;

                @Override
                public boolean isDebug() {
                    return false;
                }

                @Override
                public int getThreads() {
                    return threads;
                }

                @Override
                public void setThreads(int threads) {
                    this.threads = threads;
                }

                @Override
                public Collection<String> getTransformers() {
                    return new ArrayList<>();
                }

                @Override
                public Path getJarPath() {
                    return serverJar.toPath();
                }

                @Override
                public Path getMappingPath() {
                    return serverMappings.toPath();
                }

                @Override
                public Path getOutputPath() {
                    return deobfuJar.toPath();
                }

                @Override
                public Collection<String> getExcludedPackages() {
                    return Arrays.asList("com.google.", "com.mojang.", "io.netty.", "it.unimi.", "javax.", "joptsimple.", "org.apache.");
                }
            });
            reconstruct.load();
            LOGGER.error("Successfully generated the deobfuscated JAR.");
        } else {
            LOGGER.info("JAR already exists!");
        }
    }

    public static void getServerJarWithLibrariesAndMappings(String version) throws IOException {
        // Mojang's version manifest is located at https://launchermeta.mojang.com/mc/game/version_manifest.json
        // If we query this (it's a json object), we can then search for the id we want.
        InputStream versionManifestStream = new URL("https://launchermeta.mojang.com/mc/game/version_manifest.json").openStream();
        LOGGER.info("Successfully queried Mojang's version_manifest.json.");

        JsonObject versionManifestJson = GSON.fromJson(new InputStreamReader(versionManifestStream), JsonObject.class);
        LOGGER.info("Successfully read Mojang's version_manifest.json into a json object.");

        JsonArray versionArray = versionManifestJson.getAsJsonArray("versions");
        LOGGER.info("Iterating over the version manifest to find a version with the id '" + version + "'.");

        JsonObject versionEntry = null;
        for (JsonElement element : versionArray) {
            if (element.isJsonObject()) {
                JsonObject entry = element.getAsJsonObject();
                if (entry.get("id").getAsString().equals(version)) {
                    LOGGER.info("Successfully found a version with the id '" + version + "'.");
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
        LOGGER.info("Successfully queried '" + version + ".json'.");

        JsonObject versionJson = GSON.fromJson(new InputStreamReader(versionStream), JsonObject.class);
        LOGGER.info("Successfully read '" + version + ".json' into a json object.");
        // Now we need to navigate to "downloads.client.url" and "downloads.server.url" }
        JsonObject downloadsJson = versionJson.getAsJsonObject("downloads");

        // Server
        {
            JsonObject serverJson = downloadsJson.getAsJsonObject("server");
            final String jarURL = serverJson.get("url").getAsString();
            final String sha1 = serverJson.get("sha1").getAsString();

            LOGGER.info("Found all information required to download the server JAR file.");
            LOGGER.info("Attempting download.");
            serverJar = new File(WORK_FOLDER, version + ".jar");
            download(jarURL, sha1, serverJar);
            LOGGER.info("Download successful.");
        }
        // Server mappings
        {
            JsonObject serverJson = downloadsJson.getAsJsonObject("server_mappings");
            final String txtUrl = serverJson.get("url").getAsString();
            final String sha1 = serverJson.get("sha1").getAsString();

            LOGGER.info("Found all information required to download the server Mappings file.");
            LOGGER.info("Attempting download.");
            serverMappings = new File(WORK_FOLDER, version + ".txt");
            download(txtUrl, sha1, serverMappings);
            LOGGER.info("Download successful.");
        }
    }

    private static void download(
            @NotNull String url,
            @NotNull String sha1Source,
            @NotNull File target)
            throws IOException {
        // Download
        try (BufferedInputStream in = new BufferedInputStream(new URL(url).openStream())) {
            Files.copy(in, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IOException("Failed to download Minecraft server jar.", e);
        }
        // TODO: Verify Checksum
    }
}
