package net.minestom.server.utils.collection

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import java.util.AbstractMap.SimpleImmutableEntry
import java.util.function.BiConsumer
import java.util.function.IntFunction
import java.lang.IndexOutOfBoundsException
import java.util.concurrent.atomic.AtomicReference
import net.minestom.server.utils.collection.MappedCollection
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.annotations.Contract

@ApiStatus.Internal
class IndexMap<K> {
    private val write = Object2IntOpenHashMap<K>()
    private var read = copy()
    private var lastIndex = 0

    init {
        write.defaultReturnValue(-1)
    }

    @Contract(pure = true)
    operator fun get(key: K): Int {
        var index = read.getInt(key)
        if (index == -1) {
            synchronized(write) {
                val write = write
                index = write.getInt(key)
                if (index == -1) {
                    write[key] = lastIndex++.also { index = it }
                    read = copy()
                }
            }
        }
        return index
    }

    private fun copy(): Object2IntOpenHashMap<K> {
        val map = Object2IntOpenHashMap(write)
        map.defaultReturnValue(-1)
        return map
    }
}