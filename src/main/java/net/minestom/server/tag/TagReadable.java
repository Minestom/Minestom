package net.minestom.server.tag;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

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
    <T> @Nullable T getTag(@NotNull Tag<T> tag);

    /**
     * Returns if a tag is present.
     *
     * @param tag the tag to check
     * @return true if the tag is present, false otherwise
     */
    default boolean hasTag(@NotNull Tag<?> tag) {
        return getTag(tag) != null;
    }

    /**
     * Converts an nbt compound to a tag reader.
     *
     * @param compound the compound to convert
     * @return a {@link TagReadable} capable of reading {@code compound}
     */
    static @NotNull TagReadable fromCompound(@NotNull NBTCompound compound) {
        return new TagReadable() {
            @Override
            public <T> @Nullable T getTag(@NotNull Tag<T> tag) {
                return tag.read(compound);
            }
        };
    }
}
