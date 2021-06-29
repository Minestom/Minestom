package net.minestom.server.tag;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface used to create custom {@link Tag tags}.
 *
 * @param <T> the type to serialize
 */
public interface TagSerializer<T> {

    /**
     * Reads the custom tag from a {@link TagReadable}.
     *
     * @param reader the reader
     * @return the deserialized value, null if invalid
     */
    @Nullable T read(@NotNull TagReadable reader);

    /**
     * Writes the custom tag to a {@link TagWritable}.
     *
     * @param writer the writer
     * @param value  the value to serialize, null to remove
     */
    void write(@NotNull TagWritable writer, @Nullable T value);
}
