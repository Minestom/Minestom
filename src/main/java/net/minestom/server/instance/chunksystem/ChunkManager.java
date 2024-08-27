package net.minestom.server.instance.chunksystem;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * Manager for a ticket-based chunk system.
 * Every instance has a separate {@link ChunkManager}
 * {@code Instance#getChunkManager()}
 */
public interface ChunkManager {

    /**
     * The default priority for all chunk loads.
     *
     * @return the default priority
     */
    int getDefaultPriority();

    /**
     * Changes the default priority. This priority is used for loads if no priority is otherwise specified.
     *
     * @param priority the new default priority
     */
    void setDefaultPriority(int priority);

    /**
     * Adds a ticket to a chunk. The ticket will have radius 0 (single-chunk)
     * Adding a ticket can take an undefined period of time, chunk generation might have to happen first.
     * Tickets added with this method will make the chunk fully generate.
     *
     * @param chunkX the chunk X, in chunk coordinate space
     * @param chunkZ the chunk Z, in chunk coordinate space
     */
    @NotNull CompletableFuture<@NotNull ChunkAndTicket> addTicket(int chunkX, int chunkZ);

    /**
     * Adds a ticket to a chunk.
     * Adding a ticket can take an undefined period of time, chunk generation might have to happen first.
     * Tickets added with this method will make the chunk fully generate.
     *
     * @param chunkX the chunk X, in chunk coordinate space
     * @param chunkZ the chunk Z, in chunk coordinate space
     * @param radius the radius of this {@link ChunkTicket}. Use 0 to only load a single chunk.
     */
    @NotNull CompletableFuture<@NotNull ChunkAndTicket> addTicket(int chunkX, int chunkZ, int radius);

    /**
     * Adds a ticket to a chunk.
     * Adding a ticket can take an undefined period of time, chunk generation might have to happen first.
     * Tickets added with this method will make the chunk fully generate.
     *
     * @param chunkX   the chunk X, in chunk coordinate space
     * @param chunkZ   the chunk Z, in chunk coordinate space
     * @param radius   the radius of this {@link ChunkTicket}. Use 0 to only load a single chunk.
     * @param priority the priority of the ticket. Higher priorities get processed before lower priorities.
     * @return a future for when the ticket has been added successfully
     */
    @NotNull CompletableFuture<@NotNull ChunkAndTicket> addTicket(int chunkX, int chunkZ, int radius, int priority);

    /**
     * Removes a ticket from a chunk.
     * This should mostly be used to remove permanent {@link ChunkTicket}s.
     * Best practice is to use expiring {@link ChunkTicket}s and let the {@link ChunkManager} take care of removing them when they are no longer valid.
     *
     * @param chunkX the chunk X, in chunk coordinate space
     * @param chunkZ the chunk Z, in chunk coordinate space
     * @param ticket the {@link ChunkTicket} that should be removed
     * @return a future for when the ticket was removed.
     * @implNote ideally the ticket is removed as soon as the method returns (the future is already completed), this is not a requirement though.
     */
    @NotNull CompletableFuture<Void> removeTicket(int chunkX, int chunkZ, @NotNull ChunkTicket ticket);
}
