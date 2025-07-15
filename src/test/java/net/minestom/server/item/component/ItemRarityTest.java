package net.minestom.server.item.component;

import net.minestom.server.codec.Transcoder;
import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponents;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static net.kyori.adventure.nbt.StringBinaryTag.stringBinaryTag;
import static net.minestom.server.codec.CodecAssertions.assertOk;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ItemRarityTest extends AbstractItemComponentTest<ItemRarity> {
    @Override
    protected DataComponent<ItemRarity> component() {
        return DataComponents.RARITY;
    }

    @Override
    protected List<Map.Entry<String, ItemRarity>> directReadWriteEntries() {
        return List.of(
                Map.entry("common", ItemRarity.COMMON)
        );
    }

    @Test
    void testReadFromNbtInt() {
        var value = assertOk(ItemRarity.CODEC.decode(Transcoder.NBT, stringBinaryTag("rare")));
        assertEquals(ItemRarity.RARE, value);
    }
}
