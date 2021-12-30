package net.minestom.server.extensions;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minestom.server.extensions.ExtensionDescriptor;
import net.minestom.server.extensions.HierarchyClassLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.*;

public class ExtensionDescriptorTest {
    private final HierarchyClassLoader EMPTY_CLASS_LOADER = new HierarchyClassLoader("<test>", new URL[0]);

    //
    // Validation Tests
    //

    @ParameterizedTest
    @ValueSource(strings = {"ab", "a.b", "ab", "a.b.b.b...b"})
    public void testValidNames(String name) {
        var exception = assertDoesNotThrow(
                () -> ExtensionDescriptor.newDescriptor(
                        name,
                        "1.0.0", List.of(), "entrypoint", List.of(), List.of(),
                        new JsonObject(), Paths.get("."), EMPTY_CLASS_LOADER)
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", ".", ".b", "a.", "aa."})
    public void testInvalidNames(String name) {
        var exception = assertThrowsExactly(
                IllegalArgumentException.class,
                () -> ExtensionDescriptor.newDescriptor(
                        name,
                        "1.0.0", List.of(), "entrypoint", List.of(), List.of(),
                        new JsonObject(), Paths.get("."), EMPTY_CLASS_LOADER)
        );

        assertTrue(exception.getMessage().startsWith("Invalid extension name: "),
                "Expected invalid name, received: " + exception.getMessage());
    }

    //
    // fromReader
    //

    @Test
    public void testReadFromReader(@TempDir Path tempDir) {
        InputStream is = getClass().getClassLoader().getResourceAsStream("META-INF/ext_descriptor_readFromReader.json");
        assertNotNull(is);

        // This test can be fairly basic because at the moment its just a call to `fromJson`
        Reader reader = new InputStreamReader(is);
        ExtensionDescriptor descriptor = assertDoesNotThrow(() -> ExtensionDescriptor.fromReader(reader, tempDir));
        assertEquals("ReadFromReader", descriptor.name());
    }

    //
    // fromJson
    //

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidManifestObjectProvider")
    public void testInvalidManifestObjects(String ignoredName, String expectedError, String jsonString, @TempDir Path tempDir) {
        JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();
        var exception = assertThrows(IllegalArgumentException.class,
                () -> ExtensionDescriptor.fromJson(json, tempDir));

        assertEquals(expectedError, exception.getMessage());

    }

    private static Stream<Arguments> invalidManifestObjectProvider() {
        return Stream.of(
                of("missing name", "Extensions must provide a name", """
                        {
                            
                        }
                        """),
                of("missing version", "Extensions must provide a version", """
                        {
                            "name": "Test"
                        }
                        """),
                of("authors as object", "Extension authors must be an array or single String.", """
                        {
                            "name": "Test",
                            "version": "1.0.0",
                            "authors": {}
                        }
                        """),
                of("authors as array of array", "Authors must be strings, not: []", """
                        {
                            "name": "Test",
                            "version": "1.0.0",
                            "authors": ["abc", []]
                        }
                        """),
                of("authors as array of object", "Authors must be strings, not: {}", """
                        {
                            "name": "Test",
                            "version": "1.0.0",
                            "authors": [
                                "abc", {}
                            ]
                        }
                        """),
                of("missing entrypoint", "Extensions must provide an entrypoint", """
                        {
                            "name": "Test",
                            "version": "1.0.0"
                        }
                        """),
                of("repositories as non-array", "Repositories must be an array, not: {}", """
                        {
                            "name": "Test",
                            "version": "1.0.0",
                            "entrypoint": "abc",
                            "repositories": {}
                        }
                        """),
                of("repository as non-object", "Repository definitions must be objects, not: []", """
                        {
                            "name": "Test",
                            "version": "1.0.0",
                            "entrypoint": "abc",
                            "repositories": [
                                []
                            ]
                        }
                        """),
                of("repository missing name", "Repository must have a name", """
                        {
                            "name": "Test",
                            "version": "1.0.0",
                            "entrypoint": "abc",
                            "repositories": [
                                {}
                            ]
                        }
                        """),
                of("repository missing url", "Repository must have a url", """
                        {
                            "name": "Test",
                            "version": "1.0.0",
                            "entrypoint": "abc",
                            "repositories": [
                                {
                                    "name": "Maven Central"
                                }
                            ]
                        }
                        """),
                of("dependencies as non-array", "Dependencies must be an array, not: {}", """
                        {
                            "name": "Test",
                            "version": "1.0.0",
                            "entrypoint": "abc",
                            "dependencies": {}
                        }
                        """),
                of("meta as non-object", "Extension meta must be an object, not: []", """
                        {
                            "name": "Test",
                            "version": "1.0.0",
                            "entrypoint": "abc",
                            "meta": []
                        }
                        """)
        );
    }


}
