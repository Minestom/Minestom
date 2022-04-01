package net.minestom.server.utils.binary

import net.kyori.adventure.text.Component
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
import net.minestom.server.coordinate.Point
import net.minestom.server.utils.Utils
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
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.function.Consumer

/**
 * Class used to write to a byte array.
 * WARNING: not thread-safe.
 */
class BinaryWriter : OutputStream {
    /**
     * Gets the raw buffer used by this binary writer.
     *
     * @return the raw buffer
     */
    /**
     * Changes the buffer used by this binary writer.
     *
     * @param buffer the new buffer used by this binary writer
     */
    var buffer: ByteBuffer
    private var nbtWriter // Lazily initialized
            : NBTWriter? = null
    private val resizable: Boolean

    private constructor(buffer: ByteBuffer, resizable: Boolean) {
        this.buffer = buffer
        this.resizable = resizable
    }

    constructor(buffer: ByteBuffer) {
        this.buffer = buffer
        resizable = true
    }

    @JvmOverloads
    constructor(initialCapacity: Int = 255) : this(ByteBuffer.allocate(initialCapacity)) {
    }

    protected fun ensureSize(length: Int) {
        if (!resizable) return
        val position = buffer.position()
        if (position + length >= buffer.limit()) {
            val newLength = (position + length) * 4
            val copy = if (buffer.isDirect) ByteBuffer.allocateDirect(newLength) else ByteBuffer.allocate(newLength)
            copy.put(buffer.flip())
            buffer = copy
        }
    }

    /**
     * Writes a component to the buffer as a sized string.
     *
     * @param component the component
     */
    fun writeComponent(component: Component) {
        writeSizedString(GsonComponentSerializer.gson().serialize(component))
    }

    /**
     * Writes a single byte to the buffer.
     *
     * @param b the byte to write
     */
    fun writeByte(b: Byte) {
        ensureSize(java.lang.Byte.BYTES)
        buffer.put(b)
    }

    /**
     * Writes a single boolean to the buffer.
     *
     * @param b the boolean to write
     */
    fun writeBoolean(b: Boolean) {
        writeByte((if (b) 1 else 0).toByte())
    }

    /**
     * Writes a single char to the buffer.
     *
     * @param c the char to write
     */
    fun writeChar(c: Char) {
        ensureSize(Character.BYTES)
        buffer.putChar(c)
    }

    /**
     * Writes a single short to the buffer.
     *
     * @param s the short to write
     */
    fun writeShort(s: Short) {
        ensureSize(java.lang.Short.BYTES)
        buffer.putShort(s)
    }

    /**
     * Writes a single int to the buffer.
     *
     * @param i the int to write
     */
    fun writeInt(i: Int) {
        ensureSize(Integer.BYTES)
        buffer.putInt(i)
    }

    /**
     * Writes a single long to the buffer.
     *
     * @param l the long to write
     */
    fun writeLong(l: Long) {
        ensureSize(java.lang.Long.BYTES)
        buffer.putLong(l)
    }

    /**
     * Writes a single float to the buffer.
     *
     * @param f the float to write
     */
    fun writeFloat(f: Float) {
        ensureSize(java.lang.Float.BYTES)
        buffer.putFloat(f)
    }

    /**
     * Writes a single double to the buffer.
     *
     * @param d the double to write
     */
    fun writeDouble(d: Double) {
        ensureSize(java.lang.Double.BYTES)
        buffer.putDouble(d)
    }

    /**
     * Writes a single var-int to the buffer.
     *
     * @param i the int to write
     */
    fun writeVarInt(i: Int) {
        ensureSize(5)
        Utils.writeVarInt(buffer, i)
    }

    /**
     * Writes a single var-long to the buffer.
     *
     * @param l the long to write
     */
    fun writeVarLong(l: Long) {
        ensureSize(10)
        Utils.writeVarLong(buffer, l)
    }

    /**
     * Writes a string to the buffer.
     *
     *
     * The size is a var-int type.
     *
     * @param string the string to write
     */
    fun writeSizedString(string: String) {
        val bytes = string.toByteArray(StandardCharsets.UTF_8)
        writeVarInt(bytes.size)
        writeBytes(bytes)
    }

    /**
     * Writes a null terminated string to the buffer. This method adds the null character
     * to the end of the string before writing.
     *
     * @param string  the string to write
     * @param charset the charset to encode in
     */
    fun writeNullTerminatedString(string: String, charset: Charset) {
        val bytes = (string + '\u0000').toByteArray(charset)
        writeBytes(bytes)
    }

    /**
     * Writes a var-int array to the buffer.
     *
     *
     * It is sized by another var-int at the beginning.
     *
     * @param array the integers to write
     */
    fun writeVarIntArray(array: IntArray?) {
        if (array == null) {
            writeVarInt(0)
            return
        }
        writeVarInt(array.size)
        for (element in array) {
            writeVarInt(element)
        }
    }

    fun writeVarLongArray(array: LongArray?) {
        if (array == null) {
            writeVarInt(0)
            return
        }
        writeVarInt(array.size)
        for (element in array) {
            writeVarLong(element)
        }
    }

    fun writeLongArray(array: LongArray?) {
        if (array == null) {
            writeVarInt(0)
            return
        }
        writeVarInt(array.size)
        for (element in array) {
            writeLong(element)
        }
    }

    fun writeByteArray(array: ByteArray?) {
        if (array == null) {
            writeVarInt(0)
            return
        }
        writeVarInt(array.size)
        writeBytes(array)
    }

