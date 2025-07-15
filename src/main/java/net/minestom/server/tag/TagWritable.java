package net.minestom.server.tag;

import org.jspecify.annotations.Nullable;
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
    <T> void setTag(Tag<T> tag, @Nullable T value);

    default void removeTag(Tag<?> tag) {
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
    <T> @Nullable T getAndSetTag(Tag<T> tag, @Nullable T value);

    <T> void updateTag(Tag<T> tag,
                       UnaryOperator<@UnknownNullability T> value);

    <T> @UnknownNullability T updateAndGetTag(Tag<T> tag,
                                              UnaryOperator<@UnknownNullability T> value);

    <T> @UnknownNullability T getAndUpdateTag(Tag<T> tag,
                                              UnaryOperator<@UnknownNullability T> value);
}
