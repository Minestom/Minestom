package net.minestom.server.instance.block.incubator;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

class BlockImpl implements BlockType {

    private final short id;
    private final List<BlockProperty<?>> properties;

    protected BlockImpl(short id, BlockProperty<?>... properties) {
        this.id = id;
        this.properties = Arrays.asList(properties);
    }

    @Override
    public @NotNull <T> BlockType withProperty(@NotNull BlockProperty<T> property, @NotNull T value) {
        if (properties.isEmpty()) {
            // This block doesn't have any state
            return this;
        }
        final int index = properties.indexOf(property);
        if (index == -1) {
            // Invalid state
            return this;
        }

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
