package net.minestom.server.item.component;

import net.minestom.server.adventure.MinestomAdventure;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.IntBinaryTag;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponentMap;
import net.minestom.server.component.DataComponents;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.predicate.BlockPredicate;
import net.minestom.server.instance.block.predicate.ComponentPredicateSet;
import net.minestom.server.instance.block.predicate.DataComponentPredicate;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.registry.RegistryTag;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static java.util.Map.entry;
import static net.minestom.server.codec.CodecAssertions.assertOk;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BlockPredicatesTest extends AbstractItemComponentTest<BlockPredicates> {

    @Override
    protected DataComponent<@NotNull BlockPredicates> component() {
        return DataComponents.CAN_PLACE_ON; // CAN_BREAK is the same thing
    }

    @Override
    protected List<Map.Entry<String, BlockPredicates>> directReadWriteEntries() {
        CompoundBinaryTag testCompound = CompoundBinaryTag.builder().put("test", IntBinaryTag.intBinaryTag(123)).build();
        return List.of(
                entry("empty", new BlockPredicates(List.of())),
                entry("single, no tooltip", new BlockPredicates(BlockPredicate.ALL)),
                entry("many", new BlockPredicates(List.of(BlockPredicate.ALL, BlockPredicate.NONE))),
                entry("data component", new BlockPredicates(new BlockPredicate(DataComponentMap.builder().set(DataComponents.CUSTOM_DATA, new CustomData(testCompound)).build()))),
                entry("component predicate", new BlockPredicates(new BlockPredicate(new ComponentPredicateSet().add(new DataComponentPredicate.CustomData(testCompound)))))
        );
    }

    @Test
    public void testSingleBlockNbtInput() throws IOException {
        var tag = MinestomAdventure.tagStringIO().asTag("{blocks:'minecraft:stone'}");
        var component = assertOk(DataComponents.CAN_PLACE_ON.decode(Transcoder.NBT, tag));
        var expected = new BlockPredicates(new BlockPredicate(RegistryTag.direct(RegistryKey.unsafeOf("minecraft:stone"))));
        assertEquals(expected, component);
        assertEquals(1, component.predicates().getFirst().blocks().size());
        assertTrue(component.predicates().getFirst().blocks().contains(Block.STONE));
    }

    @Test
    public void testMultiMatch() {
        // Just sanity check that it actually runs both of the predicates
        var predicate = new BlockPredicates(List.of(BlockPredicate.NONE, BlockPredicate.ALL));
        assertTrue(predicate.test(Block.AIR));
    }

}
