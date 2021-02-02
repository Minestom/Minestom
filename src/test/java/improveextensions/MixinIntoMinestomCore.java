package improveextensions;

import net.minestom.server.MinecraftServer;
import net.minestom.server.extensions.Extension;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.world.DimensionType;
import org.junit.jupiter.api.Assertions;
import org.opentest4j.AssertionFailedError;

import java.util.UUID;

/**
 * Extensions should be able to use Mixins for classes loaded very early by Minestom (InstanceContainer for instance)
 */
public class MixinIntoMinestomCore extends Extension {

    public static boolean success = false;

    @Override
    public void initialize() {
        // force load of InstanceContainer class
        InstanceContainer c = new InstanceContainer(UUID.randomUUID(), DimensionType.OVERWORLD, null);
        System.out.println(c.toString());
        try {
            Assertions.assertTrue(success, "InstanceContainer must have been mixed in with improveextensions.InstanceContainerMixin");
            Assertions.assertEquals(1, MinecraftServer.getExtensionManager().getExtensionLoaders().size(), "Only one extension classloader (this extension's) must be active.");
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
