package net.minestom.server.entity;

import net.minestom.server.api.Collector;
import net.minestom.server.api.Env;
import net.minestom.server.api.EnvTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.packet.server.play.EntityMetaDataPacket;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

public class EntityMetaTest {

    @Test
    public void ensureRegistration() throws IllegalAccessException {
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
