package net.minestom.server.utils

import org.jetbrains.annotations.ApiStatus
import java.nio.ByteBuffer
import java.util.*

@ApiStatus.Internal
object Utils {
    @JvmStatic
    fun getVarIntSize(input: Int): Int {
        return if (input and -0x80 == 0) 1 else if (input and -0x4000 == 0) 2 else if (input and -0x200000 == 0) 3 else if (input and -0x10000000 == 0) 4 else 5
    }

    @JvmStatic
    fun writeVarInt(buf: ByteBuffer, value: Int) {
        if (value and (-0x1 shl 7) == 0) {
            buf.put(value.toByte())
        } else if (value and (-0x1 shl 14) == 0) {
            buf.putShort((value and 0x7F or 0x80 shl 8 or (value ushr 7)).toShort())
        } else if (value and (-0x1 shl 21) == 0) {
            buf.put((value and 0x7F or 0x80).toByte())
            buf.put((value ushr 7 and 0x7F or 0x80).toByte())
            buf.put((value ushr 14).toByte())
        } else if (value and (-0x1 shl 28) == 0) {
            buf.putInt(
                value and 0x7F or 0x80 shl 24 or (value ushr 7 and 0x7F or 0x80 shl 16)
                        or (value ushr 14 and 0x7F or 0x80 shl 8) or (value ushr 21)
            )
        } else {
            buf.putInt(
                value and 0x7F or 0x80 shl 24 or (value ushr 7 and 0x7F or 0x80 shl 16
                        ) or (value ushr 14 and 0x7F or 0x80 shl 8) or (value ushr 21 and 0x7F or 0x80)
            )
            buf.put((value ushr 28).toByte())
        }
    }

    @JvmStatic
    fun writeVarIntHeader(buffer: ByteBuffer, startIndex: Int, value: Int) {
        buffer.put(startIndex, (value and 0x7F or 0x80).toByte())
        buffer.put(startIndex + 1, (value ushr 7 and 0x7F or 0x80).toByte())
        buffer.put(startIndex + 2, (value ushr 14).toByte())
    }

    @JvmStatic
    fun writeEmptyVarIntHeader(buffer: ByteBuffer): Int {
        val index = buffer.position()
        buffer.position(index + 3) // Skip 3 bytes
        return index
    }

    @JvmStatic
    fun readVarInt(buf: ByteBuffer): Int {
        // https://github.com/jvm-profiling-tools/async-profiler/blob/a38a375dc62b31a8109f3af97366a307abb0fe6f/src/converter/one/jfr/JfrReader.java#L393
        var result = 0
        var shift = 0
        while (true) {
            val b = buf.get()
            result = result or (b and 0x7f shl shift)
            if (b >= 0) {
                return result
            }
            shift += 7
        }
    }

    fun readVarLong(buf: ByteBuffer): Long {
        // https://github.com/jvm-profiling-tools/async-profiler/blob/a38a375dc62b31a8109f3af97366a307abb0fe6f/src/converter/one/jfr/JfrReader.java#L404
        var result: Long = 0
        var shift = 0
        while (shift < 56) {
            val b = buf.get()
            result = result or (b and 0x7fL shl shift)
            if (b >= 0) {
                return result
            }
            shift += 7
        }
        return result or (buf.get() and 0xffL) shl 56
    }

    fun writeVarLong(buffer: ByteBuffer, value: Long) {
        var value = value
        do {
            var temp = (value and 127).toByte()
            value = value ushr 7
            if (value != 0L) {
                temp = temp or 128
            }
            buffer.put(temp)
        } while (value != 0L)
    }

    @JvmStatic
    fun uuidToIntArray(uuid: UUID): IntArray {
        val array = IntArray(4)
        val uuidMost = uuid.mostSignificantBits
        val uuidLeast = uuid.leastSignificantBits
        array[0] = (uuidMost shr 32).toInt()
        array[1] = uuidMost.toInt()
        array[2] = (uuidLeast shr 32).toInt()
        array[3] = uuidLeast.toInt()
        return array
    }

