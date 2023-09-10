package net.minestom.server.instance;

/**
 * An exception thrown to indicate we are trying to perform an operation on an unloaded chunk.
 */
public final class ChunkNotLoadedException extends RuntimeException {

    ChunkNotLoadedException(int x, int y, int z) {
        super("Unloaded chunk at " + x + "," + y + "," + z);
    }
}
