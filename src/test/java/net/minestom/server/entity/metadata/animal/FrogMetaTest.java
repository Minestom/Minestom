package net.minestom.server.entity.metadata.animal;

import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Metadata;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

class FrogMetaTest {
    @Test
    public void testSetVariant() {
        Entity frog = new Entity(EntityType.FROG);
        frog.set(DataComponents.FROG_VARIANT, FrogVariant.COLD);
    }

    @Test
    public void testSerializeVariant() {
        // Variant is serialized as meta as of 1.21.6
        Entity frog = new Entity(EntityType.FROG);
        frog.set(DataComponents.FROG_VARIANT, FrogVariant.COLD);
        boolean found = false;
        for (Map.Entry<Integer, Metadata.Entry<?>> entry : frog.getMetadataPacket().entries().entrySet()) {
            if (entry.getValue().type() == Metadata.TYPE_FROG_VARIANT) {
                Assertions.assertEquals(FrogVariant.COLD, entry.getValue().value());
                found = true;
            }
        }

        Assertions.assertTrue(found, "Frog variant was not serialized");
    }
}