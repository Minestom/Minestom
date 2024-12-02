package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.network.packet.server.play.data.ChunkData;
import net.minestom.server.network.packet.server.play.data.LightData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Called when a player receive a new chunk data.
 */
public class PlayerChunkLoadEvent implements PlayerInstanceEvent {

    private final Player player;
    private final int chunkX, chunkZ;

    private ChunkData chunkData;
    private LightData lightData;

    public PlayerChunkLoadEvent(@NotNull Player player, int chunkX, int chunkZ) {
        this.player = player;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
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
     * Sets the chunk data for this event, replacing what would be sent to the client otherwise. Should often be used
     * alongside {@link PlayerChunkLoadEvent#setLightData(LightData)} to prevent lighting glitches on the client.
     *
     * @param chunkData the overriding chunk data
     * @see Chunk#getChunkData()
     */
    public void setChunkData(@Nullable ChunkData chunkData) {
        this.chunkData = chunkData;
    }

    /**
     * Sets the light data for this event, replacing what would be sent to the client otherwise. Should often be used
     * alongside {@link PlayerChunkLoadEvent#setChunkData(ChunkData)} to prevent lighting glitches on the client.
     *
     * @param lightData the overriding chunk data
     * @see Chunk#getLightData()
     */
    public void setLightData(@Nullable LightData lightData) {
        this.lightData = lightData;
    }

    /**
     * The overriding {@link ChunkData}. {@code null} indicates the instance chunk's data will be used instead.
     *
     * @return the overriding chunk data
     */
    public ChunkData chunkData() {
        return chunkData;
    }

    /**
     * The overriding {@link LightData}. {@code null} indicates the instance chunk's data will be used instead.
     *
     * @return the overriding chunk data
     */
    public LightData lightData() {
        return lightData;
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }
}
