package net.minestom.server.item.component;

import net.kyori.adventure.nbt.IntBinaryTag;
import net.minestom.server.component.DataComponent;
import net.minestom.server.item.ItemComponent;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ItemRarityTest extends AbstractItemComponentTest<ItemRarity> {
    @Override
    protected @NotNull DataComponent<ItemRarity> component() {
        return ItemComponent.RARITY;
    }

    @Override
    protected @NotNull List<Map.Entry<String, ItemRarity>> directReadWriteEntries() {
        return List.of(
                Map.entry("common", ItemRarity.COMMON)
        );
    }

    @Test
    void testReadFromNbtInt() {
        var value = ItemRarity.NBT_TYPE.read(IntBinaryTag.intBinaryTag(2));
        assertEquals(ItemRarity.RARE, value);
    }
}
