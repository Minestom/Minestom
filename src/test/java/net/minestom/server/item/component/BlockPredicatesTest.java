package net.minestom.server.item.component;

import net.kyori.adventure.nbt.TagStringIOExt;
import net.minestom.server.component.DataComponent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.predicate.BlockPredicate;
import net.minestom.server.item.ItemComponent;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BlockPredicatesTest extends AbstractItemComponentTest<BlockPredicates> {

    @Override
    protected @NotNull DataComponent<BlockPredicates> component() {
        return ItemComponent.CAN_PLACE_ON; // CAN_BREAK is the same thing
    }

    @Override
    protected @NotNull List<Map.Entry<String, BlockPredicates>> directReadWriteEntries() {
        return List.of(
                entry("empty", new BlockPredicates(List.of(), true)),
                entry("single, no tooltip", new BlockPredicates(BlockPredicate.ALL, false)),
                entry("many", new BlockPredicates(List.of(BlockPredicate.ALL, BlockPredicate.NONE), true))
        );
    }

    @Test
    public void testSingleBlockNbtInput() throws IOException {
        var tag = TagStringIOExt.readTag("{blocks:'minecraft:stone'}");
        var component = ItemComponent.CAN_PLACE_ON.read(BinaryTagSerializer.Context.EMPTY, tag);
        var expected = new BlockPredicates(new BlockPredicate(Block.STONE));
        assertEquals(expected, component);
    }

    @Test
    public void testMultiMatch() {
        // Just sanity check that it actually runs both of the predicates
        var predicate = new BlockPredicates(List.of(BlockPredicate.NONE, BlockPredicate.ALL), true);
        assertTrue(predicate.test(Block.AIR));
    }

}
