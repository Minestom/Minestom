package net.minestom.server.instance;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@EnvTest
public class DefaultBlockHandlerTest {

    private static final BiFunction<Key, Boolean, BlockHandler> BLOCK_HANDLER_SUPPLIER = (key, isDefault) -> new BlockHandler() {
        @Override
        public @NotNull Key getKey() {
            return key;
        }

        @Override
        public boolean defaultHandler() {
            return isDefault;
        }
    };

    @Test
    public void testDefaultHandler(Env env) {
        final BlockHandler stoneHandler = BLOCK_HANDLER_SUPPLIER.apply(Block.STONE.key(), true);
        env.process().block().registerDefaultHandlerForAllStates(stoneHandler);
        assertNull(Block.STONE.handler());
        assertEquals(stoneHandler, env.process().block().getBlockHandler(Block.STONE));
    }

    @Test
    public void testDefaultHandlerForState(Env env) {
        final BlockHandler noteBlockHandler = BLOCK_HANDLER_SUPPLIER.apply(Block.NOTE_BLOCK.key(), true);
        env.process().block().registerDefaultHandlerForState(noteBlockHandler, Block.NOTE_BLOCK);
        assertNull(Block.NOTE_BLOCK.handler());
        assertEquals(noteBlockHandler, env.process().block().getBlockHandler(Block.NOTE_BLOCK));
        assertNotEquals(noteBlockHandler, env.process().block().getBlockHandler(Block.NOTE_BLOCK.withProperty("note", "1")));
    }

    @Test
    public void testDefaultHandlerNbt(Env env) {
        final BlockHandler noteBlockHandler = BLOCK_HANDLER_SUPPLIER.apply(Block.NOTE_BLOCK.key(), true);
        env.process().block().registerDefaultHandlerForState(noteBlockHandler, Block.NOTE_BLOCK);
        assertNull(Block.NOTE_BLOCK.handler());
        assertEquals(noteBlockHandler, env.process().block().getBlockHandler(Block.NOTE_BLOCK.withNbt(CompoundBinaryTag.builder().putString("test", "test").build())));
    }

}
