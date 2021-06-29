package net.minestom.server.tag;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

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
     * Converts an nbt compound to a tag writer.
     *
     * @param compound the compound to convert
     * @return a {@link TagWritable} capable of writing {@code compound}
     */
    static @NotNull TagWritable fromCompound(@NotNull NBTCompound compound) {
        return new TagWritable() {
            @Override
            public <T> void setTag(@NotNull Tag<T> tag, @Nullable T value) {
                tag.write(compound, value);
            }
        };
    }
}
