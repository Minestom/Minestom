package net.minestom.server.component;

import net.kyori.adventure.key.Key;
import net.minestom.testing.RegistriesTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@RegistriesTest
public class DataComponentLookupRegistriesTest {
    @Test
    public void registry() { // Tricky registry; so we ensure they are loaded (requires class loading before accessible keys)
        Assertions.assertNotNull(DataComponent.fromKey(Key.key("lore")), "Registry class was not initialized");
    }

    @Test
    public void stringFromKey() {
        Assertions.assertSame(DataComponent.fromKey("lore"), DataComponent.fromKey(Key.key("lore")));
    }

    @Test
    public void testStatic() {
        Assertions.assertSame(DataComponents.LORE, DataComponent.fromKey("lore"));
    }
}
