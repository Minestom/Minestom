package net.minestom.server.extensions;

import net.minestom.server.MinecraftServer;
import net.minestom.server.extensions.descriptor.ExtensionDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Responsible for discovering extensions from multiple sources:
 * <ul>
 *     <li>Filesystem (extensions directory)</li>
 *     <li>indevclasses/indevresources vm arguments</li>
 *     <li>Autoscan (root classpath)</li>
 * </ul>
 */
interface ExtensionDiscoverer {
    Logger LOGGER = LoggerFactory.getLogger(ExtensionDiscoverer.class);

    List<ExtensionDescriptor> discover(@NotNull Path extensionDirectory);

    ExtensionDiscoverer FILESYSTEM = (extensionDirectory) -> {
        List<ExtensionDescriptor> extensions = new ArrayList<>();
        try {
            for (Path file : Files.list(extensionDirectory).toList()) {
                String filename = file.getFileName().toString();

                // Filename validation
                if (!filename.endsWith(".jar")) {
                    continue;
                }

                // Manifest validation
                String name = filename.substring(0, filename.length() - 4);
                try (ZipFile zip = new ZipFile(file.toFile())) {
                    ZipEntry manifest = findExtensionManifest(name, zip);
                    if (manifest == null) {
                        LOGGER.error("Missing extension.json in extension {}.", name);
                        continue;
                    }

                    try (InputStreamReader reader = new InputStreamReader(zip.getInputStream(manifest))) {
                        extensions.add(ExtensionDescriptor.fromReader(
                                reader,
                                extensionDirectory,
                                file.toUri().toURL()
                        ));
                    } catch (IllegalArgumentException e) {
                        LOGGER.error("Unable to load {}: {}", name, e.getMessage());
                    }
                } catch (Exception e) {
                    //todo
//                    MinecraftServer.getExceptionManager().handleException(e);
                }
            }
        } catch (Exception e) {
            //todo
//            MinecraftServer.getExceptionManager().handleException(e);
        }
        return Collections.unmodifiableList(extensions);
    };

    ExtensionDiscoverer INDEV = (extensionDirectory) -> {
        String indevclasses = System.getProperty("minestom.extension.indevfolder.classes");
        String indevresources = System.getProperty("minestom.extension.indevfolder.resources");

        if (indevclasses != null && indevresources == null) {
            LOGGER.warn("Found indev classes folder, but not indev resources folder. This is likely a mistake.");
        } else if (indevclasses == null && indevresources != null) {
            LOGGER.warn("Found indev resources folder, but not indev classes folder. This is likely a mistake.");
        } else if (indevclasses != null) try {
            LOGGER.info("Found indev folders for extension. Adding to list of discovered extensions.");

            DiscoveredExtension extension = discoverDynamic(Paths.get(indevresources, "extension.json"),
                    new URL("file://" + indevclasses),
                    new URL("file://" + indevresources));

            if (extension != null && extension.loadStatus == DiscoveredExtension.LoadStatus.LOAD_SUCCESS) {
                extensions.add(extension);
            }
        } catch (MalformedURLException e) {
            MinecraftServer.getExceptionManager().handleException(e);
        }

        return Collections.emptyList();
    };

    ExtensionDiscoverer AUTOSCAN = (extensionDirectory) -> {
        return Collections.emptyList();
    };

    ExtensionDiscoverer DEFAULT = (extensionDirectory) -> {
        List<ExtensionDescriptor> combined = new ArrayList<>();
        combined.addAll(FILESYSTEM.discover(extensionDirectory));
        combined.addAll(INDEV.discover(extensionDirectory));
        combined.addAll(AUTOSCAN.discover(extensionDirectory));
        return Collections.unmodifiableList(combined);
    };

    @Nullable
    private static ZipEntry findExtensionManifest(String name, ZipFile zipFile) {
        ZipEntry entry = zipFile.getEntry("META-INF/extension.json");
        if (entry == null) {
            // Legacy location
            entry = zipFile.getEntry("extension.json");
            if (entry != null) {
                LOGGER.warn("The extension.json for {} is in the root of the extension, it should be in META-INF.", name);
            }
        }
        return entry;
    }
}
