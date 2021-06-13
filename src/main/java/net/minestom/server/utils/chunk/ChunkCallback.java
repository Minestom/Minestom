package net.minestom.server.utils.chunk;

import net.minestom.server.world.Chunk;

import java.util.function.Consumer;

public interface ChunkCallback extends Consumer<Chunk> {
}
