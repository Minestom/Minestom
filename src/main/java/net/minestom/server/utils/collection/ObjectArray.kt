package net.minestom.server.utils.collection

import java.util.AbstractMap.SimpleImmutableEntry
import java.util.function.BiConsumer
import java.util.function.IntFunction
import java.lang.IndexOutOfBoundsException
import java.util.concurrent.atomic.AtomicReference
import net.minestom.server.utils.collection.MappedCollection
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.annotations.UnknownNullability
import java.util.*

/**
 * Represents an array which will be resized to the highest required index.
 *
 * @param <T> the type of the array
</T> */
@ApiStatus.Internal
class ObjectArray<T> @JvmOverloads constructor(size: Int = 0) {
    private var array: Array<T>
    private var max = 0

    init {
        array = arrayOfNulls<Any>(size) as Array<T?>
    }

    operator fun set(index: Int, `object`: T?) {
        var array: Array<T?> = array
        if (index >= array.size) {
            val newLength = index * 2 + 1
            array = Arrays.copyOf(array, newLength)
            this.array = array
        }
        array[index] = `object`
        max = Math.max(max, index)
    }

    operator fun get(index: Int): @UnknownNullability T? {
        val array = array
        return if (index < array.size) array[index] else null
    }

    fun trim() {
        array = Arrays.copyOf(array, max + 1)
    }
}