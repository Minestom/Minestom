package net.minestom.server.instance;

import net.minestom.server.tag.Tag;
import net.minestom.server.world.DimensionType;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnvTest
public class InstanceContainerTest {
    @Test
    public void copyPreservesTag(Env ignoredEnv) {
        var tag = Tag.String("test");
        var instance = new InstanceContainer(UUID.randomUUID(), DimensionType.OVERWORLD);
        instance.setTag(tag, "123");

        var copyInstance = instance.copy();
        var result = copyInstance.getTag(tag);
        assertEquals("123", result);
    }
}
