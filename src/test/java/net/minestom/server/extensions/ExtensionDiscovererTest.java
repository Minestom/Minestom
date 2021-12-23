package net.minestom.server.extensions;

import net.minestom.server.extensions.descriptor.ExtensionDescriptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static net.minestom.server.extensions.ExtensionDiscoverer.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.of;

public class ExtensionDiscovererTest {
    @TempDir
    private Path extensionDirectory;

    //
    // Filesystem
    //

    @Test
    public void testHandleMissingExtensionDirectory() {
        var result = FILESYSTEM.discover(Paths.get("./does/not/exist"));

        assertEquals(0, result.size());
    }

    @Test
    public void testHandleEmptyExtensionDirectory() {
        var result = FILESYSTEM.discover(extensionDirectory);

        assertEquals(0, result.size());
    }

    @Test
    public void testHandleSingleFile() throws IOException {
        addTestJarFile("test.jar", "test", false);
        var result = FILESYSTEM.discover(extensionDirectory);

        assertEquals(1, result.size());
    }

    @Test
    public void testHandleMultipleFiles() throws IOException {
        addTestJarFile("test1.jar", "test1", false);
        addTestJarFile("test2.jar", "test2", false);
        var result = FILESYSTEM.discover(extensionDirectory);

        assertEquals(2, result.size());
        assertEquals("test1", result.get(0).name());
        assertEquals("test2", result.get(1).name());
    }

    @ParameterizedTest
    @ValueSource(strings = {"test.something", "test.jar.dis", "no_extension", ".only_extension"})
    public void testHandleNonStandardFileNames(String filename) throws IOException {
        addTestJarFile(filename, "test", false);
        var result = FILESYSTEM.discover(extensionDirectory);

        assertEquals(0, result.size());
    }

