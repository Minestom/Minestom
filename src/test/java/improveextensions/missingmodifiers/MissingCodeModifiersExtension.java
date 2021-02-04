package improveextensions.missingmodifiers;

import net.minestom.server.MinecraftServer;
import net.minestom.server.extensions.Extension;
import org.junit.jupiter.api.Assertions;
import org.opentest4j.AssertionFailedError;

public class MissingCodeModifiersExtension extends Extension {

    @Override
    public void initialize() {
        // force load of InstanceContainer class
        try {
            Assertions.assertFalse(areCodeModifiersAllLoadedCorrectly(), "Mixin configuration could not be loaded and code modifiers are unavailable, the failure should be reported");
            Assertions.assertTrue(getDescription().hasFailedToLoadMixin(), "Mixin configuration does not exist and should not be loaded");
            Assertions.assertEquals(1, getDescription().getMissingCodeModifiers().size(), "Code modifier does not exist, it should be reported as missing");
            Assertions.assertEquals("InvalidCodeModifierClass", getDescription().getMissingCodeModifiers().get(0));
            System.out.println("All tests passed.");
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