    /**
     * Writes a byte array.
     *
     *
     * WARNING: it doesn't write the length of `bytes`.
     *
     * @param bytes the byte array to write
     */
    fun writeBytes(bytes: ByteArray) {
        if (bytes.size == 0) return
        ensureSize(bytes.size)
        buffer.put(bytes)
    }

    /**
     * Writes a string to the buffer.
     *
     *
     * The array is sized by a var-int and all strings are wrote using [.writeSizedString].
     *
     * @param array the string array to write
     */
    fun writeStringArray(array: Array<String>?) {
        if (array == null) {
            writeVarInt(0)
            return
        }
        writeVarInt(array.size)
        for (element in array) {
            writeSizedString(element)
        }
    }

    /**
     * Writes an [UUID].
     * It is done by writing both long, the most and least significant bits.
     *
     * @param uuid the [UUID] to write
     */
    fun writeUuid(uuid: UUID) {
        writeLong(uuid.mostSignificantBits)
        writeLong(uuid.leastSignificantBits)
    }

    fun writeBlockPosition(point: Point) {
        writeBlockPosition(point.blockX(), point.blockY(), point.blockZ())
    }

    fun writeBlockPosition(x: Int, y: Int, z: Int) {
        writeLong(SerializerUtils.positionToLong(x, y, z))
    }

    fun writeItemStack(itemStack: ItemStack) {
        if (itemStack.isAir) {
            writeBoolean(false)
        } else {
            writeBoolean(true)
            writeVarInt(itemStack.material.id())
            writeByte(itemStack.amount.toByte())
            write(itemStack.meta)
        }
    }

    fun writeNBT(name: String, tag: NBT) {
        if (nbtWriter == null) {
            nbtWriter = NBTWriter(this, CompressedProcesser.NONE)
        }
        try {
            nbtWriter!!.writeNamed(name, tag)
        } catch (e: IOException) {
            // should not throw, as nbtWriter points to this PacketWriter
            MinecraftServer.getExceptionManager().handleException(e)
        }
    }

    /**
     * Writes the given writeable object into this writer.
     *
     * @param writeable the object to write
     */
    fun write(writeable: Writeable) {
        writeable.write(this)
    }

    fun write(buffer: ByteBuffer) {
        ensureSize(buffer.remaining())
        this.buffer.put(buffer)
    }

    fun write(writer: BinaryWriter) {
        write(writer.buffer)
    }

    /**
     * Writes an array of writeable objects to this writer. Will prepend the binary stream with a var int to denote the
     * length of the array.
     *
     * @param writeables the array of writeables to write
     */
    fun writeArray(writeables: Array<Writeable>) {
        writeVarInt(writeables.size)
        for (w in writeables) {
            write(w)
        }
    }

    fun <T> writeVarIntList(list: Collection<T>, consumer: BiConsumer<BinaryWriter, T>) {
        writeVarInt(list.size)
        writeList(list, consumer)
    }

    fun <T> writeByteList(list: Collection<T>, consumer: BiConsumer<BinaryWriter, T>) {
        writeByte(list.size.toByte())
        writeList(list, consumer)
    }

    private fun <T> writeList(list: Collection<T>, consumer: BiConsumer<BinaryWriter, T>) {
        for (t in list) consumer.accept(this, t)
    }

    /**
     * Converts the internal buffer to a byte array.
     *
     * @return the byte array containing all the [BinaryWriter] data
     */
    fun toByteArray(): ByteArray {
        buffer.flip()
        val bytes = ByteArray(buffer.remaining())
        buffer[bytes]
        return bytes
    }

    /**
     * Adds a [BinaryWriter]'s [ByteBuffer] at the beginning of this writer.
     *
     * @param headerWriter the [BinaryWriter] to add at the beginning
     */
    fun writeAtStart(headerWriter: BinaryWriter) {
        // Get the buffer of the header
        val headerBuf = headerWriter.buffer
        // Merge both the headerBuf and this buffer
        val finalBuffer = concat(headerBuf, buffer)
        // Change the buffer used by this writer
        buffer = finalBuffer
    }

    /**
     * Adds a [BinaryWriter]'s [ByteBuffer] at the end of this writer.
     *
     * @param footerWriter the [BinaryWriter] to add at the end
     */
    fun writeAtEnd(footerWriter: BinaryWriter) {
        // Get the buffer of the footer
        val footerBuf = footerWriter.buffer
        // Merge both this buffer and the footerBuf
        val finalBuffer = concat(buffer, footerBuf)
        // Change the buffer used by this writer
        buffer = finalBuffer
    }

    override fun write(b: Int) {
        writeByte(b.toByte())
    }

    fun writeUnsignedShort(yourShort: Int) {
        // FIXME unsigned
        ensureSize(java.lang.Short.BYTES)
        buffer.putShort((yourShort and 0xFFFF).toShort())
    }

    companion object {
        @ApiStatus.Experimental
        fun view(buffer: ByteBuffer): BinaryWriter {
            return BinaryWriter(buffer, false)
        }

        fun concat(vararg buffers: ByteBuffer): ByteBuffer {
            val combined = ByteBuffer.allocate(Arrays.stream(buffers).mapToInt { obj: ByteBuffer -> obj.remaining() }
                .sum())
            Arrays.stream(buffers).forEach { b: ByteBuffer -> combined.put(b.duplicate()) }
            return combined
        }

        /**
         * Returns a byte[] with the contents written via BinaryWriter
         */
        fun makeArray(writing: Consumer<BinaryWriter?>): ByteArray {
            val writer = BinaryWriter()
            writing.accept(writer)
            return writer.toByteArray()
        }
    }
}