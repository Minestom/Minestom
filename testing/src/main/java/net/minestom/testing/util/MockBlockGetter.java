package net.minestom.testing.util;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.HashMap;
import java.util.Map;

public class MockBlockGetter implements Block.Getter, Block.Setter {

    public static @NotNull MockBlockGetter empty() {
        return new MockBlockGetter(Map.of(), Block.AIR);
    }

    public static @NotNull MockBlockGetter single(@NotNull Block block) {
        return new MockBlockGetter(Map.of(Vec.ZERO, block), Block.AIR);
    }

    public static @NotNull MockBlockGetter all(@NotNull Block block) {
        return new MockBlockGetter(Map.of(), block);
    }

    private final Map<Vec, Block> blocks = new HashMap<>();
    private final Block defaultBlock;

    private MockBlockGetter(Map<Vec, Block> blocks, Block defaultBlock) {
        blocks.forEach((pos, block) -> this.blocks.put(new Vec(pos.blockX(), pos.blockY(), pos.blockZ()), block));
        this.defaultBlock = defaultBlock;
    }

    @Override
    public @UnknownNullability Block getBlock(int x, int y, int z, @NotNull Condition condition) {
        return blocks.getOrDefault(new Vec(x, y, z), defaultBlock);
    }

    @Override
    public void setBlock(int x, int y, int z, @NotNull Block block) {
        blocks.put(new Vec(x, y, z), block);
    }
}
