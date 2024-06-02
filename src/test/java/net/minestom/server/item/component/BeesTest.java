package net.minestom.server.item.component;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.component.DataComponent;
import net.minestom.server.item.ItemComponent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

import static java.util.Map.entry;

public class BeesTest extends AbstractItemComponentTest<List<Bee>> {
    private static final CustomData SOME_DATA = new CustomData(CompoundBinaryTag.builder()
            .putString("Id", "minecraft:bee")
            .build());

    @Override
    protected @NotNull DataComponent<List<Bee>> component() {
        return ItemComponent.BEES;
    }

    @Override
    public @NotNull List<Map.Entry<String, List<Bee>>> directReadWriteEntries() {
        return List.of(
                entry("empty", List.of()),
                entry("single", List.of(new Bee(SOME_DATA, 1, 2))),
                entry("multiple", List.of(new Bee(SOME_DATA, 1, 2), new Bee(SOME_DATA, 3, 4)))
        );
    }
}
