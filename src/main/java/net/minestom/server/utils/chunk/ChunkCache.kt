package net.minestom.server.utils.chunk

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
import net.minestom.server.instance.block.Block
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.annotations.UnknownNullability

@ApiStatus.Internal
class ChunkCache @JvmOverloads constructor(
    private val instance: Instance, private var chunk: Chunk?,
    private val defaultBlock: Block = Block.AIR
) : Block.Getter {
    override fun getBlock(x: Int, y: Int, z: Int, condition: Block.Getter.Condition): @UnknownNullability Block? {
        var chunk = chunk
        val chunkX = ChunkUtils.getChunkCoordinate(x)
        val chunkZ = ChunkUtils.getChunkCoordinate(z)
        if (chunk == null || chunk.chunkX != chunkX || chunk.chunkZ != chunkZ) {
            chunk = instance.getChunk(chunkX, chunkZ)
            this.chunk = chunk
        }
        return if (chunk != null) {
            synchronized(chunk) { return chunk.getBlock(x, y, z, condition) }
        } else defaultBlock
    }
}