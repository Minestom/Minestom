package net.minestom.server.tag;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound;

/**
 * Represents an element which can write {@link Tag tags}.
 */
public interface TagWritable {

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
     * Converts a nbt compound to a tag writer.
     * <p>
     * The returned tag writer is not thread-safe.
     *
     * @param compound the compound to convert
     * @return a {@link TagWritable} capable of writing {@code compound}
     */
    static @NotNull TagWritable fromCompound(@NotNull MutableNBTCompound compound) {
        return new TagWritable() {
            @Override
            public <T> void setTag(@NotNull Tag<T> tag, @Nullable T value) {
                tag.write(compound, value);
            }
        };
    }
}
