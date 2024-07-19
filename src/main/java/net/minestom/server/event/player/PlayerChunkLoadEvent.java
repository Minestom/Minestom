package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.instance.Chunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Called when a player receive a new chunk data.
 */
public class PlayerChunkLoadEvent implements PlayerInstanceEvent {

    private final Player player;
    private final int chunkX, chunkZ;
    private Chunk chunk;

    public PlayerChunkLoadEvent(@NotNull Player player, int chunkX, int chunkZ, @Nullable Chunk chunk) {
        validateChunk(chunk, chunkX, chunkZ);
        this.player = player;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.chunk = chunk;
    }

    public PlayerChunkLoadEvent(@NotNull Player player, int chunkX, int chunkZ) {
        this(player, chunkX, chunkZ, null);
    }

    private static void validateChunk(Chunk chunk, int chunkX, int chunkZ) {
        if (chunk == null) return;
        if (chunk.getChunkX() != chunkX || chunk.getChunkZ() != chunkZ)
            throw new IllegalArgumentException("Cannot send a chunk with differing chunk coordinates");
    }

    /**
     * Gets the chunk X.
     *
     * @return the chunk X
     */
    public int getChunkX() {
        return chunkX;
    }

    /**
     * Gets the chunk Z.
     *
     * @return the chunk Z
     */
    public int getChunkZ() {
        return chunkZ;
    }

    /**
     * The chunk that will be sent to the player.
     *
     * @return the chunk that will be sent to the player, null to use the instance's chunk
     */
    public @Nullable Chunk chunk() {
        return chunk;
    }

    /**
     * Sets the chunk that will be sent to the player. Note that if non-null, this chunk must have the same chunk
     * coordinates as specified by {@link PlayerChunkLoadEvent#getChunkX()} and {@link PlayerChunkLoadEvent#getChunkZ()}.
     * Otherwise, if null, the chunk sent will be the one retrieved from the instance.
     *
     * @param chunk the chunk that will be sent to the player
     * @throws IllegalArgumentException if the chunk coordinates of {@code chunk} differ from this event's coordinates
     */
    public void setChunk(@Nullable Chunk chunk) {
        validateChunk(chunk, chunkX, chunkZ);
        this.chunk = chunk;
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }
}
