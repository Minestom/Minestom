package net.minestom.server.tag;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

/**
 * Represents an element which can read {@link Tag tags}.
 */
public interface TagReadable {

    /**
     * Reads the specified tag.
     *
     * @param tag the tag to read
     * @param <T> the tag type
     * @return the read tag, null if not present
     */
    <T> @UnknownNullability T getTag(@NotNull Tag<T> tag);

    /**
     * Returns if a tag is present.
     *
     * @param tag the tag to check
     * @return true if the tag is present, false otherwise
     */
    default boolean hasTag(@NotNull Tag<?> tag) {
        return getTag(tag) != null;
    }
}
