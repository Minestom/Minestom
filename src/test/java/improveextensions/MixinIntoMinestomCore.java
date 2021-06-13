package improveextensions;

import net.minestom.server.MinecraftServer;
import net.minestom.server.extensions.Extension;
import net.minestom.server.world.WorldContainer;
import net.minestom.server.world.DimensionType;
import org.junit.jupiter.api.Assertions;
import org.opentest4j.AssertionFailedError;

import java.util.UUID;

/**
 * Extensions should be able to use Mixins for classes loaded very early by Minestom (WorldContainer for example)
 */
public class MixinIntoMinestomCore extends Extension {

    public static boolean success = false;

    @Override
    public void initialize() {
        // force load of WorldContainer class
        WorldContainer c = new WorldContainer(UUID.randomUUID(), DimensionType.OVERWORLD, null);
        System.out.println(c.toString());
        try {
            Assertions.assertTrue(success, "WorldContainer must have been mixed in with improveextensions.WorldContainerMixin");
            Assertions.assertEquals(1, MinecraftServer.getExtensionManager().getExtensions().stream().map(extension -> extension.getOrigin().getMinestomExtensionClassLoader()).toArray().length, "Only one extension classloader (this extension's) must be active.");
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
