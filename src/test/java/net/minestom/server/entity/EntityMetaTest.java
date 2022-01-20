package net.minestom.server.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class EntityMetaTest {

    @Test
    public void ensureRegistration() throws IllegalAccessException {
        var fields = EntityTypes.class.getDeclaredFields();
        for (var field : fields) {
            EntityType entityType = (EntityType) field.get(this);
            assertNotNull(EntityTypeImpl.ENTITY_META_SUPPLIER.get(entityType.name()), "Meta for " + entityType.name() + " is null");
        }
    }
}
