package net.minestom.server.coordinate;

import org.jetbrains.annotations.NotNull;

/**
 * Helper class to iterate over chunks within a range.
 */
public final class ChunkRange {

    /**
     * Get the amount of chunks in a square range.
     *
     * @param range the range
     * @return the amount of chunks in the square range
     */
    public static int chunksCount(int range) {
        if (range < 0) throw new IllegalArgumentException("Range cannot be negative");
        final int square = range * 2 + 1;
        return square * square;
    }

    public static void chunksInRangeDiffering(int newChunkX, int newChunkZ,
                                              int oldChunkX, int oldChunkZ,
                                              int range, @NotNull ChunkConsumer callback) {
        for (int x = newChunkX - range; x <= newChunkX + range; x++) {
            for (int z = newChunkZ - range; z <= newChunkZ + range; z++) {
                if (Math.abs(x - oldChunkX) > range || Math.abs(z - oldChunkZ) > range) {
                    callback.accept(x, z);
                }
            }
        }
    }

    public static void chunksInRangeDiffering(int newChunkX, int newChunkZ,
                                              int oldChunkX, int oldChunkZ,
                                              int range,
                                              @NotNull ChunkConsumer newCallback, @NotNull ChunkConsumer oldCallback) {
        // Find the new chunks
        chunksInRangeDiffering(newChunkX, newChunkZ, oldChunkX, oldChunkZ, range, newCallback);
        // Find the old chunks
        chunksInRangeDiffering(oldChunkX, oldChunkZ, newChunkX, newChunkZ, range, oldCallback);
    }

    /**
     * New implementation comes from <a href="https://github.com/KryptonMC/Krypton/blob/a9eff5463328f34072cdaf37aae3e77b14fcac93/server/src/main/kotlin/org/kryptonmc/krypton/util/math/Maths.kt#L62">Krypton</a>
     * which comes from kotlin port by <a href="https://github.com/Esophose">Esophose</a>, which comes from <a href="https://stackoverflow.com/questions/398299/looping-in-a-spiral">a stackoverflow answer</a>.
     */
    public static void chunksInRange(int chunkX, int chunkZ, int range, ChunkConsumer consumer) {
        // Send in spiral around the center chunk
        // Note: its not really required to start at the center anymore since the chunk queue is sorted by distance,
        //       however we still should send a circle so this method is still fine, and good for any other case a
        //       spiral might be needed.
        consumer.accept(chunkX, chunkZ);
        for (int id = 1; id < (range * 2 + 1) * (range * 2 + 1); id++) {
            final int index = id - 1;
            // compute radius (inverse arithmetic sum of 8 + 16 + 24 + ...)
            final int radius = (int) Math.floor((Math.sqrt(index + 1.0) - 1) / 2) + 1;
            // compute total point on radius -1 (arithmetic sum of 8 + 16 + 24 + ...)
            final int p = 8 * radius * (radius - 1) / 2;
            // points by face
            final int en = radius * 2;
            // compute de position and shift it so the first is (-r, -r) but (-r + 1, -r)
            // so the square can connect
            final int a = (1 + index - p) % (radius * 8);
            switch (a / (radius * 2)) {
                // find the face (0 = top, 1 = right, 2 = bottom, 3 = left)
                case 0 -> consumer.accept(a - radius + chunkX, -radius + chunkZ);
                case 1 -> consumer.accept(radius + chunkX, a % en - radius + chunkZ);
                case 2 -> consumer.accept(radius - a % en + chunkX, radius + chunkZ);
                case 3 -> consumer.accept(-radius + chunkX, radius - a % en + chunkZ);
                default -> throw new IllegalStateException("unreachable");
            }
        }
    }

    public static void chunksInRange(@NotNull Point point, int range, ChunkConsumer consumer) {
        chunksInRange(point.chunkX(), point.chunkZ(), range, consumer);
    }

    @FunctionalInterface
    public interface ChunkConsumer {
        void accept(int chunkX, int chunkZ);
    }
}
