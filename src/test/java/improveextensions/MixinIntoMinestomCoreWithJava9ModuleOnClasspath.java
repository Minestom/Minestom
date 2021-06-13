package improveextensions;

import net.minestom.server.MinecraftServer;
import net.minestom.server.extensions.Extension;
import net.minestom.server.world.WorldContainer;
import net.minestom.server.world.DimensionType;
import org.junit.jupiter.api.Assertions;
import org.opentest4j.AssertionFailedError;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Extensions should be able to use Mixins for classes loaded very early by Minestom (WorldContainer for example)
 */
public class MixinIntoMinestomCoreWithJava9ModuleOnClasspath extends Extension {

    @Override
    public void initialize() {
        // use Mockito only to ensure J9 modules on the classpath are supported
        List mockedList = mock(List.class);
        when(mockedList.get(0)).thenReturn("Test");
        // force load of WorldContainer class
        WorldContainer c = new WorldContainer(UUID.randomUUID(), DimensionType.OVERWORLD, null);
        System.out.println(c.toString());
        try {
            Assertions.assertTrue(MixinIntoMinestomCore.success, "WorldContainer must have been mixed in with improveextensions.WorldContainerMixin");
            Assertions.assertEquals(1, MinecraftServer.getExtensionManager().getExtensions().stream().map(extension -> extension.getOrigin().getMinestomExtensionClassLoader()).toArray().length, "Only one extension classloader (this extension's) must be active.");
            Assertions.assertEquals("Test", mockedList.get(0));
        } catch (AssertionFailedError e) {
            e.printStackTrace();
        }
        MinecraftServer.stopCleanly();
    }

    @Override
    public void terminate() {
        getLogger().info("Terminate extension");
    }
}
