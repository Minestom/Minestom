package net.minestom.server.instance.block.incubator;

import org.jetbrains.annotations.NotNull;

public interface BlockType {

    <T> @NotNull BlockType withProperty(@NotNull BlockProperty<T> property, @NotNull T value);

    @NotNull BlockType getDefaultBlock();

    short getProtocolId();

}
