package net.minestom.server.item.component;

import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.EntityType;
import net.minestom.server.registry.RegistryTag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class MobVisibilityTest extends AbstractItemComponentRegistriesTest<MobVisibility> {
    private static final RegistryTag<EntityType> TARGETS = RegistryTag.direct(EntityType.ZOMBIE, EntityType.SKELETON);

    @Override
    protected DataComponent<MobVisibility> component() {
        return DataComponents.MOB_VISIBILITY;
    }

    @Override
    protected List<Map.Entry<String, MobVisibility>> directReadWriteEntries() {
        return List.of(
                Map.entry("hidden", new MobVisibility(TARGETS, 0f)),
                Map.entry("half", new MobVisibility(TARGETS, 0.5f)),
                Map.entry("max", new MobVisibility(TARGETS, 10f))
        );
    }

    @Test
    public void outOfRange() {
        assertThrows(IllegalArgumentException.class, () -> new MobVisibility(TARGETS, -0.1f));
        assertThrows(IllegalArgumentException.class, () -> new MobVisibility(TARGETS, 10.1f));
    }
}
