package net.minestom.server.tag;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

/**
 * Represents an element which can write {@link Tag tags}.
 */
public interface TagWriter {

    /**
     * Writes the specified type.
     *
     * @param tag   the tag to write
     * @param value the tag value, null to remove
     * @param <T>   the tag type
     */
    <T> void setTag(@NotNull Tag<T> tag, @Nullable T value);

    /**
     * Converts an nbt compound to a tag writer.
     *
     * @param compound the compound to convert
     * @return a {@link TagWriter} capable of writing {@code compound}
     */
    static @NotNull TagWriter fromCompound(@NotNull NBTCompound compound) {
        return new TagWriter() {
            @Override
            public <T> void setTag(@NotNull Tag<T> tag, @Nullable T value) {
                tag.write(compound, value);
            }
        };
    }
}
