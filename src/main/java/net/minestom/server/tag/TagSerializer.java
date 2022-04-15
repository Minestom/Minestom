package net.minestom.server.tag;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.function.Function;

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
     * @param value  the value to serialize
     */
    void write(@NotNull TagWritable writer, @NotNull T value);

    @ApiStatus.Experimental
    static <T> TagSerializer<T> fromCompound(@NotNull Function<NBTCompound, T> reader,
                                             @NotNull Function<T, NBTCompound> writer) {
        return TagSerializerImpl.fromCompound(reader, writer);
    }
}
