package net.minestom.server.utils.collection

import java.util.AbstractMap.SimpleImmutableEntry
import java.util.function.BiConsumer
import java.util.function.IntFunction
import java.lang.IndexOutOfBoundsException
import java.util.concurrent.atomic.AtomicReference
import net.minestom.server.utils.collection.MappedCollection
import org.jetbrains.annotations.ApiStatus
import java.util.AbstractList

@ApiStatus.Internal
class IntMappedArray<R>(private val elements: IntArray, private val function: IntFunction<R>) : AbstractList<R>() {
    override fun get(index: Int): R {
        val elements = elements
        if (index < 0 || index >= elements.size) throw IndexOutOfBoundsException("Index " + index + " is out of bounds for length " + elements.size)
        return function.apply(elements[index])
    }

    override fun size(): Int {
        return elements.size
    }
}