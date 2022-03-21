package net.minestom.server.extensions;

import net.minestom.server.MinecraftServer;
import net.minestom.server.utils.PropertyUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
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
@FunctionalInterface
public interface ExtensionDiscoverer {
    //todo streams probably not necessary here since it will always be completely processed by ext mgr

    Collection<ExtensionDescriptor> discover(@NotNull Path extensionDirectory) throws Exception;

    ExtensionDiscoverer FILESYSTEM = new ExtensionDiscoverer() {
        private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionDiscoverer.class);

        @Override
        public Collection<ExtensionDescriptor> discover(@NotNull Path extensionDirectory) throws Exception {
            List<ExtensionDescriptor> discovered = new ArrayList<>();
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
                } catch (IOException e) {
                    // Cannot load this one, but can still move on to any others.
                    LOGGER.error("Unable to load {}: {}", name, e.getMessage());
                }
            }
            return discovered;
        }
    };

    ExtensionDiscoverer INDEV = new ExtensionDiscoverer() {
        private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionDiscoverer.class);

        private static final String INDEV_CLASSES_PROPERTY = "minestom.extension.indevfolder.classes";
        private static final String INDEV_RESOURCES_PROPERTY = "minestom.extension.indevfolder.resources";

        @Override
        public Collection<ExtensionDescriptor> discover(@NotNull Path extensionDirectory) {
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
                    return List.of();
                }

                Path manifest = findExtensionManifest(
                        "<indev>", "extension.json",
                        resources::resolve, Files::exists);
                if (manifest == null) {
                    LOGGER.error("Missing extension.json in extension <indev>.");
                    return List.of();
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

            return List.of();
        }
    };

    ExtensionDiscoverer AUTOSCAN = new ExtensionDiscoverer() {
        private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionDiscoverer.class);

        private static final String AUTOSCAN_ENABLED_PROPERTY = "minestom.extension.autoscan";
        private static final String AUTOSCAN_TARGETS_PROPERTY = "minestom.extension.autoscan.targets";

        @Override
        public Collection<ExtensionDescriptor> discover(@NotNull Path extensionDirectory) {
            boolean enabled = PropertyUtils.getBoolean(AUTOSCAN_ENABLED_PROPERTY, true);
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
                            LOGGER.debug("No extension manifest found for <autoscan:{}>", target);
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
                        // Unable to load this one, but can keep going to others.
                        LOGGER.error("Unable to load <autoscan:{}>: {}", target, e.getMessage());
                    }
                }
                return discovered;
            } else LOGGER.trace("Autoscan disabled");
            return List.of();
        }
    };

    ExtensionDiscoverer DEFAULT = extensionDirectory -> {
        List<ExtensionDescriptor> allDescriptors = new ArrayList<>();
        FILESYSTEM.discover(extensionDirectory).forEach(allDescriptors::add);
        INDEV.discover(extensionDirectory).forEach(allDescriptors::add);
        AUTOSCAN.discover(extensionDirectory).forEach(allDescriptors::add);
        return allDescriptors;
    };

    @Nullable
    private static <T> T findExtensionManifest(String name, String filename, Function<String, T> getEntry, Predicate<T> validator) {
        T entry = getEntry.apply("META-INF/" + filename);
        if (!validator.test(entry)) {
            // Legacy location
            entry = getEntry.apply(filename);
            if (validator.test(entry)) {
                LoggerFactory.getLogger("Deprecation") // A bit weird, but the interface should not have a logger.
                        .warn("The extension.json for {} is in the root of the extension, it should be in META-INF.", name);
            } else return null;
        }
        return entry;
    }
}
