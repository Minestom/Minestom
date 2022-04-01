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
import net.minestom.server.network.socket.Server
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
import net.minestom.server.utils.cache.LocalCache
import org.jetbrains.annotations.ApiStatus
import java.lang.ref.Cleaner
import java.lang.ref.SoftReference
import java.nio.ByteBuffer

@ApiStatus.Internal
@ApiStatus.Experimental
object PooledBuffers {
    private val POOLED_BUFFERS: MessagePassingQueue<SoftReference<BinaryBuffer?>> = MpmcUnboundedXaddArrayQueue(1024)
    private val BUFFER_SIZE = Integer.getInteger("minestom.pooled-buffer-size", 262143)
    private val CLEANER = Cleaner.create()
    private val PACKET_BUFFER = LocalCache.ofBuffer(Server.MAX_PACKET_SIZE)
    private val LOCAL_BUFFER = LocalCache.ofBuffer(Server.MAX_PACKET_SIZE)

    /**
     * Thread local buffer containing raw packet stream.
     */
    fun packetBuffer(): ByteBuffer {
        return PACKET_BUFFER.get().clear()
    }

    /**
     * Thread local buffer targeted at very small scope operations (encryption, compression, ...).
     */
    fun tempBuffer(): ByteBuffer {
        return LOCAL_BUFFER.get().clear()
    }

    fun get(): BinaryBuffer {
        var buffer: BinaryBuffer
        var ref: SoftReference<BinaryBuffer>
        while (POOLED_BUFFERS.relaxedPoll().also { ref = it } != null) {
            if (ref.get().also { buffer = it!! } != null) return buffer
        }
        return BinaryBuffer.Companion.ofSize(BUFFER_SIZE)
    }

    fun add(buffer: BinaryBuffer) {
        POOLED_BUFFERS.relaxedOffer(SoftReference(buffer.clear()))
    }

    fun clear() {
        POOLED_BUFFERS.clear()
    }

    fun count(): Int {
        return POOLED_BUFFERS.size()
    }

    fun bufferSize(): Int {
        return BUFFER_SIZE
    }

    fun registerBuffer(ref: Any?, buffer: AtomicReference<BinaryBuffer?>?) {
        CLEANER.register(ref, BufferRefCleaner(buffer))
    }

    fun registerBuffer(ref: Any?, buffer: BinaryBuffer?) {
        CLEANER.register(ref, BufferCleaner(buffer))
    }

    fun registerBuffers(ref: Any?, buffers: Collection<BinaryBuffer?>?) {
        CLEANER.register(ref, BuffersCleaner(buffers))
    }

    private inner class BufferRefCleaner : Runnable {
        override fun run() {
            add(bufferRef.get())
        }
    }

    private inner class BufferCleaner : Runnable {
        override fun run() {
            add(buffer)
        }
    }

    private inner class BuffersCleaner : Runnable {
        override fun run() {
            if (buffers.isEmpty()) return
            for (buffer in buffers) {
                add(buffer)
            }
        }
    }
}