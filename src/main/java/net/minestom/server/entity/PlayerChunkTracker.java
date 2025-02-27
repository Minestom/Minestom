package net.minestom.server.entity;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.chunksystem.ChunkClaim;
import net.minestom.server.instance.chunksystem.ChunkManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for loading chunks around and sending chunks to the player
 */
public class PlayerChunkTracker {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerChunkTracker.class);
    private final Player player;
    private final LongSet visibleChunks = LongSets.synchronize(new LongOpenHashSet());
    private int priority = 0;
    private Tracked tracked = null;

    public PlayerChunkTracker(Player player) {
        this.player = player;
    }

    public void changeTracked(Tracked tracked) {

    }

    public void startTracking() {

    }

    public void stopTracking() {
        if (tracked == null) {
            LOGGER.warn("Tried to stop tracking when nothing was being tracked. This is most likely a logic error.", new Exception());
            return;
        }
        tracked.chunkManager.removeClaim(tracked.claim);
    }

    public Tracked addClaim(Instance instance, Point pos) {
        return addClaim(instance, pos.chunkX(), pos.chunkZ());
    }

    public Tracked addClaim(Instance instance, int chunkX, int chunkZ) {
        var chunkManager = instance.getChunkManager();
        var chunkAndClaim = chunkManager.addClaim(chunkX, chunkZ, this.player.getSettings().effectiveViewDistance(), this.priority, ChunkClaim.Shape.CIRCLE);
        var claim = chunkAndClaim.claim();
        return new Tracked(chunkManager, claim, chunkX, chunkZ);
    }

    public record Tracked(ChunkManager chunkManager, ChunkClaim claim, int chunkX, int chunkZ) {
    }
}
