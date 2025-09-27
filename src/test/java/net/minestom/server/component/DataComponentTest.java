package net.minestom.server.component;

import net.kyori.adventure.key.Key;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@EnvTest
public class DataComponentTest {
    @Test
    public void registry(Env env) { // Tricky registry; so we ensure they are loaded (requires class loading before accessible keys)
        Assertions.assertNotNull(DataComponent.fromKey(Key.key("lore")), "Registry class was not initialized");
    }

    @Test
    public void stringFromKey(Env env) {
        Assertions.assertSame(DataComponent.fromKey("lore"), DataComponent.fromKey(Key.key("lore")));
    }

    @Test
    public void testStatic(Env env) {
        Assertions.assertSame(DataComponents.LORE, DataComponent.fromKey("lore"));
    }
}
