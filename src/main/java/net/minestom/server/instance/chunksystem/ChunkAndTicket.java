package net.minestom.server.instance.chunksystem;

import net.minestom.server.instance.Chunk;
import org.jetbrains.annotations.NotNull;

public record ChunkAndTicket(@NotNull ChunkTicket chunkTicket, @NotNull Chunk chunk) {
}
