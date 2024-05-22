package net.minestom.server.entity;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class EntityMetaTest {

    @Test
    void ensureRegistration() throws IllegalAccessException {
        List<String> list = new ArrayList<>();
        for (var field : EntityTypes.class.getDeclaredFields()) {
            final EntityType entityType = (EntityType) field.get(this);
            final String name = entityType.name();
            if (EntityTypeImpl.ENTITY_META_SUPPLIER.get(name) == null) {
                list.add(name);
            }
        }
        assertTrue(list.isEmpty(), "Missing meta for: " + list);
    }
}
