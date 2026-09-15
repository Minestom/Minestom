package net.minestom.server.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EntityCreatureTest {

    @Test
    public void rejectNonLivingEntityType() {
        assertThrows(IllegalArgumentException.class, () -> new EntityCreature(EntityType.MINECART));
    }

    @Test
    public void acceptLivingEntityType() {
        assertDoesNotThrow(() -> new EntityCreature(EntityType.ZOMBIE));
    }
}
