package net.minestom.server.extensions;

import net.minestom.server.extensions.descriptor.ExtensionDescriptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static net.minestom.server.extensions.ExtensionDiscoverer.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
        Files.createFile(extensionDirectory.resolve("no_extension_manifest.jar"));
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
    public void testIgnoreIndevIfMissingOneOrBoth() {
        String classes = Paths.get("classes").toAbsolutePath().toString();
        String resources = Paths.get("resources").toAbsolutePath().toString();

        {   // classes set, resources missing
            System.setProperty("minestom.extension.indevfolder.classes", classes);
            System.clearProperty("minestom.extension.indevfolder.resources");

            var result = INDEV.discover(extensionDirectory);
            assertEquals(0, result.size());
        }

        {   // classes set, resources missing
            System.clearProperty("minestom.extension.indevfolder.classes");
            System.setProperty("minestom.extension.indevfolder.resources", resources);

            var result = INDEV.discover(extensionDirectory);
            assertEquals(0, result.size());
        }

        {   // neither set
            System.clearProperty("minestom.extension.indevfolder.classes");
            System.clearProperty("minestom.extension.indevfolder.resources");

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

        System.setProperty("minestom.extension.indevfolder.classes", classes.toString());
        System.setProperty("minestom.extension.indevfolder.resources", resources.toString());
        var result = INDEV.discover(extensionDirectory);

        assertEquals(0, result.size());
    }

    @Test
    public void testFailIfMissingResourcesDirectory() throws IOException {
        Path classes = extensionDirectory.resolve("classes").toAbsolutePath();
        Path resources = extensionDirectory.resolve("resources").toAbsolutePath();
        Files.createDirectories(classes);

        System.setProperty("minestom.extension.indevfolder.classes", classes.toString());
        System.setProperty("minestom.extension.indevfolder.resources", resources.toString());
        var result = INDEV.discover(extensionDirectory);

        assertEquals(0, result.size());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void testIndevExtensionLoadedCorrectly(boolean legacy) throws IOException {
        Path classes = extensionDirectory.resolve("classes").toAbsolutePath();
        Path resources = extensionDirectory.resolve("resources").toAbsolutePath();
        addTestIndevDirs(legacy);

        System.setProperty("minestom.extension.indevfolder.classes", classes.toString());
        System.setProperty("minestom.extension.indevfolder.resources", resources.toString());
        var result = INDEV.discover(extensionDirectory);

        assertEquals(1, result.size());
        ExtensionDescriptor descriptor = result.get(0);
        assertEquals("test", descriptor.name());

        //todo ensure the classpath makes sense.
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
}
