package net.minestom.server.utils.collection

import java.util.AbstractMap.SimpleImmutableEntry
import java.util.function.BiConsumer
import java.util.function.IntFunction
import java.lang.IndexOutOfBoundsException
import java.util.concurrent.atomic.AtomicReference
import net.minestom.server.utils.collection.MappedCollection
import org.jetbrains.annotations.ApiStatus
import java.util.*
import java.util.function.Predicate
import java.util.stream.Stream

@ApiStatus.Internal
class MergedMap<K, V>(first: Map<K, V>, second: Map<K, V>) : AbstractMap<K?, V?>() {
    private val first: Map<K, V>
    private val second: Map<K, V>

    // mandatory methods
    val entrySet: Set<Map.Entry<K, V>> = object : AbstractSet<Map.Entry<K, V>?>() {
        override fun iterator(): MutableIterator<Map.Entry<K, V>> {
            return stream().iterator()
        }

        override fun size(): Int {
            return stream().count().toInt()
        }

        override fun stream(): Stream<Map.Entry<K, V>?> {
            return Stream.concat(first.entries.stream(), secondStream())
                .map { (key, value): Map.Entry<K, V> ->
                    SimpleImmutableEntry(
                        key, value
                    )
                }
        }

        override fun parallelStream(): Stream<Map.Entry<K, V>?> {
            return stream().parallel()
        }

        override fun spliterator(): Spliterator<Map.Entry<K, V>?> {
            return stream().spliterator()
        }
    }

    init {
        this.first = Objects.requireNonNull(first)
        this.second = Objects.requireNonNull(second)
    }

    fun secondStream(): Stream<Map.Entry<K, V>> {
        return second.entries.stream().filter { (key): Map.Entry<K, V> -> !first.containsKey(key) }
    }

    override fun entrySet(): Set<Map.Entry<K, V>> {
        return entrySet
    }

    // optimizations
    override fun containsKey(key: Any?): Boolean {
        return first.containsKey(key) || second.containsKey(key)
    }

    override fun containsValue(value: Any?): Boolean {
        return first.containsValue(value) ||
                secondStream().anyMatch(Predicate.isEqual(value))
    }

    override operator fun get(key: Any?): V? {
        val v = first[key]
        return v ?: second[key]
    }

    override fun getOrDefault(key: Any, defaultValue: V): V? {
        val v = first.get(key)
        return v ?: second.getOrDefault(key, defaultValue)
    }

    override fun forEach(action: BiConsumer<in K, in V>) {
        first.forEach(action)
        second.forEach { (k: K, v: V) -> if (!first.containsKey(k)) action.accept(k, v) }
    }
}