package net.minestom.server.utils.chunk;

import net.minestom.server.instance.Chunk;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class ChunkUpdateLimitChecker {
    private final int maxHistorySize;
    private final long[] chunkHistory;
    private int historySize;

    public ChunkUpdateLimitChecker(int historySize) {
        this.maxHistorySize = historySize;
        this.chunkHistory = new long[historySize + 1];
        this.historySize = 0;
    }

    /**
     * Adds the chunk to the history
     *
     * @param index chunk index to add
     * @return {@code true} if it's a new chunk in the history
     */
    public boolean addToHistory(long index) {
        boolean result = true;
        final int lastIndex = historySize;
        for (int i = 0; i < lastIndex; i++) {
            if (chunkHistory[i] == index) {
                result = false;
                break;
            }
        }
        chunkHistory[lastIndex] = index;
        if (historySize < maxHistorySize) {
            historySize++;
        }
        else {
            System.arraycopy(chunkHistory, 1, chunkHistory, 0, lastIndex);
        }
        return result;
    }
}
