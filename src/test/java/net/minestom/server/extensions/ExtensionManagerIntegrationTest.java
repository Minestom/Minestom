package net.minestom.server.extensions;

import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.util.TestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ExtensionManagerIntegrationTest {
    private @TempDir Path dataRoot;
    private EventNode<Event> globalNode = EventNode.all("global");
    private ExtensionManager extensionManager;

    @BeforeEach
    public void setup() {
        extensionManager = new ExtensionManager(TestUtil.IGNORING_EXTENSION_MANAGER, globalNode);
        extensionManager.setExtensionDataRoot(dataRoot);
    }

    @Test
    public void testLoadEmptyExtension() throws IOException {
        copyRelevantExtensions("empty");

        extensionManager.start();

        assertNotNull(extensionManager.getExtension("empty"));
    }

    @Test
    public void testLoadDependentExtension() throws IOException {
        copyRelevantExtensions("basic_dependency", "empty");

        extensionManager.start();

        assertNotNull(extensionManager.getExtension("empty"));
        assertNotNull(extensionManager.getExtension("basic_dependency"));
    }

    // Test an extension that returns failed from preinitialize. It should not be loaded, or should a dependent of it (could be a variant of empty)
    // Exceptions from preinitialize & initialize

    private void copyRelevantExtensions(String... names) throws IOException {
        for (String name : names) {
            try (InputStream is = getClass().getClassLoader().getResourceAsStream("net/minestom/server/extension/" + name + ".jar")) {
                Files.copy(is, dataRoot.resolve(name + ".jar"));
            }
        }
    }
}
