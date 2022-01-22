package net.minestom.server.instance;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.block.BlockUtils;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class BlockClientNbtTest {

    @Test
    public void basic() {
        assertNull(BlockUtils.extractClientNbt(Block.STONE));
        assertNull(BlockUtils.extractClientNbt(Block.GRASS));
        assertEquals(NBTCompound.EMPTY, BlockUtils.extractClientNbt(Block.CHEST));

        var nbt = NBT.Compound(Map.of("test", NBT.String("test")));
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
            public @NotNull NamespaceID getNamespaceId() {
                return NamespaceID.from("minestom:test");
            }
        };

        var nbt = NBT.Compound(Map.of("test", NBT.String("test")));
        assertNull(BlockUtils.extractClientNbt(Block.STONE.withNbt(nbt).withHandler(handler)));
        assertEquals(nbt, BlockUtils.extractClientNbt(Block.CHEST.withNbt(nbt).withHandler(handler)));
        assertEquals(nbt, BlockUtils.extractClientNbt(Block.CHEST
                .withNbt(NBT.Compound(Map.of("test", NBT.String("test"), "test2", NBT.String("test"))))
                .withHandler(handler)));
    }
}
