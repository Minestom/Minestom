package net.minestom.server.item.component;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.component.DataComponent;
import net.minestom.server.item.ItemComponent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

import static java.util.Map.entry;

public class CustomDataTest extends AbstractItemComponentTest<CustomData> {
    @Override
    protected @NotNull DataComponent<CustomData> component() {
        return ItemComponent.CUSTOM_DATA;
    }

    @Override
    protected @NotNull List<Map.Entry<String, CustomData>> directReadWriteEntries() {
        return List.of(
                entry("simple", new CustomData(CompoundBinaryTag.builder()
                        .putString("hello", "world")
                        .put("nested", CompoundBinaryTag.builder()
                                .putInt("number", 42)
                                .build())
                        .build()))
        );
    }
}
