package net.minestom.server.item.component;

import net.kyori.adventure.nbt.TagStringIOExt;
import net.minestom.server.component.DataComponent;
import net.minestom.server.item.ItemComponent;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class UnbreakableTest extends AbstractItemComponentTest<Unbreakable> {
    @Override
    protected @NotNull DataComponent<Unbreakable> component() {
        return ItemComponent.UNBREAKABLE;
    }

    @Override
    protected @NotNull List<Map.Entry<String, Unbreakable>> directReadWriteEntries() {
        return List.of(
                Map.entry("shown", new Unbreakable(true)),
                Map.entry("not shown", new Unbreakable(false))
        );
    }

    @Test
    void testDefaultNbtValue() throws IOException {
        var tag = TagStringIOExt.readTag("{}");
        var value = ItemComponent.UNBREAKABLE.read(BinaryTagSerializer.Context.EMPTY, tag);
        assertTrue(value.showInTooltip());
    }
}
