package net.minestom.server.extensions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
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
        addTestFile("test.jar", "test", false);
        var result = FILESYSTEM.discover(extensionDirectory);

        assertEquals(1, result.size());
    }

    @Test
    public void testHandleMultipleFiles() throws IOException {
        addTestFile("test1.jar", "test1", false);
        addTestFile("test2.jar", "test2", false);
        var result = FILESYSTEM.discover(extensionDirectory);

        assertEquals(2, result.size());
        assertEquals("test1", result.get(0).name());
        assertEquals("test2", result.get(1).name());
    }

    @ParameterizedTest
    @ValueSource(strings = {"test.something", "test.jar.dis", "no_extension", ".only_extension"})
    public void testHandleNonStandardFileNames(String filename) throws IOException {
        addTestFile(filename, "test", false);
        var result = FILESYSTEM.discover(extensionDirectory);

        assertEquals(0, result.size());
    }

    @Test
    public void testIgnoreFilesWithoutExtensionManifest() throws IOException {
        Files.createFile(extensionDirectory.resolve("no_extension_manifest.jar"));
        var result = FILESYSTEM.discover(extensionDirectory);

        assertEquals(0, result.size());
    }

    private void addTestFile(String filename, String name, boolean legacy) throws IOException {
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
    // - nothing if one or both are missing
    // - extension added if present
    // - extension should have correct classpath
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

    //
    // Autoscan
    //
}
