package net.minestom.server.item.component;

import net.kyori.adventure.nbt.IntBinaryTag;
import net.minestom.server.component.DataComponent;
import net.minestom.server.item.ItemComponent;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DyedItemColorTest extends AbstractItemComponentTest<DyedItemColor> {

    @Override
    protected @NotNull DataComponent<DyedItemColor> component() {
        return ItemComponent.DYED_COLOR;
    }

    @Override
    protected @NotNull List<Map.Entry<String, DyedItemColor>> directReadWriteEntries() {
        return List.of(
                entry("default leather", DyedItemColor.LEATHER),
                entry("no tooltip", new DyedItemColor(0xCAFEBB, false))
        );
    }

    @Test
    void alternativeSyntax() {
        var value = ItemComponent.DYED_COLOR.read(BinaryTagSerializer.Context.EMPTY, IntBinaryTag.intBinaryTag(16777045));
        assertEquals(new DyedItemColor(16777045), value);
    }
}
