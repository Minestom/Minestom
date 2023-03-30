package net.minestom.server.tag;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

public interface Taggable extends TagReadable, TagWritable {

    @NotNull TagHandler tagHandler();

    @Override
    default <T> @UnknownNullability T getTag(@NotNull Tag<T> tag) {
        return tagHandler().getTag(tag);
    }

    @Override
    default boolean hasTag(@NotNull Tag<?> tag) {
        return tagHandler().hasTag(tag);
    }

    @Override
    default <T> void setTag(@NotNull Tag<T> tag, @Nullable T value) {
        tagHandler().setTag(tag, value);
    }

    @Override
    default void removeTag(@NotNull Tag<?> tag) {
        tagHandler().removeTag(tag);
    }
}
