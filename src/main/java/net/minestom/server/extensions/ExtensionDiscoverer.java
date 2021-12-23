package net.minestom.server.extensions;

import net.minestom.server.MinecraftServer;
import net.minestom.server.extensions.descriptor.ExtensionDescriptor;
import net.minestom.server.utils.PropertyUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.io.Reader;
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

    String INDEV_CLASSES_PROPERTY = "minestom.extension.indevfolder.classes";
    String INDEV_RESOURCES_PROPERTY = "minestom.extension.indevfolder.resources";

    String AUTOSCAN_ENABLED_PROPERTY = "minestom.extension.autoscan";
    String AUTOSCAN_TARGETS_PROPERTY = "minestom.extension.autoscan.targets";

    List<ExtensionDescriptor> discover(@NotNull Path extensionDirectory);

    ExtensionDiscoverer FILESYSTEM = (extensionDirectory) -> {
        List<ExtensionDescriptor> discovered = new ArrayList<>();
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
                    ZipEntry manifest = findExtensionManifest(
                            name, "extension.json",
                            zip::getEntry, Objects::nonNull);
                    if (manifest == null) {
                        LOGGER.error("Missing extension.json in extension {}.", name);
                        continue;
                    }

                    try (Reader reader = new InputStreamReader(zip.getInputStream(manifest))) {
                        discovered.add(ExtensionDescriptor.fromReader(
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
        return Collections.unmodifiableList(discovered);
    };

    ExtensionDiscoverer INDEV = (extensionDirectory) -> {
        String indevclasses = System.getProperty(INDEV_CLASSES_PROPERTY);
        String indevresources = System.getProperty(INDEV_RESOURCES_PROPERTY);

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

            Path manifest = findExtensionManifest(
                    "<indev>", "extension.json",
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
        boolean enabled = PropertyUtil.getBoolean(AUTOSCAN_ENABLED_PROPERTY, true);
        if (enabled) {
            List<ExtensionDescriptor> discovered = new ArrayList<>();
            ClassLoader rootClassLoader = MinecraftServer.class.getClassLoader();

            String targets = System.getProperty(AUTOSCAN_TARGETS_PROPERTY, "extension.json");
            for (String target : targets.split(",")) {
                try {
                    URL extensionManifest = findExtensionManifest(
                            "<autoscan:" + target + ">", target,
                            rootClassLoader::getResource, Objects::nonNull);
                    if (extensionManifest == null) {
                        LOGGER.error("Missing extension.json in extension <autoscan:{}>.", target);
                        continue;
                    }

                    LOGGER.info("Autoscan found {}. Adding to list of discovered extensions.", target);
                    try (Reader reader = new InputStreamReader(extensionManifest.openStream())) {
                        discovered.add(ExtensionDescriptor.fromReader(
                                reader, extensionDirectory
                                // No files, the classloader will exclusively load from the root classloader.
                        ));
                    }
                } catch (Exception e) {
                    // todo
//                    MinecraftServer.getExceptionManager().handleException(e);
                }
            }
            return Collections.unmodifiableList(discovered);
        } else LOGGER.trace("Autoscan disabled");
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
    private static <T> T findExtensionManifest(String name, String filename, Function<String, T> getEntry, Predicate<T> validator) {
        T entry = getEntry.apply("META-INF/" + filename);
        if (!validator.test(entry)) {
            // Legacy location
            entry = getEntry.apply(filename);
            if (validator.test(entry)) {
                LOGGER.warn("The extension.json for {} is in the root of the extension, it should be in META-INF.", name);
            } else return null;
        }
        return entry;
    }
}
