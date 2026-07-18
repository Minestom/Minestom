package net.minestom.server.registry;

import net.minestom.server.component.DataComponentMap;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@EnvTest
public class RegistriesTest {

    @Test
    void testMaterialPrototypes() {
        var registries = Registries.vanilla();
        for (var entry : registries.material().values()) {
            var prototype = entry.prototype();
            Assertions.assertNotNull(prototype);
            if (prototype.isEmpty()) {
                Assertions.assertSame(DataComponentMap.EMPTY, prototype);
            }
        }
    }
}
