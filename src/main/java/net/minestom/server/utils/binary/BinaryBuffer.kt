package net.minestom.server.utils.binary

import org.jglrxavpok.hephaistos.nbt.NBTReader.read
import org.jglrxavpok.hephaistos.nbt.NBTWriter.writeNamed
import net.minestom.server.utils.binary.BinaryReader
import net.minestom.server.utils.binary.BinaryWriter
import org.jglrxavpok.hephaistos.nbt.NBTReader
import org.jglrxavpok.hephaistos.nbt.NBTWriter
import net.minestom.server.utils.binary.BinaryBuffer
import java.lang.RuntimeException
import java.io.IOException
import java.nio.channels.WritableByteChannel
import java.nio.channels.ReadableByteChannel
import java.nio.BufferUnderflowException
import net.minestom.server.utils.SerializerUtils
import net.minestom.server.item.ItemStack
import net.minestom.server.utils.NBTUtils
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.jglrxavpok.hephaistos.nbt.NBT
import org.jglrxavpok.hephaistos.nbt.CompressedProcesser
import net.minestom.server.MinecraftServer
import org.jglrxavpok.hephaistos.nbt.NBTException
import java.lang.Runnable
import java.nio.charset.Charset
import net.minestom.server.utils.binary.Writeable
import java.util.function.BiConsumer
import java.util.function.ToIntFunction
import org.jctools.queues.MessagePassingQueue
import org.jctools.queues.MpmcUnboundedXaddArrayQueue
import net.minestom.server.utils.binary.PooledBuffers
import java.util.concurrent.atomic.AtomicReference
import net.minestom.server.utils.binary.PooledBuffers.BufferRefCleaner
import net.minestom.server.utils.binary.PooledBuffers.BufferCleaner
import net.minestom.server.utils.binary.PooledBuffers.BuffersCleaner
import org.jetbrains.annotations.ApiStatus
import java.nio.ByteBuffer

/**
 * Manages off-heap memory.
 * Not thread-safe.
 */
@ApiStatus.Internal
class BinaryBuffer private constructor(  // To become a `MemorySegment` once released
    private val nioBuffer: ByteBuffer
) {
    private val nbtReader: NBTReader? = null
    private val nbtWriter: NBTWriter? = null
    private val capacity: Int
    private var readerOffset = 0
    private var writerOffset = 0

    init {
        capacity = nioBuffer.capacity()
    }

    @JvmOverloads
    fun write(buffer: ByteBuffer, index: Int = buffer.position(), length: Int = buffer.remaining()) {
        nioBuffer.put(writerOffset, buffer, index, length)
        writerOffset += length
    }

    fun write(buffer: BinaryBuffer) {
        write(buffer.asByteBuffer(buffer.readerOffset, buffer.writerOffset - buffer.readerOffset))
    }

    fun readVarInt(): Int {
        var value = 0
        for (i in 0..4) {
            val offset = readerOffset + i
            val k = nioBuffer[offset]
            value = value or (k and 0x7F shl i * 7)
            if (k and 0x80 != 128) {
                readerOffset = offset + 1
                return value
            }
        }
        throw RuntimeException("VarInt is too big")
    }

    fun mark(): Marker {
        return Marker(readerOffset, writerOffset)
    }

    fun reset(readerOffset: Int, writerOffset: Int) {
        this.readerOffset = readerOffset
        this.writerOffset = writerOffset
    }

    fun reset(marker: Marker) {
        reset(marker.readerOffset(), marker.writerOffset())
    }

    fun canRead(size: Int): Boolean {
        return readerOffset + size <= writerOffset
    }

    fun canWrite(size: Int): Boolean {
        return writerOffset + size < capacity
    }

    fun capacity(): Int {
        return capacity
    }

    fun readerOffset(): Int {
        return readerOffset
    }

    fun readerOffset(offset: Int) {
        readerOffset = offset
    }

    fun writerOffset(): Int {
        return writerOffset
    }

    fun readableBytes(): Int {
        return writerOffset - readerOffset
    }

    fun writeBytes(bytes: ByteArray) {
        nioBuffer.put(writerOffset, bytes)
        writerOffset += bytes.size
    }

    fun readBytes(length: Int): ByteArray {
        val bytes = ByteArray(length)
        nioBuffer[readerOffset, bytes]
        readerOffset += length
        return bytes
    }

    fun readRemainingBytes(): ByteArray {
        return readBytes(readableBytes())
    }

    fun clear(): BinaryBuffer {
        readerOffset = 0
        writerOffset = 0
        nioBuffer.limit(capacity)
        return this
    }

    fun asByteBuffer(reader: Int, length: Int): ByteBuffer {
        return nioBuffer.slice(reader, length)
    }

    @ApiStatus.Internal
    fun asByteBuffer(): ByteBuffer {
        return nioBuffer
    }

    @Throws(IOException::class)
    fun writeChannel(channel: WritableByteChannel): Boolean {
        if (readerOffset == writerOffset) return true // Nothing to write
        val writeBuffer = nioBuffer.slice(readerOffset, writerOffset - readerOffset)
        val count = channel.write(writeBuffer)
        if (count == -1) {
            // EOS
            throw IOException("Disconnected")
        }
        readerOffset += count
        return writeBuffer.limit() == writeBuffer.position()
    }

    @Throws(IOException::class)
    fun readChannel(channel: ReadableByteChannel) {
        val count = channel.read(nioBuffer.slice(writerOffset, capacity - writerOffset))
        if (count == -1) {
            // EOS
            throw IOException("Disconnected")
        }
        writerOffset += count
    }

    override fun toString(): String {
        return "BinaryBuffer{" +
                "readerOffset=" + readerOffset +
                ", writerOffset=" + writerOffset +
                ", capacity=" + capacity +
                '}'
    }

    inner class Marker
    companion object {
        @ApiStatus.Internal
        fun ofSize(size: Int): BinaryBuffer {
            return BinaryBuffer(ByteBuffer.allocateDirect(size))
        }

        @ApiStatus.Internal
        fun wrap(buffer: ByteBuffer): BinaryBuffer {
            assert(buffer.isDirect)
            return BinaryBuffer(buffer)
        }

        fun copy(buffer: BinaryBuffer): BinaryBuffer {
            val size = buffer.readableBytes()
            val temp = ByteBuffer.allocateDirect(size)
                .put(buffer.asByteBuffer(0, size))
            val newBuffer = BinaryBuffer(temp)
            newBuffer.writerOffset = size
            return newBuffer
        }
    }
}