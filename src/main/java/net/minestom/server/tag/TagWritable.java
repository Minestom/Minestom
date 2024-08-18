package net.minestom.server.tag;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.function.UnaryOperator;

/**
 * Represents an element which can write {@link Tag tags}.
 */
public interface TagWritable extends TagReadable {

    /**
     * Writes the specified type.
     *
     * @param tag   the tag to write
     * @param value the tag value, null to remove
     * @param <T>   the tag type
     */
    <T> void setTag(@NotNull Tag<T> tag, @Nullable T value);

    default void removeTag(@NotNull Tag<?> tag) {
        setTag(tag, null);
    }

    /**
     * Reads the current value, and then write the new one.
     *
     * @param tag   the tag to write
     * @param value the tag value, null to remove
     * @param <T>   the tag type
     * @return the previous tag value, null if not present
     */
    <T> @Nullable T getAndSetTag(@NotNull Tag<T> tag, @Nullable T value);

    <T> void updateTag(@NotNull Tag<T> tag,
                       @NotNull UnaryOperator<@UnknownNullability T> value);

    <T> @UnknownNullability T updateAndGetTag(@NotNull Tag<T> tag,
                                              @NotNull UnaryOperator<@UnknownNullability T> value);

    <T> @UnknownNullability T getAndUpdateTag(@NotNull Tag<T> tag,
                                              @NotNull UnaryOperator<@UnknownNullability T> value);
}
