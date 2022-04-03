package net.minestom.server.utils.chunk

import net.minestom.server.coordinate.Point
import net.minestom.server.utils.chunk.ChunkUtils
import net.minestom.server.utils.chunk.ChunkCallback
import java.util.concurrent.CompletableFuture
import java.lang.Void
import java.util.concurrent.atomic.AtomicInteger
import net.minestom.server.utils.callback.OptionalCallback
import java.lang.IllegalArgumentException
import net.minestom.server.utils.function.IntegerBiConsumer
import net.minestom.server.coordinate.Vec
import net.minestom.server.instance.Chunk
import net.minestom.server.instance.Instance
import org.jetbrains.annotations.ApiStatus
import java.util.function.Consumer

@ApiStatus.Internal
object ChunkUtils {
    /**
     * Executes [Instance.loadOptionalChunk] for the array of chunks `chunks`
     * with multiple callbacks, `eachCallback` which is executed each time a new chunk is loaded and
     * `endCallback` when all the chunks in the array have been loaded.
     *
     *
     * Be aware that [Instance.loadOptionalChunk] can give a null chunk in the callback
     * if [Instance.hasEnabledAutoChunkLoad] returns false and the chunk is not already loaded.
     *
     * @param instance     the instance to load the chunks from
     * @param chunks       the chunks to loaded, long value from [.getChunkIndex]
     * @param eachCallback the optional callback when a chunk get loaded
     * @return a [CompletableFuture] completed once all chunks have been processed
     */
    @JvmStatic
    fun optionalLoadAll(
        instance: Instance, chunks: LongArray,
        eachCallback: ChunkCallback?
    ): CompletableFuture<Void?> {
        val completableFuture = CompletableFuture<Void?>()
        val counter = AtomicInteger(0)
        for (visibleChunk in chunks) {
            // WARNING: if autoload is disabled and no chunks are loaded beforehand, player will be stuck.
            instance.loadOptionalChunk(getChunkCoordX(visibleChunk), getChunkCoordZ(visibleChunk))
                .thenAccept(Consumer { chunk: Chunk? ->
                    OptionalCallback.execute(eachCallback, chunk)
                    val isLast = counter.get() == chunks.size - 1
                    if (isLast) {
                        // This is the last chunk to be loaded , spawn player
                        completableFuture.complete(null)
                    } else {
                        // Increment the counter of current loaded chunks
                        counter.incrementAndGet()
                    }
                })
        }
        return completableFuture
    }

    fun isLoaded(chunk: Chunk?): Boolean {
        return chunk != null && chunk.isLoaded
    }

    /**
     * Gets if a chunk is loaded.
     *
     * @param instance the instance to check
     * @param x        instance X coordinate
     * @param z        instance Z coordinate
     * @return true if the chunk is loaded, false otherwise
     */
    fun isLoaded(instance: Instance, x: Double, z: Double): Boolean {
        val chunk = instance.getChunk(getChunkCoordinate(x), getChunkCoordinate(z))
        return isLoaded(chunk)
    }

    @JvmStatic
    fun isLoaded(instance: Instance, point: Point): Boolean {
        val chunk = instance.getChunk(point.chunkX(), point.chunkZ())
        return isLoaded(chunk)
    }

    fun retrieve(instance: Instance, originChunk: Chunk?, x: Double, z: Double): Chunk? {
        val chunkX = getChunkCoordinate(x)
        val chunkZ = getChunkCoordinate(z)
        val sameChunk = originChunk != null && originChunk.chunkX == chunkX && originChunk.chunkZ == chunkZ
        return if (sameChunk) originChunk else instance.getChunk(chunkX, chunkZ)
    }

    @JvmStatic
    fun retrieve(instance: Instance, originChunk: Chunk?, position: Point): Chunk? {
        return retrieve(instance, originChunk, position.x(), position.z())
    }

    /**
     * @param xz the instance coordinate to convert
     * @return the chunk X or Z based on the argument
     */
    fun getChunkCoordinate(xz: Double): Int {
        return getChunkCoordinate(Math.floor(xz).toInt())
    }

    @JvmStatic
    fun getChunkCoordinate(xz: Int): Int {
        // Assume chunk/section size being 16 (4 bits)
        return xz shr 4
    }

    /**
     * Gets the chunk index of chunk coordinates.
     *
     *
     * Used when you want to store a chunk somewhere without using a reference to the whole object
     * (as this can lead to memory leaks).
     *
     * @param chunkX the chunk X
     * @param chunkZ the chunk Z
     * @return a number storing the chunk X and Z
     */
    fun getChunkIndex(chunkX: Int, chunkZ: Int): Long {
        return chunkX.toLong() shl 32 or (chunkZ and 0xffffffffL).toLong()
    }

    fun getChunkIndex(chunk: Chunk): Long {
        return getChunkIndex(chunk.chunkX, chunk.chunkZ)
    }

