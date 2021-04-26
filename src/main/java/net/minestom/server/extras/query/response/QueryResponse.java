package net.minestom.server.extras.query.response;

import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

/**
 * A query response.
 */
public interface QueryResponse {

    /**
     * Writes the query response to a writer.
     *
     * @param writer the writer to write the response to
     */
    void write(@NotNull BinaryWriter writer);
}