    @JvmStatic
    fun intArrayToUuid(array: IntArray): UUID {
        val uuidMost = array[0].toLong() shl 32 or array[1].toLong() and 0xFFFFFFFFL
        val uuidLeast = array[2].toLong() shl 32 or array[3].toLong() and 0xFFFFFFFFL
        return UUID(uuidMost, uuidLeast)
    }

    private val MAGIC = intArrayOf(
        -1, -1, 0, Int.MIN_VALUE, 0, 0, 1431655765, 1431655765, 0, Int.MIN_VALUE,
        0, 1, 858993459, 858993459, 0, 715827882, 715827882, 0, 613566756, 613566756,
        0, Int.MIN_VALUE, 0, 2, 477218588, 477218588, 0, 429496729, 429496729, 0,
        390451572, 390451572, 0, 357913941, 357913941, 0, 330382099, 330382099, 0, 306783378,
        306783378, 0, 286331153, 286331153, 0, Int.MIN_VALUE, 0, 3, 252645135, 252645135,
        0, 238609294, 238609294, 0, 226050910, 226050910, 0, 214748364, 214748364, 0,
        204522252, 204522252, 0, 195225786, 195225786, 0, 186737708, 186737708, 0, 178956970,
        178956970, 0, 171798691, 171798691, 0, 165191049, 165191049, 0, 159072862, 159072862,
        0, 153391689, 153391689, 0, 148102320, 148102320, 0, 143165576, 143165576, 0,
        138547332, 138547332, 0, Int.MIN_VALUE, 0, 4, 130150524, 130150524, 0, 126322567,
        126322567, 0, 122713351, 122713351, 0, 119304647, 119304647, 0, 116080197, 116080197,
        0, 113025455, 113025455, 0, 110127366, 110127366, 0, 107374182, 107374182, 0,
        104755299, 104755299, 0, 102261126, 102261126, 0, 99882960, 99882960, 0, 97612893,
        97612893, 0, 95443717, 95443717, 0, 93368854, 93368854, 0, 91382282, 91382282,
        0, 89478485, 89478485, 0, 87652393, 87652393, 0, 85899345, 85899345, 0,
        84215045, 84215045, 0, 82595524, 82595524, 0, 81037118, 81037118, 0, 79536431,
        79536431, 0, 78090314, 78090314, 0, 76695844, 76695844, 0, 75350303, 75350303,
        0, 74051160, 74051160, 0, 72796055, 72796055, 0, 71582788, 71582788, 0,
        70409299, 70409299, 0, 69273666, 69273666, 0, 68174084, 68174084, 0, Int.MIN_VALUE,
        0, 5
    )

    @JvmStatic
    fun encodeBlocks(blocks: IntArray, bitsPerEntry: Int): LongArray {
        val maxEntryValue = (1L shl bitsPerEntry) - 1
        val valuesPerLong = (64 / bitsPerEntry).toChar()
        val magicIndex = 3 * (valuesPerLong.toInt() - 1)
        val divideMul = Integer.toUnsignedLong(MAGIC[magicIndex])
        val divideAdd = Integer.toUnsignedLong(MAGIC[magicIndex + 1])
        val divideShift = MAGIC[magicIndex + 2]
        val size = (blocks.size + valuesPerLong.toInt() - 1) / valuesPerLong.toInt()
        val data = LongArray(size)
        for (i in blocks.indices) {
            val value = blocks[i].toLong()
            val cellIndex = (i * divideMul + divideAdd shr 32L shr divideShift).toInt()
            val bitIndex = (i - cellIndex * valuesPerLong.toInt()) * bitsPerEntry
            data[cellIndex] =
                data[cellIndex] and (maxEntryValue shl bitIndex).inv() or (value and maxEntryValue) shl bitIndex
        }
        return data
    }
}