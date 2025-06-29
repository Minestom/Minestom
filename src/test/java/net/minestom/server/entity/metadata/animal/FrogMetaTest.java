package net.minestom.server.entity.metadata.animal;

import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import org.junit.jupiter.api.Test;

class FrogMetaTest {
    @Test
    public void testSetVariant() {
        Entity frog = new Entity(EntityType.FROG);
        frog.set(DataComponents.FROG_VARIANT, FrogVariant.COLD);
    }
}