package net.minestom.server.item.component;

import net.minestom.server.adventure.MinestomAdventure;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponents;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.predicate.BlockPredicate;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static java.util.Map.entry;
import static net.minestom.server.codec.CodecAssertions.assertOk;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

public class BlockPredicatesTest extends AbstractItemComponentTest<BlockPredicates> {

    @Override
    protected DataComponent<BlockPredicates> component() {
        return DataComponents.CAN_PLACE_ON; // CAN_BREAK is the same thing
    }

    @Override
    protected List<Map.Entry<String, BlockPredicates>> directReadWriteEntries() {
        return List.of(
                // TODO(1.21.5)
                entry("empty", new BlockPredicates(List.of()))
//                entry("single, no tooltip", new BlockPredicates(BlockPredicate.ALL)),
//                entry("many", new BlockPredicates(List.of(BlockPredicate.ALL, BlockPredicate.NONE)))
        );
    }

    @Test
    public void testSingleBlockNbtInput() throws IOException {
        assumeFalse(true, "TODO(1.21.5)");
        var tag = MinestomAdventure.tagStringIO().asTag("{blocks:'minecraft:stone'}");
        var component = assertOk(DataComponents.CAN_PLACE_ON.decode(Transcoder.NBT, tag));
        var expected = new BlockPredicates(new BlockPredicate(Block.STONE));
        assertEquals(expected, component);
    }

    @Test
    public void testMultiMatch() {
        // Just sanity check that it actually runs both of the predicates
        var predicate = new BlockPredicates(List.of(BlockPredicate.NONE, BlockPredicate.ALL));
        assertTrue(predicate.test(Block.AIR));
    }

}
