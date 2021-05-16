package net.minestom.server.instance.block.incubator;

import org.jetbrains.annotations.NotNull;

class BlockStateImpl implements BlockType {

    private final BlockType original;
    private final short id;

    protected BlockStateImpl(BlockType original, short id) {
        this.original = original;
        this.id = id;
    }

    @Override
    public @NotNull <T> BlockType withProperty(@NotNull BlockProperty<T> property, @NotNull T value) {
        return null;
    }

    @Override
    public @NotNull BlockType getDefaultBlock() {
        return original;
    }

    @Override
    public short getProtocolId() {
        return id;
    }
}
