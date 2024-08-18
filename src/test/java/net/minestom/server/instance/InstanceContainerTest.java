package net.minestom.server.instance;

import net.minestom.server.tag.Tag;
import net.minestom.server.world.DimensionType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InstanceContainerTest {

    static {
    }

    @Test
    void copyPreservesTag() {
        var tag = Tag.String("test");
        var instance = new InstanceContainer(UUID.randomUUID(), DimensionType.OVERWORLD);
        instance.setTag(tag, "123");

        var copyInstance = instance.copy();
        var result = copyInstance.getTag(tag);
        assertEquals("123", result);
    }
}
