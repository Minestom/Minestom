package net.minestom.server.instance.block.incubator;

import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagReadable;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface BlockType extends TagReadable {

    <T> @NotNull BlockType withProperty(@NotNull BlockProperty<T> property, @NotNull T value);

    <T> @NotNull BlockType withTag(@NotNull Tag<T> tag, @Nullable T value);

    @NotNull BlockType getDefaultBlock();

    @NotNull NamespaceID getNamespaceId();

    short getProtocolId();

}
