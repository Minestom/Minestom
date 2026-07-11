package net.minestom.server.item.component;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponents;

import java.util.List;
import java.util.Map;

import static java.util.Map.entry;

import net.minestom.server.entity.EntityType;

public class BeesTest extends AbstractItemComponentTest<List<Bee>> {
    private static final TypedCustomData<EntityType> SOME_DATA = new TypedCustomData<>(
            EntityType.BEE, CompoundBinaryTag.empty()
    );

    @Override
    protected DataComponent<List<Bee>> component() {
        return DataComponents.BEES;
    }

    @Override
    public List<Map.Entry<String, List<Bee>>> directReadWriteEntries() {
        return List.of(
                entry("empty", List.of()),
                entry("single", List.of(new Bee(SOME_DATA, 1, 2))),
                entry("multiple", List.of(new Bee(SOME_DATA, 1, 2), new Bee(SOME_DATA, 3, 4)))
        );
    }
}
