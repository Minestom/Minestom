package net.minestom.server.tag;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.function.UnaryOperator;

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

    @Override
    default <T> @Nullable T getAndSetTag(@NotNull Tag<T> tag, @Nullable T value) {
        return tagHandler().getAndSetTag(tag, value);
    }

    @Override
    default <T> void updateTag(@NotNull Tag<T> tag, @NotNull UnaryOperator<@UnknownNullability T> value) {
        tagHandler().updateTag(tag, value);
    }

    @Override
    default <T> @UnknownNullability T updateAndGetTag(@NotNull Tag<T> tag, @NotNull UnaryOperator<@UnknownNullability T> value) {
        return tagHandler().updateAndGetTag(tag, value);
    }

    @Override
    default <T> @UnknownNullability T getAndUpdateTag(@NotNull Tag<T> tag, @NotNull UnaryOperator<@UnknownNullability T> value) {
        return tagHandler().getAndUpdateTag(tag, value);
    }
}
