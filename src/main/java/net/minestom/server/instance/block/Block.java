package net.minestom.server.instance.block;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagReadable;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("removal")
public interface Block extends Keyed, TagReadable, BlockOld {

    <T> @NotNull Block withProperty(@NotNull BlockProperty<T> property, @NotNull T value);

    <T> @NotNull Block withTag(@NotNull Tag<T> tag, @Nullable T value);

    @NotNull Block getDefaultBlock();

    @NotNull NamespaceID getNamespaceId();

    @Override
    default @NotNull Key key() {
        return getNamespaceId();
    }

    short getProtocolId();

}
