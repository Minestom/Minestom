package net.minestom.server.tag;

import org.jspecify.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.function.UnaryOperator;

public interface Taggable extends TagReadable, TagWritable {

    TagHandler tagHandler();

    @Override
    default <T> @UnknownNullability T getTag(Tag<T> tag) {
        return tagHandler().getTag(tag);
    }

    @Override
    default boolean hasTag(Tag<?> tag) {
        return tagHandler().hasTag(tag);
    }

    @Override
    default <T> void setTag(Tag<T> tag, @Nullable T value) {
        tagHandler().setTag(tag, value);
    }

    @Override
    default void removeTag(Tag<?> tag) {
        tagHandler().removeTag(tag);
    }

    @Override
    default <T> @Nullable T getAndSetTag(Tag<T> tag, @Nullable T value) {
        return tagHandler().getAndSetTag(tag, value);
    }

    @Override
    default <T> void updateTag(Tag<T> tag, UnaryOperator<@UnknownNullability T> value) {
        tagHandler().updateTag(tag, value);
    }

    @Override
    default <T> @UnknownNullability T updateAndGetTag(Tag<T> tag, UnaryOperator<@UnknownNullability T> value) {
        return tagHandler().updateAndGetTag(tag, value);
    }

    @Override
    default <T> @UnknownNullability T getAndUpdateTag(Tag<T> tag, UnaryOperator<@UnknownNullability T> value) {
        return tagHandler().getAndUpdateTag(tag, value);
    }
}
