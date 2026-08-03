package net.minestom.server.registry;

import net.minestom.server.component.DataComponentMap;
import net.minestom.testing.RegistriesTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@RegistriesTest
public class VanillaRegistriesTest {

    @Test
    void testMaterialPrototypes(Registries registries) {
        for (var entry : registries.material().values()) {
            var prototype = entry.prototype();
            Assertions.assertNotNull(prototype);
            if (prototype.isEmpty()) {
                Assertions.assertSame(DataComponentMap.EMPTY, prototype);
            }
        }
    }
}
