package net.minestom.server.utils.chunk

import net.minestom.server.utils.chunk.ChunkUtils
import net.minestom.server.utils.chunk.ChunkCallback
import java.util.concurrent.CompletableFuture
import java.lang.Void
import java.util.concurrent.atomic.AtomicInteger
import net.minestom.server.utils.callback.OptionalCallback
import java.lang.IllegalArgumentException
import net.minestom.server.utils.function.IntegerBiConsumer
import net.minestom.server.coordinate.Vec
import net.minestom.server.instance.Chunk
import java.util.function.Consumer

interface ChunkCallback : Consumer<Chunk?>