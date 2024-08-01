package net.minestom.server.instance;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.block.BlockUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class BlockClientNbtTest {

    @Test
    public void basic() {
        assertNull(BlockUtils.extractClientNbt(Block.STONE));
        assertNull(BlockUtils.extractClientNbt(Block.GRASS_BLOCK));
        assertEquals(CompoundBinaryTag.empty(), BlockUtils.extractClientNbt(Block.CHEST));

        var nbt = CompoundBinaryTag.builder().putString("test", "test").build();
        assertEquals(nbt, BlockUtils.extractClientNbt(Block.CHEST.withNbt(nbt)));
    }

    @Test
    public void handler() {
        var handler = new BlockHandler() {
            @Override
            public @NotNull Collection<Tag<?>> getBlockEntityTags() {
                return List.of(Tag.String("test"));
            }

            @Override
            public @NotNull Key key() {
                return Key.key("minestom:test");
            }
        };

        var nbt = CompoundBinaryTag.builder().putString("test", "test").build();
        assertNull(BlockUtils.extractClientNbt(Block.STONE.withNbt(nbt).withHandler(handler)));
        assertEquals(nbt, BlockUtils.extractClientNbt(Block.CHEST.withNbt(nbt).withHandler(handler)));
        assertEquals(nbt, BlockUtils.extractClientNbt(Block.CHEST
                .withNbt(CompoundBinaryTag.builder().putString("test", "test").putString("test2", "test2").build())
                .withHandler(handler)));
    }
}
