package net.minestom.server.utils.binary;

import org.jetbrains.annotations.NotNull;

/**
 * Represents an element which can write to a {@link BinaryWriter}.
 */
public interface Writeable {

    /**
     * Writes into a {@link BinaryWriter}.
     *
     * @param writer the writer to write to
     */
    void write(@NotNull BinaryWriter writer);

}
