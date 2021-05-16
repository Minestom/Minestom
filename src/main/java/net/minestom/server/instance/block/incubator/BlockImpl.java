package net.minestom.server.instance.block.incubator;

import org.jetbrains.annotations.NotNull;

class BlockImpl implements BlockType {

    private final short id;
    private final BlockProperty<?>[] properties;

    protected BlockImpl(short id, BlockProperty<?>... properties) {
        this.id = id;
        this.properties = properties;
    }

    @Override
    public @NotNull <T> BlockType withProperty(@NotNull BlockProperty<T> property, @NotNull T value) {
        return null;
    }

    @Override
    public @NotNull BlockType getDefaultBlock() {
        return this;
    }

    @Override
    public short getProtocolId() {
        return id;
    }
}
