package net.minestom.server.extensions;

import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.util.TestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;

@ExtendWith(MockitoExtension.class)
public class ExtensionManagerTest {
    private @TempDir Path dataRoot;
    private EventNode<Event> globalNode = EventNode.all("global");
    private ExtensionManager extensionManager;

    @BeforeEach
    public void setup() {
        extensionManager = new ExtensionManager(
                TestUtil.IGNORING_EXTENSION_MANAGER,
                globalNode, ExtensionDiscoverer.AUTOSCAN);
        extensionManager.setExtensionDataRoot(dataRoot);
    }

    @Test
    public void testingwoo() {
        System.out.println(dataRoot.toAbsolutePath());
        System.out.println(globalNode.getChildren());
        TestUtil.IGNORING_EXTENSION_MANAGER.setExceptionHandler(ignored -> {});

//        assertThat(IGNORING_EXTENSION_MANAGER)
//                .hasReceived(IllegalStateException.class)
    }


}
