package net.minestom.server.instance.chunksystem;

import net.minestom.server.instance.Chunk;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link ChunkTicket} is an object that can keep a chunk loaded/load new chunks.
 * {@link ChunkTicket}s are usually specific for a chunk, meaning it is for chunk (X,Z) and only for that chunk.
 * <p>
 * A chunk can have multiple identical {@link ChunkTicket}s, so that {@code ticket1.equals(ticket2)} equals {@code true}.
 * In case of explicit removal, only one {@link ChunkTicket} may be removed from the chunk (per call).
 * <p>
 * The {@link Chunk} reference is not in this class, but rather in {@link ChunkAndTicket}.
 * This is to reduce the memory taken up by these tickets, and to reduce references to a {@link Chunk}
 *
 * @param radius    the radius of this {@link ChunkTicket}. Use 0 for a single chunk.
 * @param priority  the priority of this {@link ChunkTicket}. Higher priorities are loaded first.
 * @param tickAdded at which tick this {@link ChunkTicket} was added to a chunk.
 */
public record ChunkTicket(int radius, int priority, int tickAdded) implements Comparable<ChunkTicket> {
    /**
     * Compares two ChunkTickets by their radius in order to quickly access the ChunkTicket with the highest radius from a sorted collection.
     * This will be used mostly by the chunk system to determine which radius to propagate to neighbouring chunks.
     */
    @Override
    public int compareTo(@NotNull ChunkTicket o) {
        return Integer.compare(radius, o.radius);
    }
}
