package net.minestom.server.tag;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface used to create custom types compatible with {@link Tag#Custom(String, TagSerializer)}.
 *
 * @param <T> the type to serialize
 */
public interface TagSerializer<T> {

    /**
     * Reads the custom tag from a {@link TagReader}.
     *
     * @param reader the reader
     * @return the deserialized value
     */
    @Nullable T read(@NotNull TagReader reader);

    /**
     * Writes the custom tag to a {@link TagWriter}.
     *
     * @param writer the writer
     * @param value  the value to serialize
     */
    void write(@NotNull TagWriter writer, @NotNull T value);
}
