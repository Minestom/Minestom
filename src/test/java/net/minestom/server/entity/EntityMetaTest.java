package net.minestom.server.entity;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class EntityMetaTest {

    @Test
    public void ensureRegistration() throws IllegalAccessException {
        List<String> list = new ArrayList<>();
        for (var field : EntityTypes.class.getDeclaredFields()) {
            final EntityType entityType = (EntityType) field.get(this);
            if (MetadataHolder.ENTITY_META_SUPPLIER.get(entityType) == null) {
                list.add(entityType.name());
            }
        }
        assertTrue(list.isEmpty(), "Missing meta for: " + list);
    }
}
