package net.minestom.server.utils.chunk;

import net.minestom.server.coordinate.CoordConversion;
import net.minestom.server.instance.Chunk;
import org.jetbrains.annotations.ApiStatus;

import java.util.Arrays;

/**
 * Allows to limit operations with recently operated chunks
 * <p>
 * {@link ChunkUpdateLimitChecker#historySize} defines how many last chunks will be remembered
 * to skip operations with them via {@link ChunkUpdateLimitChecker#addToHistory(Chunk)} returning {@code false}
 */
@ApiStatus.Internal
public final class ChunkUpdateLimitChecker {

    private final int historySize;
    private final long[] chunkHistory;

    public ChunkUpdateLimitChecker(int historySize) {
        this.historySize = Math.max(0, historySize);
        this.chunkHistory = new long[this.historySize];
        this.clearHistory();
    }

    public boolean isEnabled() {
        return historySize > 0;
    }

    /**
     * Adds the chunk to the history
     *
     * @param chunk chunk to add
     * @return {@code true} if it's a new chunk in the history
     */
    public boolean addToHistory(Chunk chunk) {
        if (!isEnabled()) {
            return true;
        }
        final long index = CoordConversion.chunkIndex(chunk.getChunkX(), chunk.getChunkZ());
        boolean result = true;
        final int lastIndex = historySize - 1;
        for (int i = 0; i <= lastIndex; i++) {
            if (chunkHistory[i] == index) {
                result = false;
            }
            if (i != lastIndex) {
                chunkHistory[i] = chunkHistory[i + 1];
            }
        }
        chunkHistory[lastIndex] = index;
        return result;
    }

    public void clearHistory() {
        Arrays.fill(this.chunkHistory, Long.MAX_VALUE);
    }
}
