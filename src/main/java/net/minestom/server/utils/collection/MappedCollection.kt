package net.minestom.server.utils.collection

import java.util.AbstractMap.SimpleImmutableEntry
import java.util.function.BiConsumer
import java.util.function.IntFunction
import java.lang.IndexOutOfBoundsException
import java.util.concurrent.atomic.AtomicReference
import net.minestom.server.utils.collection.MappedCollection
import org.jetbrains.annotations.ApiStatus
import java.lang.UnsupportedOperationException
import java.util.function.Function

@ApiStatus.Internal
class MappedCollection<O, R> : MutableCollection<R> {
    override fun size(): Int {
        return original.size
    }

    override fun isEmpty(): Boolean {
        return original.isEmpty()
    }

    override operator fun contains(o: Any): Boolean {
        for (entry in original) {
            if (mapper.apply(entry) == o) return true
        }
        return false
    }

    override fun iterator(): MutableIterator<R> {
        val iterator: Iterator<O> = original.iterator()
        return object : MutableIterator<R> {
            override fun hasNext(): Boolean {
                return iterator.hasNext()
            }

            override fun next(): R {
                return mapper.apply(iterator.next())
            }
        }
    }

    override fun toArray(): Array<Any> {
        // TODO
        throw UnsupportedOperationException("Unsupported array object")
    }

    override fun <T> toArray(a: Array<T>): Array<T> {
        // TODO
        throw UnsupportedOperationException("Unsupported array generic")
    }

    override fun containsAll(c: Collection<*>): Boolean {
        if (c.size > original.size) return false
        for (entry in c) {
            if (!contains(entry)) return false
        }
        return true
    }

    override fun add(t: R): Boolean {
        throw UnsupportedOperationException("Unmodifiable collection")
    }

    override fun remove(o: Any): Boolean {
        throw UnsupportedOperationException("Unmodifiable collection")
    }

    override fun addAll(c: Collection<R>): Boolean {
        throw UnsupportedOperationException("Unmodifiable collection")
    }

    override fun removeAll(c: Collection<*>): Boolean {
        throw UnsupportedOperationException("Unmodifiable collection")
    }

    override fun retainAll(c: Collection<*>): Boolean {
        throw UnsupportedOperationException("Unmodifiable collection")
    }

    override fun clear() {
        throw UnsupportedOperationException("Unmodifiable collection")
    }

    companion object {
        @JvmStatic
        fun <O : AtomicReference<R>?, R> plainReferences(original: Collection<O>): MappedCollection<O, R> {
            return MappedCollection(original, Function { obj: O -> obj!!.plain })
        }
    }
}