package net.minestom.server.utils.binary;

import org.jetbrains.annotations.NotNull;

/**
 * Represents an element which can read from a {@link BinaryReader}.
 */
public interface Readable {

    /**
     * Reads from a {@link BinaryReader}.
     *
     * @param reader the reader to read from
     */
    void read(@NotNull BinaryReader reader);

}