    @Test
    public void testIgnoreFilesWithoutExtensionManifest() throws IOException {
        //todo i need to be a real zip file so it doesnt fail to open!
        Path file = extensionDirectory.resolve("no_extension_manifest.jar");
        Files.createFile(file);
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(file))) {
            // Just need to write zip header
        }
        var result = FILESYSTEM.discover(extensionDirectory);

        assertEquals(0, result.size());
    }

    private void addTestJarFile(String filename, String name, boolean legacy) throws IOException {
        Path file = extensionDirectory.resolve(filename);
        Files.createFile(file);

        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(file))) {
            ZipEntry entry = !legacy ?
                    new ZipEntry("META-INF/extension.json") :
                    new ZipEntry("extension.json");
            zip.putNextEntry(entry);
            zip.write("""
                    {
                        "name": "$NAME$",
                        "version": "1.0.0",
                        "entrypoint": "nonexistent"
                    }
                    """.replace("$NAME$", name).getBytes());
            zip.closeEntry();
        }
    }

    //
    // Indev property
    //

    @Test
    public void testIgnoreIndevIfMissingOneOrBoth() throws IOException {
        addTestIndevDirs(false);
        String classes = Paths.get("classes").toAbsolutePath().toString();
        String resources = Paths.get("resources").toAbsolutePath().toString();

        {   // classes set, resources missing
            System.setProperty(INDEV_CLASSES_PROPERTY, classes);
            System.clearProperty(INDEV_RESOURCES_PROPERTY);

            var result = INDEV.discover(extensionDirectory);
            assertEquals(0, result.size());
        }

        {   // classes set, resources missing
            System.clearProperty(INDEV_CLASSES_PROPERTY);
            System.setProperty(INDEV_RESOURCES_PROPERTY, resources);

            var result = INDEV.discover(extensionDirectory);
            assertEquals(0, result.size());
        }

        {   // neither set
            System.clearProperty(INDEV_CLASSES_PROPERTY);
            System.clearProperty(INDEV_RESOURCES_PROPERTY);

            var result = INDEV.discover(extensionDirectory);
            assertEquals(0, result.size());
        }
    }

    @Test
    public void testFailIfMissingClassesDirectory() throws IOException {
        Path classes = extensionDirectory.resolve("classes").toAbsolutePath();
        Path resources = extensionDirectory.resolve("resources").toAbsolutePath();
        addTestIndevDirs(false);
        Files.delete(classes);

        System.setProperty(INDEV_CLASSES_PROPERTY, classes.toString());
        System.setProperty(INDEV_RESOURCES_PROPERTY, resources.toString());
        var result = INDEV.discover(extensionDirectory);

        assertEquals(0, result.size());
    }

    @Test
    public void testFailIfMissingResourcesDirectory() throws IOException {
        Path classes = extensionDirectory.resolve("classes").toAbsolutePath();
        Path resources = extensionDirectory.resolve("resources").toAbsolutePath();
        Files.createDirectories(classes);

        System.setProperty(INDEV_CLASSES_PROPERTY, classes.toString());
        System.setProperty(INDEV_RESOURCES_PROPERTY, resources.toString());
        var result = INDEV.discover(extensionDirectory);

        assertEquals(0, result.size());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void testIndevExtensionLoadedCorrectly(boolean legacy) throws IOException {
        Path classes = extensionDirectory.resolve("classes").toAbsolutePath();
        Path resources = extensionDirectory.resolve("resources").toAbsolutePath();
        addTestIndevDirs(legacy);

        System.setProperty(INDEV_CLASSES_PROPERTY, classes.toString());
        System.setProperty(INDEV_RESOURCES_PROPERTY, resources.toString());
        var result = INDEV.discover(extensionDirectory);

        assertEquals(1, result.size());
        ExtensionDescriptor descriptor = result.get(0);
        assertEquals("test", descriptor.name());

        //todo ensure the classpath makes sense.
    }

    @Test
    public void testIndevMissingManifest() throws IOException {
        Path classes = extensionDirectory.resolve("classes").toAbsolutePath();
        Files.createDirectories(classes);
        Path resources = extensionDirectory.resolve("resources").toAbsolutePath();
        Files.createDirectories(resources);

        System.setProperty(INDEV_CLASSES_PROPERTY, classes.toString());
        System.setProperty(INDEV_RESOURCES_PROPERTY, resources.toString());
        var result = INDEV.discover(extensionDirectory);

        assertEquals(0, result.size());
    }

    private void addTestIndevDirs(boolean legacy) throws IOException {
        Path classes = extensionDirectory.resolve("classes");
        Files.createDirectories(classes);
        Path resources = extensionDirectory.resolve("resources");
        Files.createDirectories(resources);

        Path extensionJson = !legacy ?
                resources.resolve("META-INF/extension.json") :
                resources.resolve("extension.json");

        Files.createDirectories(extensionJson.getParent());
        Files.writeString(extensionJson, """
                {
                    "name": "test",
                    "version": "1.0.0",
                    "entrypoint": "nonexistent"
                }
                """);
    }

    //
    // Autoscan
    //

    @Test
    public void testAutoscanDisabled() {
        System.setProperty(AUTOSCAN_ENABLED_PROPERTY, "false");
        System.setProperty(AUTOSCAN_TARGETS_PROPERTY, "ext_discoverer_test1.json");
        var result = AUTOSCAN.discover(extensionDirectory);

        assertEquals(0, result.size());
    }

    @ParameterizedTest(name = "{1} exts loaded from {0}")
    @MethodSource("autoscanTargetCasesProvider")
    public void testAutoscanTargetCases(List<String> targets, int expectedLoaded) {
        System.setProperty(AUTOSCAN_ENABLED_PROPERTY, "true");
        System.setProperty(AUTOSCAN_TARGETS_PROPERTY,
                targets.stream()
                        .map(target -> "ext_discoverer_" + target + ".json")
                        .collect(Collectors.joining(",")));
        var result = AUTOSCAN.discover(extensionDirectory);

        assertEquals(expectedLoaded, result.size());
    }

    public static Stream<Arguments> autoscanTargetCasesProvider() {
        return Stream.of(
                of(List.of("test1"), 1),
                of(List.of("test1", "test2"), 2),
                of(List.of("test1", "missing", "test2"), 2),
                of(List.of("test3"), 1) // Legacy
        );
    }
}
