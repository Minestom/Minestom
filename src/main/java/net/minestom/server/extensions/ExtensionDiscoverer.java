package net.minestom.server.extensions;

import net.minestom.server.MinecraftServer;
import net.minestom.server.extensions.descriptor.ExtensionDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
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
                    ZipEntry manifest = findExtensionManifest(name, zip::getEntry, Objects::nonNull);
                    if (manifest == null) {
                        LOGGER.error("Missing extension.json in extension {}.", name);
                        continue;
                    }

                    try (Reader reader = new InputStreamReader(zip.getInputStream(manifest))) {
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

            Path classes = Paths.get(indevclasses).toAbsolutePath();
            Path resources = Paths.get(indevresources).toAbsolutePath();
            if (!Files.exists(classes) || !Files.exists(resources)) {
                LOGGER.error("Failed to load <indev>: Classes or resources directory does not exist.");
                return Collections.emptyList();
            }

            Path manifest = findExtensionManifest("<indev>",
                    resources::resolve, Files::exists);
            if (manifest == null) {
                LOGGER.error("Missing extension.json in extension <indev>.");
                return Collections.emptyList();
            }

            try (Reader reader = Files.newBufferedReader(manifest)) {
                return List.of(ExtensionDescriptor.fromReader(
                        reader, extensionDirectory,
                        new URL("file://" + indevclasses),
                        new URL("file://" + indevresources)
                ));
            }
        } catch (IllegalArgumentException e) {
            LOGGER.error("Unable to load <indev>: {}", e.getMessage());
        } catch (Exception e) {
            // todo
//            MinecraftServer.getExceptionManager().handleException(e);
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
    private static <T> T findExtensionManifest(String name, Function<String, T> getEntry, Predicate<T> validator) {
        T entry = getEntry.apply("META-INF/extension.json");
        if (!validator.test(entry)) {
            // Legacy location
            entry = getEntry.apply("extension.json");
            if (validator.test(entry)) {
                LOGGER.warn("The extension.json for {} is in the root of the extension, it should be in META-INF.", name);
            } else return null;
        }
        return entry;
    }
}
