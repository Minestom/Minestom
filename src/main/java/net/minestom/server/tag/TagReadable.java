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
     * Returns the specified tag or a default value if it doesn't exist.
     *
     * @param tag the tag to read
     * @param defaultValue the fallback value if the tag isn't present
     * @param <T> the tag type
     * @return the read tag or the default value
     */
    default <T> T getTag(@NotNull Tag<T> tag, T defaultValue) {
        T value = getTag(tag);
        return value == null ? defaultValue : value;
    }

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
