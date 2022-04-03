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

/**
 * Represents an element which can write to a [BinaryWriter].
 */
interface Writeable {
    /**
     * Writes into a [BinaryWriter].
     *
     * @param writer the writer to write to
     */
    fun write(writer: BinaryWriter)
}