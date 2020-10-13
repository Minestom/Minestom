package net.minestom.server.instance.batch;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.BlockModifier;
import net.minestom.server.utils.thread.MinestomThread;

import java.util.concurrent.ExecutorService;

/**
 * A Batch is a tool used to cache a list of blocks to place and flush it whenever you want.
 * <p>
 * Does offer a performance benefit because clients are notified of the changes only once all the blocks are placed.
 */
public interface InstanceBatch extends BlockModifier {
    ExecutorService BLOCK_BATCH_POOL = new MinestomThread(MinecraftServer.THREAD_COUNT_BLOCK_BATCH, MinecraftServer.THREAD_NAME_BLOCK_BATCH);
}