    @JvmStatic
    fun getChunkIndex(point: Point): Long {
        return getChunkIndex(point.chunkX(), point.chunkZ())
    }

    /**
     * Converts a chunk index to its chunk X position.
     *
     * @param index the chunk index computed by [.getChunkIndex]
     * @return the chunk X based on the index
     */
    @JvmStatic
    fun getChunkCoordX(index: Long): Int {
        return (index shr 32).toInt()
    }

    /**
     * Converts a chunk index to its chunk Z position.
     *
     * @param index the chunk index computed by [.getChunkIndex]
     * @return the chunk Z based on the index
     */
    @JvmStatic
    fun getChunkCoordZ(index: Long): Int {
        return index.toInt()
    }

    @JvmStatic
    fun getChunkCount(range: Int): Int {
        require(range >= 0) { "Range cannot be negative" }
        val square = range * 2 + 1
        return square * square
    }

    fun forDifferingChunksInRange(
        newChunkX: Int, newChunkZ: Int,
        oldChunkX: Int, oldChunkZ: Int,
        range: Int, callback: IntegerBiConsumer
    ) {
        for (x in newChunkX - range..newChunkX + range) {
            for (z in newChunkZ - range..newChunkZ + range) {
                if (Math.abs(x - oldChunkX) > range || Math.abs(z - oldChunkZ) > range) {
                    callback.accept(x, z)
                }
            }
        }
    }

    @JvmStatic
    fun forDifferingChunksInRange(
        newChunkX: Int, newChunkZ: Int,
        oldChunkX: Int, oldChunkZ: Int,
        range: Int,
        newCallback: IntegerBiConsumer, oldCallback: IntegerBiConsumer
    ) {
        // Find the new chunks
        forDifferingChunksInRange(newChunkX, newChunkZ, oldChunkX, oldChunkZ, range, newCallback)
        // Find the old chunks
        forDifferingChunksInRange(oldChunkX, oldChunkZ, newChunkX, newChunkZ, range, oldCallback)
    }

    fun forChunksInRange(chunkX: Int, chunkZ: Int, range: Int, consumer: IntegerBiConsumer) {
        for (x in -range..range) {
            for (z in -range..range) {
                consumer.accept(chunkX + x, chunkZ + z)
            }
        }
    }

    @JvmStatic
    fun forChunksInRange(point: Point, range: Int, consumer: IntegerBiConsumer) {
        forChunksInRange(point.chunkX(), point.chunkZ(), range, consumer)
    }

    /**
     * Gets the block index of a position.
     *
     * @param x the block X
     * @param y the block Y
     * @param z the block Z
     * @return an index which can be used to store and retrieve later data linked to a block position
     */
    @JvmStatic
    fun getBlockIndex(x: Int, y: Int, z: Int): Int {
        var x = x
        var z = z
        x = x % Chunk.CHUNK_SIZE_X
        z = z % Chunk.CHUNK_SIZE_Z
        var index = x and 0xF // 4 bits
        index = index or (y shl 4 and 0x0FFFFFF0) // 24 bits
        index = index or (z shl 28 and -0x10000000) // 4 bits
        return index
    }

    /**
     * @param index  an index computed from [.getBlockIndex]
     * @param chunkX the chunk X
     * @param chunkZ the chunk Z
     * @return the instance position of the block located in `index`
     */
    @JvmStatic
    fun getBlockPosition(index: Int, chunkX: Int, chunkZ: Int): Point {
        val x = blockIndexToChunkPositionX(index) + Chunk.CHUNK_SIZE_X * chunkX
        val y = index ushr 4 and 0xFF
        val z = blockIndexToChunkPositionZ(index) + Chunk.CHUNK_SIZE_Z * chunkZ
        return Vec(x.toDouble(), y.toDouble(), z.toDouble())
    }

    /**
     * Converts a block index to a chunk position X.
     *
     * @param index an index computed from [.getBlockIndex]
     * @return the chunk position X (O-15) of the specified index
     */
    @JvmStatic
    fun blockIndexToChunkPositionX(index: Int): Int {
        return index and 0xF // 0-4 bits
    }

    /**
     * Converts a block index to a chunk position Y.
     *
     * @param index an index computed from [.getBlockIndex]
     * @return the chunk position Y of the specified index
     */
    @JvmStatic
    fun blockIndexToChunkPositionY(index: Int): Int {
        return index shr 4 and 0x0FFFFFF // 4-28 bits
    }

    /**
     * Converts a block index to a chunk position Z.
     *
     * @param index an index computed from [.getBlockIndex]
     * @return the chunk position Z (O-15) of the specified index
     */
    @JvmStatic
    fun blockIndexToChunkPositionZ(index: Int): Int {
        return index shr 28 and 0xF // 28-32 bits
    }

    /**
     * Converts a global coordinate value to a section coordinate
     *
     * @param xyz global coordinate
     * @return section coordinate
     */
    @JvmStatic
    fun toSectionRelativeCoordinate(xyz: Int): Int {
        return xyz and 0xF
    }
}