package net.minestom.server.item.component;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.adventure.MinestomAdventure;
import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CustomDataTest extends AbstractItemComponentTest<CustomData> {
    // This is not a test, but it creates a compile error if the component type is changed away,
    // as a reminder that tests should be added for that new component type.
    private static final List<DataComponent<CustomData>> SHARED_COMPONENTS = List.of(
            DataComponents.CUSTOM_DATA,
            DataComponents.ENTITY_DATA,
            DataComponents.BUCKET_ENTITY_DATA,
            DataComponents.BLOCK_ENTITY_DATA
    );

    @Override
    protected DataComponent<CustomData> component() {
        return SHARED_COMPONENTS.getFirst();
    }

    @Override
    protected List<Map.Entry<String, CustomData>> directReadWriteEntries() {
        return List.of(
                entry("simple", new CustomData(CompoundBinaryTag.builder()
                        .putString("hello", "world")
                        .put("nested", CompoundBinaryTag.builder()
                                .putInt("number", 42)
                                .build())
                        .build()))
        );
    }

    @Test
    void customDataTagPath() throws IOException {
        final ItemStack item = ItemStack.builder(Material.STICK)
                .set(Tag.Integer("num").path("test"), 5)
                .build();
        final String snbt = MinestomAdventure.tagStringIO().asString(item.get(DataComponents.CUSTOM_DATA).nbt());
        assertEquals("{test:{num:5}}", snbt);
    }
}
