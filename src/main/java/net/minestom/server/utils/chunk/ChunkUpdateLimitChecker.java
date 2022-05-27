package net.minestom.server.utils.chunk;

import net.minestom.server.instance.Chunk;

import java.util.ArrayDeque;
import java.util.Queue;

public class ChunkUpdateLimitChecker {
    private final int historySize;
    private final Queue<Chunk> chunkHistory;

    public ChunkUpdateLimitChecker(int historySize) {
        this.historySize = historySize;
        this.chunkHistory = new ArrayDeque<>(historySize);
    }

    /**
     * Adds the chunk to the history
     *
     * @param chunk chunk to add
     * @return {@code true} if it's a new chunk in the history
     */
    public boolean addToHistory(Chunk chunk) {
        boolean result = !chunkHistory.contains(chunk);
        if (chunkHistory.size() == historySize) {
            chunkHistory.remove();
        }
        chunkHistory.offer(chunk);
        return result;
    }
}
