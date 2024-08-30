package net.minestom.server.utils.chunk;

import net.minestom.server.instance.Chunk;
import org.jetbrains.annotations.ApiStatus;

import java.util.Arrays;

@ApiStatus.Internal
public final class ChunkUpdateLimitChecker {
    private final int historySize;
    private final long[] chunkHistory;

    public ChunkUpdateLimitChecker(int historySize) {
        this.historySize = historySize;
        this.chunkHistory = new long[historySize];
        this.clearHistory();
    }

    /**
     * Adds the chunk to the history
     *
     * @param chunk chunk to add
     * @return {@code true} if it's a new chunk in the history
     */
    public boolean addToHistory(Chunk chunk) {
        final long index = ChunkUtils.getChunkIndex(chunk);
        boolean result = true;
        final int lastIndex = historySize - 1;
        for (int i = 0; i < lastIndex; i++) {
            if (chunkHistory[i] == index) {
                result = false;
            }
            chunkHistory[i] = chunkHistory[i + 1];
        }
        chunkHistory[lastIndex] = index;
        return result;
    }

    /**
     * Removes the chunk from the history
     *
     * @param index index of chunk to remove
     * @return {@code true} if the chunk existed in the history
     */
    public boolean removeFromHistory(long index) {
        final int lastIndex = historySize - 1;
        for (int i = 0; i < lastIndex; i++) {
            if (chunkHistory[i] == index) {
                chunkHistory[i] = Long.MAX_VALUE;
                return true;
            }
        }
        return false;
    }

    public void clearHistory() {
        Arrays.fill(this.chunkHistory, Long.MAX_VALUE);
    }
}
