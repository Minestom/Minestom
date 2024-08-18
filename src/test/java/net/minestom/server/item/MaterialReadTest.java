package net.minestom.server.item;

import net.minestom.server.MinecraftServer;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@EnvTest
public class MaterialReadTest {

    static {
        MinecraftServer.init();
    }

    @Test
    void loadAllMaterials() {
        // Materials are lazy loaded now so this is a sanity check that they all load
        for (Material material : Material.values()) {
            // Just loading the material should be enough to test that it exists
            assertNotNull(material.prototype());
        }
    }

}
