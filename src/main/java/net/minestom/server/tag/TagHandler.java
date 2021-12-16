package net.minestom.server.tag;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound;

/**
 * Represents an element which can read and write {@link Tag tags}.
 */
@ApiStatus.Experimental
public interface TagHandler extends TagReadable, TagWritable {

    /**
     * Converts a nbt compound to a tag handler.
     * <p>
     * The returned tag handler is not thread-safe.
     *
     * @param compound the compound to convert
     * @return a {@link TagHandler} capable of writing and reading {@code compound}
     */
    static @NotNull TagHandler fromCompound(@NotNull MutableNBTCompound compound) {
        return new TagHandler() {
            @Override
            public <T> @Nullable T getTag(@NotNull Tag<T> tag) {
                return tag.read(compound);
            }

            @Override
            public <T> void setTag(@NotNull Tag<T> tag, @Nullable T value) {
                tag.write(compound, value);
            }
        };
    }
}
