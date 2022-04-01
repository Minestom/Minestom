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
import net.minestom.server.utils.validate.Check
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.function.Function
import java.util.function.Supplier

/**
 * Class used to read from a byte array.
 *
 *
 * WARNING: not thread-safe.
 */
class BinaryReader(val buffer: ByteBuffer) : InputStream() {
    private var nbtReader: NBTReader? = null

    constructor(bytes: ByteArray?) : this(ByteBuffer.wrap(bytes)) {}

    fun readVarInt(): Int {
        return Utils.readVarInt(buffer)
    }

    fun readVarLong(): Long {
        return Utils.readVarLong(buffer)
    }

    fun readBoolean(): Boolean {
        return buffer.get().toInt() == 1
    }

    fun readByte(): Byte {
        return buffer.get()
    }

    fun readShort(): Short {
        return buffer.short
    }

    fun readChar(): Char {
        return buffer.char
    }

    fun readUnsignedShort(): Int {
        return buffer.short and 0xFFFF
    }

    /**
     * Same as readInt
     */
    fun readInteger(): Int {
        return buffer.int
    }

    /**
     * Same as readInteger, created for parity with BinaryWriter
     */
    fun readInt(): Int {
        return buffer.int
    }

    fun readLong(): Long {
        return buffer.long
    }

    fun readFloat(): Float {
        return buffer.float
    }

    fun readDouble(): Double {
        return buffer.double
    }

    /**
     * Reads a string size by a var-int.
     *
     *
     * If the string length is higher than `maxLength`,
     * the code throws an exception and the string bytes are not read.
     *
     * @param maxLength the max length of the string
     * @return the string
     * @throws IllegalStateException if the string length is invalid or higher than `maxLength`
     */
    @JvmOverloads
    fun readSizedString(maxLength: Int = Int.MAX_VALUE): String {
        val length = readVarInt()
        val bytes = ByteArray(length)
        try {
            buffer[bytes]
        } catch (e: BufferUnderflowException) {
            throw RuntimeException("Could not read " + length + ", " + buffer.remaining() + " remaining.")
        }
        val str = String(bytes, StandardCharsets.UTF_8)
        Check.stateCondition(
            str.length > maxLength,
            "String length ({0}) was higher than the max length of {1}", length, maxLength
        )
        return str
    }

    fun readBytes(length: Int): ByteArray {
        val bytes = ByteArray(length)
        buffer[bytes]
        return bytes
    }

    fun readByteArray(): ByteArray {
        return readBytes(readVarInt())
    }

    @JvmOverloads
    fun readSizedStringArray(maxLength: Int = Int.MAX_VALUE): Array<String?> {
        val size = readVarInt()
        val strings = arrayOfNulls<String>(size)
        for (i in 0 until size) {
            strings[i] = readSizedString(maxLength)
        }
        return strings
    }

    fun readVarIntArray(): IntArray {
        val size = readVarInt()
        val array = IntArray(size)
        for (i in 0 until size) {
            array[i] = readVarInt()
        }
        return array
    }

    fun readVarLongArray(): LongArray {
        val size = readVarInt()
        val array = LongArray(size)
        for (i in 0 until size) {
            array[i] = readVarLong()
        }
        return array
    }

    fun readLongArray(): LongArray {
        val size = readVarInt()
        val array = LongArray(size)
        for (i in 0 until size) {
            array[i] = readLong()
        }
        return array
    }

    fun readRemainingBytes(): ByteArray {
        return readBytes(available())
    }

    fun readBlockPosition(): Point {
        return SerializerUtils.longToBlockPosition(buffer.long)
    }

    fun readUuid(): UUID {
        return UUID(readLong(), readLong())
    }

    /**
     * Tries to read an [ItemStack].
     *
     * @return the read item
     * @throws NullPointerException if the item could not get read
     */
    fun readItemStack(): ItemStack {
        val itemStack = NBTUtils.readItemStack(this)
        Check.notNull(itemStack, "#readSlot returned null, probably because the buffer was corrupted")
        return itemStack
    }

    @JvmOverloads
    fun readComponent(maxLength: Int = Int.MAX_VALUE): Component {
        val jsonObject = readSizedString(maxLength)
        return GsonComponentSerializer.gson().deserialize(jsonObject)
    }

    /**
     * Creates a new object from the given supplier and calls its [Readable.read] method with this reader.
     *
     * @param supplier supplier to create new instances of your object
     * @param <T>      the readable object type
     * @return the read object
    </T> */
    fun <T : Readable?> read(supplier: Supplier<T>): T {
        val result = supplier.get()
        result!!.read(this)
        return result
    }

    /**
     * Reads the length of the array to read as a varint, creates the array to contain the readable objects and call
     * their respective [Readable.read] methods.
     *
     * @param supplier supplier to create new instances of your object
     * @param <T>      the readable object type
     * @return the read objects
    </T> */
    fun <T : Readable?> readArray(supplier: Supplier<T>): Array<T?> {
        val result = arrayOfNulls<Readable>(readVarInt())
        for (i in result.indices) {
            result[i] = supplier.get()
            result[i]!!.read(this)
        }
        return result as Array<T?>
    }

    fun <T> readVarIntList(supplier: Function<BinaryReader, T>): List<T> {
        return readList(readVarInt(), supplier)
    }

    fun <T> readByteList(supplier: Function<BinaryReader, T>): List<T> {
        return readList(readByte().toInt(), supplier)
    }

    private fun <T> readList(length: Int, supplier: Function<BinaryReader, T>): List<T> {
        val list: MutableList<T> = ArrayList(length)
        for (i in 0 until length) {
            list.add(supplier.apply(this))
        }
        return list
    }

    override fun read(): Int {
        return readByte() and 0xFF
    }

    override fun available(): Int {
        return buffer.remaining()
    }

    fun readTag(): NBT {
        var reader = nbtReader
        if (reader == null) {
            reader = NBTReader(this, CompressedProcesser.NONE)
            nbtReader = reader
        }
        return try {
            reader.read()
        } catch (e: IOException) {
            MinecraftServer.getExceptionManager().handleException(e)
            throw RuntimeException()
        } catch (e: NBTException) {
            MinecraftServer.getExceptionManager().handleException(e)
            throw RuntimeException()
        }
    }

    /**
     * Records the current position, runs the given Runnable, and then returns the bytes between the position before
     * running the runnable and the position after.
     * Can be used to extract a subsection of this reader's buffer with complex data
     *
     * @param extractor the extraction code, simply call the reader's read* methods here.
     */
    fun extractBytes(extractor: Runnable): ByteArray {
        val startingPosition = buffer.position()
        extractor.run()
        val endingPosition = buffer.position()
        val output = ByteArray(endingPosition - startingPosition)
        buffer[output, 0, output.size]
        //buffer.get(startingPosition, output);
        return output
    }
}